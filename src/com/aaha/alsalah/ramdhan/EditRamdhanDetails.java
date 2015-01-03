package com.aaha.alsalah.ramdhan;

import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

import com.aaha.alsalah.R;
import com.aaha.db.DBAdapter;
import com.aaha.db.DBAdapter.Ramdhan;
import com.aaha.util.Util;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class EditRamdhanDetails extends SherlockFragmentActivity implements
		OnClickListener {

	int id;
	DBAdapter db;
	Button selectDate, save;
	CheckBox siyam, taraweeh, quran;
	EditText quranJuz;
	Context context;
	TextView ramdhanMessage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ramdhan);

		context = getApplicationContext();

		db = new DBAdapter(getApplicationContext());
		db.open();

		selectDate = (Button) findViewById(R.id.selectDate);
		selectDate.setEnabled(false);

		save = (Button) findViewById(R.id.saveRamdhanButton);
		save.setOnClickListener(this);
		save.setText(Util.getString(context, R.string.update));

		siyam = (CheckBox) findViewById(R.id.siyamCheckbox);
		taraweeh = (CheckBox) findViewById(R.id.taraweehCheckbox);
		quran = (CheckBox) findViewById(R.id.quranCheckbox);
		quranJuz = (EditText) findViewById(R.id.quranEditText);
		ramdhanMessage = (TextView) findViewById(R.id.ramdhanMessage);

		quranJuz.setVisibility(View.GONE);
		ramdhanMessage
				.setText("Please add the fastings, taraweeh you complete every ramdhan. Also add the Quran you recite everyday.");

		quran.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					quranJuz.setVisibility(View.VISIBLE);
				} else {
					quranJuz.setVisibility(View.GONE);
				}
			}
		});

		Bundle extras = getIntent().getExtras();
		id = (int) extras.getLong(Ramdhan.KEY_ID);
		setRamdhanSettings(id);
	}

	private void setRamdhanSettings(int id) {
		Cursor c = db.ramdhan.get(id);
		long date = (new Date()).getTime();
		int s = 0, t = 0, q = 0;
		float qv = 0;
		try {
			if (c != null) {
				c.moveToFirst();
				s = c.getInt(c.getColumnIndex(Ramdhan.KEY_SIYAM));
				t = c.getInt(c.getColumnIndex(Ramdhan.KEY_TARAWEEH));
				q = c.getInt(c.getColumnIndex(Ramdhan.KEY_QURAN));
				qv = c.getFloat(c.getColumnIndex(Ramdhan.KEY_QURAN_JUZ));
				date = c.getLong(c.getColumnIndex(Ramdhan.KEY_DATE));
			}
		} catch (Exception e) {
			e.printStackTrace();
			Util.Toast(getApplicationContext(), "Error occurred: " + e);
		} finally {
			if (c != null)
				c.close();
		}
		siyam.setChecked(s == 1);
		taraweeh.setChecked(t == 1);
		quran.setChecked(q == 1);
		quranJuz.setText(String.valueOf(qv));
		selectDate.setText(Util.formatDate(date * 1000));
	}

	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.saveRamdhanButton:

			int f = siyam.isChecked() ? 1 : 0;
			int t = taraweeh.isChecked() ? 1 : 0;
			int q = quran.isChecked() ? 1 : 0;
			float qv = 0;
			if (q == 1) {
				try {
					qv = Float.parseFloat(quranJuz.getText().toString());
				} catch (NumberFormatException nef) {
					Util.Toast(getApplicationContext(),
							"Please enter number of Juz\'");
					return;
				}
			}
			updateRamdhanDetails(id, f, t, q, qv);
			EditRamdhanDetails.this.finish();
			break;
		}
	}

	private void updateRamdhanDetails(int id, int f, int t, int q, float qv) {

		if (db.ramdhan.update(id, f, t, q, qv) > 0) {
			Util.Toast(getApplicationContext(), "Details updated!");
		} else {
			Util.Toast(getApplicationContext(), "Error while updating details");
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			db.close();
		} catch (Exception ignoreMe) {
		}
	}
}
