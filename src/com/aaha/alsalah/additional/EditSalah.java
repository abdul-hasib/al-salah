package com.aaha.alsalah.additional;

import java.util.Date;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.aaha.alsalah.R;
import com.aaha.db.DBAdapter;
import com.aaha.db.DBAdapter.Prayers;
import com.aaha.util.Salah;
import com.aaha.util.Util;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class EditSalah extends SherlockFragmentActivity implements
		OnClickListener {

	int prayerId;
	DBAdapter db;
	Cursor mCursor = null;
	EditText fajrCount, zoharCount, asrCount, magribCount, ishaCount;
	Button addAdditonalPrayers, prayerDate;
	Button fajrNext, zoharNext, asrNext, magribNext, ishaNext;
	Button fajrPrev, zoharPrev, asrPrev, magribPrev, ishaPrev;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_additional_salah);

		db = new DBAdapter(getApplicationContext());
		db.open();

		prayerDate = (Button) findViewById(R.id.selectDate);
		prayerDate.setEnabled(false);

		fajrCount = (EditText) findViewById(R.id.additionalFajr);
		zoharCount = (EditText) findViewById(R.id.additionalZohar);
		asrCount = (EditText) findViewById(R.id.additionalAsr);
		magribCount = (EditText) findViewById(R.id.additionalMagrib);
		ishaCount = (EditText) findViewById(R.id.additionalIsha);

		addAdditonalPrayers = (Button) findViewById(R.id.addAdditonalSalah);
		addAdditonalPrayers.setText(getResources().getString(R.string.update));
		addAdditonalPrayers.setOnClickListener(this);
		addAdditonalPrayers.setEnabled(true);

		fajrNext = (Button) findViewById(R.id.fajrNext);
		zoharNext = (Button) findViewById(R.id.zoharNext);
		asrNext = (Button) findViewById(R.id.asrNext);
		magribNext = (Button) findViewById(R.id.magribNext);
		ishaNext = (Button) findViewById(R.id.ishaNext);

		fajrPrev = (Button) findViewById(R.id.fajrPrev);
		zoharPrev = (Button) findViewById(R.id.zoharPrev);
		asrPrev = (Button) findViewById(R.id.asrPrev);
		magribPrev = (Button) findViewById(R.id.magribPrev);
		ishaPrev = (Button) findViewById(R.id.ishaPrev);

		fajrNext.setOnClickListener(this);
		zoharNext.setOnClickListener(this);
		asrNext.setOnClickListener(this);
		magribNext.setOnClickListener(this);
		ishaNext.setOnClickListener(this);

		fajrPrev.setOnClickListener(this);
		zoharPrev.setOnClickListener(this);
		asrPrev.setOnClickListener(this);
		magribPrev.setOnClickListener(this);
		ishaPrev.setOnClickListener(this);

		Bundle extras = getIntent().getExtras();
		prayerId = (int) extras.getLong(Salah.PRAYER_ID);
		setAdditionalPrayers(prayerId);
	}

	public void setAdditionalPrayers(int prayerId) {
		mCursor = db.prayer.get(prayerId);
		long date = (new Date()).getTime();
		int f = 0, z = 0, a = 0, m = 0, i = 0;
		if (mCursor != null) {
			if (mCursor.moveToFirst()) {
				f = mCursor.getInt(mCursor.getColumnIndex(Prayers.KEY_FAJR));
				z = mCursor.getInt(mCursor.getColumnIndex(Prayers.KEY_ZOHAR));
				a = mCursor.getInt(mCursor.getColumnIndex(Prayers.KEY_ASR));
				m = mCursor.getInt(mCursor.getColumnIndex(Prayers.KEY_MAGRIB));
				i = mCursor.getInt(mCursor.getColumnIndex(Prayers.KEY_ISHA));
				date = mCursor
						.getLong(mCursor.getColumnIndex(Prayers.KEY_DATE));
			}
			mCursor.close();
		}
		fajrCount.setText(String.valueOf(f));
		zoharCount.setText(String.valueOf(z));
		asrCount.setText(String.valueOf(a));
		magribCount.setText(String.valueOf(m));
		ishaCount.setText(String.valueOf(i));
		prayerDate.setText(Util.formatDate(date * 1000));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			mCursor.close();

		} catch (Exception ignoreMe) {
		}
		try {
			db.close();
		} catch (Exception ignoreMe) {
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.addAdditonalSalah:

			String value = fajrCount.getText().toString().trim();
			int f = Util.isEmptyString(value) ? 0 : Integer.parseInt(value);

			value = zoharCount.getText().toString().trim();
			int z = Util.isEmptyString(value) ? 0 : Integer.parseInt(value);

			value = asrCount.getText().toString().trim();
			int a = Util.isEmptyString(value) ? 0 : Integer.parseInt(value);

			value = magribCount.getText().toString().trim();
			int m = Util.isEmptyString(value) ? 0 : Integer.parseInt(value);

			value = ishaCount.getText().toString().trim();
			int i = Util.isEmptyString(value) ? 0 : Integer.parseInt(value);

			if (validateFields(f, z, a, m, i)) {
				if (db.prayer.update(prayerId, f, z, a, m, i) > 0) {
					Util.Toast(getApplicationContext(), "Qadha Salah updated!");
					super.finish();
				}
			}
			break;
		case R.id.fajrNext:
			increment(fajrCount);
			break;
		case R.id.fajrPrev:
			decrement(fajrCount);
			break;
		case R.id.zoharNext:
			increment(zoharCount);
			break;
		case R.id.zoharPrev:
			decrement(zoharCount);
			break;
		case R.id.asrNext:
			increment(asrCount);
			break;
		case R.id.asrPrev:
			decrement(asrCount);
			break;
		case R.id.magribNext:
			increment(magribCount);
			break;
		case R.id.magribPrev:
			decrement(magribCount);
			break;
		case R.id.ishaNext:
			increment(ishaCount);
			break;
		case R.id.ishaPrev:
			decrement(ishaCount);
			break;
		}
	}

	private void increment(TextView tv) {
		int current = 0;
		try {
			current = Integer.valueOf(tv.getText().toString());
			current++;
		} catch (NumberFormatException e) {
			current = 1;
		}
		tv.setText(String.valueOf(current));
	}

	private void decrement(TextView tv) {
		int current = 0;
		try {
			current = Integer.valueOf(tv.getText().toString());
			if (current > 0) {
				current--;
			}
		} catch (NumberFormatException e) {
		}
		tv.setText(String.valueOf(current));
	}

	private boolean validateFields(int f, int z, int a, int m, int i) {
		if (f == 0 && z == 0 && a == 0 && m == 0 && i == 0) {
			Util.Toast(getApplicationContext(), "Please enter prayers");
			return false;
		}
		return true;
	}
}
