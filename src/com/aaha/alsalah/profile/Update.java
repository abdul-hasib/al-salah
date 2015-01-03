package com.aaha.alsalah.profile;

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
import android.widget.Spinner;

import com.aaha.alsalah.R;
import com.aaha.db.DBAdapter;
import com.aaha.db.DBAdapter.User;
import com.aaha.util.LogUtil;
import com.aaha.util.Util;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class Update extends SherlockFragmentActivity implements OnClickListener {

	DBAdapter db;
	Cursor mCursor = null;

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

		loadCurrentUser();
	}

	public void loadCurrentUser() {

		mCursor = db.user.get();
		if (mCursor == null) {
			LogUtil.toastShort(getApplicationContext(),
					"Error occurred while loading user profile");
			return;

		}
		mCursor.moveToFirst();

		eUsername.setText(mCursor.getString(mCursor
				.getColumnIndex(User.KEY_NAME)));
		ePassword.setText(mCursor.getString(mCursor
				.getColumnIndex(User.KEY_PASSWORD)));
		bSelectDate.setText(mCursor.getString(mCursor
				.getColumnIndex(User.KEY_BIRTHDATE)));

		int pubage = mCursor.getInt(mCursor.getColumnIndex(User.KEY_PUBAGE));
		for (int pos = 0; pos < sPubAgeSpinner.getCount(); pos++) {
			if (String.valueOf(sPubAgeSpinner.getItemAtPosition(pos)).equals(
					String.valueOf(pubage))) {
				sPubAgeSpinner.setSelection(pos);
				break;
			}
		}
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
			String username = eUsername.getText().toString().trim();
			String password = ePassword.getText().toString();
			String dob = bSelectDate.getText().toString();
			String pub_age = String.valueOf(sPubAgeSpinner.getSelectedItem());
			int pubAge = Integer.parseInt(pub_age);

			if (validateFields(username, password, dob)) {
				db.user.updateUser(db.user.getActiveUserId(), username, dob,
						pubAge, password);
				LogUtil.toastShort(Update.this, username + ", your profile is updated");
				super.finish();
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
			LogUtil.toastShort(getApplicationContext(), "Please select date of birth");
			return false;
		}

		if (sPubAgeSpinner.getSelectedItemPosition() == 0) {
			LogUtil.toastShort(getApplicationContext(), "Please select pubert age");
			return false;
		}

		return true;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_DIALOG_ID:
			String dob = bSelectDate.getText().toString();
			Calendar c = Calendar.getInstance();
			Date date = Util.parseDate(dob);
			c.setTime(date);
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
		try {
			mCursor.close();
		} catch (Exception ignoreMe) {

		}
		try {
			db.close();
		} catch (Exception ignoreMe) {

		}
		super.onDestroy();
	}
}
