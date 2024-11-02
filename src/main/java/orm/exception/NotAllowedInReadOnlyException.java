package orm.exception;

public class NotAllowedInReadOnlyException extends OrmPersistenceException {

    public NotAllowedInReadOnlyException(String message) {
        super(message);
    }

    public NotAllowedInReadOnlyException(String message, Throwable cause) {
        super(message, cause);
    }
}
