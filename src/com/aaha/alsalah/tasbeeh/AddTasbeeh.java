package com.aaha.alsalah.tasbeeh;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.aaha.alsalah.R;
import com.aaha.db.DBAdapter;
import com.aaha.util.Util;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class AddTasbeeh extends SherlockFragmentActivity implements
		OnClickListener {
	DBAdapter db;
	TextView tasbeehName, tasbeehField, tasbeehMeaning, tasbeehCount,
			tasbeehNotes;
	Button addTasbeeh;

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
		addTasbeeh.setOnClickListener(this);

		db = new DBAdapter(getApplicationContext());
		db.open();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			db.close();
		} catch (Exception ignoreMe) {

		}
	}

	private void addTasbeeh() {
		String name = tasbeehName.getText().toString().trim();
		if (name.length() == 0) {
			Util.Toast(getApplicationContext(), "Please enter Tasbeeh name");
			return;
		}

		if (db.tasbeeh.isTasbeehExist(name) > -1) {
			Util.Toast(getApplicationContext(), "Tasbeeh already exist");
			return;
		}

		String tasbeeh = tasbeehField.getText().toString().trim();
		if (tasbeeh.length() == 0) {
			Util.Toast(getApplicationContext(), "Please enter Tasbeeh ");
			return;
		}

		int count = 100;
		try {
			count = Integer.valueOf(tasbeehCount.getText().toString().trim());
		} catch (Exception e) {
			Util.Toast(getApplicationContext(),
					"Please enter default Tasbeeh count");
			return;
		}

		if (count == 0) {
			Util.Toast(getApplicationContext(),
					"Please enter default Tasbeeh count");
			return;
		}

		String notes = tasbeehNotes.getText().toString().trim();
		if (notes.length() == 0) {
			notes = "";
		}

		String meaning = tasbeehMeaning.getText().toString().trim();
		if (meaning.length() == 0) {
			meaning = "";
		}

		int order = db.tasbeeh.getMaxTasbeehOrder() + 1;

		if (db.tasbeeh.add(name, tasbeeh, meaning, count, notes, order) > -1) {
			Util.Toast(getApplicationContext(), "Tasbeeh added!!!");
			super.finish();
		} else {
			Util.Toast(getApplicationContext(), "Add failed!!!");
		}

	}

	@Override
	public void onBackPressed() {
		super.finish();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btnAddTasbeeh:
			addTasbeeh();
			break;
		}
	}
}
