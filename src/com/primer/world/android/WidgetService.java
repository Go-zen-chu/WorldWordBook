package com.primer.world.android;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

//サービスはウィジェットがタッチされる度に起動する。全ウィジェット共通なので、idに注意
public class WidgetService extends Service {
	
	public static final String TYPE_WORD_BOOK_ID = "WORD_BOOK_ID";
	public static final String TYPE_COLOR = "COLOR";
	public static final String TYPE_WORD_ID = "WORD_ID";
	private int mWidgetID = -1;
	private int mWordBookID = -1;//どの単語帳でテストをするか
	private int mWordID = -1;
	private int mTextColor = Color.BLACK;
	private Word mWord = null;

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		//TODO
		 android.os.Debug.waitForDebugger(); 
		 
		RemoteViews remoteViews = new RemoteViews(
				this.getPackageName(), R.layout.test_widget_layout);
		//widgetのIDを取得し、widgetごとに動作を制御する
		mWidgetID = intent.getIntExtra(this.getPackageName() + ".widgetID",-1);	
		//プレファレンスから単語帳のIDを取得し、そのテストをする。また色を取得する。
		SharedPreferences pref = PreferenceManager
											.getDefaultSharedPreferences(this);
		mWordBookID = pref.getInt(
				makeKey(TYPE_WORD_BOOK_ID, mWidgetID), -1);
		mTextColor = pref.getInt(
				makeKey(TYPE_COLOR, mWidgetID), Color.BLACK);
		//テキストの色を設定する
		remoteViews.setTextColor(R.id.testWidget_QandATV, mTextColor);
		remoteViews.setTextColor(R.id.testWidget_QuestionTV, mTextColor);
		
		// テキストビューが押された時、答えか解答を表示する
		if (TestWidget.ACTION_CLICK_TEXT_VIEW.equals(intent.getAction())) {
			//プレファレンスから単語IDを取得。-1のとき問題、それ以外は解答を表示
			mWordID = pref.getInt(
					makeKey(TYPE_WORD_ID, mWidgetID), -1);
			if(mWordID <= 0){//問題を表示させる
				mWord = Utils.getRandomWord(this, mWordBookID);
				remoteViews.setTextViewText(
						R.id.testWidget_QandATV, "Q");	//Question
				remoteViews.setTextViewText(
						R.id.testWidget_QuestionTV, mWord.getQuestion());
				//次は解答を表示させるために、単語IDを記録しておく
                Editor e = pref.edit();
                e.putInt(makeKey(TYPE_WORD_ID, mWidgetID), mWord.getID());
                e.commit();
			}else{//答えを表示させる
				mWord = Utils.getAnswer(this, mWordID);
				remoteViews.setTextViewText(
						R.id.testWidget_QandATV, "A"); //Answer	
				remoteViews.setTextViewText(
						R.id.testWidget_QuestionTV, mWord.getAnswer());
				//次は問題を表示させるため、単語IDを-1に戻しておく
                Editor e = pref.edit();
                e.putInt(makeKey(TYPE_WORD_ID, mWidgetID), -1);
                e.commit();
			}
		}

		// AppWidgetの更新（widget_idを設定し、特定）
     AppWidgetManager awm = AppWidgetManager.getInstance(this);
     awm.updateAppWidget(mWidgetID, remoteViews);
     
     stopSelf();//サービスを終える
   }
		
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	/**プレファレンスで保存する用のキーを作成する。*/
	public static String makeKey(String type, int widgetID){
		String widgetIDStr = String.valueOf(widgetID);
		if(type.equals(TYPE_WORD_BOOK_ID)){
			return "WORD_BOOK_ID_OF_WIDGET_" + widgetIDStr;
		}else if(type.equals("COLOR")){
			return "TEXT_COLOR_OF_WIDGET_" + widgetIDStr;
		}else if(type.equals("T_ID")){
			return "WORD_ID_OF_WIDGET_" + widgetIDStr;
		}else{
			return null;
		}
	}

}
