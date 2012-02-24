package com.primer.world.android;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TestRecordActivity extends Activity {
	
	private WordBook mWordBook = null;
	private ArrayList<Word> mWordArray25 = null;//正答率25%の単語を格納
	private ArrayList<DateAndAverageAccuracyRate> mDataArray = null;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_layout_1);
        // Intentの取得
        Intent intent = getIntent();	//　Intentから単語名を取得する        
        if(intent != null){				// intentの中身がある場合
    		mWordBook = (WordBook)intent.getSerializableExtra(
    									getPackageName() + ".wordBook");
        }
        
        TextView ques_tv = (TextView)findViewById(R.id.record_1_questionsTV);
        ListView lv = (ListView)findViewById(R.id.record_1_LV);
        
        if(mWordBook.getID() > 0){
        	//正答率の低い単語を取得
        	mWordArray25 = Utils.getWordsByAccuracyRate(
        						this, mWordBook.getID(), 25);
        	String questions = "";
        	for(Word t: mWordArray25)
        		questions += t.getQuestion() + "\t";
        	ques_tv.setText(questions);
        	
        	//テストした日付、その時の平均正答率を取得
        	mDataArray = Utils.getDateAndAverageAccuracyRates(
        									this, mWordBook.getID());
        	DateAndAccurateRateAdapter adapter 
        		= new DateAndAccurateRateAdapter(this, mDataArray);
        	lv.setAdapter(adapter);
        }
        setTitle(String.format(getString(R.string.test_record_title),
        						mWordBook.getWordBookName()));
    }
    
    public void showGraph(View v){
    	if(!isConnected(this)){
    		Toast.makeText(this, getString(R.string.test_record_no_internet),
    						Toast.LENGTH_SHORT);
    		return;
    	}else{
	    	//google chart でチャートを作成するためのString
	    	String chartStr = "http://chart.apis.google.com/chart?"
				+ "chxs=0,00AA00,15,0,lt,676767|1,676767,15,0.5,l,676767"
				+ "&chxtc=0,5"
				+ "&chxt=x,y"
				+ "&cht=lc"
				+ "&chco=FF0000"
				+ "&chdlp=l"
				+ "&chg=0,25,0,0"
				+ "&chls=3"
				+ "&chma=0,0,0,20|10,10"
				+ "&chtt=Record"
				+ "&chts=676767,17";
	    	
	    	setContentView(R.layout.record_layout_2);
	    	ImageView graph_imgv 
	    				=(ImageView)findViewById(R.id.record_2_graphImgV);
	    	
	    	//1:グラフの大きさを設定するために画面のサイズを取得
	    	WindowManager wm = (WindowManager)getSystemService(
												Context.WINDOW_SERVICE);
	    	Display display = wm.getDefaultDisplay();
	    	chartStr += "&chs=" + display.getWidth()*4/5
	    				+ "x" + display.getHeight()*4/5;
	    	//2:平均正答率をグラフに書き込む
	    	String accuracyRateData = "";
	    	for(int i=0; i<mDataArray.size(); i++){
	    		if(i == mDataArray.size() - 1)	//末尾には , を入れない
	    			accuracyRateData 
	    				+= mDataArray.get(i).getAverageAccuracyRate();
	    		else
	    			accuracyRateData 
	    				+= mDataArray.get(i).getAverageAccuracyRate() + ",";
	    	}
	    	chartStr += "&chd=t:" + accuracyRateData;
	    	//3:日付をグラフに書き込む
	    	String dateData = "";
	    	SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
	    	for(int i=0; i<mDataArray.size(); i++){
	    		Date d = new Date(mDataArray.get(i).getDate());
	    		dateData += sdf.format(d) + "|"; 
	    	}
	    	chartStr += "&chxl=0:|" + dateData + "1:|0|25|50|75|100";
	    	//4:チャートのデータを作成したurlから取得
	    	try{
	    		InputStream is = (InputStream)new URL(chartStr).getContent();
	            Drawable d = Drawable.createFromStream(is, "");
	            is.close();
	            graph_imgv.setImageDrawable(d);
	    	}catch (MalformedURLException e) {
				e.printStackTrace();
			}catch (IOException e) {
	    		e.printStackTrace();
			}
    	}
    }
    
    public void backToRecord(View v) {
		onCreate(null);
	}
    
    //インターネットに繋げることが出来るかどうかを判定
    public static boolean isConnected(Context context){
        ConnectivityManager cm 
        	= (ConnectivityManager)context.getSystemService(
        								Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if( ni != null ){
            return cm.getActiveNetworkInfo().isConnected();
        }
        return false;
    }
    
///////////////////////////////////////////////////////////////////
    
	/**日付と平均正答率格納クラス*/
	public static class DateAndAverageAccuracyRate{
		long mDate = 0;
		String mAverageAccuracyRateStr = "NONE";
		
		public long getDate() {
			return mDate;
		}
		public void setDate(long date) {
			mDate = date;
		}
		public String getAverageAccuracyRate() {
			return mAverageAccuracyRateStr;
		}
		public void setAverageAccuracyRate(String aveARateStr) {
			mAverageAccuracyRateStr = aveARateStr;
		}
	}
}
