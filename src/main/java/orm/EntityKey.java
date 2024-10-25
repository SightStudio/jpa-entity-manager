package orm;

import orm.dsl.holder.EntityIdHolder;

import java.io.Serializable;

public record EntityKey (
        Class<?> clazz,
        Object idValue
) implements Serializable {

    public <E> EntityKey(EntityIdHolder<E> entityIdHolder) {
        this(entityIdHolder.getEntityClass(), entityIdHolder.getIdValue());
    }
}
