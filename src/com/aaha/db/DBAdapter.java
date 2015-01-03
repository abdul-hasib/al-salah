package com.aaha.db;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.aaha.util.Util;

public class DBAdapter {

	public static final String TAG = "Salah";
	private static final String DATABASE_NAME = "alSalahDb.db";
	private static final int DATABASE_VERSION = 6;

	private final Context context;
	private DatabaseHelper DBHelper;
	public User user;
	public Prayers prayer;
	public Tasbeeh tasbeeh;
	public TasbeehCount tasbeehCount;
	public Notification notification;
	public Qasr qasr;
	public Ramdhan ramdhan;
	public Menstruation menstruation;
	private SQLiteDatabase db;

	public DBAdapter(Context ctx) {
		this.context = ctx;
		DBHelper = new DatabaseHelper(context);

		user = new User();
		prayer = new Prayers();
		tasbeeh = new Tasbeeh();
		tasbeehCount = new TasbeehCount();
		notification = new Notification();
		qasr = new Qasr();
		menstruation = new Menstruation();
		ramdhan = new Ramdhan();
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			try {
				Log.d(TAG, "Creating database tables");
				db.execSQL(User.CREATE_SQL);
				Log.d(TAG, "Finished creating database table: "
						+ User.TABLE_NAME);
				db.execSQL(Prayers.CREATE_SQL);
				Log.d(TAG, "Finished creating database table: "
						+ Prayers.TABLE_NAME);
				db.execSQL(Tasbeeh.CREATE_SQL);
				Log.d(TAG, "Finished creating database table: "
						+ Tasbeeh.TABLE_NAME);
				db.execSQL(TasbeehCount.CREATE_SQL);
				Log.d(TAG, "Finished creating database table: "
						+ TasbeehCount.TABLE_NAME);
				db.execSQL(Notification.CREATE_SQL);
				Log.d(TAG, "Finished creating database table: "
						+ Notification.TABLE_NAME);
				db.execSQL(Qasr.CREATE_SQL);
				Log.d(TAG, "Finished creating database table: "
						+ Qasr.TABLE_NAME);
				db.execSQL(Menstruation.CREATE_SQL);
				Log.d(TAG, "Finished creating database table: "
						+ Menstruation.TABLE_NAME);
				db.execSQL(Ramdhan.CREATE_SQL);
				Log.d(TAG, "Finished creating database table: "
						+ Ramdhan.TABLE_NAME);
			} catch (SQLException e) {
				Log.e(TAG,
						"Exception while creating database tables: "
								+ e.toString());
				e.printStackTrace();
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");

			switch (newVersion) {
			case 2:
				break;
			case 3:
				break;
			case 4:
				onCreate(db);
				break;
			case 5:
				onCreate(db);
				break;
			case 6:
				onCreate(db);
				break;
			}

			onCreate(db);
		}
	}

	public class User {
		private static final String TABLE_NAME = "Users";
		public static final String KEY_USERID = "_id";
		public static final String KEY_NAME = "name";
		public static final String KEY_PASSWORD = "password";
		public static final String KEY_BIRTHDATE = "birthdate";
		public static final String KEY_ACTIVE = "active";
		public static final String KEY_PUBAGE = "pubage";

		private static final String CREATE_SQL = "CREATE TABLE IF NOT EXISTS "
				+ TABLE_NAME + " (" + KEY_USERID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_NAME
				+ " VARCHAR not null UNIQUE, " + KEY_PASSWORD
				+ " VARCHAR not null, " + KEY_BIRTHDATE + " date, "
				+ KEY_ACTIVE + " INTEGER, " + KEY_PUBAGE + " INTEGER " + ");";

		public int isAuthorizedUser(String username, String password)
				throws SQLException {

			String sql = "SELECT " + KEY_USERID + ", " + KEY_PASSWORD
					+ " FROM " + TABLE_NAME + " WHERE " + KEY_NAME + "=?";
			Cursor c = db.rawQuery(sql,
					new String[] { username.toLowerCase(Locale.ENGLISH) });

			int userid = -1;
			if (c == null)
				return userid;

			if (c.moveToFirst()) {
				if (c.getString(c.getColumnIndex(KEY_PASSWORD))
						.equals(password)) {
					userid = c.getInt(c.getColumnIndex(KEY_USERID));
				}
			}
			c.close();
			return userid;
		}

		public boolean isAuthorizedUser(int userId, String password)
				throws SQLException {

			String sql = "SELECT " + KEY_PASSWORD + " FROM " + TABLE_NAME
					+ " WHERE " + KEY_USERID + "=" + userId;
			Cursor c = db.rawQuery(sql, null);

			boolean flag = false;
			if (c == null) {
				return flag;
			}
			if (c.moveToFirst()) {
				if (c.getString(c.getColumnIndex(KEY_PASSWORD))
						.equals(password)) {
					flag = true;
				}
			}
			c.close();
			return flag;
		}

		public long add(String username, String password, String birthdate,
				int pubage) {
			ContentValues initialValues = new ContentValues();
			initialValues.put(KEY_NAME, username.toLowerCase(Locale.ENGLISH));
			initialValues.put(KEY_BIRTHDATE, birthdate);
			initialValues.put(KEY_PUBAGE, pubage);
			initialValues.put(KEY_PASSWORD, password);
			initialValues.put(KEY_ACTIVE, 0);
			return db.insert(TABLE_NAME, null, initialValues);
		}

		public boolean delete(int userId) {
			return db.delete(TABLE_NAME, KEY_USERID + "=" + userId, null) > 0;
		}

		public Cursor getAll() {
			return db.query(TABLE_NAME, new String[] { KEY_USERID, KEY_NAME,
					KEY_BIRTHDATE, KEY_PUBAGE, KEY_PASSWORD }, null, null,
					null, null, null);
		}

		public Cursor get() throws SQLException {
			return db.query(true, TABLE_NAME, new String[] { KEY_USERID,
					KEY_NAME, KEY_BIRTHDATE, KEY_PUBAGE, KEY_PASSWORD },
					KEY_USERID + "=?",
					new String[] { String.valueOf(getActiveUserId()) }, null,
					null, null, null);
		}

		public String getActiveUsername1() throws SQLException {
			Cursor c = db.query(true, TABLE_NAME, new String[] { KEY_NAME },
					KEY_ACTIVE + "=?", new String[] { String.valueOf(1) },
					null, null, null, null);
			String activeUsername = "Guest";
			if (c == null) {
				Log.d(TAG, "*** NO active user found");
				return activeUsername;
			}
			if (c.moveToFirst()) {
				activeUsername = c.getString(c.getColumnIndex(KEY_NAME));
				Log.d(TAG, "*** Active user: " + activeUsername);
			}
			c.close();
			return activeUsername;
		}

		public Cursor getActiveUsername() throws SQLException {
			return db.query(true, TABLE_NAME, new String[] { KEY_NAME },
					KEY_ACTIVE + "=?", new String[] { String.valueOf(1) },
					null, null, null, null);

		}

		public int getActiveUserId() throws SQLException {

			Cursor c = db.query(true, TABLE_NAME, new String[] { KEY_USERID },
					KEY_ACTIVE + "=?", new String[] { String.valueOf(1) },
					null, null, null, null);
			int userId = -1;
			if (c == null)
				return userId;
			if (c.moveToFirst()) {
				userId = c.getInt(c.getColumnIndex(KEY_USERID));
			}
			c.close();

			return userId;
		}

		public boolean setActiveUserId(long userId) throws SQLException {
			ContentValues args = new ContentValues();
			args.put(KEY_ACTIVE, 1);
			return db.update(TABLE_NAME, args, KEY_USERID + "=?",
					new String[] { String.valueOf(userId) }) > 0;
		}

		public Cursor get(String username) throws SQLException {
			return db.query(true, TABLE_NAME, new String[] { KEY_USERID },
					KEY_NAME + "=?", new String[] { username }, null, null,
					null, null);
		}

		public boolean isExist(String username) {
			Log.d(TAG, "Checking if user exist ");
			boolean result = false;
			try {
				Cursor c = get(username);
				if (c == null) {
					return false;
				}
				c.moveToFirst();
				Log.i(TAG, "row count: " + c.getCount());
				result = c.getCount() > 0;
				c.close();
			} catch (Exception e) {
				Log.e(TAG, "Exception occurred: " + e.toString());
			}
			return result;
		}

		public boolean updateUser(long userId, String username,
				String birthdate, int pubage, String password) {
			ContentValues args = new ContentValues();
			args.put(KEY_NAME, username.toLowerCase(Locale.ENGLISH));
			args.put(KEY_BIRTHDATE, birthdate);
			args.put(KEY_PUBAGE, pubage);
			args.put(KEY_PASSWORD, password);
			return db.update(TABLE_NAME, args, KEY_USERID + "=?",
					new String[] { String.valueOf(userId) }) > 0;
		}

		public boolean removeActiveUserId() {
			ContentValues args = new ContentValues();
			args.putNull(KEY_ACTIVE);
			boolean status = db.update(TABLE_NAME, args, null, null) > 0;
			return status;
		}

		public boolean changePassword(int activeUserId, String password) {
			ContentValues args = new ContentValues();
			args.put(KEY_PASSWORD, password);
			return db.update(TABLE_NAME, args, KEY_USERID + "=?",
					new String[] { String.valueOf(activeUserId) }) > 0;
		}

		public int getFarzPrayers() {
			Cursor c = get();
			String dob = null;
			String pubage = null;

			if (c == null)
				return 0;

			if (c.moveToFirst()) {
				dob = c.getString(c.getColumnIndex(User.KEY_BIRTHDATE));
				pubage = c.getString(c.getColumnIndex(User.KEY_PUBAGE));
			}
			c.close();

			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(Util.parseDate(dob).getTime());
			cal.add(Calendar.YEAR, Integer.parseInt(pubage));

			long day = 1000 * 60 * 60 * 24;
			float totalPrayers = (((new Date().getTime()) - cal
					.getTimeInMillis()) / day);

			return Math.round(totalPrayers);
		}
	}

	public static enum PrayerType {
		ADA(0), QADHA(1), ADDITIONAL(2);

		int value;

		PrayerType(int value) {
			this.value = value;
		}

		public int getValue() {
			return this.value;
		}
	}

	public class Prayers {

		public static final int ADA = 0;
		public static final int QADHA = 1;
		public static final int ADDITIONAL = 2;

		public static final String TABLE_NAME = "Prayers";
		public static final String KEY_PRAYER_ID = "_id";
		public static final String KEY_USER_ID = "userid";
		public static final String KEY_DATE = "prayerdate";
		public static final String KEY_FAJR = "fajr";
		public static final String KEY_ZOHAR = "zohar";
		public static final String KEY_ASR = "asr";
		public static final String KEY_MAGRIB = "magrib";
		public static final String KEY_ISHA = "isha";
		public static final String KEY_TYPE = "type";

		private final static String CREATE_SQL = "CREATE TABLE IF NOT EXISTS "
				+ TABLE_NAME + " (" + KEY_PRAYER_ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_USER_ID
				+ " INTEGER, " + KEY_FAJR + " INTEGER, " + KEY_ZOHAR
				+ " INTEGER, " + KEY_ASR + " INTEGER, " + KEY_MAGRIB
				+ " INTEGER, " + KEY_ISHA + " INTEGER, " + KEY_DATE
				+ " INTEGER, " + KEY_TYPE + " INTEGER );";

		public boolean delete(long prayerId) {
			Util.e("Deleting prayer id: from DB: " + prayerId);
			return db.delete(TABLE_NAME, KEY_PRAYER_ID + "=?",
					new String[] { String.valueOf(prayerId) }) > 0;
		}

		public long add(long userId, String prayerDate, int fajr, int zohar,
				int asr, int magrib, int isha, PrayerType type) {
			ContentValues initialValues = new ContentValues();

			Date date = Util.parseDate(prayerDate);

			initialValues.put(KEY_DATE, date.getTime() / 1000);
			initialValues.put(KEY_USER_ID, userId);
			initialValues.put(KEY_FAJR, fajr);
			initialValues.put(KEY_ZOHAR, zohar);
			initialValues.put(KEY_ASR, asr);
			initialValues.put(KEY_MAGRIB, magrib);
			initialValues.put(KEY_ISHA, isha);
			initialValues.put(KEY_TYPE, type.getValue());
			return db.insert(TABLE_NAME, null, initialValues);
		}

		public long update(long prayerId, int fajr, int zohar, int asr,
				int magrib, int isha) {
			ContentValues initialValues = new ContentValues();
			initialValues.put(KEY_FAJR, fajr);
			initialValues.put(KEY_ZOHAR, zohar);
			initialValues.put(KEY_ASR, asr);
			initialValues.put(KEY_MAGRIB, magrib);
			initialValues.put(KEY_ISHA, isha);

			return db.update(TABLE_NAME, initialValues, KEY_PRAYER_ID + "=?",
					new String[] { String.valueOf(prayerId) });
		}

		public Cursor getPrayers() {
			return db.query(TABLE_NAME, new String[] { KEY_PRAYER_ID, KEY_DATE,
					KEY_TYPE, KEY_FAJR, KEY_ZOHAR, KEY_ASR, KEY_MAGRIB,
					KEY_ISHA }, KEY_USER_ID + "=?",
					new String[] { String.valueOf(user.getActiveUserId()) },
					null, null, KEY_DATE + " DESC", null);
		}

		public Cursor getPrayersToExport() {
			return db.query(TABLE_NAME, new String[] { KEY_DATE, KEY_TYPE,
					KEY_FAJR, KEY_ZOHAR, KEY_ASR, KEY_MAGRIB, KEY_ISHA },
					KEY_USER_ID + "=?",
					new String[] { String.valueOf(user.getActiveUserId()) },
					null, null, KEY_DATE + " DESC", null);
		}

		public long getMostRecentSalahDate(PrayerType type) {

			Cursor c = getPrayers(type);

			if (c == null)
				return 0;

			c.moveToFirst();
			long recentDate = c.getLong(c.getColumnIndex(KEY_DATE));
			c.close();
			return recentDate;
		}

		public long getDate(long id) {

			Cursor c = get(id);

			if (c == null)
				return 0;

			c.moveToFirst();
			long recentDate = c.getLong(c.getColumnIndex(KEY_DATE));
			c.close();
			return recentDate;
		}

		public Cursor getPrayers(PrayerType type, int limit) {
			return db.query(TABLE_NAME, new String[] { KEY_PRAYER_ID, KEY_DATE,
					KEY_FAJR, KEY_ZOHAR, KEY_ASR, KEY_MAGRIB, KEY_ISHA },
					KEY_USER_ID + "=? AND " + KEY_TYPE + "=?",
					new String[] { String.valueOf(user.getActiveUserId()),
							String.valueOf(type.getValue()) }, null, null,
					KEY_DATE + " DESC", String.valueOf(limit));
		}

		public Cursor getPrayers(PrayerType type, int limit,
				boolean hidePerfectDays) {

			if (hidePerfectDays) {
				return db.query(TABLE_NAME, new String[] { KEY_PRAYER_ID,
						KEY_DATE, KEY_FAJR, KEY_ZOHAR, KEY_ASR, KEY_MAGRIB,
						KEY_ISHA }, KEY_USER_ID + "=? AND " + KEY_TYPE
						+ "=? AND NOT (" + KEY_FAJR + "=1 AND " + KEY_ZOHAR
						+ "=1 AND " + KEY_ASR + "=1 AND " + KEY_MAGRIB
						+ "=1 AND " + KEY_ISHA + "=1" + ")",
						new String[] { String.valueOf(user.getActiveUserId()),
								String.valueOf(type.getValue()) }, null, null,
						KEY_DATE + " DESC", String.valueOf(limit));
			} else {
				return getPrayers(type, limit);
			}
		}

		public Cursor getPrayers(PrayerType type, boolean hidePerfectDays) {

			if (hidePerfectDays) {
				return db.query(TABLE_NAME, new String[] { KEY_PRAYER_ID,
						KEY_DATE, KEY_FAJR, KEY_ZOHAR, KEY_ASR, KEY_MAGRIB,
						KEY_ISHA }, KEY_USER_ID + "=? AND " + KEY_TYPE
						+ "=? AND NOT (" + KEY_FAJR + "=1 AND " + KEY_ZOHAR
						+ "=1 AND " + KEY_ASR + "=1 AND " + KEY_MAGRIB
						+ "=1 AND " + KEY_ISHA + "=1" + ")",
						new String[] { String.valueOf(user.getActiveUserId()),
								String.valueOf(type.getValue()) }, null, null,
						KEY_DATE + " DESC", null);
			} else {
				return getPrayers(type);
			}
		}

		public Cursor getPrayers(PrayerType type) {
			return db.query(TABLE_NAME, new String[] { KEY_PRAYER_ID, KEY_DATE,
					KEY_FAJR, KEY_ZOHAR, KEY_ASR, KEY_MAGRIB, KEY_ISHA },
					KEY_USER_ID + "=? AND " + KEY_TYPE + "=?",
					new String[] { String.valueOf(user.getActiveUserId()),
							String.valueOf(type.getValue()) }, null, null,
					KEY_DATE + " DESC", null);
		}

		public Cursor get(long id) {
			return db.query(TABLE_NAME, new String[] { KEY_PRAYER_ID, KEY_DATE,
					KEY_FAJR, KEY_ZOHAR, KEY_ASR, KEY_MAGRIB, KEY_ISHA },
					KEY_USER_ID + "=? AND " + KEY_PRAYER_ID + "=?",
					new String[] { String.valueOf(user.getActiveUserId()),
							String.valueOf(id) }, null, null, null, null);
		}

		public Cursor get(String prayerDate, PrayerType type) {
			Date date = Util.parseDate(prayerDate);
			Cursor c = db.query(
					TABLE_NAME,
					new String[] { KEY_PRAYER_ID },
					KEY_USER_ID + "=? and " + KEY_DATE + "=? and " + KEY_TYPE
							+ "=?",
					new String[] { String.valueOf(user.getActiveUserId()),
							String.valueOf(date.getTime() / 1000),
							String.valueOf(type.getValue()) }, null, null,
					null, null);
			if (c == null) {
				return null;
			}

			if (c.moveToFirst()) {
				int prayerId = c.getInt(0);
				c.close();
				return get(prayerId);
			}
			c.close();
			return null;
		}

		public Cursor get(String prayerDate) {
			Date date = Util.parseDate(prayerDate);
			return db.query(TABLE_NAME, new String[] { KEY_PRAYER_ID },
					KEY_USER_ID + "=? and " + KEY_DATE + "=?",
					new String[] { String.valueOf(user.getActiveUserId()),
							String.valueOf(date.getTime() / 1000) }, null,
					null, null, null);
		}

		public long isExist(String prayerDate, PrayerType type) {
			long prayerId = -1;

			Cursor c = get(prayerDate, type);
			if (c == null) {
				return prayerId;
			}

			if (c.moveToFirst()) {
				prayerId = c.getInt(c.getColumnIndex(KEY_PRAYER_ID));
			}
			c.close();

			return prayerId;
		}

		public long isExist(String prayerDate) {
			long prayerId = -1;

			Cursor c = get(prayerDate);
			if (c != null && c.moveToFirst()) {
				prayerId = c.getInt(c.getColumnIndex(KEY_PRAYER_ID));
			}
			c.close();
			return prayerId;
		}

		public Cursor getCounts(PrayerType type, long userId) {
			String sql = "SELECT Sum(" + Prayers.KEY_FAJR + "), " + "Sum("
					+ Prayers.KEY_ZOHAR + ")," + "Sum(" + Prayers.KEY_ASR
					+ "), " + "Sum(" + Prayers.KEY_MAGRIB + "), " + "Sum("
					+ Prayers.KEY_ISHA + ") FROM " + TABLE_NAME + " WHERE "
					+ Prayers.KEY_TYPE + "=? AND " + Prayers.KEY_USER_ID + "=?";
			return db.rawQuery(
					sql,
					new String[] { String.valueOf(type.getValue()),
							String.valueOf(userId) });
		}
	}

	public class Tasbeeh {
		public static final String TABLE_NAME = "Tasbeeh";
		public static final String KEY_ID = "_id";
		public static final String KEY_NAME = "name";
		public static final String KEY_TASBEEH = "tasbeeh";
		public static final String KEY_MEANING = "meaning";
		public static final String KEY_DEFAULT_COUNT = "default_count";
		public static final String KEY_NOTES = "notes";
		public static final String KEY_ORDER = "tasbeeh_order";

		private final static String CREATE_SQL = "CREATE TABLE IF NOT EXISTS "
				+ TABLE_NAME + " (" + KEY_ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_NAME
				+ " VARCHAR, " + KEY_TASBEEH + " VARCHAR, " + KEY_MEANING
				+ " VARCHAR, " + KEY_DEFAULT_COUNT + " INTEGER, " + KEY_NOTES
				+ " VARCHAR, " + KEY_ORDER + " INTEGER );";

		public long add(String name, String tasbeeh, String meaning,
				int default_count, String notes, int order) {
			ContentValues initialValues = new ContentValues();
			initialValues.put(KEY_NAME, name);
			initialValues.put(KEY_TASBEEH, tasbeeh);
			initialValues.put(KEY_MEANING, meaning);
			initialValues.put(KEY_DEFAULT_COUNT, default_count);
			initialValues.put(KEY_NOTES, notes);
			initialValues.put(KEY_ORDER, order);

			long id = db.insert(TABLE_NAME, null, initialValues);

			return id;
		}

		public long update(long tasbeehId, String name, String tasbeeh,
				String meaning, int default_count, String notes) {
			ContentValues initialValues = new ContentValues();
			initialValues.put(KEY_NAME, name);
			initialValues.put(KEY_TASBEEH, tasbeeh);
			initialValues.put(KEY_MEANING, meaning);
			initialValues.put(KEY_DEFAULT_COUNT, default_count);
			initialValues.put(KEY_NOTES, notes);

			return db.update(TABLE_NAME, initialValues, KEY_ID + "=?",
					new String[] { String.valueOf(tasbeehId) });
		}

		public long updateTasbeehOrder(long tasbeehId, int order) {
			ContentValues initialValues = new ContentValues();
			initialValues.put(KEY_ORDER, order);
			return db.update(TABLE_NAME, initialValues, KEY_ID + "=?",
					new String[] { String.valueOf(tasbeehId) });
		}

		public Cursor get() {
			Cursor c = db.query(TABLE_NAME, new String[] { KEY_ID, KEY_NAME,
					KEY_TASBEEH, KEY_MEANING, KEY_DEFAULT_COUNT, KEY_NOTES,
					KEY_ORDER }, null, null, null, null, KEY_ORDER + " ASC",
					null);
			return c;
		}

		public Cursor getTasbeehToExport() {
			Cursor c = db.query(TABLE_NAME, new String[] { KEY_NAME,
					KEY_TASBEEH, KEY_MEANING, KEY_DEFAULT_COUNT, KEY_NOTES },
					null, null, null, null, KEY_ORDER + " ASC", null);
			return c;
		}

		public Cursor getTasbeeh(long tasbeehId) {
			return db.query(TABLE_NAME, new String[] { KEY_ID, KEY_NAME,
					KEY_TASBEEH, KEY_MEANING, KEY_DEFAULT_COUNT, KEY_NOTES,
					KEY_ORDER }, KEY_ID + "=?",
					new String[] { String.valueOf(tasbeehId) }, null, null,
					null, null);
		}

		public long isTasbeehExist(String tasbeehName) {
			Cursor c = db.query(TABLE_NAME, new String[] { KEY_ID, KEY_NAME,
					KEY_TASBEEH, KEY_MEANING, KEY_DEFAULT_COUNT, KEY_NOTES },
					KEY_NAME + "=?", new String[] { tasbeehName }, null, null,
					null, null);

			long tasbeehId = -1;
			if (c != null) {
				if (c.moveToFirst()) {
					tasbeehId = c.getLong(c.getColumnIndex(KEY_ID));
				}
				c.close();
			}
			return tasbeehId;
		}

		public long getTasbeehId(int tasbeehOrder) {
			Cursor c = db.query(TABLE_NAME, new String[] { KEY_ID }, KEY_ORDER
					+ "=?", new String[] { String.valueOf(tasbeehOrder) },
					null, null, null, null);

			long tasbeehId = -1;
			if (c != null) {
				if (c.moveToFirst()) {
					tasbeehId = c.getLong(c.getColumnIndex(KEY_ID));
				}
				c.close();
			}
			return tasbeehId;
		}

		public int getTasbeehOrder(long tasbeehId) {
			Cursor c = getTasbeeh(tasbeehId);
			int tasbeehOrder = -1;
			if (c != null) {
				if (c.moveToFirst()) {
					tasbeehOrder = c.getInt(c.getColumnIndex(KEY_ORDER));
				}
				c.close();
			}
			return tasbeehOrder;
		}

		public int getMaxTasbeehOrder() {
			String sql = "SELECT Max(" + KEY_ORDER + ") FROM " + TABLE_NAME;
			Cursor c = db.rawQuery(sql, null);
			int maxOrder = 0;
			if (c != null) {
				if (c.moveToFirst()) {
					maxOrder = c.getInt(0);
				}
				c.close();
			}
			return maxOrder;
		}

		public int getMaxTasbeehId() {
			String sql = "SELECT Max(" + KEY_ID + ") FROM " + TABLE_NAME;
			Cursor c = db.rawQuery(sql, null);
			int maxId = -1;
			if (c != null) {
				if (c.moveToFirst()) {
					maxId = c.getInt(0);
				}
				c.close();
			}
			return maxId;
		}

		public int getMinTasbeehId() {
			String sql = "SELECT Min(" + KEY_ID + ") FROM " + TABLE_NAME;
			Cursor c = db.rawQuery(sql, null);
			int maxId = -1;
			if (c != null) {
				if (c.moveToFirst()) {
					maxId = c.getInt(0);
				}
				c.close();
			}
			return maxId;
		}
	}

	public class TasbeehCount {
		public static final String TABLE_NAME = "TasbeehCount";
		public static final String KEY_ID = "_id";
		public static final String KEY_USER_ID = "userid";
		public static final String KEY_TASBEEH_ID = "tasbeehid";
		public static final String KEY_DATE = "date";
		public static final String KEY_COUNT_TODAY = "today";
		public static final String KEY_COUNT_OVERALL = "overall";

		private final static String CREATE_SQL = "CREATE TABLE IF NOT EXISTS "
				+ TABLE_NAME + " (" + KEY_ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_DATE
				+ " VARCHAR, " + KEY_USER_ID + " INTEGER, " + KEY_TASBEEH_ID
				+ " INTEGER, " + KEY_COUNT_TODAY + " INTEGER, "
				+ KEY_COUNT_OVERALL + " INTEGER);";

		public long add(String date, long tasbeehId, int todayCount,
				int overallCount) {
			ContentValues initialValues = new ContentValues();
			initialValues.put(KEY_DATE, date);
			initialValues.put(KEY_USER_ID, user.getActiveUserId());
			initialValues.put(KEY_TASBEEH_ID, tasbeehId);
			initialValues.put(KEY_COUNT_TODAY, todayCount);
			initialValues.put(KEY_COUNT_OVERALL, overallCount);
			return db.insert(TABLE_NAME, null, initialValues);
		}

		public long update(String date, long tasbeehId, Integer todayCount,
				int overallCount) {
			ContentValues initialValues = new ContentValues();
			initialValues.put(KEY_DATE, date);
			initialValues.put(KEY_COUNT_TODAY, todayCount);
			initialValues.put(KEY_COUNT_OVERALL, overallCount);
			return db.update(
					TABLE_NAME,
					initialValues,
					KEY_TASBEEH_ID + "=? and " + KEY_USER_ID + "=?",
					new String[] { String.valueOf(tasbeehId),
							String.valueOf(user.getActiveUserId()) });
		}

		public long resetCount(long tasbeehId, int todayCount, int overallCount) {
			ContentValues initialValues = new ContentValues();
			initialValues.put(KEY_COUNT_TODAY, todayCount);
			initialValues.put(KEY_COUNT_OVERALL, overallCount);
			if (tasbeehId == -1) {
				return db
						.update(TABLE_NAME, initialValues, KEY_USER_ID + "=?",
								new String[] { String.valueOf(user
										.getActiveUserId()) });
			} else {
				return db.update(
						TABLE_NAME,
						initialValues,
						KEY_TASBEEH_ID + "=? and " + KEY_USER_ID + "=?",
						new String[] { String.valueOf(tasbeehId),
								String.valueOf(user.getActiveUserId()) });
			}
		}

		public Cursor get(long tasbeehId) {
			return db.query(
					TABLE_NAME,
					new String[] { KEY_DATE, KEY_TASBEEH_ID, KEY_COUNT_TODAY,
							KEY_COUNT_OVERALL },
					KEY_TASBEEH_ID + "=? and " + KEY_USER_ID + "=?",
					new String[] { String.valueOf(tasbeehId),
							String.valueOf(user.getActiveUserId()) }, null,
					null, null, null);
		}

		public boolean isTasbeehExist(long tasbeehId) {
			Cursor c = db.query(TABLE_NAME, new String[] { KEY_TASBEEH_ID },
					KEY_TASBEEH_ID + "=?",
					new String[] { String.valueOf(tasbeehId) }, null, null,
					null, null);

			boolean flag = false;
			if (c != null) {
				if (c.moveToFirst()) {
					flag = true;
				}
				c.close();
			}
			return flag;
		}

		public Cursor getTasbeehCountToExport() {
			String sql = "SELECT " + KEY_COUNT_OVERALL + ", "
					+ Tasbeeh.KEY_NAME + " FROM " + TABLE_NAME + " tc, "
					+ Tasbeeh.TABLE_NAME + " t WHERE tc." + KEY_TASBEEH_ID
					+ "=t." + Tasbeeh.KEY_ID;
			return db.rawQuery(sql, null);
		}
	}

	public class Notification {
		public static final String TABLE_NAME = "Notification";
		public static final String KEY_ID = "_id";
		public static final String KEY_NAME = "name";
		public static final String KEY_STATE = "status";
		public static final String KEY_HOUR = "hour";
		public static final String KEY_MINUTE = "minute";

		private final static String CREATE_SQL = "CREATE TABLE IF NOT EXISTS "
				+ TABLE_NAME + " (" + KEY_ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_NAME
				+ " VARCHAR, " + KEY_STATE + " INTEGER, " + KEY_HOUR
				+ " INTEGER, " + KEY_MINUTE + " INTEGER);";

		public long add(String name, int status, int hour, int minute) {
			ContentValues initialValues = new ContentValues();
			initialValues.put(KEY_NAME, name);
			initialValues.put(KEY_STATE, status);
			initialValues.put(KEY_HOUR, hour);
			initialValues.put(KEY_MINUTE, minute);
			return db.insert(TABLE_NAME, null, initialValues);
		}

		private long update(String name, Integer state, Integer hour,
				Integer minute) {
			ContentValues initialValues = new ContentValues();
			initialValues.put(KEY_NAME, name);
			initialValues.put(KEY_STATE, state);
			initialValues.put(KEY_HOUR, hour);
			initialValues.put(KEY_MINUTE, minute);
			return db.update(TABLE_NAME, initialValues, KEY_NAME + "=?",
					new String[] { name });
		}

		public void enableNotification(String name, int hour, int minute) {

			this.update(name, 1, hour, minute);
		}

		public void disableNotification(String name) {

			this.update(name, 0, null, null);
		}

		public boolean isExist() {
			boolean flag = false;

			Cursor c = get();
			if (c == null) {
				return flag;
			}

			if (c.moveToFirst()) {
				flag = true;
			}
			c.close();

			return flag;
		}

		public Cursor get(String name) {
			return db.query(TABLE_NAME, new String[] { KEY_NAME, KEY_STATE,
					KEY_HOUR, KEY_MINUTE }, KEY_NAME + "=?",
					new String[] { name }, null, null, null, null);
		}

		public boolean getState(String name) {
			Cursor c = db.query(TABLE_NAME, new String[] { KEY_STATE },
					KEY_NAME + "=?", new String[] { name }, null, null, null,
					null);

			if (c == null) {
				return false;
			}

			int status = 0;
			if (c.moveToFirst()) {
				status = c.getInt(c.getColumnIndex(Notification.KEY_STATE));
				c.close();
			}
			return status == 1;
		}

		public int getHour(String name) {
			int hour = 0;
			Cursor c = db.query(TABLE_NAME, new String[] { KEY_HOUR }, KEY_NAME
					+ "=?", new String[] { name }, null, null, null, null);

			if (c != null) {

				c.moveToFirst();
				hour = c.getInt(c.getColumnIndex(Notification.KEY_HOUR));
				c.close();
				return hour;
			}

			return 0;
		}

		public int getMinute(String name) {
			int minute = 0;
			Cursor c = db.query(TABLE_NAME, new String[] { KEY_MINUTE },
					KEY_NAME + "=?", new String[] { name }, null, null, null,
					null);

			if (c != null) {
				c.moveToFirst();
				minute = c.getInt(c.getColumnIndex(Notification.KEY_MINUTE));
				c.close();
				return minute;
			}

			return 0;
		}

		public CharSequence getTime(String name) {
			int hour = 0;
			int minute = 0;
			Cursor c = db.query(TABLE_NAME,
					new String[] { KEY_HOUR, KEY_MINUTE }, KEY_NAME + "=?",
					new String[] { name }, null, null, null, null);

			if (c != null) {

				c.moveToFirst();
				hour = c.getInt(c.getColumnIndex(Notification.KEY_HOUR));
				minute = c.getInt(c.getColumnIndex(Notification.KEY_MINUTE));
				c.close();
				return (Util.get12HourFormatDate(hour, minute));
			}

			return null;
		}

		public Cursor get() {
			return db.query(TABLE_NAME, new String[] { KEY_NAME, KEY_STATE,
					KEY_HOUR, KEY_MINUTE }, null, null, null, null, null, null);
		}

	}

	public class Qasr {

		public static final String TABLE_NAME = "Qasr";
		public static final String KEY_ID = "_id";
		public static final String KEY_PRAYER_ID = "prayer_id";
		public static final String KEY_USER_ID = "user_id";
		public static final String KEY_DATE = "date";
		public static final String KEY_FAJR = "fajr";
		public static final String KEY_ZOHAR = "zohar";
		public static final String KEY_ASR = "asr";
		public static final String KEY_MAGRIB = "magrib";
		public static final String KEY_ISHA = "isha";

		private final static String CREATE_SQL = "CREATE TABLE IF NOT EXISTS "
				+ TABLE_NAME + " (" + KEY_ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_DATE
				+ " INTEGER, " + KEY_PRAYER_ID + " INTEGER, " + KEY_USER_ID
				+ " INTEGER, " + KEY_FAJR + " INTEGER, " + KEY_ZOHAR
				+ " INTEGER, " + KEY_ASR + " INTEGER, " + KEY_MAGRIB
				+ " INTEGER, " + KEY_ISHA + " INTEGER );";

		public boolean delete(long prayerId) {
			Util.e("Deleting Qasr prayer id: " + prayerId);
			return db.delete(TABLE_NAME, KEY_PRAYER_ID + "=?",
					new String[] { String.valueOf(prayerId) }) > 0;
		}

		public long add(long prayerId, String prayerDate, int fajr, int zohar,
				int asr, int magrib, int isha) {
			ContentValues initialValues = new ContentValues();

			long date = Util.parseDate(prayerDate).getTime() / 1000;

			initialValues.put(KEY_PRAYER_ID, prayerId);
			initialValues.put(KEY_DATE, date);
			initialValues.put(KEY_FAJR, fajr);
			initialValues.put(KEY_ZOHAR, zohar);
			initialValues.put(KEY_ASR, asr);
			initialValues.put(KEY_MAGRIB, magrib);
			initialValues.put(KEY_ISHA, isha);
			initialValues.put(KEY_USER_ID, user.getActiveUserId());
			return db.insert(TABLE_NAME, null, initialValues);
		}

		public long update(long prayerId, int fajr, int zohar, int asr,
				int magrib, int isha) {
			ContentValues initialValues = new ContentValues();
			initialValues.put(KEY_PRAYER_ID, prayerId);
			initialValues.put(KEY_FAJR, fajr);
			initialValues.put(KEY_ZOHAR, zohar);
			initialValues.put(KEY_ASR, asr);
			initialValues.put(KEY_MAGRIB, magrib);
			initialValues.put(KEY_ISHA, isha);

			return db.update(TABLE_NAME, initialValues, KEY_PRAYER_ID + "=?",
					new String[] { String.valueOf(prayerId) });
		}

		public Cursor get(long prayerId) {
			return db.query(TABLE_NAME, new String[] { KEY_ID, KEY_FAJR,
					KEY_ZOHAR, KEY_ASR, KEY_MAGRIB, KEY_ISHA }, KEY_PRAYER_ID
					+ "=?", new String[] { String.valueOf(prayerId) }, null,
					null, null, null);
		}

		public long get(String prayerDate) {
			Date date = Util.parseDate(prayerDate);
			long prayerId = -1;
			Cursor c = db.query(TABLE_NAME, new String[] { KEY_PRAYER_ID,
					KEY_FAJR, KEY_ZOHAR, KEY_ASR, KEY_MAGRIB, KEY_ISHA },
					KEY_DATE + "=?", new String[] { String.valueOf(date) },
					null, null, null, null);

			if (c != null) {
				if (c.moveToFirst()) {
					prayerId = c.getInt(c.getColumnIndex(KEY_PRAYER_ID));
				}
				c.close();
			}
			return prayerId;
		}

		public boolean isExist(long prayerId) {
			Cursor c = get(prayerId);
			if (c != null && c.moveToFirst()) {
				c.close();
				return true;
			}

			return false;
		}

		public boolean isExist(String prayerDate) {
			return (get(prayerDate) > -1);
		}

		public Cursor getQasrPrayersToExport() {
			return db.query(TABLE_NAME, new String[] { KEY_DATE, KEY_FAJR,
					KEY_ZOHAR, KEY_ASR, KEY_MAGRIB, KEY_ISHA }, KEY_USER_ID
					+ "=?",
					new String[] { String.valueOf(user.getActiveUserId()) },
					null, null, KEY_DATE + " DESC", null);
		}

	}

	public class Menstruation {

		public static final String TABLE_NAME = "Menstruation";
		public static final String KEY_ID = "_id";
		public static final String KEY_PRAYER_ID = "prayer_id";
		public static final String KEY_USER_ID = "user_id";
		public static final String KEY_DATE = "date";
		public static final String KEY_FAJR = "fajr";
		public static final String KEY_ZOHAR = "zohar";
		public static final String KEY_ASR = "asr";
		public static final String KEY_MAGRIB = "magrib";
		public static final String KEY_ISHA = "isha";

		private final static String CREATE_SQL = "CREATE TABLE IF NOT EXISTS "
				+ TABLE_NAME + " (" + KEY_ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_DATE
				+ " INTEGER, " + KEY_PRAYER_ID + " INTEGER, " + KEY_USER_ID
				+ " INTEGER, " + KEY_FAJR + " INTEGER, " + KEY_ZOHAR
				+ " INTEGER, " + KEY_ASR + " INTEGER, " + KEY_MAGRIB
				+ " INTEGER, " + KEY_ISHA + " INTEGER );";

		public boolean delete(long prayerId) {
			Util.e("Deleting Qasr prayer id: " + prayerId);
			return db.delete(TABLE_NAME, KEY_PRAYER_ID + "=?",
					new String[] { String.valueOf(prayerId) }) > 0;
		}

		public long add(long prayerId, String prayerDate, int fajr, int zohar,
				int asr, int magrib, int isha) {
			ContentValues initialValues = new ContentValues();

			long date = Util.parseDate(prayerDate).getTime() / 1000;

			initialValues.put(KEY_PRAYER_ID, prayerId);
			initialValues.put(KEY_DATE, date);
			initialValues.put(KEY_FAJR, fajr);
			initialValues.put(KEY_ZOHAR, zohar);
			initialValues.put(KEY_ASR, asr);
			initialValues.put(KEY_MAGRIB, magrib);
			initialValues.put(KEY_ISHA, isha);
			initialValues.put(KEY_USER_ID, user.getActiveUserId());
			return db.insert(TABLE_NAME, null, initialValues);
		}

		public long update(long prayerId, int fajr, int zohar, int asr,
				int magrib, int isha) {
			ContentValues initialValues = new ContentValues();
			initialValues.put(KEY_FAJR, fajr);
			initialValues.put(KEY_ZOHAR, zohar);
			initialValues.put(KEY_ASR, asr);
			initialValues.put(KEY_MAGRIB, magrib);
			initialValues.put(KEY_ISHA, isha);

			return db.update(TABLE_NAME, initialValues, KEY_PRAYER_ID + "=?",
					new String[] { String.valueOf(prayerId) });
		}

		public Cursor get(long prayerId) {
			return db.query(TABLE_NAME, new String[] { KEY_ID, KEY_FAJR,
					KEY_ZOHAR, KEY_ASR, KEY_MAGRIB, KEY_ISHA }, KEY_PRAYER_ID
					+ "=?", new String[] { String.valueOf(prayerId) }, null,
					null, null, null);
		}

		public long get(String prayerDate) {
			Date date = Util.parseDate(prayerDate);
			long prayerId = -1;
			Cursor c = db.query(TABLE_NAME, new String[] { KEY_PRAYER_ID,
					KEY_FAJR, KEY_ZOHAR, KEY_ASR, KEY_MAGRIB, KEY_ISHA },
					KEY_DATE + "=?", new String[] { String.valueOf(date) },
					null, null, null, null);

			if (c != null) {

				if (c.moveToFirst()) {
					prayerId = c.getInt(c.getColumnIndex(KEY_PRAYER_ID));
				}
				c.close();
			}
			return prayerId;
		}

		public boolean isExist(long prayerId) {
			Cursor c = get(prayerId);
			if (c != null && c.moveToFirst()) {
				c.close();
				return true;
			}

			return false;
		}

		public Cursor getCounts(long userId) {
			String sql = "SELECT Sum(" + Menstruation.KEY_FAJR + "), " + "Sum("
					+ Menstruation.KEY_ZOHAR + ")," + "Sum("
					+ Menstruation.KEY_ASR + "), " + "Sum("
					+ Menstruation.KEY_MAGRIB + "), " + "Sum("
					+ Menstruation.KEY_ISHA + ") FROM " + TABLE_NAME
					+ " WHERE " + Menstruation.KEY_USER_ID + "=?";
			return db.rawQuery(sql, new String[] { String.valueOf(userId) });
		}

		public Cursor getMenstruationPrayersToExport() {
			return db.query(TABLE_NAME, new String[] { KEY_DATE, KEY_FAJR,
					KEY_ZOHAR, KEY_ASR, KEY_MAGRIB, KEY_ISHA }, KEY_USER_ID
					+ "=?",
					new String[] { String.valueOf(user.getActiveUserId()) },
					null, null, KEY_DATE + " DESC", null);
		}
	}

	public class Ramdhan {
		public static final String TABLE_NAME = "Ramdhan";
		public static final String KEY_ID = "_id";
		public static final String KEY_DATE = "ramdhan_date";
		public static final String KEY_USER_ID = "user_id";
		public static final String KEY_SIYAM = "siyam";
		public static final String KEY_TARAWEEH = "taraweeh";
		public static final String KEY_QURAN = "quran";
		public static final String KEY_QURAN_JUZ = "juz";

		private final static String CREATE_SQL = "CREATE TABLE IF NOT EXISTS "
				+ TABLE_NAME + " (" + KEY_ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_DATE
				+ " INTEGER, " + KEY_USER_ID + " INTEGER, " + KEY_SIYAM
				+ " INTEGER, " + KEY_TARAWEEH + " INTEGER, " + KEY_QURAN
				+ " INTEGER, " + KEY_QURAN_JUZ + " DECIMAL );";

		public boolean delete(long id) {
			Util.e("Deleting ramdhan details id: " + id);
			return db.delete(TABLE_NAME, KEY_ID + "=?",
					new String[] { String.valueOf(id) }) > 0;
		}

		public long add(String date, int fast, int taraweeh, int quran,
				float quranVol) {
			ContentValues initialValues = new ContentValues();

			long dbDate = Util.parseDate(date).getTime() / 1000;

			initialValues.put(KEY_DATE, dbDate);
			initialValues.put(KEY_SIYAM, fast);
			initialValues.put(KEY_TARAWEEH, taraweeh);
			initialValues.put(KEY_QURAN, quran);
			initialValues.put(KEY_QURAN_JUZ, quranVol);
			initialValues.put(KEY_USER_ID, user.getActiveUserId());
			return db.insert(TABLE_NAME, null, initialValues);
		}

		public long update(long id, int fast, int taraweeh, int quran,
				float quranVol) {
			ContentValues initialValues = new ContentValues();
			initialValues.put(KEY_SIYAM, fast);
			initialValues.put(KEY_TARAWEEH, taraweeh);
			initialValues.put(KEY_QURAN, quran);
			initialValues.put(KEY_QURAN_JUZ, quranVol);

			return db.update(TABLE_NAME, initialValues, KEY_ID + "=?",
					new String[] { String.valueOf(id) });
		}

		public Cursor get(long id) {
			return db.query(TABLE_NAME, new String[] { KEY_ID, KEY_SIYAM,
					KEY_TARAWEEH, KEY_QURAN, KEY_QURAN_JUZ, KEY_DATE }, KEY_ID
					+ "=?", new String[] { String.valueOf(id) }, null, null,
					null, null);
		}

		public Cursor get() {
			return db.query(TABLE_NAME, new String[] { KEY_ID, KEY_DATE,
					KEY_SIYAM, KEY_TARAWEEH, KEY_QURAN, KEY_QURAN_JUZ },
					KEY_USER_ID + "=?",
					new String[] { String.valueOf(user.getActiveUserId()) },
					null, null, KEY_DATE + " DESC", null);
		}

		public float getTotalQuranJuz() {
			String sql = "SELECT Sum(" + Ramdhan.KEY_QURAN_JUZ + ") FROM "
					+ Ramdhan.TABLE_NAME + " WHERE " + Ramdhan.KEY_USER_ID
					+ "=?";
			Cursor c = db.rawQuery(sql,
					new String[] { String.valueOf(user.getActiveUserId()) });
			float count = 0;
			if (c != null) {
				if (c.moveToFirst()) {
					count = c.getFloat(0);
				}
			}
			return count;
		}

		public Cursor get(String date) {
			Date dbDate = Util.parseDate(date);
			return db.query(TABLE_NAME, new String[] { KEY_ID, KEY_DATE,
					KEY_SIYAM, KEY_TARAWEEH, KEY_QURAN, KEY_QURAN_JUZ },
					KEY_DATE + "=?",
					new String[] { String.valueOf(dbDate.getTime() / 1000) },
					null, null, null, null);
		}

		public boolean isExist(long id) {
			Cursor c = get(id);
			if (c != null && c.moveToFirst()) {
				c.close();
				return true;
			}

			return false;
		}

		public long isExist(String date) {
			Cursor c = get(date);
			long id = -1;
			if (c != null && c.moveToFirst()) {
				id = c.getLong(c.getColumnIndex(Ramdhan.KEY_ID));
				c.close();
			}
			return id;
		}

		public Cursor getRamdhanDetailsToExport() {
			return db.query(TABLE_NAME, new String[] { KEY_DATE, KEY_SIYAM,
					KEY_TARAWEEH, KEY_QURAN, KEY_QURAN_JUZ }, KEY_USER_ID
					+ "=?",
					new String[] { String.valueOf(user.getActiveUserId()) },
					null, null, KEY_DATE + " DESC", null);
		}
	}

	public Cursor getAllTables() {
		String sql = "SELECT * FROM sqlite_master";
		return db.rawQuery(sql, new String[0]);
	}

	public void dropTable() {
		db.execSQL("DROP TABLE IF EXISTS " + Qasr.TABLE_NAME);
	}

	public void createTable() {
		db.execSQL(Qasr.CREATE_SQL);
	}

	public DBAdapter open() throws SQLException {
		db = DBHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		try {
			DBHelper.close();
		} catch (Exception e) {
			Util.e("Exception occurred: " + e);
		}
	}
}
