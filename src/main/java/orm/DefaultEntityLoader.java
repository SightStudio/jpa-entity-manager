package orm;

import orm.dsl.QueryBuilder;
import orm.dsl.QueryRunner;

public class DefaultEntityLoader implements EntityLoader {

    private final QueryBuilder queryBuilder;
    private final QueryRunner queryRunner;

    public DefaultEntityLoader(QueryBuilder queryBuilder, QueryRunner queryRunner) {
        this.queryBuilder = queryBuilder;
        this.queryRunner = queryRunner;
    }

    @Override
    public <T> T find(Class<T> clazz, Object id) {
        return queryBuilder.selectFrom(clazz, queryRunner)
                .findById(id)
                .fetchOne();
    }
}
