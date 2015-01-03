package com.aaha.alsalah.backup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.aaha.alsalah.R;
import com.aaha.db.DBAdapter;
import com.aaha.db.DBAdapter.T_Menstruation;
import com.aaha.db.DBAdapter.PrayerType;
import com.aaha.db.DBAdapter.T_Prayers;
import com.aaha.db.DBAdapter.T_Qasr;
import com.aaha.db.DBAdapter.T_Ramdhan;
import com.aaha.db.DBAdapter.T_Tasbeeh;
import com.aaha.db.DBAdapter.T_TasbeehCount;
import com.aaha.db.DBAdapter.User;
import com.aaha.db.DatabaseBackupAssistant;
import com.aaha.util.LogUtil;
import com.aaha.util.Util;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class BackupRestore extends SherlockFragmentActivity implements
		OnClickListener {

	Button btnExportPrayers, btnImportData;
	TextView backupStatus, importStatus, importFileNameField;
	DBAdapter db;
	Cursor mCursor = null;
	FileWriter writer;
	String username;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_backup_restore);

		db = new DBAdapter(getApplicationContext());
		db.open();

		btnExportPrayers = (Button) findViewById(R.id.btnExport);
		btnImportData = (Button) findViewById(R.id.btnImport);

		backupStatus = (TextView) findViewById(R.id.txtbackupStatus);
		importStatus = (TextView) findViewById(R.id.txtImportStatus);

		backupStatus.setText("");
		importStatus.setText("");

		importFileNameField = (TextView) findViewById(R.id.importFileNameField);
		String activeUsername = "Guest";
		try {
			mCursor = db.user.getActiveUsername();
			if (mCursor != null && mCursor.moveToFirst()) {
				activeUsername = mCursor.getString(mCursor
						.getColumnIndex(User.KEY_NAME));
			}
		} catch (Exception e) {
			LogUtil.toastShort(getApplicationContext(),
					"Error occurred while retrieving username");
			e.printStackTrace();
		} finally {
			mCursor.close();
		}

		username = activeUsername.replace(" ", "_").replace(".", "_");
		importFileNameField.setHint(username + ".xml");

		btnExportPrayers.setOnClickListener(this);
		btnImportData.setOnClickListener(this);
	}

	private int importData(String filename) {
		File fXmlFile = new File(Environment.getExternalStorageDirectory(),
				filename);
		try {

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);

			doc.getDocumentElement().normalize();

			NodeList nTableNodes = doc.getElementsByTagName("table");

			Node nPrayersTableNode = getChildNode(nTableNodes,
					T_Prayers.TABLE_NAME);
			importDataIntoTables(nPrayersTableNode);

			Node nTasbeehTableNode = getChildNode(nTableNodes,
					T_Tasbeeh.TABLE_NAME);
			importDataIntoTables(nTasbeehTableNode);

			Node nTasbeehCountTableNode = getChildNode(nTableNodes,
					T_TasbeehCount.TABLE_NAME);
			importDataIntoTables(nTasbeehCountTableNode);

			Node nQasrTableNode = getChildNode(nTableNodes, T_Qasr.TABLE_NAME);
			importDataIntoTables(nQasrTableNode);

			Node nMenstruationTableNode = getChildNode(nTableNodes,
					T_Menstruation.TABLE_NAME);
			importDataIntoTables(nMenstruationTableNode);

			Node nRamdhanTableNode = getChildNode(nTableNodes,
					T_Ramdhan.TABLE_NAME);
			importDataIntoTables(nRamdhanTableNode);

			return 0;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			return 3;
		}

	}

	public static Node getChildNode(NodeList nTableNodes, String nodeName) {
		for (int i = 0; i < nTableNodes.getLength(); i++) {
			Element eElement = (Element) nTableNodes.item(i);
			if (eElement.getAttribute("name").equalsIgnoreCase(nodeName)) {
				return nTableNodes.item(i);
			}
		}
		return null;
	}

	private int getRowDataValue(Map<String, String> rowData, String key) {
		int value = 0;
		String temp = rowData.get(key);
		if (temp != null) {
			value = Integer.valueOf(temp);
		}
		return value;
	}

	public void importDataIntoTables(Node nTableNode) {

		if (nTableNode == null) {
			return;
		}

		NodeList nRowNodes = nTableNode.getChildNodes();
		Element eElement = (Element) nTableNode;
		String tableName = eElement.getAttribute("name");
		Log.d(DBAdapter.TAG, "Importing data from table: " + tableName);
		Log.d(DBAdapter.TAG,
				"Number of nodes: " + String.valueOf(nRowNodes.getLength()));

		for (int row = 0; row < nRowNodes.getLength(); row++) {
			Node nRowNode = nRowNodes.item(row);
			Map<String, String> rowData = getRowData(nRowNode);

			if (tableName.equalsIgnoreCase(T_Prayers.TABLE_NAME)) {
				long userId = db.user.getActiveUserId();
				String prayerDate = rowData.get(T_Prayers.KEY_DATE);
				int fajr = getRowDataValue(rowData, T_Prayers.KEY_FAJR);
				int zohar = getRowDataValue(rowData, T_Prayers.KEY_ZOHAR);
				PrayerType type = Util.getPrayerType(getRowDataValue(rowData,
						T_Prayers.KEY_TYPE));
				int isha = getRowDataValue(rowData, T_Prayers.KEY_ISHA);
				int magrib = getRowDataValue(rowData, T_Prayers.KEY_MAGRIB);
				int asr = getRowDataValue(rowData, T_Prayers.KEY_ASR);

				long prayerId = db.prayer.isExist(prayerDate, type);
				if (prayerId == -1) {
					db.prayer.add(userId, prayerDate, fajr, zohar, asr, magrib,
							isha, type);
				} else {
					db.prayer.update(prayerId, fajr, zohar, asr, magrib, isha);
				}
			} else if (tableName.equalsIgnoreCase(T_Qasr.TABLE_NAME)) {
				String prayerDate = rowData.get(T_Qasr.KEY_DATE);
				int fajr = Integer.valueOf(rowData.get(T_Qasr.KEY_FAJR));
				int zohar = Integer.valueOf(rowData.get(T_Qasr.KEY_ZOHAR));
				int isha = Integer.valueOf(rowData.get(T_Qasr.KEY_ISHA));
				int magrib = Integer.valueOf(rowData.get(T_Qasr.KEY_MAGRIB));
				int asr = Integer.valueOf(rowData.get(T_Qasr.KEY_ASR));

				long prayerId = db.prayer.isExist(prayerDate, PrayerType.ADA);
				if (prayerId != -1) {
					// if prayer exists, get qasr prayer id
					if (db.qasr.isExist(prayerId)) {
						// if the qasr salah is found in table -- update
						db.qasr.update(prayerId, fajr, zohar, asr, magrib, isha);
					} else {
						// if qasr salah is not found -- add new entry
						db.qasr.add(prayerId, prayerDate, fajr, zohar, asr,
								magrib, isha);
					}
				}
			} else if (tableName.equalsIgnoreCase(T_Menstruation.TABLE_NAME)) {
				String prayerDate = rowData.get(T_Menstruation.KEY_DATE);
				int fajr = getRowDataValue(rowData, T_Menstruation.KEY_FAJR);
				int zohar = getRowDataValue(rowData, T_Menstruation.KEY_ZOHAR);
				int isha = getRowDataValue(rowData, T_Menstruation.KEY_ISHA);
				int magrib = getRowDataValue(rowData, T_Menstruation.KEY_MAGRIB);
				int asr = getRowDataValue(rowData, T_Menstruation.KEY_ASR);

				long prayerId = db.prayer.isExist(prayerDate, PrayerType.ADA);

				if (prayerId != -1) {
					// if prayer exists
					if (db.menstruation.isExist(prayerId)) {
						// if its found in table -- update
						db.menstruation.update(prayerId, fajr, zohar, asr,
								magrib, isha);
					} else {
						// if not found -- add new entry
						db.menstruation.add(prayerId, prayerDate, fajr, zohar,
								asr, magrib, isha);
					}
				}
			} else if (tableName.equalsIgnoreCase(T_Ramdhan.TABLE_NAME)) {
				String date = rowData.get(T_Ramdhan.KEY_DATE);
				int siyam = getRowDataValue(rowData, T_Ramdhan.KEY_SIYAM);
				int taraweeh = getRowDataValue(rowData, T_Ramdhan.KEY_TARAWEEH);
				int quran = getRowDataValue(rowData, T_Ramdhan.KEY_QURAN);
				String value = rowData.get(T_Ramdhan.KEY_QURAN_JUZ);
				Float quranJuz = value != null ? Float.valueOf(value) : 0;

				long id = db.ramdhan.isExist(date);
				if (id != -1) {
					// if the details found in table -- update
					db.ramdhan.update(id, siyam, taraweeh, quran, quranJuz);
				} else {
					// if not -- add new entry
					db.ramdhan.add(date, siyam, taraweeh, quran, quranJuz);
				}
			} else if (tableName.equalsIgnoreCase(T_Tasbeeh.TABLE_NAME)) {
				String name = rowData.get(T_Tasbeeh.KEY_NAME);
				String tasbeeh = rowData.get(T_Tasbeeh.KEY_TASBEEH);
				String meaning = rowData.get(T_Tasbeeh.KEY_MEANING);
				int default_count = getRowDataValue(rowData,
						T_Tasbeeh.KEY_DEFAULT_COUNT);
				String notes = rowData.get(T_Tasbeeh.KEY_NOTES);
				int order = db.tasbeeh.getMaxTasbeehOrder() + 1;

				long tasbeehId = db.tasbeeh.isTasbeehExist(name);
				if (tasbeehId != -1) {
					db.tasbeeh.update(tasbeehId, name, tasbeeh, meaning,
							default_count, notes);
				} else {
					db.tasbeeh.add(name, tasbeeh, meaning, default_count,
							notes, order);
				}
			} else if (tableName.equalsIgnoreCase(T_TasbeehCount.TABLE_NAME)) {
				String tasbeehName = rowData.get(T_Tasbeeh.KEY_NAME);
				int overall_count = getRowDataValue(rowData,
						T_TasbeehCount.KEY_COUNT_OVERALL);

				long tasbeehId = db.tasbeeh.isTasbeehExist(tasbeehName);
				if (tasbeehId != -1) {
					long today = (new Date()).getTime();
					mCursor = null;
					mCursor = db.tasbeehCount.get(tasbeehId);

					if (mCursor == null) {
						db.tasbeehCount.add(Util.formatDate(today), tasbeehId,
								0, overall_count);
					} else {
						if (mCursor.moveToFirst()) {
							db.tasbeehCount.update(Util.formatDate(today),
									tasbeehId, 0, overall_count);
						} else {
							db.tasbeehCount.add(Util.formatDate(today),
									tasbeehId, 0, overall_count);
						}
						mCursor.close();
					}
				}
			}
		}
	}

	public static Map<String, String> getRowData(Node nRowNode) {
		NodeList nColNodes = nRowNode.getChildNodes();
		Map<String, String> rowData = new HashMap<String, String>();
		for (int col = 0; col < nColNodes.getLength(); col++) {
			Node nColNode = nColNodes.item(col);
			Element eElement = (Element) nColNode;
			String fieldName = eElement.getNodeName();
			String value = eElement.getTextContent();
			rowData.put(fieldName, value);
		}
		return rowData;
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnExport:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(
					"This might overwrite the backup file " + username
							+ ".xml, Are you sure you want to continue?")
					.setTitle("Confirm")
					.setCancelable(true)
					.setIcon(android.R.drawable.ic_delete)
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									exportAlSalahData();
								}
							})
					.setNegativeButton("No",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
			AlertDialog alert = builder.create();
			alert.show();
			break;
		case R.id.btnImport:

			if (importFileNameField.getText().toString().trim().length() == 0) {
				LogUtil.toastShort(getApplicationContext(), "Please enter the filename");
				return;
			}

			builder = new AlertDialog.Builder(this);
			builder.setMessage(
					"This might overwrite existing Prayers/ Tasbeeh/ Tasbeeh Count, Are you sure you want to continue?")
					.setTitle("Confirm")
					.setCancelable(true)
					.setIcon(android.R.drawable.ic_delete)
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									importAlSalahData();
								}
							})
					.setNegativeButton("No",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
			alert = builder.create();
			alert.show();
			break;
		}
	}

	public void exportAlSalahData() {
		final ProgressDialog progress = ProgressDialog.show(BackupRestore.this,
				"Exporting", "Please wait...", true, false);

		new Thread(new Runnable() {
			public void run() {
				final String filename = username + ".xml";

				DatabaseBackupAssistant export = new DatabaseBackupAssistant(
						getApplicationContext(), db, filename);
				final int status = export.exportData();

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						switch (status) {
						case 0:
							LogUtil.toastShort(getApplicationContext(),
									"Prayers exported successfully!!!");
							backupStatus
									.setText("Prayers have been saved to SD card: "
											+ filename);
							break;
						case 1:
							LogUtil.toastShort(getApplicationContext(),
									"Problem while reading prayers!!!");
							backupStatus
									.setText("Problem while reading prayers");
							break;
						case 2:
							LogUtil.toastShort(getApplicationContext(),
									"No Data available to export");
							backupStatus.setText("No Data available to export");
							break;
						case 3:
							LogUtil.toastShort(getApplicationContext(),
									"No SD card found");
							backupStatus.setText("No SD card found");
							break;
						}
					}
				});
				progress.cancel();
			}
		}).start();
	}

	public void importAlSalahData() {
		final ProgressDialog progress = ProgressDialog.show(BackupRestore.this,
				"Importing", "Please wait...", true, false);

		new Thread(new Runnable() {
			public void run() {
				String temp = importFileNameField.getText().toString().trim();
				temp += temp.indexOf(".") > -1 ? "" : ".xml";

				final String filename = temp;
				final int status = importData(filename);

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						switch (status) {
						case 0:
							LogUtil.toastShort(getApplicationContext(),
									"Prayers imported successfully");
							importStatus.setText("Prayers imported from: "
									+ filename);
							break;
						case 1:
							LogUtil.toastShort(getApplicationContext(),
									"File not found or invalid file");
							importStatus
									.setText("File not found or invalid file: "
											+ filename);
							break;
						case 2:
							LogUtil.toastShort(getApplicationContext(),
									"No prayers available to import");
							importStatus
									.setText("No prayers available to import from file: "
											+ filename);
							break;
						case 3:
							LogUtil.toastShort(getApplicationContext(),
									"Either backup file is corrupted or does not have any data");
							importStatus
									.setText("Either backup file is corrupted or does not have any data: "
											+ filename);
							break;
						}
					}
				});
				progress.cancel();
			}
		}).start();
	}

}