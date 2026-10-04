package kr.ac.jbnu.se.tetris.boss;

public final class RisingBlockPattern implements BossPattern {

    @Override
    public int intervalMillis() {
        return 20_000;
    }

    @Override
    public int warningMillis() {
        return 3_000;
    }

    @Override
    public String warningMessage() {
        return "!! 파도가 밀려온다 !!";
    }

    @Override
    public void attack(BossPatternTarget target) {
        target.raiseBlocks();
        target.showBossMessage("해신이 파도를 일으켰다! 블록이 한 줄 올라온다");
    }
}
