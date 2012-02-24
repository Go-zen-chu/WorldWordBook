package com.primer.world.android;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class WordBookAdapter extends ArrayAdapter<WordBook> {

	private Context mContext = null;
	private LayoutInflater mLayoutInflater = null;
	private ViewHolder mViewHolder = null;
	private DateFormat mDateFormat = null;
    private DateFormat mTimeFormat = null;
    
    //リスト表示を早くするためのViewHolder(findViewByIdの回数を減らせる)
    private static class ViewHolder{
    	TextView wordBookName_tv;
    	TextView date_tv;
    }
    
    /**コンストラクタ */
    public WordBookAdapter(Context context,
    					ArrayList<WordBook> wordBookArray) {
        super(context, R.layout.two_vertical_text_inflater, wordBookArray);
        mContext = context;
        //Listのレイアウトを作るのに必要
        mLayoutInflater = LayoutInflater.from(context);
        mDateFormat = android.text.format.DateFormat.getDateFormat(context);
        mTimeFormat = android.text.format.DateFormat.getTimeFormat(context);
    }
    
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		//position行のWordBookデータを取得
		final WordBook wordBook = getItem(position);
		
	    if(convertView == null) {	//初めて表示される単語のとき
	    	convertView = mLayoutInflater.inflate(
	    						R.layout.two_vertical_text_inflater, null);
	    	mViewHolder = new ViewHolder();
		    mViewHolder.wordBookName_tv = (TextView)convertView.findViewById(
	    									R.id.twoVerticalTextInf_TV1);
		    mViewHolder.date_tv	= (TextView)convertView.findViewById(
	    									R.id.twoVerticalTextInf_TV2);
		    convertView.setTag(mViewHolder);
		    
	    }else{	//一度は表示された単語
	    	mViewHolder = (ViewHolder)convertView.getTag();
	    }
	    
	    if(mViewHolder != null){
	    	//表示データの設定
	    	mViewHolder.wordBookName_tv.setText(wordBook.getWordBookName());	    	
	    	Date d = new Date(wordBook.getTestedDate());
		    mViewHolder.date_tv.setText(
		    	String.format(
		    		mContext.getString(R.string.word_book_adapter_date),
		    		mDateFormat.format(d) + " " + mTimeFormat.format(d)));
	    }
		return convertView;
	}
	
}
