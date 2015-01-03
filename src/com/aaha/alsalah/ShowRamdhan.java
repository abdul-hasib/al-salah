package com.aaha.alsalah;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.aaha.alsalah.R;
import com.aaha.alsalah.ramdhan.AddRamdhanDetails;
import com.aaha.alsalah.ramdhan.EditRamdhanDetails;
import com.aaha.alsalah.settings.Settings;
import com.aaha.db.DBAdapter;
import com.aaha.db.DBAdapter.T_Ramdhan;
import com.aaha.util.LogUtil;
import com.aaha.util.Util;
import com.actionbarsherlock.app.SherlockFragment;

@SuppressWarnings("deprecation")
public class ShowRamdhan extends SherlockFragment implements OnClickListener {

	ListView fastList = null;

	SimpleCursorAdapter myCursorAdapter;
	Button add;
	ProgressBar quranReciteProgress;
	TextView quranRevision, quranPercentage, quranTotal;

	DBAdapter db = null;
	Cursor mCursor = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_show_ramdhan_details,
				container, false);

		add = (Button) view.findViewById(R.id.addRamdhanDetails);
		add.setOnClickListener(this);

		fastList = (ListView) view.findViewById(R.id.ramdhanDetailsList);
		registerForContextMenu(fastList);

		quranRevision = (TextView) view.findViewById(R.id.quranRevision);
		quranPercentage = (TextView) view.findViewById(R.id.quranPercentage);
		quranTotal = (TextView) view.findViewById(R.id.quranTotal);

		quranReciteProgress = (ProgressBar) view
				.findViewById(R.id.quranProgressBar);

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
			loadRamdhanDetails();
		} catch (Exception e) {
			LogUtil.toastShort(
					getActivity().getApplicationContext(),
					"Exception occured while loading ramdhan details: "
							+ e.toString());
			e.printStackTrace();
		}

		try {
			setProgressBar();
		} catch (Exception e) {
			LogUtil.toastShort(
					getActivity().getApplicationContext(),
					"Exception occured while loading quran progress: "
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
		inflater.inflate(R.menu.options_ramdhan_details, menu);
	}

	private void deleteRamdhanDetaisl(final long ramdhanId) {
		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
		alert.setMessage("Are you sure you want to delete?");
		alert.setCancelable(true);
		alert.setTitle("Confirm");
		alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				db.ramdhan.delete(ramdhanId);
				LogUtil.toastShort(getActivity().getApplicationContext(),
						"Details deleted!");
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
		case R.id.editRamdhanDetails:
			Intent i = new Intent(getActivity(), EditRamdhanDetails.class);
			i.putExtra(T_Ramdhan.KEY_ID, menuInfo.id);
			startActivity(i);
			break;

		case R.id.deleteRamdhanDetails:
			try {
				deleteRamdhanDetaisl(menuInfo.id);
			} catch (Exception e) {
				LogUtil.toastShort(getActivity().getApplicationContext(),
						"Exception while deleting details: " + e.toString());
			}
			break;
		}
		return true;
	}

	private void setProgressBar() {
		final int quranjuz = 30;
		float vol = db.ramdhan.getTotalQuranJuz();
		float percentage = ((vol % quranjuz) / quranjuz) * 100;
		quranReciteProgress.setProgress((int) percentage);

		int revision = ((int) (vol / quranjuz)) + 1;
		quranRevision.setText("#" + String.valueOf(revision));
		quranPercentage.setText(String.valueOf((int) percentage) + "%");
		quranTotal.setText(String.valueOf(vol % quranjuz));
	}

	private void loadRamdhanDetails() {

		mCursor = db.ramdhan.get();

		String[] databaseColumnNames = new String[] { T_Ramdhan.KEY_DATE,
				T_Ramdhan.KEY_SIYAM, T_Ramdhan.KEY_TARAWEEH, T_Ramdhan.KEY_QURAN_JUZ };

		int[] toViewIDs = new int[] { R.id.item_date, R.id.item_siyam,
				R.id.item_taraweeh, R.id.item_quran_vol };

		myCursorAdapter = new SimpleCursorAdapter(getActivity(),
				R.layout.table_layout_ramdhan_details, mCursor,
				databaseColumnNames, toViewIDs);

		myCursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

			public boolean setViewValue(View view, Cursor cursor,
					int columnIndex) {

				if (cursor.getColumnName(columnIndex).equalsIgnoreCase(
						T_Ramdhan.KEY_DATE)) {

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

				if (cursor.getColumnName(columnIndex).equalsIgnoreCase(
						T_Ramdhan.KEY_TARAWEEH)
						|| cursor.getColumnName(columnIndex).equalsIgnoreCase(
								T_Ramdhan.KEY_SIYAM)) {
					if (cursor.getInt(columnIndex) == 1) {
						((TextView) view).setTextColor(Color.rgb(0, 128, 0));
						((TextView) view).setText("Yes");
						((TextView) view).setTextSize(
								TypedValue.COMPLEX_UNIT_SP, 16);
					}
					if (cursor.getInt(columnIndex) == 0) {
						((TextView) view).setTextSize(
								TypedValue.COMPLEX_UNIT_SP, 16);
						((TextView) view).setText("No");
						((TextView) view).setTextColor(Color.RED);
					}
					return true;
				}

				return false;
			}
		});
		fastList.setAdapter(myCursorAdapter);
	}

	@Override
	public void onClick(View view) {
		Intent i;
		switch (view.getId()) {
		case R.id.addRamdhanDetails:
			i = new Intent(getActivity(), AddRamdhanDetails.class);
			startActivity(i);
			break;
		}
	}

}
