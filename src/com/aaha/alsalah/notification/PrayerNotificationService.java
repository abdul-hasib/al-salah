package com.aaha.alsalah.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.aaha.alsalah.Home;
import com.aaha.alsalah.R;
import com.aaha.db.DBAdapter;
import com.aaha.util.Alarm;
import com.aaha.util.Util;

public class PrayerNotificationService extends BroadcastReceiver {

	DBAdapter db = null;
	Context context;

	@Override
	public void onReceive(Context context, Intent intent) {
		this.context = context;
		if (intent.getBooleanExtra(Alarm.PRAYER_NOTIFY, false)) {

			db = new DBAdapter(context);
			db.open();

			String prayerName = intent.getStringExtra(Alarm.PRAYER_NAME);
			int prayerCode = intent.getIntExtra(Alarm.PRAYER_CODE, 270182);
			showNotification(prayerName, prayerCode);

			try {
				Alarm.enablePrayerReminders(context, db);
			} catch (Exception e) {
				Util.Toast(context, "Exception occured while setting alarms", e);
			}

			db.close();
		}
	}

	private void showNotification(String prayerName, int prayerAlarmCode) {
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		prayerAlarmCode = 270182;

		Intent intent = new Intent(context, Home.class);
		intent.putExtra(Alarm.CANCEL, true);
		intent.putExtra(Alarm.CANCEL_ID, prayerAlarmCode);
		intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
				| Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET
				| Intent.FLAG_ACTIVITY_NEW_TASK);

		PendingIntent pendingIntent = PendingIntent.getActivity(context,
				prayerAlarmCode, intent, 0);

		String ticker = "Time to offer " + prayerName;
		String contentText = "Did you offer " + prayerName + "?";
		String contentTitle = Util.getString(context, R.string.app_name);

		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
				context).setContentIntent(pendingIntent).setTicker(ticker)
				.setContentTitle(contentTitle).setContentText(contentText)
				.setSmallIcon(R.drawable.ic_launcher)
				.setDefaults(Notification.DEFAULT_ALL)
				.setWhen(System.currentTimeMillis());

		Notification notification = notificationBuilder.getNotification();

		// notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.defaults |= Notification.DEFAULT_LIGHTS;
		notification.defaults |= Notification.DEFAULT_SOUND;

		notificationManager.notify(prayerAlarmCode, notification);

	}
}
