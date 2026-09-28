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

## 3. Run
파일: `src/kr/ac/jbnu/se/tetris/app/GameWindow.java`  
Main class: `kr.ac.jbnu.se.tetris.app.GameWindow`  
→ **B파트 `GamePanel` UI** (1280×720, Galmuri 폰트) 전체가 뜸  
**Build → Rebuild Project** 후 `main` 옆 ▶

---

### Cursor 터미널에서 `target/classes`로 실행할 때
먼저 컴파일:
```powershell
powershell -File scripts/compile.ps1
java -cp target/classes kr.ac.jbnu.se.tetris.app.GameWindow
```
(`GameWindow.class` 없으면 ClassNotFoundException)
