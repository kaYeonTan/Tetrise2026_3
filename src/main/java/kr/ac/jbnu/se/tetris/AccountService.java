package kr.ac.jbnu.se.tetris;

/** 회원가입·로그인 규칙(중복 검사, 대소문자 무시). 저장은 UserRepository에 맡긴다. */
public class AccountService {

    private final UserRepository users;

    public AccountService(UserRepository users) {
        if (users == null) {
            throw new IllegalArgumentException("users must not be null");
        }
        this.users = users;
    }

    /** 문제가 있으면 안내 문구, 성공하면 null. */
    public String signUp(String nickname, String email, String password) {
        for (UserRecord user : users.findAll()) {
            if (user.nick().equalsIgnoreCase(nickname)) {
                return "이미 사용 중인 닉네임이에요.";
            }
            if (user.email().equalsIgnoreCase(email)) {
                return "이미 가입된 이메일이에요.";
            }
        }
        if (!users.add(new UserRecord(nickname, email, PasswordHasher.hash(password)))) {
            return "저장에 실패했어요.";
        }
        return null;
    }

    public boolean login(String nickname, String password) {
        for (UserRecord user : users.findAll()) {
            if (user.nick().equalsIgnoreCase(nickname) && PasswordHasher.matches(password, user.password())) {
                upgradeLegacyPassword(user, password);
                return true;
            }
        }
        return false;
    }

    /** 평문으로 저장된 옛 계정은 로그인에 성공한 이 시점에만 해시로 바꿀 수 있다(평문을 알고 있으므로). */
    private void upgradeLegacyPassword(UserRecord user, String password) {
        if (!PasswordHasher.isHashed(user.password())) {
            users.replacePassword(user.nick(), PasswordHasher.hash(password));
        }
    }

    /** 가입 당시의 대소문자로 닉네임을 돌려준다. 없으면 입력값 그대로. */
    public String canonicalNick(String nickname) {
        for (UserRecord user : users.findAll()) {
            if (user.nick().equalsIgnoreCase(nickname)) {
                return user.nick();
            }
        }
        return nickname;
    }
}
