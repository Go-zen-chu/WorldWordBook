package com.primer.world.android;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

public class TestWidget extends AppWidgetProvider {
	
	public static final String ACTION_CLICK_TEXT_VIEW 
		= "com.primer.world.android.CLICK_TEXT_VIEW";
	public static final String ACTION_CLICK_BUTTON
		= "com.primer.world.android.CLICK_BUTTON";
	
	//android:updatePeriodMillis="0" としているため、作成時の一回のみ起動
	@Override
	public void onUpdate(Context context, 
								AppWidgetManager appWidgetManager,
								int[] appWidgetIds) {	
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		//TODO
		android.os.Debug.waitForDebugger();
		for(int i=0; i<appWidgetIds.length; i++){
			int widgetID = appWidgetIds[i];
			//appWidgetのレイアウトを取得する
			RemoteViews remoteViews = new RemoteViews(
					context.getPackageName(), R.layout.test_widget_layout);
			//テキストビュー、ボタンが押された時のインテント
			Intent tv_Intent = new Intent(context,WidgetService.class);
			Intent btn_Intent = new Intent(context,WidgetSettingActivity.class);
			tv_Intent.setAction(ACTION_CLICK_TEXT_VIEW);
			btn_Intent.setAction(ACTION_CLICK_BUTTON);
			//Activityはwidgetとは別に存在するため、new task として起動する
			btn_Intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			//serviceなどで、IDを取得出来るようにする。
			tv_Intent.putExtra(context.getPackageName() +".widgetID", widgetID);
			btn_Intent.putExtra(context.getPackageName() +".widgetID",widgetID);
			//クリック時に送付するintentを決める
			PendingIntent tv_pnd_Intent = PendingIntent.getService(
											context, widgetID, tv_Intent,0);
			PendingIntent btn_pnd_Intent = PendingIntent.getActivity(
											context, widgetID, btn_Intent,0);
			//テキストビューとボタンのクリック時、設定したintentが送られる
			remoteViews.setOnClickPendingIntent(
								R.id.testWidget_QandATV, tv_pnd_Intent);
			remoteViews.setOnClickPendingIntent(
								R.id.testWidget_QuestionTV, tv_pnd_Intent);
			remoteViews.setOnClickPendingIntent(
								R.id.testWidget_BTN, btn_pnd_Intent);
			appWidgetManager.updateAppWidget(widgetID, remoteViews);
			
			//context.startService(tv_Intent);
		}
	}
	
	// ウィジェットが削除されたときに、保存していたIDなどを全部削除する。
	@Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        for(int i=0; i<appWidgetIds.length; i++){
        	int widgetID = appWidgetIds[i];
        	//SharedPreferences　が消されていないので、削除する
        	SharedPreferences pref = PreferenceManager
									.getDefaultSharedPreferences(context);
        	Editor e = pref.edit();
        	e.remove(WidgetService.makeKey(
        			WidgetService.TYPE_WORD_BOOK_ID, widgetID));
        	e.remove(WidgetService.makeKey(
        			WidgetService.TYPE_COLOR, widgetID));
        	e.remove(WidgetService.makeKey(
        			WidgetService.TYPE_WORD_ID, widgetID));
        	e.commit();
        }
    }
}
