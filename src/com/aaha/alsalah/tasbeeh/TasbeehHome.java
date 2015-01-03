package com.aaha.alsalah.tasbeeh;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.aaha.alsalah.R;
import com.aaha.db.DBAdapter;
import com.aaha.db.DBAdapter.Tasbeeh;
import com.aaha.util.Util;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;

public class TasbeehHome extends SherlockFragmentActivity implements
		OnClickListener {

	DBAdapter db;
	ListView tasbeehList = null;
	Button addTasbeeh;
	Cursor mCursor = null;
	SimpleCursorAdapter mCursorAdapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tasbeeh_home);

		db = new DBAdapter(getApplicationContext());
		db.open();

		tasbeehList = (ListView) findViewById(R.id.tasbeehListView);
		registerForContextMenu(tasbeehList);

		tasbeehList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent i = new Intent(TasbeehHome.this, Zikr.class);
				i.putExtra("TASBEEH_ID", id);
				startActivity(i);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		try {
			loadTasbeehList();
		} catch (Exception e) {
			e.printStackTrace();
			Util.Toast(getApplicationContext(),
					"Error occurred while loading Tasbeeh: " + e);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.options_menu_tasbeeh, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_tasbeeh_reset_all:
			resetTasbeeh(-1,
					"Are you sure you want to reset the count of all Tasbeeh?");
			break;
		}
		return true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mCursor != null) {
			mCursor.close();
		}
	}

	@Override
	protected void onDestroy() {
		try {
			if (mCursor != null) {
				mCursor.close();
			}
			db.close();
		} catch (Exception ignoreMe) {

		}
		super.onDestroy();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.options_context_menu_tasbeeh, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.menu_tasbeeh_edit:
			Intent i = new Intent(this, EditTasbeeh.class);
			i.putExtra("TASBEEH_ID", menuInfo.id);
			startActivity(i);
			break;
		case R.id.menu_tasbeeh_moveup:
			int tasbeehOrder = db.tasbeeh.getTasbeehOrder(menuInfo.id);
			if (tasbeehOrder != 1) {
				int newOrder = tasbeehOrder - 1;
				long id = db.tasbeeh.getTasbeehId(newOrder);
				db.tasbeeh.updateTasbeehOrder(menuInfo.id, newOrder);
				db.tasbeeh.updateTasbeehOrder(id, tasbeehOrder);
				mCursor = db.tasbeeh.get();
				mCursorAdapter.changeCursor(mCursor);
			}
			break;
		case R.id.menu_tasbeeh_movedown:
			tasbeehOrder = db.tasbeeh.getTasbeehOrder(menuInfo.id);
			if (tasbeehOrder != db.tasbeeh.getMaxTasbeehOrder()) {
				int newOrder = tasbeehOrder + 1;
				long id = db.tasbeeh.getTasbeehId(newOrder);
				db.tasbeeh.updateTasbeehOrder(menuInfo.id, newOrder);
				db.tasbeeh.updateTasbeehOrder(id, tasbeehOrder);
				mCursor = db.tasbeeh.get();
				mCursorAdapter.changeCursor(mCursor);
			}
			break;
		case R.id.menu_tasbeeh_reset:
			resetTasbeeh(menuInfo.id,
					"Are you sure you want to reset the count of this Tasbeeh?");
			break;
		}
		return true;
	}

	private void resetTasbeeh(final long tasbeehId, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message)
				.setCancelable(true)
				.setTitle("Confirm")
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								db.tasbeehCount.resetCount(tasbeehId, 0, 0);
								Util.Toast(getApplicationContext(),
										"Tasbeeh count has been reset!");
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

	public void addNewTasbeeh(View view) {
		Intent i;
		i = new Intent(this, AddTasbeeh.class);
		startActivity(i);
	}

	@SuppressWarnings("deprecation")
	private void loadTasbeehList() {
		mCursor = db.tasbeeh.get();

		if (mCursor == null)
			return;

		if (!mCursor.moveToFirst()) {
			preLoadTasbeeh();
			mCursor.close();
			mCursor = db.tasbeeh.get();
		}

		String[] databaseColumnNames = new String[] { Tasbeeh.KEY_NAME,
				Tasbeeh.KEY_DEFAULT_COUNT, Tasbeeh.KEY_NOTES };

		int[] toViewIDs = new int[] { R.id.item_tasbeeh_name,
				R.id.item_default_count, R.id.item_tasbeeh_notes };

		mCursorAdapter = new SimpleCursorAdapter(this,
				R.layout.table_layout_tasbeeh, mCursor, databaseColumnNames,
				toViewIDs);

		tasbeehList.setAdapter(mCursorAdapter);
	}

	private void preLoadTasbeeh() {
		db.tasbeeh.add("Subhan'Allah", "Subhan'Allah", "Glory be to Allah", 33,
				"Muslim 1:418", 1);
		db.tasbeeh.add("Alhamdulillah", "Alhamdulillah", "Praise be to Allah",
				33, "Muslim 1:418", 2);
		db.tasbeeh.add("Allahu Akbar", "Allahu Akbar", "Allah is most Great",
				34, "Muslim 1:418", 3);
		db.tasbeeh
				.add("Allahumma ajirni min an-naar",
						"Allahumma ajirni min an-naar",
						"O Allah, save me from the Fire",
						7,
						"After performing the dawn [Fajr] or sunset [Maghrib] Prayer, "
								+ "before you utter another word, say the above du'a seven times."
								+ " If you die that day, or that night, "
								+ "Allah will decree that you be saved from Hellfire",
						4);

		db.tasbeeh
				.add("Allahumma antas-Salaamu...",
						"Allahumma antas-Salaamu, wa minkas-Salaamu, "
								+ "tabarakta ya Dhal Jalaali wal-Ikram",
						"O Allah! You are Peace and from You comes peace. Blessed are You,"
								+ " O Owner of majesty and Honour",
						1,
						"abu Dawud 2:62, ibn Majah 2:1267, at-Tirmidhi 5:515, Ahmad 5:360",
						5);
		db.tasbeeh
				.add("La ilaha ilAllahu wah-dahu 1",
						"La ilaha ilAllahu wah-dahu la shareeka lahu, "
								+ "lahul Mulku wa lahul Hamd, wa Huwa ala kulli shaiy'iin Qadeer",
						"None has the right to be worshipped Except Allah alone. "
								+ "He has no partner, His is the dominion and His is the Praise"
								+ " and He is Able to do all things.", 1,
						"Muslim 1:418", 6);
		db.tasbeeh
				.add("Aayatul Kursi",
						"Allahu la ilaha illa Huwa, Al-Haiyul-Qaiyum La ta'khudhuhu sinatun wa la nawm, "
								+ "lahu man fis-samawati wa ma fil-'ard Man dhal-ladhi yashfa'u 'indahu illa bi-idhnihi "
								+ "Ya'lamu ma baina aidihim wa ma khalfahum, wa la yuhituna bi shai'im-min 'ilmihi "
								+ "illa bima sha'a Wasi'a kursiyuhus-samawati wal ard, "
								+ "wa la ya'uduhu hifdhuhuma Wa Huwal 'Aliyul-Adheem",
						"There is no god but He - the Living, The Self-subsisting, Eternal. "
								+ "No slumber can seize Him Nor Sleep. His are all things "
								+ "In the heavens and on earth. Who is there can intercede "
								+ "In His presence except As he permitteth? "
								+ "He knoweth What (appeareth to His creatures As) Before or After or Behind them. "
								+ "Nor shall they compass Aught of his knowledge Except as He willeth. "
								+ "His throne doth extend Over the heavens And on earth, and "
								+ "He feeleth No fatigue in guarding And preserving them, "
								+ "For He is the Most High. The Supreme (in glory).",
						1,
						"[Surah al-Baqarah 2: 255] "
								+ "The one who recites it after each of the obligatory prayers, "
								+ "then death will be the only thing preventing him from entering Paradise.",
						7);
		db.tasbeeh
				.add("Shahada",
						"La ilaha illallah muhammadur rasulullah",
						"There is no god but Allah, Muhammad is the messenger of Allah",
						100, "", 8);

		db.tasbeeh
				.add("La ilaha ilAllahu wah-dahu 2",
						"La ilaha ilAllahu wah-dahu la shareeka lahu, lahul Mulku wa lahul Hamd, "
								+ "wa Huwa ala kulli shaiy'iin Qadeer. Allahumma la maan'ia lima a'taita, "
								+ "wa la mu'tiya lima mana'ta, wa la yanfa'u dhal-jaddi minkal-jiddu",
						"None has the right to be worshipped Except Allah alone. He has no partner, "
								+ "His is the dominion and His is the Praise and He is Able to do all things. "
								+ "O Allah! There is none who can withold what You have Given and none may "
								+ "Give what You have withheld; and the might of a mighty person cannot "
								+ "benefit him againt You", 1,
						"Sahih al-Bukhari 1:255, Muslim 1:414", 9);
	}

	@Override
	public void onClick(View view) {
		Intent i;
		switch (view.getId()) {
		case R.id.addTasbeeh:
			i = new Intent(this, AddTasbeeh.class);
			startActivity(i);
			break;
		}
	}

}
