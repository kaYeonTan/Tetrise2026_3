package kr.ac.jbnu.se.tetris.persistence;

/** 슬롯 목록 UI용. */
public class SaveSlotMeta {

	public final int slot;
	public final boolean occupied;
	public final String summary;
	public final long savedAtMs;

	public SaveSlotMeta(int slot, boolean occupied, String summary, long savedAtMs) {
		this.slot = slot;
		this.occupied = occupied;
		this.summary = summary;
		this.savedAtMs = savedAtMs;
	}
}
