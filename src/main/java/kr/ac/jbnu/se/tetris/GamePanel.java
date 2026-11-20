package kr.ac.jbnu.se.tetris;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Stroke;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.Serial;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import kr.ac.jbnu.se.tetris.boss.BossPatternController;
import kr.ac.jbnu.se.tetris.input.KeyAction;
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
import kr.ac.jbnu.se.tetris.repository.Repositories;
import kr.ac.jbnu.se.tetris.menu.MenuView;
import kr.ac.jbnu.se.tetris.menu.PauseView;
import kr.ac.jbnu.se.tetris.menu.PixelButton;
import kr.ac.jbnu.se.tetris.menu.RankingView;
import kr.ac.jbnu.se.tetris.menu.ResultView;
import kr.ac.jbnu.se.tetris.menu.SavesView;
import kr.ac.jbnu.se.tetris.menu.SettingsView;
import kr.ac.jbnu.se.tetris.menu.TitleView;
import kr.ac.jbnu.se.tetris.repository.KeyBindingRepository;
import kr.ac.jbnu.se.tetris.repository.SaveRepository;
import kr.ac.jbnu.se.tetris.network.coop.CoopNetworkBridge;
import kr.ac.jbnu.se.tetris.network.coop.CoopSnapshotMapper;
import com.google.gson.Gson;
import kr.ac.jbnu.se.tetris.network.lobby.LobbyService;
import kr.ac.jbnu.se.tetris.network.lobby.LobbyClient;

public class GamePanel extends JPanel implements ActionListener, BossPatternTarget {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final ScreenId SCREEN_TITLE = ScreenId.TITLE;
    public static final ScreenId SCREEN_SAVES = ScreenId.SAVES;
    public static final ScreenId SCREEN_RANKING = ScreenId.RANKING;
    public static final ScreenId SCREEN_SETTINGS = ScreenId.SETTINGS;
    public static final ScreenId SCREEN_GAME = ScreenId.GAME;
    public static final ScreenId SCREEN_PAUSE = ScreenId.PAUSE;
    public static final ScreenId SCREEN_RESULT = ScreenId.RESULT;
    public static final ScreenId SCREEN_COOP_SLOTS = ScreenId.COOP_SLOTS;

    private ScreenId screen = ScreenId.TITLE;

    Timer timer;
    int tick = 0;
    int flameFrame = 0;
    float glow = 1f;
    int mouseX, mouseY;

    private String loginUser = null;

    public Font font;
    public Font fontBold;
    public Font fontSmall;
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
    /** 슬롯 UI 닫기/완료 후 돌아갈 화면 */
    ScreenId coopSlotReturnScreen = ScreenId.TITLE;
    static final int SCREEN_CENTER_X = 640;
    static final int CELL_SIZE = 30;

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
    /** ~0.19s base + 0.5s extra at 16ms/tick (boss hit pose + flash). */
    /** Fit boss PNG into playfield (full character visible above board). */
    static final int BOSS_MAX_DRAW_WIDTH = 520;
    static final int BOSS_MAX_DRAW_HEIGHT = 560;

    private final HitFlash hitFlash = new HitFlash();
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
    private static final kr.ac.jbnu.se.tetris.input.KeyBindings keySingle = kr.ac.jbnu.se.tetris.input.KeyBindings.defaultSingle();
    private static final kr.ac.jbnu.se.tetris.input.KeyBindings keyP1 = kr.ac.jbnu.se.tetris.input.KeyBindings.defaultP1();
    private static final kr.ac.jbnu.se.tetris.input.KeyBindings keyP2 = kr.ac.jbnu.se.tetris.input.KeyBindings.defaultP2();
    private static int keyPause = KeyEvent.VK_P;

    public static kr.ac.jbnu.se.tetris.input.KeyBindings keySingle() {
        return keySingle;
    }

    public static kr.ac.jbnu.se.tetris.input.KeyBindings keyP1() {
        return keyP1;
    }

    public static kr.ac.jbnu.se.tetris.input.KeyBindings keyP2() {
        return keyP2;
    }

    public static int keyPause() {
        return keyPause;
    }

    /** 설정 저장/불러오기에서 게임이 읽는 키를 통째로 바꾼다(값만 복사해 객체 자체는 유지). */
    static void setKeys(kr.ac.jbnu.se.tetris.input.KeyBindings single, kr.ac.jbnu.se.tetris.input.KeyBindings p1,
                        kr.ac.jbnu.se.tetris.input.KeyBindings p2, int pause) {
        for (KeyAction action : KeyAction.values()) {
            keySingle.set(action, single.get(action));
            keyP1.set(action, p1.get(action));
            keyP2.set(action, p2.get(action));
        }
        keyPause = pause;
    }

    HashSet<Integer> pressed = new HashSet<>();


    int rankTab = 0;
    java.util.List<RecordEntry> rankList = new ArrayList<>();
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
    private InputStream openResource(String path) throws java.io.IOException {
        InputStream in = getClass().getResourceAsStream(path);
        if (in == null) {
            throw new java.io.IOException("resource not found: " + path);
        }
        return in;
    }

    private final Repositories repos;
    private final java.util.Map<ScreenId, MenuView> views = new java.util.EnumMap<>(ScreenId.class);
    private final SettingsView settingsView;

    public GamePanel(Repositories repos) {
        this(new SoundManager(), repos);
    }

    @SuppressWarnings("this-escape")
    public GamePanel(SoundManager soundManager, Repositories repos) {
        this.repos = repos;
        views.put(SCREEN_TITLE, new TitleView(this));
        views.put(SCREEN_SAVES, new SavesView(this));
        views.put(SCREEN_RANKING, new RankingView(this));
        settingsView = new SettingsView(this);
        views.put(SCREEN_SETTINGS, settingsView);
        views.put(SCREEN_PAUSE, new PauseView(this));
        views.put(SCREEN_RESULT, new ResultView(this));
        this.soundManager = soundManager == null ? new SoundManager() : soundManager;
        setPreferredSize(new Dimension(1280, 720));
        setFocusable(true);
        setLayout(null);
        try {
            InputStream in = openResource("/fonts/Galmuri11.ttf");
            font = Font.createFont(Font.TRUETYPE_FONT, in);
            in = openResource("/fonts/Galmuri11-Bold.ttf");
            fontBold = Font.createFont(Font.TRUETYPE_FONT, in);
            in = openResource("/fonts/Galmuri9.ttf");
            fontSmall = Font.createFont(Font.TRUETYPE_FONT, in);
            mapImg = ImageIO.read(openResource("/images/worldmap.png"));
        } catch (java.awt.FontFormatException | java.io.IOException e) {
            System.out.println("font or image load error");
            font = new Font("Dialog", Font.PLAIN, 12);
            fontBold = new Font("Dialog", Font.BOLD, 12);
            fontSmall = font;
        }
        SwingUtilities.invokeLater(() -> {
            addKeyListener(new KeyHandler());
            MouseHandler mouseHandler = new MouseHandler();
            addMouseListener(mouseHandler);
            addMouseMotionListener(mouseHandler);
            addFocusListener(new FocusHandler());
        });
        setFocusTraversalKeysEnabled(false);
        repos.keys().loadKeys();
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
        if (screen == SCREEN_GAME) {
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
        aEngineBridge.setVisible(screen == SCREEN_GAME && !stageClear);
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
        if (screen == SCREEN_PAUSE && isOnlineCoop && coopBridge != null && coopBridge.isHost()) {
            coopBridge.maybeSendSnapshot();
            return;
        }
        if (screen != SCREEN_GAME || stageClear) {
            return;
        }
        updateGravity();
        if (screen != SCREEN_GAME || stageClear) {
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
        } else if (useAEngine() && screen == SCREEN_GAME && !stageClear) {
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
        if (screen != SCREEN_GAME) {
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
        hitFlash.tick();
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
        int[] originalKeys = (isOnlineCoop || !isTwoPlayer ? keySingle : keyP1).toArray();
        return applyTemporaryKeyMapping(originalKeys, temporaryPrimaryKeyMapping);
    }

    int[] onlineCoopKeysForP2() {
        if (isOnlineCoop) {
            return localPlayerIndex == 1 ? currentPrimaryKeys() : keySingle.toArray();
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
        openCoopSlotScreen(mode, SCREEN_TITLE);
    }

    public void openCoopSlotScreen(int mode, ScreenId returnScreen) {
        coopSlotMode = mode;
        coopSlotReturnScreen = returnScreen;
        coopSlotList = SaveService.listCoopSlots(loginUser);
        screen = SCREEN_COOP_SLOTS;
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
        screen = SCREEN_COOP_SLOTS;
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
        if (screen == SCREEN_COOP_SLOTS && coopSlotMode == 3) {
            screen = SCREEN_PAUSE;
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
        screen = SCREEN_COOP_SLOTS;
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
        if (screen == SCREEN_COOP_SLOTS && coopSlotMode == 3 && awaitingGuestCoopSave) {
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
        return applyTemporaryKeyMapping(keyP2.toArray(), temporarySecondaryKeyMapping);
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
        return kr.ac.jbnu.se.tetris.boss.brain.BossBrainAdvisor.select(
                useAEngine(), aEngineBridge, () -> random.nextInt(10),
                BossBrainRunner.Decision::garbageHole,
                session -> maxMinBrain.chooseGarbageHole(brainBoard(session), aEngineBridge.currentShape()));
    }

    @Override
    public Tetrominoes selectForcedPiece() {
        return kr.ac.jbnu.se.tetris.boss.brain.BossBrainAdvisor.select(
                useAEngine(), aEngineBridge, () -> Tetrominoes.values()[1 + random.nextInt(7)],
                BossBrainRunner.Decision::forcedPiece,
                session -> maxMinBrain.chooseForcedPiece(brainBoard(session)));
    }

    private Grid brainBoard(GameSession session) {
        Grid snapshot = aEngineBridge.brainSnapshot();
        return snapshot == null ? Grid.from(session) : snapshot;
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
        return (isOnlineCoop || !isTwoPlayer ? keySingle : keyP1).toArray();
    }

    @Override
    public int[] secondaryOriginalKeys() {
        return (isOnlineCoop ? keySingle : keyP2).toArray();
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
        hitFlash.start();
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
        bossAwakeningEffect = kr.ac.jbnu.se.tetris.effect.AwakeningEffect.forStage(stage).duration();
    }

    public void notifyEngineGameOver() {
        if (useCoopEngineRender() || (isOnlineCoop && coopEngineBridge != null)) {
            onCoopPlayerEliminated(localPlayerIndex >= 0 ? localPlayerIndex : 0);
            return;
        }
        if (screen == SCREEN_GAME && !stageClear) {
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
        if (endIfBothDead && coopEliminated0 && coopEliminated1 && screen == SCREEN_GAME && !stageClear) {
            gameEnd(false);
        }
        repaint();
    }

    public void onCoopPlayerEliminated(int playerIndex) {
        if (screen != SCREEN_GAME || stageClear) {
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
        screen = SCREEN_GAME;
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
        hitFlash.set(0);
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
            hitFlash.start();
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
        if (screen == SCREEN_RESULT) {
            return;
        }
        allClear = clear;
        screen = SCREEN_RESULT;
        pressed.clear();
        if (isOnlineCoop && coopBridge != null) {
            coopBridge.sendGameOver();
        }
        bossPattern.stop(this);
        soundManager.stopBgm();
        int total = p1Score + (isTwoPlayer ? p2Score : 0);
        if (loginUser != null) {
            repos.records().add(new RecordEntry(loginUser, total, stage, isTwoPlayer ? 2 : 1, clear, System.currentTimeMillis()));
            resultMsg = "랭킹에 기록했어요!";
            if (saveFile != null) {
                repos.saves().deleteSave(saveFile);
                saveFile = null;
            }
        } else {
            resultMsg = "게스트 기록은 랭킹에 남지 않아요.";
        }
    }

    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        MenuView view = views.get(screen);
        if (view != null) {
            view.key(e);
        } else if (screen == SCREEN_GAME) {
            handleGameKey(code);
        }
    }

    private void handleGameKey(int code) {
        if (code == KeyEvent.VK_ESCAPE || code == keyPause) {
            if (stageClear) return;
            if (isOnlineCoop && coopBridge != null) {
                performCoopAction(localPlayerIndex, PlayerAction.PAUSE, false);
            } else {
                enterLocalPause("");
            }
            return;
        }
        if (stageClear || pressed.contains(code)) {
            return;
        }
        pressed.add(code);
        handleSingleGameKey(code);
        handleTwoPlayerKey(code);
    }

    private void handleSingleGameKey(int code) {
        int[] k = currentPrimaryKeys();
        if (useAEngine()) {
            aEngineBridge.handleKey(code, k);
            resetRepeatCount(0, code, k);
        } else if (controlsPlayer(0)) {
            dispatchPlayerKey(0, code, k);
        }
    }

    private void handleTwoPlayerKey(int code) {
        if (!isTwoPlayer || screen != SCREEN_GAME || stageClear || !controlsPlayer(1)) return;
        dispatchPlayerKey(1, code, onlineCoopKeysForP2());
    }

    private static final PlayerAction[] KEY_ACTIONS = {
        PlayerAction.MOVE_LEFT, PlayerAction.MOVE_RIGHT, PlayerAction.SOFT_DROP, PlayerAction.HARD_DROP,
        PlayerAction.ROTATE_CW, PlayerAction.ROTATE_CCW, PlayerAction.HOLD
    };

    private void dispatchPlayerKey(int player, int code, int[] keys) {
        for (int i = 0; i < KEY_ACTIONS.length; i++) {
            if (code == keys[i]) {
                performCoopAction(player, KEY_ACTIONS[i], false);
                resetRepeatCount(player, code, keys);
                return;
            }
        }
    }

    /** 왼쪽/오른쪽/아래 키의 반복 입력 카운터를 0으로 되돌린다. */
    private void resetRepeatCount(int player, int code, int[] keys) {
        if (code == keys[KeyAction.LEFT.ordinal()]) {
            if (player == 0) {
                p1LeftCount = 0;
            } else {
                p2LeftCount = 0;
            }
        } else if (code == keys[KeyAction.RIGHT.ordinal()]) {
            if (player == 0) {
                p1RightCount = 0;
            } else {
                p2RightCount = 0;
            }
        } else if (code == keys[KeyAction.SOFT_DROP.ordinal()]) {
            if (player == 0) {
                p1DownCount = 0;
            } else {
                p2DownCount = 0;
            }
        }
    }

    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        pressed.remove(code);
        if (screen != SCREEN_GAME) return;
        resetRepeatCount(0, code, currentPrimaryKeys());
        if (!isTwoPlayer) return;
        resetRepeatCount(1, code, onlineCoopKeysForP2());
    }


    public void focusLost(FocusEvent e) {
        // 창 바뀌면 키 떼진걸 못받아서 계속 눌린걸로 됨
        pressed.clear();
    }

    boolean isMouseInside(int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    boolean clicked(int x, int y, int w, int h) {
        if (!isMouseInside(x, y, w, h)) {
            return false;
        }
        playButtonClickSound();
        return true;
    }

    public boolean clicked(Rectangle r) {
        return clicked(r.x, r.y, r.width, r.height);
    }

    public void mousePressed(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        requestFocusInWindow();
        MenuView view = views.get(screen);
        if (view != null) {
            view.click(mouseX, mouseY);
        } else if (screen == SCREEN_GAME && stageAdvancePrompt && isOnlineCoop) {
            if (!isStageReady(localPlayerIndex)
                    && clicked(STAGE_ADVANCE_BTN_X, STAGE_ADVANCE_BTN_Y, STAGE_ADVANCE_BTN_W, STAGE_ADVANCE_BTN_H)) {
                performCoopAction(localPlayerIndex, PlayerAction.STAGE_READY, false);
            }
        } else if (screen == SCREEN_COOP_SLOTS) {
            handleCoopSlotClick();
        }
    }

    public void reloadSaveList() {
        saveList = loginUser == null ? new ArrayList<>() : repos.saves().getSaveList(loginUser);
    }

    public ArrayList<String[]> saveList() {
        return saveList;
    }

    public java.util.List<RecordEntry> rankList() {
        return rankList;
    }

    public int rankTab() {
        return rankTab;
    }

    public void selectRankTab(int tab) {
        rankTab = tab;
        loadRanking();
    }

    public BufferedImage mapImage() {
        return mapImg;
    }

    public void showLoginDialog(boolean signUpMode) {
        new LoginFrame(this, repos.accounts(), signUpMode).setVisible(true);
    }

    /** 확인 버튼을 누르면 true, 취소하거나 창을 닫으면 false. */
    public boolean confirmOrCancel(String message, String title, String confirmLabel) {
        int choice = JOptionPane.showOptionDialog(this, message, title, JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, new String[]{confirmLabel, "취소"}, "취소");
        return choice == 0;
    }

    public void resumeSave(String savePath) {
        if (repos.saves().loadGame(savePath, this)) {
            screen = SCREEN_GAME;
            pressed.clear();
        } else {
            JOptionPane.showMessageDialog(this, "저장 파일을 읽을 수 없어요.");
        }
    }

    /** save 배열: {파일경로, 날짜, 스테이지, 점수, 인원}. */
    public void deleteSaveAfterConfirm(String[] save) {
        String message = "스테이지 " + save[2] + " · 점수 " + save[3] + "\n이 저장을 지울까요? 되돌릴 수 없어요.";
        if (confirmOrCancel(message, "저장 삭제", "삭제")) {
            repos.saves().deleteSave(save[0]);
            saveList = repos.saves().getSaveList(loginUser);
        }
    }

    public void showMsg(String text, int frames) {
        msg = text;
        msgTimer = frames;
    }

    public void leaveToTitle() {
        screen = SCREEN_TITLE;
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
            screen = SCREEN_TITLE;
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
                screen = SCREEN_PAUSE;
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
        screen = SCREEN_PAUSE;
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
        screen = SCREEN_PAUSE;
    }

    private void loadCoopSlot(int slot) {
        try {
            CoopGameMemento mem = SaveService.loadCoop(loginUser, slot);
            if (isOnlineCoop && coopBridge != null &&             coopBridge.isHost() && coopSlotReturnScreen == SCREEN_PAUSE) {
                resumeOnlineCoopAsHost(slot, mem);
            } else if (mem.onlineResume) {
                openLobbyForCoopResume(slot, mem);
            } else {
                resumeLocalCoop(slot, mem);
            }
        } catch (LoadException ex) {
            showMsg(ex.getMessage(), 180);
            if (coopSlotReturnScreen == SCREEN_PAUSE) {
                screen = SCREEN_PAUSE;
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
        screen = SCREEN_PAUSE;
        coopBridge.pushLoadedState(remapped);
        showMsg("슬롯 " + slot + " · 온라인 협동 저장을 불러왔어요.", 220);
    }

    private void openLobbyForCoopResume(int slot, CoopGameMemento mem) {
        pendingCoopResume = mem;
        showMsg("슬롯 " + slot + "을 불러왔어요. 로비에서 「게임 시작 (방장)」으로 이어하세요.", 260);
        screen = coopSlotReturnScreen;
        if (coopSlotReturnScreen == SCREEN_TITLE) {
            SwingUtilities.invokeLater(() -> {
                onlineLobbyFrame = new LobbyFrame(this);
                onlineLobbyFrame.setVisible(true);
            });
        }
    }

    private void resumeLocalCoop(int slot, CoopGameMemento mem) {
        isOnlineCoop = false;
        CoopEngineBridge.applyLoadedMemento(this, mem);
        if (coopSlotReturnScreen == SCREEN_PAUSE) {
            screen = SCREEN_PAUSE;
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

    public void openSettings() {
        settingsView.open();
    }

    public KeyBindingRepository keys() {
        return repos.keys();
    }

    public SaveRepository saves() {
        return repos.saves();
    }

    public void clearPressed() {
        pressed.clear();
    }

    public boolean isAwaitingGuestCoopSave() {
        return awaitingGuestCoopSave;
    }

    public boolean isAllClear() {
        return allClear;
    }

    public String resultMsg() {
        return resultMsg;
    }

    public void loadRanking() {
        rankList = RankingBoard.rank(repos.records().findAll(), rankTab == 1);
    }

    public static String keyName(int code) {
        if (code == KeyEvent.VK_LEFT) return "←";
        if (code == KeyEvent.VK_RIGHT) return "→";
        if (code == KeyEvent.VK_UP) return "↑";
        if (code == KeyEvent.VK_DOWN) return "↓";
        if (code == KeyEvent.VK_SLASH) return "/";
        if (code == KeyEvent.VK_PERIOD) return ".";
        if (code == KeyEvent.VK_COMMA) return ",";
        return KeyEvent.getKeyText(code);
    }

    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        MenuView view = views.get(screen);
        if (view != null) {
            view.paint(g);
            return;
        }
        switch (screen) {
            case GAME -> drawGame(g);
            case COOP_SLOTS -> {
                drawTitle(g);
                drawCoopSlots(g);
            }
            default -> {
            }
        }
    }

    /** 환경설정 화면 뒤에 깔리는 배경(타이틀 또는 게임). */
    public void drawSettingsBackdrop(Graphics2D g, ScreenId from) {
        if (from == SCREEN_TITLE) {
            drawTitle(g);
        } else {
            drawGame(g);
        }
    }

    void drawCoopSlots(Graphics2D g) {
        g.setColor(new Color(8, 6, 10, 190));
        g.fillRect(0, 0, 1280, 720);
        drawPanel(g, 300, 100, 680, 520);
        g.setFont(fontBold.deriveFont(30f));
        String title = coopSlotMode == 1 ? "협동 저장 · 슬롯 선택"
                : coopSlotMode == 3 ? "참가자 저장 · 슬롯 선택" : "협동 불러오기 · 슬롯 선택";
        centerText(g, title, 150, new Color(0x1a1216), new Color(0xefe0bf));
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

    public void drawCastle(Graphics2D g, boolean dark) {
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
        new PixelButton(text, new Rectangle(x, y, w, h)).draw(g, font, isMouseInside(x, y, w, h), type, size);
    }

    public void drawButton(Graphics2D g, PixelButton button, int type, float size) {
        button.draw(g, font, button.contains(mouseX, mouseY), type, size);
    }

    public boolean clicked(PixelButton button) {
        return clicked(button.bounds());
    }

    public void drawPanel(Graphics2D g, int x, int y, int w, int h) {
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

    public void shadowText(Graphics2D g, String text, int x, int y, Color c, Color shadow, int off) {
        g.setColor(shadow);
        g.drawString(text, x + off, y + off);
        g.setColor(c);
        g.drawString(text, x, y);
    }

    public void centerText(Graphics2D g, String text, int y, Color c, Color shadow) {
        int w = g.getFontMetrics().stringWidth(text);
        shadowText(g, text, SCREEN_CENTER_X - w / 2, y, c, shadow, 2);
    }

    public void centerPlainText(Graphics2D g, String text, int cx, int y, Color c) {
        int w = g.getFontMetrics().stringWidth(text);
        g.setColor(c);
        g.drawString(text, cx - w / 2, y);
    }

    private void drawTitle(Graphics2D g) {
        views.get(SCREEN_TITLE).paint(g);
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

    void drawCellGray(Graphics2D g, int x, int y) {
        Color c = new Color(0x5c5c66);
        g.setColor(c.darker());
        g.fillRect(x, y, CELL_SIZE, CELL_SIZE);
        g.setColor(c);
        g.fillRect(x + 2, y + 2, CELL_SIZE - 4, CELL_SIZE - 4);
        g.setColor(new Color(0x787882));
        g.fillRect(x + 2, y + 2, CELL_SIZE - 4, 3);
        g.fillRect(x + 2, y + 2, 3, CELL_SIZE - 4);
    }

    void drawGhostCell(Graphics2D g, int x, int y, int v) {
        Color c = blockColor(v);
        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 36));
        g.fillRect(x + 2, y + 2, CELL_SIZE - 4, CELL_SIZE - 4);
        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 72));
        g.drawRect(x + 2, y + 2, CELL_SIZE - 5, CELL_SIZE - 5);
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
                    drawGhostCell(g, bx + cx * 30, by + (cy - 4) * 30, blockValue(type));
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
                    drawCellGray(g, x, y);
                } else {
                    drawCell(g, x, y, 30, engineBlockValue(shape));
                }
            }
        }

        if (gray || screen == SCREEN_RESULT || stageClear || session.curPiece.getShape() == Tetrominoes.NoShape) {
            return;
        }
        int blockVal = engineBlockValue(session.curPiece.getShape());
        int ghostY = session.ghostLandingY();
        if (ghostY != session.curY) {
            for (int i = 0; i < session.curPiece.blockCount(); i++) {
                int x = session.curX + session.curPiece.x(i);
                int y = ghostY - session.curPiece.y(i);
                if (x >= 0 && x < 10 && y >= 0 && y < 20) {
                    drawGhostCell(g, boardX + x * 30, boardY + (19 - y) * 30, blockVal);
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
        if (screen == SCREEN_RESULT || stageClear) {
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
        kr.ac.jbnu.se.tetris.effect.AwakeningEffect.forStage(stage).draw(g, bossAwakeningEffect, bossX);
    }

    int[] bossDrawSize(BufferedImage sprite) {
        int sw = sprite.getWidth();
        int sh = sprite.getHeight();
        double scale = Math.min(1.0, Math.min((double) BOSS_MAX_DRAW_WIDTH / sw, (double) BOSS_MAX_DRAW_HEIGHT / sh));
        int dw = Math.max(1, (int) Math.round(sw * scale));
        int dh = Math.max(1, (int) Math.round(sh * scale));
        return new int[]{dw, dh};
    }

    /** Draws boss PNG with optional hit flash limited to non-transparent pixels. */
    void drawBossMonster(Graphics2D g, BufferedImage sprite, int x, int y, int dw, int dh, boolean flashWhite) {
        g.drawImage(sprite, x, y, dw, dh, null);
        if (!flashWhite) {
            return;
        }
        Composite previous = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN, 0.62f));
        g.setColor(Color.WHITE);
        g.fillRect(x, y, dw, dh);
        g.setComposite(previous);
    }

    public void drawGame(Graphics2D g) {
        kr.ac.jbnu.se.tetris.render.GameView view = new kr.ac.jbnu.se.tetris.render.GameView();
        java.util.List<kr.ac.jbnu.se.tetris.render.GameLayer> layers = java.util.List.of(
            new kr.ac.jbnu.se.tetris.render.BackdropRenderer(graphics -> drawGameBackdrop(graphics, view)),
            new kr.ac.jbnu.se.tetris.render.BoardRenderer(this::drawGameBoards),
            new kr.ac.jbnu.se.tetris.render.HudRenderer(graphics -> drawGameHud(graphics, view)),
            new kr.ac.jbnu.se.tetris.render.BannerRenderer(graphics -> drawGameBanners(graphics, view)));
        for (kr.ac.jbnu.se.tetris.render.GameLayer layer : layers) layer.draw(g);
    }

    private void drawGameBackdrop(Graphics2D g, kr.ac.jbnu.se.tetris.render.GameView view) {
        BufferedImage bg = Art.background(stage);
        g.drawImage(bg, 0, 0, 1280, 720, null);
        Art.MonsterPose bossPose = stageClear ? Art.MonsterPose.DEATH
                : (hitFlash.get() > 0 ? Art.MonsterPose.HIT : Art.MonsterPose.IDLE);
        BufferedImage m = Art.monster(stage, bossPose);
        int[] drawSize = bossDrawSize(m);
        int drawW = drawSize[0];
        int drawH = drawSize[1];
        int bob = bossPose == Art.MonsterPose.IDLE && (tick / 36) % 2 == 0 ? 0 : 3;
        if (bossPose != Art.MonsterPose.IDLE) {
            bob = 0;
        }
        int mx = isTwoPlayer ? 640 - drawW / 2 : 1100 - drawW / 2;
        int my = 704 - drawH + bob;
        drawBossMonster(g, m, mx, my, drawW, drawH, !stageClear && hitFlash.isVisible());
        view.bossX = mx;
        view.bossY = my;
        view.bossWidth = drawW;
        view.spriteWidth = m.getWidth();
        Art.effect(g, stage, 1280, 720, gameTime);
        if (gameTime < blizzardEnd) {
            g.setColor(new Color(200, 230, 255, 50));
            g.fillRect(0, 0, 1280, 720);
        }

    }

    private void drawGameBoards(Graphics2D g) {
        // 1p 보드
        int bx = isTwoPlayer ? 190 : 490;
        int by = 104;
        drawBoardFrame(g, bx, by);
        if (useCoopEngineRender()) {
            drawEngineSessionBoard(g, coopEngineBridge.session(0), bx, by,
                    coopEngineBridge.isLineClearBlinkOn(0), 0);
        } else if (!useAEngine()) {
            boolean gray0 = isCoopPlayerEliminated(0);
            drawLegacyCells(g, board1, bx, by, gray0);
            if (!gray0 && screen != SCREEN_RESULT && !stageClear) {
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
        drawScorePanel(g, isTwoPlayer ? 40 : 336, isTwoPlayer ? 524 : 530, p1Score, p1Lines, onlineCoopPlayerLabel(0));

        // 2p도 똑같이
        if (isTwoPlayer) {
            bx = 790;
            drawBoardFrame(g, bx, by);
            if (useCoopEngineRender()) {
                drawEngineSessionBoard(g, coopEngineBridge.session(1), bx, by,
                        coopEngineBridge.isLineClearBlinkOn(1), 1);
            } else {
                boolean gray1 = isCoopPlayerEliminated(1);
                drawLegacyCells(g, board2, bx, by, gray1);
                if (!gray1 && screen != SCREEN_RESULT && !stageClear) {
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
            drawScorePanel(g, 1108, 524, p2Score, p2Lines, onlineCoopPlayerLabel(1));

            g.setFont(fontBold.deriveFont(22f));
            if (isOnlineCoop) {
                shadowText(g, "P1 · 방장", 56, 90, new Color(0x7fc8ff), new Color(0x0c090d), 2);
                shadowText(g, "P2 · 참가", 1108, 90, new Color(0xffa07a), new Color(0x0c090d), 2);
            } else {
                shadowText(g, "P1", 56, 90, new Color(0x7fc8ff), new Color(0x0c090d), 2);
                shadowText(g, "P2", 1108, 90, new Color(0xffa07a), new Color(0x0c090d), 2);
            }
        }

    }

    private void drawGameHud(Graphics2D g, kr.ac.jbnu.se.tetris.render.GameView view) {
        if (bossAwakeningEffect > 0) {
            drawBossAwakening(g, view.bossX + view.bossWidth / 2);
        }

        // 보스 체력
        String[] stageNames = {"", "불타는 사막", "고립된 섬", "고요한 초원", "얼어붙은 설원", "하늘 위 마왕성"};
        String[] bossNames = {"", "분노한 선인장 괴물", "심연의 해신", "천년 거목", "설원의 포효자", "타락한 악의 신"};
        drawBossHealthBar(g, stageNames, bossNames);
    }

    private void drawGameBanners(Graphics2D g, kr.ac.jbnu.se.tetris.render.GameView view) {
        String[] stageNames = {"", "불타는 사막", "고립된 섬", "고요한 초원", "얼어붙은 설원", "하늘 위 마왕성"};
        String[] bossNames = {"", "분노한 선인장 괴물", "심연의 해신", "천년 거목", "설원의 포효자", "타락한 악의 신"};
        drawBossWarningBanner(g);
        drawMessageBanner(g);
        drawRandomKeyGuide(g, view.bossX + view.spriteWidth / 2, view.bossY);
        if (stageAdvancePrompt && isOnlineCoop) {
            drawStageAdvancePrompt(g, stageNames, bossNames);
        } else if (stageClear) {
            drawStageClearBanner(g, bossNames[stage]);
        }
        drawPauseHelp(g);
    }

    private void drawBoardFrame(Graphics2D g, int bx, int by) {
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
    }

    private void drawLegacyCells(Graphics2D g, int[][] board, int bx, int by, boolean gray) {
        for (int r = 4; r < 24; r++) {
            for (int c = 0; c < 10; c++) {
                if (board[r][c] != 0) {
                    int x = bx + c * 30;
                    int y = by + (r - 4) * 30;
                    if (gray) {
                        drawCellGray(g, x, y);
                    } else {
                        drawCell(g, x, y, 30, board[r][c]);
                    }
                }
            }
        }
    }

    private void drawScorePanel(Graphics2D g, int x, int y, int score, int lines, String playerLabel) {
        g.setFont(fontSmall.deriveFont(13.5f));
        g.setColor(new Color(0x9a8f9f));
        g.drawString("점수", x + 14, y + 30);
        g.drawString("지운 줄", x + 14, y + 84);
        g.drawString("플레이어", x + 14, y + 136);
        g.setFont(font.deriveFont(22f));
        g.setColor(new Color(0xf0e2c8));
        g.drawString(String.format("%,d", score), x + 14, y + 58);
        g.drawString("" + lines, x + 14, y + 112);
        g.setFont(font.deriveFont(16.5f));
        g.setColor(new Color(0xe9c98f));
        g.drawString(playerLabel, x + 14, y + 158);
    }

    private void drawBossHealthBar(Graphics2D g, String[] stageNames, String[] bossNames) {
        int barX = 380 + hitFlash.shakeOffset();
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
        boolean blueprintOk = customVictory && violations == 0;
        g.setColor(blueprintOk ? new Color(0x4fa65a) : new Color(0xc9432f));
        g.fillRect(barX + 19, 53, filled, 18);
        g.setColor(new Color(255, 255, 255, 60));
        g.fillRect(barX + 19, 53, filled, 4);
        g.setColor(new Color(0, 0, 0, 90));
        for (int i = 1; i < 10; i++) {
            g.fillRect(barX + 19 + 482 * i / 10, 53, 2, 18);
        }
        g.setFont(font.deriveFont(13.5f));
        g.setColor(new Color(0xf0e2c8));
        String hpText = customVictory
                ? "BLUEPRINT " + Math.round(progress * 100) + "%" + (violations > 0 ? "  MISPLACED " + violations : "")
                : bossHp + " / " + bossMax;
        g.drawString(hpText, barX + 260 - g.getFontMetrics().stringWidth(hpText) / 2, 67);
    }

    private void drawBossWarningBanner(Graphics2D g) {
        if (!(bossWarning && screen == SCREEN_GAME && (tick / 10) % 2 == 0)) {
            return;
        }
        String warn = bossWarningText.isEmpty() ? "!! 보스 공격 준비 중 !!" : bossWarningText;
        g.setFont(fontBold.deriveFont(22f));
        int w = g.getFontMetrics().stringWidth(warn);
        g.setColor(new Color(120, 20, 20, 200));
        g.fillRect(640 - w / 2 - 14, 96, w + 28, 36);
        g.setColor(new Color(0xfff4e6));
        g.drawString(warn, 640 - w / 2, 122);
    }

    private void drawMessageBanner(Graphics2D g) {
        if (!(msgTimer > 0 && screen == SCREEN_GAME)) {
            return;
        }
        g.setFont(font.deriveFont(16.5f));
        int w = g.getFontMetrics().stringWidth(msg);
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(640 - w / 2 - 12, 140, w + 24, 30);
        g.setColor(new Color(0xe9c98f));
        g.drawString(msg, 640 - w / 2, 161);
    }

    private void drawStageClearBanner(Graphics2D g, String bossName) {
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRect(0, 280, 1280, 140);
        g.setFont(fontBold.deriveFont(66f));
        centerText(g, "GameClear!", 350, new Color(0xe9c98f), new Color(0x6b2a1a));
        g.setFont(font.deriveFont(22f));
        centerText(g, bossName + "을(를) 쓰러뜨렸다!", 390, new Color(0xf0e2c8), new Color(0x0c090d));
    }

    private void drawPauseHelp(Graphics2D g) {
        String help = keyName(keyPause) + " / Esc 일시정지";
        g.setFont(fontSmall.deriveFont(13.5f));
        int hw = g.getFontMetrics().stringWidth(help);
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(8, 696, hw + 20, 20);
        g.setColor(new Color(0xd9c29a));
        g.drawString(help, 18, 711);
    }

    void drawRandomKeyGuide(Graphics2D g, int bossCenterX, int bossTopY) {
        if (stage != 5 || screen != SCREEN_GAME || stageClear
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
        int[] legacyOriginal = (playerIndex == 0 ? keyP1 : keyP2).toArray();
        return RandomKeyMappingGenerator.toPermutation(legacyOriginal, legacyKeys);
    }

    public void promptTwoPlayerMode() {
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
        screen = SCREEN_GAME;
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
            returnToOnlineLobby(text == null ? "상대와의 방이 종료되었어요." : text);
            return;
        }
        msg = text == null ? "상대 연결이 끊어졌어요." : text;
        msgTimer = 300;
        if (screen == SCREEN_GAME && !stageClear) {
            screen = SCREEN_PAUSE;
            soundManager.pauseBgm();
        }
    }

    void enterLocalPause(String pauseMsg) {
        screen = SCREEN_PAUSE;
        msg = pauseMsg == null ? "" : pauseMsg;
        msgTimer = msg.isEmpty() ? 0 : 300;
        pressed.clear();
        if (useAEngine()) {
            aEngineBridge.pauseEngine();
        }
        soundManager.pauseBgm();
    }

    public void resumeLocalPause() {
        screen = SCREEN_GAME;
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

    public void performCoopAction(int playerIndex, PlayerAction action, boolean fromNetwork) {
        int reportedLines = reportedLineCount(action);
        if (reportedLines > 0) {
            if (fromNetwork && useCoopEngineHost() && playerIndex == 1 && coopEngineBridge != null) {
                coopEngineBridge.creditGuestLines(reportedLines);
            }
            return;
        }
        if (action == PlayerAction.STAGE_READY) {
            if (screen != SCREEN_GAME || !stageAdvancePrompt || isStageReady(playerIndex)) {
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
            if (screen == SCREEN_GAME && !stageClear) {
                String pauseMsg = fromNetwork ? "상대방이 일시정지했어요." : "";
                enterLocalPause(pauseMsg);
            }
            if (!fromNetwork && isOnlineCoop && coopBridge != null) {
                coopBridge.sendLocalAction(action);
            }
            return;
        }
        if (action == PlayerAction.RESUME) {
            if (screen == SCREEN_PAUSE && isOnlineCoop && awaitingGuestCoopSave && !fromNetwork
                    && coopBridge != null && !coopBridge.isHost()) {
                msg = "참가자 저장을 먼저 완료해 주세요.";
                msgTimer = 200;
                return;
            }
            if (screen == SCREEN_PAUSE) {
                resumeLocalPause();
            }
            if (!fromNetwork && isOnlineCoop && coopBridge != null) {
                coopBridge.sendLocalAction(action);
            }
            return;
        }
        if (screen != SCREEN_GAME || stageClear) {
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

    public ScreenId getScreen() {
        return screen;
    }

    public void setScreen(ScreenId screen) {
        this.screen = screen;
    }

    public String getLoginUser() {
        return loginUser;
    }

    public void setLoginUser(String loginUser) {
        this.loginUser = loginUser;
    }

    public int getHitEffect() {
        return hitFlash.get();
    }

    public void setHitEffect(int hitEffect) {
        hitFlash.set(hitEffect);
    }

    /** 실제로 쓰는 키 이벤트만 GamePanel로 전달 */
    private class KeyHandler extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) { GamePanel.this.keyPressed(e); }

        @Override
        public void keyReleased(KeyEvent e) { GamePanel.this.keyReleased(e); }
    }

    /** 클릭 + 마우스 위치(버튼 hover)만 전달 */
    private class MouseHandler extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) { GamePanel.this.mousePressed(e); }

        @Override
        public void mouseMoved(MouseEvent e) { GamePanel.this.mouseMoved(e); }
    }

    /** 창 전환 시 눌린 키 초기화만 전달 */
    private class FocusHandler extends FocusAdapter {
        @Override
        public void focusLost(FocusEvent e) { GamePanel.this.focusLost(e); }
    }
}
