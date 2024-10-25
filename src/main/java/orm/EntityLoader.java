package orm;

public interface EntityLoader {
    <T> T find(Class<T> clazz, Object id);
}
