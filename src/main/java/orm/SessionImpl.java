package orm;

import orm.dsl.QueryBuilder;
import orm.dsl.QueryRunner;

public class SessionImpl implements EntityManager {

    private final StatefulPersistenceContext persistenceContext;
    private final EntityPersister entityPersister;

    public SessionImpl(QueryRunner queryRunner) {
        this.persistenceContext = new StatefulPersistenceContext();
        this.entityPersister = new DefaultEntityPersister(new QueryBuilder(), queryRunner);
    }

    @Override
    public <T> T find(Class<T> clazz, Object id) {
        T entityInContext = persistenceContext.getEntity(clazz, id);
        if (entityInContext != null) {
            return entityInContext;
        }

        T entity = entityPersister.find(clazz, id);
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
        entityPersister.update(entity);
        persistenceContext.updateEntity(entity);
        return entity;
    }

    @Override
    public void remove(Object entity) {
        entityPersister.remove(entity);
        persistenceContext.removeEntity(entity);
    }
}
