package com.aaha.alsalah.menstruation;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;

import com.aaha.alsalah.R;
import com.aaha.db.DBAdapter;
import com.aaha.db.DBAdapter.Qasr;
import com.aaha.util.Salah;
import com.aaha.util.Util;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class SetMenstruation extends SherlockFragmentActivity implements
		OnClickListener {

	int prayerId;
	long date;
	DBAdapter db;
	Button prayerDate, savePrayer;
	CheckBox fajr, zohar, asr, magrib, isha;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_daily_salah);

		db = new DBAdapter(getApplicationContext());
		db.open();

		prayerDate = (Button) findViewById(R.id.selectDate);
		prayerDate.setEnabled(false);

		savePrayer = (Button) findViewById(R.id.savePrayerButton);
		savePrayer.setOnClickListener(this);
		savePrayer.setText(Util.getString(getApplicationContext(),
				R.string.mark_menstruation));

		fajr = (CheckBox) findViewById(R.id.fajrCheckbox);
		zohar = (CheckBox) findViewById(R.id.zoharCheckbox);
		asr = (CheckBox) findViewById(R.id.asrCheckbox);
		magrib = (CheckBox) findViewById(R.id.magribCheckbox);
		isha = (CheckBox) findViewById(R.id.ishaCheckbox);

		Bundle extras = getIntent().getExtras();
		prayerId = (int) extras.getLong(Salah.PRAYER_ID);
		setPrayer(prayerId);
	}

	private void setPrayer(int prayerId) {
		Cursor c = db.menstruation.get(prayerId);
		int f = 0, z = 0, a = 0, m = 0, i = 0;
		try {
			if (c != null && c.moveToFirst()) {
				f = c.getInt(c.getColumnIndex(Qasr.KEY_FAJR));
				z = c.getInt(c.getColumnIndex(Qasr.KEY_ZOHAR));
				a = c.getInt(c.getColumnIndex(Qasr.KEY_ASR));
				m = c.getInt(c.getColumnIndex(Qasr.KEY_MAGRIB));
				i = c.getInt(c.getColumnIndex(Qasr.KEY_ISHA));
			}
		} catch (Exception e) {
			e.printStackTrace();
			Util.Toast(getApplicationContext(), "Error occurred: " + e);
		} finally {
			if (c != null)
				c.close();
		}
		fajr.setChecked(f == 1);
		zohar.setChecked(z == 1);
		asr.setChecked(a == 1);
		magrib.setChecked(m == 1);
		isha.setChecked(i == 1);

		date = db.prayer.getDate(prayerId);
		prayerDate.setText(Util.formatDate(date * 1000));
	}

	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.savePrayerButton:

			int f = fajr.isChecked() ? 1 : 0;
			int z = zohar.isChecked() ? 1 : 0;
			int a = asr.isChecked() ? 1 : 0;
			int m = magrib.isChecked() ? 1 : 0;
			int i = isha.isChecked() ? 1 : 0;

			String date = prayerDate.getText().toString();

			setHaize(prayerId, date, f, z, a, m, i);
			backToHome();
			break;
		}
	}

	private void setHaize(int prayerId, String date, int f, int z, int a,
			int m, int i) {
		long result = -1;
		if (db.menstruation.isExist(prayerId)) {
			result = db.menstruation.update(prayerId, f, z, a, m, i);
		} else {
			result = db.menstruation.add(prayerId, date, f, z, a, m, i);
		}

		if (result > 0) {
			Util.Toast(getApplicationContext(), "Menstruation settings saved!");
		} else {
			Util.Toast(getApplicationContext(),
					"Error while updating Menstruation settings");

		}

	}

	private void backToHome() {
		super.finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			db.close();
		} catch (Exception ignoreMe) {
		}
	}
}
