# S3 — F-02 7-Bag + Next

- `SevenBagRandomizer` — seed, next/peek, saveState
- `GameSession.spawnPiece` — randomizer.next()
- `NextPreviewPanel` — peekNext() 표시
- 창 가로 280 (NEXT 패널 EAST)

## 플레이 확인

1. 오른쪽 NEXT와 떨어지는 블록: NEXT는 **그 다음** 블록
2. 블록 7개 떨어질 때까지 같은 종류 2번 연속 없음
3. 고스트·회전·줄삭제 회귀
