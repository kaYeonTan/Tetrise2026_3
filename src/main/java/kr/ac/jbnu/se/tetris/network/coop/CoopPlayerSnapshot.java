package kr.ac.jbnu.se.tetris.network.coop;

import java.util.ArrayList;
import java.util.List;

/** 한 플레이어 보드·피스·큐 스냅샷 (JSON). */
public class CoopPlayerSnapshot {

	public int type;
	public int rot;
	public int x;
	public int y;
	public int hold = -1;
	public boolean holdUsed;
	public int score;
	public int lines;
	public int garbage;
	public List<Integer> next = new ArrayList<>();
	public List<Integer> bag = new ArrayList<>();
	/** 240 digits, row-major 24×10 (FileManager 호환). */
	public String board = "";
}
