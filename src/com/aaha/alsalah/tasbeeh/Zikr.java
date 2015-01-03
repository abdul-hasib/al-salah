package com.aaha.alsalah.tasbeeh;

import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aaha.alsalah.R;
import com.aaha.db.DBAdapter;
import com.aaha.db.DBAdapter.T_Tasbeeh;
import com.aaha.db.DBAdapter.T_TasbeehCount;
import com.aaha.util.LogUtil;
import com.aaha.util.Util;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class Zikr extends SherlockFragmentActivity {

	DBAdapter db;
	Cursor mCursor = null;
	TextView txtTasbeeh, txtMeaning;
	TextView txtTodaysCount, txtOverallCount;
	Button btnZikr;
	ProgressBar progressBar;
	Toast toast = null;
	int tasbeehId;
	int currentCount = 0, defaultCount = 0;
	int todaysCount = 0, overallCount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tasbeeh_zikr);

		txtTasbeeh = (TextView) findViewById(R.id.txtTasbeeh);
		txtMeaning = (TextView) findViewById(R.id.txtTasbeehMeaning);
		txtTodaysCount = (TextView) findViewById(R.id.txtTasbeehTodaysCount);
		txtOverallCount = (TextView) findViewById(R.id.txtTasbeehOverallCount);

		progressBar = (ProgressBar) findViewById(R.id.tasbeehProgressBar);

		db = new DBAdapter(getApplicationContext());
		db.open();

		Bundle extras = getIntent().getExtras();
		tasbeehId = (int) extras.getLong("TASBEEH_ID");

		progressBar.setProgress(0);
		try {
			loadTasbeeh(tasbeehId);
			loadTasbeehCount(tasbeehId);
		} catch (Exception e) {
			LogUtil.toastShort(getApplicationContext(),
					"Error occureed while loading Tasbeeh: " + e);
			e.printStackTrace();
		}
	}

	private void loadTasbeeh(long tasbeehId) {
		Cursor mCursor = db.tasbeeh.getTasbeeh(tasbeehId);
		if (mCursor == null) {
			LogUtil.toastShort(getApplicationContext(),
					"Error occured while loading tasbeeh, please try again");
		} else {
			if (mCursor.moveToFirst()) {
				txtTasbeeh.setText(mCursor.getString(mCursor
						.getColumnIndex(T_Tasbeeh.KEY_TASBEEH)));
				txtMeaning.setText(mCursor.getString(mCursor
						.getColumnIndex(T_Tasbeeh.KEY_MEANING)));

				defaultCount = mCursor.getInt(mCursor
						.getColumnIndex(T_Tasbeeh.KEY_DEFAULT_COUNT));
				progressBar.setMax(defaultCount);
			}
			mCursor.close();
		}
	}

	public void loadTasbeehCount(long tasbeehId) {
		mCursor = null;
		mCursor = db.tasbeehCount.get(tasbeehId);
		if (mCursor == null) {
			return;
		}
		if (mCursor.moveToFirst()) {
			String today = Util.formatDate((new Date()).getTime());
			String day = mCursor.getString(mCursor
					.getColumnIndex(T_TasbeehCount.KEY_DATE));
			if (day.equals(today)) {
				todaysCount = mCursor.getInt(mCursor
						.getColumnIndex(T_TasbeehCount.KEY_COUNT_TODAY));
			} else {
				todaysCount = 0;
			}

			overallCount = mCursor.getInt(mCursor
					.getColumnIndex(T_TasbeehCount.KEY_COUNT_OVERALL));

			txtTodaysCount.setText(String.valueOf(todaysCount));
			txtOverallCount.setText(String.valueOf(overallCount));
		}
		mCursor.close();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		super.finish();
	}

	@Override
	protected void onDestroy() {
		if (currentCount > 0) {
			Log.d(DBAdapter.TAG, "Current Count: " + currentCount);
			saveTasbeehCount();
		}
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

	private void saveTasbeehCount() {
		String today = Util.formatDate((new Date()).getTime());
		mCursor = null;
		mCursor = db.tasbeehCount.get(tasbeehId);
		if (mCursor == null) {
			LogUtil.toastShort(getApplicationContext(),
					"Error occurred while saving tasbeeh");
			return;
		}
		if (mCursor.moveToFirst()) {
			db.tasbeehCount.update(today, tasbeehId, todaysCount, overallCount);
		} else {
			db.tasbeehCount.add(today, tasbeehId, todaysCount, overallCount);
		}
		mCursor.close();
	}

	public void addTasbeeh(View view) {
		currentCount++;
		todaysCount++;
		overallCount++;

		if (currentCount > defaultCount) {
			return;
		}

		String msg = String.valueOf(currentCount) + " / "
				+ String.valueOf(defaultCount);

		if (toast != null) {
			toast.setText(msg);
		} else {
			toast = Toast.makeText(getApplicationContext(), msg,
					Toast.LENGTH_SHORT);
		}
		toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL,
				0, 0);
		toast.show();

		progressBar.setProgress(currentCount);

		txtTodaysCount.setText(String.valueOf(todaysCount));
		txtOverallCount.setText(String.valueOf(overallCount));

		if (currentCount == defaultCount) {
			toast.cancel();
			progressBar.setProgress(0);
			Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			v.vibrate(100);

			Intent i = new Intent(this, this.getClass());
			i.putExtra("TASBEEH_ID", getNextTasbeehId());
			startActivity(i);
			super.finish();
		}
	}

	private long getNextTasbeehId() {

		int nextTasbeehId = 0, nextTasbeehOrder = 0;
		int currentTasbeehOrder = db.tasbeeh.getTasbeehOrder(tasbeehId);
		int maxTasbeehOrder = db.tasbeeh.getMaxTasbeehOrder();
		if (maxTasbeehOrder == currentTasbeehOrder) {
			nextTasbeehOrder = 1;
		} else {
			nextTasbeehOrder = currentTasbeehOrder + 1;
		}
		nextTasbeehId = (int) db.tasbeeh.getTasbeehId(nextTasbeehOrder);

		return nextTasbeehId;
	}
}
