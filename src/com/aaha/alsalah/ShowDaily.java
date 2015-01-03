package com.aaha.alsalah;

import java.util.Date;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.aaha.alsalah.ada.AddAda;
import com.aaha.alsalah.ada.EditAda;
import com.aaha.alsalah.additional.AddSalah;
import com.aaha.alsalah.additional.EditSalah;
import com.aaha.alsalah.menstruation.SetMenstruation;
import com.aaha.alsalah.qasr.SetQasr;
import com.aaha.alsalah.settings.Settings;
import com.aaha.db.DBAdapter;
import com.aaha.db.DBAdapter.PrayerType;
import com.aaha.db.DBAdapter.Prayers;
import com.aaha.util.Salah;
import com.aaha.util.Util;
import com.actionbarsherlock.app.SherlockFragment;

public class ShowDaily extends SherlockFragment implements OnClickListener {

	DBAdapter db = null;
	private Cursor mCursor = null;

	Button addAda;
	ListView recentPrayersList = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_show_daily_prayers,
				container, false);

		addAda = (Button) view.findViewById(R.id.addDailySalah);
		addAda.setOnClickListener(this);

		recentPrayersList = (ListView) view.findViewById(R.id.recentPrayers);
		registerForContextMenu(recentPrayersList);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onStart() {
		super.onStart();

		db = new DBAdapter(getSherlockActivity().getApplicationContext());
		db.open();
		
		try {
			addRecentMissingSalah();
			loadRecentPrayers();
		} catch (Exception e) {
			e.printStackTrace();
			Util.Toast(getActivity(), "Exception occurred:" + e);
		}
	}

	@Override
	public void onStop() {
		try {
			mCursor.close();
		} catch (Exception ignoreMe) {
		}

		try {
			db.close();
		} catch (Exception ignoreMe) {
		}
		super.onStop();
	}

	private void addRecentMissingSalah() {
		long lastDate = db.prayer.getMostRecentSalahDate(PrayerType.ADA);
		long today = (new Date()).getTime() / 1000;
		long day = 60 * 60 * 24;

		int totalDays = Math.round((today - lastDate) / day);

		for (int i = 0; i < totalDays; i++) {
			lastDate = (lastDate + day);
			String nextDay = Util.formatDate(lastDate * 1000);
			db.prayer.add(db.user.getActiveUserId(), nextDay, 0, 0, 0, 0, 0,
					PrayerType.ADA);
		}
	}

	@SuppressWarnings("deprecation")
	private void loadRecentPrayers() {
		int limit = 0;
		try {
			limit = Settings.getInt(Settings.PREF_DAILY_SALAH_LIMIT,
					getActivity().getApplicationContext(), 0);
		} catch (Exception e) {
			e.printStackTrace();
			Util.Toast(getActivity(), "Exception while retrieving limit:" + e);
		}

		boolean hidePerfectDays = false;
		try {
			hidePerfectDays = Settings.getBoolean(
					Settings.PREF_HIDE_PERFECT_DAYS, getActivity()
							.getApplicationContext(), false);
		} catch (Exception e) {
			e.printStackTrace();
			Util.Toast(getActivity(),
					"Exception while retrieving perfect day settings:" + e);
		}

		if (limit > 0) {
			mCursor = db.prayer.getPrayers(PrayerType.ADA, limit,
					hidePerfectDays);
		} else {
			mCursor = db.prayer.getPrayers(PrayerType.ADA, hidePerfectDays);
		}

		String[] databaseColumnNames = new String[] { Prayers.KEY_DATE,
				Prayers.KEY_FAJR, Prayers.KEY_ZOHAR, Prayers.KEY_ASR,
				Prayers.KEY_MAGRIB, Prayers.KEY_ISHA };

		int[] toViewIDs = new int[] { R.id.item_date, R.id.item_fajr,
				R.id.item_zohar, R.id.item_asr, R.id.item_magrib,
				R.id.item_isha };

		SimpleCursorAdapter mCursorAdapter = new SimpleCursorAdapter(
				getActivity(), R.layout.table_layout_recent, mCursor,
				databaseColumnNames, toViewIDs);

		mCursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

			public boolean setViewValue(View view, Cursor cursor,
					int columnIndex) {

				long date = cursor.getLong(cursor
						.getColumnIndex(Prayers.KEY_DATE));

				if (cursor.getColumnName(columnIndex).equalsIgnoreCase(
						Prayers.KEY_DATE)) {

					((TextView) view).setText(Util.formatDate(date * 1000));

					boolean highlight = Settings
							.getBoolean(Settings.PREF_HIGHLIGHT_FRIDAY,
									getActivity(), true);
					if (highlight) {
						if (Util.isFriday(date * 1000)) {
							((TextView) view).setTypeface(null, Typeface.BOLD);
						} else {
							((TextView) view)
									.setTypeface(null, Typeface.NORMAL);
						}
					}

					((TextView) view).setTextSize(TypedValue.COMPLEX_UNIT_SP,
							16);
					return true;
				}

				if (cursor.getInt(columnIndex) == 0) {
					((TextView) view).setTextSize(TypedValue.COMPLEX_UNIT_SP,
							16);
					((TextView) view).setText("No");

					if (getQadhaPrayerCount(view, cursor) > 0) {
						((TextView) view).setText("Yes");
						((TextView) view).setTextColor(Color.BLUE);
					} else if (isExcusedPrayer(view, cursor)) {
						((TextView) view).setText("-");
						((TextView) view).setTextColor(Color.BLACK);
					} else if (isQasrPrayer(view, cursor)) {
						((TextView) view).setTextColor(Color.MAGENTA);
					} else {
						((TextView) view).setTextColor(Color.RED);
					}

					return true;
				}

				if (cursor.getInt(columnIndex) == 1) {
					((TextView) view).setTextColor(Color.rgb(0, 128, 0));
					((TextView) view).setText("Yes");
					((TextView) view).setTextSize(TypedValue.COMPLEX_UNIT_SP,
							16);
					return true;
				}

				return false;
			}
		});

		recentPrayersList.setAdapter(mCursorAdapter);
	}

	private int getQadhaPrayerCount(View view, Cursor cursor) {
		int qadhaPrayerCount = 0;

		long date = cursor.getLong(cursor.getColumnIndex(Prayers.KEY_DATE));

		Cursor c = db.prayer
				.get(Util.formatDate(date * 1000), PrayerType.QADHA);

		if (c != null) {
			if (c.moveToFirst()) {
				switch (view.getId()) {
				case R.id.item_fajr:
					qadhaPrayerCount = c.getInt(c
							.getColumnIndex(Prayers.KEY_FAJR));
					break;
				case R.id.item_zohar:
					qadhaPrayerCount = c.getInt(c
							.getColumnIndex(Prayers.KEY_ZOHAR));
					break;
				case R.id.item_asr:
					qadhaPrayerCount = c.getInt(c
							.getColumnIndex(Prayers.KEY_ASR));
					break;
				case R.id.item_magrib:
					qadhaPrayerCount = c.getInt(c
							.getColumnIndex(Prayers.KEY_MAGRIB));
					break;
				case R.id.item_isha:
					qadhaPrayerCount = c.getInt(c
							.getColumnIndex(Prayers.KEY_ISHA));
					break;
				}
			}
			c.close();
		}
		return qadhaPrayerCount;
	}

	private boolean isQasrPrayer(View view, Cursor cursor) {
		boolean isQasrPrayer = false;

		long prayerId = cursor.getLong(cursor
				.getColumnIndex(Prayers.KEY_PRAYER_ID));

		Cursor c = db.qasr.get(prayerId);

		if (c != null) {
			if (c.moveToFirst()) {
				switch (view.getId()) {
				case R.id.item_fajr:
					isQasrPrayer = c.getInt(c.getColumnIndex(Prayers.KEY_FAJR)) == 1;
					break;
				case R.id.item_zohar:
					isQasrPrayer = c
							.getInt(c.getColumnIndex(Prayers.KEY_ZOHAR)) == 1;
					break;
				case R.id.item_asr:
					isQasrPrayer = c.getInt(c.getColumnIndex(Prayers.KEY_ASR)) == 1;
					break;
				case R.id.item_magrib:
					isQasrPrayer = c.getInt(c
							.getColumnIndex(Prayers.KEY_MAGRIB)) == 1;
					break;
				case R.id.item_isha:
					isQasrPrayer = c.getInt(c.getColumnIndex(Prayers.KEY_ISHA)) == 1;
					break;
				}
			}
			c.close();
		}

		return isQasrPrayer;
	}

	private boolean isExcusedPrayer(View view, Cursor cursor) {

		if (Settings.getInt(Settings.PREF_PROFILE_GENDER, getActivity(),
				Settings.Gender.MALE) == Settings.Gender.MALE) {
			return false;
		}

		boolean isExcusedPrayer = false;

		long prayerId = cursor.getLong(cursor
				.getColumnIndex(Prayers.KEY_PRAYER_ID));

		Cursor c = db.menstruation.get(prayerId);

		if (c != null) {
			if (c.moveToFirst()) {
				switch (view.getId()) {
				case R.id.item_fajr:
					isExcusedPrayer = c.getInt(c
							.getColumnIndex(Prayers.KEY_FAJR)) == 1;
					break;
				case R.id.item_zohar:
					isExcusedPrayer = c.getInt(c
							.getColumnIndex(Prayers.KEY_ZOHAR)) == 1;
					break;
				case R.id.item_asr:
					isExcusedPrayer = c.getInt(c
							.getColumnIndex(Prayers.KEY_ASR)) == 1;
					break;
				case R.id.item_magrib:
					isExcusedPrayer = c.getInt(c
							.getColumnIndex(Prayers.KEY_MAGRIB)) == 1;
					break;
				case R.id.item_isha:
					isExcusedPrayer = c.getInt(c
							.getColumnIndex(Prayers.KEY_ISHA)) == 1;
					break;
				}
			}
			c.close();
		}

		return isExcusedPrayer;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getActivity().getMenuInflater();

		int gender = Integer.valueOf(Settings.getString(
				Settings.PREF_PROFILE_GENDER, getActivity(), "1"));
		if (gender == 1) {
			inflater.inflate(R.menu.options_daily_prayers_male, menu);
		} else {
			inflater.inflate(R.menu.options_daily_prayers_female, menu);

		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item
				.getMenuInfo();

		switch (item.getItemId()) {
		case R.id.menu_ada:
			Intent i = new Intent(getActivity(), EditAda.class);
			i.putExtra(Salah.PRAYER_ID, menuInfo.id);
			startActivity(i);
			break;
		case R.id.menu_qasr:
			i = new Intent(getActivity(), SetQasr.class);
			i.putExtra(Salah.PRAYER_ID, menuInfo.id);
			startActivity(i);
			break;
		case R.id.menu_menstruation:
			i = new Intent(getActivity(), SetMenstruation.class);
			i.putExtra(Salah.PRAYER_ID, menuInfo.id);
			startActivity(i);
			break;
		case R.id.menu_qadha:
			long date = db.prayer.getDate(menuInfo.id);
			String prayerDate = Util.formatDate(date * 1000);

			long prayerId = db.prayer.isExist(prayerDate, PrayerType.QADHA);

			if (prayerId == -1) {
				i = new Intent(getActivity(), AddSalah.class);
				i.putExtra(Salah.PRAYER_DATE, date);
			} else {
				i = new Intent(getActivity(), EditSalah.class);
				i.putExtra(Salah.PRAYER_ID, prayerId);
			}

			i.putExtra(Salah.PRAYER_TYPE, PrayerType.QADHA.getValue());
			startActivity(i);
			break;
		}
		return true;
	}

	@Override
	public void onClick(View view) {
		Intent i;
		switch (view.getId()) {
		case R.id.addDailySalah:
			i = new Intent(getActivity(), AddAda.class);
			startActivity(i);
			break;
		}
	}

}
