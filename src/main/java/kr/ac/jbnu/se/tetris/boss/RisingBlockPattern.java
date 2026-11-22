package kr.ac.jbnu.se.tetris.boss;

import java.util.Optional;
import kr.ac.jbnu.se.tetris.audio.SfxType;

public final class RisingBlockPattern implements BossPattern {

    private final RisingBlockTarget target;

    public RisingBlockPattern(RisingBlockTarget target) {
        this.target = target;
    }

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
    public Optional<SfxType> activationSound() {
        return Optional.of(SfxType.STAGE_2_PATTERN);
    }

    @Override
    public void attack() {
        target.raiseBlocks(target.selectGarbageHole());
        target.showBossMessage("해신이 파도를 일으켰다! 블록이 한 줄 올라온다");
    }
}
