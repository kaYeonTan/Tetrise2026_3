# [팀 공유] A 파트 작업 현황 (B/C 합류용)

> **작성 목적:** B·C가 브랜치 합치거나 API 맞출 때 참고  
> **패키지:** `kr.ac.jbnu.se.tetris`  
> **실행 진입점:** `kr.ac.jbnu.se.tetris.app.GameWindow#main` (`Tetris.main`은 여기로 위임)

---

## 1. A가 지금까지 한 것 (세션)

| 세션 | 기능 ID | 상태 | 요약 |
|------|---------|------|------|
| S0 | (기반) | ✅ | Maven `pom.xml`, `event/` GameEventBus |
| S1 | F-00 | ✅ | `GameEngine`, `GameSession`, tick·lock·line·GAME_OVER |
| S2 | F-01 | ✅ | SRS, KickTable, ↑ CCW / ↓ CW |
| S3 | F-02 | ✅ | SevenBag, NEXT×3 |
| S4 | F-03 | ✅ | Hold(C), HoldPreview |
| S5 | F-04 | ✅ | 펜토 P/U/V/X/F, stage≥3, 15% bag |
| S6 | F-05 | ✅ | 줄 삭제 300ms blink, ScoreCalculator, GO 회색조 |
| S7 | F-06 Save | ⏸ **보류** | Login(B) 준비 후 A가 SaveService 예정 |
| S8 | 2-5 Router | ✅ | Route, ScreenRouter, Title→Mode→Game, Pause overlay |
| S9 | 통합·2인 골격 | ❌ 미착수 | B F-07 / C StageManager 이후 또는 병행 |

**1인 플레이:** TITLE → 1 Player → 게임 (SRS, Hold, 고스트, 펜토, 점수, Pause) **동작 가능**.

---

## 2. A가 **당장** 더 안 하는 것 (합의)

- **Save(F-06)** → B `AccountService` / LOGIN 화면 후 진행
- **2인 UI·키·좌우 보드** → **B (F-07)**
- **보스·스테이지 진행·패턴** → **C (F-10~F-15)**
- **보드 24행·CellType·Board 클래스(명세版)** → **B**

A는 위가 붙을 때까지 **필수 추가 작업 없음**으로 두어도 됨. (선택: S9 엔진 2세션 골격, Pause→Save UX 다듬기)

---

## 3. 소스 구조 (어디에 뭐가 있는지)

```
src/kr/ac/jbnu/se/tetris/
├── app/              GameWindow(main), AccountService(스텁)
├── core/             GameEngine, GameSession, GameMode, ScoreCalculator
├── event/            GameEventType, GameEvent, GameEventBus
├── piece/            SRS, 7-Bag, Hold, PentominoPolicy, Randomizer
├── ui/router/        Route(R-01~11), ScreenRouter, Guard들
├── ui/screen/        Title/Mode/Game/Pause/Login/Save 껍데기
├── view/             RenderOptions (회색조 등)
├── Board.java        그리기 + 키 (임시 Input; B가 빼갈 예정)
├── Shape.java, Tetrominoes.java, Preview*, PieceColors
└── Tetris.java       @Deprecated → GameWindow

test/                 JUnit (event, SRS, bag, hold, score, line anim, AuthGuard)
docs/                 S0~S8 handoff, A-part-plan-and-prompts.md
```

---

## 4. B팀 — 붙일 때 알아둘 것

### 4.1 B가 **소유**하는 영역 (A가 명세版 Board로 **대체하지 않음**)

- `input/` InputHandler, KeyBindings
- LOGIN / SIGNUP / SETTINGS **실 UI**
- `AccountService` **실구현** (지금은 `app/AccountService` 스텁)
- 2인 COOP_LOBBY, **보드 2개** 레이아웃 (F-07)
- Board **10×22 → 24행·CellType** 등 명세 보드

### 4.2 B가 **연결**하면 되는 지점

| 위치 | 내용 |
|------|------|
| `GameWindow` | `Route.LOGIN`, `SIGNUP`, `SETTINGS`, `COOP_LOBBY` factory를 B Screen으로 교체 |
| `AccountService` | `isLoggedIn()`, `currentUserId()` — `AuthGuard`가 이미 사용 |
| `Board` / `GameScreen` | 키 입력을 `InputHandler` → `GameEngine.moveLeft()` 등으로 **위임** (엔진 API는 이미 있음) |
| `GameScreen` | 2인일 때 보드 2개 + 세션 0/1 (엔진은 아직 session 1개; S9 또는 B PR 시 `GameEngine` 확장) |

### 4.3 A 쪽 **건드리지 말고 PR로 조율**할 파일 (충돌 주의)

- `Board.java` — B가 패널/입력 분리할 때 A와 diff 큼
- `GameWindow.java` — 라우트 register 목록

### 4.4 임시·테스트용 (나중에 제거/교체)

- **키 1~5:** `engine.setStage(n)` (C `StageManager` 전)
- **회전:** ↑ CCW, ↓ CW (Space 하드, D 소프트)
- **Hold:** C

---

## 5. C팀 — 붙일 때 알아둘 것

### 5.1 C가 **소유**

- `stage/` StageManager, Boss, BossPattern, BGM 등
- 보스 HP → `STAGE_CLEAR` → 다음 스테이지 (**명세 본편**)

### 5.2 C가 **구독**하면 되는 것 (코어 수정 불필요)

`GameEngine` 생성 시 `GameEventBus` 공유:

```java
engine.getBus().subscribe(GameEventType.LINES_CLEARED, e -> {
    int playerIndex = e.playerIndex;
    int lines = e.value;  // 1~4, 연출 **끝난 뒤** 발행 (보스 데미지용)
});
engine.getBus().subscribe(GameEventType.GAME_OVER, e -> { ... });
engine.getBus().subscribe(GameEventType.LINE_CLEAR_ANIM_START, e -> { ... });
engine.getBus().subscribe(GameEventType.LINE_CLEAR_ANIM_END, e -> { ... });
```

| 이벤트 | 시점 | `value` |
|--------|------|---------|
| `PIECE_LOCKED` | 착지 직후 | 0 |
| `LINE_CLEAR_ANIM_START` | 줄 있음, 삭제 **전** | 지울 줄 수 |
| `LINES_CLEARED` | blink 300ms **후** | 지운 줄 수 |
| `LINE_CLEAR_ANIM_END` | 삭제·점수 반영 후 | 지운 줄 수 |
| `GAME_OVER` | 1회 | 0 |

**스테이지 번호 (펜토 정책):**

```java
engine.getStage();
engine.setStage(n);  // 1~5, SevenBagRandomizer.setStage 연동
```

C `StageManager`가 생기면 **게임 시작·STAGE_CLEAR 시** `engine.setStage(...)` 호출하면 됨. (지금은 자동 스테이지 업 **없음**.)

### 5.3 C가 **아직 없어서** A에만 있는 것

- 보스 HP 바 UI 없음
- `BOSS_DAMAGED` / `STAGE_CLEAR` / `GAME_CLEAR` — **발행 안 함** (enum만 존재)

---

## 6. 게임·점수 규칙 (A 구현 기준)

- **점수:** `ScoreCalculator` — 1/2/3/4줄 → 100/300/500/800 × **currentStage**, D 1칸=1, Space 1칸=2
- **펜토:** stage **1~2** 없음, **3+** bag당 15%로 1개 삽입 (7 tetromino 유지)
- **Hold:** 착지 전 1회, swap 시 SPAWN 회전
- **줄 삭제:** lock → 300ms blink → 삭제·점수 → 다음 블록

---

## 7. 화면 라우터 (S8)

- **Route:** `TITLE`, `MODE_SELECT`, `GAME`, `PAUSE`(overlay), `SAVE_SLOTS`(overlay), `LOGIN`, … (`ui/router/Route.java`)
- **Guard:** 게스트 → `SAVE_SLOTS`/`RECORDS` 시 `LOGIN` redirect (`AuthGuard`)
- **Pause:** P → overlay (Resume / Save / Title). **Save는 F-06 미구현** → 슬롯 UI만 또는 LOGIN; 게스트는 게임 화면에서 빠져 LOGIN으로 갈 수 있어 “멈춘 것처럼” 보일 수 있음

상세: `docs/S8-handoff.md`

---

## 8. 실행·테스트

- **IntelliJ:** JDK **17** (`pom.xml`), main = `GameWindow`
- **JUnit:** `test/kr/ac/jbnu/se/tetris/**` (로컬 PATH에 `mvn` 없을 수 있음 → IDE 실행)
- **세션별 상세:** `docs/S0-handoff.md` … `docs/S8-handoff.md`
- **A 로드맵 전체:** `docs/A-part-plan-and-prompts.md`

---

## 9. B/C 합류 **최소 체크리스트**

### B PR 머지 전에 확인

- [ ] `GameWindow.main` 실행 → Title → 1인 → 플레이 가능
- [ ] LOGIN 스텁: 타이틀 Load(게스트) → LOGIN → 로그인 스텁 → SAVE_SLOTS 껍데기
- [ ] B가 `Board`/입력 바꿀 때 `GameEngine` public API 유지 또는 A와 한 번 sync

### C PR 머지 전에 확인

- [ ] `LINES_CLEARED` 구독으로 줄 수 수신
- [ ] `setStage(3)` 후 장시간 플레이 시 펜토 등장
- [ ] StageManager가 `setStage`만 호출해도 randomizer stage 반영되는지

---

## 10. A가 **나중에** 할 일 (팀 진행 후)

1. **F-06 Save** — `GameMemento`, `SaveService`, `SAVE_SLOTS` UI 연동, AuthGuard와 게스트 정책
2. **S9 (선택)** — `GameEngine.start(COOP_LOCAL)` session 2개, tick 분리
3. Pause → Save → LOGIN → **GAME+Pause 복귀** UX
4. B Board 교체 시 `GameSession` ↔ BoardSnapshot 어댑터

---

## 11. 연락·역할 한 줄

| 팀 | A에게 필요한 것 |
|----|-----------------|
| **B** | AccountService, LOGIN UI, InputHandler 연결, 2인 보드 UI, 명세 Board |
| **C** | StageManager → `setStage`, Boss HP ↔ `LINES_CLEARED`, RESULT/GAME_CLEAR 라우트 |

**A 상태:** 1인 코어 + 라우터 **1차 완료**. Save·2인 엔진·명세 스테이지 진행은 **B/C 또는 후속 A**.

---

*갱신: S8 반영 기준 · 세부 AC는 `_spec_extract.txt` 및 `docs/S*-handoff.md`*
