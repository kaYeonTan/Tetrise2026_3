# [팀 내부용] S8 — ScreenRouter (2-5)

## 구조

- **`app/GameWindow`** — `main`, JFrame + 라우터 부트
- **`ui.router`** — `Route`, `ScreenRouter`, `AuthGuard`, `LeaveGuard`, `ParentGuard`
- **`ui.screen`** — Title, Mode, **GameScreen**, Pause, SaveSlots, Login placeholder
- **`Tetris.main`** → `GameWindow.main` 위임 (@Deprecated)

화면은 서로 참조하지 않고 **`router.navigate` / `back`** 만 사용.

## 흐름 (1인)

1. **TITLE** → Start → **MODE_SELECT** → 1 Player → **GAME**
2. **P** → 일시정지 + **PAUSE** 오버레이
3. **Resume** → `back` + 재개
4. PAUSE **Save** → **SAVE_SLOTS** (게스트면 **LOGIN** redirect)
5. PAUSE **Title** → LeaveGuard 확인 → **TITLE**

## Guard

| Guard | 역할 |
|-------|------|
| AuthGuard | 게스트 + SAVE_SLOTS/RECORDS → LOGIN(returnTo) |
| LeaveGuard | GAME 밑에서 TITLE 나갈 때 확인 |
| ParentGuard | PAUSE/SAVE/SETTINGS는 currentScreen==GAME |

## SMELL_LOG

| 위치 | 냄새 | 이유 |
|------|------|------|
| overlayHost | BorderLayout 겹침 | JLayeredPane 미사용 |
| GameScreen | Board 직접 소유 | GamePanel 분리는 B 이후 |
| SaveSlots | 빈 껍데기 | F-06 보류 |

## 체크리스트

| 동작 | 기대 |
|------|------|
| TITLE→1인→플레이 | 게임 시작 |
| P | PAUSE, 입력 멈춤 |
| Resume | 플레이 재개 |
| Load(타이틀, 게스트) | LOGIN |
| 로그인 스텁 후 | SAVE_SLOTS |
| Title 나가기 | 확인창 |

## 테스트

- `AuthGuardTest`

## B/C

- **B:** LOGIN/SIGNUP/SETTINGS UI, AccountService 실구현
- **C:** RESULT, BGM onRouteChanged (리스너 추가 예정)

## 다음

- S9 통합·2인 골격 (Save는 나중에)
