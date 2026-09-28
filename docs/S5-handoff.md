# [팀 내부용] S5 — F-04 펜토미노

> **상태:** 코드 반영 완료.

## 전체 흐름

1. `SevenBagRandomizer.appendOneBag()` — 7 테트로미노 셔플 후, `PentominoPolicy.shouldInsert(stage)` 이면 P5_* 1개를 임의 위치에 삽입 (bag 길이 7 또는 8).
2. `GameEngine.currentStage` — `start()` 시 랜덤izer에 전달, **1~5** 키로 런타임 변경 (C `StageManager` 전 임시).
3. `Shape` — 펜토는 5칸 coords + `blockCount()`; 이동·착지·고스트·미리보기는 모두 `blockCount` 기준.
4. SRS — I/O 제외 기본 `KickGroup`(JLSTZ) 그대로 펜토에도 적용.

## 기능 위치

| 기능 | 파일 |
|------|------|
| P5 enum | `Tetrominoes.java` |
| 5칸 좌표·회전 | `Shape.java` |
| 삽입 규칙 | `piece/PentominoPolicy.java` |
| bag + stage | `SevenBagRandomizer.java`, `GameEngine.setStage` |
| 색 | `PieceColors.java` |
| 입력(스테이지) | `Board` — **1~5** |
| 상태줄 | `stN` 표시 |

## AC

| AC | 구현 |
|----|------|
| AC-04-1 | `stage < minStage(3)` → `shouldInsert` false |
| AC-04-2 | `GameSession` / hold / SRS 동일 경로, `blockCount()` |
| AC-04-3 | 셔플 7종 유지 + 펜토 1개 optional insert, `hasAllTetrominoes` 검증 |

## SMELL_LOG

| 위치 | 냄새 | 이유 |
|------|------|------|
| `Shape.spawnCoords` switch | Giant switch | PieceType enum + coords 테이블로 안 감 |
| `Board` 1~5 키 | 테스트 훅 | C StageManager 오면 제거 예정 |
| `queueContainsPentomino` | 테스트 전용 public | bag 내부 노출 |
| 펜토 SRS | JLSTZ 공용 | 명세 전용 킥 테이블 미분리 |

## 체크리스트 (1인)

| 입력 | 기대 |
|------|------|
| 새 게임, st1 | NEXT/HOLD에 P5 없음 (여러 bag) |
| **3** 키 | status `st3`, 시간 지나면 5칸 블록 등장 |
| 펜토 | 이동·↓ 착지·줄 삭제 |
| 펜토 **C** | 홀드 swap, SPAWN 각도 |
| 펜토 **↑↓** | 벽에서 킥 시도 (막히면 그대로) |
| st1 복귀 **1** | 이후 새 bag부터 펜토 off |

## 테스트

- `test/.../piece/PentominoPolicyTest.java`

## 기존 기능

- 7-Bag×3, Hold, 고스트, SRS — 회귀 확인 권장.

## 다음 (S6)

- F-05 줄삭제 연출 + `ScoreCalculator`
