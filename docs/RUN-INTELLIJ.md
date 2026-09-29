# IntelliJ 실행 (3단계)

## 1. Open
`Tetrise2026_3` **루트** (`pom.xml` 보이는 폴더)

## 2. Sources (Project Structure → Modules → Sources)
| 폴더 | 설정 |
|------|------|
| `src` | Sources (파란) — **`src/kr`만 X** |
| `src/main/java` | Sources (파란) |
| `test`, `src/test/java` | Test (초록) |

`src/main`, `src/test`는 **Excluded(주황)** 해도 됨 (A·B 겹침 방지)

## 3. Gson (온라인 로비)
`LobbyCodec`이 **Gson**을 씁니다. IntelliJ 모듈 `tetris2026`에 **`lib/gson-2.11.0.jar`** 가 연결돼 있어야 합니다.  
`package com.google.gson does not exist` → **File → Project Structure → Libraries**에 Gson이 있는지 확인, 없으면 `lib/gson-2.11.0.jar` 추가 후 **Rebuild**.

## 4. Run
파일: `src/kr/ac/jbnu/se/tetris/app/GameWindow.java`  
Main class: `kr.ac.jbnu.se.tetris.app.GameWindow`  
로비 서버: Run **`LobbyServerMain`** (포트 8765) — 게임과 **별도** 실행  
→ **B파트 `GamePanel` UI** (1280×720, Galmuri 폰트) 전체가 뜸  
**Build → Rebuild Project** 후 `main` 옆 ▶

팀 핸드오프(Git·로비·1PC 2P 설계): **`docs/team-handoff-coop-network.md`**

---

### Cursor 터미널에서 `target/classes`로 실행할 때
먼저 컴파일:
```powershell
powershell -File scripts/compile.ps1
java -cp "target/classes;lib/gson-2.11.0.jar" kr.ac.jbnu.se.tetris.app.GameWindow
java -cp "target/classes;lib/gson-2.11.0.jar" kr.ac.jbnu.se.tetris.network.lobby.LobbyServerMain
```
(`GameWindow.class` 없으면 ClassNotFoundException)
