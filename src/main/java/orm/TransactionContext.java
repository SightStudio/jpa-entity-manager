package orm;

import jakarta.persistence.FlushModeType;

public class TransactionContext {
    private final FlushModeType flushModeType;
    private final boolean isReadOnly;

    public TransactionContext() {
        this(FlushModeType.AUTO, false);
    }

    public TransactionContext(FlushModeType flushModeType, boolean isReadOnly) {
        this.flushModeType = flushModeType;
        this.isReadOnly = isReadOnly;
    }

    public FlushModeType getFlushModeType() {
        return flushModeType;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }
}
