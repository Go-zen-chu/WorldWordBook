package com.primer.world.android;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.primer.world.android.ListingRegisteredUrlActivity.SiteNameAndUrl;

public class TestActivity extends Activity {

	public static final int NORMAL = 0;	//テストのモード
	public static final int REVERSE = 1;
	public static final int RANDOM = 2;
	public static final int DIFFICULTY = 3;
	private WordBook mWordBook = null;
	private ArrayList<Word> mWordArray = null;
	private int mTestMode = NORMAL;
	private int mQuestionCount = 0;	//テストの場所を示すカウンタ（0 ~ 単語数-1）
	private int mWordBookSize = 0;
	private int mAccuracyRate = 25; //DIFFICULTY用の正答率
	private Word mWord = null;
	private ArrayList<SiteNameAndUrl> mSiteNameAndUrlList = null;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();	//　Intentから単語帳を取得する        
        if(intent != null){				// intentの中身がある場合
    		mWordBook = (WordBook)intent.getSerializableExtra(
    								getPackageName() + ".wordBook");
    		//タイトル設定
            setTitle(String.format(getString(R.string.test_title),
            		mWordBook.getWordBookName()));
            //単語帳が正確なIDを持っている場合
            if(mWordBook.getID() > 0){
            	mTestMode = Utils.getTestMode(this, mWordBook.getID());
            	//解説部分に登録したサイトで調べられるボタンを作成するためのデータ取得
            	mSiteNameAndUrlList 
            		= Utils.getRegisteredSiteNameAndUrls(this,
            									mWordBook.getID());
            	testInitialization();//テストモードに応じた設定の初期化
            }
        }else
        	finish();
    }
    
////////////////////////////////////////////////////////////////////////////
    
    /**テスト設定の初期化*/
    public void testInitialization(){
    	//レイアウトの設定
    	setContentView(R.layout.test_layout_1);
    	TextView question_tv 
    		= (TextView)findViewById(R.id.test_1_questionTV);
    	TextView accuracy_rate_tv_1 
    		= (TextView)findViewById(R.id.test_1_accuracyRateTV);
    	TextView test_count_tv 
    		= (TextView)findViewById(R.id.test_1_testCountTV);
    	
    	switch(mTestMode){
    		//正順テストの場合
	    	case NORMAL:
	        	mWordArray = Utils.getWords(this, mWordBook.getID());
	        	mWordBookSize = mWordArray.size();
	        	//ブックマークが登録されていた場合、そこから始める
	        	searchArray(mWordArray,
	        				Utils.getBookMark(this,mWordBook.getID()));
	        	mWord = mWordArray.get(mQuestionCount);	//テストする単語を取得
	        	question_tv.setText(mWord.getQuestion());
	        	accuracy_rate_tv_1.setText(
	        		String.format(getString(R.string.test_accuracy_rate),
        				Utils.returnAccuracyRate(mWord.getWrongCount(),
        										mWord.getTestCount())));
	        	test_count_tv.setText(
	        		String.format(getString(R.string.test_test_count),
	        				mWord.getTestCount()));
	        	break;
	        
	        //問題、解答が逆であるテストの場合
	    	case REVERSE:
	        	mWordArray = Utils.getWords(this, mWordBook.getID());
	        	mWordBookSize = mWordArray.size();
	        	searchArray(mWordArray,
       				 		Utils.getBookMark(this, mWordBook.getID()));
	        	mWord = mWordArray.get(mQuestionCount);
	        	question_tv.setText(mWord.getAnswer());//反転だから答えを表示
	        	accuracy_rate_tv_1.setText(
	        		String.format(getString(R.string.test_accuracy_rate),
        				Utils.returnAccuracyRate(mWord.getWrongCount(),
        										mWord.getTestCount())));
	        	test_count_tv.setText(
	        		String.format(getString(R.string.test_test_count),
	        				mWord.getTestCount()));
	        	break;
	        	
	        //ランダムテストの場合
	    	case RANDOM:
	        	mWordArray = Utils.getWords(this, mWordBook.getID());
	        	mWordBookSize = mWordArray.size();
	        	randomArray(mWordArray);
	        	mQuestionCount = 0;
	        	mWord = mWordArray.get(mQuestionCount);
	    		question_tv.setText(mWord.getQuestion());
	    		accuracy_rate_tv_1.setText(
	        		String.format(getString(R.string.test_accuracy_rate),
	        				Utils.returnAccuracyRate(mWord.getWrongCount(),
        											mWord.getTestCount())));
	        	test_count_tv.setText(
	        		String.format(getString(R.string.test_test_count),
	        				mWord.getTestCount()));
	    		break;
	    		
	    	//難易度順のテストの場合
	    	case DIFFICULTY:
	    		mAccuracyRate = 25;//正答率は最初25%以下から
	    		setCorRateSlide();	//スライドを挟む
	    		break;
		}
    }
    
    /**ブックマーク（単語ID）をもつ単語が単語帳のどこにあるのかを調べるメソッド<br>
     * もし、ブックマークした単語が削除されていた場合、最初から始める*/
    public void searchArray(ArrayList<Word> tng_array, int book_mark){
    	for(int i=0; i<mWordBookSize; i++){
    		if(tng_array.get(i).getID() == book_mark){
    			mQuestionCount = i;	//この場所にブックマークされていた。
    			return;	//代入が終われば調べる必要はない
    		}
    	}
    	mQuestionCount = 0;	// 検索した結果、存在しなかった場合（削除などの理由）
    }
    
    /**正答率を示すスライドを入れる(難問順テストに使う)<br>
     * 難易度（25...25%以下,50...50%以下,75...75%以下,100...100%以下）*/
    public void setCorRateSlide(){
    	if(mAccuracyRate > 100){	//テスト自体が終了した
    		finishTest(0);
    		return;
    	}else{
	    	setContentView(R.layout.test_layout_3);//スライドのレイアウトを設定
			TextView accuracy_rate_tv 
				= (TextView)findViewById(R.id.test_3_accuracyRateTV);
			accuracy_rate_tv.setText(
				String.format(getString(R.string.test_accuracy_rate_under),
					mAccuracyRate));
			mWordArray = Utils.getWordsByAccuracyRate(
								this,mWordBook.getID(), mAccuracyRate);
			mWordBookSize = mWordArray.size();//同じ正答率の要素数を保存
			mQuestionCount = 0;	//問題は最初から
    	}
	}
    
    /**ArrayListをランダムにするメソッド*/
    public static void randomArray(ArrayList<Word> tangoArray){
    	ArrayList<Word> tmpArray = new ArrayList<Word>();
    	while(tangoArray.size() > 0){
    		int i = (int)(Math.random()*tangoArray.size());
    		tmpArray.add(tangoArray.remove(i));
    	}
    	for(Word tng:tmpArray){
    		tangoArray.add(tng);
    	}
    }
    
    /**テストを終える時の処理<br>
     * ブックマークの初期化、テスト日の保存、テストモードの保存、<br>
     * レコードに単語帳全体平均の正答率の保存、レコードに日付の保存*/
    public void finishTest(int bookMark){
    	long time = System.currentTimeMillis();
    	// 単語帳の結果を残す(ブックマークは0)
    	Utils.updateTestedWordBookRecord(
    			this, mWordBook.getID(), bookMark, time, mTestMode);
    	//　レコードを更新する
    	Utils.createRecord(this, mWordBook.getID(), time);
		finish();	//アクティビティの終了
	}
    
//////////////////////////////////////////////////////////////////////////   
    //テストを進行させるonClick
    
    /**問題を表示させるメソッド*/
    public void showQuestion(View v){
		mWord = mWordArray.get(mQuestionCount);
		//指定した正答率に単語がなかった場合、次の正答率に移行
		if(mTestMode == DIFFICULTY && !mWord.hasID()){
			mAccuracyRate += 25;
			setCorRateSlide();
			return;
		}
		setContentView(R.layout.test_layout_1);
    	TextView question_tv 
    		= (TextView)findViewById(R.id.test_1_questionTV);
    	TextView accuracy_rate_tv_1 
    		= (TextView)findViewById(R.id.test_1_accuracyRateTV);
        TextView test_count_tv 
        	= (TextView)findViewById(R.id.test_1_testCountTV);
        
        if(mTestMode == REVERSE){
    		question_tv.setText(mWord.getAnswer());	//反転だから答え表示
        }else{
	    	question_tv.setText(mWord.getQuestion());
        }
        accuracy_rate_tv_1.setText(
        		String.format(getString(R.string.test_accuracy_rate),
        				Utils.returnAccuracyRate(mWord.getWrongCount(),
    											mWord.getTestCount())));
        	test_count_tv.setText(
        		String.format(getString(R.string.test_test_count),
        				mWord.getTestCount()));
    }
    
    
    /**解答、解説を表示させるメソッド*/
    public void showAnswer(View v){
    	setContentView(R.layout.test_layout_2);
    	TextView answer_tv 
    		= (TextView)findViewById(R.id.test_2_answerTV);
    	TextView wrong_count_tv 
    		= (TextView)findViewById(R.id.test_2_wrongCountTV);
    	TextView explanation_tv 
    		= (TextView)findViewById(R.id.test_2_explanationTV);
    	//登録されているサイトを調べられるボタンを作成
    	Button[] buttons 
    		= {	(Button)findViewById(R.id.test_2_webButton1),
    			(Button)findViewById(R.id.test_2_webButton2),
    			(Button)findViewById(R.id.test_2_webButton3)};	
       
        wrong_count_tv.setText(
        		String.format(getString(R.string.test_wrong_count),
        				mWord.getWrongCount()));
        explanation_tv.setText(mWord.getExplanation());
        //問題解答反転テストだったら、答えは問題部となる。それ以外は、解答
        answer_tv.setText(
        	(mTestMode == REVERSE)? mWord.getQuestion() : mWord.getAnswer());     
        //以下、登録したURLに問題部分を埋め込んで、ウェブサイトを見れるようにする箇所
        //URLに問題部を埋め込むために作成
        final String question 
        	= (mTestMode == REVERSE)? mWord.getAnswer() : mWord.getQuestion();
        for(int i=0; i<mSiteNameAndUrlList.size(); i++){
        	final SiteNameAndUrl registeredData = mSiteNameAndUrlList.get(i);
        	//登録したURLに、問題部分を埋め込んで、webで問題部分を検索できるようにする
        	buttons[i].setText(registeredData.getSiteName());
        	buttons[i].setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i = new Intent(Intent.ACTION_VIEW);
					// @wordを問題と置き換えて、検索できるようにする。
					i.setData(Uri.parse(
						registeredData.getUrl().replaceAll("@word", question))); 
					startActivity(i); 
				}
			});
        }
    }
    
    
    
    /**次の問題に移るための準備をする。主にテスト終了判定*/
    public void prepareForNextQuestion(View v){
    	Utils.updateTestedWordRecord(this, mWord);//テスト結果を更新する
    	mQuestionCount++;	//次の問題に移る
    	
    	switch(mTestMode){
	    	case NORMAL:
	    	case REVERSE:
	    		if(mQuestionCount >= mWordBookSize){//テストが終わった
	    			finishTest(0);
	    			return;
	    		}
	    		break;
	    		
	    	case RANDOM:
	    		if(mQuestionCount >= mWordBookSize){//テストが一週した
	        		testInitialization();
	        		return;
	        	}
	        	break;
	    	
	    	case DIFFICULTY:
	    		if(mQuestionCount >= mWordBookSize){//同正答率のテストは終了した
	    			mAccuracyRate += 25;
	    			setCorRateSlide();
	    			return;
	    		}
	    		break;
    	}
    	showQuestion(v);
    }
    
    /**答えを間違えたとき、wrong_countを一つ上げる*/
    public void addWrongCount(View v){
    	int wrong_count = mWord.getWrongCount() + 1;
    	Utils.updateWrongCount(
    			this, mWord.getID(), wrong_count);
    	mWord.setWrongCount(wrong_count);
    	prepareForNextQuestion(v);
    }

////////////////////////////////////////////////////////////////////////////
    
    /*戻るボタンが押された時、止めた場所(ブックマーク) を保存する*/
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
        	switch(mTestMode){
	        	case NORMAL:
	        	case REVERSE:
	        		askBookMarkDialog();
	                break;
	                
	        	case RANDOM:
	        	case DIFFICULTY:
	        		finishTest(0);//ブックマークを初期化
                  	break;
        	}
        }
        return false;//戻るボタンで戻れないようにする（ただし、finish()で終了）
    }
////////////////////////////////////////////////////////////////////////////
    
    //メニューの作成(文字と標準のアイコンを加える)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(0, Menu.FIRST, Menu.NONE,
    			getString(R.string.test_normal))
    				.setIcon(R.drawable.ic_menu_normal);
    	menu.add(0, Menu.FIRST + 1, Menu.NONE,
    			getString(R.string.test_reverse))
    				.setIcon(R.drawable.ic_menu_reverse);
    	menu.add(0, Menu.FIRST + 2, Menu.NONE,
    			getString(R.string.test_random))
    				.setIcon(R.drawable.ic_menu_random);
    	menu.add(0, Menu.FIRST + 3, Menu.NONE,
    			getString(R.string.test_difficulty))
    				.setIcon(R.drawable.ic_menu_difficulty);
    	return super.onCreateOptionsMenu(menu);
    }
    
    //メニューのボタンが押された時の動作
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()){
    		case Menu.FIRST:	//正順
    			mTestMode = NORMAL;
    	 		break;
    	 	
    	 	case Menu.FIRST + 1:	//表裏反転
    	 		mTestMode = REVERSE;
    	 		break;
    	 		
    	 	case Menu.FIRST + 2:	//ランダム
    	 		mTestMode = RANDOM;
    	 		break;
    	 		
    	 	case Menu.FIRST + 3:	//難問集
    	 		mTestMode = DIFFICULTY;
    	 		break;
    	}
    	testInitialization();//テストの初期化
    	return super.onOptionsItemSelected(item);
    }
////////////////////////////////////////////////////////////////////////////
    
    /**ブックマーク登録を尋ねるアラート*/
    public void askBookMarkDialog() {
       	AlertDialog.Builder adb = new AlertDialog.Builder(this);
    	adb.setMessage(getString(
    									R.string.test_book_mark_dialog));
    	adb.setPositiveButton("yes",
                 new DialogInterface.OnClickListener(){
                     @Override
                     public void onClick( DialogInterface dialog,int which){
                    	finishTest(mWord.getID());//始まる単語のを保存する
                     }
                 });
    	adb.setNegativeButton("no",
                 new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick( DialogInterface dialog,int which){
                    	 finishTest(0);//ブックマークの初期化
                     }
                 });
        AlertDialog alertDialog = adb.create();
        alertDialog.show();
	}
}

