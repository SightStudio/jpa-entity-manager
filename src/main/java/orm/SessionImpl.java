package orm;

import orm.dsl.QueryBuilder;
import orm.dsl.QueryRunner;

public class SessionImpl implements EntityManager {

    private final StatefulPersistenceContext persistenceContext;
    private final EntityPersister entityPersister;
    private final EntityLoader entityLoader;

    public SessionImpl(QueryRunner queryRunner) {
        final var queryBuilder = new QueryBuilder();
        this.persistenceContext = new StatefulPersistenceContext();
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
        Object databaseSnapshot = persistenceContext.getDatabaseSnapshot(entity, entityPersister);

        entityPersister.update(entity, databaseSnapshot);

        persistenceContext.removeEntity(entity);
        persistenceContext.addEntity(entity);
        return entity;
    }

    @Override
    public void remove(Object entity) {
        entityPersister.remove(entity);
        persistenceContext.removeEntity(entity);
    }
}
