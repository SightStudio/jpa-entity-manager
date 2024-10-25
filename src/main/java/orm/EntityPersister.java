package orm;

public interface EntityPersister {

    // @TODO 이후에 EntityLoader로 이동시켜야함
    <T> T find(Class<T> clazz, Object id);

    <T> T persist(T entity);

    <T> T update(T entity);

    void remove(Object entity);
}
