package com.primer.world.android;

import java.io.File;
import java.util.ArrayList;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

/**
 * csvファイルから単語帳をインポートするアクティビティ<br>
 * sdカード内全てのcsvファイルを検索する
 */
public class ImportFromSdCardActivity extends ListActivity {

	/** このアプリが使用するSDカード内のディレクトリのパス */
	private String mDataSavedPath = Utils.PARENT_PATH_OF_SD + "WordBooks/";
	private ArrayList<File> csvFiles = null; // csvファイルを集めるリスト

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_only_layout);

		// このアプリ専用のディレクトリ(SDカード内)でCSVファイルを検索
		csvFiles = Utils.searchCsvFiles(getApplicationContext(), 
										new File(mDataSavedPath));
		if (csvFiles == null) {
			finish();
		} else if (!csvFiles.isEmpty()) {
			// リスト表示用のアダプタ
			FileAdapter adapter = new FileAdapter(this, csvFiles);
			getListView().setAdapter(adapter);
		}
		setTitle(R.string.import_title);
	}

	// 選んだファイルの名前を取得する
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		File selectedFile = (File) l.getAdapter().getItem(position);
		int newWordBookID = -1;
		// ファイル名.csvを.で分ける
		String[] splited_name = selectedFile.getName().split("\\.");
		// csvファイルの名前と同一名称の単語帳を作り、その単語帳IDを格納する
		newWordBookID = Utils.createWordBookReturnID(this, splited_name[0]);

		if (newWordBookID > 0) {// 単語帳IDとして正しい値が格納されている時
			Utils.showToast(this, R.string.import_start);
			// ここで、実際にインポートがされている
			ArrayList<Word> wordArray = Utils
													.readCsvFiles(selectedFile.getPath());
			Utils.createWordsFromArray(this, wordArray, newWordBookID);
			Utils.showToast(this, R.string.import_finish);
			finish();
		} else {// そうでないとき
			Utils.showToast(this, R.string.import_failed);
		}
	}

}
