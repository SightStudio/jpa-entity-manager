package orm;

public interface PersistenceContext {

    <T> T getEntity(Class<T> entityClazz, Object id);

    <T> T addEntity(T entity);

    void updateEntity(Object entity);

    void removeEntity(Object entity);
}
