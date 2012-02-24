package com.primer.world.android;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * en:This class makes the foundation of this app's database.<br>
 * jp:データベースの基幹をなす部分。SQL文で、テーブルの作成、更新を行う。
 */
public class DataBaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "mydatabase.db";
	private static final int DATABASE_VERSION = 1;

	/**
	 * table<br>
	 * WORD_BOOKS<br>
	 * columns<br>
	 * WORD_BOOK_ID, WORD_BOOK_NAME, TESTED_DATE,<br>
	 * TEST_MODE, BOOK_MARK, URL_ID_1, URL_ID_2, URL_ID_3
	 */
	public static final String TABLE_WORD_BOOK = "WORD_BOOK";
	/**
	 * en:This ID identifies each wordbooks.<br>
	 * jp:単語帳ID。単語帳を区別するために用いられる
	 */
	public static final String COLUMN_WORD_BOOK_ID = "WORD_BOOK_ID";
	public static final String COLUMN_WORD_BOOK_NAME = "WORD_BOOK_NAME";
	/**
	 * en:In order to save the date easily, we use INTEGER for this column.<br>
	 * jp:日付はフォーマットしやすくするため、INTEGER型にする（javaはlong）
	 */
	public static final String COLUMN_TESTED_DATE = "TESTED_DATE";
	/**
	 * en:This shows which test mode each wordbook is using.<br>
	 * jp:テストのモード（正順、ランダムなどがある）
	 */
	public static final String COLUMN_TEST_MODE = "TEST_MODE";
	/**
	 * en:This book mark is used for memorizing where to start the test.<br>
	 * jp:テスト時に開始する単語IDで、次にテストする場所をブックマークする
	 */
	public static final String COLUMN_BOOK_MARK = "BOOK_MARK";
	/**
	 * en:These are used for registering urls.<br>
	 * jp:単語帳において調べたいURLを保存しておく。 デフォルトでID 1,2,3が入っている
	 */
	public static final String COLUMN_URL_ID_1 = "URL_ID_1";
	public static final String COLUMN_URL_ID_2 = "URL_ID_2";
	public static final String COLUMN_URL_ID_3 = "URL_ID_3";

	/**
	 * table<br>
	 * WORDS<br>
	 * column<br>
	 * WORD_ID, WORD_BOOK_ID, QUESTION, ANSWER,<br>
	 * EXPLANATION, TEST_COUNT, WRONG_COUNT, ACCURACY_RATE
	 */
	public static final String TABLE_WORD = "WORD";
	public static final String COLUMN_WORD_ID = "WORD_ID";
	public static final String COLUMN_QUESTION = "QUESTION";
	public static final String COLUMN_ANSWER = "ANSWER";
	public static final String COLUMN_EXPLANATION = "EXPLANATION";
	/**
	 * en:This is used for saving how many times have 
	 * you tested the wrodbook<br>
	 * jp:単語帳ごとにテストした回数
	 */
	public static final String COLUMN_TEST_COUNT = "TEST_COUNT";
	/**
	 * en:This is used for saving how many times have you got wrong answer for
	 * the word.<br>
	 * jp:その単語を間違えた回数
	 */
	public static final String COLUMN_WRONG_COUNT = "WRONG_COUNT";
	/**
	 * jp:単語ごとの正答率(Correct Answer Rate)をジャンル分けしたもの<br>
	 * （25...25%以下,50...50%以下,75...75%以下,100...100%以下）の四つがある
	 */
	public static final String COLUMN_ACCURACY_RATE = "ACCURACY_RATE";

	/**
	 * table<br>
	 * RECORD<br>
	 * column<br>
	 * WORD_BOOK_ID, TESTED_DATE, AVERAGE_ACCURACY_RATE
	 */
	public static final String TABLE_RECORD = "RECORD";
	/**
	 * en:The average of accuracy rate of the whole word of the wordbook.
	 * jp:単語帳内の単語の平均正答率
	 */
	public static final String COLUMN_AVERAGE_ACCURACY_RATE 
											= "AVERAGE_ACCURACY_RATE";

	/**
	 * table<br>
	 * URL<br>
	 * column<br>
	 * URL_ID, NAME, URL
	 */
	public static final String TABLE_URL = "URL";
	public static final String COLUMN_URL_ID = "URL_ID";
	/** そのURLが指しているサイト名など */
	public static final String COLUMN_SITE_NAME = "SITE_NAME";
	/** ウェブで調べる機能で使われるURL */
	public static final String COLUMN_URL = "URL";

	private String[] defaultSiteNames = { "Google", "etymonline",
			"Dictionary.com" };
	private String[] defaultUrl = {
			"http://www.google.com/search?q=@word",
			"http://www.etymonline.com/index.php?allowed_in_frame=0"
					+ "&search=@word&searchmode=none",
			"http://dictionary.reference.com/browse/@word" };

	/** Constructor */
	public DataBaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// jp:データベース処理開始
		db.beginTransaction();
		try {
			// jp:テーブル作成を実行
			db.execSQL("CREATE TABLE " + TABLE_WORD_BOOK + " ("
					+ COLUMN_WORD_BOOK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ COLUMN_WORD_BOOK_NAME + " TEXT," + COLUMN_TESTED_DATE
					+ " INTEGER," + COLUMN_TEST_MODE + " INTEGER,"
					+ COLUMN_BOOK_MARK + " INTEGER," + COLUMN_URL_ID_1
					+ " INDEGER DEFAULT 1," + COLUMN_URL_ID_2
					+ " INDEGER DEFAULT 2," + COLUMN_URL_ID_3 + " INDEGER DEFAULT 3"
					+ " );");

			db.execSQL("CREATE TABLE " + TABLE_WORD + " (" + COLUMN_WORD_ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_WORD_BOOK_ID
					+ " INTEGER," + COLUMN_QUESTION + " TEXT," + COLUMN_ANSWER
					+ " TEXT," + COLUMN_EXPLANATION + " TEXT," + COLUMN_TEST_COUNT
					+ " INTEGER," + COLUMN_WRONG_COUNT + " INTEGER,"
					+ COLUMN_ACCURACY_RATE + " INTEGER" + " );");

			db.execSQL("CREATE TABLE " + TABLE_RECORD + " (" + COLUMN_WORD_BOOK_ID
					+ " INTEGER," + COLUMN_TESTED_DATE + " INTEGER,"
					+ COLUMN_AVERAGE_ACCURACY_RATE + " INTEGER" + " );");

			db.execSQL("CREATE TABLE " + TABLE_URL + " (" + COLUMN_URL_ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_SITE_NAME
					+ " TEXT," + COLUMN_URL + " TEXT" + " );");

			// jp:予め三つのサイト名とURLをTABLE_URLに登録しておく
			for (int i = 0; i < defaultSiteNames.length; i++) {
				ContentValues values = new ContentValues();
				values.put(COLUMN_SITE_NAME, defaultSiteNames[i]);
				values.put(COLUMN_URL, defaultUrl[i]);
				db.insert(TABLE_URL, null, values);
			}

			// jp:SQL処理を反映
			db.setTransactionSuccessful();
		} finally {
			// jp:データベース処理終了
			db.endTransaction();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		// jp:バージョンが新しくなったら更新
		if (newVersion > oldVersion) {
			final String TMP_TABLE_WORD_BOOK = "TMP_WORD_BOOK";
			final String TMP_TABLE_WORD = "TMP_WORD";
			final String TMP_TABLE_RECORD = "TMP_RECORD";
			final String TMP_TABLE_URL = "TMP_URL";

			// jp:以下、テーブルを新しいものに置き変える操作
			db.beginTransaction();
			try {
				// jp:今までのデータをコピーするための仮テーブル作成とデータ移行を実行
				db.execSQL("CREATE TABLE " + TMP_TABLE_WORD_BOOK
						+ " AS SELECT * FROM " + TABLE_WORD_BOOK);

				db.execSQL("CREATE TABLE " + TMP_TABLE_WORD + " AS SELECT * FROM "
						+ TABLE_WORD);

				db.execSQL("CREATE TABLE " + TMP_TABLE_RECORD
						+ " AS SELECT * FROM " + TABLE_RECORD);

				db.execSQL("CREATE TABLE " + TMP_TABLE_URL + " AS SELECT * FROM "
						+ TABLE_URL);

				// jp:今までのテーブルは必要なくなったので削除
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORD_BOOK + " ;");
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORD + " ;");
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECORD + " ;");
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_URL + " ;");

				// jp:新しい定義のテーブル作成を実行（バージョン１なので関係ない）
				onCreate(db);

				// jp:作成した新しいテーブルに既存のデータを移す
				db.execSQL("INSERT INTO " + TABLE_WORD_BOOK + " SELECT * FROM "
						+ TMP_TABLE_WORD_BOOK + " ;");
				db.execSQL("INSERT INTO " + TABLE_WORD + " SELECT * FROM "
						+ TMP_TABLE_WORD + " ;");
				db.execSQL("INSERT INTO " + TABLE_RECORD + " SELECT * FROM "
						+ TMP_TABLE_RECORD + " ;");
				db.execSQL("INSERT INTO " + TABLE_URL + " SELECT * FROM "
						+ TMP_TABLE_URL + " ;");

				// jp:仮テーブルは必要なくなったので削除
				db.execSQL("DROP TABLE IF EXISTS " + TMP_TABLE_WORD_BOOK + " ;");
				db.execSQL("DROP TABLE IF EXISTS " + TMP_TABLE_WORD + " ;");
				db.execSQL("DROP TABLE IF EXISTS " + TMP_TABLE_RECORD + " ;");
				db.execSQL("DROP TABLE IF EXISTS " + TMP_TABLE_URL + " ;");

				// jp:SQL処理を反映
				db.setTransactionSuccessful();
			} finally {
				// jp:データベース処理終了
				db.endTransaction();
			}
		}
	}
}
