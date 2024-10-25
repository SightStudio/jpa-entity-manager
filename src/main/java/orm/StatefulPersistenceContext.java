package orm;

import orm.dsl.extractor.EntityIdHolder;

import java.util.HashMap;
import java.util.Map;

public class StatefulPersistenceContext implements PersistenceContext {

    private final Map<EntityKey, Object> cachedEntities;

    public StatefulPersistenceContext() {
        this.cachedEntities = new HashMap<>();
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
        return entity;
    }

    @Override
    public void updateEntity(Object entity) {
        var entityKey = new EntityKey(new EntityIdHolder<>(entity));
        cachedEntities.put(entityKey, entity);
    }

    @Override
    public void removeEntity(Object entity) {
        var entityKey = new EntityKey(new EntityIdHolder<>(entity));
        cachedEntities.remove(entityKey);
    }

    private <T> T castEntity(Class<T> clazz, Object persistedEntity) {
        if (!clazz.isInstance(persistedEntity)) {
            throw new IllegalArgumentException("Invalid type for persisted entity");
        }
        return clazz.cast(persistedEntity);
    }
}
