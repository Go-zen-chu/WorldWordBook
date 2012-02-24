package com.primer.world.android;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.RESTUtility;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.TokenPair;

public class ManageDropboxActivity extends Activity {

	final static private String APP_KEY = "";
	final static private String APP_SECRET = "";
	/** ドロップボックスへのアクセス方法（アプリ作成時に用いる） */
	final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;
	final static private String ACCOUNT_PREFS_NAME = "preferense";
	final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
	final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";
	final private String WORD_BOOK_DIR = "WordBooks";

	private int mMode = -1;
	private DropboxAPI<AndroidAuthSession> mApi = null;
	private boolean mLoggedIn = false;
	/** なんか、うまくログアウト出来ないので、設定 */
	private boolean mAuthenticationStarted = false;
	private WordBook mWordBook = null;

	// UI
	private Button submit_btn;
	private ListView file_lv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manage_dropbox_layout);
		// エクスポートかインポートかを判断する
		Intent intent = getIntent();
		if (intent.hasExtra(getPackageName() + ".mode")) {
			int mode = intent.getIntExtra(getPackageName() + ".mode", -1);
			if (mode == Utils.IMPORT) {
				mMode = mode;
			} else if (mode == Utils.EXPORT) {
				mMode = mode;
				mWordBook = (WordBook) intent.getSerializableExtra(
													getPackageName() + ".wordBook");
			}
		} else {
			finish();
		}
		// 権利化を行うセッションインスタンスを作成
		AndroidAuthSession session = buildSession();
		mApi = new DropboxAPI<AndroidAuthSession>(session);

		submit_btn = (Button) findViewById(R.id.manageDropbox_submitBTN);
		submit_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mLoggedIn) {
					// sessionからcredentialデータを削除（ログアウト処理の一環）
					mApi.getSession().unlink();
					// プレファレンスに登録していたアカウント情報を削除
					SharedPreferences prefs = getSharedPreferences(
							ACCOUNT_PREFS_NAME, MODE_PRIVATE);
					Editor editor = prefs.edit();
					editor.clear().commit();
					setUserInterface(false); // UIを変化させる
				} else {
					// ログインしていない時にボタンを押すことで、権限化を始める
					mAuthenticationStarted = true;
					// ドロップボックスのアプリを起動するか、webに繋いでログインさせる
					mApi.getSession()
							.startAuthentication(ManageDropboxActivity.this);
				}
			}
		});
		file_lv = (ListView) findViewById(R.id.manageDropbox_LV);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// ここで実はログイン処理を行ったかどうかを見ている
		AndroidAuthSession session = mApi.getSession();
		// 以下、ドロップボックスの権限化を行うためonResumeに必要
		if (session.authenticationSuccessful() && mAuthenticationStarted) {
			try {
				// 権限化を終わらせる
				session.finishAuthentication();
				// ログインすることで取得したキーをローカルに蓄えておく
				TokenPair tokens = session.getAccessTokenPair();
				storeKeys(tokens.key, tokens.secret);
				mAuthenticationStarted = false;
			} catch (IllegalStateException e) {
				Utils.showToast(
						this,
						getString(R.string.manage_dropbox_could_not_authenticate)
								+ e.getLocalizedMessage());
			}
		}
		// ログインしているかどうかを判断し、状態に応じてUIを変化させる
		setUserInterface(mApi.getSession().isLinked());
	}

	// ////////////////////////////////////////////////////////////////////

	private AndroidAuthSession buildSession() {
		// ログインに必要なAPP_KEY,APP_SECRETを格納するインスタンスを作成
		AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
		AndroidAuthSession session;

		String[] storedKeys = getKeys();
		if (storedKeys != null) { // キーが登録されているとき（前回、ログインしたまま）
			AccessTokenPair accessToken = new AccessTokenPair(storedKeys[0],
					storedKeys[1]);
			session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE,accessToken);
		} else { // 登録されているキーがないとき（最初または前回ログアウトした）
			session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
		}
		return session;
	}

	private String[] getKeys() {
		// プレファレンスからログインに必要なAPP_KEY,APP_SECRETを取得
		SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME,
				MODE_PRIVATE);
		String key = prefs.getString(ACCESS_KEY_NAME, null);
		String secret = prefs.getString(ACCESS_SECRET_NAME, null);
		// ちゃんとキーなどが取得できたかどうかを判定
		if (key != null && secret != null) {
			String[] keys = new String[2];
			keys[0] = key;
			keys[1] = secret;
			return keys;
		} else {
			return null;
		}
	}

	/** ログイン状態に応じて、UIを変化 */
	private void setUserInterface(boolean loggedIn) {
		mLoggedIn = loggedIn;
		if (loggedIn) {
			// ログインするときは、ドロップボックスからログアウトできるようなレイアウトになる
			submit_btn.setText(getString(R.string.manage_dropbox_log_out));
			switch (mMode) {
			case Utils.IMPORT:
				// リストビューにはインポートしたいファイル名を表示させる
				file_lv.setVisibility(View.VISIBLE);
				ArrayList<Entry> csvFiles = Utils.searchCsvFiles(this, mApi,
						WORD_BOOK_DIR);
				if (!csvFiles.isEmpty()) {
					// csvFile内が空でなかったら、リストに表示
					EntryAdapter adapter = new EntryAdapter(getApplicationContext(),
							csvFiles);
					file_lv.setAdapter(adapter);
					file_lv.setOnItemClickListener(new EntryClickListener(
							getApplicationContext()));
				}
				break;

			case Utils.EXPORT:
				// エクスポートの場合、ただファイルを書き込むだけなので、ボタンは要らない
				submit_btn.setVisibility(View.INVISIBLE);
				ExportCsvFileToDropBox export = new ExportCsvFileToDropBox(this, mApi, mWordBook);
				export.execute();
				// finish();
				break;
			}
		} else {
			// ログアウト時はここで他のビューが表示されないように設定している
			submit_btn.setText(getString(R.string.manage_dropbox_log_in));
			file_lv.setVisibility(View.GONE);
		}
	}

	/** 後でも使うため、プリファレンスにアクセスキーを保存 */
	private void storeKeys(String key, String secret) {
		SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME,
				MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putString(ACCESS_KEY_NAME, key);
		editor.putString(ACCESS_SECRET_NAME, secret);
		editor.commit();
	}
	
////////////////////////////////////////////////////////////////////////

	/** インポートする際、タッチすればそのファイルを取得する */
	private class EntryClickListener implements AdapterView.OnItemClickListener{
		Context mContext = null;
		//constructor
		public EntryClickListener(Context context) {
			mContext = context;
		}
		@Override
		public void onItemClick(AdapterView<?> av, View v, 
											int position, long id) {
			int newWordBookID = -1;
			Entry entry = (Entry) ((ListView) av).getItemAtPosition(position);
			// ファイル名を分割し、それを単語帳名にする
			String[] splited_name = entry.fileName().split("\\.");
			newWordBookID = Utils
					.createWordBookReturnID(mContext, splited_name[0]);
			// SDカードに一旦ファイルを移すため、SDへのパスを作成
			String copiedFilePath = Utils.PARENT_PATH_OF_SD + "WordBooks/"
					+ splited_name[0] + "_copied.csv";
			File copiedFile = new File(copiedFilePath);
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(copiedFile);
				// ドロップボックスからファイルを取得、SDカードに出力する
				mApi.getFile(entry.path, null, fos, null);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (DropboxException e) {
				e.printStackTrace();
			} finally {
				try {
					if (fos != null)
						fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (newWordBookID > 0) {
				ArrayList<Word> wordArray = Utils.readCsvFiles(copiedFilePath);
				Utils.createWordsFromArray(mContext, wordArray, newWordBookID);
				finish();
			} else {
				Utils.showToast(mContext, R.string.import_failed);
			}

		}
	}

	////////////////////////////////////////////////////////////////////////

	private class EntryAdapter extends ArrayAdapter<Entry> {

		private LayoutInflater mLayoutInflater = null;
		private DateFormat mDateFormat = null;
		private ViewHolder mViewHolder = null;

		// findViewの回数を少なくするためのViewHolder
		private class ViewHolder {
			TextView entryName_tv;
			TextView date_tv;
			TextView file_size_tv;
		}

		/** コンストラクタ */
		public EntryAdapter(Context context, ArrayList<Entry> entryArray) {
			super(context, R.layout.three_text_inflater_1, entryArray);
			mLayoutInflater = LayoutInflater.from(context);
			mDateFormat = android.text.format.DateFormat.getDateFormat(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Entry entry = getItem(position);// position行のfileを取得

			if (convertView == null) {// 初めて表示される単語のとき
				// file_inflater.xmlから1行分のレイアウトを生成
				convertView = mLayoutInflater.inflate(
						R.layout.three_text_inflater_1, null);
				mViewHolder = new ViewHolder();
				// 各idの項目に値をセット
				mViewHolder.entryName_tv = (TextView) convertView
						.findViewById(R.id.threeTextInf_1_TV1);
				mViewHolder.date_tv = (TextView) convertView
						.findViewById(R.id.threeTextInf_1_TV2);
				mViewHolder.file_size_tv = (TextView) convertView
						.findViewById(R.id.threeTextInf_1_TV3);
				// convertViewにViewに関するタグ（情報）を添付しておく
				convertView.setTag(mViewHolder);
			} else { // すでに表示された単語のときタグの情報を読み取る
				mViewHolder = (ViewHolder) convertView.getTag();
			}

			if (mViewHolder != null) {
				// ファイルの変更された日付をDate型に変換(Dropbox特有のフォーマットを利用)
				Date date = RESTUtility.parseDate(entry.modified);

				// 表示データの設定（タッチしたfileに関する情報はviewHolderに格納されている）
				mViewHolder.entryName_tv.setText(entry.fileName());
				mViewHolder.date_tv.setText("date : "
						+ ((date != null) ? mDateFormat.format(date) : ""));
				mViewHolder.file_size_tv.setText("size : "+ returnFileSize(entry));
			}
			return convertView;
		}

		/** ファイルサイズを表示するメソッド(dropbox) */
		private String returnFileSize(Entry entry) {
			long entrySize = entry.bytes;
			if (0 <= entrySize && entrySize < 1024) {
				return String.valueOf(entrySize) + "B";
			} else if (1024 <= entrySize && entrySize <= 1048576) {
				return String.valueOf(entrySize / 1024) + "KB";
			} else {
				return String.valueOf(entrySize / 1048576) + "MB";
			}
		}
	}
}
