package kr.ac.jbnu.se.tetris;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Stroke;
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
import java.io.Serial;
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
import kr.ac.jbnu.se.tetris.boss.RandomKeyMappingGenerator;
import kr.ac.jbnu.se.tetris.boss.brain.BossBrainRunner;
import kr.ac.jbnu.se.tetris.boss.brain.Grid;
import kr.ac.jbnu.se.tetris.boss.brain.MaxMinBrain;
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
import kr.ac.jbnu.se.tetris.network.lobby.LobbyService;

public class GamePanel extends JPanel implements ActionListener, KeyListener, MouseListener, MouseMotionListener, FocusListener, BossPatternTarget {

    @Serial
    private static final long serialVersionUID = 1L;

    
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
    /** 온라인 협동: P1(왼쪽)=방장, P2(오른쪽)=입장자 */
    public String onlineCoopHostNick;
    public String onlineCoopGuestNick;
    public String onlineCoopId;
    public transient CoopNetworkBridge coopBridge;
    public transient LobbyService onlineLobbyClient;
    transient LobbyFrame onlineLobbyFrame;
    private transient boolean returningToOnlineLobby;
    public transient CoopEngineBridge coopEngineBridge;
    public String lastOnlineRoomId;
    public CoopGameMemento pendingCoopResume;
    java.util.List<SaveSlotMeta> coopSlotList = new ArrayList<>();
    /** 8=협동 슬롯 UI — 1 저장, 2 불러오기, 3 온라인 참가자 필수 저장 */
    int coopSlotMode;
    /** 슬롯 UI 닫기/완료 후 돌아갈 화면 (0 메인, 6 일시정지) */
    int coopSlotReturnScreen;
    static final int PAUSE_BTN_X = 490;
    static final int PAUSE_BTN_W = 300;
    static final int PAUSE_BTN_H = 54;
    static final int PAUSE_BTN_GAP = 18;
    static final int PAUSE_Y_CONTINUE = 210;
    static final int PAUSE_Y_SAVE = PAUSE_Y_CONTINUE + PAUSE_BTN_H + PAUSE_BTN_GAP;
    static final int PAUSE_Y_LOAD = PAUSE_Y_SAVE + PAUSE_BTN_H + PAUSE_BTN_GAP;
    static final int PAUSE_Y_SETTINGS = PAUSE_Y_LOAD + PAUSE_BTN_H + PAUSE_BTN_GAP;
    static final int PAUSE_Y_MAIN = PAUSE_Y_SETTINGS + PAUSE_BTN_H + PAUSE_BTN_GAP;
    /** 일시정지 안내 문구 — 마지막 버튼(그림자 포함) 아래 */
    static final int PAUSE_MSG_Y = PAUSE_Y_MAIN + PAUSE_BTN_H + 28;
    static final int PAUSE_PANEL_H = PAUSE_MSG_Y - 120 + 24;
    /** 방장이 선택한 슬롯 (참가자 저장 UI) */
    int onlineCoopHostSaveSlot;
    CoopGameMemento pendingGuestCoopSave;
    boolean awaitingGuestCoopSave;
    public int stage = 1;
    public int bossHp;
    public int bossMax;
    public int bossTimer;
    public long gameTime;
    public boolean bossWarning = false;
    public String bossWarningText = "";
    public long blizzardEnd = 0;
    /** 스테이지 2 쓰레기 줄 (방장 발행, 참가자 자기 보드에 적용) */
    public int garbageSeq;
    public int garbageHole0 = -1;
    public int garbageHole1 = -1;
    /** 스테이지 3: 참가자 블루프린트 칸 (bit = row * 10 + col) */
    public long onlineGuestBlueprint;
    public boolean stageClear = false;
    boolean stageClearSoundPlayed;
    public int clearTimer = 0;
    /** 온라인: 클리어 연출 후 다음 스테이지 확인 패널 (방장 스냅샷 권위) */
    public boolean stageAdvancePrompt = false;
    public boolean stageReadyPlayer0 = false;
    public boolean stageReadyPlayer1 = false;
    /** 협동 엔진: P0/P1 보드 탈락 (둘 다 true일 때만 결과 화면) */
    public boolean coopEliminated0;
    public boolean coopEliminated1;
    int nextStagePreview = 0;
    static final int STAGE_ADVANCE_BTN_X = 490;
    static final int STAGE_ADVANCE_BTN_Y = 470;
    static final int STAGE_ADVANCE_BTN_W = 300;
    static final int STAGE_ADVANCE_BTN_H = 54;
    boolean allClear = false;
    public int hitEffect = 0;
    int bossAwakeningEffect = 0;
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
    private final MaxMinBrain maxMinBrain = new MaxMinBrain();
    private final transient SoundManager soundManager;
    public int gravityMultiplier = 1;
    boolean p1FastNext;
    boolean p2FastNext;
    boolean p1FastCurrent;
    boolean p2FastCurrent;
    int[] temporaryPrimaryKeyMapping;
    int[] temporarySecondaryKeyMapping;
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
            updateStageClear();
            return;
        }
        if (!(isOnlineCoop && localPlayerIndex == 1)) {
            gameTime += 16;
        }
        updateTemporaryBossEffects();
        updateKeyRepeat();
        if (screen == 6 && isOnlineCoop && coopBridge != null && coopBridge.isHost()) {
            coopBridge.maybeSendSnapshot();
            return;
        }
        if (screen != 5 || stageClear) {
            return;
        }
        updateGravity();
        if (screen != 5 || stageClear) {
            return;
        }
        updateBossAndNetwork();
    }

    private void updateStageClear() {
        if (isOnlineCoop && coopBridge != null && !coopBridge.isHost()) {
            return;
        }
        clearTimer -= 16;
        if (clearTimer <= 0) {
            if (isOnlineCoop && coopBridge != null && stage < 5) {
                if (!stageAdvancePrompt) {
                    stageAdvancePrompt = true;
                    stageReadyPlayer0 = false;
                    stageReadyPlayer1 = false;
                    ensureNextStagePreviewLoaded(stage + 1);
                }
                tryAdvanceOnlineCoopStage();
            } else if (stage == 5) {
                gameEnd(true);
            } else {
                stage++;
                startStage();
                msg = "STAGE " + stage + " 시작!";
                msgTimer = 90;
            }
        }
        // 클리어 팝업은 여기서 켜지는데, 예전에는 return 때문에 참가자에게 스냅샷이 안 갔다
        if (coopBridge != null) {
            coopBridge.maybeSendSnapshot();
        }
    }

    private void updateKeyRepeat() {
        int[] k = currentPrimaryKeys();
        if (!useAEngine() && controlsPlayer(0)) {
            repeatHeldKeys(0, k, i -> performCoopAction(0, KEY_ACTIONS[i], false));
        } else if (useAEngine() && screen == 5 && !stageClear) {
            repeatHeldKeys(0, k, i -> aEngineBridge.handleKey(k[i], k));
        }
        if (isTwoPlayer && controlsPlayer(1)) {
            repeatHeldKeys(1, onlineCoopKeysForP2(), i -> performCoopAction(1, KEY_ACTIONS[i], false));
        }
    }

    /** 왼쪽/오른쪽/아래 키(0~2번)를 누르고 있는 동안 DAS·ARR 간격으로 fire를 호출한다. */
    private void repeatHeldKeys(int player, int[] keys, java.util.function.IntConsumer fire) {
        int[] counts = player == 0
                ? new int[]{p1LeftCount, p1RightCount, p1DownCount}
                : new int[]{p2LeftCount, p2RightCount, p2DownCount};
        for (int i = 0; i < 3; i++) {
            if (!pressed.contains(keys[i])) {
                continue;
            }
            boolean softDrop = i == 2;
            counts[i] += 16;
            if (counts[i] >= (softDrop ? SOFT_DROP_REPEAT_MS : HORIZONTAL_DAS_DELAY_MS)) {
                fire.accept(i);
                counts[i] = softDrop ? 0 : counts[i] - HORIZONTAL_ARR_INTERVAL_MS;
            }
        }
        if (player == 0) {
            p1LeftCount = counts[0];
            p1RightCount = counts[1];
            p1DownCount = counts[2];
        } else {
            p2LeftCount = counts[0];
            p2RightCount = counts[1];
            p2DownCount = counts[2];
        }
    }

    private void updateGravity() {
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
    }

    private void updateBossAndNetwork() {
        if (useCoopEngineHost()) {
            coopEngineBridge.tickHost();
        } else if (isOnlineCoop && useCoopEngineRender()) {
            coopEngineBridge.tickGuestLocal(localPlayerIndex);
        }
        if (!isOnlineCoop || localPlayerIndex == 0) {
            bossPattern.tick(16, this);
            bossTimer = bossPattern.elapsedMillis();
        }
        if (hitEffect > 0) {
            hitEffect--;
        }
        if (bossAwakeningEffect > 0) {
            bossAwakeningEffect--;
        }
        if (coopBridge != null) {
            coopBridge.maybeSendSnapshot();
            coopBridge.maybeSendMyBoard();
        }
    }

    boolean useAEngine() {
        return !isTwoPlayer && aEngineBridge != null && aEngineBridge.isAttached();
    }

    static int baseDropIntervalMillis(int stage) {
        return Math.max(1, 1000 - (stage - 1) * 150);
    }

    int[] currentPrimaryKeys() {
        int[] originalKeys = isOnlineCoop || !isTwoPlayer ? keySingle : keyP1;
        return applyTemporaryKeyMapping(originalKeys, temporaryPrimaryKeyMapping);
    }

    int[] onlineCoopKeysForP2() {
        if (isOnlineCoop) {
            return localPlayerIndex == 1 ? currentPrimaryKeys() : keySingle;
        }
        return currentSecondaryKeys();
    }

    private int[] applyTemporaryKeyMapping(int[] originalKeys, int[] mapping) {
        return mapping == null
                ? originalKeys
                : RandomKeyMappingGenerator.applyPermutation(originalKeys, mapping);
    }

    boolean controlsPlayer(int playerIndex) {
        if (useCoopEngineRender() && isCoopPlayerEliminated(playerIndex)) {
            return false;
        }
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
        if (isOnlineCoop) {
            return false;
        }
        return true;
    }

    public boolean useCoopEngineHost() {
        return coopEngineBridge != null && coopEngineBridge.isDrivingHost();
    }

    public boolean useCoopEngineRender() {
        return coopEngineBridge != null && coopEngineBridge.hasRenderEngine();
    }

    void openCoopSlotScreen(int mode) {
        openCoopSlotScreen(mode, 0);
    }

    void openCoopSlotScreen(int mode, int returnScreen) {
        coopSlotMode = mode;
        coopSlotReturnScreen = returnScreen;
        coopSlotList = SaveService.listCoopSlots(loginUser);
        screen = 8;
        if (mode == 1 && isOnlineCoop && coopBridge != null && coopBridge.isHost()) {
            coopBridge.notifyGuestSaveUiOpen();
        }
    }

    void openCoopGuestSaveScreen(int hostSlot, CoopGameMemento memento) {
        awaitingGuestCoopSave = true;
        onlineCoopHostSaveSlot = hostSlot;
        pendingGuestCoopSave = memento;
        coopSlotMode = 3;
        coopSlotList = SaveService.listCoopSlots(loginUser);
        screen = 8;
        msg = "방장이 슬롯 " + hostSlot + "에 저장했어요. 이어서 저장해 주세요.";
        msgTimer = 260;
    }

    public void onHostCoopSaveSessionOpen() {
        if (!isOnlineCoop || coopBridge == null || coopBridge.isHost()) {
            return;
        }
        openCoopGuestSaveScreenWaiting();
    }

    public void onHostCoopSaveSessionCancelled() {
        if (!isOnlineCoop || coopBridge == null || coopBridge.isHost()) {
            return;
        }
        awaitingGuestCoopSave = false;
        pendingGuestCoopSave = null;
        if (screen == 8 && coopSlotMode == 3) {
            screen = 6;
            msg = "방장이 저장을 취소했어요.";
            msgTimer = 180;
        }
    }

    void openCoopGuestSaveScreenWaiting() {
        awaitingGuestCoopSave = true;
        onlineCoopHostSaveSlot = 0;
        pendingGuestCoopSave = null;
        coopSlotMode = 3;
        coopSlotList = SaveService.listCoopSlots(loginUser);
        screen = 8;
        msg = "방장과 함께 저장합니다. 방장이 슬롯을 고르면 이어서 저장해 주세요.";
        msgTimer = 280;
    }

    public void onHostCoopSavePush(int hostSlot, String snapshotJson) {
        if (!isOnlineCoop || coopBridge == null || coopBridge.isHost() || snapshotJson == null || snapshotJson.isBlank()) {
            return;
        }
        CoopGameMemento mem = new Gson().fromJson(snapshotJson, CoopGameMemento.class);
        if (mem == null) {
            return;
        }
        if (screen == 8 && coopSlotMode == 3 && awaitingGuestCoopSave) {
            pendingGuestCoopSave = mem;
            onlineCoopHostSaveSlot = hostSlot;
            msg = "방장이 슬롯 " + hostSlot + "을(를) 골랐어요. 같은 슬롯에 저장해 주세요.";
            msgTimer = 260;
            return;
        }
        openCoopGuestSaveScreen(hostSlot, mem);
    }

    boolean confirmCoopSlotOverwrite(int slot) {
        if (slot < 1 || slot > 3 || coopSlotList.size() < slot) {
            return true;
        }
        SaveSlotMeta meta = coopSlotList.get(slot - 1);
        if (!meta.occupied) {
            return true;
        }
        int r = JOptionPane.showConfirmDialog(this,
                "슬롯 " + slot + "의 기존 저장을 덮어씌울까요?\n\n" + meta.summary,
                "덮어쓰기", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return r == JOptionPane.YES_OPTION;
    }

    void fillCoopAccountIds(CoopGameMemento mem) {
        if (!isOnlineCoop || mem == null || loginUser == null) {
            return;
        }
        String host = onlineCoopHostNick != null && !onlineCoopHostNick.isBlank() ? onlineCoopHostNick : loginUser;
        String guest = onlineCoopGuestNick;
        if (coopBridge != null && coopBridge.isHost()) {
            host = loginUser;
        } else if (coopBridge != null && !coopBridge.isHost()) {
            guest = loginUser;
        }
        mem.hostAccountId = host;
        mem.guestAccountId = guest;
    }

    int[] currentSecondaryKeys() {
        return applyTemporaryKeyMapping(keyP2, temporarySecondaryKeyMapping);
    }

    void updateTemporaryBossEffects() {
        if (gravityMultiplier > 1 && gameTime >= blizzardEnd) {
            gravityMultiplier = 1;
            soundManager.stopManagedSfx(SfxType.STAGE_4_PATTERN);
            if (useAEngine()) {
                aEngineBridge.setGravityMultiplier(1);
            }
        }
        if (temporaryPrimaryKeyMapping != null && gameTime >= randomKeyEnd) {
            temporaryPrimaryKeyMapping = null;
            temporarySecondaryKeyMapping = null;
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

    boolean raiseBoardAndPiece(int[][] board, boolean playerOne, int hole) {
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
        raiseBlocks(random.nextInt(10));
    }

    @Override
    public void raiseBlocks(int garbageHole) {
        if (garbageHole < 0 || garbageHole >= GameSession.WIDTH) {
            throw new IllegalArgumentException("Garbage hole must be on the board");
        }
        if (useCoopEngineHost()) {
            if (!coopEngineBridge.raiseGarbageLines(garbageHole, garbageHole)) {
                gameEnd(false);
            }
            return;
        }
        if (useAEngine()) {
            if (!aEngineBridge.raiseGarbageLine(garbageHole)) {
                gameEnd(false);
            }
            return;
        }
        if (!raiseBoardAndPiece(board1, true, garbageHole)) {
            gameEnd(false);
            return;
        }
        if (isTwoPlayer && !raiseBoardAndPiece(board2, false, garbageHole)) {
            gameEnd(false);
        }
    }

    @Override
    public int selectGarbageHole() {
        if (useAEngine()) {
            GameSession session = aEngineBridge.session();
            if (session != null) {
                BossBrainRunner.Decision decision = aEngineBridge.brainDecision();
                if (decision != null && decision.responsePiece() == session.curPiece.getShape()) {
                    return decision.garbageHole();
                }
                Grid board = aEngineBridge.brainSnapshot();
                return maxMinBrain.chooseGarbageHole(board == null ? Grid.from(session) : board,
                        session.curPiece.getShape());
            }
        }
        return random.nextInt(10);
    }

    @Override
    public Tetrominoes selectForcedPiece() {
        if (useAEngine()) {
            GameSession session = aEngineBridge.session();
            if (session != null) {
                BossBrainRunner.Decision decision = aEngineBridge.brainDecision();
                if (decision != null && decision.responsePiece() == session.curPiece.getShape()) {
                    return decision.forcedPiece();
                }
                Grid board = aEngineBridge.brainSnapshot();
                return maxMinBrain.chooseForcedPiece(board == null ? Grid.from(session) : board);
            }
        }
        return Tetrominoes.values()[1 + random.nextInt(7)];
    }

    @Override
    public void replaceCurrentPiece(Tetrominoes type) {
        if (type == null || type == Tetrominoes.NoShape) {
            return;
        }
        if (useAEngine()) {
            aEngineBridge.replaceCurrentPiece(type);
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
        return (isOnlineCoop || !isTwoPlayer ? keySingle : keyP1).clone();
    }

    @Override
    public int[] secondaryOriginalKeys() {
        return (isOnlineCoop ? keySingle : keyP2).clone();
    }

    @Override
    public boolean isTwoPlayerMode() {
        return isTwoPlayer;
    }

    @Override
    public void applyTemporaryKeyMappings(int[] primaryMapping, int[] secondaryMapping, int durationMillis) {
        temporaryPrimaryKeyMapping = primaryMapping.clone();
        temporarySecondaryKeyMapping = secondaryMapping == null ? null : secondaryMapping.clone();
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
        temporaryPrimaryKeyMapping = null;
        temporarySecondaryKeyMapping = null;
        randomKeyEnd = 0;
        clearInputRepeatState();
        if (aEngineBridge != null) {
            aEngineBridge.resetBossEffects();
        }
    }

    boolean isCoopPlayers() {
        return isTwoPlayer || isOnlineCoop;
    }

    @Override
    public boolean isBlueprintCellOccupied(int row, int column) {
        if (row < 0 || row >= Blueprint.ROWS || column < 0 || column >= Blueprint.COLUMNS) {
            return false;
        }
        if (useCoopEngineHost()) {
            GameSession hostSession = coopEngineBridge.session(0);
            int engineY = Blueprint.ROWS - 1 - row;
            if (hostSession != null && hostSession.shapeAt(column, engineY) != Tetrominoes.NoShape) {
                return true;
            }
            int bit = row * Blueprint.COLUMNS + column;
            return ((onlineGuestBlueprint >>> bit) & 1L) != 0;
        }
        if (useCoopEngineRender()) {
            boolean occupied = false;
            for (int player = 0; player < 2; player++) {
                GameSession session = coopEngineBridge.session(player);
                int engineY = Blueprint.ROWS - 1 - row;
                if (session != null && session.shapeAt(column, engineY) != Tetrominoes.NoShape) {
                    occupied = true;
                    break;
                }
            }
            return occupied;
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
        triggerBossAwakening();
        beginStageClearSequence();
        pressed.clear();
        msg = "BLUEPRINT COMPLETE!";
        msgTimer = 120;
        bossPattern.stop(this);
        playStageClearSound();
    }

    public void notifyBossBoardChanged() {
        if (stage != 3 || stageClear) {
            return;
        }
        if (isOnlineCoop && coopBridge != null && !coopBridge.isHost()) {
            coopBridge.sendBlueprintMask(localBlueprintMask());
            return;
        }
        bossPattern.onBoardChanged(this);
    }

    long localBlueprintMask() {
        if (coopEngineBridge == null) {
            return 0;
        }
        GameSession session = coopEngineBridge.session(localPlayerIndex);
        if (session == null) {
            return 0;
        }
        long mask = 0;
        for (int row = 0; row < Blueprint.ROWS; row++) {
            int engineY = Blueprint.ROWS - 1 - row;
            for (int column = 0; column < Blueprint.COLUMNS; column++) {
                if (session.shapeAt(column, engineY) != Tetrominoes.NoShape) {
                    mask |= 1L << (row * Blueprint.COLUMNS + column);
                }
            }
        }
        return mask;
    }

    public void applyGuestBlueprintMask(String hex) {
        if (!useCoopEngineHost() || hex == null || hex.isBlank()) {
            return;
        }
        try {
            onlineGuestBlueprint = Long.parseUnsignedLong(hex, 16);
        } catch (NumberFormatException ignored) {
            return;
        }
        if (stage == 3 && !stageClear) {
            bossPattern.onBoardChanged(this);
        }
    }

    public void reportOnlineLinesCleared(int count) {
        if (!isOnlineCoop || coopBridge == null || coopBridge.isHost() || count <= 0) {
            return;
        }
        int n = Math.min(4, count);
        PlayerAction action = switch (n) {
            case 1 -> PlayerAction.LINES_1;
            case 2 -> PlayerAction.LINES_2;
            case 3 -> PlayerAction.LINES_3;
            default -> PlayerAction.LINES_4;
        };
        coopBridge.sendLocalAction(action);
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
    void beginStageClearSequence() {
        stageClear = true;
        clearTimer = 2000;
        stageAdvancePrompt = false;
        stageReadyPlayer0 = false;
        stageReadyPlayer1 = false;
    }

    public void ensureNextStagePreviewLoaded(int nextStage) {
        if (nextStage < 1 || nextStage > 5) {
            return;
        }
        nextStagePreview = nextStage;
    }

    boolean isStageReady(int playerIndex) {
        return playerIndex == 0 ? stageReadyPlayer0 : stageReadyPlayer1;
    }

    void setStageReady(int playerIndex, boolean ready) {
        if (playerIndex == 0) {
            stageReadyPlayer0 = ready;
        } else {
            stageReadyPlayer1 = ready;
        }
    }

    boolean bothStageReadyOnline() {
        return stageReadyPlayer0 && stageReadyPlayer1;
    }

    void tryAdvanceOnlineCoopStage() {
        if (!isOnlineCoop || !useCoopEngineHost() || !stageAdvancePrompt || !bothStageReadyOnline()) {
            return;
        }
        advanceOnlineCoopStage();
    }

    void advanceOnlineCoopStage() {
        stageAdvancePrompt = false;
        stageClear = false;
        stageReadyPlayer0 = false;
        stageReadyPlayer1 = false;
        stageClearSoundPlayed = false;
        clearTimer = 0;
        stage++;
        startStage();
        msg = "STAGE " + stage + " 시작!";
        msgTimer = 90;
    }

    static String stageGimmickHint(int nextStage) {
        return switch (nextStage) {
            case 2 -> "20초마다 바닥에서 쓰레기 줄이 한 줄씩 올라옵니다.";
            case 3 -> "블루프린트 목표대로 블록을 배치해 클리어하세요.";
            case 4 -> "보스 패턴 동안 중력이 빨라집니다.";
            case 5 -> "주기적으로 조작키가 바뀝니다.";
            default -> "협력해서 보스 HP를 0으로 만드세요.";
        };
    }

    public void applyBossLineDamage(int count) {
        if (count <= 0 || stageClear) {
            return;
        }
        if (stage == 3) {
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
        triggerBossAwakening();
        if (bossHp <= 0) {
            bossHp = 0;
            beginStageClearSequence();
            pressed.clear();
            bossPattern.stop(this);
            playStageClearSound();
        }
    }

    void triggerBossAwakening() {
        bossAwakeningEffect = stage == 5 ? 75 : 70;
    }

    public void notifyEngineGameOver() {
        if (useCoopEngineRender() || (isOnlineCoop && coopEngineBridge != null)) {
            onCoopPlayerEliminated(localPlayerIndex >= 0 ? localPlayerIndex : 0);
            return;
        }
        if (screen == 5 && !stageClear) {
            gameEnd(false);
        }
    }

    public boolean isCoopPlayerEliminated(int playerIndex) {
        return playerIndex == 0 ? coopEliminated0 : coopEliminated1;
    }

    public void applyCoopEliminationSync(boolean p0, boolean p1) {
        applyCoopEliminationFromSave(p0, p1, true);
    }

    public void applyCoopEliminationFromSave(boolean p0, boolean p1, boolean endIfBothDead) {
        coopEliminated0 = p0;
        coopEliminated1 = p1;
        if (coopEngineBridge != null) {
            if (p0) {
                coopEngineBridge.freezeEngine(0);
            }
            if (p1) {
                coopEngineBridge.freezeEngine(1);
            }
        }
        if (endIfBothDead && coopEliminated0 && coopEliminated1 && screen == 5 && !stageClear) {
            gameEnd(false);
        }
        repaint();
    }

    public void onCoopPlayerEliminated(int playerIndex) {
        if (screen != 5 || stageClear) {
            return;
        }
        if (playerIndex == 0) {
            if (coopEliminated0) {
                return;
            }
            coopEliminated0 = true;
        } else {
            if (coopEliminated1) {
                return;
            }
            coopEliminated1 = true;
        }
        if (isOnlineCoop && coopBridge != null) {
            coopBridge.syncEliminationState(playerIndex);
        }
        if (coopEliminated0 && coopEliminated1) {
            gameEnd(false);
            return;
        }
        if (playerIndex == localPlayerIndex) {
            msg = "보드가 가득 찼어요. 상대가 끝낼 때까지 대기합니다.";
        } else {
            msg = onlineCoopPlayerLabel(playerIndex) + " 보드가 가득 찼어요.";
        }
        msgTimer = 280;
        repaint();
    }

    void resetCoopElimination() {
        coopEliminated0 = false;
        coopEliminated1 = false;
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
        if (isCoopPlayers()) {
            bossMax *= 2;
        }
        bossHp = bossMax;
        bossTimer = 0;
        bossWarning = false;
        bossWarningText = "";
        blizzardEnd = 0;
        stageClear = false;
        garbageSeq = 0;
        garbageHole0 = -1;
        garbageHole1 = -1;
        onlineGuestBlueprint = 0;
        stageAdvancePrompt = false;
        stageReadyPlayer0 = false;
        stageReadyPlayer1 = false;
        resetCoopElimination();
        stageClearSoundPlayed = false;
        hitEffect = 0;
        bossAwakeningEffect = 0;
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
        } else if (useCoopEngineHost()) {
            coopEngineBridge.restartStageEngine(stage);
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
        if (type < 0 || type >= SHAPES.length || rot < 0 || rot >= SHAPES[type].length) {
            return false;
        }
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
        if (count > 0 && !stageClear) {
            if (stage == 3) {
                playLineClearSound(count);
                return count;
            }
            int damage = 0;
            if (count == 1) damage = 10;
            if (count == 2) damage = 25;
            if (count == 3) damage = 40;
            if (count == 4) damage = 60;
            bossHp -= damage;
            hitEffect = 12;
            triggerBossAwakening();
            if (bossHp <= 0) {
                bossHp = 0;
                beginStageClearSequence();
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
        if (isOnlineCoop && coopBridge != null) {
            coopBridge.sendGameOver();
        }
        if (isOnlineCoop) {
            tryDissolveOnlineRoom();
        }
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
                    if (isOnlineCoop && coopBridge != null) {
                        performCoopAction(localPlayerIndex, PlayerAction.PAUSE, false);
                    } else {
                        enterLocalPause("");
                    }
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
                if (isOnlineCoop && coopBridge != null) {
                    performCoopAction(localPlayerIndex, PlayerAction.RESUME, false);
                } else {
                    resumeLocalPause();
                }
            } else if (screen == 1 || screen == 3) {
                screen = 0;
            }
        }
    }

    private static final PlayerAction[] KEY_ACTIONS = {
        PlayerAction.MOVE_LEFT, PlayerAction.MOVE_RIGHT, PlayerAction.SOFT_DROP, PlayerAction.HARD_DROP,
        PlayerAction.ROTATE_CW, PlayerAction.ROTATE_CCW, PlayerAction.HOLD
    };

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
                promptTwoPlayerMode();
            } else if (clicked(88, 402, 300, 54)) {
                loadRanking();
                screen = 3;
            } else if (clicked(88, 472, 300, 54)) {
                settingFrom = 0;
                editKeys = new int[][]{keySingle.clone(), keyP1.clone(), keyP2.clone(), {keyPause}};
                setTab = 0;
                captureRow = -1;
                setMsg = "";
                screen = 4;
            } else if (clicked(88, 542, 300, 54)) {
                int r = JOptionPane.showOptionDialog(this, "모험을 마치고 게임을 종료할까요?", "게임 종료",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[]{"종료", "취소"}, "취소");
                if (r == 0) {
                    System.exit(0);
                }
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
                newGame(false);
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
        } else if (screen == 5 && stageAdvancePrompt && isOnlineCoop) {
            if (!isStageReady(localPlayerIndex)
                    && clicked(STAGE_ADVANCE_BTN_X, STAGE_ADVANCE_BTN_Y, STAGE_ADVANCE_BTN_W, STAGE_ADVANCE_BTN_H)) {
                performCoopAction(localPlayerIndex, PlayerAction.STAGE_READY, false);
            }
        } else if (screen == 6) {
            if (clicked(PAUSE_BTN_X, PAUSE_Y_CONTINUE, PAUSE_BTN_W, PAUSE_BTN_H)) {
                if (isOnlineCoop && awaitingGuestCoopSave) {
                    msg = "방장 저장에 이어 참가자 저장을 먼저 완료해 주세요.";
                    msgTimer = 200;
                } else if (isOnlineCoop && coopBridge != null) {
                    performCoopAction(localPlayerIndex, PlayerAction.RESUME, false);
                } else {
                    resumeLocalPause();
                }
            } else if (clicked(PAUSE_BTN_X, PAUSE_Y_SAVE, PAUSE_BTN_W, PAUSE_BTN_H)) {
                if (isOnlineCoop && loginUser == null) {
                    msg = "온라인 협동 저장은 로그인 후 이용할 수 있어요.";
                    msgTimer = 200;
                } else if (isOnlineCoop && coopBridge != null && !coopBridge.isHost()) {
                    msg = "방장만 협동 저장을 시작할 수 있어요.";
                    msgTimer = 180;
                } else if (isTwoPlayer || isOnlineCoop) {
                    openCoopSlotScreen(1, 6);
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
            } else if (clicked(PAUSE_BTN_X, PAUSE_Y_LOAD, PAUSE_BTN_W, PAUSE_BTN_H)) {
                if (isOnlineCoop && coopBridge != null && !coopBridge.isHost()) {
                    msg = "방장만 불러올 수 있어요.";
                    msgTimer = 180;
                } else if (isOnlineCoop && loginUser == null) {
                    msg = "온라인 협동은 로그인 후 불러올 수 있어요.";
                    msgTimer = 200;
                } else if (isTwoPlayer || isOnlineCoop) {
                    openCoopSlotScreen(2, 6);
                }
            } else if (clicked(PAUSE_BTN_X, PAUSE_Y_SETTINGS, PAUSE_BTN_W, PAUSE_BTN_H)) {
                openSettings();
            } else if (clicked(PAUSE_BTN_X, PAUSE_Y_MAIN, PAUSE_BTN_W, PAUSE_BTN_H)) {
                if (isOnlineCoop) {
                    int r = JOptionPane.showOptionDialog(this,
                            "온라인 협동을 종료하고 로비로 돌아갈까요?\n(이 방은 목록에서 사라져요.)",
                            "로비로",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                            new String[]{"로비로", "취소"}, "취소");
                    if (r == 0) {
                        returnToOnlineLobby(null);
                    }
                } else if (loginUser != null) {
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
                if (isOnlineCoop) {
                    returnToOnlineLobby(null);
                } else {
                    screen = 0;
                }
            }
        } else if (screen == 8) {
            handleCoopSlotClick();
        }
    }

    private void showMsg(String text, int frames) {
        msg = text;
        msgTimer = frames;
    }

    private void leaveToTitle() {
        screen = 0;
        soundManager.stopBgm();
    }

    void tryDissolveOnlineRoom() {
        LobbyService service = onlineLobbyClient;
        if (service == null) {
            return;
        }
        try {
            service.leaveRoom();
        } catch (Exception ignored) {
        }
    }

    private LobbyClient resolveLobbyClient() {
        if (onlineLobbyClient instanceof LobbyClient client) {
            return client;
        }
        return null;
    }

    public void returnToOnlineLobby(String reason) {
        if (!isOnlineCoop) {
            leaveToTitle();
            return;
        }
        if (returningToOnlineLobby) {
            return;
        }
        returningToOnlineLobby = true;
        try {
            LobbyClient client = resolveLobbyClient();
            if (coopBridge != null) {
                coopBridge.detach();
                coopBridge = null;
            }
            tryDissolveOnlineRoom();
            clearOnlineCoopGameState();
            screen = 0;
            pressed.clear();
            soundManager.stopBgm();
            if (client != null) {
                onlineLobbyClient = client;
                closeOnlineLobbyUi();
                SwingUtilities.invokeLater(() -> {
                    onlineLobbyFrame = new LobbyFrame(this, client);
                    onlineLobbyFrame.setVisible(true);
                    if (reason != null && !reason.isBlank()) {
                        JOptionPane.showMessageDialog(this, reason, "온라인 로비", JOptionPane.INFORMATION_MESSAGE);
                    }
                });
            } else {
                leaveToTitle();
            }
        } finally {
            returningToOnlineLobby = false;
        }
    }

    private void clearOnlineCoopGameState() {
        isOnlineCoop = false;
        isTwoPlayer = false;
        onlineCoopId = null;
        onlineCoopHostNick = null;
        onlineCoopGuestNick = null;
        pendingCoopResume = null;
        pendingGuestCoopSave = null;
        awaitingGuestCoopSave = false;
        stageClear = false;
        coopEliminated0 = false;
        coopEliminated1 = false;
        if (coopEngineBridge != null) {
            coopEngineBridge.stop();
            coopEngineBridge = null;
        }
        bossPattern.stop(this);
    }

    void handleCoopSlotClick() {
        if (clicked(490, 560, 300, 48)) {
            if (coopSlotMode == 3) {
                msg = "참가자 저장을 완료해야 합니다.";
                msgTimer = 180;
                return;
            }
            if (coopSlotMode == 1 && isOnlineCoop && coopBridge != null && coopBridge.isHost()) {
                coopBridge.notifyGuestSaveUiCancelled();
            }
            if (coopSlotMode == 1 || coopSlotMode == 3) {
                screen = 6;
            } else {
                screen = coopSlotReturnScreen;
            }
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
        if (coopSlotMode == 3) {
            saveCoopSlotAsGuest(slot);
        } else if (coopSlotMode == 1) {
            saveCoopSlotAsHost(slot);
        } else {
            loadCoopSlot(slot);
        }
    }

    private void saveCoopSlotAsGuest(int slot) {
        if (loginUser == null) {
            showMsg("로그인 후 저장할 수 있어요.", 180);
            return;
        }
        if (pendingGuestCoopSave == null) {
            showMsg("방장이 슬롯을 선택할 때까지 기다려 주세요.", 200);
            return;
        }
        boolean allFull = SaveService.allCoopSlotsOccupied(loginUser);
        if (!allFull && slot != onlineCoopHostSaveSlot) {
            showMsg("방장과 같은 슬롯 " + onlineCoopHostSaveSlot + "에 저장해 주세요.", 220);
            return;
        }
        if (!confirmCoopSlotOverwrite(slot)) {
            return;
        }
        try {
            SaveService.saveCoop(loginUser, slot, pendingGuestCoopSave);
            pendingGuestCoopSave = null;
            awaitingGuestCoopSave = false;
            showMsg("협동 슬롯 " + slot + "에 저장했어요.", 200);
        } catch (Exception ex) {
            showMsg("저장 실패: " + ex.getMessage(), 200);
        }
        screen = 6;
    }

    private void saveCoopSlotAsHost(int slot) {
        if (!confirmCoopSlotOverwrite(slot)) {
            return;
        }
        try {
            saveCoopToSlot(slot);
            showMsg(isOnlineCoop
                    ? "협동 슬롯 " + slot + "에 저장했어요. 참가자도 저장할 때까지 기다려 주세요."
                    : "협동 슬롯 " + slot + "에 저장했어요.", 200);
        } catch (Exception ex) {
            showMsg("저장 실패: " + ex.getMessage(), 200);
        }
        screen = 6;
    }

    private void loadCoopSlot(int slot) {
        try {
            CoopGameMemento mem = SaveService.loadCoop(loginUser, slot);
            if (isOnlineCoop && coopBridge != null &&             coopBridge.isHost() && coopSlotReturnScreen == 6) {
                resumeOnlineCoopAsHost(slot, mem);
            } else if (mem.onlineResume) {
                openLobbyForCoopResume(slot, mem);
            } else {
                resumeLocalCoop(slot, mem);
            }
        } catch (LoadException ex) {
            showMsg(ex.getMessage(), 180);
            if (coopSlotReturnScreen == 6) {
                screen = 6;
            }
        }
    }

    private void resumeOnlineCoopAsHost(int slot, CoopGameMemento mem) throws LoadException {
        if (!mem.onlineResume) {
            throw new LoadException("온라인 협동 저장 슬롯만 불러올 수 있어요.");
        }
        SaveService.assertOnlineRoomMatchesSave(mem, onlineCoopHostNick, onlineCoopGuestNick);
        CoopGameMemento remapped = SaveService.remapForCurrentOnlineHost(mem, onlineCoopHostNick,
                onlineCoopGuestNick);
        onlineCoopId = remapped.coopId;
        CoopEngineBridge.applyLoadedMemento(this, remapped);
        screen = 6;
        coopBridge.pushLoadedState(remapped);
        showMsg("슬롯 " + slot + " · 온라인 협동 저장을 불러왔어요.", 220);
    }

    private void openLobbyForCoopResume(int slot, CoopGameMemento mem) {
        pendingCoopResume = mem;
        showMsg("슬롯 " + slot + "을 불러왔어요. 로비에서 「게임 시작 (방장)」으로 이어하세요.", 260);
        screen = coopSlotReturnScreen;
        if (coopSlotReturnScreen == 0) {
            SwingUtilities.invokeLater(() -> {
                onlineLobbyFrame = new LobbyFrame(this);
                onlineLobbyFrame.setVisible(true);
            });
        }
    }

    private void resumeLocalCoop(int slot, CoopGameMemento mem) {
        isOnlineCoop = false;
        CoopEngineBridge.applyLoadedMemento(this, mem);
        if (coopSlotReturnScreen == 6) {
            screen = 6;
            msg = "슬롯 " + slot + " · 단일 PC 2인 협동을 불러왔어요.";
        } else {
            msg = "슬롯 " + slot + " · 단일 PC 2인 협동을 이어합니다.";
        }
        msgTimer = 150;
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
        fillCoopAccountIds(mem);
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
        m.stageAdvancePrompt = stageAdvancePrompt;
        m.stageReadyPlayer0 = stageReadyPlayer0;
        m.stageReadyPlayer1 = stageReadyPlayer1;
        m.blizzardEnd = blizzardEnd;
        m.blueprintIndex = bossPattern.blueprintIndex();
        m.onlineResume = isOnlineCoop;
        m.lastRoomId = lastOnlineRoomId;
        m.player0Eliminated = coopEliminated0;
        m.player1Eliminated = coopEliminated1;
        m.gameOver = coopEliminated0 && coopEliminated1;
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
            if (screen == 1) {
                drawSaves(g);
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
        String title = coopSlotMode == 1 ? "협동 저장 · 슬롯 선택"
                : coopSlotMode == 3 ? "참가자 저장 · 슬롯 선택" : "협동 불러오기 · 슬롯 선택";
        centerText(g, title, 640, 150, new Color(0x1a1216), new Color(0xefe0bf));
        g.setFont(font.deriveFont(16f));
        g.setColor(new Color(0x1a1216));
        String hint;
        if (coopSlotMode == 3) {
            boolean allFull = loginUser != null && SaveService.allCoopSlotsOccupied(loginUser);
            hint = allFull
                    ? "슬롯이 가득 찼어요. 덮어쓸 슬롯을 고르세요. (기본: 슬롯 " + onlineCoopHostSaveSlot + ")"
                    : "방장과 같은 슬롯 " + onlineCoopHostSaveSlot + "에 저장해 주세요.";
        } else if (isOnlineCoop || coopSlotMode == 2) {
            hint = loginUser == null ? "온라인 협동은 로그인이 필요해요." : "로그인: " + loginUser;
        } else {
            hint = loginUser == null ? "게스트는 `_local` 슬롯에 저장됩니다." : "로그인: " + loginUser;
        }
        g.drawString(hint, 640 - g.getFontMetrics().stringWidth(hint) / 2, 188);
        for (int i = 0; i < 3; i++) {
            SaveSlotMeta meta = i < coopSlotList.size() ? coopSlotList.get(i) : new SaveSlotMeta(i + 1, false, "비어 있음", 0);
            drawButton(g, "슬롯 " + meta.slot + " — " + meta.summary, 340, 220 + i * 92, 600, 72, i, 17f);
        }
        if (coopSlotMode != 3) {
            drawButton(g, "닫기", 490, 560, 300, 48, 3, 20f);
        }
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

    void centerPlainText(Graphics2D g, String text, int cx, int y, Color c) {
        int w = g.getFontMetrics().stringWidth(text);
        g.setColor(c);
        g.drawString(text, cx - w / 2, y);
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
        drawButton(g, "1인 플레이", 88, 262, 300, 54, 1, 22f);
        drawButton(g, "2인 플레이", 88, 332, 300, 54, 0, 22f);
        drawButton(g, "랭킹", 88, 402, 300, 54, 0, 22f);
        drawButton(g, "환경설정", 88, 472, 300, 54, 0, 22f);
        drawButton(g, "게임 종료", 88, 542, 300, 44, 0, 18f);
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
                g.drawString((s[4].equals("2") ? "단일 PC 2인" : "1인 플레이")
                        + "  ·  점수 " + String.format("%,d", Integer.parseInt(s[3])), 390, y + 54);
                g.drawString("마지막 플레이  " + df.format(new Date(Long.parseLong(s[1]))), 390, y + 74);
                drawButton(g, "이어하기", 760, y + 18, 120, 44, 1, 16.5f);
                drawButton(g, "삭제", 890, y + 18, 110, 44, 0, 16.5f);
            }
        }
        drawButton(g, "+ 새 게임", 490, 612, 300, 54, 1, 22f);
        drawButton(g, "← 뒤로", 40, 640, 140, 44, 2, 16.5f);
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

    void drawCellGray(Graphics2D g, int x, int y, int size) {
        Color c = new Color(0x5c5c66);
        g.setColor(c.darker());
        g.fillRect(x, y, size, size);
        g.setColor(c);
        g.fillRect(x + 2, y + 2, size - 4, size - 4);
        g.setColor(new Color(0x787882));
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
        if (type < 0 || type >= SHAPES.length) {
            return;
        }
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

    void drawEngineSessionBoard(Graphics2D g, GameSession session, int boardX, int boardY, boolean blinkOn,
            int playerIndex) {
        if (session == null) {
            return;
        }
        boolean gray = isCoopPlayerEliminated(playerIndex);
        for (int screenRow = 0; screenRow < 20; screenRow++) {
            int engineY = 19 - screenRow;
            boolean blinkRow = !gray && blinkOn && session.lineClearBlinkRows[engineY];
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
                } else if (gray) {
                    drawCellGray(g, x, y, 30);
                } else {
                    drawCell(g, x, y, 30, engineBlockValue(shape));
                }
            }
        }

        if (gray || screen == 7 || stageClear || session.curPiece.getShape() == Tetrominoes.NoShape) {
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
        drawEngineSessionBoard(g, aEngineBridge.session(), boardX, boardY, aEngineBridge.isLineClearBlinkOn(), 0);
    }

    boolean isPlayerBlueprintCellOccupied(int player, int row, int column) {
        if (useCoopEngineRender()) {
            GameSession session = coopEngineBridge.session(player);
            int engineY = Blueprint.ROWS - 1 - row;
            return session != null && session.shapeAt(column, engineY) != Tetrominoes.NoShape;
        }
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

    void drawBlueprintCellHighlight(Graphics2D g, int x, int y, Color color, int inset, float stroke) {
        Stroke previous = g.getStroke();
        g.setStroke(new BasicStroke(stroke));
        g.setColor(color);
        g.drawRect(x + inset, y + inset, 29 - inset * 2, 29 - inset * 2);
        g.setStroke(previous);
    }

    void drawBlueprintForbiddenMark(Graphics2D g, int x, int y, boolean emphasized) {
        float stroke = emphasized ? 3f : 2f;
        Stroke previous = g.getStroke();
        g.setStroke(new BasicStroke(stroke + 1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(40, 10, 10, emphasized ? 220 : 180));
        g.drawLine(x + 7, y + 7, x + 22, y + 22);
        g.drawLine(x + 22, y + 7, x + 7, y + 22);
        g.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(emphasized ? new Color(255, 235, 225) : new Color(235, 100, 90));
        g.drawLine(x + 7, y + 7, x + 22, y + 22);
        g.drawLine(x + 22, y + 7, x + 7, y + 22);
        g.setStroke(previous);
    }

    boolean isPlayerBlueprintActivePieceAt(int player, int row, int column) {
        if (screen == 7 || stageClear) {
            return false;
        }
        int engineY = Blueprint.ROWS - 1 - row;
        if (useCoopEngineRender()) {
            GameSession session = coopEngineBridge.session(player);
            if (session == null || session.curPiece.getShape() == Tetrominoes.NoShape) {
                return false;
            }
            for (int i = 0; i < session.curPiece.blockCount(); i++) {
                int pieceX = session.curX + session.curPiece.x(i);
                int pieceY = session.curY - session.curPiece.y(i);
                if (pieceX == column && pieceY == engineY) {
                    return true;
                }
            }
            return false;
        }
        if (useAEngine()) {
            GameSession session = aEngineBridge.session();
            if (session == null || session.curPiece.getShape() == Tetrominoes.NoShape) {
                return false;
            }
            for (int i = 0; i < session.curPiece.blockCount(); i++) {
                int pieceX = session.curX + session.curPiece.x(i);
                int pieceY = session.curY - session.curPiece.y(i);
                if (pieceX == column && pieceY == engineY) {
                    return true;
                }
            }
            return false;
        }
        int type = player == 0 ? p1Type : p2Type;
        int rot = player == 0 ? p1Rot : p2Rot;
        int px = player == 0 ? p1X : p2X;
        int py = player == 0 ? p1Y : p2Y;
        int boardRow = 18 + row;
        for (int i = 0; i < blockCount(type); i++) {
            int cx = px + SHAPES[type][rot][i * 2];
            int cy = py + SHAPES[type][rot][i * 2 + 1];
            if (cx == column && cy == boardRow) {
                return true;
            }
        }
        return false;
    }

    boolean isPlayerBlueprintCellVisuallyOccupied(int player, int row, int column) {
        return isPlayerBlueprintCellOccupied(player, row, column)
                || isPlayerBlueprintActivePieceAt(player, row, column);
    }

    void drawBlueprintOverlay(Graphics2D g, int boardX, int boardY, int player) {
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

                if (blueprint.mustBeEmpty(row, column)) {
                    boolean blocked = isPlayerBlueprintCellVisuallyOccupied(player, row, column);
                    drawBlueprintCellHighlight(g, x, y,
                            blocked ? new Color(235, 70, 55) : new Color(190, 75, 65, 200),
                            blocked ? 1 : 3,
                            blocked ? 3f : 1.5f);
                } else if (blueprint.isTarget(row, column)) {
                    if (occupied) {
                        drawBlueprintCellHighlight(g, x, y, new Color(90, 235, 120), 1, 2.5f);
                    } else if (filledByOther) {
                        drawBlueprintCellHighlight(g, x, y, new Color(150, 185, 220), 2, 2f);
                        g.setColor(new Color(210, 230, 250));
                        g.drawLine(x + 5, y + 20, x + 14, y + 27);
                        g.drawLine(x + 14, y + 4, x + 27, y + 14);
                    } else {
                        drawBlueprintCellHighlight(g, x, y, new Color(245, 215, 150), 2, 2f);
                        g.setColor(new Color(255, 248, 220, 180));
                        g.drawRect(x + 5, y + 5, 19, 19);
                    }
                }
            }
        }
        for (int row = 0; row < Blueprint.ROWS; row++) {
            for (int column = 0; column < Blueprint.COLUMNS; column++) {
                if (!blueprint.mustBeEmpty(row, column)) {
                    continue;
                }
                int x = boardX + column * 30;
                int y = boardY + (14 + row) * 30;
                boolean blocked = isPlayerBlueprintCellVisuallyOccupied(player, row, column);
                drawBlueprintForbiddenMark(g, x, y, blocked);
            }
        }
    }

    void drawBossAwakening(Graphics2D g, int bossX) {
        int passed = (stage == 5 ? 75 : 70) - bossAwakeningEffect;
        if (stage == 1) {
            for (int i = 0; i < 14; i++) {
                int x = bossX - 40 - i * 72 + passed * 9;
                int y = 610 - (i * 53 + passed * 5) % 520;
                g.setColor(new Color(75, 112, 46, 210));
                g.fillPolygon(new int[]{x, x + 18, x + 5}, new int[]{y, y + 5, y + 13}, 3);
                g.setColor(new Color(220, 204, 120, 220));
                g.drawLine(x + 2, y + 2, x + 15, y + 5);
            }
        } else if (stage == 2) {
            for (int wave = 0; wave < 3; wave++) {
                int y = 580 - wave * 90 - (passed * 4) % 80;
                g.setColor(new Color(88, 190, 231, 120));
                for (int x = 0; x < 1280; x += 24) {
                    int y1 = y + (int) (Math.sin((x + passed * 18) * 0.05) * 11);
                    int y2 = y + (int) (Math.sin((x + 24 + passed * 18) * 0.05) * 11);
                    g.drawLine(x, y1, x + 24, y2);
                }
            }
            g.setColor(new Color(245, 250, 255, 220));
            int x = bossX - 20 + (passed % 3) * 12;
            int y = 110;
            for (int i = 0; i < 6; i++) {
                int nextX = x + (i % 2 == 0 ? -18 : 20);
                g.drawLine(x, y, nextX, y + 42);
                x = nextX;
                y += 42;
            }
        } else if (stage == 3) {
            for (int i = 0; i < 38; i++) {
                int x = Math.floorMod(i * 97 + passed * (i % 3 + 2) * 5, 1280);
                int y = Math.floorMod(i * 47 + passed * (i % 4 + 3) * 4, 610);
                g.setColor(i % 3 == 0 ? new Color(211, 170, 70, 200)
                        : new Color(92, 151, 70, 210));
                g.fillPolygon(new int[]{x, x + 10, x + 4, x - 5},
                        new int[]{y, y + 4, y + 12, y + 7}, 4);
            }
        } else if (stage == 4) {
            for (int i = 0; i < 44; i++) {
                int x = Math.floorMod(i * 83 + passed * (i % 4 + 2) * 4, 1280);
                int y = Math.floorMod(i * 59 + passed * (i % 3 + 3) * 5, 600);
                g.setColor(i % 4 == 0 ? new Color(145, 218, 255, 210)
                        : new Color(248, 253, 255, 220));
                int size = i % 4 == 0 ? 10 : 5;
                g.fillRect(x, y, size, size);
                if (i % 4 == 0) {
                    g.drawLine(x - 4, y + 5, x + 14, y + 5);
                    g.drawLine(x + 5, y - 4, x + 5, y + 14);
                }
            }
        } else if (stage == 5) {
            int phase = passed / 25;
            if (phase == 1) {
                g.setColor(new Color(15, 16, 52, 185));
                g.fillRect(0, 0, 1280, 720);
                g.setColor(new Color(214, 225, 255, 235));
                g.fillOval(1016, 38, 88, 88);
            } else {
                g.setColor(new Color(255, 212, 105, 135));
                g.fillRect(0, 0, 1280, 720);
                g.setColor(new Color(255, 238, 160, 235));
                g.fillOval(1010, 32, 100, 100);
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
        if (useCoopEngineRender()) {
            drawEngineSessionBoard(g, coopEngineBridge.session(0), bx, by,
                    coopEngineBridge.isLineClearBlinkOn(0), 0);
        } else if (!useAEngine()) {
            boolean gray0 = isCoopPlayerEliminated(0);
            for (int r = 4; r < 24; r++) {
                for (int c = 0; c < 10; c++) {
                    if (board1[r][c] != 0) {
                        int x = bx + c * 30;
                        int y = by + (r - 4) * 30;
                        if (gray0) {
                            drawCellGray(g, x, y, 30);
                        } else {
                            drawCell(g, x, y, 30, board1[r][c]);
                        }
                    }
                }
            }
            if (!gray0 && screen != 7 && !stageClear) {
                drawLegacyActivePiece(g, bx, by, board1, p1Type, p1Rot, p1X, p1Y);
            }
        } else {
            drawSingleEngineBoard(g, bx, by);
        }
        drawBlueprintOverlay(g, bx, by, 0);
        int hx = isTwoPlayer ? 56 : 356;
        drawBox(g, hx, 104, 116, 104, "홀드");
        if (useCoopEngineRender()) {
            GameSession coopS0 = coopEngineBridge.session(0);
            if (coopS0 != null && !coopS0.hold.isEmpty()) {
                drawMini(g, engineType(coopS0.hold.getStored()), hx + 22, 150);
            }
        } else if (!useAEngine()) {
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
                if (useCoopEngineRender()) {
                    GameSession coopS0 = coopEngineBridge.session(0);
                    if (coopS0 != null && coopS0.randomizer != null) {
                        drawMini(g, engineType(coopS0.randomizer.peekAt(i)), 56 + 22, 270 + i * 76);
                    }
                } else if (i < p1Next.size()) {
                    drawMini(g, p1Next.get(i), 56 + 22, 270 + i * 76);
                }
            }
            drawBox(g, 40, 524, 132, 176, null);
        } else {
            drawBox(g, 808, 104, 116, 400, "다음");
            if (!useAEngine()) {
                for (int i = 0; i < 5; i++) {
                    if (i < p1Next.size()) {
                        drawMini(g, p1Next.get(i), 808 + 22, 146 + i * 70);
                    }
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
        g.drawString(onlineCoopPlayerLabel(0), ix + 14, iy + 158);

        
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
            if (useCoopEngineRender()) {
                drawEngineSessionBoard(g, coopEngineBridge.session(1), bx, by,
                        coopEngineBridge.isLineClearBlinkOn(1), 1);
            } else {
                boolean gray1 = isCoopPlayerEliminated(1);
                for (int r = 4; r < 24; r++) {
                    for (int c = 0; c < 10; c++) {
                        if (board2[r][c] != 0) {
                            int x = bx + c * 30;
                            int y = by + (r - 4) * 30;
                            if (gray1) {
                                drawCellGray(g, x, y, 30);
                            } else {
                                drawCell(g, x, y, 30, board2[r][c]);
                            }
                        }
                    }
                }
                if (!gray1 && screen != 7 && !stageClear) {
                    drawLegacyActivePiece(g, bx, by, board2, p2Type, p2Rot, p2X, p2Y);
                }
            }
            drawBlueprintOverlay(g, bx, by, 1);
            drawBox(g, 1108, 104, 116, 104, "홀드");
            if (useCoopEngineRender()) {
                GameSession coopS1 = coopEngineBridge.session(1);
                if (coopS1 != null && !coopS1.hold.isEmpty()) {
                    drawMini(g, engineType(coopS1.hold.getStored()), 1108 + 22, 150);
                }
            } else {
                drawMini(g, p2Hold, 1108 + 22, 150);
            }
            drawBox(g, 1108, 226, 116, 280, "다음");
            for (int i = 0; i < 3; i++) {
                if (useCoopEngineRender()) {
                    GameSession coopS1 = coopEngineBridge.session(1);
                    if (coopS1 != null && coopS1.randomizer != null) {
                        drawMini(g, engineType(coopS1.randomizer.peekAt(i)), 1108 + 22, 270 + i * 76);
                    }
                } else if (i < p2Next.size()) {
                    drawMini(g, p2Next.get(i), 1108 + 22, 270 + i * 76);
                }
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
            g.drawString(onlineCoopPlayerLabel(1), 1122, 682);

            g.setFont(fontBold.deriveFont(22f));
            if (isOnlineCoop) {
                shadowText(g, "P1 · 방장", 56, 90, new Color(0x7fc8ff), new Color(0x0c090d), 2);
                shadowText(g, "P2 · 참가", 1108, 90, new Color(0xffa07a), new Color(0x0c090d), 2);
            } else {
                shadowText(g, "P1", 56, 90, new Color(0x7fc8ff), new Color(0x0c090d), 2);
                shadowText(g, "P2", 1108, 90, new Color(0xffa07a), new Color(0x0c090d), 2);
            }
        }

        if (bossAwakeningEffect > 0) {
            drawBossAwakening(g, mx + m.getWidth() / 2);
        }

        
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
        drawRandomKeyGuide(g, mx + m.getWidth() / 2, my);
        if (stageAdvancePrompt && isOnlineCoop) {
            drawStageAdvancePrompt(g, stageNames, bossNames);
        } else if (stageClear) {
            g.setColor(new Color(0, 0, 0, 120));
            g.fillRect(0, 280, 1280, 140);
            g.setFont(fontBold.deriveFont(66f));
            centerText(g, "GameClear!", 640, 350, new Color(0xe9c98f), new Color(0x6b2a1a));
            g.setFont(font.deriveFont(22f));
            centerText(g, bossNames[stage] + "을(를) 쓰러뜨렸다!", 640, 390, new Color(0xf0e2c8), new Color(0x0c090d));
        }
        String help = keyName(keyPause) + " / Esc 일시정지";
        g.setFont(fontSmall.deriveFont(13.5f));
        int hw = g.getFontMetrics().stringWidth(help);
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(8, 696, hw + 20, 20);
        g.setColor(new Color(0xd9c29a));
        g.drawString(help, 18, 711);
    }

    void drawRandomKeyGuide(Graphics2D g, int bossCenterX, int bossTopY) {
        if (stage != 5 || screen != 5 || stageClear
                || temporaryPrimaryKeyMapping == null || gameTime >= randomKeyEnd) {
            return;
        }

        String[] actions = {
                "왼쪽 이동", "오른쪽 이동", "빠르게 내리기", "즉시 내리기",
                "오른쪽 회전", "왼쪽 회전", "홀드"
        };
        int[] primaryKeys = currentPrimaryKeys();
        boolean showLocalTwoPlayer = isTwoPlayer && !isOnlineCoop;
        int[] secondaryKeys = showLocalTwoPlayer ? currentSecondaryKeys() : null;

        int panelWidth = showLocalTwoPlayer ? 300 : 272;
        int headerHeight = showLocalTwoPlayer ? 54 : 38;
        int rowHeight = 20;
        int panelHeight = headerHeight + actions.length * rowHeight + 10;
        int panelX = Math.max(8, Math.min(1280 - panelWidth - 8, bossCenterX - panelWidth / 2));
        int panelY = Math.max(174, bossTopY - panelHeight - 12);

        g.setColor(new Color(8, 6, 12, 225));
        g.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 14, 14);
        g.setStroke(new BasicStroke(2f));
        g.setColor(new Color(0xb65cff));
        g.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 14, 14);

        int pointerX = Math.max(panelX + 14, Math.min(panelX + panelWidth - 14, bossCenterX));
        int panelBottom = panelY + panelHeight;
        g.setColor(new Color(8, 6, 12, 225));
        g.fillPolygon(new int[]{pointerX - 8, pointerX + 8, pointerX},
                new int[]{panelBottom - 1, panelBottom - 1, panelBottom + 9}, 3);
        g.setColor(new Color(0xb65cff));
        g.drawLine(pointerX - 8, panelBottom, pointerX, panelBottom + 9);
        g.drawLine(pointerX, panelBottom + 9, pointerX + 8, panelBottom);

        long remainingMillis = Math.max(0, randomKeyEnd - gameTime);
        String titlePrefix = isOnlineCoop ? "내 조작키 · 혼란  " : "조작키 혼란  ";
        String title = titlePrefix + String.format("%.1f초", remainingMillis / 1000.0);
        g.setFont(fontBold.deriveFont(16.5f));
        g.setColor(new Color(0xffd7ff));
        g.drawString(title, panelX + 12, panelY + 24);

        if (showLocalTwoPlayer) {
            g.setFont(fontBold.deriveFont(12.5f));
            g.setColor(new Color(0x7fc8ff));
            centerPlainText(g, "P1", panelX + 190, panelY + 45, new Color(0x7fc8ff));
            centerPlainText(g, "P2", panelX + 259, panelY + 45, new Color(0xffa07a));
        }

        g.setFont(fontSmall.deriveFont(12.5f));
        int firstRowY = panelY + headerHeight;
        for (int i = 0; i < actions.length; i++) {
            int rowY = firstRowY + i * rowHeight;
            g.setColor(i % 2 == 0 ? new Color(255, 255, 255, 12) : new Color(255, 255, 255, 4));
            g.fillRect(panelX + 7, rowY, panelWidth - 14, rowHeight);
            g.setFont(fontSmall.deriveFont(12.5f));
            g.setColor(new Color(0xeee4f0));
            g.drawString(actions[i], panelX + 13, rowY + 14);

            if (showLocalTwoPlayer) {
                drawRandomKeyCap(g, keyName(primaryKeys[i]), panelX + 157, rowY + 2, 66, rowHeight - 4,
                        new Color(0x7fc8ff));
                drawRandomKeyCap(g, keyName(secondaryKeys[i]), panelX + 226, rowY + 2, 66, rowHeight - 4,
                        new Color(0xffa07a));
            } else {
                drawRandomKeyCap(g, keyName(primaryKeys[i]), panelX + 166, rowY + 2, 94, rowHeight - 4,
                        new Color(0xe9c98f));
            }
        }
    }

    private void drawRandomKeyCap(Graphics2D g, String key, int x, int y, int width, int height, Color accent) {
        g.setColor(new Color(33, 27, 38, 245));
        g.fillRoundRect(x, y, width, height, 7, 7);
        g.setColor(accent);
        g.drawRoundRect(x, y, width, height, 7, 7);
        float keyFontSize = 11.5f;
        g.setFont(fontSmall.deriveFont(keyFontSize));
        while (g.getFontMetrics().stringWidth(key) > width - 6 && keyFontSize > 8f) {
            keyFontSize -= 0.5f;
            g.setFont(fontSmall.deriveFont(keyFontSize));
        }
        int textX = x + (width - g.getFontMetrics().stringWidth(key)) / 2;
        int textY = y + (height - g.getFontMetrics().getHeight()) / 2 + g.getFontMetrics().getAscent();
        g.drawString(key, textX, textY);
    }

    void drawStageAdvancePrompt(Graphics2D g, String[] stageNames, String[] bossNames) {
        g.setColor(new Color(8, 6, 10, 190));
        g.fillRect(0, 0, 1280, 720);
        drawPanel(g, 340, 130, 600, 460);
        int next = nextStagePreview >= 1 && nextStagePreview <= 5 ? nextStagePreview : Math.min(5, stage + 1);
        g.setFont(fontBold.deriveFont(40f));
        centerPlainText(g, "GameClear!", 640, 185, new Color(0x1a1216));
        g.setFont(fontBold.deriveFont(26f));
        centerPlainText(g, "다음 스테이지", 640, 235, new Color(0x1a1216));
        g.setFont(fontBold.deriveFont(22f));
        String title = "STAGE " + next + " · " + stageNames[next];
        centerPlainText(g, title, 640, 275, new Color(0x1a1216));
        g.setFont(font.deriveFont(18f));
        centerPlainText(g, "보스: " + bossNames[next], 640, 310, new Color(0x1a1216));
        g.setFont(font.deriveFont(16f));
        String hint = stageGimmickHint(next);
        int hintW = g.getFontMetrics().stringWidth(hint);
        if (hintW > 520) {
            g.setColor(new Color(0x1a1216));
            g.drawString(hint, 640 - 260, 350);
        } else {
            centerPlainText(g, hint, 640, 350, new Color(0x1a1216));
        }
        g.setFont(font.deriveFont(15f));
        String p1 = onlineCoopPlayerLabel(0) + ": "
                + (stageReadyPlayer0 ? "준비 완료" : "대기 중…");
        String p2 = onlineCoopPlayerLabel(1) + ": "
                + (stageReadyPlayer1 ? "준비 완료" : "대기 중…");
        centerPlainText(g, p1, 640, 400, new Color(0x1a1216));
        centerPlainText(g, p2, 640, 428, new Color(0x1a1216));
        boolean mineReady = isStageReady(localPlayerIndex);
        if (mineReady) {
            g.setFont(font.deriveFont(17f));
            centerPlainText(g, "상대방 확인을 기다리는 중…", 640, 455, new Color(0x1a1216));
        } else {
            drawButton(g, "다음 스테이지 진행", STAGE_ADVANCE_BTN_X, STAGE_ADVANCE_BTN_Y, STAGE_ADVANCE_BTN_W,
                    STAGE_ADVANCE_BTN_H, 1, 20f);
        }
        g.setFont(fontSmall.deriveFont(13f));
        centerPlainText(g, "두 플레이어 모두 동의하면 시작됩니다.", 640, 545, new Color(0x1a1216));
    }

    void drawPause(Graphics2D g) {
        g.setColor(new Color(8, 6, 10, 170));
        g.fillRect(0, 0, 1280, 720);
        drawPanel(g, 420, 120, 440, PAUSE_PANEL_H);
        g.setFont(fontBold.deriveFont(33f));
        centerText(g, "일시정지", 640, 180, new Color(0x1a1216), new Color(0xefe0bf));
        drawButton(g, "계속하기", PAUSE_BTN_X, PAUSE_Y_CONTINUE, PAUSE_BTN_W, PAUSE_BTN_H, 1, 22f);
        drawButton(g, "저장하기", PAUSE_BTN_X, PAUSE_Y_SAVE, PAUSE_BTN_W, PAUSE_BTN_H, 0, 22f);
        boolean showLoad = isTwoPlayer || isOnlineCoop;
        if (showLoad) {
            drawButton(g, "불러오기", PAUSE_BTN_X, PAUSE_Y_LOAD, PAUSE_BTN_W, PAUSE_BTN_H, 0, 22f);
        }
        drawButton(g, "환경설정", PAUSE_BTN_X, PAUSE_Y_SETTINGS, PAUSE_BTN_W, PAUSE_BTN_H, 0, 22f);
        drawButton(g, isOnlineCoop ? "로비로" : "메인으로", PAUSE_BTN_X, PAUSE_Y_MAIN, PAUSE_BTN_W, PAUSE_BTN_H, 0, 22f);
        g.setFont(fontSmall.deriveFont(13.5f));
        g.setColor(new Color(0x1a1216));
        String t = msgTimer > 0 ? msg : "";
        g.drawString(t, 640 - g.getFontMetrics().stringWidth(t) / 2, PAUSE_MSG_Y);
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
        drawButton(g, isOnlineCoop ? "로비로" : "메인으로", 490, 470, 300, 54, 1, 22f);
    }

    public void applyOnlineCoopRoster(java.util.List<String> playersInOrder, int myPlayerIndex) {
        localPlayerIndex = myPlayerIndex;
        onlineCoopHostNick = null;
        onlineCoopGuestNick = null;
        if (playersInOrder != null && !playersInOrder.isEmpty()) {
            onlineCoopHostNick = playersInOrder.get(0);
        }
        if (playersInOrder != null && playersInOrder.size() >= 2) {
            onlineCoopGuestNick = playersInOrder.get(1);
        }
    }

    String onlineCoopPlayerLabel(int playerIndex) {
        if (!isOnlineCoop) {
            if (playerIndex == 0) {
                return loginUser == null ? "게스트" : loginUser;
            }
            return "P2";
        }
        String nick = playerIndex == 0 ? onlineCoopHostNick : onlineCoopGuestNick;
        if (nick == null || nick.isEmpty()) {
            nick = playerIndex == 0 ? "방장" : "참가";
        }
        if (localPlayerIndex == playerIndex) {
            return nick + " (나)";
        }
        return nick;
    }

    /** 온라인 스냅샷: 보스 기믹 필드를 memento에 기록 (방장 → 참가자). */
    public void fillOnlineBossSyncFields(CoopGameMemento mem) {
        if (mem == null) {
            return;
        }
        mem.gravityMultiplier = gravityMultiplier;
        mem.garbageSeq = garbageSeq;
        mem.garbageHole0 = garbageHole0;
        mem.garbageHole1 = garbageHole1;
        mem.randomKeyEnd = randomKeyEnd;
        if (randomKeyEnd > gameTime && temporaryPrimaryKeyMapping != null) {
            mem.randomKeyMappingForPlayer0 = temporaryPrimaryKeyMapping.clone();
            mem.randomKeyMappingForPlayer1 = temporarySecondaryKeyMapping != null
                    ? temporarySecondaryKeyMapping.clone() : null;
        }
    }

    /** 온라인 참가자: 방장 스냅샷 기준 보스 기믹·경고 UI 동기화 */
    public void applyOnlineBossSync(CoopGameMemento mem) {
        if (mem == null) {
            return;
        }
        gravityMultiplier = mem.gravityMultiplier <= 0 ? 1 : mem.gravityMultiplier;
        blizzardEnd = mem.blizzardEnd;
        if (stage != mem.stage || bossPattern.blueprintIndex() != mem.blueprintIndex) {
            bossPattern.startStage(mem.stage, this, mem.blueprintIndex);
        }
        bossPattern.restoreElapsed(mem.bossTimer, this);
        bossTimer = mem.bossTimer;
        if (mem.randomKeyEnd > mem.gameTime) {
            int[] mapping = onlineKeyMapping(mem, localPlayerIndex);
            if (mapping != null) {
                temporaryPrimaryKeyMapping = mapping;
                temporarySecondaryKeyMapping = null;
                randomKeyEnd = mem.randomKeyEnd;
                return;
            }
        }
        temporaryPrimaryKeyMapping = null;
        temporarySecondaryKeyMapping = null;
        randomKeyEnd = 0;
    }

    private int[] onlineKeyMapping(CoopGameMemento mem, int playerIndex) {
        int[] mapping = playerIndex == 0
                ? mem.randomKeyMappingForPlayer0 : mem.randomKeyMappingForPlayer1;
        if (mapping != null) {
            return mapping.clone();
        }

        int[] legacyKeys = playerIndex == 0 ? mem.randomKeyForPlayer0 : mem.randomKeyForPlayer1;
        int[] legacyOriginal = playerIndex == 0 ? keyP1 : keyP2;
        return RandomKeyMappingGenerator.toPermutation(legacyOriginal, legacyKeys);
    }

    void promptTwoPlayerMode() {
        int choice = JOptionPane.showOptionDialog(this,
                "2인 플레이 방식을 선택하세요.",
                "2인 플레이",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                new String[]{"온라인 (로비)", "로컬 (단일 PC)", "취소"},
                "온라인 (로비)");
        if (choice == 0) {
            if (loginUser == null || loginUser.isBlank()) {
                JOptionPane.showMessageDialog(this,
                        "온라인 협동은 로그인 후 이용할 수 있어요.\n메인 화면에서 로그인해 주세요.",
                        "로그인 필요", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            onlineLobbyFrame = new LobbyFrame(this);
            onlineLobbyFrame.setVisible(true);
        } else if (choice == 1) {
            beginLocalCoop();
        }
    }

    public void beginLocalCoop() {
        isOnlineCoop = false;
        localPlayerIndex = 0;
        onlineCoopId = null;
        pendingCoopResume = null;
        pendingGuestCoopSave = null;
        awaitingGuestCoopSave = false;
        if (coopBridge != null) {
            coopBridge.detach();
            coopBridge = null;
        }
        onlineLobbyClient = null;
        closeOnlineLobbyUi();
        if (coopEngineBridge != null) {
            coopEngineBridge.stop();
            coopEngineBridge = null;
        }
        newGame(true);
        msg = "단일 PC 2인 · P1 / P2";
        msgTimer = 120;
        requestFocusInWindow();
    }

    public void beginOnlineCoop(int playerIndex, String coopId, long seed) {
        isOnlineCoop = true;
        localPlayerIndex = playerIndex;
        onlineCoopId = coopId;
        random.setSeed(seed);
        newGame(true);
        if (coopEngineBridge == null) {
            coopEngineBridge = new CoopEngineBridge(this);
        }
        if (playerIndex == 0) {
            coopEngineBridge.startOnlineHost(stage, seed);
        } else {
            coopEngineBridge.startOnlineGuest(stage, seed);
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
        if (isOnlineCoop) {
            String reason = text == null ? "상대와의 방이 종료되었어요." : text;
            returnToOnlineLobby(reason);
            return;
        }
        msg = text == null ? "상대 연결이 끊어졌어요." : text;
        msgTimer = 300;
        if (screen == 5 && !stageClear) {
            screen = 6;
            soundManager.pauseBgm();
        }
    }

    void enterLocalPause(String pauseMsg) {
        screen = 6;
        msg = pauseMsg == null ? "" : pauseMsg;
        msgTimer = msg.isEmpty() ? 0 : 300;
        pressed.clear();
        if (useAEngine()) {
            aEngineBridge.pauseEngine();
        }
        soundManager.pauseBgm();
    }

    void resumeLocalPause() {
        screen = 5;
        pressed.clear();
        if (useAEngine()) {
            aEngineBridge.resumeEngine();
        }
        soundManager.resumeBgm();
    }

    static int reportedLineCount(PlayerAction action) {
        return switch (action) {
            case LINES_1 -> 1;
            case LINES_2 -> 2;
            case LINES_3 -> 3;
            case LINES_4 -> 4;
            default -> 0;
        };
    }

    void performCoopAction(int playerIndex, PlayerAction action, boolean fromNetwork) {
        int reportedLines = reportedLineCount(action);
        if (reportedLines > 0) {
            if (fromNetwork && useCoopEngineHost() && playerIndex == 1 && coopEngineBridge != null) {
                coopEngineBridge.creditGuestLines(reportedLines);
            }
            return;
        }
        if (action == PlayerAction.STAGE_READY) {
            if (screen != 5 || !stageAdvancePrompt || isStageReady(playerIndex)) {
                return;
            }
            if (useCoopEngineHost()) {
                setStageReady(playerIndex, true);
                tryAdvanceOnlineCoopStage();
            }
            if (!fromNetwork && isOnlineCoop && coopBridge != null && localPlayerIndex == playerIndex) {
                coopBridge.sendLocalAction(action);
            }
            return;
        }
        if (action == PlayerAction.PAUSE) {
            if (screen == 5 && !stageClear) {
                String pauseMsg = fromNetwork ? "상대방이 일시정지했어요." : "";
                enterLocalPause(pauseMsg);
            }
            if (!fromNetwork && isOnlineCoop && coopBridge != null) {
                coopBridge.sendLocalAction(action);
            }
            return;
        }
        if (action == PlayerAction.RESUME) {
            if (screen == 6 && isOnlineCoop && awaitingGuestCoopSave && !fromNetwork
                    && coopBridge != null && !coopBridge.isHost()) {
                msg = "참가자 저장을 먼저 완료해 주세요.";
                msgTimer = 200;
                return;
            }
            if (screen == 6) {
                resumeLocalPause();
            }
            if (!fromNetwork && isOnlineCoop && coopBridge != null) {
                coopBridge.sendLocalAction(action);
            }
            return;
        }
        if (screen != 5 || stageClear) {
            return;
        }
        if (useCoopEngineHost()) {
            coopEngineBridge.applyAction(playerIndex, action);
            if (!fromNetwork && isOnlineCoop && coopBridge != null && localPlayerIndex == playerIndex) {
                coopBridge.sendLocalAction(action);
            }
        } else if (isOnlineCoop && coopBridge != null && !coopBridge.isHost()) {
            if (!fromNetwork && localPlayerIndex == playerIndex) {
                if (coopEngineBridge != null) {
                    coopEngineBridge.applyGuestOptimistic(playerIndex, action);
                }
                coopBridge.sendLocalAction(action);
            }
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
