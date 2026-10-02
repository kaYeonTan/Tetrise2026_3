package kr.ac.jbnu.se.tetris.app;

// B F-09 전까지 로그인 스텁 (AuthGuard용)
public class AccountService {

	boolean loggedIn;
	String userId;

	public boolean isLoggedIn() {
		return loggedIn;
	}

	public String currentUserId() {
		return loggedIn ? userId : null;
	}

	public void loginStub(String userId) {
		this.loggedIn = true;
		this.userId = userId;
	}

	public void logout() {
		loggedIn = false;
		userId = null;
	}
}
