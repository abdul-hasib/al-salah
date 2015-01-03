package com.aaha.alsalah.ramdhan;

import java.util.Calendar;
import java.util.Date;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.aaha.alsalah.R;
import com.aaha.db.DBAdapter;
import com.aaha.db.DBAdapter.Ramdhan;
import com.aaha.util.Util;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class AddRamdhanDetails extends SherlockFragmentActivity implements
		OnClickListener {
	public int year, month, day, hour, minute;
	static final int DATE_DIALOG_ID = 1;

	DBAdapter db = null;

	Button add, selectDate;
	ListView recentPrayersList = null;
	CheckBox siyam, taraweeh, quran;
	EditText quranJuz;
	TextView ramdhanMessage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ramdhan);

		db = new DBAdapter(getApplicationContext());
		db.open();

		add = (Button) findViewById(R.id.saveRamdhanButton);
		add.setOnClickListener(this);

		selectDate = (Button) findViewById(R.id.selectDate);
		selectDate.setOnClickListener(this);

		siyam = (CheckBox) findViewById(R.id.siyamCheckbox);
		taraweeh = (CheckBox) findViewById(R.id.taraweehCheckbox);
		quran = (CheckBox) findViewById(R.id.quranCheckbox);
		quranJuz = (EditText) findViewById(R.id.quranEditText);
		ramdhanMessage = (TextView) findViewById(R.id.ramdhanMessage);

		quranJuz.setVisibility(View.GONE);
		ramdhanMessage
				.setText("Please add the fastings, taraweeh you complete every ramdhan. Also add the Quran you recite everyday.");

		quran.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					quranJuz.setVisibility(View.VISIBLE);

					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
						quranJuz.setText("How many Juz\'?");
					}

				} else {
					quranJuz.setVisibility(View.GONE);
				}

			}
		});

		setDate(new Date());
	}

	@Override
	protected void onDestroy() {
		db.close();
		super.onDestroy();
	}

	private void setDate(Date date) {

		Date today = new Date();
		if (date.compareTo(today) > 0) {
			Util.Toast(getApplicationContext(),
					"Thanks for testing, Please select correct date");
		} else {
			selectDate.setText(Util.formatDate(date.getTime()));
			setRamdhanSettings();
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
			setDate(date);
		}
	};

	private void setRamdhanSettings() {
		Cursor c = null;
		String day = selectDate.getText().toString();

		long id = db.ramdhan.isExist(day);
		if (id == -1) {
			siyam.setChecked(false);
			taraweeh.setChecked(false);
			quran.setChecked(false);
			quranJuz.setText("");
		} else {
			try {
				c = db.ramdhan.get(day);
				if (c != null) {
					c.moveToFirst();
					siyam.setChecked((c.getInt(c
							.getColumnIndex(Ramdhan.KEY_SIYAM)) == 1));
					taraweeh.setChecked(c.getInt(c
							.getColumnIndex(Ramdhan.KEY_TARAWEEH)) == 1);
					quran.setChecked(c.getInt(c
							.getColumnIndex(Ramdhan.KEY_QURAN)) == 1);
					quranJuz.setText(c.getString(c
							.getColumnIndex(Ramdhan.KEY_QURAN_JUZ)));
				}
			} catch (Exception e) {
				Util.Toast(
						getApplicationContext(),
						"Exception occured while loading existing details"
								+ e.toString());
				e.printStackTrace();
			} finally {
				if (c != null)
					c.close();
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.saveRamdhanButton:
			String day = selectDate.getText().toString();
			long id = db.ramdhan.isExist(day);

			int s = siyam.isChecked() ? 1 : 0;
			int t = taraweeh.isChecked() ? 1 : 0;
			int q = quran.isChecked() ? 1 : 0;

			float qv = 0;
			if (q == 1) {
				String juz = quranJuz.getText().toString();

				try {
					qv = Float.parseFloat(juz);
				} catch (NumberFormatException nfe) {
					Util.Toast(getApplicationContext(),
							"Please enter number of Juz\'");
					return;
				}

			}
			if (id == -1) {
				addRamdhanDetails(day, s, t, q, qv);
			} else {
				updateRamdhanDetails(id, s, t, q, qv);
			}
			AddRamdhanDetails.this.finish();
			break;
		case R.id.selectDate:
			showDialog(DATE_DIALOG_ID);
			break;
		}
	}

	private void addRamdhanDetails(String day, int siyam, int taraweeh,
			int quran, float quranVolume) {

		if (db.ramdhan.add(day, siyam, taraweeh, quran, quranVolume) > 0) {
			Util.Toast(getApplicationContext(), "Details saved!");
		} else {
			Util.Toast(getApplicationContext(), "Error while saving details");
		}
	}

	private void updateRamdhanDetails(long id, int siyam, int taraweeh,
			int quran, float quranVolume) {

		if (db.ramdhan.update(id, siyam, taraweeh, quran, quranVolume) > 0) {
			Util.Toast(getApplicationContext(), "Details updated!");
		} else {
			Util.Toast(getApplicationContext(), "Error while updating details");
		}
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
