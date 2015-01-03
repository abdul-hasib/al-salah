package com.aaha.alsalah.ada;

import java.util.Calendar;
import java.util.Date;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;

import com.aaha.alsalah.R;
import com.aaha.alsalah.settings.Settings;
import com.aaha.alsalah.tasbeeh.TasbeehHome;
import com.aaha.db.DBAdapter;
import com.aaha.db.DBAdapter.PrayerType;
import com.aaha.db.DBAdapter.Prayers;
import com.aaha.util.Util;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class AddAda extends SherlockFragmentActivity implements OnClickListener {
	public int year, month, day, hour, minute;
	static final int DATE_DIALOG_ID = 1;

	DBAdapter db;
	Button prayerDate, savePrayer;
	CheckBox fajr, zohar, asr, magrib, isha;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_daily_salah);

		db = new DBAdapter(getApplicationContext());
		db.open();

		prayerDate = (Button) findViewById(R.id.selectDate);
		prayerDate.setOnClickListener(this);

		savePrayer = (Button) findViewById(R.id.savePrayerButton);
		savePrayer.setOnClickListener(this);

		fajr = (CheckBox) findViewById(R.id.fajrCheckbox);
		zohar = (CheckBox) findViewById(R.id.zoharCheckbox);
		asr = (CheckBox) findViewById(R.id.asrCheckbox);
		magrib = (CheckBox) findViewById(R.id.magribCheckbox);
		isha = (CheckBox) findViewById(R.id.ishaCheckbox);

		setPrayerDate(new Date());

	}

	private void setPrayerDate(Date date) {

		Date today = new Date();
		if (date.compareTo(today) > 0) {
			Util.Toast(getApplicationContext(),
					"Thanks for testing, Please select correct date");
		} else {
			prayerDate.setText(Util.formatDate(date.getTime()));
			setPrayers();
		}
	}

	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int yearSelected,
				int monthOfYear, int dayOfMonth) {
			year = yearSelected;
			month = monthOfYear + 1;
			day = dayOfMonth;
			Date date = new Date();
			try {
				date = Util.parseDate(day + "/" + month + "/" + year);
			} catch (Exception e) {
				Util.Toast(getApplicationContext(),
						"Unknown exception occured while parsing date");
			}
			setPrayerDate(date);
		}
	};

	@SuppressWarnings("deprecation")
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.savePrayerButton:

			String day = prayerDate.getText().toString();
			long prayerId = db.prayer.isExist(day, PrayerType.ADA);

			int f = fajr.isChecked() ? 1 : 0;
			int z = zohar.isChecked() ? 1 : 0;
			int a = asr.isChecked() ? 1 : 0;
			int m = magrib.isChecked() ? 1 : 0;
			int i = isha.isChecked() ? 1 : 0;

			if (prayerId == -1) {
				addPrayer(day, f, z, a, m, i);
			} else {
				updatePrayer(prayerId, f, z, a, m, i);
			}
			backToHome();
			break;
		case R.id.selectDate:
			showDialog(DATE_DIALOG_ID);
			break;
		}
	}

	private void setPrayers() {

		String day = prayerDate.getText().toString();
		long prayerId = db.prayer.isExist(day, PrayerType.ADA);
		if (prayerId == -1) {
			fajr.setChecked(false);
			zohar.setChecked(false);
			asr.setChecked(false);
			magrib.setChecked(false);
			isha.setChecked(false);
		} else {
			Cursor c = db.prayer.get(prayerId);
			try {
				if (c != null) {
					c.moveToFirst();
					fajr.setChecked((c.getInt(c
							.getColumnIndex(Prayers.KEY_FAJR)) == 1));
					zohar.setChecked(c.getInt(c
							.getColumnIndex(Prayers.KEY_ZOHAR)) == 1);
					asr.setChecked(c.getInt(c.getColumnIndex(Prayers.KEY_ASR)) == 1);
					magrib.setChecked(c.getInt(c
							.getColumnIndex(Prayers.KEY_MAGRIB)) == 1);
					isha.setChecked(c.getInt(c.getColumnIndex(Prayers.KEY_ISHA)) == 1);

				}
			} catch (Exception e) {
				Util.Toast(
						getApplicationContext(),
						"Exception occured while loading existing prayers"
								+ e.toString());
				e.printStackTrace();
			} finally {
				if (c != null)
					c.close();
			}
		}
	}

	private void addPrayer(String day, int f, int z, int a, int m, int i) {

		if (db.prayer.add(db.user.getActiveUserId(), day, f, z, a, m, i,
				PrayerType.ADA) > 0) {
			Util.Toast(getApplicationContext(), "Salah saved!");
		} else {
			Util.Toast(getApplicationContext(), "Error while saving prayer");
		}
	}

	private void updatePrayer(long prayerId, int f, int z, int a, int m, int i) {

		if (db.prayer.update(prayerId, f, z, a, m, i) > 0) {
			Util.Toast(getApplicationContext(), "Salah updated!");
		} else {
			Util.Toast(getApplicationContext(), "Error while updating prayer");
		}
	}

	private void backToHome() {

		boolean showTasbeehPage = Settings.getBoolean(
				Settings.PREF_TASBEEH_AUTO_DIRECT, getApplicationContext(),
				false);

		if (showTasbeehPage) {
			Intent i = new Intent(AddAda.this, TasbeehHome.class);
			startActivity(i);
		}
		AddAda.this.finish();
	}

	@Override
	protected void onDestroy() {
		db.close();
		super.onDestroy();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {

		case DATE_DIALOG_ID:
			Calendar c = Calendar.getInstance();
			year = c.get(Calendar.YEAR);
			month = c.get(Calendar.MONTH);
			day = c.get(Calendar.DAY_OF_MONTH);

			return new DatePickerDialog(this, mDateSetListener, year, month,
					day);
		}
		return null;
	}
}
