package orm;

import java.io.Serializable;

public record EntityKey (
        Class<?> clazz,
        Object idValue
) implements Serializable {

}
