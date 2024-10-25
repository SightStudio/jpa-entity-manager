package orm;

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
    public <T> T update(T entity) {
        queryBuilder.update(entity, queryRunner).byId().execute();
        return entity;
    }

    @Override
    public void remove(Object entity) {
        queryBuilder.deleteFrom(entity, queryRunner).byId().execute();
    }
}
