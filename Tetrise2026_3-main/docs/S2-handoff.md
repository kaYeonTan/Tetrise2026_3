# [팀 내부용] S2 — F-01 SRS

## 추가된 것

- `piece/RotationState` — 0, R, 2, L
- `piece/KickGroup`, `KickTable` — JLSTZ / I / O 킥 표
- `piece/SrsRotationSystem` — 5번까지 wall kick 시도
- `Shape` — rotationState, 스폰 시 SPAWN
- `GameSession.tryRotate` / `GameEngine.rotateLeft·Right` → SRS

## AC

- AC-01-1 T 왼쪽 벽 → 킥으로 오른쪽 밀림 (플레이 테스트)
- AC-01-2 I 전용 KickTable
- AC-01-3 O 회전 시 칸 동일
- AC-01-4 5회 실패 시 위치·회전 유지

## SMELL_LOG

| 위치 | 냄새 | 이유 |
|------|------|------|
| KickTable static 배열 | Data Clumps | 표를 파일로 빼기 귀찮음 |
| Shape + piece 패키지 | 패키지 이중 | 기존 Shape 안 고침 |
