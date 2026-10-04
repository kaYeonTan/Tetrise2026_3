# 화면 작업 메모

1인 게임 화면은 `GamePanel`이 직접 그린다.

예전에는 `Board` 화면이 따로 보여서 일시정지나 메인 화면에서도 블록이 남는 경우가 있었다. 지금은 `ASinglePlayerBridge`가 엔진 상태만 가져오고, 실제 보드와 HOLD, NEXT는 `GamePanel.drawGame()`에서 그린다.

스테이지 3은 일반 HP 방식이 아니다. 보드 아래 6줄의 도안을 맞추면 클리어된다.

- `T`: 블록이 있어야 함
- `E`: 비어 있어야 함
- `-`: 아무거나 가능

2인 온라인에서는 두 보드를 같이 봐서 도안을 판정한다.

주요 파일은 `GamePanel.java`, `ASinglePlayerBridge.java`, `Blueprint.java`이다.
