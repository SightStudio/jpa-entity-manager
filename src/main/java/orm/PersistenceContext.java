package orm;

import orm.dsl.holder.EntityIdHolder;

public interface PersistenceContext {

    <T> T getEntity(Class<T> entityClazz, Object id);

    <T> T addEntity(T entity);

    <T> boolean contains(EntityIdHolder<T> idHolder);

    void removeEntity(Object entity);

    <T> Object getDatabaseSnapshot(EntityIdHolder<T> idHolder, EntityPersister entityPersister);
}
