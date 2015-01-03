package com.aaha.alsalah;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
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

import com.aaha.alsalah.R;
import com.aaha.alsalah.additional.AddSalah;
import com.aaha.alsalah.additional.EditSalah;
import com.aaha.alsalah.settings.Settings;
import com.aaha.db.DBAdapter;
import com.aaha.db.DBAdapter.PrayerType;
import com.aaha.db.DBAdapter.T_Prayers;
import com.aaha.util.LogUtil;
import com.aaha.util.Salah;
import com.aaha.util.Util;
import com.actionbarsherlock.app.SherlockFragment;

@SuppressWarnings("deprecation")
public class ShowQadha extends SherlockFragment implements OnClickListener {

	ListView salahList = null;

	SimpleCursorAdapter myCursorAdapter;
	Button addSalah;
	TextView message;

	DBAdapter db = null;
	Cursor mCursor = null;

	PrayerType prayerType = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_show_additional_salah,
				container, false);

		addSalah = (Button) view.findViewById(R.id.addAdditionalSalah);
		addSalah.setOnClickListener(this);

		salahList = (ListView) view.findViewById(R.id.additionalSalahList);
		registerForContextMenu(salahList);

		message = (TextView) view.findViewById(R.id.additionalSalahMsg);
		message.setText("Offer the Salah you missed in your life (Qadha) and add them here!!!");
		prayerType = PrayerType.QADHA;

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		db = new DBAdapter(getSherlockActivity().getApplicationContext());
		db.open();
	}

	@Override
	public void onResume() {
		super.onResume();

		try {
			loadQadhaPrayers();
		} catch (Exception e) {
			LogUtil.toastShort(
					getActivity().getApplicationContext(),
					"Exception occured while loading qhaza prayers: "
							+ e.toString());
			e.printStackTrace();
		}
	}

	@Override
	public void onDestroy() {
		try {
			if (mCursor != null) {
				mCursor.close();
			}
		} catch (Exception ignoreMe) {

		}
		super.onDestroy();
	}

	@Override
	public void onStop() {
		try {
			if (mCursor != null) {
				mCursor.close();
			}
		} catch (Exception ignoreMe) {

		}
		try {
			db.close();
		} catch (Exception ignoreMe) {
		}
		super.onStop();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.options_additional_salah, menu);
	}

	private void deletePrayer(final long prayerId) {
		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
		alert.setMessage("Are you sure you want to delete?");
		alert.setCancelable(true);
		alert.setTitle("Confirm");
		alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				db.prayer.delete(prayerId);
				LogUtil.toastShort(getActivity().getApplicationContext(),
						"Salah deleted!");
				myCursorAdapter.runQueryOnBackgroundThread("");
				myCursorAdapter.notifyDataSetChanged();
				onResume();
			}
		});
		alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		AlertDialog alertDialog = alert.create();
		alertDialog.show();
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// Get extra info about list item that was long-pressed
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item
				.getMenuInfo();

		switch (item.getItemId()) {
		case R.id.editSalah:
			Intent i = new Intent(getActivity(), EditSalah.class);
			i.putExtra(Salah.PRAYER_ID, menuInfo.id);
			startActivity(i);
			break;

		case R.id.deleteSalah:
			try {
				deletePrayer(menuInfo.id);
			} catch (Exception e) {
				LogUtil.toastShort(getActivity().getApplicationContext(),
						"Exception while deleting prayer: " + e.toString());
			}
			break;
		}
		return true;
	}

	private void loadQadhaPrayers() {

		mCursor = db.prayer.getPrayers(PrayerType.QADHA);

		String[] databaseColumnNames = new String[] { T_Prayers.KEY_DATE,
				T_Prayers.KEY_FAJR, T_Prayers.KEY_ZOHAR, T_Prayers.KEY_ASR,
				T_Prayers.KEY_MAGRIB, T_Prayers.KEY_ISHA };

		int[] toViewIDs = new int[] { R.id.item_date, R.id.item_fajr,
				R.id.item_zohar, R.id.item_asr, R.id.item_magrib,
				R.id.item_isha };

		myCursorAdapter = new SimpleCursorAdapter(getActivity(),
				R.layout.table_layout_additional, mCursor, databaseColumnNames,
				toViewIDs);

		myCursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

			public boolean setViewValue(View view, Cursor cursor,
					int columnIndex) {

				if (cursor.getColumnName(columnIndex).equalsIgnoreCase(
						T_Prayers.KEY_DATE)) {

					long date = cursor.getLong(columnIndex);
					((TextView) view).setText(Util.formatDate(date * 1000));
					((TextView) view).setTextSize(TypedValue.COMPLEX_UNIT_SP,
							16);

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

					return true;
				}
				return false;
			}
		});
		salahList.setAdapter(myCursorAdapter);
	}

	@Override
	public void onClick(View view) {
		Intent i;
		switch (view.getId()) {
		case R.id.addAdditionalSalah:
			i = new Intent(getActivity(), AddSalah.class);
			i.putExtra(Salah.PRAYER_TYPE, PrayerType.QADHA.getValue());
			startActivity(i);
			break;
		}
	}

}
