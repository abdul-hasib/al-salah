package com.aaha.util;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.aaha.alsalah.R;
import com.aaha.alsalah.notification.PrayerNotificationService;
import com.aaha.db.DBAdapter;

public class Alarm {

	public static final int ALARM_CODE_FAJR = 271820;
	public static final int ALARM_CODE_ZOHAR = 271821;
	public static final int ALARM_CODE_ASR = 271822;
	public static final int ALARM_CODE_MAGRIB = 271823;
	public static final int ALARM_CODE_ISHA = 271824;

	public final static String PRAYER_NAME = "com.aaha.al_salah.prayer.notification.PRAYER_NAME";
	public final static String PRAYER_CODE = "com.aaha.al_salah.prayer.notification.PRAYER_CODE";
	public final static String PRAYER_NOTIFY = "com.aaha.al_salah.prayer.notification.PRAYER_NOTIFY";
	public final static String CANCEL = "com.aaha.al_salah.prayer.notification.CANCEL";
	public final static String CANCEL_ID = "com.aaha.al_salah.prayer.notification.CANCEL_ID";

	public final static String REDIRECT = "com.aaha.al_salah.prayer.notification.REDIRECT";

	public static void setAlarm(Context context, String prayer,
			int prayerAlarmCode, int hour, int minute) {

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, 0);

		Calendar calNow = Calendar.getInstance();

		if (calendar.before(calNow)) {
			calendar.add(Calendar.DATE, 1);
		}

		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		Intent intent = new Intent(context, PrayerNotificationService.class);
		intent.putExtra(Alarm.PRAYER_NAME, prayer);
		intent.putExtra(Alarm.PRAYER_CODE, prayerAlarmCode);
		intent.putExtra(Alarm.PRAYER_NOTIFY, true);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
				prayerAlarmCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
				pendingIntent);
	}

	public static void cancelAlarm(Context context, String prayer,
			int prayerAlarmCode) {
		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, PrayerNotificationService.class);

		PendingIntent cancelIntent = PendingIntent.getBroadcast(context,
				prayerAlarmCode, intent, 0);

		alarmManager.cancel(cancelIntent);
	}

	public static void enablePrayerReminders(Context context, DBAdapter db) {
		String fajr, zohar, asr, magrib, isha;

		fajr = Util.getString(context, R.string.fajr);
		zohar = Util.getString(context, R.string.zohar);
		asr = Util.getString(context, R.string.asr);
		magrib = Util.getString(context, R.string.magrib);
		isha = Util.getString(context, R.string.isha);

		int hour, minute;

		if (db.notification.getState(fajr)) {
			hour = db.notification.getHour(fajr);
			minute = db.notification.getMinute(fajr);
			setAlarm(context, fajr, ALARM_CODE_FAJR, hour, minute);
		} else {
			cancelAlarm(context, fajr, ALARM_CODE_FAJR);
		}

		if (db.notification.getState(zohar)) {
			hour = db.notification.getHour(zohar);
			minute = db.notification.getMinute(zohar);
			setAlarm(context, zohar, ALARM_CODE_ZOHAR, hour, minute);
		} else {
			cancelAlarm(context, zohar, ALARM_CODE_ZOHAR);
		}

		if (db.notification.getState(asr)) {
			hour = db.notification.getHour(asr);
			minute = db.notification.getMinute(asr);
			setAlarm(context, asr, ALARM_CODE_ASR, hour, minute);
		} else {
			cancelAlarm(context, asr, ALARM_CODE_ASR);
		}

		if (db.notification.getState(magrib)) {
			hour = db.notification.getHour(magrib);
			minute = db.notification.getMinute(magrib);
			setAlarm(context, magrib, ALARM_CODE_MAGRIB, hour, minute);
		} else {
			cancelAlarm(context, magrib, ALARM_CODE_MAGRIB);
		}

		if (db.notification.getState(isha)) {
			hour = db.notification.getHour(isha);
			minute = db.notification.getMinute(isha);
			setAlarm(context, isha, ALARM_CODE_ISHA, hour, minute);
		} else {
			cancelAlarm(context, isha, ALARM_CODE_ISHA);
		}
	}

}
