package orm;

import orm.dsl.holder.EntityIdHolder;
import orm.life_cycle.EntityEntry;
import orm.life_cycle.Status;
import orm.util.ReflectionUtils;

import java.util.HashMap;
import java.util.Map;

public class StatefulPersistenceContext implements PersistenceContext {

    private final Map<EntityKey, Object> cachedEntities;
    private final Map<EntityKey, Object> snapshotEntity;
    private final EntityEntryContext entryContext;

    public StatefulPersistenceContext() {
        this.cachedEntities = new HashMap<>();
        this.snapshotEntity = new HashMap<>();
        this.entryContext = new EntityEntryContext();
    }

    public StatefulPersistenceContext(Map<EntityKey, Object> cachedEntities, Map<EntityKey, Object> snapshotEntity) {
        this.cachedEntities = cachedEntities;
        this.snapshotEntity = snapshotEntity;
        this.entryContext = new EntityEntryContext();
    }

    public StatefulPersistenceContext(EntityEntryContext entryContext) {
        this.cachedEntities = new HashMap<>();
        this.snapshotEntity = new HashMap<>();
        this.entryContext = entryContext;
    }

    @Override
    public <T> T getEntity(Class<T> clazz, Object id) {
        final EntityKey entityKey = new EntityKey(clazz, id);
        var cachedEntity = cachedEntities.get(entityKey);
        if (cachedEntity == null) {
            return null;
        }

        entryContext.addEntry(entityKey, Status.MANAGED);
        return castEntity(clazz, cachedEntity);
    }

    @Override
    public <T> T addEntity(T entity) {
        var entityKey = EntityKey.ofEntity(entity);
        cachedEntities.put(entityKey, entity);
        snapshotEntity.put(entityKey, ReflectionUtils.deepCopyObject(entity));
        return entity;
    }

    @Override
    public <T> boolean contains(EntityIdHolder<T> idHolder) {
        return cachedEntities.containsKey(new EntityKey(idHolder));
    }

    @Override
    public void removeEntity(Object entity) {
        var entityKey = EntityKey.ofEntity(entity);
        cachedEntities.remove(entityKey);
        snapshotEntity.remove(entityKey);
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

    @Override
    public <T> EntityEntry getEntry(EntityIdHolder<T> idHolder) {
        var entityKey = new EntityKey(idHolder);
        return entryContext.getEntry(entityKey);
    }

    @Override
    public EntityEntry getEntry(Object entity) {
        var idHolder = new EntityIdHolder<>(entity);
        return this.getEntry(idHolder);
    }

    @Override
    public EntityEntry addEntry(Object entity, Status status) {
        var idHolder = new EntityIdHolder<>(entity);
        return entryContext.addEntry(new EntityKey(idHolder), status);
    }

    private <T> T castEntity(Class<T> clazz, Object persistedEntity) {
        if (!clazz.isInstance(persistedEntity)) {
            throw new IllegalArgumentException("Invalid type for persisted entity");
        }
        return clazz.cast(persistedEntity);
    }
}
