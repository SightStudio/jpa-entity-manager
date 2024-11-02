package orm;

import config.PluggableH2test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import orm.life_cycle.EntityEntry;
import orm.life_cycle.Status;
import orm.life_cycle.TrackableEntityEntryContext;
import persistence.sql.ddl.Person;
import test_entity.PersonWithAI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static steps.Steps.테이블_생성;

public class StatefulPersistenceContextTest extends PluggableH2test {

    @Test
    @DisplayName("persist 메서드를 사용하면 1차 캐시와 DB 스냅샷에 엔티티가 저장된다.")
    void persistence_테스트() {
        Map<EntityKey, Object> _1차캐시 = new HashMap<>();
        Map<EntityKey, Object> DB_스냅샷 = new HashMap<>();

        runInH2Db(queryRunner -> {
            // given
            PersistenceContext trackablePersistenceContext = new StatefulPersistenceContext(_1차캐시, DB_스냅샷);
            테이블_생성(queryRunner, Person.class);
            EntityManager session = new SessionImpl(queryRunner, trackablePersistenceContext);

            // when
            session.persist(new Person(1L, 30, "설동민"));

            // then
            assertThat(_1차캐시).hasSize(1);
            assertThat(DB_스냅샷).hasSize(1);
        });
    }

    // @TODO 2-4 단계에서 영속성 컨텍스트 상태가 추가되면 다르게 동작해야할것으로 보임
    @Test
    @DisplayName("merge 메서드를 사용해도 1차 캐시와 DB 스냅샷에 엔티티가 저장된다.")
    void merge_테스트() {
        Map<EntityKey, Object> _1차캐시 = new HashMap<>();
        Map<EntityKey, Object> DB_스냅샷 = new HashMap<>();

        runInH2Db(queryRunner -> {
            // given
            PersistenceContext trackablePersistenceContext = new StatefulPersistenceContext(_1차캐시, DB_스냅샷);
            테이블_생성(queryRunner, Person.class);
            EntityManager session = new SessionImpl(queryRunner, trackablePersistenceContext);

            Person origin = new Person(1L, 30, "설동민");
            session.persist(origin); // insert

            Person person = session.find(Person.class, 1L);
            person.setAge(20);

            // when
            session.merge(person); // update

            // then
            assertThat(_1차캐시).hasSize(1);
            assertThat(DB_스냅샷).hasSize(1);
        });
    }
}
