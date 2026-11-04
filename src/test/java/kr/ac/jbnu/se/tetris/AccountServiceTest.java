package kr.ac.jbnu.se.tetris;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class AccountServiceTest {

    private static class MemoryUsers implements UserRepository {
        final List<UserRecord> list = new ArrayList<>();
        boolean failOnAdd;

        @Override
        public List<UserRecord> findAll() {
            return new ArrayList<>(list);
        }

        @Override
        public boolean add(UserRecord user) {
            if (failOnAdd) {
                return false;
            }
            list.add(user);
            return true;
        }

        @Override
        public boolean replacePassword(String nick, String storedPassword) {
            for (int i = 0; i < list.size(); i++) {
                UserRecord u = list.get(i);
                if (u.nick().equalsIgnoreCase(nick)) {
                    list.set(i, new UserRecord(u.nick(), u.email(), storedPassword));
                    return true;
                }
            }
            return false;
        }
    }

    @Test
    void legacyPlainTextAccountIsUpgradedToHashOnLogin() {
        MemoryUsers repo = new MemoryUsers();
        repo.list.add(new UserRecord("Old", "o@x.com", "12345678"));
        AccountService s = new AccountService(repo);
        assertTrue(s.login("old", "12345678"));
        assertTrue(PasswordHasher.isHashed(repo.list.get(0).password()));
        assertTrue(s.login("Old", "12345678"));
    }

    @Test
    void failedLoginDoesNotTouchLegacyPassword() {
        MemoryUsers repo = new MemoryUsers();
        repo.list.add(new UserRecord("Old", "o@x.com", "12345678"));
        assertFalse(new AccountService(repo).login("Old", "wrong"));
        assertEquals("12345678", repo.list.get(0).password());
    }

    @Test
    void signUpStoresUserAndRejectsDuplicates() {
        MemoryUsers repo = new MemoryUsers();
        AccountService s = new AccountService(repo);
        assertNull(s.signUp("Hero", "h@x.com", "12345678"));
        assertEquals("이미 사용 중인 닉네임이에요.", s.signUp("hero", "o@x.com", "12345678"));
        assertEquals("이미 가입된 이메일이에요.", s.signUp("Other", "H@X.com", "12345678"));
        assertEquals(1, repo.list.size());
    }

    @Test
    void signUpReportsSaveFailure() {
        MemoryUsers repo = new MemoryUsers();
        repo.failOnAdd = true;
        assertEquals("저장에 실패했어요.", new AccountService(repo).signUp("Hero", "h@x.com", "12345678"));
    }

    @Test
    void loginIgnoresNickCaseButNotPasswordCase() {
        MemoryUsers repo = new MemoryUsers();
        AccountService s = new AccountService(repo);
        s.signUp("Hero", "h@x.com", "Passw0rd!");
        assertTrue(s.login("HERO", "Passw0rd!"));
        assertFalse(s.login("Hero", "passw0rd!"));
        assertFalse(s.login("nobody", "Passw0rd!"));
    }

    @Test
    void canonicalNickReturnsRegisteredCase() {
        MemoryUsers repo = new MemoryUsers();
        AccountService s = new AccountService(repo);
        s.signUp("Hero", "h@x.com", "12345678");
        assertEquals("Hero", s.canonicalNick("hERO"));
        assertEquals("zzz", s.canonicalNick("zzz"));
    }

    @Test
    void userRecordRoundTripsAndSkipsBrokenLines() {
        UserRecord u = UserRecord.fromLine("Hero,h@x.com,12345678");
        assertEquals("Hero,h@x.com,12345678", u.toLine());
        assertNull(UserRecord.fromLine("broken,line"));
    }

    @Test
    void rejectsNullRepository() {
        assertThrows(IllegalArgumentException.class, () -> new AccountService(null));
    }
}
