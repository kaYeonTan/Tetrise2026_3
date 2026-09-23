# [팀 내부용] S6 — F-05 연출 + ScoreCalculator

## 흐름

1. 블록 lock → `prepareLineClearAnimation()` (줄은 **아직** 안 지움, `lineClearBlinkRows` 표시)
2. **300ms** 깜빡임 (`LINE_CLEAR_ANIM_START` → Board 타이mer 100ms)
3. `commitLineClear()` → 점수·`linesRemoved` → `LINES_CLEARED`(value=지운 줄 수) → `LINE_CLEAR_ANIM_END` → 스폰
4. 연출 중 `isInputBlocked()` — 이동·회전·D·Space·C 무시 (P 일시정지는 가능)
5. 게임오버: 보드 유지 + `RenderOptions` 회색조 + "GAME OVER"

## 점수 (ScoreCalculator)

| 항목 | 공식 |
|------|------|
| 줄 | {0,100,300,500,800}[n] × **stage** (n>4는 4로) |
| 소프트(D) | 1칸당 1 |
| 하드(Space) | 1칸당 2 |

**제거됨:** S5 테스트용 “10점마다 st+1”. 스테이지는 **1~5 키** 또는 추후 C `StageManager`.

## AC

| AC | 구현 |
|----|------|
| AC-05-1 | anim 끝난 뒤 `commitLineClear` + 점수 |
| AC-05-2 | `isInputBlocked` + Board early return |
| AC-05-3 | `isGameOver()` 시 grayscale + 문구 |

## SMELL_LOG

| 위치 | 냄새 | 이유 |
|------|------|------|
| `GameEngine` | 상태 플래그 3개 | LINE_CLEAR 전용 enum state 없음 |
| `Board.paint` | God draw | RenderOptions만 분리 |
| 타이mer 100/400 | 이벤트로 delay 변경 | 전용 AnimTimer 없음 |

## 체크리스트

| 확인 | 기대 |
|------|------|
| 1줄 | 흰 깜빡 → 사라짐, sc +100×st |
| 2줄 한번에 | 두 줄 동시 깜빡 → +300×st |
| 깜빡는 동안 방향키 | 무 반응 |
| GO | 색 회색, 배치 그대로 |
| D / Space | sc +1 / +2 per cell |

## 테스트

- `ScoreCalculatorTest`
- `LineClearAnimationTest`

## 다음

- S7 Save / Memento
