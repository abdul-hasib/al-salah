package com.aaha.alsalah.additional;

import java.util.Calendar;
import java.util.Date;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.aaha.alsalah.R;
import com.aaha.db.DBAdapter;
import com.aaha.db.DBAdapter.PrayerType;
import com.aaha.db.DBAdapter.T_Prayers;
import com.aaha.util.LogUtil;
import com.aaha.util.Salah;
import com.aaha.util.Util;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class AddSalah extends SherlockFragmentActivity implements
		OnClickListener {

	public int year, month, day, hour, minute;
	static final int DATE_DIALOG_ID = 1;

	DBAdapter db;
	Cursor mCursor = null;

	EditText fajrCount, zoharCount, asrCount, magribCount, ishaCount;
	Button addSalah, prayerDate;
	Button fajrNext, zoharNext, asrNext, magribNext, ishaNext;
	Button fajrPrev, zoharPrev, asrPrev, magribPrev, ishaPrev;
	PrayerType prayerType = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_additional_salah);

		db = new DBAdapter(getApplicationContext());
		db.open();

		fajrCount = (EditText) findViewById(R.id.additionalFajr);
		zoharCount = (EditText) findViewById(R.id.additionalZohar);
		asrCount = (EditText) findViewById(R.id.additionalAsr);
		magribCount = (EditText) findViewById(R.id.additionalMagrib);
		ishaCount = (EditText) findViewById(R.id.additionalIsha);

		addSalah = (Button) findViewById(R.id.addAdditonalSalah);
		addSalah.setOnClickListener(this);

		prayerDate = (Button) findViewById(R.id.selectDate);
		prayerDate.setOnClickListener(this);

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
		if (extras.containsKey(Salah.PRAYER_DATE)) {
			long milliseconds = extras.getLong(Salah.PRAYER_DATE);
			setPrayerDate(new Date(milliseconds * 1000));
		} else {
			setPrayerDate(new Date());
		}

		if (extras.containsKey(Salah.PRAYER_TYPE)) {
			prayerType = Util.getPrayerType(extras.getInt(Salah.PRAYER_TYPE));
		}

		switch (prayerType) {
		case QADHA:
			addSalah.setText(Util.getString(getApplicationContext(),
					R.string.add_qadha));
			break;
		case ADDITIONAL:
			addSalah.setText(Util.getString(getApplicationContext(),
					R.string.add_additional_salah));
			break;
		default:
			break;
		}

	}

	private void setPrayerDate(Date date) {

		Date today = new Date();
		if (date.compareTo(today) > 0) {
			LogUtil.toastShort(getApplicationContext(),
					"Thanks for testing, Please select correct date");
		} else {
			prayerDate.setText(Util.formatDate(date.getTime()));
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
				LogUtil.toastShort(getApplicationContext(),
						"Unknown exception occured while parsing date");
			}
			setPrayerDate(date);
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		super.finish();

		try {
			mCursor.close();
		} catch (Exception ignoreMe) {

		}

		try {
			db.close();
		} catch (Exception ignoreMe) {

		}
	}

	@SuppressWarnings("deprecation")
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
				addAdditionalPrayers(f, z, a, m, i, prayerType);
			}
			break;
		case R.id.selectDate:
			showDialog(DATE_DIALOG_ID);
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

	private boolean addAdditionalPrayers(int f, int z, int a, int m, int i,
			PrayerType type) {

		// String today = Util.formatDate((new Date()).getTime());
		String today = prayerDate.getText().toString();

		mCursor = db.prayer.get(today, type);

		long result = -1;
		if (mCursor == null) {
			try {
				result = db.prayer.add(db.user.getActiveUserId(), today, f, z,
						a, m, i, type);
			} catch (Exception e) {
				LogUtil.toastShort(getApplicationContext(), "Exception: " + e);
			}
		} else if (mCursor.moveToFirst()) {

			int prayerId = mCursor.getInt(mCursor
					.getColumnIndex(T_Prayers.KEY_PRAYER_ID));
			int currentFajrCount = mCursor.getInt(mCursor
					.getColumnIndex(T_Prayers.KEY_FAJR));
			int currentZoharCount = mCursor.getInt(mCursor
					.getColumnIndex(T_Prayers.KEY_ZOHAR));
			int currentAsrCount = mCursor.getInt(mCursor
					.getColumnIndex(T_Prayers.KEY_ASR));
			int currentMagribCount = mCursor.getInt(mCursor
					.getColumnIndex(T_Prayers.KEY_MAGRIB));
			int currentIshaCount = mCursor.getInt(mCursor
					.getColumnIndex(T_Prayers.KEY_ISHA));

			f += currentFajrCount;
			z += currentZoharCount;
			a += currentAsrCount;
			m += currentMagribCount;
			i += currentIshaCount;

			try {
				result = db.prayer.update(prayerId, f, z, a, m, i);
			} catch (Exception e) {
				LogUtil.toastShort(getApplicationContext(), "Exception: " + e);
			}
		}

		if (result == -1) {
			LogUtil.toastShort(getApplicationContext(),
					"Error occurred while adding bulk prayers");
			return false;
		} else {
			LogUtil.toastShort(getApplicationContext(), "Salah added!");
			super.finish();
		}
		return true;
	}

	private boolean validateFields(int f, int z, int a, int m, int i) {
		if (f == 0 && z == 0 && a == 0 && m == 0 && i == 0) {
			LogUtil.toastShort(getApplicationContext(), "Please enter prayers");
			return false;
		}
		return true;
	}
}
