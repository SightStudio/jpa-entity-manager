package orm.life_cycle;

import config.PluggableH2test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import orm.EntityManager;
import orm.SessionImpl;
import orm.StatefulPersistenceContext;
import persistence.sql.ddl.Person;
import test_entity.PersonWithAI;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static steps.Steps.테이블_생성;

public class EntityEntryContextTest extends PluggableH2test {

    @Test
    @DisplayName("[클라이언트에서 생성되는 ID] 신규 엔티티를 저장하면 SAVING -> MANAGED 상태로 저장된다.")
    void entityEntry_persist_테스트() {
        TrackableEntityEntryContext entityEntryContext = new TrackableEntityEntryContext();

        runInH2Db(queryRunner -> {
            // given
            테이블_생성(queryRunner, Person.class);
            EntityManager session = new SessionImpl(queryRunner, new StatefulPersistenceContext(entityEntryContext));
            Person origin = new Person(1L, 30, "설동민");

            // when
            session.persist(origin); // insert
            List<EntityEntry> entryChangeLog = entityEntryContext.getEntryChangeLog(origin);

            // then
            assertThat(entryChangeLog).asList()
                    .extracting("status")
                    .containsExactly(Status.SAVING, Status.MANAGED);
        });
    }

    @Test
    @DisplayName("[DB에서 생성되는 ID] 신규 엔티티를 저장하면 SAVING 없이 바로 MANAGED 상태로 저장된다.")
    void entityEntry_persist_ai_테스트() {
        TrackableEntityEntryContext entityEntryContext = new TrackableEntityEntryContext();

        runInH2Db(queryRunner -> {
            // given
            테이블_생성(queryRunner, PersonWithAI.class);
            EntityManager session = new SessionImpl(queryRunner, new StatefulPersistenceContext(entityEntryContext));
            PersonWithAI origin = new PersonWithAI(30L, "설동민");

            // when
            session.persist(origin); // insert
            List<EntityEntry> entryChangeLog = entityEntryContext.getEntryChangeLog(origin);

            // then
            assertThat(entryChangeLog).asList()
                    .extracting("status")
                    .containsExactly(Status.MANAGED);
        });
    }
}
