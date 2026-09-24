package kr.ac.jbnu.se.tetris.ui.router;

public class SaveSlotParams implements RouteParams {

	public final SavePurpose purpose;

	public SaveSlotParams(SavePurpose purpose) {
		this.purpose = purpose;
	}
}
