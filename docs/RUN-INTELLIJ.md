# IntelliJ 실행 방법

## 1. 프로젝트 열기

IntelliJ에서 `pom.xml`이 보이는 저장소 최상위 `Tetrise2026_3` 폴더를 연다.

```text
Tetrise2026_3/
├─ pom.xml
├─ src/main/java/
├─ src/main/resources/
└─ src/test/java/
```

프로젝트의 Java SDK는 **Java 17**로 설정한다.

## 2. Maven 의존성 불러오기

게임은 다음 Maven 의존성을 사용한다.

- Gson: 저장 데이터와 온라인 로비
- MP3SPI: MP3 형식의 BGM과 효과음
- JUnit 5: 테스트

`pom.xml`을 연 뒤 우측 Maven 창에서 **Reload All Maven Projects**를 실행한다.

다음 오류가 나오면 Maven 의존성이 아직 반영되지 않은 상태다.

```text
package com.google.gson does not exist
MP3 음원을 읽을 수 없음
```

이 경우 다음 항목을 확인한다.

1. IntelliJ가 `pom.xml`을 Maven 프로젝트로 인식했는지 확인한다.
2. Maven 창에서 **Reload All Maven Projects**를 다시 실행한다.
3. **File → Project Structure → Project SDK**가 Java 17인지 확인한다.
4. **Build → Rebuild Project**를 실행한다.

## 3. 소스 폴더 확인

현재 통합 프로젝트는 표준 Maven 구조를 사용한다.

| 폴더 | IntelliJ 설정 |
|---|---|
| `src/main/java` | Sources |
| `src/main/resources` | Resources |
| `src/test/java` | Test Sources |

이전 구조였던 `src/kr` 경로는 더 이상 사용하지 않는다.

## 4. 음원 파일 확인

다음 MP3 파일이 `src/main/resources/sound` 폴더에 있어야 한다.

```text
Decktonic - BGM.mp3
press_button.mp3
line_clear1.mp3
line_clear2.mp3
turn_key1.mp3
dummy_line.mp3
blizzard.mp3
confusion.mp3
level_clear.mp3
```

파일 이름은 공백과 대소문자를 포함해 위 목록과 같아야 한다. 음원 파일이 없거나 재생에 실패하더라도 게임은 무음으로 계속 실행된다.

## 5. 게임 실행

실행 파일:

```text
src/main/java/kr/ac/jbnu/se/tetris/app/GameWindow.java
```

Main class:

```text
kr.ac.jbnu.se.tetris.app.GameWindow
```

실행 순서:

1. `GameWindow.java`를 연다.
2. `main` 메서드 옆의 실행 버튼을 누른다.
3. 실행 구성의 JRE가 Java 17인지 확인한다.
4. 1280×720 게임 화면이 표시되는지 확인한다.

## 6. 온라인 협동 실행

온라인 협동을 확인할 때는 로비 서버를 게임과 별도로 먼저 실행한다.

서버 Main class:

```text
kr.ac.jbnu.se.tetris.network.lobby.LobbyServerMain
```

기본 포트는 `8765`다. 서버 실행 후 `GameWindow`를 실행하고 온라인 협동 로비에서 서버에 연결한다.

## 7. 터미널에서 실행

시스템에 Maven이 설치되어 있다면 저장소 최상위 폴더에서 다음 명령을 사용한다.

```powershell
mvn compile
mvn exec:java
```

테스트는 다음 명령으로 실행한다.

```powershell
mvn test
```

MP3SPI는 여러 전이 의존성을 사용하므로 `java -cp`에 Gson JAR만 직접 지정하는 이전 실행 방식은 사용하지 않는다. IntelliJ의 Maven 실행 또는 `mvn exec:java`를 사용한다.

## 8. 실행 전 체크리스트

- [ ] 저장소 최상위 폴더를 열었다.
- [ ] Project SDK가 Java 17이다.
- [ ] Maven 프로젝트를 다시 불러왔다.
- [ ] `src/main/java`와 `src/main/resources`가 올바르게 인식된다.
- [ ] `src/main/resources/sound`에 MP3 파일 9개가 있다.
- [ ] `GameWindow` 실행 구성이 올바른 Main class를 사용한다.

팀 인수인계 문서는 [`team-handoff-coop-network.md`](team-handoff-coop-network.md)를 참고한다.
