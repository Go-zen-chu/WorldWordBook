package com.primer.world.android;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.primer.world.android.ListingRegisteredUrlActivity.SiteNameAndUrl;
import com.primer.world.android.TestRecordActivity.DateAndAverageAccuracyRate;

/**
 * SQLとJavaを繋げる重要な役目のクラス。
 */
public class Utils {

	/**新規作成モード*/
	public static final int NEW = 0;
	/**編集モード*/
	public static final int EDIT = 1;
	/**インポートモード*/
	public static final int IMPORT = 0;
	/**エクスポートモード*/
	public static final int EXPORT = 1;
	/**テーブルURLに常に存在するURL_ID。データ消去時に、このURL_IDを登録してから消すといい*/
	public static final int DEFAULT_URL_ID = 1;
	/**このアプリ用のディレクトリ（SDカード内）
	 * mnd/sdcard/Android/data/com.primer.world.android/　*/
	public static final String PARENT_PATH_OF_SD 
		= Environment.getExternalStorageDirectory().toString()
			+ "/Android/data/com.primer.world.android/";
	public static final String PATH_OF_WORD_BOOKS_IN_SD
		= PARENT_PATH_OF_SD + "WordBooks/";
	
/////////////////////////////////////////////////////////////////////
	
	/**contextとstringだけ渡せばトーストを表示するメソッド*/
	public static void showToast(Context context, String message){
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}
	
	/**contextとstringだけ渡せばトーストを表示するメソッド*/
	public static void showToast(Context context, int resId){
		Toast.makeText(context, context.getString(resId),
						Toast.LENGTH_SHORT).show();
	}
	
	
	/**正答率を計算して返すメソッド*/
    public static int returnAccuracyRate(int wrongCount, int testCount){
    	if(testCount == 0)	//NAN対策
    		return 0;
    	else{
    		float cor_rate = (1-((float)wrongCount/testCount))*100;
    		return (int)cor_rate;
    	}
    }
    
    /**Check weather we can use SD card. */
    public static boolean isSDcardMounted(){
    	String status = Environment.getExternalStorageState();
    	if(status.equals(Environment.MEDIA_MOUNTED))
    		return true;
    	else 
    		return false;
    }
	
//////////////　データのリスト表示用検索　////////////////////////////////
	
	/**全単語帳とそのデータをAraryListで返すメソッド*/
	public static ArrayList<WordBook> getWordBooks(Context context){
		/*初期化*/
		DataBaseHelper helper = null;
		SQLiteDatabase db = null;
		Cursor cur = null;
		ArrayList<WordBook> wordBookList = new ArrayList<WordBook>();
		
		try{
			helper = new DataBaseHelper(context);	// Helperクラスの生成
			db = helper.getWritableDatabase();//書込み用SQLiteDatabaseを生成
			
			//WORD_BOOK内の全データを取得
			cur = db.query(DataBaseHelper.TABLE_WORD_BOOK,
							null, null, null, null, null, null);
			
			//検索が終了したら、cursorに蓄えられたデータをいじる。
			//まず、カーソルの位置を最初に当てる
			if (cur != null && cur.moveToFirst()) {	//検索成功
				// カーソルを次に向かわす。終わったら、falseで抜け出す
				do{	
					WordBook wordBook = new WordBook();
					wordBook.setID(cur.getInt(cur.getColumnIndex(
							DataBaseHelper.COLUMN_WORD_BOOK_ID)));
					wordBook.setWordBookName(cur.getString(cur.getColumnIndex(
							DataBaseHelper.COLUMN_WORD_BOOK_NAME)));
					wordBook.setTestedDate(cur.getLong(cur.getColumnIndex(
							DataBaseHelper.COLUMN_TESTED_DATE)));
					wordBook.setTestMode(cur.getInt(cur.getColumnIndex(
							DataBaseHelper.COLUMN_TEST_MODE)));
					wordBook.setBookMark(cur.getInt(cur.getColumnIndex(
							DataBaseHelper.COLUMN_BOOK_MARK)));
					wordBookList.add(wordBook);
				}while(cur.moveToNext());
			}else{	//検索失敗（あるいは初期状態）
				wordBookList.add(new WordBook());
			}
			return wordBookList;
		}finally {
			if (cur != null) 	cur.close();
			if (db != null) 	db.close();
			if (helper != null) helper.close();
		}
	}

	/**単語帳名とそのIDのみをリストで返すメソッド（ウィジェットで用いる）<br>
	 * ウィジェットでは平均正答率などを表示させないため、取得データを最小限にとどめる*/
	public static ArrayList<WordBook> getWordBookNameAndID(Context context){
		/*初期化*/
		DataBaseHelper helper = null;
		SQLiteDatabase db = null;
		Cursor cur = null;
		ArrayList<WordBook> wordBookArray = new ArrayList<WordBook>();
		
		try{
			helper = new DataBaseHelper(context);	// Helperクラスの生成
			db = helper.getWritableDatabase();//書込み用SQLiteDatabaseを生成
			
			//WORD_BOOK内の単語帳名、IDを取得
			cur = db.query(DataBaseHelper.TABLE_WORD_BOOK,
					new String[]{DataBaseHelper.COLUMN_WORD_BOOK_ID,
									DataBaseHelper.COLUMN_WORD_BOOK_NAME },
					null,null,null,null,null);

			if (cur != null && cur.moveToFirst()) {	//検索成功
				do{	
					WordBook wordBook = new WordBook();
					wordBook.setID(cur.getInt(cur.getColumnIndex(
							DataBaseHelper.COLUMN_WORD_BOOK_ID)));
					wordBook.setWordBookName(cur.getString(cur.getColumnIndex(
							DataBaseHelper.COLUMN_WORD_BOOK_NAME)));
					wordBookArray.add(wordBook);
				}while(cur.moveToNext());
			}else{	//検索失敗（あるいは初期状態）
				wordBookArray.add(new WordBook());
			}
			return wordBookArray;
		}finally {
			if (cur != null) 	cur.close();
			if (db != null) 	db.close();
			if (helper != null) helper.close();
		}
	}
	
	/**単語帳IDから単語帳内の全ての単語を取得*/
	public static ArrayList<Word> getWords(Context context,
												int wordBookID) {
		/*初期化*/
		DataBaseHelper helper = null;
		SQLiteDatabase db = null;
		Cursor cur = null;
		ArrayList<Word> wordArray = new ArrayList<Word>();
		
		try {
			helper = new DataBaseHelper(context);	// Helperクラスの生成
			db = helper.getWritableDatabase();//書込み用SQLiteDatabaseを生成
			
			cur = db.query(DataBaseHelper.TABLE_WORD,
					null, DataBaseHelper.COLUMN_WORD_BOOK_ID + " = ?",
					new String[]{String.valueOf(wordBookID)},
					null,null,null);
			
			if (cur != null && cur.moveToFirst()){	//検索成功
				//単語関連の情報を全て取得する
				do{
					Word word = new Word();
					word.setWordBookID(wordBookID);//wordBookIDを挿入
					word.setID(cur.getInt(cur.getColumnIndex(
							DataBaseHelper.COLUMN_WORD_ID)));
					word.setQuestion(cur.getString(cur.getColumnIndex(
							DataBaseHelper.COLUMN_QUESTION)));
					word.setAnswer(cur.getString(cur.getColumnIndex(
							DataBaseHelper.COLUMN_ANSWER)));
					word.setExplanation(cur.getString(cur.getColumnIndex(
							DataBaseHelper.COLUMN_EXPLANATION)));
					word.setTestCount(cur.getInt(cur.getColumnIndex(
							DataBaseHelper.COLUMN_TEST_COUNT)));
					word.setWrongCount(cur.getInt(cur.getColumnIndex(
							DataBaseHelper.COLUMN_WRONG_COUNT)));
					word.setAccuracyRate(cur.getInt(cur.getColumnIndex(
							DataBaseHelper.COLUMN_ACCURACY_RATE)));
					wordArray.add(word);
				}while(cur.moveToNext());
			}else{	//検索失敗（あるいは初期状態）
				wordArray.add(new Word());
			}
			return wordArray;
		} finally {
			if (cur != null) 	cur.close();
			if (db != null) 	db.close();
			if (helper != null) helper.close();
		}
	}
	
	
	/**ある正答率（25,50,75,100）を指定して、それに該当する単語のみを取得してくる*/
	public static ArrayList<Word> getWordsByAccuracyRate(Context context,
														int wordBookID,
														int accuracyRate){
		/*初期化*/
		DataBaseHelper helper = null;
		SQLiteDatabase db = null;
		Cursor cur = null;
		ArrayList<Word> wordBookArray = new ArrayList<Word>();
		
		try {
			helper = new DataBaseHelper(context);	// Helperクラスの生成
			db = helper.getWritableDatabase();//書込み用SQLiteDatabaseを生成
			
			cur = db.query(DataBaseHelper.TABLE_WORD,
					null,
					DataBaseHelper.COLUMN_WORD_BOOK_ID + " = ? AND "
					+ DataBaseHelper.COLUMN_ACCURACY_RATE + " = ?",
					new String[]{String.valueOf(wordBookID),
									String.valueOf(accuracyRate)},
					null,null,null);
			
			if (cur != null && cur.moveToFirst()){	//検索成功
				do{
					Word word = new Word();
					word.setID(cur.getInt(
						cur.getColumnIndex(DataBaseHelper.COLUMN_WORD_ID)));
					word.setQuestion(cur.getString(cur.getColumnIndex(
							DataBaseHelper.COLUMN_QUESTION)));
					word.setAnswer(cur.getString(cur.getColumnIndex(
							DataBaseHelper.COLUMN_ANSWER)));
					word.setExplanation(cur.getString(cur.getColumnIndex(
							DataBaseHelper.COLUMN_EXPLANATION)));
					word.setTestCount(cur.getInt(cur.getColumnIndex(
							DataBaseHelper.COLUMN_TEST_COUNT)));
					word.setWrongCount(cur.getInt(cur.getColumnIndex(
							DataBaseHelper.COLUMN_WRONG_COUNT)));
					word.setAccuracyRate(accuracyRate);//正答率を挿入
					word.setWordBookID(wordBookID);//単語帳IDを挿入
					wordBookArray.add(word);
				}while(cur.moveToNext());
			}else{	//検索失敗（あるいは初期状態）
				wordBookArray.add(new Word());
			}
			return wordBookArray;
		} finally {
			if (cur != null) 	cur.close();
			if (db != null) 	db.close();
			if (helper != null) helper.close();
		}
	}
	
	/**ランダムに単語を取得(ウィジェットでテストする際に用いる)*/
	public static Word getRandomWord(Context context, int wordBookID){
		/*初期化*/
		DataBaseHelper helper = null;
		SQLiteDatabase db = null;
		Cursor cur = null;
		
		try {
			helper = new DataBaseHelper(context);	// Helperクラスの生成
			db = helper.getWritableDatabase();//書込み用SQLiteDatabaseを生成
			
			cur = db.query(DataBaseHelper.TABLE_WORD,
					new String[]{DataBaseHelper.COLUMN_WORD_ID,
								DataBaseHelper.COLUMN_QUESTION },
					DataBaseHelper.COLUMN_WORD_BOOK_ID + " = ?",
					new String[]{String.valueOf(wordBookID)},
					null,null,null);
			
			int rowCount = cur.getCount();//データ数を取得
			int random = (int)(Math.random()*rowCount);
			if (cur != null && cur.moveToFirst()){	//検索成功
				for(int i=0; i<random; i++)
					cur.moveToNext();
				Word word = new Word();
				word.setID(cur.getInt(
						cur.getColumnIndex(DataBaseHelper.COLUMN_WORD_ID)));
				word.setQuestion(cur.getString(
						cur.getColumnIndex(DataBaseHelper.COLUMN_QUESTION)));
				return word;
			}else{	//検索失敗（あるいはなかった）
				return new Word();
			}
		} finally {
			if (cur != null) 	cur.close();
			if (db != null) 	db.close();
			if (helper != null) helper.close();
		}
	}
	
	/**単語IDを用いてその単語の答えを取得（ウィジェットで使用）*/
	public static Word getAnswer(Context context, int wordID){
		/*初期化*/
		DataBaseHelper helper = null;
		SQLiteDatabase db = null;
		Cursor cur = null;
		
		try {
			helper = new DataBaseHelper(context);	// Helperクラスの生成
			db = helper.getWritableDatabase();//書込み用SQLiteDatabaseを生成
			
			cur = db.query(DataBaseHelper.TABLE_WORD,
					new String[]{ DataBaseHelper.COLUMN_ANSWER},
					DataBaseHelper.COLUMN_WORD_ID + " = ?",
					new String[]{String.valueOf(wordID)},
					null,null,null,"1");
			
			if (cur != null && cur.moveToFirst()){	//検索成功
				Word word = new Word();
				word.setAnswer(cur.getString(
						cur.getColumnIndex(DataBaseHelper.COLUMN_ANSWER)));
				return word;
			}else{	//検索失敗（あるいはなかった）
				return new Word();
			}
		} finally {
			if (cur != null) 	cur.close();
			if (db != null) 	db.close();
			if (helper != null) helper.close();
		}
	}
	
	/**RECORDからテストした日付と平均正答率を取得するメソッド<br>
	 * RecordActivityのリストビューで用いる*/
	public static ArrayList<DateAndAverageAccuracyRate> 
			getDateAndAverageAccuracyRates(Context context, int wordBookID){
		/*初期化*/
		DataBaseHelper helper = null;
		SQLiteDatabase db = null;
		Cursor cur = null;
		ArrayList<DateAndAverageAccuracyRate> dataArray 
					= new ArrayList<DateAndAverageAccuracyRate>();
		
		try {
			helper = new DataBaseHelper(context);// Helperクラスの生成
			db = helper.getWritableDatabase();//書込み用SQLiteDatabaseを生成
			
			//日付、平均正答率の取得
			cur = db.query(DataBaseHelper.TABLE_RECORD,
					new String[]{DataBaseHelper.COLUMN_TESTED_DATE,
								DataBaseHelper.COLUMN_AVERAGE_ACCURACY_RATE},
					DataBaseHelper.COLUMN_WORD_BOOK_ID + " = ?",
					new String[]{String.valueOf(wordBookID)},
					null,null,null);
			
			if (cur != null && cur.moveToFirst()){
				do{
					DateAndAverageAccuracyRate data 
								= new DateAndAverageAccuracyRate();
					//日付と平均正答率を挿入していく
					data.setDate(cur.getLong(cur.getColumnIndex(
						DataBaseHelper.COLUMN_TESTED_DATE)));
					data.setAverageAccuracyRate(
						String.valueOf(cur.getInt(cur.getColumnIndex(
							DataBaseHelper.COLUMN_AVERAGE_ACCURACY_RATE))));
					dataArray.add(data);
				}while(cur.moveToNext());
			}else{	// テストをしておらず、レコードが存在しない場合
				dataArray.add(new DateAndAverageAccuracyRate());
			}
			return dataArray;
		}finally{
			if (cur != null) 	cur.close();
			if (db != null) 	db.close();
			if (helper != null) helper.close();
		}
	}
	
	/**テーブルURL内に登録したURLとそのサイト名、IDを全取得するメソッド*/
	public static ArrayList<SiteNameAndUrl> 
								getSiteNameAndUrls(Context context){
		/*初期化*/
		DataBaseHelper helper = null;
		SQLiteDatabase db = null;
		Cursor cur = null;
		ArrayList<SiteNameAndUrl> siteNameAndUrlArray 
									= new ArrayList<SiteNameAndUrl>();
		
		try {
			helper = new DataBaseHelper(context);// Helperクラスの生成
			db = helper.getWritableDatabase();//書込み用SQLiteDatabaseを生成
			
			//テーブルURL内の全データを探索
			cur = db.query(DataBaseHelper.TABLE_URL,
							null,null,null,null,null,null);
			
			if (cur != null && cur.moveToFirst()){
				do{
					SiteNameAndUrl data = new SiteNameAndUrl();
					//ID、日付と平均正答率を挿入していく
					data.setUrlID(cur.getInt(
						cur.getColumnIndex(DataBaseHelper.COLUMN_URL_ID)));
					data.setSiteName(cur.getString(
						cur.getColumnIndex(DataBaseHelper.COLUMN_SITE_NAME)));
					data.setUrl(cur.getString(
						cur.getColumnIndex(DataBaseHelper.COLUMN_URL)));
					siteNameAndUrlArray.add(data);
				}while(cur.moveToNext());
			}else{	//URLなどが登録されていなかった場合
				siteNameAndUrlArray.add(new SiteNameAndUrl());
			}
			return siteNameAndUrlArray;
		}finally{
			if (cur != null) cur.close();
			if (db != null) db.close();
			if (helper != null) helper.close();
		}
	}
	
	/**テーブルWORD_BOOK内に登録したURLのサイト名、URLを返すメソッド。*/
	public static ArrayList<SiteNameAndUrl> 
			getRegisteredSiteNameAndUrls(Context context,int wordBookID){
		/*初期化*/
		DataBaseHelper helper = null;
		SQLiteDatabase db = null;
		Cursor cur = null;
		ArrayList<SiteNameAndUrl> siteNameAndUrlArray 
								= new ArrayList<SiteNameAndUrl>();
		int[] urlIDs = null;
		String[] columnNames = {DataBaseHelper.COLUMN_URL_ID_1,
								DataBaseHelper.COLUMN_URL_ID_2,
								DataBaseHelper.COLUMN_URL_ID_3};
		
		try {
			helper = new DataBaseHelper(context);// Helperクラスの生成
			db = helper.getWritableDatabase();//書込み用SQLiteDatabaseを生成
			
			//WORD_BOOK内に登録されたURLのIDを探索
			cur = db.query(DataBaseHelper.TABLE_WORD_BOOK,
					new String[]{DataBaseHelper.COLUMN_URL_ID_1,
								DataBaseHelper.COLUMN_URL_ID_2,
								DataBaseHelper.COLUMN_URL_ID_3},
					DataBaseHelper.COLUMN_WORD_BOOK_ID + " = ?",
					new String[]{String.valueOf(wordBookID)},
					null,null,null,"1");
			//登録されていたURLのIDを代入
			if (cur != null && cur.moveToFirst()){
				urlIDs = new int[3];
				for(int i=0; i<urlIDs.length; i++){
					urlIDs[i] = cur.getInt(cur.getColumnIndex(
												columnNames[i]));
				}
			}
			//使い終わったカーソルはクローズしないとエラーが出る
			if(cur != null) cur.close();
			
			for(int i=0; i<urlIDs.length; i++){
				//URLのIDからそのサイト名やURLを検索
				cur = db.query(DataBaseHelper.TABLE_URL,
						new String[]{DataBaseHelper.COLUMN_SITE_NAME,
									DataBaseHelper.COLUMN_URL},
						DataBaseHelper.COLUMN_URL_ID + " = ?",
						new String[]{String.valueOf(urlIDs[i])},
						null,null,null,"1");
				//URLID、サイト名とURL,カラム名を代入していく
				if(cur != null && cur.moveToFirst()){
					SiteNameAndUrl data = new SiteNameAndUrl();
					data.setUrlID(urlIDs[i]);
					data.setSiteName(cur.getString(cur.getColumnIndex(
							DataBaseHelper.COLUMN_SITE_NAME)));
					data.setUrl(cur.getString(cur.getColumnIndex(
							DataBaseHelper.COLUMN_URL)));
					data.setColumnName(columnNames[i]);
					siteNameAndUrlArray.add(data);
				}else{	//データが上手く検索出来なかった場合
					siteNameAndUrlArray.add(new SiteNameAndUrl());
				}
				if(cur != null) cur.close();
			}
			return siteNameAndUrlArray;
		}finally{
			if (cur != null) cur.close();
			if (db != null) db.close();
			if (helper != null) helper.close();
		}
	}
	
	/**WORD_BOOKに登録した三つのURLIDを配列で返すメソッド。*/
	public static int[] getRegisteredUrlIDs(Context context, int wordBookID){
		/*初期化*/
		DataBaseHelper helper = null;
		SQLiteDatabase db = null;
		Cursor cur = null;
		int[] urlIDs = null;
		
		try {
			helper = new DataBaseHelper(context);// Helperクラスの生成
			db = helper.getWritableDatabase();//書込み用SQLiteDatabaseを生成
			
			//WORD_BOOK内に登録されたURLのIDを探索
			cur = db.query(DataBaseHelper.TABLE_WORD_BOOK,
					new String[]{DataBaseHelper.COLUMN_URL_ID_1,
								DataBaseHelper.COLUMN_URL_ID_2,
								DataBaseHelper.COLUMN_URL_ID_3},
					DataBaseHelper.COLUMN_WORD_BOOK_ID + " = ?",
					new String[]{String.valueOf(wordBookID)},
					null,null,null,"1");
			//登録されていたURLのIDを代入
			if (cur != null && cur.moveToFirst()){	
				urlIDs = new int[3];
				urlIDs[0] = cur.getInt(cur.getColumnIndex(
								DataBaseHelper.COLUMN_URL_ID_1));
				urlIDs[1] = cur.getInt(cur.getColumnIndex(
								DataBaseHelper.COLUMN_URL_ID_2));
				urlIDs[2] = cur.getInt(cur.getColumnIndex(
								DataBaseHelper.COLUMN_URL_ID_3));
			}
			return urlIDs;
		}finally{
			if (cur != null) 	cur.close();
			if (db != null) 	db.close();
			if (helper != null) helper.close();
		}
	}
	
///////////その他の検索////////////////////////////////////////////////////
	
	/**テストのモードを単語IDから検索するメソッド<br>エラーなら、NORMALを返却する*/
	public static int getTestMode(Context context, int wordBookID){
		/*初期化*/
		DataBaseHelper helper = null;
		SQLiteDatabase db = null;
		Cursor cur = null;
		int testMode = TestActivity.NORMAL;
		
		try {
			helper = new DataBaseHelper(context);// Helperクラスの生成
			db = helper.getWritableDatabase();//書込み用SQLiteDatabaseを生成
			
			cur = db.query(DataBaseHelper.TABLE_WORD_BOOK,
							new String[]{DataBaseHelper.COLUMN_TEST_MODE},
							DataBaseHelper.COLUMN_WORD_BOOK_ID + " = ?",
							new String[]{String.valueOf(wordBookID)},
							null,null,null,"1");
			
			if (cur != null && cur.moveToFirst()){
				testMode = cur.getInt(0);	//TEST_MODEが得られる
			}
			return testMode;
		}finally{
			if (cur != null) 	cur.close();
			if (db != null) 	db.close();
			if (helper != null) helper.close();
		}
	}
	
	/**間違えた回数を単語IDから検索するメソッド<br>エラーなら、0(間違えなし)を返却する*/
	public static int getWrongCount(Context context, int wordID){
		/*初期化*/
		DataBaseHelper helper = null;
		SQLiteDatabase db = null;
		Cursor cur = null;
		int wrongCount = 0;
		
		try {
			helper = new DataBaseHelper(context);// Helperクラスの生成
			db = helper.getWritableDatabase();//書込み用SQLiteDatabaseを生成
			
			cur = db.query(DataBaseHelper.TABLE_WORD,
					new String[]{DataBaseHelper.COLUMN_WRONG_COUNT},
					DataBaseHelper.COLUMN_WORD_ID + " = ?",
					new String[]{String.valueOf(wordID)},
					null,null,null,"1");
						
			if (cur != null && cur.moveToFirst()){
				wrongCount = cur.getInt(0);	//WRONG_COUNTを得られる
			}
			return wrongCount;
		}finally{
			if (cur != null) 	cur.close();
			if (db != null) 	db.close();
			if (helper != null) helper.close();
		}
	}
	
	/**テストを始める場所（ブックマーク）を単語IDから検索するメソッド<br>
	 * エラーなら、0（つまり一番最初から）を返却する*/
	public static int getBookMark(Context context, int wordNoteID){
		/*初期化*/
		DataBaseHelper helper = null;
		SQLiteDatabase db = null;
		Cursor cur = null;
		int bookMark = 0;
		
		try {
			helper = new DataBaseHelper(context);// Helperクラスの生成
			db = helper.getWritableDatabase();//書込み用SQLiteDatabaseを生成
			
			cur = db.query(DataBaseHelper.TABLE_WORD_BOOK,
					new String[]{DataBaseHelper.COLUMN_BOOK_MARK},
					DataBaseHelper.COLUMN_WORD_BOOK_ID + " = ?",
					new String[]{String.valueOf(wordNoteID)},
					null,null,null,"1");
			
			if (cur != null && cur.moveToFirst()){
				bookMark = cur.getInt(0);	//book_markを得られる
			}
			return bookMark;
		}finally{
			if (cur != null) 	cur.close();
			if (db != null) 	db.close();
			if (helper != null) helper.close();
		}
	}
	
//////////////　データの新規作成　///////////////////////////////////////////

	/**単語帳を新しく作成<br>
	 * 自動でIDは登録してくれる（SQLの設定）から、単語帳名、日付、テストモード、
	 * ブックマークを格納。*/
	public static void createWordBook(Context context, String wordBookName){
		/*初期化*/
		DataBaseHelper helper = null;
		SQLiteDatabase db = null;
		WordBook wordBook = new WordBook();
		
		try {
			helper = new DataBaseHelper(context);// Helperクラスの生成
			db = helper.getWritableDatabase();// 書込み用SQLiteDatabaseを生成
			
			ContentValues values = new ContentValues();//書き込み用のデータを作成
			// 単語帳名をコンテントバリューに追加
			values.put(DataBaseHelper.COLUMN_WORD_BOOK_NAME,
					wordBookName);
			values.put(DataBaseHelper.COLUMN_TESTED_DATE,
					System.currentTimeMillis());
			values.put(DataBaseHelper.COLUMN_TEST_MODE,
					wordBook.getTestMode());
			values.put(DataBaseHelper.COLUMN_BOOK_MARK,
					wordBook.getBookMark());
			//挿入(IDは自動で設定させられる)
			db.insert(DataBaseHelper.TABLE_WORD_BOOK, null, values);
		} finally {
			if (db != null)	db.close();
			if (helper != null)	helper.close();
		}
	}
	
	/**単語帳を新しく登録して、そのidを返す（エラー時は単語帳IDを-1で返す） */
	public static int createWordBookReturnID(Context context,
											String wordBookName){
		/*初期化*/
		DataBaseHelper helper = null;
		SQLiteDatabase db = null;
		Cursor cur = null;
		long date = System.currentTimeMillis();
		WordBook wordBook = new WordBook();
		int wordBookID = -1;
		
		try {
			helper = new DataBaseHelper(context);// Helperクラスの生成
			db = helper.getWritableDatabase();// 書込み用SQLiteDatabaseを生成
			
			ContentValues values = new ContentValues();//書き込み用のデータを作成
			// 挿入するデータをコンテントバリューに追加
			values.put(DataBaseHelper.COLUMN_WORD_BOOK_NAME, wordBookName);
			values.put(DataBaseHelper.COLUMN_TESTED_DATE, date);
			values.put(DataBaseHelper.COLUMN_TEST_MODE,
					wordBook.getTestMode());
			values.put(DataBaseHelper.COLUMN_BOOK_MARK,
					wordBook.getBookMark());
			//挿入(IDは自動で設定させられる)
			db.insert(DataBaseHelper.TABLE_WORD_BOOK, null, values);
			
			// 単語帳IDを検索（AUTO INCREMENTのため、最大値は今作成した単語帳である）
			cur = db.query(DataBaseHelper.TABLE_WORD_BOOK,
					new String[]{
						"MAX(" + DataBaseHelper.COLUMN_WORD_BOOK_ID + ")"},
					null, null, null, null, null,"1");
			if (cur != null && cur.moveToFirst()){
				wordBookID = cur.getInt(0);	//wordBookIDを取得
			}
			return wordBookID;
		} finally {
			if(cur != null) cur.close();
			if (db != null)	db.close();
			if (helper != null)	helper.close();
		}
	}
	
	
	/**新しい単語を作成する*/
	public static void createWord(Context context, Word word){
		/*初期化*/
		DataBaseHelper helper = null;
		SQLiteDatabase db = null;
		Cursor cur = null;
		
		try {
			helper = new DataBaseHelper(context);// Helperクラスの生成
			db = helper.getWritableDatabase();// 書込み用SQLiteDatabaseを生成
			
			// 単語IDは自動生成だから、ここで単語IDを格納する必要はない
			ContentValues values = new ContentValues();
			values.put(DataBaseHelper.COLUMN_WORD_BOOK_ID,
					word.getWordBookID());
			values.put(DataBaseHelper.COLUMN_QUESTION,
					word.getQuestion());
			values.put(DataBaseHelper.COLUMN_ANSWER,
					word.getAnswer());
			values.put(DataBaseHelper.COLUMN_EXPLANATION,
					word.getExplanation());
			values.put(DataBaseHelper.COLUMN_TEST_COUNT,
					word.getTestCount());
			values.put(DataBaseHelper.COLUMN_WRONG_COUNT,
					word.getWrongCount());
			values.put(DataBaseHelper.COLUMN_ACCURACY_RATE,
					word.getAccuracyRate());
			db.insert(DataBaseHelper.TABLE_WORD, null, values);	//データの挿入
			
			//新しく作成した単語のIDを取得するため、現存するIDの最大値を取得する
			/*
			cur = db.query(DataBaseHelper.TABLE_WORD,
							new String[]{
								"MAX(" + DataBaseHelper.COLUMN_WORD_ID + ")"},
							null, null, null, null, null,"1");
			
			if (cur != null && cur.moveToFirst()) {
				//単語IDを取得して、格納する。カラムはWORD_ID
				word.setWordID(cur.getInt(0));
			}
			*/
		} finally {
			if(cur != null) cur.close();
			if (db != null)	db.close();
			if (helper != null)	helper.close();
		}
	}
	
	/**ArrayList<Word>から新しく単語を作成する */
	public static void createWordsFromArray(Context context,
											ArrayList<Word> wordArray,
											int wordBookID){
		/*初期化*/
		DataBaseHelper helper = null;
		SQLiteDatabase db = null;
		Cursor cur = null;
		
		try {
			helper = new DataBaseHelper(context);// Helperクラスの生成
			db = helper.getWritableDatabase();// 書込み用SQLiteDatabaseを生成
			
			for(int i=0; i < wordArray.size(); i++){
				ContentValues values = new ContentValues();
				Word word = wordArray.get(i);	//格納する単語を取得
				values.put(DataBaseHelper.COLUMN_WORD_BOOK_ID,
						wordBookID);
				values.put(DataBaseHelper.COLUMN_QUESTION,
						word.getQuestion());
				values.put(DataBaseHelper.COLUMN_ANSWER,
						word.getAnswer());
				values.put(DataBaseHelper.COLUMN_EXPLANATION,
						word.getExplanation());
				values.put(DataBaseHelper.COLUMN_TEST_COUNT,
						word.getTestCount());
				values.put(DataBaseHelper.COLUMN_WRONG_COUNT,
						word.getWrongCount());
				values.put(DataBaseHelper.COLUMN_ACCURACY_RATE,
						word.getAccuracyRate());
				db.insert(DataBaseHelper.TABLE_WORD, null, values);
				/*
				//新しく作成した単語のIDを取得するため、現存するIDの最大値を取得する
				cur = db.query(DataBaseHelper.TABLE_WORD,
								new String[]{
									"MAX("+DataBaseHelper.COLUMN_WORD_ID+")"},
								null, null, null, null, null,"1");
				if (cur != null && cur.moveToFirst()) {
					word.setWordID(cur.getInt(0));
				}
				*/
			}
		} finally {
			if(cur != null) cur.close();
			if (db != null)	db.close();
			if (helper != null)	helper.close();
		}
	}
	
	/**レコードを更新する<br>具体的には、日付と平均正答率*/
	public static void createRecord(Context context,
									int wordBookID,
									long testedDate){
		DataBaseHelper helper = null;
		SQLiteDatabase db = null;
		Cursor cur = null;
		float averageAccuracyRate = 0;//単語帳全体の正答率の平均
		int rowCount = 0;	//列数を数える（つまり単語の数）
		int wrongCount = 0;	//間違えた回数を数える
		int testCount = 0;	//テスト回数を数える
		
		try {
			helper = new DataBaseHelper(context);
			db = helper.getWritableDatabase();		
			
			cur = db.query(DataBaseHelper.TABLE_WORD,
					new String[]{DataBaseHelper.COLUMN_WRONG_COUNT,
								DataBaseHelper.COLUMN_TEST_COUNT},
					DataBaseHelper.COLUMN_WORD_BOOK_ID + " = ?",
					new String[]{String.valueOf(wordBookID)},
					null, null, null);
						
			if (cur != null && cur.moveToFirst()) {	//検索成功
				// カーソルを次に向かわす。終わったら、falseで抜け出す
				do{	
					rowCount++;
					wrongCount = cur.getInt(cur.getColumnIndex(
										DataBaseHelper.COLUMN_WRONG_COUNT));
					testCount = cur.getInt(cur.getColumnIndex(
										DataBaseHelper.COLUMN_TEST_COUNT));
					averageAccuracyRate += returnAccuracyRate(wrongCount,
															testCount);
				}while(cur.moveToNext());
			}
			averageAccuracyRate /= rowCount;	//単語数で割る
			
			ContentValues values = new ContentValues();
			values.put(DataBaseHelper.COLUMN_WORD_BOOK_ID, wordBookID);
			values.put(DataBaseHelper.COLUMN_TESTED_DATE, testedDate);
			values.put(DataBaseHelper.COLUMN_AVERAGE_ACCURACY_RATE,
						(int)averageAccuracyRate);
			db.insert(DataBaseHelper.TABLE_RECORD, null, values);
			
		} finally {
			if (cur != null) cur.close();
			if (db != null) db.close();
			if (helper != null)	helper.close();
		}
	}
	
	/**URLとサイト名を新規登録する*/
	public static void createSiteNameAndUrl(Context context,
											SiteNameAndUrl newSiteNameAndUrl){
		/*初期化*/
		DataBaseHelper helper = null;
		SQLiteDatabase db = null;
		Cursor cur = null;
		
		try {
			helper = new DataBaseHelper(context);// Helperクラスの生成
			db = helper.getWritableDatabase();// 書込み用SQLiteDatabaseを生成
			
			// URL_IDは自動生成だから、ここでIDを格納する必要はない
			ContentValues values = new ContentValues();
			values.put(DataBaseHelper.COLUMN_SITE_NAME,
						newSiteNameAndUrl.getSiteName());
			values.put(DataBaseHelper.COLUMN_URL,
						newSiteNameAndUrl.getUrl());
			db.insert(DataBaseHelper.TABLE_URL, null, values);	//データの挿入
			
		} finally {
			if(cur != null) cur.close();
			if (db != null)	db.close();
			if (helper != null)	helper.close();
		}
	}
	
//////////////　データの更新　/////////////////////////////////////////////////

	
	/**単語帳IDを使って、単語帳名を更新*/
	public static void updateWordBookName(Context context,
										String newWordBookName,
										int wordBookID){
		/*初期化*/
		DataBaseHelper helper = null;
		SQLiteDatabase db = null;
		
		try {
			helper = new DataBaseHelper(context);
			db = helper.getWritableDatabase();
			
			ContentValues values = new ContentValues();// 書き込み用のデータを作成
			//更新したい内容を挿入
			values.put(DataBaseHelper.COLUMN_WORD_BOOK_NAME, newWordBookName);
			// アップデート処理の実行
			db.update(DataBaseHelper.TABLE_WORD_BOOK,
						values,
						DataBaseHelper.COLUMN_WORD_BOOK_ID + " = ?",
						new String[] { String.valueOf(wordBookID)});
			return;
		} finally {
			if (db != null) db.close();
			if (helper != null) helper.close();
		}
	}	
	
	/**wordに含まれるwordIDを使って、単語の中身を更新*/
	public static void updateWord(Context context, Word word) {
		/*初期化*/
		DataBaseHelper helper = null;
		SQLiteDatabase db = null;
		
		try {
			helper = new DataBaseHelper(context);
			db = helper.getWritableDatabase();
			
			// 書き込み用のデータを作成（単語名、答え、説明以外は変える必要がない）
			ContentValues values = new ContentValues();
			values.put(DataBaseHelper.COLUMN_QUESTION,
						word.getQuestion());
			values.put(DataBaseHelper.COLUMN_ANSWER,
						word.getAnswer());
			values.put(DataBaseHelper.COLUMN_EXPLANATION,
						word.getExplanation());
			
			// アップデート処理の実行//？に 単語IDが入る
			db.update(DataBaseHelper.TABLE_WORD, values,
						DataBaseHelper.COLUMN_WORD_ID + " = ?",
				        new String[] { String.valueOf(word.getID())});
			return;
		} finally {
			if (db != null) db.close();
			if (helper != null)	helper.close();
		}
	}
	

	/**問題のみを更新するメソッド<br>wordID を使う。*/
	public static void updateQuestion(Context context,
									String question,
									int wordID) {
		DataBaseHelper helper = null;
		SQLiteDatabase db = null;
		
		try {
			helper = new DataBaseHelper(context);
			db = helper.getWritableDatabase();
			
			ContentValues values = new ContentValues();
			values.put(DataBaseHelper.COLUMN_QUESTION, question);
			
			db.update(DataBaseHelper.TABLE_WORD,
						values,
						DataBaseHelper.COLUMN_WORD_ID + " = ?",
						new String[] { String.valueOf(wordID)});
			return;
		} finally {
			if (db != null) db.close();
			if (helper != null)	helper.close();
		}
	}
	
	
	/**間違えた回数を更新するメソッド<br>wordID を使う。*/
	public static void updateWrongCount(Context context,
										int wordID,
										int wrongCount){
		DataBaseHelper helper = null;
		SQLiteDatabase db = null;
		
		try {
			helper = new DataBaseHelper(context);
			db = helper.getWritableDatabase();
			
			ContentValues values = new ContentValues();
			values.put(DataBaseHelper.COLUMN_WRONG_COUNT, wrongCount);
			db.update(DataBaseHelper.TABLE_WORD, values,
						DataBaseHelper.COLUMN_WORD_ID + " = ?",
						new String[] { String.valueOf(wordID)});
			return;
		} finally {
			if (db != null) db.close();
			if (helper != null)	helper.close();
		}
	}
	
	/**テスト結果を保存する*/
	public static void updateTestedWordBookRecord(Context context,
													int wordBookID,
													int bookMark,
													long testedDate,
													int testMode) {
		DataBaseHelper helper = null;
		SQLiteDatabase db = null;
		
		try {
			helper = new DataBaseHelper(context);
			db = helper.getWritableDatabase();
			
			ContentValues values = new ContentValues();
			values.put(DataBaseHelper.COLUMN_BOOK_MARK, bookMark);
			values.put(DataBaseHelper.COLUMN_TESTED_DATE, testedDate);
			values.put(DataBaseHelper.COLUMN_TEST_MODE, testMode);
			db.update(DataBaseHelper.TABLE_WORD_BOOK,
						values,
						DataBaseHelper.COLUMN_WORD_BOOK_ID + " = ?",
						new String[] { String.valueOf(wordBookID)});
			return;
		} finally {
			if (db != null) db.close();
			if (helper != null)	helper.close();
		}
	}
	
	/**単語のテスト結果を更新する<br>具体的には、テスト回数の更新、正答率の更新*/
	public static void updateTestedWordRecord(Context context, Word word){
		DataBaseHelper helper = null;
		SQLiteDatabase db = null;
		Cursor cur = null;
		int testCount = word.getTestCount() + 1;	//テスト回数を１増やす
		float accuracyRate 
				= (1-((float)word.getWrongCount()/ testCount))*100;
		
		try {
			helper = new DataBaseHelper(context);
			db = helper.getWritableDatabase();
			
			//大雑把な正答率を求めて、区別しやすくする
			if(accuracyRate <= 25){
				accuracyRate = 25;
			}else if(accuracyRate > 25 && accuracyRate <= 50){
				accuracyRate = 50;
			}else if(accuracyRate > 50 && accuracyRate <= 75){
				accuracyRate = 75;
			}else if(accuracyRate > 75 && accuracyRate <= 100){
				accuracyRate = 100;
			}
			
			ContentValues values = new ContentValues();
			values.put(DataBaseHelper.COLUMN_TEST_COUNT, testCount);
			values.put(DataBaseHelper.COLUMN_ACCURACY_RATE,
						(int)accuracyRate);
			db.update(DataBaseHelper.TABLE_WORD,
						values,
						DataBaseHelper.COLUMN_WORD_ID + " = ?",
						new String[] { String.valueOf(word.getID())});
			return;
		} finally {
			if (cur != null) cur.close();
			if (db != null) db.close();
			if (helper != null)	helper.close();
		}
	}
	
	/** 選択したURLのIDをテーブルWORD_BOOKのURL_IDカラムに保存する
	 * @param context applicationContext を送ることを推奨
	 * @param wordBookID 登録するURLを変更しようとしている単語帳のID
	 * @param urlID 新しく登録するURLのID
	 * @param columnName URLを登録するカラム名*/
	public static void updateUrlID(Context context, int wordBookID,
									int urlID, String columnName){
		DataBaseHelper helper = null;
		SQLiteDatabase db = null;
		
		try {
			helper = new DataBaseHelper(context);
			db = helper.getWritableDatabase();
			
			ContentValues values = new ContentValues();
			values.put(columnName, urlID);
			db.update(DataBaseHelper.TABLE_WORD_BOOK, values,
						DataBaseHelper.COLUMN_WORD_BOOK_ID + " = ?",
						new String[] { String.valueOf(wordBookID)});
			return;
		} finally {
			if (db != null) db.close();
			if (helper != null)	helper.close();
		}
	}
	
	public static void updateSiteNameAndUrl(Context context,
										SiteNameAndUrl newSiteNameAndUrl){
		DataBaseHelper helper = null;
		SQLiteDatabase db = null;
		
		try {
			helper = new DataBaseHelper(context);
			db = helper.getWritableDatabase();
			
			ContentValues values = new ContentValues();
			values.put(DataBaseHelper.COLUMN_SITE_NAME,
						newSiteNameAndUrl.getSiteName());
			values.put(DataBaseHelper.COLUMN_URL,
						newSiteNameAndUrl.getUrl());
			db.update(DataBaseHelper.TABLE_URL, values,
						DataBaseHelper.COLUMN_URL_ID + " = ?",
						new String[]{String.valueOf(
										newSiteNameAndUrl.getUrlID())});
		} finally {
			if (db != null) db.close();
			if (helper != null)	helper.close();
		}
	}

//////////////　データの削除　//////////////////////////////////////////////////
	
	/**wordBookIDで単語帳を削除<br>それに含まれている単語も全部削除*/
	public static void deleteWordBook(Context context, int wordBookID){
		/*初期化*/
		DataBaseHelper helper = null;
		SQLiteDatabase db = null;
		
		try {
			helper = new DataBaseHelper(context);
			db = helper.getWritableDatabase();
			
			// 単語帳の削除
			db.delete(DataBaseHelper.TABLE_WORD_BOOK,
					  DataBaseHelper.COLUMN_WORD_BOOK_ID + " = ?",
					  new String[] {String.valueOf(wordBookID)});
			//　登録されている単語の削除
			db.delete(DataBaseHelper.TABLE_WORD,
					  DataBaseHelper.COLUMN_WORD_BOOK_ID + " = ?",
					  new String[] {String.valueOf(wordBookID)});
			//　登録されているレコードの削除
			db.delete(DataBaseHelper.TABLE_RECORD,
					  DataBaseHelper.COLUMN_WORD_BOOK_ID + " = ?",
					  new String[] {String.valueOf(wordBookID)});
		} finally {
			if (db != null)	db.close();
			if (helper != null) helper.close();
		}
	}
	
	
	/**wordIDで単語を削除*/
	public static void deleteWord(Context context, int wordID) {
		/*初期化*/
		DataBaseHelper helper = null;
		SQLiteDatabase db = null;
		
		try {
			helper = new DataBaseHelper(context);
			db = helper.getWritableDatabase();
			
			//単語の削除
			db.delete(DataBaseHelper.TABLE_WORD,
						DataBaseHelper.COLUMN_WORD_ID + " = ?",
					  new String[] {String.valueOf(wordID)});
		} finally {
			if (db != null)	db.close();
			if (helper != null) helper.close();
		}
	}
	
	/**wordBookIDでレコードを削除*/
	public static void deleteRecord(Context context, int wordBookID) {
		/*初期化*/
		DataBaseHelper helper = null;
		SQLiteDatabase db = null;
		
		try {
			helper = new DataBaseHelper(context);
			db = helper.getWritableDatabase();
			
			//レコードの削除
			db.delete(DataBaseHelper.TABLE_RECORD,
					  DataBaseHelper.COLUMN_WORD_BOOK_ID + " = ?",
					  new String[] {String.valueOf(wordBookID)});
			
			//単語テーブルの中で、レコードを消す単語帳に含まれるものをリセット
			ContentValues values = new ContentValues();
			values.put(DataBaseHelper.COLUMN_TEST_COUNT, 0);
			values.put(DataBaseHelper.COLUMN_WRONG_COUNT, 0);
			values.put(DataBaseHelper.COLUMN_ACCURACY_RATE, 25);
			db.update(DataBaseHelper.TABLE_WORD,
					values,
					DataBaseHelper.COLUMN_WORD_BOOK_ID + " = ?",
					new String[] { String.valueOf(wordBookID)});			
		} finally {
			if (db != null)	db.close();
			if (helper != null) helper.close();
		}
	}
	
	/**urlIDで登録されているサイト名とURLを削除*/
	public static void deleteSiteNameAndUrl(Context context, int urlID) {
		/*初期化*/
		DataBaseHelper helper = null;
		SQLiteDatabase db = null;
		
		try {
			helper = new DataBaseHelper(context);
			db = helper.getWritableDatabase();
			
			db.delete(DataBaseHelper.TABLE_URL,
					  DataBaseHelper.COLUMN_URL_ID + " = ?",
					  new String[] {String.valueOf(urlID)});
						
		} finally {
			if (db != null)	db.close();
			if (helper != null) helper.close();
		}
	}

//////////////ファイルの操作//////////////////////////////////////////////

	
	/**単語帳をcsvファイルとして、sdカードに保存するメソッド
	 * @return File
	 * @throws IOException */
	public static File exportWordBookToSD(Context context,WordBook wordBook){
    	File parentDir = new File(PATH_OF_WORD_BOOKS_IN_SD);
    	//このアプリ用のディレクトリが存在しなかった場合、作成してしまう
    	if(!parentDir.exists()){
    		if(parentDir.mkdirs()){
    			//このアプリ用のディレクトリの作成に成功
    			showToast(context, R.string.utils_made_folder);
    		}else{
    			//作成できなかったら、エクスポート終了
    			showToast(context, R.string.utils_couldnt_make_folder);
    			return null;
    		}
    	}
    	//親のディレクトリがあるはずなので、csvファイルを作成する
    	String filePath = PATH_OF_WORD_BOOKS_IN_SD 
    						+ wordBook.getWordBookName() + ".csv";
    	File exportingFile = new File(filePath);
		ArrayList<Word> wordArray = getWords(context, wordBook.getID());
		FileOutputStream fos = null;
		OutputStreamWriter osw = null;
		BufferedWriter bw = null;
        String csvString = "";	//単語帳データのcsv
        
        try {
        	for(int i=0; i<wordArray.size(); i++){
        		Word word = wordArray.get(i);
        		csvString += "\"" + word.getQuestion() + "\"," 
        					+ "\"" + word.getAnswer() + "\","
        					+ "\"" + word.getExplanation() + "\"\n";
        	}
        	//このファイルを作成
        	exportingFile.getParentFile().mkdir();
        	
            fos = new FileOutputStream(exportingFile, false);
            osw = new OutputStreamWriter(fos, "UTF-8");
            bw = new BufferedWriter(osw);
            bw.write(csvString);
            bw.flush();
            showToast(context,
            		String.format(context.getString(R.string.utils_export),
       				wordBook.getWordBookName()));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(bw != null || osw != null || fos != null)
				try {
					bw.close();
					osw.close();
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        }
        return exportingFile;
	}
	
	
	/**（SDカード用）
	 * 指定したパス内にあるcsvファイルを検索するメソッド
     * csvファイルが集められたリストを返す。なかった場合はnullを返す
     * @param context application's context
     * @param file  the file you want to look into
     * (File should contain the path).
     * @return csvFiles(if it contains nothing, then null)*/
    public static ArrayList<File> searchCsvFiles(Context context, File file){
    	
    	ArrayList<File> csvFiles = null;
    	if(file == null) return null;
    	//インポートするファイル検索をしていると伝える
    	showToast(context, R.string.utils_search_files);
    	
    	if(!file.exists()){	//インポート先のファイルが存在しない場合
    		if(file.mkdirs()){
    			showToast(context, R.string.utils_made_folder);
    		}else{
    			showToast(context, R.string.utils_couldnt_make_folder);
    		}
    	}else{
    		csvFiles = searchFiles(file, new ArrayList<File>(), "csv");
			if(csvFiles.isEmpty()){
				//CSVファイルが見つからなかった場合
				showToast(context, R.string.utils_no_csv);
				csvFiles = null;
			}
    	}
		return csvFiles;
    }
	
	/**（SDカード用）
	 * 再帰的に指定した拡張子のファイルを検索するメソッド
	 * 検索に引っ掛かったファイルはリストに格納されて、最終的にリストを返却する*/
	public static ArrayList<File> searchFiles(File file,
										ArrayList<File> filesArray,
										String extension) {
		if(file.isDirectory()){
			//file がディレクトリならそれ以下のディレクトリ、ファイルを捜査
    		File[] files = file.listFiles();
    		if(files != null)
	    		for(File f : files)
	    			searchFiles(f, filesArray, extension); // 再帰  
    	}else{ //file がファイルの場合、それがcsvファイルかどうかを判断
    		//csvファイルであるならば、リストに加える
        	if(file.getName().endsWith("." + extension)
        		|| file.getName().endsWith("." + extension.toUpperCase())){
        		filesArray.add(file);
    		}
    	}
		//ディレクトリ内にファイルが一つもなかったか、fileがファイルだった場合、値を返す
		return filesArray;
	}
	
	
	
	
	
	
	/**(dropbox用)
	 * 指定したパス内にあるcsvファイルを検索するメソッド
	 * Dropbox apiを使っているため、entryのリストを返す。何もなかった場合はnullを返す*/
    public static ArrayList<Entry> searchCsvFiles( final Context context,
    							final DropboxAPI<AndroidAuthSession> api,
    											final String directoryName){
    	List<Entry> folders = null;
    	ArrayList<Entry> csvFiles = new ArrayList<DropboxAPI.Entry>();
    	//インポートするファイル検索をしていると伝える
    	showToast(context, R.string.utils_search_files);
    	
		try {
			//ルートパスにあるフォルダーを100個まで探す
			folders = api.search("", directoryName, 100, false);
		} catch (DropboxException e) {
			e.printStackTrace();
		}
		if(folders.isEmpty()){	//所定のフォルダがないので、作成するかを尋ねる
			// フォルダ作成を尋ねるダイアログ
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setMessage(context.getString(
									R.string.utils_make_folder))
			       .setPositiveButton("Yes",
			    		   new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id){
			                try {
								api.createFolder(directoryName);
							} catch (DropboxException e) {
								showToast(context, e.toString());
							}
							Activity a = (Activity)context;
							a.finish();
			           }
			       })
			       .setNegativeButton("No",
			    		   new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id){
			                dialog.cancel();
			                Activity a = (Activity)context;
							a.finish();
			           }
			       });
			AlertDialog alert = builder.create();
			alert.show();
			
		}else{	// "WordBooks"　という名のつくフォルダは全部調べる。
			for(Entry e: folders){
				if(e.isDir){
					try {
						List<Entry> files = api.search(e.path, ".csv",
														100, false);
						if(!files.isEmpty())
							csvFiles.addAll(files);
					} catch (DropboxException e1) {
						showToast(context, e1.toString());
					}
				}
			}
			
			if(csvFiles.isEmpty())
				showToast(context, R.string.utils_no_csv);
			return csvFiles;
		}
		/*
		if(entry != null){
			csvFiles = searchFiles( entry, new ArrayList<Entry>(), "csv");
			if(csvFiles.isEmpty()){
	    		//CSVファイルが見つからなかった場合
	    		showToast(context, context.getString(R.string.utils_no_csv));
				csvFiles = null;
	    	}
		}
		*/
		return csvFiles;
    }
	
	
	
    /**渡されたパスに存在するcsvファイルを読み込むメソッド
     * 存在しなかった場合は、空のArrayListが返却される*/
    public static ArrayList<Word> readCsvFiles(String path){
    	BufferedReader br = null;
    	ArrayList<Word> wordArray = new ArrayList<Word>();
    	String strLine = "";
    	try{
    		br = new BufferedReader(new FileReader(path));
    		//一行ずつ読み取って、Stringに格納
    		while((strLine = br.readLine()) != null){
    			//csvの一行を , で分割
    			String[] wordArgs = strLine.split(",");
    			//単語、答え、解説の長さを求めておき、substringする(" を外すのに使う)
    			int[] wordEnd = {wordArgs[0].length(),
    							wordArgs[1].length(),
    							wordArgs[2].length()};
    			//data[0]:question, data[1]:answer, data[2]:explanation
    			String[] data = new String[3];
    			
    			for(int i=0; i<3; i++){
    				//最初の文字が　"だった場合、""に囲まれていると考え、中のみを抽出
	    			if(wordArgs[i].charAt(0) == '\"')
	    				data[i] = wordArgs[i].substring(1, wordEnd[i] - 1);
	    			else
	    				data[i] = wordArgs[i];
    			}
    			Word word = new Word();
    			//読み込んだ一行を切りとって、正式なデータに加工して読み取る
    			word.setQuestion(data[0]);
    			word.setAnswer(data[1]);
    			word.setExplanation(data[2]);
    			wordArray.add(word);
    		}
    	} catch (FileNotFoundException e) {
			e.printStackTrace();
    	} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();	//データ形式が正しくない。TODO
		} catch (IOException e) {
			e.printStackTrace();
		}finally{	//BufferedReaderを閉じる
			try {
				if(br != null) br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return wordArray;
    }
}

