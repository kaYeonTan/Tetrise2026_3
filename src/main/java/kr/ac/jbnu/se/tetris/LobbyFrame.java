package kr.ac.jbnu.se.tetris;



import java.awt.BorderLayout;

import java.awt.Color;

import java.awt.Dimension;

import java.awt.FlowLayout;

import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;

import javax.swing.Box;

import javax.swing.BoxLayout;

import javax.swing.DefaultListModel;

import javax.swing.JButton;
import javax.swing.JComboBox;

import javax.swing.JDialog;

import javax.swing.JLabel;

import javax.swing.JList;

import javax.swing.JOptionPane;

import javax.swing.JPanel;

import javax.swing.JScrollPane;

import javax.swing.JTextArea;

import javax.swing.JTextField;

import javax.swing.ListSelectionModel;

import javax.swing.SwingUtilities;

import javax.swing.Timer;



import com.google.gson.Gson;

import kr.ac.jbnu.se.tetris.network.coop.CoopNetworkBridge;
import kr.ac.jbnu.se.tetris.network.lobby.LobbyClient;

import kr.ac.jbnu.se.tetris.network.lobby.LobbyConfig;

import kr.ac.jbnu.se.tetris.network.lobby.LobbyMessage;

import kr.ac.jbnu.se.tetris.network.lobby.LobbyMessageType;

import kr.ac.jbnu.se.tetris.network.lobby.RoomSnapshot;

import kr.ac.jbnu.se.tetris.network.lobby.RoomVisibility;



/**

 * 온라인 협동 로비 (MVP). 실제 in-game 동기화는 이후 세션.

 */

public class LobbyFrame extends JDialog {



	private static final long serialVersionUID = 1L;

	private static final Dimension DIALOG_SIZE = new Dimension(720, 640);



	private final GamePanel panel;

	private LobbyClient client;

	private final DefaultListModel<String> roomModel = new DefaultListModel<>();

	private List<RoomSnapshot> lastRooms = List.of();

	private Timer refreshTimer;



	private JLabel statusLabel;

	private JLabel waitingLabel;

	private JTextField hostField;

	private JTextField portField;

	private JTextField roomNameField;
	private JComboBox<String> roomVisibilitySelector;
	private JList<String> roomList;

	private JButton connectBtn;

	private JTextArea myRoomPanel;
	/** 방장 비밀방 코드 — MY_ROOM 응답에 없을 때 화면 유지용 */
	private String rememberedJoinCode;
	private String rememberedRoomId;
	private JButton startGameBtn;
	private boolean retainConnectionOnClose;



	@SuppressWarnings("this-escape")

	public LobbyFrame(GamePanel panel) {

		super(SwingUtilities.getWindowAncestor(panel), "온라인 2인 플레이 로비", ModalityType.APPLICATION_MODAL);

		this.panel = panel;

		setMinimumSize(DIALOG_SIZE);

		buildUi();

		clearMyRoomPanel();

		setSize(DIALOG_SIZE);

		setLocationRelativeTo(panel);

	}



	private Font uiFont() {

		return panel.font.deriveFont(15f);

	}



	private void buildUi() {

		JPanel root = new JPanel(new BorderLayout(8, 8));

		root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

		root.setBackground(new Color(0xefe0bf));



		JPanel top = new JPanel();

		top.setOpaque(false);

		top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));



		JPanel connectRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));

		connectRow.setOpaque(false);

		connectBtn = button("서버 연결", true);

		connectBtn.setPreferredSize(new Dimension(132, 42));

		connectBtn.setMinimumSize(new Dimension(132, 42));

		connectBtn.addActionListener(e -> connect());

		connectRow.add(connectBtn);



		JLabel addrLabel = new JLabel("주소");

		addrLabel.setFont(uiFont());

		connectRow.add(addrLabel);

		hostField = compactField("127.0.0.1", 110);

		connectRow.add(hostField);



		JLabel portLabel = new JLabel("포트");

		portLabel.setFont(uiFont());

		connectRow.add(portLabel);

		portField = compactField("" + LobbyConfig.DEFAULT_PORT, 56);

		connectRow.add(portField);

		JButton refreshBtn = button("목록 새로고침", false);
		refreshBtn.addActionListener(e -> refreshRooms());
		connectRow.add(refreshBtn);



		top.add(connectRow);



		JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));

		statusRow.setOpaque(false);

		statusLabel = new JLabel("연결되지 않음 — 먼저 「서버 연결」을 눌러 주세요.");

		statusLabel.setFont(uiFont().deriveFont(14f));

		statusLabel.setForeground(new Color(0x4a3a2a));

		statusRow.add(statusLabel);

		top.add(statusRow);



		JPanel center = new JPanel(new BorderLayout(8, 0));

		center.setOpaque(false);

		roomList = new JList<>(roomModel);

		roomList.setFont(uiFont());

		roomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		roomList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && !e.isConsumed()) {
					joinSelected();
				}
			}
		});

		JScrollPane listScroll = new JScrollPane(roomList);

		listScroll.setPreferredSize(new Dimension(300, 360));

		center.add(listScroll, BorderLayout.CENTER);



		JPanel right = new JPanel(new BorderLayout(0, 8));

		right.setOpaque(false);

		right.setPreferredSize(new Dimension(280, 360));



		myRoomPanel = new JTextArea(7, 20);

		myRoomPanel.setFont(uiFont());

		myRoomPanel.setEditable(false);

		myRoomPanel.setLineWrap(true);

		myRoomPanel.setWrapStyleWord(true);

		myRoomPanel.setBackground(new Color(0xf5ead0));

		myRoomPanel.setForeground(new Color(0x1a1216));

		myRoomPanel.setBorder(BorderFactory.createCompoundBorder(

				BorderFactory.createLineBorder(new Color(0x1a1216), 2),

				BorderFactory.createEmptyBorder(6, 8, 6, 8)));

		JScrollPane myRoomScroll = new JScrollPane(myRoomPanel);
		myRoomScroll.setBorder(BorderFactory.createEmptyBorder());

		JPanel myRoomBox = new JPanel(new BorderLayout(0, 6));
		myRoomBox.setOpaque(false);
		myRoomBox.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(new Color(0x8a6a2a)), "내 대기실"));
		myRoomBox.add(myRoomScroll, BorderLayout.CENTER);
		startGameBtn = actionButton("게임 시작 (방장)", true, this::startCoopGame);
		startGameBtn.setEnabled(false);
		myRoomBox.add(startGameBtn, BorderLayout.SOUTH);
		myRoomBox.setPreferredSize(new Dimension(260, 200));
		right.add(myRoomBox, BorderLayout.NORTH);

		JPanel actions = new JPanel();
		actions.setOpaque(false);
		actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));
		actions.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(new Color(0x8a6a2a)), "새 방 만들기"));

		JLabel roomNameLabel = new JLabel("새 방 이름");
		roomNameLabel.setFont(uiFont());
		roomNameLabel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
		actions.add(roomNameLabel);
		actions.add(Box.createVerticalStrut(4));

		roomNameField = new JTextField("협동 모험");
		roomNameField.setFont(uiFont());
		Dimension nameSize = new Dimension(260, 32);
		roomNameField.setPreferredSize(nameSize);
		roomNameField.setMaximumSize(nameSize);
		roomNameField.setAlignmentX(JPanel.LEFT_ALIGNMENT);
		actions.add(roomNameField);
		actions.add(Box.createVerticalStrut(8));

		roomVisibilitySelector = new JComboBox<>(new String[]{"공개 방", "비밀 방"});
		roomVisibilitySelector.setFont(uiFont());
		roomVisibilitySelector.setMaximumSize(new Dimension(260, 32));
		roomVisibilitySelector.setAlignmentX(JPanel.LEFT_ALIGNMENT);
		actions.add(roomVisibilitySelector);
		actions.add(Box.createVerticalStrut(6));

		JButton loadBtn = actionButton("협동 저장 불러오기", false, this::openCoopSaveSlots);
		actions.add(loadBtn);
		actions.add(Box.createVerticalStrut(6));
		actions.add(actionButton("방 만들기", true, this::createRoom));
		actions.add(Box.createVerticalStrut(6));
		actions.add(actionButton("방 나가기", false, this::leaveRoom));

		right.add(actions, BorderLayout.CENTER);



		center.add(right, BorderLayout.EAST);



		waitingLabel = new JLabel(" ");

		waitingLabel.setFont(uiFont().deriveFont(15f));

		waitingLabel.setForeground(new Color(0x1a1216));



		JPanel bottom = new JPanel(new BorderLayout());

		bottom.setOpaque(false);

		bottom.add(waitingLabel, BorderLayout.CENTER);

		JButton closeBtn = button("닫기", false);

		closeBtn.addActionListener(e -> dispose());

		bottom.add(closeBtn, BorderLayout.EAST);



		root.add(top, BorderLayout.NORTH);

		root.add(center, BorderLayout.CENTER);

		root.add(bottom, BorderLayout.SOUTH);

		setContentPane(root);



		portField.addActionListener(e -> connect());

		hostField.addActionListener(e -> connect());

	}



	private void clearMyRoomPanel() {

		myRoomPanel.setText("아직 방에 없어요.\n새 방을 만들면\n여기에 대기실 정보가 표시돼요.");

		waitingLabel.setText("로비");

		rememberedJoinCode = null;

		rememberedRoomId = null;

		if (startGameBtn != null) {
			startGameBtn.setEnabled(false);
			startGameBtn.setText("게임 시작 (방장)");
		}

	}



	private void applyMyRoomFromServer(LobbyMessage resp) {

		if (resp == null || resp.players == null) {

			clearMyRoomPanel();

			return;

		}

		String myNick = client != null ? client.getNick() : displayNick();

		String hostNick = resp.players.isEmpty() ? myNick : resp.players.get(0);

		boolean host = "HOST".equals(resp.role);

		if (resp.joinCode != null && !resp.joinCode.isEmpty()) {

			rememberedJoinCode = resp.joinCode;

			rememberedRoomId = resp.roomId;

		}

		boolean isPrivate = RoomVisibility.PRIVATE.name().equals(resp.visibility)

				|| (host && rememberedJoinCode != null && resp.roomId != null

						&& resp.roomId.equals(rememberedRoomId));

		String joinCode = resp.joinCode;

		if (host && (joinCode == null || joinCode.isEmpty()) && resp.roomId != null

				&& resp.roomId.equals(rememberedRoomId)) {

			joinCode = rememberedJoinCode;

		}

		StringBuilder sb = new StringBuilder();

		sb.append("방: ").append(resp.roomName).append('\n');

		if (isPrivate) {

			sb.append("종류: 비밀방\n");

		} else {

			sb.append("종류: 공개방\n");

		}

		sb.append("───\n");

		if (host) {

			sb.append(myNick);

			if ("Guest".equalsIgnoreCase(myNick)) {

				sb.append(" (게스트)");

			}

			sb.append(" (방장)\n");

		} else {

			sb.append(hostNick).append(" (방장)\n");

			sb.append(myNick);

			if ("Guest".equalsIgnoreCase(myNick)) {

				sb.append(" (게스트)");

			}

			sb.append(" (참가)\n");

		}

		if (host && isPrivate) {

			sb.append("───\n");

			if (joinCode != null && !joinCode.isEmpty()) {

				sb.append("입장 코드 (비밀번호):\n").append(joinCode).append('\n');

				sb.append("(친구에게 알려 주세요)\n");

			} else {

				sb.append("입장 코드를 불러오지 못했어요.\n방 나가기 후 다시 만들어 주세요.\n");

			}

		}

		sb.append("───\n");

		sb.append(resp.players.size()).append(" / 2명");

		if (resp.players.size() < 2) {

			sb.append(" · 상대 대기 중");

		} else {

			sb.append(" · 준비 완료");

		}

		myRoomPanel.setText(sb.toString());

		waitingLabel.setText("대기 중 · " + resp.roomName + " · " + resp.players.size() + "/2");
		boolean isHostRole = "HOST".equals(resp.role);
		boolean full = resp.players.size() >= 2;
		if (startGameBtn != null) {
			startGameBtn.setEnabled(isHostRole && full && client != null && client.isConnected());
			if (isHostRole && panel.pendingCoopResume != null) {
				startGameBtn.setText("저장 이어하기 (방장)");
			} else {
				startGameBtn.setText(isHostRole ? "게임 시작 (방장)" : "방장이 시작할 때까지 대기");
			}
		}

	}



	private void syncMyRoom() {

		if (client == null || !client.isConnected()) {

			return;

		}

		try {

			LobbyMessage mine = client.fetchMyRoom();

			if (mine == null) {

				clearMyRoomPanel();

			} else {

				applyMyRoomFromServer(mine);

			}

		} catch (Exception ignored) {

			// 목록만 실패해도 대기실 텍스트는 유지

		}

	}



	private JTextField compactField(String text, int widthPx) {

		JTextField f = new JTextField(text);

		f.setFont(uiFont());

		Dimension d = new Dimension(widthPx, 30);

		f.setPreferredSize(d);

		f.setMinimumSize(d);

		f.setMaximumSize(new Dimension(widthPx, 30));

		return f;

	}



	private JButton button(String text, boolean primary) {

		JButton b = new JButton(text);
		b.addActionListener(e -> panel.playButtonClickSound());

		b.setFont(uiFont());

		b.setFocusPainted(false);

		b.setOpaque(true);

		b.setBorderPainted(true);

		if (primary) {

			b.setBackground(new Color(0xa8352a));

			b.setForeground(new Color(0xfff4e6));

		} else {

			b.setBackground(new Color(0xd9c29a));

			b.setForeground(new Color(0x1a1216));

		}

		return b;

	}

	private JButton actionButton(String text, boolean primary, Runnable action) {

		JButton b = button(text, primary);

		b.setFont(uiFont().deriveFont(15.5f));

		Dimension size = new Dimension(260, 40);

		b.setPreferredSize(size);

		b.setMinimumSize(size);

		b.setMaximumSize(size);

		b.setAlignmentX(JPanel.LEFT_ALIGNMENT);

		b.addActionListener(e -> action.run());

		return b;

	}



	private String displayNick() {

		if (panel.loginUser != null && !panel.loginUser.isEmpty()) {

			return panel.loginUser;

		}

		return "Guest";

	}



	private void normalizeHostPortFields() {

		String raw = hostField.getText().trim();

		if (raw.contains(":")) {

			int colon = raw.indexOf(':');

			String hostPart = raw.substring(0, colon).trim();

			String portPart = raw.substring(colon + 1).trim().replaceAll("\\s", "");

			if (!hostPart.isEmpty()) {

				hostField.setText(hostPart);

			}

			if (!portPart.isEmpty()) {

				portField.setText(portPart);

			}

		}

	}



	private void connect() {

		normalizeHostPortFields();

		if (panel.coopBridge != null) {
			panel.coopBridge.detach();
			panel.coopBridge = null;
		}
		disconnectClient();

		String host = hostField.getText().trim();

		if (host.isEmpty()) {

			host = "127.0.0.1";

			hostField.setText(host);

		}

		int port;

		try {

			port = Integer.parseInt(portField.getText().trim());

		} catch (NumberFormatException e) {

			JOptionPane.showMessageDialog(this, "포트 번호를 확인해 주세요.");

			return;

		}

		connectBtn.setEnabled(false);

		try {

			client = new LobbyClient();

			client.connect(host, port, displayNick());
			CoopNetworkBridge bridge = new CoopNetworkBridge(panel);
			bridge.attach(client);
			panel.coopBridge = bridge;
			panel.onlineLobbyClient = client;

			statusLabel.setText("연결됨 · " + client.getNick() + " · 방 만들기/입장 가능");

			statusLabel.setForeground(new Color(0x2a5a2a));

			startAutoRefresh();

			refreshRooms();

		} catch (Exception ex) {

			client = null;

			statusLabel.setText("연결 실패 — LobbyServerMain 실행 여부 확인");

			statusLabel.setForeground(new Color(0xa8352a));

			String hint = ex.getMessage() == null ? "" : ex.getMessage();

			JOptionPane.showMessageDialog(this,

					"서버에 연결하지 못했어요.\n\n"

							+ "1) IntelliJ에서 LobbyServerMain 을 먼저 실행했는지\n"

							+ "2) 주소 " + host + "  포트 " + port + " 가 맞는지\n\n"

							+ hint);

		} finally {

			connectBtn.setEnabled(true);

		}

	}



	private void startAutoRefresh() {

		if (refreshTimer != null) {

			refreshTimer.stop();

		}

		refreshTimer = new Timer(2500, e -> refreshRooms());

		refreshTimer.start();

	}



	private void refreshRooms() {

		if (client == null || !client.isConnected()) {

			return;

		}

		try {

			lastRooms = client.listRooms();

			roomModel.clear();

			for (RoomSnapshot r : lastRooms) {

				String lock = r.visibility == RoomVisibility.PRIVATE ? "[비밀]" : "[공개]";

				String state = r.joinable ? "입장 가능" : "만원";

				roomModel.addElement(String.format("%s %s · %s · %d/%d · %s",

						lock, r.name, r.hostNick, r.playerCount, r.maxPlayers, state));

			}

			if (lastRooms.isEmpty()) {

				roomModel.addElement("(열린 방 없음 — 방을 만들거나 새로고침)");

			}

			syncMyRoom();

		} catch (Exception ex) {

			statusLabel.setText("목록 갱신 실패 — 다시 「서버 연결」");

			statusLabel.setForeground(new Color(0xa8352a));

		}

	}



	private boolean ensureConnected() {

		if (client != null && client.isConnected()) {

			return true;

		}

		JOptionPane.showMessageDialog(this,

				"서버에 연결되어 있지 않아요.\n왼쪽 위 「서버 연결」 버튼을 눌러 주세요.\n(LobbyServerMain 이 켜져 있어야 합니다.)");

		return false;

	}



	private void createRoom() {

		if (!ensureConnected()) {

			return;

		}

		String name = roomNameField.getText().trim();
		String visibilityOption = (String) roomVisibilitySelector.getSelectedItem();
		if (visibilityOption == null) {
			JOptionPane.showMessageDialog(this, "방 공개 여부를 선택해 주세요.");
			return;
		}
		RoomVisibility visibility = "비밀 방".equals(visibilityOption)
				? RoomVisibility.PRIVATE : RoomVisibility.PUBLIC;

		try {

			LobbyMessage resp = client.createRoom(name, visibility);

			if (LobbyMessageType.ERROR.name().equals(resp.type)) {

				JOptionPane.showMessageDialog(this, resp.message);

				return;

			}

			applyMyRoomFromServer(resp);

			refreshRooms();

		} catch (Exception ex) {

			JOptionPane.showMessageDialog(this, ex.getMessage());

		}

	}



	private void joinSelected() {

		if (!ensureConnected()) {

			return;

		}

		int idx = roomList.getSelectedIndex();

		if (idx < 0 || idx >= lastRooms.size()) {

			JOptionPane.showMessageDialog(this, "목록에서 방을 선택해 주세요.");

			return;

		}

		RoomSnapshot room = lastRooms.get(idx);

		if (!room.joinable) {

			JOptionPane.showMessageDialog(this, "이 방은 이미 만원이에요.");

			return;

		}

		int confirm = JOptionPane.showConfirmDialog(this,
				"\"" + room.name + "\" 방에 입장하시겠습니까?",
				"방 입장", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (confirm != JOptionPane.YES_OPTION) {
			return;
		}

		String code = null;

		if (room.visibility == RoomVisibility.PRIVATE) {

			code = JOptionPane.showInputDialog(this, "비밀방 입장 코드를 입력하세요.", "입장 코드", JOptionPane.PLAIN_MESSAGE);

			if (code == null) {

				return;

			}

		}

		try {

			LobbyMessage resp = client.joinRoom(room.roomId, code);

			if (LobbyMessageType.ERROR.name().equals(resp.type)) {

				JOptionPane.showMessageDialog(this, resp.message);

				return;

			}

			applyMyRoomFromServer(resp);

			refreshRooms();

		} catch (Exception ex) {

			JOptionPane.showMessageDialog(this, ex.getMessage());

		}

	}

	private void openCoopSaveSlots() {
		dispose();
		panel.openCoopSlotScreen(2);
	}



	private void startCoopGame() {
		if (!ensureConnected()) {
			return;
		}
		try {
			LobbyMessage resp;
			if (panel.pendingCoopResume != null) {
				Gson gson = new Gson();
				String json = gson.toJson(panel.pendingCoopResume);
				resp = client.resumeCoop(panel.pendingCoopResume.coopId, panel.pendingCoopResume.seed, json);
				panel.pendingCoopResume = null;
				startGameBtn.setText("게임 시작 (방장)");
			} else {
				resp = client.startGame();
			}
			if (LobbyMessageType.ERROR.name().equals(resp.type)) {
				JOptionPane.showMessageDialog(this, resp.message);
				return;
			}
			CoopNetworkBridge bridge = new CoopNetworkBridge(panel);
			bridge.attach(client);
			panel.coopBridge = bridge;
			panel.onlineLobbyClient = client;
			client = null;
			retainConnectionOnClose = true;
			bridge.beginFromStartMessage(resp);
			if (refreshTimer != null) {
				refreshTimer.stop();
			}
			setVisible(false);
			dispose();
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage());
		}
	}

	public void disposeKeepConnection() {
		retainConnectionOnClose = true;
		if (refreshTimer != null) {
			refreshTimer.stop();
		}
		setVisible(false);
		dispose();
	}

	private void leaveRoom() {

		if (client == null) {

			return;

		}

		try {

			client.leaveRoom();

			clearMyRoomPanel();

			refreshRooms();

		} catch (Exception ex) {

			JOptionPane.showMessageDialog(this, ex.getMessage());

		}

	}



	private void disconnectClient() {

		if (refreshTimer != null) {

			refreshTimer.stop();

		}

		if (client != null) {

			client.close();

			client = null;

		}

		clearMyRoomPanel();

	}



	@Override

	public void dispose() {

		if (!retainConnectionOnClose) {
			if (panel.coopBridge != null && panel.onlineLobbyClient == null) {
				panel.coopBridge.detach();
				panel.coopBridge = null;
			}
			disconnectClient();
		}

		super.dispose();

	}

}
