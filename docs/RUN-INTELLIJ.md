# 실행 방법

프로젝트는 Java 17로 연다.

1. IntelliJ에서 `pom.xml` 있는 폴더를 연다.
2. Maven을 새로고침한다.
3. SDK가 Java 17인지 확인한다.
4. `kr.ac.jbnu.se.tetris.app.GameWindow`를 실행한다.

온라인 2인 플레이는 서버를 먼저 켜야 한다.

```text
kr.ac.jbnu.se.tetris.network.lobby.LobbyServerMain
```

기본 포트는 `8765`이다.

터미널에서 하면 프로젝트 루트에서 아래처럼 실행한다.

```powershell
mvn compile
mvn exec:java
mvn test
```

소스는 `src/main/java`, 리소스는 `src/main/resources`, 테스트는 `src/test/java`에 있다.
