package orm.life_cycle;

public enum Status {
	MANAGED,    // 영속상태
	READ_ONLY,  // 읽기전용 상태
	DELETED,    // 삭제 예정 상태
	GONE,		// 이미 DB에서 삭제된 상태
	LOADING,    // 지연 로딩 중인 상태
	SAVING      // 커밋 전 MANAGED 상태로 전환된 후 발생
}
