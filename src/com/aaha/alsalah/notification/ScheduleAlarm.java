package com.aaha.alsalah.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.aaha.db.DBAdapter;
import com.aaha.util.Alarm;
import com.aaha.util.LogUtil;

public class ScheduleAlarm extends BroadcastReceiver {

	DBAdapter db = null;

	@Override
	public void onReceive(Context context, Intent intent) {

		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")
				|| intent.getAction().equals("android.intent.action.TIME_SET")
				|| intent.getAction().equals(
						"android.intent.action.TIMEZONE_CHANGED")) {

			try {
				db = new DBAdapter(context);
				db.open();
				Alarm.enablePrayerReminders(context, db);
			} catch (Exception e) {
				LogUtil.toastShort(context,
						"Exception occured while setting alarms", e);
			} finally {
				db.close();
			}
		}
	}
}
