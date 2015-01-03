package com.aaha.alsalah;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.aaha.alsalah.R;
import com.aaha.db.DBAdapter;
import com.aaha.db.DBAdapter.PrayerType;
import com.aaha.util.LogUtil;
import com.aaha.util.Salah;
import com.aaha.util.Util;
import com.actionbarsherlock.app.SherlockFragment;

public class ShowLifetimeCounts extends SherlockFragment {
	DBAdapter db = null;
	TableLayout prayersStatsTable = null;
	Cursor mCursorOnTime, mCursorDelayed, mCursorAdditional,
			mCursorMenstruation;
	TextView message;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_show_prayers_statistics,
				container, false);
		prayersStatsTable = (TableLayout) view
				.findViewById(R.id.prayersStatistics);

		message = (TextView) view.findViewById(R.id.lifetimeCountsMsg);
		message.setText("Numbers here tell you how many Salah you've completed "
				+ "and how many more are pending to be offered\n\n"
				+ "Tip: Offer at least 1 Qadha with every Salah you offer in a day");

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();

		if (prayersStatsTable.getChildCount() > 0) {
			prayersStatsTable.removeAllViews();
		}

		db = new DBAdapter(getSherlockActivity().getApplicationContext());
		db.open();

		try {
			loadPrayersStatistics();
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.toastShort(getSherlockActivity(), "Exception occurred" + e);
		} finally {
			mCursorOnTime.close();
			mCursorDelayed.close();
			mCursorAdditional.close();
			mCursorMenstruation.close();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		db.close();
	}

	private void loadPrayersStatistics() {

		prayersStatsTable.setStretchAllColumns(true);
		prayersStatsTable.addView(getHeaderRow());
		int userId = db.user.getActiveUserId();

		// read on time prayers
		mCursorOnTime = db.prayer.getCounts(PrayerType.ADA, userId);
		if (mCursorOnTime != null)
			mCursorOnTime.moveToFirst();

		// read additional prayers
		mCursorAdditional = db.prayer.getCounts(PrayerType.ADDITIONAL, userId);
		if (mCursorAdditional != null)
			mCursorAdditional.moveToFirst();

		// read delayed prayers
		mCursorDelayed = db.prayer.getCounts(PrayerType.QADHA, userId);
		if (mCursorDelayed != null)
			mCursorDelayed.moveToFirst();

		// read excused prayers (marked as menstrual period)
		mCursorMenstruation = db.menstruation.getCounts(userId);
		if (mCursorMenstruation != null)
			mCursorMenstruation.moveToFirst();

		int[] prayers = new int[] { 1, 2, 3, 4, 5 };
		for (int i : prayers) {
			prayersStatsTable.addView(getPrayerRow(i - 1,
					db.user.getFarzPrayers()));
		}
	}

	public TableRow getHeaderRow() {
		TableRow colHeaders = new TableRow(getActivity()
				.getApplicationContext());
		colHeaders.setBackgroundColor(Util.getHeaderColor());
		String[] headerContent = new String[] { "", "Ada", "Qadha", "Pending" };
		for (String headerCol : headerContent) {
			TextView tv = new TextView(getActivity());
			tv.setText(headerCol);
			colHeaders.addView(tv);
		}
		return colHeaders;
	}

	public TableRow getPrayerRow(int index, int totalPrayers) {
		TableRow row = new TableRow(getActivity().getApplicationContext());
		row.setBackgroundResource(R.drawable.cell_shape);

		TextView rowHeader = new TextView(getActivity());
		rowHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		rowHeader.setPadding(10, 0, 0, 0);

		switch (index) {
		case 0:
			rowHeader.setText(Salah.FAJR);
			break;
		case 1:
			rowHeader.setText(Salah.ZOHAR);
			break;
		case 2:
			rowHeader.setText(Salah.ASR);
			break;
		case 3:
			rowHeader.setText(Salah.MAGRIB);
			break;
		case 4:
			rowHeader.setText(Salah.ISHA);
			break;
		}

		row.addView(rowHeader);

		int ot_count = mCursorOnTime.getInt(index);
		int od_count = mCursorDelayed.getInt(index);
		int oa_count = mCursorAdditional.getInt(index);
		int menustruation_count = mCursorMenstruation.getInt(index);

		int pending = totalPrayers - ot_count - od_count - oa_count
				- menustruation_count;

		for (int i = 0; i < 3; i++) {
			TextView col = new TextView(getActivity());
			col.setBackgroundResource(R.layout.shape_line);
			col.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
			switch (i) {
			case 0:
				col.setTextColor(Color.rgb(0, 128, 0));
				col.setText(String.valueOf(ot_count + oa_count));
				break;
			case 1:
				col.setTextColor(Color.BLUE);
				col.setText(String.valueOf(od_count));
				break;
			case 2:
				col.setTextColor(Color.RED);
				col.setText(String.valueOf(pending));
				break;
			}
			row.addView(col);
		}

		return row;
	}

}
