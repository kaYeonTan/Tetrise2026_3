# 온라인 로비 프로토콜 (MVP)

> **담당:** 네트워크 2인 협동 (in-game 동기화는 미구현)  
> **패키지:** `kr.ac.jbnu.se.tetris.network.lobby`  
> **서버 실행:** `kr.ac.jbnu.se.tetris.network.lobby.LobbyServerMain` (기본 포트 `8765`)

## 전송 형식

- TCP, UTF-8, **한 요청 = JSON 한 줄 + `\n`**
- 응답도 **한 줄 JSON**

## 상수

| 항목 | 값 |
|------|-----|
| 동시 방 최대 | 5 (`LobbyConfig.MAX_ROOMS`) |
| 방 정원 | 2 (방장 포함) |
| 비밀방 코드 | 서버가 6자 생성 (`JoinCodeGenerator`), 클라이언트가 비밀번호 정하지 않음 |

## 클라이언트 → 서버

| type | 필드 | 설명 |
|------|------|------|
| `HELLO` | `nick` | 표시 닉 (로그인 닉 또는 Guest) |
| `LIST_ROOMS` | — | 공개 목록 (비밀방은 이름·잠금만, 코드 없음) |
| `CREATE_ROOM` | `name`, `visibility` (`PUBLIC` / `PRIVATE`) | 방 생성 |
| `JOIN_ROOM` | `roomId`, `joinCode?` | PUBLIC은 코드 생략, PRIVATE는 필수 |
| `LEAVE_ROOM` | — | 현재 방 퇴장 |
| `MY_ROOM` | — | 내가 들어 있는 방 (방장만 `joinCode` 수신) |

## 서버 → 클라이언트

| type | 설명 |
|------|------|
| `HELLO_OK` | `clientId`, `nick` |
| `ROOM_LIST` | `rooms[]` → `RoomSnapshot` |
| `ROOM_CREATED` | `roomId`, `roomName`, `visibility`, `joinCode?`(PRIVATE만), `players[]` |
| `JOINED` | `roomId`, `roomName`, `role` (`HOST`/`GUEST`), `players[]` |
| `LEFT` | 퇴장 완료 |
| `ERROR` | `code`, `message` |

### ERROR code 예

`ROOM_CAP`, `FULL`, `WRONG_CODE`, `NOT_FOUND`, `ALREADY_IN_ROOM`, `INVALID_NAME`

## UI 연동 (B팀)

- 타이틀 **「온라인 협동」** → `LobbyFrame` (임시 다이얼로그)
- 스타일 통합 시 `LobbyFrame`만 교체하고 `LobbyClient` API는 유지

## 다음 세션 (예정)

- in-game: 서버 권위 `CoopRunState` + 입력 릴레이
- coop 저장: `coopId` + 슬롯 2개 (별도 문서)
