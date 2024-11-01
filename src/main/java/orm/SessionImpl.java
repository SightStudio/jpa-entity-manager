package orm;

import orm.dsl.QueryBuilder;
import orm.dsl.QueryRunner;
import orm.dsl.holder.EntityIdHolder;

public class SessionImpl implements EntityManager {

    private final PersistenceContext persistenceContext;
    private final EntityPersister entityPersister;
    private final EntityLoader entityLoader;

    public SessionImpl(QueryRunner queryRunner) {
        final var queryBuilder = new QueryBuilder();
        this.persistenceContext = new StatefulPersistenceContext();
        this.entityPersister = new DefaultEntityPersister(queryBuilder, queryRunner);
        this.entityLoader = new DefaultEntityLoader(queryBuilder, queryRunner);
    }

    public SessionImpl(QueryRunner queryRunner, PersistenceContext persistenceContext) {
        final var queryBuilder = new QueryBuilder();
        this.persistenceContext = persistenceContext;
        this.entityPersister = new DefaultEntityPersister(queryBuilder, queryRunner);
        this.entityLoader = new DefaultEntityLoader(queryBuilder, queryRunner);
    }

    @Override
    public <T> T find(Class<T> clazz, Object id) {
        T entityInContext = persistenceContext.getEntity(clazz, id);
        if (entityInContext != null) {
            return entityInContext;
        }

        T entity = entityLoader.find(clazz, id);
        if (entity != null) {
            persistenceContext.addEntity(entity);
            return entity;
        }

        return null;
    }

    @Override
    public <T> T persist(T entity) {
        var persistedEntity = entityPersister.persist(entity);
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
    public void remove(Object entity) {
        entityPersister.remove(entity);
        persistenceContext.removeEntity(entity);
    }
}
