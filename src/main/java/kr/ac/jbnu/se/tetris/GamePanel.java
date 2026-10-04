package kr.ac.jbnu.se.tetris;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import kr.ac.jbnu.se.tetris.boss.BossPatternController;
import kr.ac.jbnu.se.tetris.boss.BossPatternTarget;
import kr.ac.jbnu.se.tetris.boss.Blueprint;
import kr.ac.jbnu.se.tetris.audio.BgmType;
import kr.ac.jbnu.se.tetris.audio.SfxType;
import kr.ac.jbnu.se.tetris.audio.SoundManager;
import kr.ac.jbnu.se.tetris.core.GameSession;
import kr.ac.jbnu.se.tetris.core.PlayerAction;
import kr.ac.jbnu.se.tetris.integration.ASinglePlayerBridge;
import kr.ac.jbnu.se.tetris.integration.CoopEngineBridge;
import kr.ac.jbnu.se.tetris.persistence.CoopGameMemento;
import kr.ac.jbnu.se.tetris.persistence.LoadException;
import kr.ac.jbnu.se.tetris.persistence.SaveService;
import kr.ac.jbnu.se.tetris.persistence.SaveSlotMeta;
import kr.ac.jbnu.se.tetris.network.coop.CoopNetworkBridge;
import kr.ac.jbnu.se.tetris.network.coop.CoopSnapshotMapper;
import com.google.gson.Gson;
import kr.ac.jbnu.se.tetris.network.lobby.LobbyClient;

public class GamePanel extends JPanel implements ActionListener, KeyListener, MouseListener, MouseMotionListener, FocusListener, BossPatternTarget {

    private static final long serialVersionUID = 1L;

    // 0 타이틀 1 저장목록 2 인원선택 3 랭킹 4 설정 5 게임 6 일시정지 7 결과
    public int screen = 0;
    int settingFrom = 0;

    Timer timer;
    int tick = 0;
    int flameFrame = 0;
    float glow = 1f;
    int mouseX, mouseY;

    public String loginUser = null;

    public Font font;
    Font fontBold;
    Font fontSmall;
    transient BufferedImage mapImg;

    public boolean isTwoPlayer = false;
    /** 2PC 온라인 협동 — 각 PC는 1인 키로 자기 보드만 조작. */
    public boolean isOnlineCoop = false;
    public int localPlayerIndex = 0;
    public String onlineCoopId;
    public transient CoopNetworkBridge coopBridge;
    public transient LobbyClient onlineLobbyClient;
    transient LobbyFrame onlineLobbyFrame;
    public transient CoopEngineBridge coopEngineBridge;
    public String lastOnlineRoomId;
    public CoopGameMemento pendingCoopResume;
    java.util.List<SaveSlotMeta> coopSlotList = new ArrayList<>();
    /** 8=협동 슬롯 UI (저장/불러오기) */
    int coopSlotMode;
    public int stage = 1;
    public int bossHp;
    public int bossMax;
    public int bossTimer;
    public long gameTime;
    public boolean bossWarning = false;
    public String bossWarningText = "";
    public long blizzardEnd = 0;
    public boolean stageClear = false;
    boolean stageClearSoundPlayed;
    int clearTimer = 0;
    boolean allClear = false;
    public int hitEffect = 0;
    public String saveFile = null;
    public String msg = "";
    public int msgTimer = 0;
    String resultMsg = "";

    public int[][] board1 = new int[24][10];
    public int p1Type, p1Rot, p1X, p1Y;
    public int p1Hold = -1;
    public boolean p1HoldUsed = false;
    public ArrayList<Integer> p1Next = new ArrayList<>();
    public ArrayList<Integer> p1Bag = new ArrayList<>();
    public int p1Score, p1Lines;
    public int p1Garbage;
    int p1DropCount;
    /** 좌·우: 첫 입력 후 이 시간(ms) 동안은 추가 이동 없음 (짧은 탭이 여러 칸으로 잡히는 것 방지) */
    static final int HORIZONTAL_DAS_DELAY_MS = 400;
    static final int HORIZONTAL_ARR_INTERVAL_MS = 90;
    static final int SOFT_DROP_REPEAT_MS = 50;
    int p1LeftCount, p1RightCount, p1DownCount;

    public int[][] board2 = new int[24][10];
    public int p2Type, p2Rot, p2X, p2Y;
    public int p2Hold = -1;
    public boolean p2HoldUsed = false;
    public ArrayList<Integer> p2Next = new ArrayList<>();
    public ArrayList<Integer> p2Bag = new ArrayList<>();
    public int p2Score, p2Lines;
    public int p2Garbage;
    int p2DropCount;
    int p2LeftCount, p2RightCount, p2DownCount;

    // 왼 오 아래 스페이스 회전 반대회전 홀드
    public static int[] keySingle = {KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN, KeyEvent.VK_SPACE, KeyEvent.VK_UP, KeyEvent.VK_Z, KeyEvent.VK_C};
    public static int[] keyP1 = {KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_S, KeyEvent.VK_SPACE, KeyEvent.VK_W, KeyEvent.VK_Q, KeyEvent.VK_E};
    public static int[] keyP2 = {KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN, KeyEvent.VK_ENTER, KeyEvent.VK_UP, KeyEvent.VK_SLASH, KeyEvent.VK_PERIOD};
    public static int keyPause = KeyEvent.VK_P;

    HashSet<Integer> pressed = new HashSet<>();

    int setTab = 0;
    int captureRow = -1;
    int[][] editKeys;
    String setMsg = "";
    boolean setMsgError = false;
    String[] actionNames = {"왼쪽 이동", "오른쪽 이동", "천천히 내리기", "바로 떨어뜨리기", "시계 방향 회전", "반시계 방향 회전", "홀드"};

    int rankTab = 0;
    ArrayList<String[]> rankList = new ArrayList<>();
    ArrayList<String[]> saveList = new ArrayList<>();

    Random random = new Random();
    public final BossPatternController bossPattern = new BossPatternController(random);
    private final transient SoundManager soundManager;
    int gravityMultiplier = 1;
    boolean p1FastNext;
    boolean p2FastNext;
    boolean p1FastCurrent;
    boolean p2FastCurrent;
    int[] temporaryPrimaryKeys;
    int[] temporarySecondaryKeys;
    long randomKeyEnd;

    /** 1인: A GameEngine + Board (B UI 위에 올림) */
    transient ASinglePlayerBridge aEngineBridge;

    // I O T S Z J L P U V X F
    static int[][][] SHAPES = {
        {{0, 1, 1, 1, 2, 1, 3, 1}, {2, 0, 2, 1, 2, 2, 2, 3}, {0, 2, 1, 2, 2, 2, 3, 2}, {1, 0, 1, 1, 1, 2, 1, 3}},
        {{1, 0, 2, 0, 1, 1, 2, 1}, {1, 0, 2, 0, 1, 1, 2, 1}, {1, 0, 2, 0, 1, 1, 2, 1}, {1, 0, 2, 0, 1, 1, 2, 1}},
        {{1, 0, 0, 1, 1, 1, 2, 1}, {1, 0, 1, 1, 2, 1, 1, 2}, {0, 1, 1, 1, 2, 1, 1, 2}, {1, 0, 0, 1, 1, 1, 1, 2}},
        {{1, 0, 2, 0, 0, 1, 1, 1}, {1, 0, 1, 1, 2, 1, 2, 2}, {1, 1, 2, 1, 0, 2, 1, 2}, {0, 0, 0, 1, 1, 1, 1, 2}},
        {{0, 0, 1, 0, 1, 1, 2, 1}, {2, 0, 1, 1, 2, 1, 1, 2}, {0, 1, 1, 1, 1, 2, 2, 2}, {1, 0, 0, 1, 1, 1, 0, 2}},
        {{0, 0, 0, 1, 1, 1, 2, 1}, {1, 0, 2, 0, 1, 1, 1, 2}, {0, 1, 1, 1, 2, 1, 2, 2}, {1, 0, 1, 1, 0, 2, 1, 2}},
        {{2, 0, 0, 1, 1, 1, 2, 1}, {1, 0, 1, 1, 1, 2, 2, 2}, {0, 1, 1, 1, 2, 1, 0, 2}, {0, 0, 1, 0, 1, 1, 1, 2}},
        {{0, 0, 1, 0, 0, 1, 1, 1, 1, 2}, {1, 0, 2, 0, 0, 1, 1, 1, 2, 1}, {0, 0, 0, 1, 1, 1, 0, 2, 1, 2}, {0, 0, 1, 0, 2, 0, 0, 1, 1, 1}},
        {{0, 0, 2, 0, 0, 1, 1, 1, 2, 1}, {0, 0, 1, 0, 0, 1, 0, 2, 1, 2}, {0, 0, 1, 0, 2, 0, 0, 1, 2, 1}, {0, 0, 1, 0, 1, 1, 0, 2, 1, 2}},
        {{0, 0, 0, 1, 0, 2, 1, 2, 2, 2}, {0, 0, 1, 0, 2, 0, 0, 1, 0, 2}, {0, 0, 1, 0, 2, 0, 2, 1, 2, 2}, {2, 0, 2, 1, 0, 2, 1, 2, 2, 2}},
        {{1, 0, 0, 1, 1, 1, 2, 1, 1, 2}, {1, 0, 0, 1, 1, 1, 2, 1, 1, 2}, {1, 0, 0, 1, 1, 1, 2, 1, 1, 2}, {1, 0, 0, 1, 1, 1, 2, 1, 1, 2}},
        {{1, 0, 2, 0, 0, 1, 1, 1, 1, 2}, {1, 0, 0, 1, 1, 1, 2, 1, 2, 2}, {1, 0, 1, 1, 2, 1, 0, 2, 1, 2}, {0, 0, 0, 1, 1, 1, 2, 1, 1, 2}}
    };

    // 월킥. 0~3 시계방향(현재 회전 기준), 4~7 반시계
    static int[][][] KICK = {
        {
            {0, 0, -1, 0, -1, 1, 0, -2, -1, -2}, {0, 0, 1, 0, 1, -1, 0, 2, 1, 2},
            {0, 0, 1, 0, 1, 1, 0, -2, 1, -2}, {0, 0, -1, 0, -1, -1, 0, 2, -1, 2},
            {0, 0, 1, 0, 1, 1, 0, -2, 1, -2}, {0, 0, 1, 0, 1, -1, 0, 2, 1, 2},
            {0, 0, -1, 0, -1, 1, 0, -2, -1, -2}, {0, 0, -1, 0, -1, -1, 0, 2, -1, 2}
        },
        {
            {0, 0, -2, 0, 1, 0, -2, -1, 1, 2}, {0, 0, -1, 0, 2, 0, -1, 2, 2, -1},
            {0, 0, 2, 0, -1, 0, 2, 1, -1, -2}, {0, 0, 1, 0, -2, 0, 1, -2, -2, 1},
            {0, 0, -1, 0, 2, 0, -1, 2, 2, -1}, {0, 0, 2, 0, -1, 0, 2, 1, -1, -2},
            {0, 0, 1, 0, -2, 0, 1, -2, -2, 1}, {0, 0, -2, 0, 1, 0, -2, -1, 1, 2}
        }
    };

    @SuppressWarnings("this-escape")
    public GamePanel() {
        this(new SoundManager());
    }

    @SuppressWarnings("this-escape")
    public GamePanel(SoundManager soundManager) {
        this.soundManager = soundManager == null ? new SoundManager() : soundManager;
        setPreferredSize(new Dimension(1280, 720));
        setFocusable(true);
        setLayout(null);
        try {
            InputStream in = getClass().getResourceAsStream("/fonts/Galmuri11.ttf");
            font = Font.createFont(Font.TRUETYPE_FONT, in);
            in = getClass().getResourceAsStream("/fonts/Galmuri11-Bold.ttf");
            fontBold = Font.createFont(Font.TRUETYPE_FONT, in);
            in = getClass().getResourceAsStream("/fonts/Galmuri9.ttf");
            fontSmall = Font.createFont(Font.TRUETYPE_FONT, in);
            mapImg = ImageIO.read(getClass().getResourceAsStream("/images/worldmap.png"));
        } catch (java.awt.FontFormatException | java.io.IOException e) {
            System.out.println("font or image load error");
            font = new Font("Dialog", Font.PLAIN, 12);
            fontBold = new Font("Dialog", Font.BOLD, 12);
            fontSmall = font;
        }
        SwingUtilities.invokeLater(() -> {
            addKeyListener(this);
            addMouseListener(this);
            addMouseMotionListener(this);
            addFocusListener(this);
        });
        setFocusTraversalKeysEnabled(false);
        FileManager.loadKeys();
        timer = new Timer(16, this);
        timer.start();
    }

    public void playButtonClickSound() {
        soundManager.playSfx(SfxType.BUTTON_CLICK);
    }

    public void playBlockLockSound() {
        soundManager.playSfx(SfxType.BLOCK_LOCK);
    }

    public void playLineClearSound(int lines) {
        if (lines == 1) {
            soundManager.playSfx(SfxType.LINE_CLEAR_SINGLE);
        } else if (lines >= 2) {
            soundManager.playSfx(SfxType.LINE_CLEAR_MULTI);
        }
    }

    public void startBattleBgm() {
        soundManager.playBgm(BgmType.BATTLE);
    }

    private void playStageClearSound() {
        if (stageClearSoundPlayed) {
            return;
        }
        stageClearSoundPlayed = true;
        soundManager.stopBgm();
        soundManager.playSfx(SfxType.STAGE_CLEAR);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        tick++;
        if (tick % 11 == 0) {
            flameFrame++;
            glow = 0.7f + random.nextFloat() * 0.3f;
        }
        if (msgTimer > 0) {
            msgTimer--;
        }
        if (screen == 5) {
            update();
        }
        syncSinglePlayerUiVisibility();
        repaint();
    }

    void syncSinglePlayerUiVisibility() {
        if (aEngineBridge == null || !aEngineBridge.isAttached()) {
            return;
        }
        // Swing children are painted after GamePanel, so they would otherwise
        // cover pause/settings/result overlays and remain visible on menus.
        aEngineBridge.setVisible(screen == 5 && !stageClear);
    }

    void update() {
        if (stageClear) {
            clearTimer -= 16;
            if (clearTimer <= 0) {
                if (stage == 5) {
                    gameEnd(true);
                } else {
                    stage++;
                    startStage();
                    msg = "STAGE " + stage + " 시작!";
                    msgTimer = 90;
                }
            }
            return;
        }
        if (!(isOnlineCoop && localPlayerIndex == 1)) {
            gameTime += 16;
        }
        updateTemporaryBossEffects();
        int[] k = currentPrimaryKeys();
        if (!useAEngine() && controlsPlayer(0)) {
            if (pressed.contains(k[0])) {
                p1LeftCount += 16;
                if (p1LeftCount >= HORIZONTAL_DAS_DELAY_MS) {
                    performCoopAction(0, PlayerAction.MOVE_LEFT, false);
                    p1LeftCount -= HORIZONTAL_ARR_INTERVAL_MS;
                }
            }
            if (pressed.contains(k[1])) {
                p1RightCount += 16;
                if (p1RightCount >= HORIZONTAL_DAS_DELAY_MS) {
                    performCoopAction(0, PlayerAction.MOVE_RIGHT, false);
                    p1RightCount -= HORIZONTAL_ARR_INTERVAL_MS;
                }
            }
            if (pressed.contains(k[2])) {
                p1DownCount += 16;
                if (p1DownCount >= SOFT_DROP_REPEAT_MS) {
                    performCoopAction(0, PlayerAction.SOFT_DROP, false);
                    p1DownCount = 0;
                }
            }
        } else if (screen == 5 && !stageClear) {
            if (pressed.contains(k[0])) {
                p1LeftCount += 16;
                if (p1LeftCount >= HORIZONTAL_DAS_DELAY_MS) {
                    aEngineBridge.handleKey(k[0], k);
                    p1LeftCount -= HORIZONTAL_ARR_INTERVAL_MS;
                }
            }
            if (pressed.contains(k[1])) {
                p1RightCount += 16;
                if (p1RightCount >= HORIZONTAL_DAS_DELAY_MS) {
                    aEngineBridge.handleKey(k[1], k);
                    p1RightCount -= HORIZONTAL_ARR_INTERVAL_MS;
                }
            }
            if (pressed.contains(k[2])) {
                p1DownCount += 16;
                if (p1DownCount >= SOFT_DROP_REPEAT_MS) {
                    aEngineBridge.handleKey(k[2], k);
                    p1DownCount = 0;
                }
            }
        }
        if (isTwoPlayer && controlsPlayer(1)) {
            int[] p2Keys = onlineCoopKeysForP2();
            if (pressed.contains(p2Keys[0])) {
                p2LeftCount += 16;
                if (p2LeftCount >= HORIZONTAL_DAS_DELAY_MS) {
                    performCoopAction(1, PlayerAction.MOVE_LEFT, false);
                    p2LeftCount -= HORIZONTAL_ARR_INTERVAL_MS;
                }
            }
            if (pressed.contains(p2Keys[1])) {
                p2RightCount += 16;
                if (p2RightCount >= HORIZONTAL_DAS_DELAY_MS) {
                    performCoopAction(1, PlayerAction.MOVE_RIGHT, false);
                    p2RightCount -= HORIZONTAL_ARR_INTERVAL_MS;
                }
            }
            if (pressed.contains(p2Keys[2])) {
                p2DownCount += 16;
                if (p2DownCount >= SOFT_DROP_REPEAT_MS) {
                    performCoopAction(1, PlayerAction.SOFT_DROP, false);
                    p2DownCount = 0;
                }
            }
        }
        if (screen != 5 || stageClear) {
            return;
        }

        // 블록 내리기
        int speed = baseDropIntervalMillis(stage);
        int p1Speed = Math.max(1, speed / (p1FastCurrent ? 10 : gravityMultiplier));
        if (!useAEngine() && runsLocalGravity(0)) {
            p1DropCount += 16;
            if (p1DropCount >= p1Speed) {
                p1DropCount = 0;
                if (canPlace(board1, p1Type, p1Rot, p1X, p1Y + 1)) {
                    p1Y++;
                } else {
                    lockP1();
                }
            }
        } else if (useAEngine()) {
            aEngineBridge.syncHud();
        }
        if (screen != 5) {
            return;
        }
        if (isTwoPlayer && runsLocalGravity(1)) {
            int p2Speed = Math.max(1, speed / (p2FastCurrent ? 10 : gravityMultiplier));
            p2DropCount += 16;
            if (p2DropCount >= p2Speed) {
                p2DropCount = 0;
                if (canPlace(board2, p2Type, p2Rot, p2X, p2Y + 1)) {
                    p2Y++;
                } else {
                    lockP2();
                }
            }
        }
        if (screen != 5 || stageClear) {
            return;
        }

        if (useCoopEngineHost()) {
            coopEngineBridge.tickHost();
        }
        if (!isOnlineCoop || localPlayerIndex == 0) {
            bossPattern.tick(16, this);
            bossTimer = bossPattern.elapsedMillis();
        }
        if (hitEffect > 0) {
            hitEffect--;
        }
        if (coopBridge != null) {
            coopBridge.maybeSendSnapshot();
        }
    }

    boolean useAEngine() {
        return !isTwoPlayer && aEngineBridge != null && aEngineBridge.isAttached();
    }

    static int baseDropIntervalMillis(int stage) {
        return Math.max(1, 1000 - (stage - 1) * 150);
    }

    int[] currentPrimaryKeys() {
        if (temporaryPrimaryKeys != null) {
            return temporaryPrimaryKeys;
        }
        if (isOnlineCoop) {
            return keySingle;
        }
        return isTwoPlayer ? keyP1 : keySingle;
    }

    int[] onlineCoopKeysForP2() {
        return isOnlineCoop ? keySingle : currentSecondaryKeys();
    }

    boolean controlsPlayer(int playerIndex) {
        if (!isTwoPlayer) {
            return playerIndex == 0;
        }
        if (!isOnlineCoop) {
            return playerIndex == 0 || playerIndex == 1;
        }
        return localPlayerIndex == playerIndex;
    }

    boolean runsLocalGravity(int playerIndex) {
        if (useCoopEngineHost()) {
            return false;
        }
        if (!isOnlineCoop) {
            return true;
        }
        if (localPlayerIndex == 0) {
            return true;
        }
        return playerIndex == 1;
    }

    public boolean useCoopEngineHost() {
        return coopEngineBridge != null && coopEngineBridge.isDrivingHost();
    }

    void openCoopSlotScreen(int mode) {
        coopSlotMode = mode;
        coopSlotList = SaveService.listCoopSlots(loginUser);
        screen = 8;
    }

    int[] currentSecondaryKeys() {
        return temporarySecondaryKeys == null ? keyP2 : temporarySecondaryKeys;
    }

    void updateTemporaryBossEffects() {
        if (gravityMultiplier > 1 && gameTime >= blizzardEnd) {
            gravityMultiplier = 1;
            soundManager.stopManagedSfx(SfxType.STAGE_4_PATTERN);
            if (useAEngine()) {
                aEngineBridge.setGravityMultiplier(1);
            }
        }
        if (temporaryPrimaryKeys != null && gameTime >= randomKeyEnd) {
            temporaryPrimaryKeys = null;
            temporarySecondaryKeys = null;
            randomKeyEnd = 0;
            clearInputRepeatState();
        }
    }

    void clearInputRepeatState() {
        pressed.clear();
        p1LeftCount = 0;
        p1RightCount = 0;
        p1DownCount = 0;
        p2LeftCount = 0;
        p2RightCount = 0;
        p2DownCount = 0;
    }

    boolean raiseBoardAndPiece(int[][] board, boolean playerOne) {
        for (int c = 0; c < 10; c++) {
            if (board[0][c] != 0) {
                return false;
            }
        }
        int type = playerOne ? p1Type : p2Type;
        int rot = playerOne ? p1Rot : p2Rot;
        int y = playerOne ? p1Y : p2Y;
        for (int i = 0; i < blockCount(type); i++) {
            if (y + SHAPES[type][rot][i * 2 + 1] - 1 < 0) {
                return false;
            }
        }
        for (int r = 0; r < 23; r++) {
            board[r] = board[r + 1];
        }
        board[23] = new int[10];
        int hole = random.nextInt(10);
        for (int c = 0; c < 10; c++) {
            if (c != hole) {
                board[23][c] = 8;
            }
        }
        if (playerOne) {
            p1Y--;
        } else {
            p2Y--;
        }
        return true;
    }

    @Override
    public void showBossWarning(String message) {
        bossWarning = true;
        bossWarningText = message;
    }

    @Override
    public void clearBossWarning() {
        bossWarning = false;
        bossWarningText = "";
    }

    @Override
    public void onBossPatternActivated() {
        if (stage == 2) {
            soundManager.playSfx(SfxType.STAGE_2_PATTERN);
        } else if (stage == 4) {
            soundManager.playManagedSfx(SfxType.STAGE_4_PATTERN);
        } else if (stage == 5) {
            soundManager.playSfx(SfxType.STAGE_5_PATTERN);
        }
    }

    @Override
    public void showBossMessage(String message) {
        msg = message;
        msgTimer = 120;
    }

    @Override
    public void raiseBlocks() {
        if (useCoopEngineHost()) {
            if (!coopEngineBridge.raiseGarbageLines(random.nextInt(10), random.nextInt(10))) {
                gameEnd(false);
            }
            return;
        }
        if (useAEngine()) {
            if (!aEngineBridge.raiseGarbageLine(random.nextInt(10))) {
                gameEnd(false);
            }
            return;
        }
        if (!raiseBoardAndPiece(board1, true)) {
            gameEnd(false);
            return;
        }
        if (isTwoPlayer && !raiseBoardAndPiece(board2, false)) {
            gameEnd(false);
        }
    }

    @Override
    public void startAcceleration(int durationMillis, int multiplier, int nextPieceMultiplier) {
        blizzardEnd = gameTime + durationMillis;
        gravityMultiplier = multiplier;
        if (useAEngine()) {
            aEngineBridge.setGravityMultiplier(multiplier);
            aEngineBridge.setNextPieceGravityMultiplier(nextPieceMultiplier);
        } else {
            p1FastNext = true;
            if (isTwoPlayer) {
                p2FastNext = true;
            }
        }
    }

    @Override
    public int[] primaryOriginalKeys() {
        return (isTwoPlayer ? keyP1 : keySingle).clone();
    }

    @Override
    public int[] secondaryOriginalKeys() {
        return keyP2.clone();
    }

    @Override
    public boolean isTwoPlayerMode() {
        return isTwoPlayer;
    }

    @Override
    public void applyTemporaryKeys(int[] primaryKeys, int[] secondaryKeys, int durationMillis) {
        temporaryPrimaryKeys = primaryKeys.clone();
        temporarySecondaryKeys = secondaryKeys == null ? null : secondaryKeys.clone();
        randomKeyEnd = gameTime + durationMillis;
        clearInputRepeatState();
    }

    @Override
    public void resetTemporaryBossEffects() {
        gravityMultiplier = 1;
        blizzardEnd = 0;
        soundManager.stopManagedSfx(SfxType.STAGE_4_PATTERN);
        p1FastNext = false;
        p2FastNext = false;
        p1FastCurrent = false;
        p2FastCurrent = false;
        temporaryPrimaryKeys = null;
        temporarySecondaryKeys = null;
        randomKeyEnd = 0;
        clearInputRepeatState();
        if (aEngineBridge != null) {
            aEngineBridge.resetBossEffects();
        }
    }

    @Override
    public boolean isBlueprintCellOccupied(int row, int column) {
        if (row < 0 || row >= Blueprint.ROWS || column < 0 || column >= Blueprint.COLUMNS) {
            return false;
        }
        if (useAEngine()) {
            GameSession session = aEngineBridge.session();
            int engineY = Blueprint.ROWS - 1 - row;
            return session != null && session.shapeAt(column, engineY) != Tetrominoes.NoShape;
        }
        int boardRow = 18 + row;
        boolean occupied = board1[boardRow][column] != 0;
        if (isTwoPlayer) {
            occupied |= board2[boardRow][column] != 0;
        }
        return occupied;
    }

    @Override
    public void completeBlueprintPattern() {
        if (stage != 3 || stageClear) {
            return;
        }
        bossHp = 0;
        stageClear = true;
        clearTimer = 2000;
        pressed.clear();
        msg = "BLUEPRINT COMPLETE!";
        msgTimer = 120;
        bossPattern.stop(this);
        playStageClearSound();
    }

    public void notifyBossBoardChanged() {
        if (stage == 3 && !stageClear) {
            bossPattern.onBoardChanged(this);
        }
    }

    public void restoreBossPatternTimer(int elapsedMillis) {
        bossPattern.restoreElapsed(elapsedMillis, this);
        bossTimer = bossPattern.elapsedMillis();
    }

    void ensureAEngineBridge() {
        if (aEngineBridge == null) {
            aEngineBridge = new ASinglePlayerBridge(this);
        }
        if (!aEngineBridge.isAttached()) {
            aEngineBridge.attach();
        }
    }

    /** A 엔진 줄 클리어 → B 보스 HP (기존 B 데미지表) */
    public void applyBossLineDamage(int count) {
        if (count <= 0 || stageClear || stage == 3) {
            return;
        }
        int damage = switch (count) {
            case 1 -> 10;
            case 2 -> 25;
            case 3 -> 40;
            default -> 60;
        };
        bossHp -= damage;
        hitEffect = 12;
        if (bossHp <= 0) {
            bossHp = 0;
            stageClear = true;
            clearTimer = 2000;
            pressed.clear();
            bossPattern.stop(this);
            playStageClearSound();
        }
    }

    public void notifyEngineGameOver() {
        if (screen == 5 && !stageClear) {
            gameEnd(false);
        }
    }

    public void newGame(boolean two) {
        isTwoPlayer = two;
        if (isTwoPlayer && aEngineBridge != null) {
            aEngineBridge.stopAndHide();
        }
        stage = 1;
        p1Score = 0;
        p1Lines = 0;
        p1Hold = -1;
        p1Next.clear();
        p1Bag.clear();
        p2Score = 0;
        p2Lines = 0;
        p2Hold = -1;
        p2Next.clear();
        p2Bag.clear();
        saveFile = null;
        gameTime = 0;
        allClear = false;
        resultMsg = "";
        startStage();
        pressed.clear();
        screen = 5;
    }

    public void startStage() {
        startStage(-1);
    }

    public void startStage(int restoredBlueprintIndex) {
        if (stage == 1) {
            bossMax = 100;
        } else if (stage == 2) {
            bossMax = 150;
        } else if (stage == 3) {
            bossMax = 120;
        } else if (stage == 4) {
            bossMax = 200;
        } else {
            bossMax = 250;
        }
        bossHp = bossMax;
        bossTimer = 0;
        bossWarning = false;
        bossWarningText = "";
        blizzardEnd = 0;
        stageClear = false;
        stageClearSoundPlayed = false;
        hitEffect = 0;
        board1 = new int[24][10];
        board2 = new int[24][10];
        p1Garbage = 0;
        p2Garbage = 0;
        p1HoldUsed = false;
        p2HoldUsed = false;
        p1DropCount = 0;
        p2DropCount = 0;
        if (stage == 3 && restoredBlueprintIndex >= 0) {
            bossPattern.startStage(stage, this, restoredBlueprintIndex);
        } else {
            bossPattern.startStage(stage, this);
        }
        pressed.clear();
        if (!isTwoPlayer) {
            // 저장 호환용 GamePanel 상태도 A 엔진과 함께 초기화한다.
            spawnP1();
            ensureAEngineBridge();
            aEngineBridge.startStage(stage, baseDropIntervalMillis(stage));
        } else {
            spawnP1();
            spawnP2();
        }
        startBattleBgm();
    }

    void fillNext(ArrayList<Integer> next, ArrayList<Integer> bag) {
        while (next.size() < 6) {
            if (bag.isEmpty()) {
                for (int i = 0; i < 7; i++) {
                    bag.add(i);
                }
                Collections.shuffle(bag, random);
                if (stage >= 3 && random.nextDouble() < 0.15) {
                    int pentomino = 7 + random.nextInt(5);
                    bag.add(random.nextInt(bag.size() + 1), pentomino);
                }
            }
            next.add(bag.remove(0));
        }
    }

    static int blockCount(int type) {
        return type >= 7 ? 5 : 4;
    }

    static int blockValue(int type) {
        // 저장 포맷이 셀 하나를 숫자 한 글자로 기록하므로 펜토미노 고정 셀은 9로 통일한다.
        return type >= 7 ? 9 : type + 1;
    }

    public static int engineType(Tetrominoes shape) {
        if (shape == null) {
            return -1;
        }
        return switch (shape) {
            case LineShape -> 0;
            case SquareShape -> 1;
            case TShape -> 2;
            case SShape -> 3;
            case ZShape -> 4;
            case MirroredLShape -> 5;
            case LShape -> 6;
            case P5_P -> 7;
            case P5_U -> 8;
            case P5_V -> 9;
            case P5_X -> 10;
            case P5_F -> 11;
            default -> -1;
        };
    }

    public static int engineBlockValue(Tetrominoes shape) {
        int type = engineType(shape);
        return type < 0 ? 0 : blockValue(type);
    }

    boolean canPlace(int[][] board, int type, int rot, int x, int y) {
        for (int i = 0; i < blockCount(type); i++) {
            int bx = x + SHAPES[type][rot][i * 2];
            int by = y + SHAPES[type][rot][i * 2 + 1];
            if (bx < 0 || bx >= 10 || by < 0 || by >= 24) {
                return false;
            }
            if (board[by][bx] != 0) {
                return false;
            }
        }
        return true;
    }

    void spawnP1() {
        fillNext(p1Next, p1Bag);
        p1Type = p1Next.remove(0);
        fillNext(p1Next, p1Bag);
        p1Rot = 0;
        p1X = 3;
        p1Y = 3;
        p1FastCurrent = p1FastNext;
        p1FastNext = false;
        p1DropCount = 0;
        if (!canPlace(board1, p1Type, p1Rot, p1X, p1Y)) {
            gameEnd(false);
        }
    }

    void spawnP2() {
        fillNext(p2Next, p2Bag);
        p2Type = p2Next.remove(0);
        fillNext(p2Next, p2Bag);
        p2Rot = 0;
        p2X = 3;
        p2Y = 3;
        p2FastCurrent = p2FastNext;
        p2FastNext = false;
        p2DropCount = 0;
        if (!canPlace(board2, p2Type, p2Rot, p2X, p2Y)) {
            gameEnd(false);
        }
    }

    void moveP1(int dx) {
        if (canPlace(board1, p1Type, p1Rot, p1X + dx, p1Y)) {
            p1X += dx;
        }
    }

    void moveP2(int dx) {
        if (canPlace(board2, p2Type, p2Rot, p2X + dx, p2Y)) {
            p2X += dx;
        }
    }

    void softDropP1() {
        if (canPlace(board1, p1Type, p1Rot, p1X, p1Y + 1)) {
            p1Y++;
            p1Score += 1;
            p1DropCount = 0;
        }
    }

    void softDropP2() {
        if (canPlace(board2, p2Type, p2Rot, p2X, p2Y + 1)) {
            p2Y++;
            p2Score += 1;
            p2DropCount = 0;
        }
    }

    void hardDropP1() {
        while (canPlace(board1, p1Type, p1Rot, p1X, p1Y + 1)) {
            p1Y++;
            p1Score += 2;
        }
        lockP1();
    }

    void hardDropP2() {
        while (canPlace(board2, p2Type, p2Rot, p2X, p2Y + 1)) {
            p2Y++;
            p2Score += 2;
        }
        lockP2();
    }

    void rotateP1(int dir) {
        if (p1Type == 1) {
            return;
        }
        int newRot = (p1Rot + dir + 4) % 4;
        int[] kick = KICK[p1Type == 0 ? 1 : 0][dir == 1 ? p1Rot : 4 + p1Rot];
        for (int i = 0; i < 5; i++) {
            int nx = p1X + kick[i * 2];
            int ny = p1Y - kick[i * 2 + 1];
            if (canPlace(board1, p1Type, newRot, nx, ny)) {
                p1X = nx;
                p1Y = ny;
                p1Rot = newRot;
                return;
            }
        }
    }

    void rotateP2(int dir) {
        if (p2Type == 1) {
            return;
        }
        int newRot = (p2Rot + dir + 4) % 4;
        int[] kick = KICK[p2Type == 0 ? 1 : 0][dir == 1 ? p2Rot : 4 + p2Rot];
        for (int i = 0; i < 5; i++) {
            int nx = p2X + kick[i * 2];
            int ny = p2Y - kick[i * 2 + 1];
            if (canPlace(board2, p2Type, newRot, nx, ny)) {
                p2X = nx;
                p2Y = ny;
                p2Rot = newRot;
                return;
            }
        }
    }

    void holdP1() {
        if (p1HoldUsed) {
            return;
        }
        if (p1Hold == -1) {
            p1Hold = p1Type;
            spawnP1();
        } else {
            int temp = p1Hold;
            p1Hold = p1Type;
            p1Type = temp;
            p1Rot = 0;
            p1X = 3;
            p1Y = 3;
            if (!canPlace(board1, p1Type, p1Rot, p1X, p1Y)) {
                gameEnd(false);
            }
        }
        p1HoldUsed = true;
    }

    void holdP2() {
        if (p2HoldUsed) {
            return;
        }
        if (p2Hold == -1) {
            p2Hold = p2Type;
            spawnP2();
        } else {
            int temp = p2Hold;
            p2Hold = p2Type;
            p2Type = temp;
            p2Rot = 0;
            p2X = 3;
            p2Y = 3;
            if (!canPlace(board2, p2Type, p2Rot, p2X, p2Y)) {
                gameEnd(false);
            }
        }
        p2HoldUsed = true;
    }

    void lockP1() {
        playBlockLockSound();
        for (int i = 0; i < blockCount(p1Type); i++) {
            board1[p1Y + SHAPES[p1Type][p1Rot][i * 2 + 1]][p1X + SHAPES[p1Type][p1Rot][i * 2]] = blockValue(p1Type);
        }
        int n = checkLine(board1);
        if (n > 0) {
            p1Lines += n;
            if (n == 1) p1Score += 100 * stage;
            if (n == 2) p1Score += 300 * stage;
            if (n == 3) p1Score += 500 * stage;
            if (n == 4) p1Score += 800 * stage;
            // System.out.println(p1Score);
        }
        p1HoldUsed = false;
        // 보스 공격으로 쌓인 줄
        while (p1Garbage > 0) {
            p1Garbage--;
            if (!addGarbage(board1)) {
                gameEnd(false);
                return;
            }
        }
        notifyBossBoardChanged();
        if (stageClear) {
            return;
        }
        spawnP1();
    }

    void lockP2() {
        playBlockLockSound();
        for (int i = 0; i < blockCount(p2Type); i++) {
            board2[p2Y + SHAPES[p2Type][p2Rot][i * 2 + 1]][p2X + SHAPES[p2Type][p2Rot][i * 2]] = blockValue(p2Type);
        }
        int n = checkLine(board2);
        if (n > 0) {
            p2Lines += n;
            if (n == 1) p2Score += 100 * stage;
            if (n == 2) p2Score += 300 * stage;
            if (n == 3) p2Score += 500 * stage;
            if (n == 4) p2Score += 800 * stage;
        }
        p2HoldUsed = false;
        while (p2Garbage > 0) {
            p2Garbage--;
            if (!addGarbage(board2)) {
                gameEnd(false);
                return;
            }
        }
        notifyBossBoardChanged();
        if (stageClear) {
            return;
        }
        spawnP2();
    }

    // 줄 지우고 보스 데미지까지
    int checkLine(int[][] board) {
        int count = 0;
        for (int r = 23; r >= 0; r--) {
            boolean full = true;
            for (int c = 0; c < 10; c++) {
                if (board[r][c] == 0) {
                    full = false;
                    break;
                }
            }
            if (full) {
                for (int rr = r; rr > 0; rr--) {
                    board[rr] = board[rr - 1].clone();
                }
                board[0] = new int[10];
                count++;
                r++;
            }
        }
        if (count > 0 && !stageClear && stage != 3) {
            int damage = 0;
            if (count == 1) damage = 10;
            if (count == 2) damage = 25;
            if (count == 3) damage = 40;
            if (count == 4) damage = 60;
            bossHp -= damage;
            hitEffect = 12;
            if (bossHp <= 0) {
                bossHp = 0;
                stageClear = true;
                clearTimer = 2000;
                pressed.clear();
                bossPattern.stop(this);
                playStageClearSound();
            }
        }
        playLineClearSound(count);
        return count;
    }

    boolean addGarbage(int[][] board) {
        for (int c = 0; c < 10; c++) {
            if (board[0][c] != 0) {
                return false;
            }
        }
        for (int r = 0; r < 23; r++) {
            board[r] = board[r + 1];
        }
        board[23] = new int[10];
        int hole = random.nextInt(10);
        for (int c = 0; c < 10; c++) {
            if (c != hole) {
                board[23][c] = 8;
            }
        }
        return true;
    }

    void gameEnd(boolean clear) {
        if (screen == 7) {
            return;
        }
        allClear = clear;
        screen = 7;
        pressed.clear();
        bossPattern.stop(this);
        soundManager.stopBgm();
        int total = p1Score + (isTwoPlayer ? p2Score : 0);
        if (loginUser != null) {
            FileManager.addRecord(loginUser, total, stage, isTwoPlayer ? 2 : 1, clear);
            resultMsg = "랭킹에 기록했어요!";
            if (saveFile != null) {
                FileManager.deleteSave(saveFile);
                saveFile = null;
            }
        } else {
            resultMsg = "게스트 기록은 랭킹에 남지 않아요.";
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (screen == 4) {
            if (captureRow >= 0) {
                if (code == KeyEvent.VK_ESCAPE) {
                    captureRow = -1;
                    setMsg = "변경을 취소했어요.";
                    setMsgError = false;
                    return;
                }
                if (code == KeyEvent.VK_SHIFT || code == KeyEvent.VK_CONTROL || code == KeyEvent.VK_ALT
                        || code == KeyEvent.VK_META || code == KeyEvent.VK_UNDEFINED) {
                    return;
                }
                String used = null;
                for (int t = 0; t < 4; t++) {
                    boolean check = t == setTab || t == 3 || setTab == 3 || (setTab == 1 && t == 2) || (setTab == 2 && t == 1);
                    if (!check) {
                        continue;
                    }
                    for (int r = 0; r < editKeys[t].length; r++) {
                        if (t == setTab && r == captureRow) {
                            continue;
                        }
                        if (editKeys[t][r] == code) {
                            String[] tabNames = {"1인 모드", "2인 P1", "2인 P2", "공통"};
                            used = tabNames[t] + " · " + (t == 3 ? "일시정지" : actionNames[r]);
                        }
                    }
                }
                if (used != null) {
                    setMsg = "'" + keyName(code) + "' 키는 이미 [" + used + "]에 쓰이고 있어요.";
                    setMsgError = true;
                } else {
                    editKeys[setTab][captureRow] = code;
                    setMsg = "[" + (setTab == 3 ? "일시정지" : actionNames[captureRow]) + "] → " + keyName(code) + "  (저장을 눌러야 적용돼요)";
                    setMsgError = false;
                }
                captureRow = -1;
                return;
            }
            if (code == KeyEvent.VK_ESCAPE) {
                closeSettings();
            }
            return;
        }
        if (screen == 5) {
            if (code == KeyEvent.VK_ESCAPE || code == keyPause) {
                if (!stageClear) {
                    screen = 6;
                    msg = "";
                    pressed.clear();
                    if (useAEngine()) {
                        aEngineBridge.pauseEngine();
                    }
                    soundManager.pauseBgm();
                }
                return;
            }
            if (stageClear || pressed.contains(code)) {
                return;
            }
            pressed.add(code);
            int[] k = currentPrimaryKeys();
            if (useAEngine()) {
                aEngineBridge.handleKey(code, k);
                if (code == k[0]) {
                    p1LeftCount = 0;
                } else if (code == k[1]) {
                    p1RightCount = 0;
                } else if (code == k[2]) {
                    p1DownCount = 0;
                }
            } else if (controlsPlayer(0)) {
                if (code == k[0]) {
                    performCoopAction(0, PlayerAction.MOVE_LEFT, false);
                    p1LeftCount = 0;
                } else if (code == k[1]) {
                    performCoopAction(0, PlayerAction.MOVE_RIGHT, false);
                    p1RightCount = 0;
                } else if (code == k[2]) {
                    performCoopAction(0, PlayerAction.SOFT_DROP, false);
                    p1DownCount = 0;
                } else if (code == k[3]) {
                    performCoopAction(0, PlayerAction.HARD_DROP, false);
                } else if (code == k[4]) {
                    performCoopAction(0, PlayerAction.ROTATE_CW, false);
                } else if (code == k[5]) {
                    performCoopAction(0, PlayerAction.ROTATE_CCW, false);
                } else if (code == k[6]) {
                    performCoopAction(0, PlayerAction.HOLD, false);
                }
            }
            if (isTwoPlayer && screen == 5 && !stageClear && controlsPlayer(1)) {
                int[] p2Keys = onlineCoopKeysForP2();
                if (code == p2Keys[0]) {
                    performCoopAction(1, PlayerAction.MOVE_LEFT, false);
                    p2LeftCount = 0;
                } else if (code == p2Keys[1]) {
                    performCoopAction(1, PlayerAction.MOVE_RIGHT, false);
                    p2RightCount = 0;
                } else if (code == p2Keys[2]) {
                    performCoopAction(1, PlayerAction.SOFT_DROP, false);
                    p2DownCount = 0;
                } else if (code == p2Keys[3]) {
                    performCoopAction(1, PlayerAction.HARD_DROP, false);
                } else if (code == p2Keys[4]) {
                    performCoopAction(1, PlayerAction.ROTATE_CW, false);
                } else if (code == p2Keys[5]) {
                    performCoopAction(1, PlayerAction.ROTATE_CCW, false);
                } else if (code == p2Keys[6]) {
                    performCoopAction(1, PlayerAction.HOLD, false);
                }
            }
            return;
        }
        if (code == KeyEvent.VK_ESCAPE) {
            if (screen == 6) {
                screen = 5;
                pressed.clear();
                if (useAEngine()) {
                    aEngineBridge.resumeEngine();
                }
                soundManager.resumeBgm();
            } else if (screen == 2) {
                screen = 1;
            } else if (screen == 1 || screen == 3) {
                screen = 0;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        pressed.remove(code);
        if (screen == 5) {
            int[] k = currentPrimaryKeys();
            if (code == k[0]) {
                p1LeftCount = 0;
            } else if (code == k[1]) {
                p1RightCount = 0;
            } else if (code == k[2]) {
                p1DownCount = 0;
            }
            if (isTwoPlayer) {
                int[] p2Keys = onlineCoopKeysForP2();
                if (code == p2Keys[0]) {
                    p2LeftCount = 0;
                } else if (code == p2Keys[1]) {
                    p2RightCount = 0;
                } else if (code == p2Keys[2]) {
                    p2DownCount = 0;
                }
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void focusGained(FocusEvent e) {
    }

    @Override
    public void focusLost(FocusEvent e) {
        // 창 바뀌면 키 떼진걸 못받아서 계속 눌린걸로 됨
        pressed.clear();
    }

    boolean in(int x, int y, int w, int h) {
        boolean a = mouseX >= x;
        boolean b = mouseX < x + w;
        boolean c = mouseY >= y;
        boolean d = mouseY < y + h;
        if (a) {
            if (b) {
                if (c) {
                    if (d) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    boolean clicked(int x, int y, int w, int h) {
        if (!in(x, y, w, h)) {
            return false;
        }
        playButtonClickSound();
        return true;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        requestFocusInWindow();
        if (screen == 0) {
            if (clicked(88, 262, 300, 54)) {
                saveList = loginUser == null ? new ArrayList<>() : FileManager.getSaveList(loginUser);
                screen = 1;
            } else if (clicked(88, 332, 300, 54)) {
                loadRanking();
                screen = 3;
            } else if (clicked(88, 402, 300, 54)) {
                settingFrom = 0;
                editKeys = new int[][]{keySingle.clone(), keyP1.clone(), keyP2.clone(), {keyPause}};
                setTab = 0;
                captureRow = -1;
                setMsg = "";
                screen = 4;
            } else if (clicked(88, 472, 300, 54)) {
                int r = JOptionPane.showOptionDialog(this, "모험을 마치고 게임을 종료할까요?", "게임 종료",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[]{"종료", "취소"}, "취소");
                if (r == 0) {
                    System.exit(0);
                }
            } else if (clicked(88, 530, 300, 44)) {
                onlineLobbyFrame = new LobbyFrame(this);
                onlineLobbyFrame.setVisible(true);
            } else if (clicked(400, 530, 300, 44)) {
                openCoopSlotScreen(2);
            } else if (loginUser == null && clicked(88, 600, 150, 44)) {
                new LoginFrame(this, false).setVisible(true);
            } else if (loginUser == null && clicked(250, 600, 150, 44)) {
                new LoginFrame(this, true).setVisible(true);
            } else if (loginUser != null && clicked(88, 600, 150, 44)) {
                int r = JOptionPane.showOptionDialog(this, "로그아웃할까요?", "로그아웃",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[]{"로그아웃", "취소"}, "취소");
                if (r == 0) {
                    loginUser = null;
                }
            }
        } else if (screen == 1) {
            if (loginUser == null && clicked(530, 420, 220, 46)) {
                new LoginFrame(this, false).setVisible(true);
                if (loginUser != null) {
                    saveList = FileManager.getSaveList(loginUser);
                }
            } else if (clicked(490, 612, 300, 54)) {
                screen = 2;
            } else if (clicked(40, 640, 140, 44)) {
                screen = 0;
            } else if (loginUser != null) {
                for (int i = 0; i < saveList.size() && i < 5; i++) {
                    int y = 146 + i * 88;
                    if (clicked(760, y + 18, 120, 44)) {
                        if (FileManager.loadGame(saveList.get(i)[0], this)) {
                            screen = 5;
                            pressed.clear();
                        } else {
                            JOptionPane.showMessageDialog(this, "저장 파일을 읽을 수 없어요.");
                        }
                        break;
                    }
                    if (clicked(890, y + 18, 110, 44)) {
                        int r = JOptionPane.showOptionDialog(this, "스테이지 " + saveList.get(i)[2] + " · 점수 " + saveList.get(i)[3]
                                        + "\n이 저장을 지울까요? 되돌릴 수 없어요.", "저장 삭제",
                                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[]{"삭제", "취소"}, "취소");
                        if (r == 0) {
                            FileManager.deleteSave(saveList.get(i)[0]);
                            saveList = FileManager.getSaveList(loginUser);
                        }
                        break;
                    }
                }
            }
        } else if (screen == 2) {
            if (clicked(370, 270, 220, 54)) {
                newGame(false);
            } else if (clicked(370, 366, 220, 54)) {
                newGame(true);
            } else if (clicked(570, 470, 140, 44)) {
                screen = 1;
            }
        } else if (screen == 3) {
            if (clicked(170, 146, 220, 46)) {
                rankTab = 0;
                loadRanking();
            } else if (clicked(402, 146, 220, 46)) {
                rankTab = 1;
                loadRanking();
            } else if (clicked(40, 640, 140, 44)) {
                screen = 0;
            }
        } else if (screen == 4) {
            for (int t = 0; t < 4; t++) {
                if (clicked(250 + t * 192, 126, 180, 42)) {
                    setTab = t;
                    captureRow = -1;
                }
            }
            for (int r = 0; r < editKeys[setTab].length; r++) {
                if (clicked(670, 190 + r * 48, 220, 36)) {
                    captureRow = r;
                    setMsg = "새 키를 누르세요. 취소하려면 Esc를 누르세요.";
                    setMsgError = false;
                }
            }
            if (clicked(250, 590, 170, 46)) {
                editKeys = new int[][]{
                    {KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN, KeyEvent.VK_SPACE, KeyEvent.VK_UP, KeyEvent.VK_Z, KeyEvent.VK_C},
                    {KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_S, KeyEvent.VK_SPACE, KeyEvent.VK_W, KeyEvent.VK_Q, KeyEvent.VK_E},
                    {KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN, KeyEvent.VK_ENTER, KeyEvent.VK_UP, KeyEvent.VK_SLASH, KeyEvent.VK_PERIOD},
                    {KeyEvent.VK_P}
                };
                captureRow = -1;
                setMsg = "기본 키 배치로 되돌렸어요. 저장을 눌러야 적용돼요.";
                setMsgError = false;
            } else if (clicked(700, 590, 150, 46)) {
                keySingle = editKeys[0].clone();
                keyP1 = editKeys[1].clone();
                keyP2 = editKeys[2].clone();
                keyPause = editKeys[3][0];
                FileManager.saveKeys();
                setMsg = "저장했어요! 바로 게임에 적용돼요.";
                setMsgError = false;
            } else if (clicked(862, 590, 150, 46)) {
                closeSettings();
            }
        } else if (screen == 6) {
            if (clicked(490, 210, 300, 54)) {
                screen = 5;
                pressed.clear();
                if (useAEngine()) {
                    aEngineBridge.resumeEngine();
                }
                soundManager.resumeBgm();
            } else if (clicked(490, 282, 300, 54)) {
                if (isOnlineCoop && coopBridge != null && !coopBridge.isHost()) {
                    msg = "방장만 협동 저장을 할 수 있어요.";
                    msgTimer = 180;
                } else if (isTwoPlayer || isOnlineCoop) {
                    openCoopSlotScreen(1);
                } else if (loginUser == null) {
                    msg = "게스트는 1인 저장을 할 수 없어요. 로그인 후 이용해 주세요.";
                    msgTimer = 180;
                } else if (saveFile == null && FileManager.getSaveList(loginUser).size() >= 5) {
                    msg = "저장 칸이 가득 찼어요(최대 5개). 메인에서 지운 뒤 저장하세요.";
                    msgTimer = 180;
                } else if (FileManager.saveGame(this)) {
                    msg = "저장했어요! [플레이]에서 이어할 수 있어요.";
                    msgTimer = 180;
                } else {
                    msg = "저장하지 못했어요.";
                    msgTimer = 180;
                }
            } else if (clicked(490, 354, 300, 54)) {
                openSettings();
            } else if (clicked(490, 426, 300, 54)) {
                if (loginUser != null) {
                    int r = JOptionPane.showOptionDialog(this, "저장하지 않은 진행은 사라져요.", "메인으로",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                            new String[]{"저장하고 나가기", "그냥 나가기", "취소"}, "취소");
                    if (r == 0) {
                        if (saveFile == null && FileManager.getSaveList(loginUser).size() >= 5) {
                            JOptionPane.showMessageDialog(this, "저장 칸이 가득 찼어요(최대 5개).");
                            return;
                        }
                        FileManager.saveGame(this);
                        screen = 0;
                        soundManager.stopBgm();
                    } else if (r == 1) {
                        screen = 0;
                        soundManager.stopBgm();
                    }
                } else {
                    int r = JOptionPane.showOptionDialog(this, "게스트 진행은 저장되지 않아요.\n메인 화면으로 갈까요?", "메인으로",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[]{"나가기", "취소"}, "취소");
                    if (r == 0) {
                        screen = 0;
                        soundManager.stopBgm();
                    }
                }
            }
        } else if (screen == 7) {
            if (clicked(490, 470, 300, 54)) {
                screen = 0;
            }
        } else if (screen == 8) {
            handleCoopSlotClick();
        }
    }

    void handleCoopSlotClick() {
        if (clicked(490, 560, 300, 48)) {
            screen = coopSlotMode == 1 ? 6 : 0;
            return;
        }
        for (int i = 0; i < 3; i++) {
            if (clicked(340, 220 + i * 92, 600, 72)) {
                pickCoopSlot(i + 1);
                return;
            }
        }
    }

    void pickCoopSlot(int slot) {
        if (coopSlotMode == 1) {
            try {
                saveCoopToSlot(slot);
                msg = "협동 슬롯 " + slot + "에 저장했어요.";
                msgTimer = 200;
                screen = 6;
            } catch (Exception ex) {
                msg = "저장 실패: " + ex.getMessage();
                msgTimer = 200;
                screen = 6;
            }
        } else {
            try {
                CoopGameMemento mem = SaveService.loadCoop(loginUser, slot);
                if (mem.onlineResume) {
                    pendingCoopResume = mem;
                    msg = "슬롯 " + slot + " 준비됨 · 로비 입장 후 방장이 「이어하기」";
                    msgTimer = 260;
                    screen = 0;
                } else {
                    CoopEngineBridge.applyLoadedMemento(this, mem);
                    msg = "슬롯 " + slot + " 불러왔어요.";
                    msgTimer = 200;
                }
            } catch (LoadException ex) {
                msg = ex.getMessage();
                msgTimer = 180;
            }
        }
    }

    void saveCoopToSlot(int slot) throws Exception {
        CoopGameMemento mem;
        if (useCoopEngineHost() && coopEngineBridge != null) {
            mem = coopEngineBridge.createMemento(onlineCoopId != null ? onlineCoopId : java.util.UUID.randomUUID().toString());
        } else {
            mem = buildCoopMementoFromPanel();
        }
        mem.onlineResume = isOnlineCoop;
        mem.lastRoomId = lastOnlineRoomId;
        SaveService.saveCoop(loginUser, slot, mem);
        if (coopBridge != null && coopBridge.isHost()) {
            coopBridge.notifySaved(slot, mem);
        }
    }

    public CoopGameMemento buildCoopMementoFromPanel() {
        CoopGameMemento m = new CoopGameMemento();
        String cid = onlineCoopId != null ? onlineCoopId : java.util.UUID.randomUUID().toString();
        m.coopId = cid;
        m.seed = random.nextLong();
        m.stage = stage;
        m.bossHp = bossHp;
        m.bossMax = bossMax;
        m.bossTimer = bossTimer;
        m.gameTime = gameTime;
        m.stageClear = stageClear;
        m.blizzardEnd = blizzardEnd;
        m.blueprintIndex = bossPattern.blueprintIndex();
        m.onlineResume = isOnlineCoop;
        m.lastRoomId = lastOnlineRoomId;
        m.panelSnapshotJson = new Gson().toJson(CoopSnapshotMapper.fromPanel(this, cid));
        return m;
    }

    void openSettings() {
        settingFrom = screen;
        editKeys = new int[][]{keySingle.clone(), keyP1.clone(), keyP2.clone(), {keyPause}};
        setTab = settingFrom == 6 && isTwoPlayer ? 1 : 0;
        captureRow = -1;
        setMsg = "";
        screen = 4;
    }

    boolean saveSettings() {
        keySingle = editKeys[0].clone();
        keyP1 = editKeys[1].clone();
        keyP2 = editKeys[2].clone();
        keyPause = editKeys[3][0];
        FileManager.saveKeys();
        setMsg = "저장했어요! 바로 게임에 적용돼요.";
        setMsgError = false;
        return true;
    }

    void closeSettings() {
        boolean changed = !java.util.Arrays.equals(editKeys[0], keySingle) || !java.util.Arrays.equals(editKeys[1], keyP1)
                || !java.util.Arrays.equals(editKeys[2], keyP2) || editKeys[3][0] != keyPause;
        if (changed) {
            int r = JOptionPane.showOptionDialog(this, "바꾼 키 설정을 저장하지 않았어요. 어떻게 할까요?", "저장하지 않은 변경",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[]{"저장", "버리기", "취소"}, "취소");
            if (r == 0) {
                saveSettings();
            } else if (r != 1) {
                return;
            }
        }
        screen = settingFrom;
        pressed.clear();
    }

    void loadRanking() {
        HashMap<String, String[]> best = new HashMap<>();
        for (String[] data : FileManager.getRecords()) {
            String[] old = best.get(data[0]);
            if (old == null) {
                best.put(data[0], data);
            } else if (rankTab == 0) {
                if (Integer.parseInt(data[1]) > Integer.parseInt(old[1])) {
                    best.put(data[0], data);
                }
            } else {
                int s1 = Integer.parseInt(data[2]) * 10 + Integer.parseInt(data[4]);
                int s2 = Integer.parseInt(old[2]) * 10 + Integer.parseInt(old[4]);
                if (s1 > s2 || (s1 == s2 && Integer.parseInt(data[1]) > Integer.parseInt(old[1]))) {
                    best.put(data[0], data);
                }
            }
        }
        rankList = new ArrayList<>(best.values());
        Collections.sort(rankList, (a, b) -> {
            if (rankTab == 1) {
                int s1 = Integer.parseInt(a[2]) * 10 + Integer.parseInt(a[4]);
                int s2 = Integer.parseInt(b[2]) * 10 + Integer.parseInt(b[4]);
                if (s1 != s2) {
                    return s2 - s1;
                }
            }
            return Integer.parseInt(b[1]) - Integer.parseInt(a[1]);
        });
    }

    static String keyName(int code) {
        if (code == KeyEvent.VK_LEFT) return "←";
        if (code == KeyEvent.VK_RIGHT) return "→";
        if (code == KeyEvent.VK_UP) return "↑";
        if (code == KeyEvent.VK_DOWN) return "↓";
        if (code == KeyEvent.VK_SLASH) return "/";
        if (code == KeyEvent.VK_PERIOD) return ".";
        if (code == KeyEvent.VK_COMMA) return ",";
        return KeyEvent.getKeyText(code);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        if (screen == 0) {
            if (screen == 0) {
                drawTitle(g);
            }
        } else {
            if (screen == 1 || screen == 2) {
                drawSaves(g);
                if (screen == 2) {
                    drawModeSelect(g);
                }
            } else {
                if (screen == 3) {
                    drawRanking(g);
                } else {
                    if (screen == 4) {
                        if (settingFrom == 0) {
                            drawTitle(g);
                        } else {
                            drawGame(g);
                        }
                        drawSettings(g);
                    } else {
                        if (screen == 5) {
                            drawGame(g);
                        } else {
                            if (screen == 6) {
                                drawGame(g);
                                drawPause(g);
                            } else {
                                if (screen == 7) {
                                    drawGame(g);
                                    drawResult(g);
                                } else if (screen == 8) {
                                    if (screen == 6) {
                                        drawGame(g);
                                        drawPause(g);
                                    } else {
                                        drawTitle(g);
                                    }
                                    drawCoopSlots(g);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    void drawCoopSlots(Graphics2D g) {
        g.setColor(new Color(8, 6, 10, 190));
        g.fillRect(0, 0, 1280, 720);
        drawPanel(g, 300, 100, 680, 520);
        g.setFont(fontBold.deriveFont(30f));
        centerText(g, coopSlotMode == 1 ? "협동 저장 · 슬롯 선택" : "협동 불러오기 · 슬롯 선택",
                640, 150, new Color(0x1a1216), new Color(0xefe0bf));
        g.setFont(font.deriveFont(16f));
        g.setColor(new Color(0x1a1216));
        String hint = loginUser == null ? "게스트도 `_local` 슬롯에 저장됩니다." : "로그인: " + loginUser;
        g.drawString(hint, 640 - g.getFontMetrics().stringWidth(hint) / 2, 188);
        for (int i = 0; i < 3; i++) {
            SaveSlotMeta meta = i < coopSlotList.size() ? coopSlotList.get(i) : new SaveSlotMeta(i + 1, false, "비어 있음", 0);
            drawButton(g, "슬롯 " + meta.slot + " — " + meta.summary, 340, 220 + i * 92, 600, 72, i, 17f);
        }
        drawButton(g, "닫기", 490, 560, 300, 48, 3, 20f);
    }

    void drawCastle(Graphics2D g, boolean dark) {
        g.drawImage(Art.castle(), 0, 0, null);
        int[][] pos = {{522, 228}, {1218, 228}};
        for (int i = 0; i < 2; i++) {
            int cx = pos[i][0] + 10;
            int cy = pos[i][1] + 16;
            g.setPaint(new RadialGradientPaint(cx, cy, 150f, new float[]{0f, 0.45f, 1f},
                    new Color[]{new Color(255, 150, 60, (int) (46 * glow)), new Color(255, 150, 60, (int) (16 * glow)), new Color(255, 150, 60, 0)}));
            g.fillOval(cx - 150, cy - 150, 300, 300);
            g.drawImage(Art.flame(flameFrame), pos[i][0], pos[i][1], null);
        }
        if (dark) {
            g.setColor(new Color(10, 7, 12, 110));
            g.fillRect(0, 0, 1280, 720);
        }
    }

    void drawButton(Graphics2D g, String text, int x, int y, int w, int h, int type, float size) {
        boolean hover = in(x, y, w, h);
        g.setColor(new Color(0x0c090d));
        g.fillRect(x, y + 5, w, h);
        g.setColor(new Color(0x1a1216));
        g.fillRect(x, y, w, h);
        Color fill;
        Color textColor;
        switch (type) {
            case 1:
                fill = hover ? new Color(0xc24536) : new Color(0xa8352a);
                textColor = new Color(0xfff4e6);
                break;
            case 2:
                fill = hover ? new Color(0x4a3f52) : new Color(0x3a3040);
                textColor = new Color(0xf0e2c8);
                break;
            default:
                fill = hover ? new Color(0xefe0bf) : new Color(0xd9c29a);
                textColor = new Color(0x1a1216);
                break;
        }
        g.setColor(fill);
        g.fillRect(x + 4, y + 4, w - 8, h - 8);
        g.setColor(new Color(255, 255, 255, 60));
        g.fillRect(x + 4, y + 4, w - 8, 3);
        g.setFont(font.deriveFont(size));
        int tw = g.getFontMetrics().stringWidth(text);
        g.setColor(textColor);
        g.drawString(text, x + (w - tw) / 2, y + h / 2 + (int) (size / 2) - 2);
    }

    void drawPanel(Graphics2D g, int x, int y, int w, int h) {
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRect(x + 8, y + 8, w, h);
        g.setColor(new Color(0x1a1216));
        g.fillRect(x, y, w, h);
        g.setColor(new Color(0xb89c6c));
        g.fillRect(x + 4, y + 4, w - 8, h - 8);
        g.setColor(new Color(0xd9c29a));
        g.fillRect(x + 8, y + 8, w - 16, h - 16);
        g.setColor(new Color(0xefe0bf));
        g.fillRect(x + 8, y + 8, w - 16, 4);
        g.setColor(new Color(0x8a6a2a));
        g.fillRect(x, y, 12, 12);
        g.fillRect(x + w - 12, y, 12, 12);
        g.fillRect(x, y + h - 12, 12, 12);
        g.fillRect(x + w - 12, y + h - 12, 12, 12);
    }

    void shadowText(Graphics2D g, String text, int x, int y, Color c, Color shadow, int off) {
        g.setColor(shadow);
        g.drawString(text, x + off, y + off);
        g.setColor(c);
        g.drawString(text, x, y);
    }

    void centerText(Graphics2D g, String text, int cx, int y, Color c, Color shadow) {
        int w = g.getFontMetrics().stringWidth(text);
        shadowText(g, text, cx - w / 2, y, c, shadow, 2);
    }

    void drawTitle(Graphics2D g) {
        drawCastle(g, false);
        for (int cx : new int[]{640, 1114}) {
            for (int i = 0; i < 12; i++) {
                g.setColor(i % 2 == 0 ? new Color(0x3a3540) : new Color(0x1a171d));
                g.fillRect(cx, 36 + i * 6, 6, 6);
            }
            g.setColor(new Color(0x0f0d12));
            g.fillRect(cx - 6, 24, 18, 12);
        }
        g.setColor(new Color(0, 0, 0, 140));
        g.fillRect(594, 124, 600, 486);
        if (mapImg != null) {
            g.drawImage(mapImg, 580, 110, 600, 486, null);
        }
        g.setFont(fontBold.deriveFont(66f));
        shadowText(g, "TETRIS", 84, 172, new Color(0xf0e2c8), new Color(0x6b2a1a), 5);
        g.setFont(font.deriveFont(22f));
        shadowText(g, "다섯 성의 보스를 무너뜨려라", 90, 214, new Color(0xd9c29a), new Color(0x0c090d), 2);
        drawButton(g, "플레이", 88, 262, 300, 54, 1, 22f);
        drawButton(g, "랭킹", 88, 332, 300, 54, 0, 22f);
        drawButton(g, "환경설정", 88, 402, 300, 54, 0, 22f);
        drawButton(g, "게임 종료", 88, 472, 300, 54, 0, 22f);
        drawButton(g, "온라인 협동", 88, 530, 300, 44, 0, 18f);
        drawButton(g, "협동 저장 불러오기", 400, 530, 300, 44, 1, 16f);
        g.setFont(font.deriveFont(22f));
        if (loginUser != null) {
            drawButton(g, "로그아웃", 88, 600, 150, 44, 2, 16.5f);
            shadowText(g, loginUser + " 용사님, 모험을 떠나요!", 88, 672, new Color(0xe9c98f), new Color(0x0c090d), 2);
        } else {
            drawButton(g, "로그인", 88, 600, 150, 44, 2, 16.5f);
            drawButton(g, "회원가입", 250, 600, 150, 44, 2, 16.5f);
            g.setFont(font.deriveFont(22f));
            shadowText(g, "게스트로 플레이 중", 88, 672, new Color(0xd9c29a), new Color(0x0c090d), 2);
        }
        g.setFont(fontSmall.deriveFont(13.5f));
        g.setColor(new Color(0x9a8f9f));
    }

    void drawSaves(Graphics2D g) {
        drawCastle(g, true);
        g.setFont(fontBold.deriveFont(44f));
        centerText(g, "저장된 모험", 640, 92, new Color(0xf0e2c8), new Color(0x6b2a1a));
        g.setFont(font.deriveFont(16.5f));
        g.setColor(new Color(0xd9c29a));
        String sub = loginUser == null ? "게스트로 플레이 중" : loginUser + " 용사님의 저장 목록 (최대 5개)";
        g.drawString(sub, 640 - g.getFontMetrics().stringWidth(sub) / 2, 126);
        if (loginUser == null || saveList.isEmpty()) {
            g.setColor(new Color(0, 0, 0, 90));
            g.fillRect(250, 250, 780, 250);
            g.setFont(fontBold.deriveFont(22f));
            String t1 = loginUser == null ? "게스트는 게임을 저장할 수 없어요." : "이전 플레이 기록이 없어요.";
            String t2 = loginUser == null ? "로그인하면 저장한 모험을 이어서 할 수 있어요. 새 게임은 바로 시작할 수 있어요."
                    : "아래 [+ 새 게임]으로 첫 모험을 시작해 보세요!";
            centerText(g, t1, 640, 330, new Color(0xf0e2c8), new Color(0x0c090d));
            g.setFont(font.deriveFont(16.5f));
            g.setColor(new Color(0xd9c29a));
            g.drawString(t2, 640 - g.getFontMetrics().stringWidth(t2) / 2, 372);
            if (loginUser == null) {
                drawButton(g, "로그인하러 가기", 530, 420, 220, 46, 0, 16.5f);
            }
        } else {
            SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd HH:mm");
            for (int i = 0; i < saveList.size() && i < 5; i++) {
                String[] s = saveList.get(i);
                int y = 146 + i * 88;
                int st = Integer.parseInt(s[2]);
                g.setColor(new Color(0x1a1216));
                g.fillRect(250, y, 780, 80);
                g.setColor(new Color(0xd9c29a));
                g.fillRect(254, y + 4, 772, 72);
                g.setColor(new Color(0xefe0bf));
                g.fillRect(254, y + 4, 772, 3);
                g.drawImage(Art.background(st), 262, y + 10, 262 + 110, y + 70, 260, 90, 1020, 630, null);
                g.setColor(new Color(0x1a1216));
                g.drawRect(262, y + 10, 110, 60);
                String[] stageNames = {"", "불타는 사막", "고립된 섬", "고요한 초원", "얼어붙은 설원", "하늘 위 마왕성"};
                g.setFont(fontBold.deriveFont(22f));
                g.drawString("스테이지 " + st + " · " + stageNames[st], 390, y + 32);
                g.setFont(font.deriveFont(16.5f));
                g.setColor(new Color(0x4a3a2a));
                g.drawString((s[4].equals("2") ? "2인 협동" : "1인 플레이") + "  ·  점수 " + String.format("%,d", Integer.parseInt(s[3])), 390, y + 54);
                g.drawString("마지막 플레이  " + df.format(new Date(Long.parseLong(s[1]))), 390, y + 74);
                drawButton(g, "이어하기", 760, y + 18, 120, 44, 1, 16.5f);
                drawButton(g, "삭제", 890, y + 18, 110, 44, 0, 16.5f);
            }
        }
        drawButton(g, "+ 새 게임", 490, 612, 300, 54, 1, 22f);
        drawButton(g, "← 뒤로", 40, 640, 140, 44, 2, 16.5f);
    }

    void drawModeSelect(Graphics2D g) {
        g.setColor(new Color(8, 6, 10, 170));
        g.fillRect(0, 0, 1280, 720);
        drawPanel(g, 330, 160, 620, 400);
        g.setFont(fontBold.deriveFont(33f));
        centerText(g, "몇 명이서 모험할까요?", 640, 230, new Color(0x1a1216), new Color(0xefe0bf));
        drawButton(g, "1인 플레이", 370, 270, 220, 54, 1, 22f);
        drawButton(g, "2인 협동", 370, 366, 220, 54, 0, 22f);
        g.setFont(font.deriveFont(16.5f));
        g.setColor(new Color(0x1a1216));
        g.drawString("혼자서 다섯 성의 보스에 도전해요", 610, 302);
        g.drawString("한 키보드로 둘이 함께 싸워요", 610, 390);
        g.drawString("(P1: WASD · P2: 방향키)", 610, 412);
        drawButton(g, "취소", 570, 470, 140, 44, 0, 16.5f);
    }

    void drawRanking(Graphics2D g) {
        drawCastle(g, true);
        g.setFont(fontBold.deriveFont(44f));
        centerText(g, "명예의 전당", 640, 92, new Color(0xf0e2c8), new Color(0x6b2a1a));
        drawButton(g, "점수 랭킹", 170, 146, 220, 46, rankTab == 0 ? 1 : 0, 16.5f);
        drawButton(g, "스테이지 랭킹", 402, 146, 220, 46, rankTab == 1 ? 1 : 0, 16.5f);
        g.setFont(font.deriveFont(16.5f));
        g.setColor(new Color(0xd9c29a));
        g.drawString(rankTab == 0 ? "플레이어별 최고 점수 순" : "가장 멀리 간 스테이지 순 · 같으면 점수 순", 650, 180);
        g.setColor(new Color(0x1a1216));
        g.fillRect(170, 214, 940, 456);
        g.setColor(new Color(0x221b24));
        g.fillRect(174, 218, 932, 448);
        String[] heads = {"순위", "닉네임", "점수", "도달 스테이지", "인원", "날짜"};
        int[] colX = {194, 280, 550, 730, 900, 1010};
        g.setFont(fontBold.deriveFont(16.5f));
        g.setColor(new Color(0xe9c98f));
        for (int i = 0; i < 6; i++) {
            g.drawString(heads[i], colX[i], 246);
        }
        g.setColor(new Color(0x4a3f52));
        g.fillRect(182, 258, 916, 3);
        if (rankList.isEmpty()) {
            g.setFont(font.deriveFont(22f));
            centerText(g, "아직 기록이 없어요. 첫 번째 용사가 되어 보세요!", 640, 450, new Color(0xf0e2c8), new Color(0x0c090d));
        }
        SimpleDateFormat df = new SimpleDateFormat("MM.dd");
        String[] stageNames = {"", "사막", "섬", "초원", "설원", "마왕성"};
        Color[] medal = {new Color(0xf2c14e), new Color(0xcfd6de), new Color(0xd08a4a)};
        for (int i = 0; i < rankList.size() && i < 10; i++) {
            String[] r = rankList.get(i);
            int y = 266 + i * 40;
            boolean me = r[0].equals(loginUser);
            if (me) {
                g.setColor(new Color(0x5a3a2a));
                g.fillRect(180, y, 920, 36);
            } else if (i % 2 == 1) {
                g.setColor(new Color(0x2a222d));
                g.fillRect(180, y, 920, 36);
            }
            if (i < 3) {
                g.setColor(medal[i]);
                g.fillRect(192, y + 6, 26, 24);
                g.setColor(new Color(0x1a1216));
                g.setFont(fontBold.deriveFont(16.5f));
                g.drawString("" + (i + 1), 200, y + 24);
            } else {
                g.setColor(new Color(0x9a8f9f));
                g.setFont(font.deriveFont(16.5f));
                g.drawString("" + (i + 1), 200, y + 24);
            }
            g.setFont(font.deriveFont(22f));
            g.setColor(me ? new Color(0xe9c98f) : new Color(0xf0e2c8));
            g.drawString(r[0] + (me ? " (나)" : ""), 280, y + 26);
            g.setColor(new Color(0xf0e2c8));
            g.drawString(String.format("%,d", Integer.parseInt(r[1])), 550, y + 26);
            g.setFont(font.deriveFont(16.5f));
            if (r[4].equals("1")) {
                g.setColor(new Color(0xe9c98f));
                g.drawString("전체 클리어", 730, y + 24);
            } else {
                g.setColor(new Color(0xd9c29a));
                g.drawString(r[2] + " · " + stageNames[Integer.parseInt(r[2])], 730, y + 24);
            }
            g.setColor(new Color(0xd9c29a));
            g.drawString(r[3] + "인", 900, y + 24);
            g.setColor(new Color(0x9a8f9f));
            g.drawString(df.format(new Date(Long.parseLong(r[5]))), 1010, y + 24);
        }
        drawButton(g, "← 뒤로", 40, 640, 120, 44, 2, 16.5f);
    }

    void drawSettings(Graphics2D g) {
        g.setColor(new Color(8, 6, 10, 170));
        g.fillRect(0, 0, 1280, 720);
        drawPanel(g, 210, 50, 860, 620);
        g.setFont(fontBold.deriveFont(33f));
        shadowText(g, "환경설정 · 키 설정", 250, 108, new Color(0x1a1216), new Color(0xefe0bf), 2);
        String[] tabNames = {"1인 모드", "2인 P1", "2인 P2", "공통"};
        for (int t = 0; t < 4; t++) {
            drawButton(g, tabNames[t], 250 + t * 192, 126, 180, 42, t == setTab ? 1 : 0, 16.5f);
        }
        for (int r = 0; r < editKeys[setTab].length; r++) {
            int y = 190 + r * 48;
            g.setFont(font.deriveFont(22f));
            g.setColor(new Color(0x1a1216));
            g.drawString(setTab == 3 ? "일시정지" : actionNames[r], 330, y + 27);
            drawButton(g, captureRow == r ? "키를 누르세요…" : keyName(editKeys[setTab][r]), 670, y, 220, 36, captureRow == r ? 1 : 0, 16.5f);
            g.setColor(new Color(0xb89c6c));
            g.fillRect(310, y + 42, 600, 2);
        }
        g.setFont(fontSmall.deriveFont(13.5f));
        g.setColor(new Color(0x5a4a3a));
        g.drawString("키 설정은 이 컴퓨터에 저장되고 모든 사용자에게 적용돼요.", 250, 540);
        g.setFont(font.deriveFont(16.5f));
        g.setColor(setMsgError ? new Color(0xa8352a) : new Color(0x3a5a2a));
        g.drawString(setMsg, 250, 568);
        drawButton(g, "기본값 복원", 250, 590, 170, 46, 0, 16.5f);
        drawButton(g, "저장", 700, 590, 150, 46, 1, 16.5f);
        drawButton(g, "닫기", 862, 590, 150, 46, 0, 16.5f);
    }

    Color blockColor(int v) {
        return switch (v) {
            case 1 -> new Color(0x4fc3d9);
            case 2 -> new Color(0xf2c94c);
            case 3 -> new Color(0xa45ad8);
            case 4 -> new Color(0x5cc05a);
            case 5 -> new Color(0xe0524a);
            case 6 -> new Color(0x4a6ee0);
            case 7 -> new Color(0xf0943a);
            case 9 -> new Color(0xc06be8);
            default -> new Color(0x7a7280);
        };
    }

    void drawCell(Graphics2D g, int x, int y, int size, int v) {
        Color c = blockColor(v);
        g.setColor(c.darker());
        g.fillRect(x, y, size, size);
        g.setColor(c);
        g.fillRect(x + 2, y + 2, size - 4, size - 4);
        g.setColor(c.brighter());
        g.fillRect(x + 2, y + 2, size - 4, 3);
        g.fillRect(x + 2, y + 2, 3, size - 4);
    }

    void drawGhostCell(Graphics2D g, int x, int y, int size, int v) {
        Color c = blockColor(v);
        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 36));
        g.fillRect(x + 2, y + 2, size - 4, size - 4);
        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 72));
        g.drawRect(x + 2, y + 2, size - 5, size - 5);
    }

    int panelGhostLandingY(int[][] board, int type, int rot, int x, int y) {
        int gy = y;
        while (canPlace(board, type, rot, x, gy + 1)) {
            gy++;
        }
        return gy;
    }

    void drawLegacyActivePiece(Graphics2D g, int bx, int by, int[][] board, int type, int rot, int px, int py) {
        int ghostY = panelGhostLandingY(board, type, rot, px, py);
        if (ghostY != py) {
            for (int i = 0; i < blockCount(type); i++) {
                int cx = px + SHAPES[type][rot][i * 2];
                int cy = ghostY + SHAPES[type][rot][i * 2 + 1];
                if (cy >= 4) {
                    drawGhostCell(g, bx + cx * 30, by + (cy - 4) * 30, 30, blockValue(type));
                }
            }
        }
        for (int i = 0; i < blockCount(type); i++) {
            int cx = px + SHAPES[type][rot][i * 2];
            int cy = py + SHAPES[type][rot][i * 2 + 1];
            if (cy >= 4) {
                drawCell(g, bx + cx * 30, by + (cy - 4) * 30, 30, blockValue(type));
            }
        }
    }

    void drawBox(Graphics2D g, int x, int y, int w, int h, String title) {
        g.setColor(new Color(0x1a1216));
        g.fillRect(x, y, w, h);
        g.setColor(new Color(0x2a2230));
        g.fillRect(x + 4, y + 4, w - 8, h - 8);
        g.setColor(new Color(0x3a3040));
        g.fillRect(x + 4, y + 4, w - 8, 3);
        if (title != null) {
            g.setFont(fontBold.deriveFont(16.5f));
            g.setColor(new Color(0xe9c98f));
            g.drawString(title, x + 12, y + 28);
        }
    }

    // 작은 블록 (홀드, 넥스트)
    void drawMini(Graphics2D g, int type, int x, int y) {
        if (type < 0) {
            return;
        }
        int size = 18;
        int ox = type == 0 ? 0 : (type == 1 ? 0 : 9);
        int oy = type == 0 ? -9 : 0;
        for (int i = 0; i < blockCount(type); i++) {
            drawCell(g, x + ox + SHAPES[type][0][i * 2] * size, y + oy + SHAPES[type][0][i * 2 + 1] * size, size, blockValue(type));
        }
    }

    void drawEngineSessionBoard(Graphics2D g, GameSession session, int boardX, int boardY, boolean blinkOn) {
        if (session == null) {
            return;
        }
        for (int screenRow = 0; screenRow < 20; screenRow++) {
            int engineY = 19 - screenRow;
            boolean blinkRow = blinkOn && session.lineClearBlinkRows[engineY];
            for (int column = 0; column < 10; column++) {
                Tetrominoes shape = session.shapeAt(column, engineY);
                if (shape == Tetrominoes.NoShape) {
                    continue;
                }
                int x = boardX + column * 30;
                int y = boardY + screenRow * 30;
                if (blinkRow) {
                    g.setColor(Color.WHITE);
                    g.fillRect(x + 2, y + 2, 26, 26);
                } else {
                    drawCell(g, x, y, 30, engineBlockValue(shape));
                }
            }
        }

        if (screen == 7 || stageClear || session.curPiece.getShape() == Tetrominoes.NoShape) {
            return;
        }
        int blockVal = engineBlockValue(session.curPiece.getShape());
        int ghostY = session.ghostLandingY();
        if (ghostY != session.curY) {
            for (int i = 0; i < session.curPiece.blockCount(); i++) {
                int x = session.curX + session.curPiece.x(i);
                int y = ghostY - session.curPiece.y(i);
                if (x >= 0 && x < 10 && y >= 0 && y < 20) {
                    drawGhostCell(g, boardX + x * 30, boardY + (19 - y) * 30, 30, blockVal);
                }
            }
        }
        for (int i = 0; i < session.curPiece.blockCount(); i++) {
            int x = session.curX + session.curPiece.x(i);
            int y = session.curY - session.curPiece.y(i);
            if (x >= 0 && x < 10 && y >= 0 && y < 20) {
                drawCell(g, boardX + x * 30, boardY + (19 - y) * 30, 30, blockVal);
            }
        }
    }

    void drawSingleEngineBoard(Graphics2D g, int boardX, int boardY) {
        if (aEngineBridge == null) {
            return;
        }
        drawEngineSessionBoard(g, aEngineBridge.session(), boardX, boardY, aEngineBridge.isLineClearBlinkOn());
    }

    boolean isPlayerBlueprintCellOccupied(int player, int row, int column) {
        if (!isTwoPlayer && useAEngine()) {
            GameSession session = aEngineBridge.session();
            int engineY = Blueprint.ROWS - 1 - row;
            return session != null && session.shapeAt(column, engineY) != Tetrominoes.NoShape;
        }
        int[][] board = player == 0 ? board1 : board2;
        return board[18 + row][column] != 0;
    }

    boolean isBlueprintCellFilledByOtherPlayer(int player, int row, int column) {
        return isTwoPlayer
                && !isPlayerBlueprintCellOccupied(player, row, column)
                && isPlayerBlueprintCellOccupied(1 - player, row, column);
    }

    void drawBlueprintGuide(Graphics2D g, int boardX, int boardY) {
        Blueprint blueprint = bossPattern.blueprint();
        if (stage != 3 || blueprint == null) {
            return;
        }
        for (int row = 0; row < Blueprint.ROWS; row++) {
            for (int column = 0; column < Blueprint.COLUMNS; column++) {
                int x = boardX + column * 30;
                int y = boardY + (14 + row) * 30;
                if (blueprint.isTarget(row, column)) {
                    g.setColor(new Color(233, 201, 143, 58));
                    g.fillRect(x + 3, y + 3, 24, 24);
                    g.setColor(new Color(233, 201, 143, 150));
                    g.drawRect(x + 3, y + 3, 23, 23);
                } else if (blueprint.mustBeEmpty(row, column)) {
                    g.setColor(new Color(190, 65, 55, 115));
                    g.drawRect(x + 4, y + 4, 21, 21);
                    g.drawLine(x + 7, y + 7, x + 22, y + 22);
                    g.drawLine(x + 22, y + 7, x + 7, y + 22);
                }
            }
        }
    }

    void drawBlueprintStatus(Graphics2D g, int boardX, int boardY, int player) {
        Blueprint blueprint = bossPattern.blueprint();
        if (stage != 3 || blueprint == null) {
            return;
        }
        for (int row = 0; row < Blueprint.ROWS; row++) {
            for (int column = 0; column < Blueprint.COLUMNS; column++) {
                boolean occupied = isPlayerBlueprintCellOccupied(player, row, column);
                boolean filledByOther = isBlueprintCellFilledByOtherPlayer(player, row, column);
                int x = boardX + column * 30;
                int y = boardY + (14 + row) * 30;
                if (blueprint.mustBeEmpty(row, column) && occupied) {
                    g.setColor(new Color(220, 55, 45, 150));
                    g.fillRect(x + 1, y + 1, 28, 28);
                    g.setColor(new Color(255, 225, 210));
                    g.drawLine(x + 6, y + 6, x + 23, y + 23);
                    g.drawLine(x + 23, y + 6, x + 6, y + 23);
                } else if (blueprint.isTarget(row, column) && occupied) {
                    g.setColor(new Color(100, 220, 120, 210));
                    g.drawRect(x + 2, y + 2, 25, 25);
                } else if (blueprint.isTarget(row, column) && filledByOther) {
                    g.setColor(new Color(115, 135, 165, 105));
                    g.fillRect(x + 3, y + 3, 24, 24);
                    g.setColor(new Color(205, 220, 235, 175));
                    g.drawRect(x + 3, y + 3, 23, 23);
                    g.drawLine(x + 5, y + 20, x + 14, y + 27);
                    g.drawLine(x + 14, y + 4, x + 27, y + 14);
                }
            }
        }
    }

    void drawGame(Graphics2D g) {
        g.drawImage(Art.background(stage), 0, 0, null);
        BufferedImage m = Art.monster(stage);
        int bob = (tick / 36) % 2 == 0 ? 0 : 3;
        int mx = isTwoPlayer ? 640 - m.getWidth() / 2 : 1100 - m.getWidth() / 2;
        int my = 704 - m.getHeight() + bob;
        g.drawImage(m, mx, my, null);
        if (hitEffect > 0 && hitEffect % 4 < 2) {
            g.setColor(new Color(255, 255, 255, 110));
            g.fillRect(mx, my, m.getWidth(), m.getHeight());
        }
        Art.effect(g, stage, 1280, 720, gameTime);
        if (gameTime < blizzardEnd) {
            g.setColor(new Color(200, 230, 255, 50));
            g.fillRect(0, 0, 1280, 720);
        }

        // 1p 보드
        int bx = isTwoPlayer ? 190 : 490;
        int by = 104;
        g.setColor(new Color(0x1a1216));
        g.fillRect(bx - 6, by - 6, 312, 612);
        g.setColor(new Color(0x6b5a3a));
        g.fillRect(bx - 3, by - 3, 306, 606);
        g.setColor(new Color(10, 8, 14, 205));
        g.fillRect(bx, by, 300, 600);
        g.setColor(new Color(255, 255, 255, 18));
        for (int c = 1; c < 10; c++) {
            g.fillRect(bx + c * 30, by, 1, 600);
        }
        for (int r = 1; r < 20; r++) {
            g.fillRect(bx, by + r * 30, 300, 1);
        }
        drawBlueprintGuide(g, bx, by);
        if (useCoopEngineHost()) {
            drawEngineSessionBoard(g, coopEngineBridge.session(0), bx, by,
                    coopEngineBridge.isLineClearBlinkOn(0));
        } else if (!useAEngine()) {
            for (int r = 4; r < 24; r++) {
                for (int c = 0; c < 10; c++) {
                    if (board1[r][c] != 0) {
                        drawCell(g, bx + c * 30, by + (r - 4) * 30, 30, board1[r][c]);
                    }
                }
            }
            if (screen != 7 && !stageClear) {
                drawLegacyActivePiece(g, bx, by, board1, p1Type, p1Rot, p1X, p1Y);
            }
        } else {
            drawSingleEngineBoard(g, bx, by);
        }
        drawBlueprintStatus(g, bx, by, 0);
        int hx = isTwoPlayer ? 56 : 356;
        drawBox(g, hx, 104, 116, 104, "홀드");
        if (!useAEngine()) {
            drawMini(g, p1Hold, hx + 22, 150);
        } else {
            GameSession session = aEngineBridge.session();
            if (session != null && !session.hold.isEmpty()) {
                drawMini(g, engineType(session.hold.getStored()), hx + 22, 150);
            }
        }
        if (isTwoPlayer) {
            drawBox(g, 56, 226, 116, 280, "다음");
            for (int i = 0; i < 3; i++) {
                drawMini(g, p1Next.get(i), 56 + 22, 270 + i * 76);
            }
            drawBox(g, 40, 524, 132, 176, null);
        } else {
            drawBox(g, 808, 104, 116, 400, "다음");
            if (!useAEngine()) {
                for (int i = 0; i < 5; i++) {
                    drawMini(g, p1Next.get(i), 808 + 22, 146 + i * 70);
                }
            } else {
                GameSession session = aEngineBridge.session();
                if (session != null && session.randomizer != null) {
                    for (int i = 0; i < 3; i++) {
                        drawMini(g, engineType(session.randomizer.peekAt(i)), 808 + 22, 146 + i * 70);
                    }
                }
            }
            drawBox(g, 336, 530, 136, 170, null);
        }
        int ix = isTwoPlayer ? 40 : 336;
        int iy = isTwoPlayer ? 524 : 530;
        g.setFont(fontSmall.deriveFont(13.5f));
        g.setColor(new Color(0x9a8f9f));
        g.drawString("점수", ix + 14, iy + 30);
        g.drawString("지운 줄", ix + 14, iy + 84);
        g.drawString("플레이어", ix + 14, iy + 136);
        g.setFont(font.deriveFont(22f));
        g.setColor(new Color(0xf0e2c8));
        g.drawString(String.format("%,d", p1Score), ix + 14, iy + 58);
        g.drawString("" + p1Lines, ix + 14, iy + 112);
        g.setFont(font.deriveFont(16.5f));
        g.setColor(new Color(0xe9c98f));
        g.drawString(loginUser == null ? "게스트" : loginUser, ix + 14, iy + 158);

        // 2p도 똑같이
        if (isTwoPlayer) {
            bx = 790;
            g.setColor(new Color(0x1a1216));
            g.fillRect(bx - 6, by - 6, 312, 612);
            g.setColor(new Color(0x6b5a3a));
            g.fillRect(bx - 3, by - 3, 306, 606);
            g.setColor(new Color(10, 8, 14, 205));
            g.fillRect(bx, by, 300, 600);
            g.setColor(new Color(255, 255, 255, 18));
            for (int c = 1; c < 10; c++) {
                g.fillRect(bx + c * 30, by, 1, 600);
            }
            for (int r = 1; r < 20; r++) {
                g.fillRect(bx, by + r * 30, 300, 1);
            }
            drawBlueprintGuide(g, bx, by);
            if (useCoopEngineHost()) {
                drawEngineSessionBoard(g, coopEngineBridge.session(1), bx, by,
                        coopEngineBridge.isLineClearBlinkOn(1));
            } else {
                for (int r = 4; r < 24; r++) {
                    for (int c = 0; c < 10; c++) {
                        if (board2[r][c] != 0) {
                            drawCell(g, bx + c * 30, by + (r - 4) * 30, 30, board2[r][c]);
                        }
                    }
                }
                if (screen != 7 && !stageClear) {
                    drawLegacyActivePiece(g, bx, by, board2, p2Type, p2Rot, p2X, p2Y);
                }
            }
            drawBlueprintStatus(g, bx, by, 1);
            drawBox(g, 1108, 104, 116, 104, "홀드");
            drawMini(g, p2Hold, 1108 + 22, 150);
            drawBox(g, 1108, 226, 116, 280, "다음");
            for (int i = 0; i < 3; i++) {
                drawMini(g, p2Next.get(i), 1108 + 22, 270 + i * 76);
            }
            drawBox(g, 1108, 524, 132, 176, null);
            g.setFont(fontSmall.deriveFont(13.5f));
            g.setColor(new Color(0x9a8f9f));
            g.drawString("점수", 1122, 554);
            g.drawString("지운 줄", 1122, 608);
            g.drawString("플레이어", 1122, 660);
            g.setFont(font.deriveFont(22f));
            g.setColor(new Color(0xf0e2c8));
            g.drawString(String.format("%,d", p2Score), 1122, 582);
            g.drawString("" + p2Lines, 1122, 636);
            g.setFont(font.deriveFont(16.5f));
            g.setColor(new Color(0xe9c98f));
            g.drawString("P2", 1122, 682);

            g.setFont(fontBold.deriveFont(22f));
            shadowText(g, "P1", 56, 90, new Color(0x7fc8ff), new Color(0x0c090d), 2);
            shadowText(g, "P2", 1108, 90, new Color(0xffa07a), new Color(0x0c090d), 2);
        }

        // 보스 체력
        String[] stageNames = {"", "불타는 사막", "고립된 섬", "고요한 초원", "얼어붙은 설원", "하늘 위 마왕성"};
        String[] bossNames = {"", "분노한 선인장 괴물", "심연의 해신", "천년 거목", "설원의 포효자", "타락한 악의 신"};
        int barX = 380 + (hitEffect > 0 ? (hitEffect % 2 == 0 ? 3 : -3) : 0);
        g.setColor(new Color(0x1a1216));
        g.fillRect(barX, 14, 520, 76);
        g.setColor(new Color(0x221b24));
        g.fillRect(barX + 4, 18, 512, 68);
        g.setFont(fontSmall.deriveFont(13.5f));
        g.setColor(new Color(0x9a8f9f));
        g.drawString("STAGE " + stage + " · " + stageNames[stage], barX + 16, 38);
        g.setFont(fontBold.deriveFont(16.5f));
        g.setColor(new Color(0xf0e2c8));
        g.drawString(bossNames[stage], barX + 504 - g.getFontMetrics().stringWidth(bossNames[stage]), 38);
        g.setColor(new Color(0x1a1216));
        g.fillRect(barX + 16, 50, 488, 24);
        boolean customVictory = bossPattern.usesCustomVictory();
        double progress = customVictory ? bossPattern.progress(this) : (double) bossHp / bossMax;
        int violations = customVictory ? bossPattern.violationCount(this) : 0;
        int filled = (int) (482.0 * Math.max(0.0, Math.min(1.0, progress)));
        g.setColor(customVictory ? (violations > 0 ? new Color(0xc9432f) : new Color(0x4fa65a)) : new Color(0xc9432f));
        g.fillRect(barX + 19, 53, filled, 18);
        g.setColor(new Color(255, 255, 255, 60));
        g.fillRect(barX + 19, 53, filled, 4);
        g.setColor(new Color(0, 0, 0, 90));
        for (int i = 1; i < 10; i++) {
            g.fillRect(barX + 19 + 482 * i / 10, 53, 2, 18);
        }
        g.setFont(font.deriveFont(13.5f));
        g.setColor(new Color(0xf0e2c8));
        String hp = customVictory
                ? "BLUEPRINT " + Math.round(progress * 100) + "%" + (violations > 0 ? "  MISPLACED " + violations : "")
                : bossHp + " / " + bossMax;
        g.drawString(hp, barX + 260 - g.getFontMetrics().stringWidth(hp) / 2, 67);

        if (bossWarning && screen == 5 && (tick / 10) % 2 == 0) {
            String warn = bossWarningText.isEmpty() ? "!! 보스 공격 준비 중 !!" : bossWarningText;
            g.setFont(fontBold.deriveFont(22f));
            int w = g.getFontMetrics().stringWidth(warn);
            g.setColor(new Color(120, 20, 20, 200));
            g.fillRect(640 - w / 2 - 14, 96, w + 28, 36);
            g.setColor(new Color(0xfff4e6));
            g.drawString(warn, 640 - w / 2, 122);
        }
        if (msgTimer > 0 && screen == 5) {
            g.setFont(font.deriveFont(16.5f));
            int w = g.getFontMetrics().stringWidth(msg);
            g.setColor(new Color(0, 0, 0, 170));
            g.fillRect(640 - w / 2 - 12, 140, w + 24, 30);
            g.setColor(new Color(0xe9c98f));
            g.drawString(msg, 640 - w / 2, 161);
        }
        if (stageClear) {
            g.setColor(new Color(0, 0, 0, 120));
            g.fillRect(0, 280, 1280, 140);
            g.setFont(fontBold.deriveFont(66f));
            centerText(g, "STAGE CLEAR!", 640, 360, new Color(0xe9c98f), new Color(0x6b2a1a));
            g.setFont(font.deriveFont(22f));
            centerText(g, bossNames[stage] + "을(를) 쓰러뜨렸다!", 640, 400, new Color(0xf0e2c8), new Color(0x0c090d));
        }
        String help = keyName(keyPause) + " / Esc 일시정지";
        g.setFont(fontSmall.deriveFont(13.5f));
        int hw = g.getFontMetrics().stringWidth(help);
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(8, 696, hw + 20, 20);
        g.setColor(new Color(0xd9c29a));
        g.drawString(help, 18, 711);
    }

    void drawPause(Graphics2D g) {
        g.setColor(new Color(8, 6, 10, 170));
        g.fillRect(0, 0, 1280, 720);
        drawPanel(g, 420, 120, 440, 480);
        g.setFont(fontBold.deriveFont(33f));
        centerText(g, "일시정지", 640, 180, new Color(0x1a1216), new Color(0xefe0bf));
        drawButton(g, "계속하기", 490, 210, 300, 54, 1, 22f);
        drawButton(g, "저장하기", 490, 282, 300, 54, 0, 22f);
        drawButton(g, "환경설정", 490, 354, 300, 54, 0, 22f);
        drawButton(g, "메인으로", 490, 426, 300, 54, 0, 22f);
        g.setFont(fontSmall.deriveFont(13.5f));
        g.setColor(new Color(0x1a1216));
        String t = msgTimer > 0 ? msg : "";
        g.drawString(t, 640 - g.getFontMetrics().stringWidth(t) / 2, 530);
    }

    void drawResult(Graphics2D g) {
        g.setColor(new Color(8, 6, 10, 170));
        g.fillRect(0, 0, 1280, 720);
        drawPanel(g, 390, 170, 500, 380);
        g.setFont(fontBold.deriveFont(44f));
        centerText(g, allClear ? "ALL CLEAR!" : "GAME OVER", 640, 240, allClear ? new Color(0x6b2a1a) : new Color(0x1a1216), new Color(0xefe0bf));
        g.setFont(font.deriveFont(22f));
        g.setColor(new Color(0x1a1216));
        String s1 = allClear ? "다섯 보스를 모두 쓰러뜨렸어요!" : "스테이지 " + stage + "에서 쓰러졌어요.";
        g.drawString(s1, 640 - g.getFontMetrics().stringWidth(s1) / 2, 300);
        String s2 = isTwoPlayer ? "P1 " + String.format("%,d", p1Score) + "  ·  P2 " + String.format("%,d", p2Score)
                : "점수 " + String.format("%,d", p1Score);
        g.drawString(s2, 640 - g.getFontMetrics().stringWidth(s2) / 2, 344);
        if (isTwoPlayer) {
            String s3 = "합계 " + String.format("%,d", p1Score + p2Score);
            g.drawString(s3, 640 - g.getFontMetrics().stringWidth(s3) / 2, 380);
        }
        g.setFont(font.deriveFont(16.5f));
        g.setColor(new Color(0x3a5a2a));
        g.drawString(resultMsg, 640 - g.getFontMetrics().stringWidth(resultMsg) / 2, 430);
        drawButton(g, "메인으로", 490, 470, 300, 54, 1, 22f);
    }

    public void beginOnlineCoop(int playerIndex, String coopId, long seed) {
        isOnlineCoop = true;
        localPlayerIndex = playerIndex;
        onlineCoopId = coopId;
        random.setSeed(seed);
        newGame(true);
        if (playerIndex == 0) {
            if (coopEngineBridge == null) {
                coopEngineBridge = new CoopEngineBridge(this);
            }
            coopEngineBridge.startOnlineHost(stage, seed);
        }
        screen = 5;
        msg = playerIndex == 0 ? "온라인 협동 · 방장 (P1)" : "온라인 협동 · 참가 (P2)";
        msgTimer = 150;
        requestFocusInWindow();
        closeOnlineLobbyUi();
    }

    public void closeOnlineLobbyUi() {
        if (onlineLobbyFrame != null) {
            onlineLobbyFrame.disposeKeepConnection();
            onlineLobbyFrame = null;
        }
    }

    public void applyRemoteCoopAction(int playerIndex, String actionName) {
        if (!isOnlineCoop || actionName == null) {
            return;
        }
        PlayerAction action;
        try {
            action = PlayerAction.valueOf(actionName);
        } catch (IllegalArgumentException e) {
            return;
        }
        performCoopAction(playerIndex, action, true);
    }

    public void onCoopPeerDisconnected(String text) {
        msg = text == null ? "상대 연결이 끊어졌어요." : text;
        msgTimer = 300;
        if (screen == 5 && !stageClear) {
            screen = 6;
            soundManager.pauseBgm();
        }
    }

    void performCoopAction(int playerIndex, PlayerAction action, boolean fromNetwork) {
        if (screen != 5 || stageClear) {
            return;
        }
        if (useCoopEngineHost()) {
            coopEngineBridge.applyAction(playerIndex, action);
        } else {
            switch (playerIndex) {
                case 0:
                    applyActionP1(action);
                    break;
                case 1:
                    applyActionP2(action);
                    break;
                default:
                    return;
            }
        }
        if (!fromNetwork && isOnlineCoop && coopBridge != null && localPlayerIndex == playerIndex) {
            coopBridge.sendLocalAction(action);
        }
    }

    void applyActionP1(PlayerAction action) {
        switch (action) {
            case MOVE_LEFT:
                moveP1(-1);
                break;
            case MOVE_RIGHT:
                moveP1(1);
                break;
            case SOFT_DROP:
                softDropP1();
                break;
            case HARD_DROP:
                hardDropP1();
                break;
            case ROTATE_CW:
                rotateP1(1);
                break;
            case ROTATE_CCW:
                rotateP1(-1);
                break;
            case HOLD:
                holdP1();
                break;
            default:
                break;
        }
    }

    void applyActionP2(PlayerAction action) {
        switch (action) {
            case MOVE_LEFT:
                moveP2(-1);
                break;
            case MOVE_RIGHT:
                moveP2(1);
                break;
            case SOFT_DROP:
                softDropP2();
                break;
            case HARD_DROP:
                hardDropP2();
                break;
            case ROTATE_CW:
                rotateP2(1);
                break;
            case ROTATE_CCW:
                rotateP2(-1);
                break;
            case HOLD:
                holdP2();
                break;
            default:
                break;
        }
    }
}
