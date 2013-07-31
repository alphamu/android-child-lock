package com.alimuzaffar.android.childlock.utils;
import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppPrefs {
	public static boolean		DEBUG			= false;

	private static AppPrefs	instance;
	private SharedPreferences	pref;

	public enum Key {
		ENABLE_APPLICATION_LOCKING;

		@Override
		public String toString() {
			return super.toString().toLowerCase(Locale.getDefault());
		}
	}

	public AppPrefs(Context context) {
		pref = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public static AppPrefs getInstance(Context context) {
		if (instance == null) {
			instance = new AppPrefs(context);
		}
		return instance;
	}

	public void set(Key key, String val) {
		SharedPreferences.Editor editor = pref.edit();
		editor.putString(key.toString(), val);
		editor.commit();
	}

	public void set(Key key, int val) {
		SharedPreferences.Editor editor = pref.edit();
		editor.putInt(key.toString(), val);
		editor.commit();
	}

	public void set(Key key, boolean val) {
		set(key.toString(), val);
	}
	
	public void set(String key, boolean val) {
		SharedPreferences.Editor editor = pref.edit();
		editor.putBoolean(key, val);
		editor.commit();
	}
	
	public void set(Key key, float val) {
		SharedPreferences.Editor editor = pref.edit();
		editor.putFloat(key.toString(), val);
		editor.commit();
	}

	public void set(Key key, double val) {
		SharedPreferences.Editor editor = pref.edit();
		editor.putString(key.toString(), String.valueOf(val));
		editor.commit();
	}

	public void set(Key key, long val) {
		SharedPreferences.Editor editor = pref.edit();
		editor.putLong(key.toString(), val);
		editor.commit();
	}

	public String getString(Key key) {
		return pref.getString(key.toString(), null);
	}

	public String getString(Key key, String defaultVal) {
		return pref.getString(key.toString(), defaultVal);
	}

	public String getString(String key) {
		return pref.getString(key, null);
	}

	public int getInt(Key key) {
		return pref.getInt(key.toString(), 0);
	}

	public long getLong(Key key) {
		return pref.getLong(key.toString(), 0);
	}

	public float getFloat(Key key) {
		return pref.getFloat(key.toString(), 0);
	}

	public double getDouble(Key key) {
		try {
			return Double.valueOf(pref.getString(key.toString(), "0"));
		} catch (NumberFormatException nfe) {
			return 0;
		}
	}

	public boolean getBoolean(Key key) {
		return pref.getBoolean(key.toString(), false);
	}

	public boolean getBoolean(String key) {
		return pref.getBoolean(key, false);
	}
}