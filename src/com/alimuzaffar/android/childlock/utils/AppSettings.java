package com.alimuzaffar.android.childlock.utils;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public class AppSettings {
	public static boolean		DEBUG			= false;
	private static final String TAG = AppSettings.class.getSimpleName();

	private static AppSettings	instance;
	private SharedPreferences	pref;
	private static final String SETTINGS_NAME = "PL_SETTINGS";
	private Context context;
	
	private String currentVersion;
	private String previousVersion;

	public enum Key {
			PIN,
			IS_PIN_SET,
			APP_VERSION_KEY;
	}
	
	public static AppSettings getInstance(Context context) {
		if (instance == null) {
			instance = new AppSettings(context);
		}
		return instance;
	}

	public AppSettings(Context context) {
		pref = context.getSharedPreferences(SETTINGS_NAME, Activity.MODE_PRIVATE);
		this.context = context.getApplicationContext();
		init();
	}
	
	private void init() {
		previousVersion = getString(Key.APP_VERSION_KEY, "");
		 Log.d(TAG, "lastVersion: " + previousVersion);
	        try {
				currentVersion = context.getPackageManager().getPackageInfo(
						context.getPackageName(), 0).versionName;
			} catch (NameNotFoundException e) {
				currentVersion = "?";
				Log.e(TAG, "could not get version name from manifest!", e);
			}
	        Log.d(TAG, "appVersion: " + currentVersion);
	        
	        // save new version number to preferences
	        set(Key.APP_VERSION_KEY, currentVersion);
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
	
	public boolean firstRun() {
		return  !previousVersion.equals(currentVersion);
	}
	
    public boolean firstRunEver() {
        return  "".equals(previousVersion);
    }
}