package com.aaha.alsalah.profile;

import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.aaha.alsalah.Home;
import com.aaha.alsalah.R;
import com.aaha.db.DBAdapter;
import com.aaha.util.Alarm;
import com.aaha.util.LogUtil;
import com.aaha.util.Util;

public class Login extends Activity implements OnClickListener {

	DBAdapter db;
	EditText eUsername, ePassword;
	Button bLogin, bRegister;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		db = new DBAdapter(getApplicationContext());
		db.open();

		bLogin = (Button) findViewById(R.id.loginButton);
		bRegister = (Button) findViewById(R.id.registerButton);
		bLogin.setOnClickListener(this);
		bRegister.setOnClickListener(this);

		eUsername = (EditText) findViewById(R.id.usernameText);
		ePassword = (EditText) findViewById(R.id.passwordText);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// once the registration is complete
		// let it auto login with the registered user
		int userId = db.user.getActiveUserId();
		if (userId > -1) {
			startHomePage();
		}
	}

	public boolean login(String username, String password) {

		if (!validateFields(username, password)) {
			return false;
		}

		try {
			int userId = db.user.isAuthorizedUser(username, password);

			if (userId > -1) {
				if (!db.user.setActiveUserId(userId)) {
					LogUtil.toastShort(getApplicationContext(),
							"Error occured while remembering user");
				}
				LogUtil.toastShort(getApplicationContext(), "Welcome "
						+ username);
				// Settings.putPref(Settings.PREF_PROFILE_USERNAME, username,
				// getApplicationContext());
				// Settings.putPref(Settings.PREF_PROFILE_PASSWORD, password,
				// getApplicationContext());
				return true;

			} else {
				LogUtil.toastShort(getApplicationContext(),
						"Invalid username or password");
			}
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(),
					"Exception occured while authenticating" + e.toString(),
					Toast.LENGTH_LONG).show();
			LogUtil.e("Exception occured: " + e.toString());
		}
		return false;

	}

	private boolean validateFields(String username, String password) {

		if (Util.isEmptyString(username)) {
			Toast.makeText(getApplicationContext(), "Please enter username",
					Toast.LENGTH_SHORT).show();
			return false;
		}
		if (Util.isEmptyString(password)) {
			Toast.makeText(getApplicationContext(), "Please enter password",
					Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}

	@Override
	protected void onDestroy() {
		db.close();
		super.onDestroy();
	}

	private void startHomePage() {
		Intent i = new Intent(this, Home.class);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			if (extras.getBoolean(Alarm.REDIRECT, false)) {
				i.putExtra(Alarm.REDIRECT, true);
			}
		}

		startActivity(i);
		finish();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.loginButton:
			if (login(
					eUsername.getText().toString().trim()
							.toLowerCase(Locale.ENGLISH), ePassword.getText()
							.toString())) {
				startHomePage();
			}
			break;
		case R.id.registerButton:
			Intent i = new Intent(this, Register.class);
			startActivity(i);
			break;
		}
	}
}
