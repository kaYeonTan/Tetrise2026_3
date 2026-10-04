# 온라인 2인 로비 메모

서버는 `LobbyServerMain`으로 실행한다. 기본 포트는 `8765`이다.

메시지는 TCP로 보내고 한 줄에 JSON 하나씩 보낸다.

## 자주 쓰는 요청

| type | 하는 일 |
|---|---|
| `HELLO` | 닉네임 연결 |
| `LIST_ROOMS` | 방 목록 받기 |
| `CREATE_ROOM` | 공개 또는 비밀 방 만들기 |
| `JOIN_ROOM` | 방 들어가기 |
| `LEAVE_ROOM` | 방 나가기 |
| `MY_ROOM` | 내 대기실 정보 받기 |
| `START_GAME` | 방장이 게임 시작 |
| `RESUME_COOP` | 방장이 협동 저장 이어하기 |

비밀방 코드는 서버가 만든다. 방장은 내 대기실에서 코드를 확인할 수 있다.

게임 중에는 `PLAYER_ACTION`으로 키 입력을 보내고, 방장은 `COOP_SNAPSHOT`을 보낸다.
저장은 방장만 할 수 있다.

관련 코드는 `network/lobby`, `network/coop`, `LobbyFrame.java` 쪽에 있다.
