package orm;

import orm.dsl.QueryBuilder;

public class SessionImpl implements EntityManager {

    private final StatefulPersistenceContext persistenceContext;
    private final EntityPersister entityPersister;

    public SessionImpl(QueryBuilder queryBuilder) {
        this.persistenceContext = new StatefulPersistenceContext();
        this.entityPersister = new DefaultEntityPersister(queryBuilder);
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

    /**
     * 엔티티 저장
     * <p>
     * 엔티티메니저에서는 bulk insert가 불가능 하지만
     * QueryBuilder 를 직접 쓰면 가능함.
     *
     * @param entity 엔티티 클래스
     * @return 엔티티
     */
    @Override
    public <T> T persist(T entity) {
        T persistedEntity = entityPersister.persist(entity);
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
