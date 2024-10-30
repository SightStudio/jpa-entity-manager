package orm;

import orm.dirty_check.DirtyCheckMarker;
import orm.dsl.QueryBuilder;
import orm.dsl.QueryRunner;

public class DefaultEntityPersister implements EntityPersister {

    private final QueryBuilder queryBuilder;
    private final QueryRunner queryRunner;

    public DefaultEntityPersister(QueryBuilder queryBuilder, QueryRunner queryRunner) {
        this.queryBuilder = queryBuilder;
        this.queryRunner = queryRunner;
    }

    @Override
    public <T> T persist(T entity) {
        return queryBuilder.insertIntoValues(entity, queryRunner)
                .returnAsEntity();
    }

    @Override
    public <T> T update(T entity, T oldVersion) {
        var objectTableEntity = new TableEntity<>(entity);
        var oldVersionTableEntity = new TableEntity<>(oldVersion);

        boolean hasDirty = new DirtyCheckMarker<>(objectTableEntity, oldVersionTableEntity).compareAndMarkChangedField();
        if (hasDirty) {
            queryBuilder.update(objectTableEntity, queryRunner)
                    .withBitsetAware()
                    .byId()
                    .execute();
        }

        return entity;
    }

    @Override
    public void remove(Object entity) {
        queryBuilder.deleteFrom(entity, queryRunner).byId().execute();
    }

    @Override
    public <T> T getDatabaseSnapshot(Class<T> entityClazz,  Object id) {
        return queryBuilder.selectFrom(entityClazz, queryRunner).findById(id).fetchOne();
    }
}
