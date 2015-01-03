package com.aaha.alsalah.ada;

import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;

import com.aaha.alsalah.R;
import com.aaha.alsalah.settings.Settings;
import com.aaha.alsalah.tasbeeh.TasbeehHome;
import com.aaha.db.DBAdapter;
import com.aaha.db.DBAdapter.Prayers;
import com.aaha.util.Salah;
import com.aaha.util.Util;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class EditAda extends SherlockFragmentActivity implements
		OnClickListener {

	int prayerId;
	DBAdapter db;
	Button prayerDate, savePrayer;
	CheckBox fajr, zohar, asr, magrib, isha;
	Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_daily_salah);

		context = getApplicationContext();

		db = new DBAdapter(getApplicationContext());
		db.open();

		prayerDate = (Button) findViewById(R.id.selectDate);
		prayerDate.setEnabled(false);

		savePrayer = (Button) findViewById(R.id.savePrayerButton);
		savePrayer.setOnClickListener(this);
		savePrayer.setText(Util.getString(context, R.string.update));

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
		Cursor c = db.prayer.get(prayerId);
		long date = (new Date()).getTime();
		int f = 0, z = 0, a = 0, m = 0, i = 0;
		try {
			if (c != null) {
				c.moveToFirst();
				f = c.getInt(c.getColumnIndex(Prayers.KEY_FAJR));
				z = c.getInt(c.getColumnIndex(Prayers.KEY_ZOHAR));
				a = c.getInt(c.getColumnIndex(Prayers.KEY_ASR));
				m = c.getInt(c.getColumnIndex(Prayers.KEY_MAGRIB));
				i = c.getInt(c.getColumnIndex(Prayers.KEY_ISHA));
				date = c.getLong(c.getColumnIndex(Prayers.KEY_DATE));
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

			updatePrayer(prayerId, f, z, a, m, i);
			backToHome();
			break;
		}
	}

	private void updatePrayer(int prayerId, int f, int z, int a, int m, int i) {

		if (db.prayer.update(prayerId, f, z, a, m, i) > 0) {
			Util.Toast(getApplicationContext(), "Salah updated!");
		} else {
			Util.Toast(getApplicationContext(), "Error while updating Salah");
		}
	}

	private void backToHome() {
		boolean showTasbeehPage = Settings.getBoolean(
				Settings.PREF_TASBEEH_AUTO_DIRECT, getApplicationContext(),
				false);

		if (showTasbeehPage) {
			Intent i = new Intent(EditAda.this, TasbeehHome.class);
			startActivity(i);
		}

		EditAda.this.finish();

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
