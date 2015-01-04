package com.aaha.alsalah;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.aaha.alsalah.R;
import com.aaha.alsalah.ada.AddAda;
import com.aaha.alsalah.backup.BackupRestore;
import com.aaha.alsalah.notification.PrayerReminder;
import com.aaha.alsalah.profile.Login;
import com.aaha.alsalah.profile.Update;
import com.aaha.alsalah.settings.Settings;
import com.aaha.alsalah.tasbeeh.TasbeehHome;
import com.aaha.db.DBAdapter;
import com.aaha.util.Alarm;
import com.aaha.util.RateApp;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class Home extends SherlockFragmentActivity {
	DBAdapter db = null;
	ActionBar actionbar;
	ActionBar.Tab dailyTab, statisticsTab, qadhaTab, additionalTab, ramdhanTab;
	Fragment dailyFragment, qadhaFragment, statisticsFragment,
			additionalFragment, ramdhanFragment;

	boolean isFromNotification = false;
	boolean redirectToAddDailyPrayer = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		/*
		 * Activity will have extras only if it is called from notification
		 */
		Bundle extras = getIntent().getExtras();
		if (extras != null) {

			// if the activity is started from notification service
			if (extras.getBoolean(Alarm.CANCEL, false)) {
				isFromNotification = true;
			}

			// if the activity is redirected from login activity. This
			// happens only when user was not logged in
			if (extras.getBoolean(Alarm.REDIRECT, false)) {
				redirectToAddDailyPrayer = true;
			}
		}

		/*
		 * Login if not already logged in
		 */
		db = new DBAdapter(getApplicationContext());
		db.open();

		int userId = db.user.getActiveUserId();
		if (userId == -1) {
			Intent i = new Intent(this, Login.class);
			if (isFromNotification) {
				// if the request has come from notification, set redirect flag
				i.putExtra(Alarm.REDIRECT, true);
			}
			startActivity(i);
			finish();
		} else if (isFromNotification) {
			// if user is already logged in and request has come from
			// notification then, set redirect flag
			redirectToAddDailyPrayer = true;
		}

		// Clear all the notifications
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancelAll();

		/*
		 * redirect from Home page to AddDailyPrayer page if the activity is
		 * started from notification.
		 */
		if (redirectToAddDailyPrayer) {
			Intent i = new Intent(this, AddAda.class);
			startActivity(i);
			finish();
		}

		// Monitor launch times and interval from installation
		RateApp.onStart(this);
		// Show a dialog if criteria is satisfied
		RateApp.showRateDialogIfNeeded(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		actionbar = getSupportActionBar();
		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionbar.setTitle(getResources().getString(R.string.app_name));

		dailyTab = actionbar.newTab().setText("Daily");
		statisticsTab = actionbar.newTab().setText("Lifetime");
		qadhaTab = actionbar.newTab().setText(
				getResources().getString(R.string.tab_qadha));
		additionalTab = actionbar.newTab().setText(
				getResources().getString(R.string.tab_additional));
		ramdhanTab = actionbar.newTab().setText(
				getResources().getString(R.string.tab_ramdhan));

		dailyFragment = new ShowDaily();
		statisticsFragment = new ShowLifetimeCounts();
		qadhaFragment = new ShowQadha();
		additionalFragment = new ShowAdditional();
		ramdhanFragment = new ShowRamdhan();

		dailyTab.setTabListener(new MyTabsListener(dailyFragment));
		statisticsTab.setTabListener(new MyTabsListener(statisticsFragment));
		qadhaTab.setTabListener(new MyTabsListener(qadhaFragment));
		additionalTab.setTabListener(new MyTabsListener(additionalFragment));
		ramdhanTab.setTabListener(new MyTabsListener(ramdhanFragment));

		if (actionbar.getTabCount() == 0) {
			actionbar.addTab(dailyTab);
			actionbar.addTab(statisticsTab);
			actionbar.addTab(qadhaTab);
			actionbar.addTab(additionalTab);
			actionbar.addTab(ramdhanTab);
		}

		hideTab(qadhaTab, getResources().getString(R.string.tab_qadha));
		hideTab(additionalTab, getResources()
				.getString(R.string.tab_additional));
		hideTab(ramdhanTab, getResources().getString(R.string.tab_ramdhan));

	}

	private void hideTab(ActionBar.Tab tab, String tabName) {
		String values = Settings.getString(Settings.PREF_HIDE_TABS,
				getApplicationContext(), "");
		int pos = isTabExist(tab);
		if (values.contains(tabName)) {
			if (pos > -1) {
				actionbar.removeTabAt(pos);
			}
		} else {
			if (pos == -1) {
				actionbar.addTab(tab);
			}
		}
	}

	private int isTabExist(Tab tab) {
		for (int index = 0; index < actionbar.getTabCount(); index++) {
			Tab currentTab = actionbar.getTabAt(index);

			if (currentTab == null)
				return -1;

			if (currentTab.getText().toString()
					.equalsIgnoreCase(tab.getText().toString())) {
				return index;
			}
		}
		return -1;
	}

	@Override
	protected void onDestroy() {
		db.close();
		// Util.setSplashScreenState(true);
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.options_menu_home, menu);
		return true;
	}

	private void signout() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to sign out?")
				.setCancelable(true)
				.setTitle("Confirm")
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								db.user.removeActiveUserId();
								finish();
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i;
		switch (item.getItemId()) {
		case R.id.menu_signout:
			signout();
			break;
		case R.id.menu_update_profile:
			i = new Intent(this, Update.class);
			startActivity(i);
			break;
		case R.id.menu_backup:
			i = new Intent(this, BackupRestore.class);
			startActivity(i);
			break;
		case R.id.menu_tasbeeh:
			i = new Intent(this, TasbeehHome.class);
			startActivity(i);
			break;
		case R.id.menu_notification:
			i = new Intent(this, PrayerReminder.class);
			startActivity(i);
			break;
		case R.id.menu_settings:
			i = new Intent(this, Settings.class);
			startActivity(i);
			break;
		}
		return true;
	}

	class MyTabsListener implements ActionBar.TabListener {
		public Fragment fragment;

		public MyTabsListener(Fragment fragment) {
			this.fragment = fragment;
		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			ft.replace(R.id.fragment_container, Fragment.instantiate(Home.this,
					fragment.getClass().getName()));
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			// ft.replace(R.id.fragment_container, fragment);
		}
	}

}