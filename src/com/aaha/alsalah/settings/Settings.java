package com.aaha.alsalah.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.aaha.alsalah.R;
import com.aaha.util.Util;

public class Settings extends PreferenceActivity {

	public static String PREF_PROFILE_PUB_AGE = "pref_profile_pube_age";
	public static String PREF_PROFILE_DOB = "pref_profile_dob";
	public static String PREF_PROFILE_USERNAME = "pref_profile_username";
	public static String PREF_PROFILE_PASSWORD = "pref_profile_password";
	public static String PREF_PROFILE_GENDER = "pref_profile_gender";
	public static String PREF_HIGHLIGHT_FRIDAY = "pref_highlight_friday";
	public static String PREF_DAILY_SALAH_LIMIT = "pref_daily_salah_limit";
	public static String PREF_HIDE_ADDITIONAL_TAB = "pref_hide_additional_tab";
	public static String PREF_HIDE_QADHA_TAB = "pref_hide_qadha_tab";
	public static String PREF_HIDE_RAMDHAN_TAB = "pref_hide_ramdhan_tab";
	public static String PREF_HIDE_PERFECT_DAYS = "pref_hide_perfect_days";
	public static String PREF_HIDE_TABS = "pref_hide_tabs";

	public static String PREF_TASBEEH_AUTO_DIRECT = "pref_auto_direct_tasbeeh";

	public static class Gender {
		public final static int MALE = 1;
		public final static int FEMALE = 0;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.settings);

	}

	public static void putPref(String key, String value, Context context) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(key, value);
		editor.commit();
	}

	public static String getString(String key, Context context,
			String defaultValue) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		return preferences.getString(key, defaultValue);
	}

	public static int getInt(String key, Context context, int defaultValue) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		int value = -1;
		try {
			String temp = preferences.getString(key,
					String.valueOf(defaultValue));
			if (!Util.isEmptyString(temp)) {
				value = Integer.valueOf(temp);
			}
		} catch (ClassCastException e) {
			return -1;
		}
		return value;
	}

	public static boolean getBoolean(String key, Context context,
			boolean defaultValue) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		return preferences.getBoolean(key, defaultValue);
	}
}
