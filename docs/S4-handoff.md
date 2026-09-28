# [팀 내부용] S4 — F-03 Hold

> **상태:** 코드 반영 완료 (이전 세션). 본 문서는 발표·보고용 정리.

## 전체 흐름

1. 플레이 중 **C** → `GameEngine.hold()` → `GameSession.tryHold()`.
2. 손 블록 → `HoldSlot`, 비었으면 `randomizer.next()` 스폰 / 있었으면 `spawnShape(보관)` 교환.
3. 성공 시 `holdLocked = true` → 착지(`onPieceLanded`)에서 `unlockHold()`.
4. 스폰 실패 → `lastHoldTopOut` → `GAME_OVER`.

## 기능 위치

| 기능 | 파일 |
|------|------|
| 홀드 데이터 | `piece/HoldSlot.java` |
| 홀드 규칙 | `GameSession.tryHold()`, `spawnShape()` |
| 게임오버 | `GameEngine.hold()` |
| UI | `HoldPreviewPanel.java`, `PreviewDraw.java` |
| 입력 | `Board` — **C** |
| 잠금 해제 | `GameEngine.onPieceLanded()` → `unlockHold()` |

## AC

| AC | 구현 |
|----|------|
| AC-03-1 | `holdLocked` (한 블록당 홀드 1회) + 보관·손 종류 같으면 swap 거부 |
| AC-03-2 | `setShape` / `spawnShape` → `RotationState.SPAWN` |
| AC-03-3 | 스폰 `tryMove` 실패 → `lastHoldTopOut` → `handleGameOver()` |

## 미리보기 방향

- HOLD 그리기는 `PreviewDraw` 사용 (보드와 Y축 동일). Z/S 좌우반전 이슈는 S3 쪽 수정으로 해결됨.

## SMELL_LOG

| 위치 | 냄새 | 이유 |
|------|------|------|
| `tryHold` | Long Method | swap + spawn + 플래그 한 메서드 |
| 홀드 `Tetrominoes`만 저장 | | 회전 상태는 항상 0도 표시 (명세) |
| `lastHoldTopOut` | 플래그 | 실패 이유 enum 없음 |

## 체크리스트 (1인)

| 입력 | 기대 |
|------|------|
| C (빈 HOLD) | 손 → HOLD, NEXT 1번 블록 스폰 |
| C 연타 (미착지) | 두 번째 무시 |
| 착지 후 C | HOLD ↔ 손 교환 |
| 꺼낸 블록 | 0도(SPAWN) |
| 꽉 찬 상태에서 C로 막힘 | game over |

## 테스트

- `test/.../HoldTest.java`

## 기존 기능

- SRS, 7-Bag×3 NEXT, 고스트 — 회귀 확인 권장.
