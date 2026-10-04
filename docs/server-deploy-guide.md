# 로비 서버 원격 배포 가이드 (3조)

> 팀원들이 **각자 다른 장소(집, 카페 등)**에서 같은 로비에 접속하려면  
> **서버는 딱 1대만** 실행하고, 나머지는 그 주소로 접속해야 합니다.

---

## 목차
1. [가장 쉬운 방법: ngrok](#1-가장-쉬운-방법-ngrok)
2. [같은 와이파이일 때: IP 직접 접속](#2-같은-와이파이일-때-ip-직접-접속)
3. [발표용: 클라우드 배포](#3-발표용-클라우드-배포)

---

## 1. 가장 쉬운 방법: ngrok

**ngrok**은 내 컴퓨터를 임시로 인터넷에 공개해주는 프로그램입니다.  
무료로 쓸 수 있고, 설치도 간단합니다.

### 1-1. ngrok 설치

1. https://ngrok.com/download 에서 다운로드
2. 압축 풀고 아무 폴더에 넣기 (예: `C:\ngrok\ngrok.exe`)
3. (선택) 회원가입하면 세션 시간 제한 없어짐

### 1-2. 서버 실행 + ngrok 연결

**터미널 1: 서버 실행**
```powershell
cd "C:\GitHub\My project\Tetrise2026_3"
.\scripts\start-server.ps1
```

**터미널 2: ngrok으로 외부 공개**
```powershell
cd C:\ngrok
.\ngrok tcp 8765
```

### 1-3. 주소 확인 및 공유

ngrok 실행하면 이런 화면이 나옵니다:
```
Forwarding    tcp://0.tcp.jp.ngrok.io:12345 -> localhost:8765
```

**팀 채팅에 공유할 내용:**
```
서버 주소: 0.tcp.jp.ngrok.io
포트: 12345
(숫자는 매번 바뀜)
```

### 1-4. 팀원들 접속

게임 실행 → 온라인 협동 → 
- 주소: `0.tcp.jp.ngrok.io` (ngrok에서 나온 주소)
- 포트: `12345` (ngrok에서 나온 포트)
- **서버 연결** 클릭

---

## 2. 같은 와이파이일 때: IP 직접 접속

학교나 카페에서 같은 와이파이 쓰면 ngrok 없이도 됩니다.

### 2-1. 서버 실행할 사람

1. 서버 실행
```powershell
.\scripts\start-server.ps1
```

2. 내 IP 확인
```powershell
ipconfig
```
→ `IPv4 주소` 찾기 (예: `192.168.0.15`)

3. 팀에 공유: `192.168.0.15:8765`

### 2-2. 접속할 사람

게임 → 온라인 협동 → 주소에 위 IP 입력

### ⚠️ 안 되면?

Windows 방화벽에서 8765 포트 열어야 합니다:
1. Windows 검색 → "방화벽" → 고급 설정
2. 인바운드 규칙 → 새 규칙
3. 포트 → TCP → 8765 → 허용

---

## 3. 발표용: 클라우드 배포

항상 같은 주소로 접속하고 싶으면 클라우드에 서버를 올립니다.

### 3-1. 서버 JAR 빌드

```powershell
cd "C:\GitHub\My project\Tetrise2026_3"
mvn package -DskipTests
```
→ `target/lobby-server.jar` 생성됨

### 3-2. Oracle Cloud (무료)

1. https://www.oracle.com/cloud/free/ 가입 (신용카드 필요하지만 무료)
2. VM 인스턴스 생성 (Ubuntu)
3. SSH 접속 후:
```bash
# Java 설치
sudo apt update
sudo apt install openjdk-17-jre-headless

# JAR 업로드 (WinSCP 등 사용)
# 실행
java -jar lobby-server.jar
```

4. 보안 그룹에서 TCP 8765 열기
5. 공인 IP로 접속

### 3-3. Fly.io (간단)

```bash
# fly.toml 파일 만들기
fly launch
fly deploy
```

(TCP 서버는 설정이 좀 복잡할 수 있음)

---

## 자주 묻는 질문

### Q: 왜 서버를 각자 켜면 안 되나요?
방 목록은 **서버 메모리**에 있습니다. 서버가 4개면 방 목록도 4개로 나뉘어서 서로 안 보입니다.

### Q: 서버 껐다 키면 방 사라지나요?
네. 지금은 DB 없이 메모리만 씁니다. 발표 직전에 방 새로 만들면 됩니다.

### Q: ngrok 무료 제한은?
- 세션 8시간 (재시작하면 됨)
- 주소가 매번 바뀜 (팀에 다시 공유)
- 회원가입하면 좀 더 길어짐

### Q: in-game 플레이도 되나요?
아직 로비(방 만들기/입장)까지만 구현됐습니다.  
실제 2인 동시 플레이는 다음 작업입니다.

---

## 요약

| 상황 | 방법 |
|------|------|
| 빠른 테스트 | ngrok (1번) |
| 같은 와이파이 | IP 직접 (2번) |
| 발표/항상 같은 주소 | 클라우드 (3번) |

**핵심: 서버는 1대만! 나머지는 그 주소로 접속!**
