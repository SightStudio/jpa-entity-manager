package orm;

import jakarta.persistence.FlushModeType;
import orm.dsl.QueryBuilder;
import orm.dsl.QueryRunner;
import orm.dsl.holder.EntityIdHolder;
import orm.exception.NotAllowedInReadOnlyException;
import orm.life_cycle.Status;

public class SessionImpl implements EntityManager {

    private final PersistenceContext persistenceContext;
    private final EntityPersister entityPersister;
    private final EntityLoader entityLoader;
    private TransactionContext transactionContext;

    public SessionImpl(QueryRunner queryRunner) {
        this(queryRunner, new QueryBuilder(), new StatefulPersistenceContext(), FlushModeType.AUTO, new TransactionContext());
    }

    public SessionImpl(QueryRunner queryRunner, PersistenceContext persistenceContext) {
        this(queryRunner, new QueryBuilder(), persistenceContext, FlushModeType.AUTO, new TransactionContext());
    }

    public SessionImpl(QueryRunner queryRunner, QueryBuilder queryBuilder, PersistenceContext persistenceContext, FlushModeType flushModeType, TransactionContext transactionContext) {
        this.persistenceContext = persistenceContext;
        this.entityPersister = new DefaultEntityPersister(queryBuilder, queryRunner);
        this.entityLoader = new DefaultEntityLoader(queryBuilder, queryRunner);
        this.transactionContext = transactionContext;
    }

    @Override
    public <T> T find(Class<T> clazz, Object id) {

        // 엔티티가 이미 1차 캐시에 존재하는 경우
        T entityInContext = persistenceContext.getEntity(clazz, id);
        if (entityInContext != null) {
            addEntryManagedReadonlyAware(entityInContext);
            return entityInContext;
        }

        // 엔티티를 DB에서 조회해오는 경우
        EntityKey entityKey = new EntityKey(clazz, id);
        persistenceContext.addEntry(entityKey, Status.LOADING);
        T entity = entityLoader.find(clazz, id);
        if (entity != null) {
            persistenceContext.addEntity(entity);
            addEntryManagedReadonlyAware(entity);
            return entity;
        }

        // 엔티티가 DB에서 조회해도 없는 경우
        persistenceContext.removeEntry(entityKey);
        return null;
    }

    @Override
    public <T> T persist(T entity) {
        persistenceContext.addEntry(entity, Status.SAVING);
        var persistedEntity = entityPersister.persist(entity);

        persistenceContext.addEntry(persistedEntity, Status.MANAGED);
        return persistenceContext.addEntity(persistedEntity);
    }

    @Override
    public <T> T merge(T entity) {
        var idHolder = new EntityIdHolder<>(entity);

        // 1차 캐시에 존재하는지 확인 후 db도 확인 후 없으면 insert
        boolean hasEntityInContext = persistenceContext.contains(idHolder);
        if (!hasEntityInContext) {
            T loadedEntity = entityLoader.find(idHolder);
            if (loadedEntity == null) {
                return persist(entity);
            }
            return persistenceContext.addEntity(loadedEntity);
        }

        // 존재하는 경우 update
        Object databaseSnapshot = persistenceContext.getDatabaseSnapshot(idHolder, entityPersister);
        entityPersister.update(entity, databaseSnapshot);
        persistenceContext.addEntity(entity);
        return entity;
    }

    @Override
    public void detach(Object entity) {
        persistenceContext.addEntry(entity, Status.GONE);
        persistenceContext.removeEntity(entity);
    }

    @Override
    public void remove(Object entity) {
        persistenceContext.addEntry(entity, Status.DELETED);
        persistenceContext.removeEntity(entity);
        persistenceContext.addEntry(entity, Status.GONE);
    }


    private <T> void addEntryManagedReadonlyAware(T entityInContext) {
        persistenceContext.addEntry(entityInContext, Status.MANAGED);
        if (transactionContext.isReadOnly()) {
            persistenceContext.addEntry(entityInContext, Status.READ_ONLY);
        }
    }

    private void addEntryManagedReadonlyAware(EntityKey entityKey) {
        persistenceContext.addEntry(entityKey, Status.MANAGED);
        if (transactionContext.isReadOnly()) {
            persistenceContext.addEntry(entityKey, Status.READ_ONLY);
        }
    }

    private void throwIfReadOnlyNotAllowed() {
        if(transactionContext.isReadOnly()) {
            throw new NotAllowedInReadOnlyException("not allowed in read-only mode");
        }
    }
}
