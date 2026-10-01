# Hui 브랜치 작업 인수인계

이 문서는 Hui 브랜치에서 진행한 1인/2인 UI 통합, 블록 속도 통일, 스테이지 3 도안 시스템 작업을 다른 팀원이 확인할 수 있도록 정리한 문서다.

프로젝트 실행 자체는 기존 문서인 [`RUN-INTELLIJ.md`](RUN-INTELLIJ.md)를 참고한다.

## 1. 작업 요약

- 1인 모드의 보드, 블록, HOLD, NEXT UI를 `GamePanel` 렌더링으로 통합했다.
- 1인 보드에도 2인 보드와 같은 배경과 그리드를 적용했다.
- 일시정지, 게임 종료, 메인 화면에서 1인 UI가 앞에 남던 문제를 해결했다.
- 1인과 2인의 스테이지별 자동 낙하 속도를 통일했다.
- 스테이지 3에 도안 완성 방식의 승리 조건을 추가했다.
- 스테이지 3 도안 6개를 등록하고 진입할 때 무작위로 선택하도록 했다.
- 2인 스테이지 3에서 두 보드를 합쳐 도안을 완성할 수 있도록 했다.
- 상대방이 채운 목표 칸을 자신의 보드에 음영으로 표시하도록 했다.
- 선택된 도안이 저장 후 이어하기에서도 유지되도록 했다.

## 2. 1인/2인 UI 통합

### 변경 전

1인 모드의 `Board`, HOLD, NEXT가 별도 Swing 컴포넌트로 `GamePanel` 위에 올라가 있었다. 이 때문에 다음 화면에서도 블록이나 HUD가 앞에 남았다.

- 일시정지 팝업
- 게임 종료 화면
- 메인 화면

### 변경 후

1인 모드는 기존 A 엔진의 게임 로직만 사용하고, 화면은 2인 모드와 동일하게 `GamePanel`이 직접 그린다.

- 고정 블록과 현재 블록: `GamePanel.drawSingleEngineBoard(...)`
- 블록 셀: `drawCell(...)`
- HOLD/NEXT: `drawMini(...)`
- 보드 배경과 그리드: `GamePanel.drawGame(...)`

`ASinglePlayerBridge`는 화면 컴포넌트를 추가하지 않고 숨겨진 `Board`에서 `GameSession` 상태와 이벤트만 전달한다.

주요 파일:

- `src/main/java/kr/ac/jbnu/se/tetris/GamePanel.java`
- `src/main/java/kr/ac/jbnu/se/tetris/integration/ASinglePlayerBridge.java`
- `src/kr/ac/jbnu/se/tetris/Board.java`

## 3. 블록 낙하 속도 통일

기존 1인 모드는 모든 스테이지에서 `400ms` 고정이었고, 2인 모드는 스테이지마다 달라서 1인이 초반부터 훨씬 빨랐다.

현재는 두 모드 모두 다음 속도를 사용한다.

| 스테이지 | 기본 낙하 주기 |
|---|---:|
| 1 | 1000ms |
| 2 | 850ms |
| 3 | 700ms |
| 4 | 550ms |
| 5 | 400ms |

스테이지 4의 3배 가속 효과도 동일한 기본 속도 위에서 적용된다.

관련 코드:

- `GamePanel.baseDropIntervalMillis(...)`
- `Board.setBaseGravityDelay(...)`
- `ASinglePlayerBridge.startStage(...)`

## 4. 스테이지 3 도안 시스템

스테이지 3은 일반 보스 HP 전투가 아니라 도안을 완성하는 스테이지다.

### 도안 문자

| 문자 | 의미 |
|---|---|
| `T` | 반드시 채워야 하는 목표 칸 |
| `E` | 반드시 비워야 하는 금지 칸 |
| `-` | 승리 조건과 무관한 자유 칸 |

### 승리 조건

1. 모든 목표 칸을 고정된 블록으로 채운다.
2. 모든 금지 칸을 비워 둔다.
3. 두 조건을 동시에 만족하면 `BLUEPRINT COMPLETE!`가 표시되고 스테이지가 클리어된다.

현재 떨어지는 블록은 판정하지 않고 보드에 고정된 블록만 사용한다. 스테이지 3에서는 줄 제거로 보스 HP가 감소하지 않는다.

### 도안 생성 규칙

- 크기는 6행 × 10열이다.
- 각 행에는 금지 칸 `E`가 최소 1개 있어야 한다.
- 맨 아래 행에는 목표 칸 `T`가 최소 1개 있어야 한다.
- 모든 목표 칸은 상·하·좌·우로 연결되어야 한다.
- 대각선으로만 닿은 목표 칸은 연결된 것으로 인정하지 않는다.

규칙을 위반한 도안은 생성 시 `IllegalArgumentException`이 발생한다.

### 무작위 도안과 저장

- 도안 6개 중 하나를 스테이지 3 진입 시 무작위로 선택한다.
- 선택된 도안 번호를 저장 파일의 `blueprint` 항목에 기록한다.
- 이어하기에서는 저장된 도안을 다시 사용한다.
- `blueprint` 항목이 없는 기존 저장 파일은 도안을 새로 무작위 선택한다.

## 5. 스테이지 3의 2인 협동 판정

2인 모드에서는 같은 좌표의 1P 보드와 2P 보드를 합쳐 판정한다.

- 목표 칸은 1P 또는 2P 중 한 명만 채워도 충족된다.
- 두 플레이어가 목표 칸을 나눠서 채울 수 있다.
- 금지 칸은 양쪽 보드 모두 비어 있어야 한다.
- 한쪽이라도 금지 칸을 채우면 `MISPLACED N` 위반으로 처리한다.

UI 표시는 다음과 같다.

| 표시 | 의미 |
|---|---|
| 금색 반투명 칸 | 아직 채워야 하는 목표 칸 |
| 빨간 X | 비워야 하는 금지 칸 |
| 녹색 테두리 | 자신이 채운 목표 칸 |
| 회청색 음영과 빗금 | 상대방만 채운 목표 칸 |
| 빨간 오버레이 | 금지 칸을 채운 상태 |

양쪽 플레이어가 같은 목표 좌표를 모두 채우면 상대 음영 대신 자신의 녹색 테두리가 표시된다.

상세 규칙과 현재 도안 6개는 [`UI-AND-STAGE3.md`](UI-AND-STAGE3.md)를 참고한다.

## 6. 주요 파일

| 파일 | 역할 |
|---|---|
| `GamePanel.java` | 통합 UI, 2인 게임, 도안 표시 및 승리 연결 |
| `ASinglePlayerBridge.java` | 기존 1인 엔진과 통합 UI 연결 |
| `Board.java` | 1인 엔진의 기본 낙하 속도 적용 |
| `Blueprint.java` | 도안 6개, 도안 해석 및 유효성 검증 |
| `BlueprintPattern.java` | 진행률, 위반 개수 및 도안 완성 판정 |
| `BossPatternController.java` | 무작위 도안 선택과 저장 도안 복원 |
| `BossPattern.java` | 도안 패턴용 공통 인터페이스 확장 |
| `BossPatternTarget.java` | 보드 점유 확인과 도안 완료 콜백 |
| `FileManager.java` | 선택된 도안 번호 저장 및 불러오기 |

## 7. IntelliJ 모듈 주의사항

현재 프로젝트는 다음 두 소스 구조를 함께 사용한다.

```text
A 엔진: src/kr
B UI:   src/main/java
```

팀에서는 `tetris2026.iml` 기반의 IntelliJ 모듈 `tetris2026`을 사용한다. `GameWindow` 실행 설정의 `Use classpath of module`도 `tetris2026`이어야 한다.

현재 상태에서 `Reload All Maven Projects`를 실행하면 Maven 모듈 `tetris-team3`이 생성되고 `src/test/java`가 일반 소스로 잘못 인식되어 다음 오류가 발생할 수 있다.

```text
package org.junit.jupiter.api does not exist
```

이 경우 `File → Project Structure → Modules → Import Module`에서 `tetris2026.iml`을 다시 불러온다.

## 8. 테스트 결과

- 보스 패턴 테스트 9개 통과
- 게임 패널 테스트 12개 통과
- Java 17 전체 소스 컴파일 통과
- Java 17 전체 테스트 컴파일 통과

주요 테스트 파일:

- `src/test/java/kr/ac/jbnu/se/tetris/GamePanelTest.java`
- `src/test/java/kr/ac/jbnu/se/tetris/boss/BossPatternControllerTest.java`

## 9. 팀원 확인 체크리스트

`git pull` 후 다음 항목을 확인한다.

- [ ] 저장소 최상위 `Tetrise2026_3` 폴더를 열었다.
- [ ] IntelliJ 모듈이 `tetris2026`이다.
- [ ] `GameWindow`가 Java 17과 `tetris2026` 클래스패스를 사용한다.
- [ ] `src/test/java`와 `test`가 Test Sources로 표시된다.
- [ ] 1인 모드의 보드, HOLD, NEXT가 정상적으로 표시된다.
- [ ] 일시정지와 게임 종료 화면이 1인 UI보다 앞에 표시된다.
- [ ] 같은 스테이지에서 1인과 2인의 자동 낙하 속도가 비슷하다.
- [ ] 스테이지 3 진입 시 도안이 표시된다.
- [ ] 2인 스테이지 3에서 상대가 채운 칸의 음영이 표시된다.
- [ ] 저장 후 이어하기에서 같은 도안이 유지된다.

## 10. 아직 구현하지 않은 항목

스테이지 3의 목숨 3개 기능은 가능 여부만 검토했으며 아직 구현하지 않았다. 적용하려면 2인 모드에서 목숨을 공유할지, 개인별로 가질지 먼저 결정해야 한다.
