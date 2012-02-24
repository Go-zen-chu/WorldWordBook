package com.primer.world.android;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

/** 単語の内容を編集兼作成 */
public class EditWordActivity extends Activity {

	private int mMode = -1;
	private Word mWord = null;
	private int mViewID = -1;
	private LinearLayout mLinearLayout = null;
	private EditText mQuestion_ET = null;
	private EditText mAnswer_ET = null;
	private EditText mExplanation_ET = null;
	private ListView mListView = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_word_layout);

		mLinearLayout = (LinearLayout)findViewById(R.id.editWord_LL);
		mQuestion_ET = (EditText) findViewById(R.id.editWord_questionET);
		mAnswer_ET = (EditText) findViewById(R.id.editWord_answerET);
		mExplanation_ET = (EditText) findViewById(R.id.editWord_explanationET);
		//音声認識した結果のリストを表示させるためのlist viewを作成
		mListView = (ListView)findViewById(R.id.editWord_LV);
		//音声認識時以外は表示しない
		mListView.setVisibility(View.GONE);

		Intent intent = getIntent();// インテントの取得
		if (intent != null) {
			if (intent.hasExtra(getPackageName() + ".mode")) {
				// モードに応じて、受け取るインテントが変化する
				mMode = intent.getIntExtra(getPackageName() + ".mode", -1);

				if (mMode == Utils.NEW) {
					// 新規作成の場合、データ引き継ぎがないから、インスタンスを生成
					mWord = new Word();
					// 単語の新規作成時はwordBookIDのみを受け取り、データ保存するためセット
					mWord.setWordBookID(intent.getIntExtra(
							getPackageName() + ".wordBookID", -1));
					// タイトル表示
					setTitle(getString(R.string.edit_word_title_new));
				} else if (mMode == Utils.EDIT) {
					// 単語の編集時は他のデータも受け取る
					mWord = (Word) intent.getSerializableExtra(
											getPackageName() + ".word");
					// 編集時は既存のデータを表示する
					mQuestion_ET.setText(mWord.getQuestion());
					mAnswer_ET.setText(mWord.getAnswer());
					mExplanation_ET.setText(mWord.getExplanation());
					// EditTextのカーソルを最後に移動する
					mQuestion_ET.setSelection(mWord.getQuestion().length());
					mAnswer_ET.setSelection(mWord.getAnswer().length());
					mExplanation_ET.setSelection(mWord.getExplanation().length());
					setTitle(mWord.getQuestion());// タイトル表示
				} else
					finish();
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////
	// onClick

	// 単語を保存する
	public void saveWord(View v) {
		// 未入力をはじく処理
		if (mQuestion_ET.getText().toString().equals("")) {
			Utils.showToast(this, R.string.edit_word_enter_question);
		} else if (mAnswer_ET.getText().toString().equals("")) {
			Utils.showToast(this, R.string.edit_word_enter_answer);
		} else {
			// 未入力ではない場合、データベースに保存する。
			mWord.setQuestion(mQuestion_ET.getText().toString());
			mWord.setAnswer(mAnswer_ET.getText().toString());
			mWord.setExplanation(mExplanation_ET.getText().toString());
			if (mMode == Utils.NEW) {
				// 単語の新規作成
				Utils.createWord(this, mWord);
			} else if (mMode == Utils.EDIT) {
				// 単語の更新
				Utils.updateWord(this, mWord);
			}
			finish();
		}
	}
	
	// 音声入力したものを追加していく
	public void voiceRecognition(View v) {
		// onActivityResultで条件分岐するためのID
		mViewID = v.getId();
		Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		// request code = 1234
		startActivityForResult(i, 1234);
	}

	// 音声認識で取得したデータを既存のテキストに追加する。
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
												Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 1234 && resultCode == RESULT_OK && mViewID != -1){
			//音声認識した結果の候補をArrayListに保存
			ArrayList<String> voiceData = data.getStringArrayListExtra(
							RecognizerIntent.EXTRA_RESULTS);
			//音声認識の結果表示時はLinearLayoutを非表示に
			mLinearLayout.setVisibility(View.GONE);
			mListView.setVisibility(View.VISIBLE);
			mListView.setAdapter(new ArrayAdapter<String>(getApplicationContext(),
														android.R.layout.simple_list_item_1,
														voiceData));
			mListView.setOnItemClickListener(
					new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
	                  int position, long id) {
						ListView lv = (ListView)parent;
						String voiceString = (String)lv.getItemAtPosition(position);
						switch (mViewID) {
							case R.id.editWord_voice_recognizer_1:
								//音声からのテキストを元々のテキストに追加
								mQuestion_ET.setText(mQuestion_ET.getText() 
																+ voiceString);
								break;
							case R.id.editWord_voice_recognizer_2:
								mAnswer_ET.setText(mAnswer_ET.getText() 
																+ voiceString);
								break;
							case R.id.editWord_voice_recognizer_3:
								mExplanation_ET.setText(mExplanation_ET.getText()
																+ voiceString);
								break;
						}
						//表示を元に戻す
						mLinearLayout.setVisibility(View.VISIBLE);
						mListView.setVisibility(View.GONE);
					}
			});
		}
		return;
	}
	
}
