package orm.life_cycle;

public class SimpleEntityEntry implements EntityEntry {

    private final Object id;
    private Status status;

    public SimpleEntityEntry(EntityEntry entityEntry) {
        this.id = entityEntry.getId();
        this.status = entityEntry.getStatus();
    }

    public SimpleEntityEntry(Object idValue, Status status) {
        this.id = idValue;
        this.status = status;
    }

    public SimpleEntityEntry(Object idValue) {
        this.id = idValue;
        this.status = Status.MANAGED;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public EntityEntry updateStatus(Status status) {
        this.status = status;
        return this;
    }

    @Override
    public boolean isStatus(Status status) {
        return this.status == status;
    }

    @Override
    public Object getId() {
        return id;
    }
}
