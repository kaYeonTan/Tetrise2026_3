# 보스 패턴 메모

보스 패턴은 `GamePanel`에서 시간을 재지 않고 `BossPatternController`에서 처리한다.

- 1스테이지: 시간 공격 없음
- 2스테이지: 20초마다 방해 줄 올림
- 3스테이지: 아래 도안 맞추는 스테이지
- 4스테이지: 30초마다 낙하 속도 빨라짐
- 5스테이지: 20초마다 조작키가 잠깐 바뀜

공격 전에는 경고 문구가 먼저 나온다. 일시정지하면 패턴 시간도 멈춘다.

피해를 받으면 스테이지별로 화면 이펙트가 나온다.

- 1: 가시
- 2: 파도, 번개
- 3: 청사진을 완성해서 보스를 쓰러뜨릴 때 나뭇잎
- 4: 얼음, 눈
- 5: 낮, 밤, 낮으로 화면 전환

주로 보는 파일:

- `GamePanel.java`
- `BossPatternController.java`
- `RisingBlockPattern.java`
- `AccelerationPattern.java`
- `RandomKeyPattern.java`
