package orm.life_cycle;

import orm.EntityEntryContext;
import orm.EntityKey;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * EntityEntryContext에서 엔티티의 변경 이력을 관리하기 위한 클래스
 * EntityEntryContext를 상속받아 구현하였고, 주로 테스트를 위한 용도로만 사용된다.
 */
public class TrackableEntityEntryContext extends EntityEntryContext {

    private final Map<EntityKey, List<EntityEntry>> entryHistoryMap;

    public TrackableEntityEntryContext() {
        super();
        this.entryHistoryMap = new LinkedHashMap<>();
    }

    public TrackableEntityEntryContext(Map<EntityKey, EntityEntry> entryMap) {
        super(entryMap);
        this.entryHistoryMap = new LinkedHashMap<>();
    }

    @Override
    public EntityEntry addEntry(EntityKey entityKey, Status status) {
        createChangeLog(entityKey, status);
        return super.addEntry(entityKey, status);
    }

    public <E> List<EntityEntry> getEntryChangeLog(E entity) {
        return entryHistoryMap.get(EntityKey.ofEntity(entity));
    }

    // 업데이트 전 상태 기록
    private void createChangeLog(EntityKey entityKey, Status status) {
        if(entityKey.hasNullIdValue()) {
            return;
        }

        entryHistoryMap.putIfAbsent(entityKey, new ArrayList<>());
        entryHistoryMap.get(entityKey).add(new SimpleEntityEntry(entityKey.idValue(), status));
    }
}
