package com.nju.andball;

import android.content.Context;
import android.content.SharedPreferences;

public class Constants {
	private static Constants instance;
	
	private static final String PREFS_NAME = "GAME_USERDATA";
	private static final String SOUND_KEY = "soundEnabled";
	private static final String SCORE_KEY = "highScore";
	private static final String FIRST_KEY = "firstEnter";
	
	private SharedPreferences mSettings;
	private SharedPreferences.Editor mEditor;
	
	private boolean soundEnabled;
	private int highScore;
	private boolean firstEnter;
	
	private Constants(Context context){
		super();
		mSettings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		mEditor = mSettings.edit();
		highScore = mSettings.getInt(SCORE_KEY, 0);
		soundEnabled = mSettings.getBoolean(SOUND_KEY, true);
		firstEnter = mSettings.getBoolean(FIRST_KEY, true);
	}
	
	public synchronized boolean isFirstEnter() {
		return firstEnter;
	}

	public synchronized void setFirstEnter(boolean firstEnter) {
		this.firstEnter = firstEnter;
		mEditor.putBoolean(FIRST_KEY, firstEnter);
		mEditor.commit();
	}

	public static Constants getInstance(Context context){
		if(instance==null){
			instance = new Constants(context);
		}
		return instance;
	}

	public synchronized boolean getSoundEnabled() {
		return soundEnabled;
	}

	public synchronized void setSoundEnabled(boolean ifMute) {
		this.soundEnabled = ifMute;
		mEditor.putBoolean(SOUND_KEY, ifMute);
		mEditor.commit();
	}

	public synchronized int getHighScore() {
		return highScore;
	}

	public synchronized void setHighScore(int highScore) {
		if(highScore > this.highScore){
			this.highScore = highScore;
			mEditor.putInt(SCORE_KEY, highScore);
			mEditor.commit();
		}
	}
}
