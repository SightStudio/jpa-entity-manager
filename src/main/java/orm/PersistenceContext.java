package orm;

public interface PersistenceContext {

    <T> T getEntity(Class<T> entityClazz, Object id);

    <T> T addEntity(T entity);

    void removeEntity(Object entity);

    Object getDatabaseSnapshot(Object entity, EntityPersister entityPersister);
}
