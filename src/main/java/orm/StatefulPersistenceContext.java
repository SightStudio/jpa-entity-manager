package orm;

import orm.dsl.holder.EntityIdHolder;
import orm.util.ReflectionUtils;

import java.util.HashMap;
import java.util.Map;

public class StatefulPersistenceContext implements PersistenceContext {

    private final Map<EntityKey, Object> cachedEntities;
    private final Map<EntityKey, Object> snapshotEntity;

    public StatefulPersistenceContext() {
        this.cachedEntities = new HashMap<>();
        this.snapshotEntity = new HashMap<>();
    }

    @Override
    public <T> T getEntity(Class<T> clazz, Object id) {
        final EntityKey entityKey = new EntityKey(clazz, id);
        var cachedEntity = cachedEntities.get(entityKey);
        if (cachedEntity == null) {
            return null;
        }
        return castEntity(clazz, cachedEntity);
    }

    @Override
    public <T> T addEntity(T entity) {
        var entityKey = new EntityKey(new EntityIdHolder<>(entity));
        cachedEntities.put(entityKey, entity);
        snapshotEntity.put(entityKey, ReflectionUtils.deepCopyObject(entity));
        return entity;
    }

    @Override
    public void removeEntity(Object entity) {
        var entityKey = new EntityKey(new EntityIdHolder<>(entity));
        cachedEntities.remove(entityKey);
    }

    @Override
    public <T> Object getDatabaseSnapshot(EntityIdHolder<T> idHolder, EntityPersister entityPersister) {
        var entityKey = new EntityKey(idHolder);

        var snapshot = snapshotEntity.get(entityKey);
        if (snapshot != null) {
            return snapshot;
        }

        Object databaseSnapshot = entityPersister.getDatabaseSnapshot(idHolder);
        if (databaseSnapshot != null) {
            snapshotEntity.put(entityKey, databaseSnapshot);
        }
        return databaseSnapshot;
    }

    private <T> T castEntity(Class<T> clazz, Object persistedEntity) {
        if (!clazz.isInstance(persistedEntity)) {
            throw new IllegalArgumentException("Invalid type for persisted entity");
        }
        return clazz.cast(persistedEntity);
    }
}
