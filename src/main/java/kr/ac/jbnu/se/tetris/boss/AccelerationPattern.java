package kr.ac.jbnu.se.tetris.boss;

import java.util.Optional;
import kr.ac.jbnu.se.tetris.audio.SfxType;

public final class AccelerationPattern implements BossPattern {

    private final AccelerationTarget target;

    public AccelerationPattern(AccelerationTarget target) {
        this.target = target;
    }

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
    public Optional<SfxType> activationSound() {
        return Optional.of(SfxType.STAGE_4_PATTERN);
    }

    @Override
    public void attack() {
        var forcedPiece = target.selectForcedPiece();
        target.replaceCurrentPiece(forcedPiece);
        target.startAcceleration(5_000, 5, 10);
        target.showBossMessage("눈보라! " + forcedPiece + " 블록이 강제로 내려온다");
    }
}
