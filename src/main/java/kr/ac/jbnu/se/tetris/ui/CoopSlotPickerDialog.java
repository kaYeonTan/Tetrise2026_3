package kr.ac.jbnu.se.tetris.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import kr.ac.jbnu.se.tetris.GamePanel;
import kr.ac.jbnu.se.tetris.network.lobby.LobbyService;
import kr.ac.jbnu.se.tetris.network.lobby.LobbyMessage;
import kr.ac.jbnu.se.tetris.persistence.CoopGameMemento;
import kr.ac.jbnu.se.tetris.persistence.LoadException;
import kr.ac.jbnu.se.tetris.persistence.SaveService;
import kr.ac.jbnu.se.tetris.persistence.SaveSlotMeta;

/** 로비 등에서 게임 화면으로 나가지 않고 협동 슬롯을 고른다. */
public final class CoopSlotPickerDialog {

	private CoopSlotPickerDialog() {
	}

	public static void showLobbyLoad(java.awt.Window owner, GamePanel panel, LobbyService client, Runnable onApplied) {
		if (panel.getLoginUser() == null || panel.getLoginUser().isBlank()) {
			JOptionPane.showMessageDialog(owner, "로그인 후 이용할 수 있어요.", "저장 불러오기",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		JDialog dialog = new JDialog(owner, "저장 불러오기 · 슬롯 선택",
				java.awt.Dialog.ModalityType.APPLICATION_MODAL);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.setMinimumSize(new Dimension(520, 360));
		dialog.setLocationRelativeTo(owner);

		JPanel root = new JPanel(new BorderLayout(8, 8));
		root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		root.setBackground(new Color(0xf5ecd8));

		JLabel title = new JLabel("온라인 협동 저장 슬롯 (1~3)");
		title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
		root.add(title, BorderLayout.NORTH);

		JPanel slots = new JPanel(new GridLayout(3, 1, 0, 8));
		slots.setOpaque(false);
		List<SaveSlotMeta> list = SaveService.listCoopSlots(panel.getLoginUser());
		for (int i = 0; i < 3; i++) {
			SaveSlotMeta meta = i < list.size() ? list.get(i) : new SaveSlotMeta(i + 1, false, "비어 있음", 0);
			int slot = meta.slot;
			JButton btn = new JButton(meta.occupied ? meta.summary : ("슬롯 " + slot + " — 비어 있음"));
			btn.setEnabled(meta.occupied);
			btn.addActionListener(e -> {
				try {
					CoopGameMemento mem = SaveService.loadCoop(panel.getLoginUser(), slot);
					if (!mem.onlineResume) {
						throw new LoadException("온라인 협동 저장만 불러올 수 있어요.");
					}
					if (client != null && client.isConnected()) {
						try {
							LobbyMessage room = client.fetchMyRoom();
							if (room != null && room.players != null && room.players.size() >= 2) {
								SaveService.assertOnlineRoomMatchesSave(mem, room.players.get(0), room.players.get(1));
							}
						} catch (LoadException ex) {
							JOptionPane.showMessageDialog(dialog, ex.getMessage(), "불러오기",
									JOptionPane.WARNING_MESSAGE);
							return;
						} catch (Exception ignored) {
						}
					}
					panel.pendingCoopResume = mem;
					dialog.dispose();
					JOptionPane.showMessageDialog(owner,
							"슬롯 " + slot + "을(를) 불러왔어요.\n2명이 모인 뒤 방장이 「게임 시작 (방장)」을 누르면 이어집니다.",
							"저장 불러오기", JOptionPane.INFORMATION_MESSAGE);
					if (onApplied != null) {
						SwingUtilities.invokeLater(onApplied);
					}
				} catch (LoadException ex) {
					JOptionPane.showMessageDialog(dialog, ex.getMessage(), "불러오기", JOptionPane.WARNING_MESSAGE);
				}
			});
			slots.add(btn);
		}
		root.add(slots, BorderLayout.CENTER);

		JButton close = new JButton("닫기");
		close.addActionListener(e -> dialog.dispose());
		JPanel south = new JPanel(new BorderLayout());
		south.setOpaque(false);
		south.add(close, BorderLayout.EAST);
		root.add(south, BorderLayout.SOUTH);

		dialog.setContentPane(root);
		dialog.pack();
		dialog.setVisible(true);
	}
}
