# [팀 내부용] S1 — GameEngine / GameSession (F-00)

## 전체 흐름

1. `Tetris.main` → `Board` 생성 → `start()` → Timer 400ms.
2. `actionPerformed` → `GameEngine.tick()` (낙하 / 줄 지운 뒤 다음 블록).
3. 키 입력 → `engine.moveLeft()` 등 → `GameSession.tryMove` → `repaint`.
4. 블록 착지 → `lockPieceToBoard` → `PIECE_LOCKED` → `removeFullLines` → `LINES_CLEARED`(value=줄 수).
5. 스폰 실패 → `GAME_OVER` 1회 → Timer 정지.

## 기능 위치 지도 (1인)

| 기능 | 위치 |
|------|------|
| 게임 진행 | `core/GameEngine.java` — tick, pause, 입력 위임 |
| 보드·블록 상태 | `core/GameSession.java` |
| 그리기·Timer·키 | `Board.java` |
| 이벤트 | `GameEventBus` (Engine이 publish) |

2인: S9에서 `sessions` 2개 예정.

## 핵심 알고리즘

- **충돌:** 4칸 각각 보드 밖/다른 블록이면 이동 실패 (AC-00-1).
- **줄 삭제:** 아래부터 full row 찾으면 위 줄 내림, 한 tick에 여러 줄 (AC-00-2).
- **줄 삭제 후:** `isFallingFinished` → 다음 tick에만 새 블록 (기존 튜토리얼과 동일).
- **점수:** TODO S6 — statusbar는 `linesRemoved`만 표시.

## 발표 Q&A

1. **Board vs GameSession?** — Board는 Swing, Session은 데이터+규칙. B가 Board 클래스 명세版 넣으면 Session이 그걸 쓸 예정.
2. **이벤트 왜?** — C 보스는 `LINES_CLEARED`만 듣면 HP 깎기 가능.
3. **GAME_OVER 한 번?** — `gameOverSent` 플래그 (AC-00-3).

## SMELL_LOG (S1)

| 위치 | 냄새 | 이유 | 2차 | 패턴 |
|------|------|------|-----|------|
| `Board` + `GameEngine` | Feature Envy | 급하게 튜토리얼 Board 쪼갬 | GamePanel 분리 | MVC |
| `GameSession` public 필드 | 데이터 클래스 노출 | paint에서 바로 읽음 | getter/스냅샷 | Memento |
| `onPieceLanded` package | — | 테스트용 | — | — |

## 체크리스트

| 입력 | 기대 |
|------|------|
| Tetris 실행, 블록 이동/회전/스페이스 | 예전과 같음 |
| 줄 2줄 동시 | 둘 다 삭제, statusbar +2 |
| 꽉 채워 game over | "game over", 재시작 start |
| `GameEngineEventTest` | GAME_OVER 1회 |

## 기존 기능

- `Shape`, `Tetrominoes`: 변경 없음.
- `event/*`: S0 유지.
- B/C 코드: 없음.
