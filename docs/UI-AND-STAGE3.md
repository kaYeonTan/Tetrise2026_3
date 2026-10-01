# UI 개선 및 스테이지 3 도안 시스템

이 문서는 1인/2인 게임 UI 통합 작업과 스테이지 3 도안 패턴의 현재 구현을 설명한다.

## 1. UI 개선

### 1.1 1인 모드 렌더링 통합

기존 1인 모드는 `Board`, HOLD, NEXT 같은 Swing 컴포넌트를 `GamePanel` 위에 별도로 올리는 구조였다. 이 때문에 일시정지 창, 게임 종료 화면, 메인 화면보다 블록과 HUD가 앞에 남는 문제가 있었다.

현재는 게임 로직만 기존 1인 엔진을 사용하고 화면은 `GamePanel`이 직접 그린다.

- 보드와 고정 블록: `GamePanel.drawSingleEngineBoard(...)`
- 현재 낙하 블록: 1인 엔진의 `GameSession` 상태를 읽어 렌더링
- HOLD/NEXT: 2인 모드와 같은 `drawMini(...)` 사용
- 블록 셀: 1인/2인 모두 `drawCell(...)` 사용
- 보드 배경과 그리드: 1인/2인 모두 `GamePanel.drawGame(...)`에서 렌더링
- 일시정지, 게임 종료, 메인 화면: 모두 같은 `GamePanel` 페인팅 순서에 포함

`ASinglePlayerBridge`는 화면 컴포넌트를 추가하지 않는다. 숨겨진 `Board`를 게임 엔진으로만 사용하고, `GameSession` 상태와 게임 이벤트를 `GamePanel`에 전달한다.

관련 파일:

- [`GamePanel.java`](../src/main/java/kr/ac/jbnu/se/tetris/GamePanel.java)
- [`ASinglePlayerBridge.java`](../src/main/java/kr/ac/jbnu/se/tetris/integration/ASinglePlayerBridge.java)

### 1.2 스테이지 3 UI 표시

보드 아래쪽 6행에 선택된 도안을 표시한다.

| 표시 | 의미 |
|---|---|
| 금색 반투명 칸 | 채워야 하는 목표 칸 |
| 빨간 X | 비워야 하는 금지 칸 |
| 녹색 테두리 | 해당 플레이어가 채운 목표 칸 |
| 회청색 음영과 빗금 | 2인 모드에서 상대만 채운 목표 칸 |
| 빨간 오버레이 | 금지 칸에 블록을 놓은 상태 |

2인 모드에서 두 플레이어가 같은 목표 좌표를 모두 채운 경우에는 각 보드에서 자신의 블록에 녹색 테두리를 표시한다. 상대방 음영은 자신의 해당 좌표가 비어 있을 때만 표시된다.

상단 진행 표시에는 목표 칸 완성률이 나타난다. 금지 칸 위반이 있으면 `MISPLACED N`으로 위반 개수를 함께 표시한다.

## 2. 스테이지 3 규칙

스테이지 3은 일반 보스 HP 전투가 아니라 도안 완성 스테이지다.

1. 모든 목표 칸을 고정된 블록으로 채운다.
2. 모든 금지 칸을 비워 둔다.
3. 두 조건을 동시에 만족하면 `BLUEPRINT COMPLETE!`와 함께 스테이지를 클리어한다.
4. 현재 떨어지는 블록은 판정에 포함하지 않고 보드에 고정된 블록만 사용한다.
5. 스테이지 3에서는 줄 제거로 보스 HP가 감소하지 않는다.

### 2.1 1인 모드

1인 엔진 보드의 아래쪽 6행만 도안 판정에 사용한다. 한 보드에서 모든 목표 칸과 금지 칸 조건을 만족해야 한다.

### 2.2 2인 모드

2인 모드는 협동 판정이다. 같은 좌표의 1P 보드와 2P 보드를 합집합으로 계산한다.

- 목표 칸: 1P 또는 2P 중 한 명만 채워도 충족
- 금지 칸: 1P와 2P 양쪽 모두 비어 있어야 충족
- 두 플레이어가 목표 칸을 나누어 완성할 수 있음
- 한 명이라도 블록 생성에 실패하거나 보드가 넘치면 전체 게임 패배

## 3. 도안 데이터 형식

도안은 `Blueprint`에 6행 × 10열 문자열로 정의한다.

| 문자 | 의미 |
|---|---|
| `T` | 반드시 채워야 하는 목표 칸(Target) |
| `E` | 반드시 비워야 하는 금지 칸(Empty) |
| `-` | 승리 조건에 영향을 주지 않는 자유 칸 |

도안에는 다음 검증 규칙이 적용된다.

- 정확히 6행이어야 한다.
- 각 행은 정확히 10칸이어야 한다.
- 각 행에는 금지 칸 `E`가 최소 1개 있어야 한다.
- 맨 아래 행에는 목표 칸 `T`가 최소 1개 있어야 한다.
- 모든 목표 칸은 상·하·좌·우 방향으로 하나의 영역에 연결되어야 한다.
- 대각선으로만 접한 목표 칸은 연결된 것으로 인정하지 않는다.

규칙을 위반한 도안은 생성할 때 `IllegalArgumentException`이 발생한다.

## 4. 현재 도안 목록

스테이지 3에 진입할 때 아래 6개 중 하나를 동일한 확률로 선택한다.

### 도안 1

```text
E--TTT---E
-ETTTT---E
ETTTTT---E
TTTTTT-E--
TTTTTT--E-
TTTETTT-E-
```

### 도안 2

```text
E---TTT--E
--ETTTT-E-
-ETTTTT--E
E-TTTTTT--
-TTTTTT-E-
TTTETTT-E-
```

### 도안 3

```text
E----TTT-E
E---TTTT-E
E--TTTTT-E
E-TTTTTT--
ETTTTTT---
E-TTTETTT-
```

### 도안 4

```text
E--TTTT--E
-E-TTTT-E-
E-TTTTT--E
-TTTTTT-E-
ETTTTTT---
TTETTTT-E-
```

### 도안 5

```text
E-TTTT---E
E-TTTTT-E-
-ETTTTTT-E
E-TTTTTT--
-TTTTTT-E-
TTTTETTT-E
```

### 도안 6

```text
E-TTT----E
-ETTTT---E
E-TTTTT-E-
-TTTTTT-E-
E-TTTTTT--
TTTETTT-E-
```

## 5. 랜덤 선택과 저장

`BossPatternController`가 스테이지 3 시작 시 `Random.nextInt(6)`으로 도안 번호를 선택한다. 선택된 번호는 `blueprint` 항목으로 저장 파일에 기록된다.

이어하기에서는 저장된 번호로 같은 도안을 복원한다. `blueprint` 항목이 없는 기존 저장 파일은 스테이지 3을 불러올 때 새 도안을 무작위로 선택한다.

관련 파일:

- [`Blueprint.java`](../src/main/java/kr/ac/jbnu/se/tetris/boss/Blueprint.java): 도안 목록, 문자 해석, 도안 검증
- [`BlueprintPattern.java`](../src/main/java/kr/ac/jbnu/se/tetris/boss/BlueprintPattern.java): 진행률, 금지 칸 위반, 완성 판정
- [`BossPatternController.java`](../src/main/java/kr/ac/jbnu/se/tetris/boss/BossPatternController.java): 랜덤 도안 선택과 복원
- [`FileManager.java`](../src/main/java/kr/ac/jbnu/se/tetris/FileManager.java): 선택된 도안 번호 저장/불러오기

## 6. 새 도안 추가 방법

1. `Blueprint.STAGE_THREE_PATTERNS`에 6행 문자열 배열을 추가한다.
2. 각 문자열이 10글자인지 확인한다.
3. 맨 아래 행에 `T`를 하나 이상 배치한다.
4. 모든 `T`가 상·하·좌·우로 연결되도록 만든다.
5. 각 행에 `E`를 하나 이상 배치한다.
6. `BossPatternControllerTest`를 실행해 도안 검증과 중복 여부를 확인한다.

도안 개수는 `stageThreePatternCount()`에서 배열 길이로 계산하므로 별도의 숫자 상수를 수정할 필요가 없다.

## 7. 테스트

관련 테스트는 다음 파일에 있다.

- [`BossPatternControllerTest.java`](../src/test/java/kr/ac/jbnu/se/tetris/boss/BossPatternControllerTest.java)
  - 맨 아래 목표 칸 검증
  - 목표 칸 연결 검증
  - 6개 도안의 유효성과 중복 여부
  - 도안 완성 및 금지 칸 위반 판정
  - 저장된 도안 번호 복원
- [`GamePanelTest.java`](../src/test/java/kr/ac/jbnu/se/tetris/GamePanelTest.java)
  - 2인 보드 합집합 판정
  - 상대가 채운 목표 칸 판정
  - 스테이지 3 저장/불러오기

현재 확인 결과:

- 보스 패턴 테스트: 9개 통과
- 게임 패널 테스트: 11개 통과
- Java 17 전체 소스 컴파일 통과

## 8. 아직 구현하지 않은 항목

스테이지 3의 목숨 3개 시스템은 설계 가능 여부만 확인했으며 아직 구현하지 않았다. 적용하려면 다음 정책을 먼저 정해야 한다.

- 목숨을 1P/2P가 공유할지 각자 가질지
- 목숨을 잃었을 때 두 보드를 모두 초기화할지 해당 플레이어 보드만 초기화할지
- 재도전할 때 같은 도안을 유지할지 새 도안을 선택할지
- 남은 목숨을 저장 파일과 UI에 어떻게 표시할지
