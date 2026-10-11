package kr.ac.jbnu.se.tetris.boss;

public final class AccelerationPattern implements BossPattern {

    @Override
    public int intervalMillis() {
        return 30_000;
    }

    @Override
    public int warningMillis() {
        return 3_000;
    }

    @Override
    public String warningMessage() {
        return "!! 눈보라가 몰려온다 !!";
    }

    @Override
    public void attack(BossPatternTarget target) {
        var forcedPiece = target.selectForcedPiece();
        target.replaceCurrentPiece(forcedPiece);
        target.startAcceleration(5_000, 3, 10);
        target.showBossMessage("눈보라! " + forcedPiece + " 블록이 강제로 내려온다");
    }
}
