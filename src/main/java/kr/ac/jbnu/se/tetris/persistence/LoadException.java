package kr.ac.jbnu.se.tetris.persistence;

/** 손상·누락된 저장 파일. */
public class LoadException extends Exception {

	public LoadException(String message) {
		super(message);
	}

	public LoadException(String message, Throwable cause) {
		super(message, cause);
	}
}
