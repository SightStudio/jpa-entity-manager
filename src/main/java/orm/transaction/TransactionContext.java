package orm.transaction;

import jakarta.persistence.FlushModeType;
import orm.dsl.QueryRunner;

public class TransactionContext {

    private final QueryRunner queryRunner;
    private final FlushModeType flushMode;

    public TransactionContext(QueryRunner queryRunner) {
        this.queryRunner = queryRunner;
        this.flushMode = FlushModeType.AUTO;
    }
}
