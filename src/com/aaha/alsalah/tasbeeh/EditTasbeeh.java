package com.aaha.alsalah.tasbeeh;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.aaha.alsalah.R;
import com.aaha.db.DBAdapter;
import com.aaha.db.DBAdapter.T_Tasbeeh;
import com.aaha.util.LogUtil;

public class EditTasbeeh extends Activity implements OnClickListener {
	DBAdapter db;
	Cursor mCursor = null;
	TextView tasbeehName, tasbeehField, tasbeehMeaning, tasbeehCount,
			tasbeehNotes;
	Button addTasbeeh;
	int tasbeehId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tasbeeh_add);

		tasbeehName = (TextView) findViewById(R.id.tasbeehNameField);
		tasbeehField = (TextView) findViewById(R.id.tasbeehField);
		tasbeehMeaning = (TextView) findViewById(R.id.tasbeehMeaningField);
		tasbeehCount = (TextView) findViewById(R.id.tasbeehCountField);
		tasbeehNotes = (TextView) findViewById(R.id.tasbeehNotesField);
		addTasbeeh = (Button) findViewById(R.id.btnAddTasbeeh);

		addTasbeeh.setText(getResources().getString(R.string.update));
		addTasbeeh.setOnClickListener(this);

		db = new DBAdapter(getApplicationContext());
		db.open();

		Bundle extras = getIntent().getExtras();
		tasbeehId = (int) extras.getLong("TASBEEH_ID");

		setTasbeeh(tasbeehId);
	}

	private void setTasbeeh(int tasbeehId) {
		mCursor = db.tasbeeh.getTasbeeh(tasbeehId);
		if (mCursor == null) {
			return;
		}

		if (mCursor.moveToFirst()) {
			tasbeehName.setText(mCursor.getString(mCursor
					.getColumnIndex(T_Tasbeeh.KEY_NAME)));
			tasbeehField.setText(mCursor.getString(mCursor
					.getColumnIndex(T_Tasbeeh.KEY_TASBEEH)));
			tasbeehMeaning.setText(mCursor.getString(mCursor
					.getColumnIndex(T_Tasbeeh.KEY_MEANING)));
			tasbeehCount.setText(mCursor.getString(mCursor
					.getColumnIndex(T_Tasbeeh.KEY_DEFAULT_COUNT)));
			tasbeehNotes.setText(mCursor.getString(mCursor
					.getColumnIndex(T_Tasbeeh.KEY_NOTES)));
		}
		mCursor.close();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			mCursor.close();

		} catch (Exception ignoreMe) {
		}
		try {
			db.close();
		} catch (Exception ignoreMe) {
		}
	}

	public void updateTasbeeh() {
		String name = tasbeehName.getText().toString().trim();
		if (name.length() == 0) {
			LogUtil.toastShort(getApplicationContext(),
					"Please enter Tasbeeh name");
			return;
		}

		String tasbeeh = tasbeehField.getText().toString().trim();
		if (tasbeeh.length() == 0) {
			LogUtil.toastShort(getApplicationContext(), "Please enter Tasbeeh ");
			return;
		}

		String meaning = tasbeehMeaning.getText().toString().trim();
		String notes = tasbeehNotes.getText().toString().trim();

		int count = 100;
		try {
			count = Integer.valueOf(tasbeehCount.getText().toString().trim());
		} catch (Exception e) {
			LogUtil.toastShort(getApplicationContext(),
					"Please enter default Tasbeeh count");
			return;
		}

		if (count == 0) {
			LogUtil.toastShort(getApplicationContext(),
					"Please enter default Tasbeeh count");
			return;
		}

		if (db.tasbeeh.update(tasbeehId, name, tasbeeh, meaning, count, notes) > -1) {
			LogUtil.toastShort(getApplicationContext(), "Tasbeeh updated!!!");
			super.finish();
		} else {
			LogUtil.toastShort(getApplicationContext(), "Update failed!!!");
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btnAddTasbeeh:
			updateTasbeeh();
			break;
		}
	}
}
