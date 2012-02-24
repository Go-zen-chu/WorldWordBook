package com.primer.world.android;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class WidgetSettingActivity extends Activity {
	
	private int mWidgetID = -1;
	private int mWordBookID = -1;
	private ArrayList<WordBook> mWordBookArray = null;
	private int mTextColor = Color.BLACK;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.widget_setting_layout);
        Intent intent = getIntent();
        mWidgetID = intent.getIntExtra(this.getPackageName()+".widgetID", -1);
        
        //spinner に設定するための単語帳を取って来る（以下spinnerの設定）
        mWordBookArray = Utils.getWordBookNameAndID(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
        						this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(
        						android.R.layout.simple_spinner_dropdown_item);
        //アイテムを追加
        for(WordBook wordBook: mWordBookArray){
        	adapter.add(wordBook.getWordBookName());
        }
        Spinner spinner = (Spinner)findViewById(R.id.widgetSetting_spinner);
        spinner.setAdapter(adapter);// アダプターを設定
        final Context context = this;
        // スピナーのアイテムが選択された時に呼び出されるコールバックリスナーを登録
        spinner.setOnItemSelectedListener(
        						new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
            										int position, long id) {
                Spinner spinner = (Spinner)parent;
                // 選択された位置の単語帳を取得。その単語帳のIDを取得
                mWordBookID = mWordBookArray.get(
                		spinner.getSelectedItemPosition()).getID();
                //選んだ単語帳のIDをプレファレンスに保存
                SharedPreferences pref = PreferenceManager
                						.getDefaultSharedPreferences(context);
                Editor e = pref.edit();
                e.putInt(WidgetService
                		.makeKey(WidgetService.TYPE_WORD_BOOK_ID, mWidgetID),
                		mWordBookID);
                e.commit();
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        setTitle(getString(R.string.widget_setting_title));
        }
    
    public void setColor(View v){
    	final Context context = this;
    	ColorPickerDialog cpd = new ColorPickerDialog(
    			this, new ColorPickerDialog.OnColorChangedListener() {
					@Override
					public void colorChanged(int color) {
						// 色が選択されるとcolorに値が入る
						mTextColor = color;
				        //テキストの色をプレファレンスに保存しておく
				        SharedPreferences pref = PreferenceManager
				        						.getDefaultSharedPreferences(context);
				        Editor e = pref.edit();
				        e.putInt(WidgetService.makeKey(
				        		WidgetService.TYPE_COLOR, mWidgetID), mTextColor);
				        e.commit();
					}
    			},Color.BLACK);
        cpd.show();
    }
    
    public void finishSetting(View v){
    	finish();
    }
}
