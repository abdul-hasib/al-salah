package com.aaha.alsalah.notification;

import java.util.Calendar;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TimePicker;

import com.aaha.alsalah.R;
import com.aaha.db.DBAdapter;
import com.aaha.util.Alarm;
import com.aaha.util.Util;
import com.actionbarsherlock.app.SherlockActivity;

public class PrayerReminder extends SherlockActivity implements OnClickListener {

	static final int TIME_DIALOG_ID_FAJR = 1;
	static final int TIME_DIALOG_ID_ZOHAR = 2;
	static final int TIME_DIALOG_ID_ASR = 3;
	static final int TIME_DIALOG_ID_MAGRIB = 4;
	static final int TIME_DIALOG_ID_ISHA = 5;

	int hour;
	int minute;

	DBAdapter db = null;
	PrayerNotificationService notificationService;

	String FAJR, ZOHAR, ASR, MAGRIB, ISHA;

	boolean f, z, a, m, i;

	Button btnFajr, btnZohar, btnAsr, btnMagrib, btnIsha, btnSetReminders;
	CheckBox cbFajr, cbZohar, cbAsr, cbMagrib, cbIsha;
	Button btnGeneral;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_prayer_reminder);

		db = new DBAdapter(getApplicationContext());
		db.open();

		notificationService = new PrayerNotificationService();

		FAJR = getResources().getString(R.string.fajr);
		ZOHAR = getResources().getString(R.string.zohar);
		ASR = getResources().getString(R.string.asr);
		MAGRIB = getResources().getString(R.string.magrib);
		ISHA = getResources().getString(R.string.isha);

		if (!db.notification.isExist()) {
			db.notification.add(FAJR, 0, 0, 0);
			db.notification.add(ZOHAR, 0, 0, 0);
			db.notification.add(ASR, 0, 0, 0);
			db.notification.add(MAGRIB, 0, 0, 0);
			db.notification.add(ISHA, 0, 0, 0);
		}

		btnFajr = (Button) findViewById(R.id.fajrTime);
		btnFajr.setOnClickListener(this);

		btnZohar = (Button) findViewById(R.id.zoharTime);
		btnZohar.setOnClickListener(this);

		btnAsr = (Button) findViewById(R.id.asrTime);
		btnAsr.setOnClickListener(this);

		btnMagrib = (Button) findViewById(R.id.magribTime);
		btnMagrib.setOnClickListener(this);

		btnIsha = (Button) findViewById(R.id.ishaTime);
		btnIsha.setOnClickListener(this);

		cbFajr = (CheckBox) findViewById(R.id.fajrReminder);
		cbZohar = (CheckBox) findViewById(R.id.zoharReminder);
		cbAsr = (CheckBox) findViewById(R.id.asrReminder);
		cbMagrib = (CheckBox) findViewById(R.id.magribReminder);
		cbIsha = (CheckBox) findViewById(R.id.ishaReminder);

		btnSetReminders = (Button) findViewById(R.id.setReminders);
		btnSetReminders.setOnClickListener(this);

		loadNotificationSettings();

	}

	private void loadNotificationSettings() {
		if (db.notification.getState(FAJR)) {
			cbFajr.setChecked(true);
			f = true;
			btnFajr.setText(db.notification.getTime(FAJR));
		} else {
			cbFajr.setChecked(false);
		}

		if (db.notification.getState(ZOHAR)) {
			cbZohar.setChecked(true);
			z = true;
			btnZohar.setText(db.notification.getTime(ZOHAR));
		} else {
			cbZohar.setChecked(false);
		}

		if (db.notification.getState(ASR)) {
			cbAsr.setChecked(true);
			a = true;
			btnAsr.setText(db.notification.getTime(ASR));
		} else {
			cbAsr.setChecked(false);
		}

		if (db.notification.getState(MAGRIB)) {
			cbMagrib.setChecked(true);
			m = true;
			btnMagrib.setText(db.notification.getTime(MAGRIB));
		} else {
			cbMagrib.setChecked(false);
		}

		if (db.notification.getState(ISHA)) {
			cbIsha.setChecked(true);
			i = true;
			btnIsha.setText(db.notification.getTime(ISHA));
		} else {
			cbIsha.setChecked(false);
		}
	}

	@Override
	protected void onStop() {

		super.onStop();
	}

	@Override
	protected void onDestroy() {
		db.close();
		super.onDestroy();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.fajrTime:
			btnGeneral = btnFajr;
			showDialog(TIME_DIALOG_ID_FAJR);

			break;
		case R.id.zoharTime:
			btnGeneral = btnZohar;
			showDialog(TIME_DIALOG_ID_ZOHAR);

			break;
		case R.id.asrTime:
			btnGeneral = btnAsr;
			showDialog(TIME_DIALOG_ID_ASR);

			break;
		case R.id.magribTime:
			btnGeneral = btnMagrib;
			showDialog(TIME_DIALOG_ID_MAGRIB);

			break;
		case R.id.ishaTime:
			btnGeneral = btnIsha;
			showDialog(TIME_DIALOG_ID_ISHA);

			break;
		case R.id.setReminders:
			try {
				updateReminderTime();
				Alarm.enablePrayerReminders(getApplicationContext(), db);
				Util.Toast(getApplicationContext(), "Notifications are updated");
				super.finish();
			} catch (NumberFormatException ex) {
				Util.Toast(getApplicationContext(), "Please set time");
			} catch (Exception ex) {
				Util.Toast(getApplicationContext(), "Exception occurred", ex);
			}

			break;
		}
	}

	private void updateReminderTime() {
		if (cbFajr.isChecked()) {
			String time = btnFajr.getText().toString();
			int hour = Util.getHour(time);
			int minute = Util.getMinute(time);
			db.notification.enableNotification(FAJR, hour, minute);
		} else {
			db.notification.disableNotification(FAJR);
		}

		if (cbZohar.isChecked()) {
			String time = btnZohar.getText().toString();
			int hour = Util.getHour(time);
			int minute = Util.getMinute(time);
			db.notification.enableNotification(ZOHAR, hour, minute);
		} else {
			db.notification.disableNotification(ZOHAR);
		}

		if (cbAsr.isChecked()) {
			String time = btnAsr.getText().toString();
			int hour = Util.getHour(time);
			int minute = Util.getMinute(time);
			db.notification.enableNotification(ASR, hour, minute);
		} else {
			db.notification.disableNotification(ASR);
		}

		if (cbMagrib.isChecked()) {
			String time = btnMagrib.getText().toString();
			int hour = Util.getHour(time);
			int minute = Util.getMinute(time);
			db.notification.enableNotification(MAGRIB, hour, minute);
		} else {
			db.notification.disableNotification(MAGRIB);
		}

		if (cbIsha.isChecked()) {
			String time = btnIsha.getText().toString();
			int hour = Util.getHour(time);
			int minute = Util.getMinute(time);
			db.notification.enableNotification(ISHA, hour, minute);
		} else {
			db.notification.disableNotification(ISHA);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());

		int curHour = c.get(Calendar.HOUR_OF_DAY);
		int curMin = c.get(Calendar.MINUTE);
		String time = "set time";

		switch (id) {
		case TIME_DIALOG_ID_FAJR:
			time = btnFajr.getText().toString();
			break;

		case TIME_DIALOG_ID_ZOHAR:
			time = btnZohar.getText().toString();
			break;

		case TIME_DIALOG_ID_ASR:
			time = btnAsr.getText().toString();
			break;

		case TIME_DIALOG_ID_MAGRIB:
			time = btnMagrib.getText().toString();
			break;

		case TIME_DIALOG_ID_ISHA:
			time = btnIsha.getText().toString();
			break;

		}

		if (time.indexOf(":") > -1) {
			curHour = Util.getHour(time);
			curMin = Util.getMinute(time);
		}

		return new TimePickerDialog(this, timePickerListener, curHour, curMin,
				false);
	}

	private TimePickerDialog.OnTimeSetListener timePickerListener = new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int selectedHour,
				int selectedMinute) {
			hour = selectedHour;
			minute = selectedMinute;
			btnGeneral.setText(Util.get12HourFormatDate(hour, minute));
		}
	};

}
