# [팀 내부용] S0 — GameEventBus + Maven

## 전체 흐름

1. 앱 실행: `Tetris.main` → 기존과 동일 (S0에서 Board 안 건드림).
2. 게임 로직(S1~)에서 `GameEventBus.publish(...)` 호출 예정.
3. C(보스·사운드), B(계정)는 `subscribe`로 같은 bus에 붙을 예정.

## 기능 위치 지도

| 기능 | 파일 |
|------|------|
| 이벤트 종류 | `event/GameEventType.java` |
| 이벤트 데이터 | `event/GameEvent.java` (public 필드) |
| 구독 인터페이스 | `event/GameEventListener.java` |
| 발행·구독 | `event/GameEventBus.java` |
| 로컬 데이터 폴더 | `data/.gitkeep` |
| 빌드 | `pom.xml` (main=`Tetris`, source=`src`, test=`test`) |

1인/2인: S0 해당 없음. playerIndex는 이벤트 필드만 준비.

## 핵심 알고리즘

- `publish(e)`: `all` 리스트 전체에 `onEvent`, 그다음 `byType.get(e.type)` 리스트에 또 `onEvent`.
- 타입만 구독한 리스너는 해당 타입 publish 때 1번 호출됨 (전체 구독과 중복 등록하면 두 번 올 수 있음 — 나중에 조심).

예: `new GameEvent(LINES_CLEARED, 0, 2)` → 0번 플레이어가 2줄 삭제.

## 발표 예상 Q&A

1. **왜 Observer?** — 줄 삭제 한 번에 보스·소리·UI가 같이 반응해야 해서, Board가 Sound 직접 부르면 꼬임.
2. **GameEvent가 record가 아닌 이유?** — 아직 필드 더 붙일 수 있어서 class로 둠 (value 말고도).
3. **Maven 왜 넣었나?** — Gson·JUnit 명세 맞추려고; 소스 폴더는 IntelliJ랑 같게 `src` 유지.
4. **data 폴더?** — 세이브·users.json 넣을 자리 (NFR-07 상대 경로).

## SMELL_LOG (S0 추가분)

| 위치 | Fowler | 왜 생겼는지 | 2차 리팩터링 | 패턴 후보 |
|------|--------|-------------|--------------|-----------|
| `GameEventBus.publish` | Observed Behavior | 전체+타입 구독 둘 다 돌리다 보니 중복 호출 가능 | publish 파이프라인 하나로 합치거나 구독 종류 분리 | Observer 유지 / EventAggregator |
| `GameEvent` public 필드 | Mutable Data | DTO 빨리 만들려고 | record 불변으로 | DTO / Event 객체 |
| `GameEventBus.publishCount` | Speculative Generality | 디버그용 | 삭제 또는 로그 프레임워크 | — |
| `pom.xml` non-standard `src/` | — | 기존 IDEA 모듈 안 깨트리려고 | `src/main/java` 이전 | — |

## 동작 체크리스트

| 입력 | 기대 |
|------|------|
| `mvn test` | `GameEventBusTest` 통과 |
| IntelliJ `Tetris.main` | 예전처럼 테트리스 플레이 가능 |
| `data/` | 빈 폴더 존재 (gitkeep) |

## 기존 기능 깨짐 여부

- `Board`, `Shape`, `Tetris`: **수정 없음**.
- `Main.java` Hello: 그대로 (게임 진입점은 `Tetris`).
