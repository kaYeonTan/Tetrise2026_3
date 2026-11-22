package kr.ac.jbnu.se.tetris.event;

import java.util.ArrayList;
import java.util.EnumMap;

// 게임 안에서 일어난 일 알림 (보스, 소리 등이 구독)
public class GameEventBus {

	ArrayList<GameEventListener> all = new ArrayList<>();
	// 타입별로만 받고 싶을 때
	EnumMap<GameEventType, ArrayList<GameEventListener>> byType = new EnumMap<>(GameEventType.class);

	int publishCount = 0;

	public void subscribe(GameEventListener l) {
		if (l == null)
			return;
		all.add(l);
	}

	public void subscribe(GameEventType t, GameEventListener l) {
		if (l == null || t == null)
			return;
		ArrayList<GameEventListener> list = byType.get(t);
		if (list == null) {
			list = new ArrayList<>();
			byType.put(t, list);
		}
		list.add(l);
	}

	public void unsubscribe(GameEventListener l) {
		all.remove(l);
		for (ArrayList<GameEventListener> list : byType.values()) {
			list.remove(l);
		}
	}

	public void publish(GameEvent e) {
		if (e == null)
			return;
		publishCount++;

		for (int i = 0; i < all.size(); i++) {
			all.get(i).onEvent(e);
		}

		ArrayList<GameEventListener> typed = byType.get(e.type);
		if (typed != null) {
			for (int j = 0; j < typed.size(); j++) {
				typed.get(j).onEvent(e);
			}
		}
	}

	public int getPublishCount() {
		return publishCount;
	}
}
