package com.aaha.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;

import com.aaha.db.DBAdapter.PrayerType;

public class Util extends Activity {

	static boolean showSplah = true;
	static String AM = " am";
	static String PM = " pm";

	@SuppressLint("SimpleDateFormat")
	public static Date parseDate(String date) {
		SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yy");
		try {
			return parser.parse(date);
		} catch (ParseException e) {
			LogUtil.e("Exception in parsing " + e.toString());
			e.printStackTrace();
		}
		return null;
	}

	@SuppressLint("SimpleDateFormat")
	public static String formatDate(long milliSeconds) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
		String formatedDate = formatter.format(milliSeconds);
		return formatedDate;
	}

	@SuppressLint("SimpleDateFormat")
	public static boolean isFriday(long milliSeconds) {
		SimpleDateFormat formatter = new SimpleDateFormat("EEE");
		String formatedDate = formatter.format(milliSeconds);
		return (formatedDate.equalsIgnoreCase("Fri"));
	}

	public static int getHeaderColor() {
		return Color.rgb(169, 198, 236);
	}

	public static boolean isEmptyString(String s) {
		return s.trim().length() == 0;
	}

	public static PrayerType getPrayerType(int type) {

		switch (type) {
		case 0:
			return PrayerType.ADA;
		case 1:
			return PrayerType.QADHA;
		case 2:
			return PrayerType.ADDITIONAL;
		}
		return null;
	}

	public static void setSplashScreenState(boolean splashScreenState) {
		showSplah = splashScreenState;
	}

	public static boolean getSplashScreenState() {
		return showSplah;
	}

	public static String pad(int c) {
		if (c >= 10)
			return String.valueOf(c);
		else
			return "0" + String.valueOf(c);
	}

	public static CharSequence get12HourFormatDate(int hour, int minute) {
		String ampm = AM;

		if (hour > 12) {
			hour = (hour % 13) + 1;
			ampm = PM;
		}

		return new StringBuilder().append(Util.pad(hour)).append(":")
				.append(Util.pad(minute)).append(ampm);
	}

	public static int getHour(String time) {
		int hour = Integer.valueOf(time.trim().split(":")[0]);

		if (time.indexOf(PM) > -1) {
			hour += 12;
		}

		return hour;
	}

	public static int getMinute(String time) {

		time = time.replace(AM, "");
		time = time.replace(PM, "");

		return Integer.valueOf(time.trim().split(":")[1]);
	}

	public static String getString(Context context, int id) {
		return context.getResources().getString(id);
	}

}
