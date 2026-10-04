package kr.ac.jbnu.se.tetris.ui.screen;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import kr.ac.jbnu.se.tetris.persistence.SaveService;
import kr.ac.jbnu.se.tetris.persistence.SaveSlotMeta;
import kr.ac.jbnu.se.tetris.ui.router.SavePurpose;
import kr.ac.jbnu.se.tetris.ui.router.SaveSlotParams;
import kr.ac.jbnu.se.tetris.ui.router.ScreenRouter;

/** F-06 협동 슬롯 UI (라우터 연동). */
public class SaveSlotsOverlayScreen extends JPanel {

	private final ScreenRouter router;
	private final SavePurpose purpose;
	private final String userId;

	public SaveSlotsOverlayScreen(ScreenRouter router, SaveSlotParams params) {
		this.router = router;
		this.purpose = params != null ? params.purpose : SavePurpose.LOAD;
		this.userId = "_local";
		setLayout(new BorderLayout(8, 8));
		JLabel title = new JLabel(purpose == SavePurpose.SAVE ? "협동 저장 (슬롯 1~3)" : "협동 불러오기");
		title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
		add(title, BorderLayout.NORTH);
		JPanel slots = new JPanel();
		List<SaveSlotMeta> list = SaveService.listCoopSlots(userId);
		for (SaveSlotMeta meta : list) {
			JButton b = new JButton(meta.summary);
			int slot = meta.slot;
			b.addActionListener(e -> onSlot(slot));
			slots.add(b);
		}
		add(slots, BorderLayout.CENTER);
		JButton close = new JButton("닫기");
		close.addActionListener(e -> router.back());
		add(close, BorderLayout.SOUTH);
	}

	void onSlot(int slot) {
		// GamePanel 경로가 본체; 라우터는 메타 표시만.
		router.back();
	}
}
