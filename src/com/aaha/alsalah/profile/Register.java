package com.aaha.alsalah.profile;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.aaha.alsalah.R;
import com.aaha.db.DBAdapter;
import com.aaha.db.DBAdapter.PrayerType;
import com.aaha.util.LogUtil;
import com.aaha.util.Util;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class Register extends SherlockFragmentActivity implements
		OnClickListener {

	DBAdapter db;
	static final int DATE_DIALOG_ID = 1;
	public int year, month, day, hour, minute;

	Button bSubmit, bSelectDate;
	EditText eUsername, ePassword;
	Spinner sPubAgeSpinner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		db = new DBAdapter(this);
		db.open();

		bSubmit = (Button) findViewById(R.id.createUser);
		bSubmit.setOnClickListener(this);

		bSelectDate = (Button) findViewById(R.id.selectDateButton);
		bSelectDate.setOnClickListener(this);

		eUsername = (EditText) findViewById(R.id.usernameText);
		ePassword = (EditText) findViewById(R.id.passwordText);
		sPubAgeSpinner = (Spinner) findViewById(R.id.pubAgeSpinner);
	}

	private void setDateOfBirth(Date date) {

		Date today = new Date();
		if (date.compareTo(today) > 0) {
			LogUtil.toastShort(getApplicationContext(),
					"Thanks for testing, Please select the correct date or birth");
		} else {
			bSelectDate.setText(Util.formatDate(date.getTime()));
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
			setDateOfBirth(date);
		}
	};

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.createUser:
			String username = eUsername.getText().toString().trim()
					.toLowerCase(Locale.ENGLISH);
			String password = ePassword.getText().toString();
			String dob = bSelectDate.getText().toString();

			if (validateFields(username, password, dob)) {

				String pub_age = String.valueOf(sPubAgeSpinner
						.getSelectedItem());
				int pubAge = Integer.parseInt(pub_age);

				if (db.user.isExist(username)) {
					Toast.makeText(
							Register.this,
							"User name is not available, try with different name",
							Toast.LENGTH_LONG).show();
				} else {
					long userId = db.user.add(username, password, dob, pubAge);
					db.prayer.add(userId,
							Util.formatDate((new Date()).getTime()), 0, 0, 0,
							0, 0, PrayerType.ADA);
					db.user.setActiveUserId(userId);
					LogUtil.toastLong(Register.this, "Assalamualaikum "
							+ eUsername.getText().toString().trim()
							+ ", Your profile is created");

					super.finish();
				}
			}
			break;
		case R.id.selectDateButton:
			showDialog(DATE_DIALOG_ID);
			break;
		case R.id.pubAgeSpinner:
			sPubAgeSpinner.removeViewAt(0);
			break;
		}
	}

	private boolean validateFields(String username, String password, String dob) {
		if (Util.isEmptyString(username)) {
			LogUtil.toastShort(getApplicationContext(), "Please enter username");
			return false;
		}

		if (Util.isEmptyString(password)) {
			LogUtil.toastShort(getApplicationContext(), "Please enter password");
			return false;
		}

		if (dob.equalsIgnoreCase(getResources().getString(R.string.dob))) {
			LogUtil.toastShort(getApplicationContext(),
					"Please select date of birth");
			return false;
		}

		if (sPubAgeSpinner.getSelectedItemPosition() == 0) {
			LogUtil.toastShort(getApplicationContext(),
					"Please select pubert age");
			return false;
		}

		return true;
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

	@Override
	protected void onDestroy() {
		db.close();
		super.onDestroy();
	}
}
