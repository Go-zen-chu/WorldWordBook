package com.primer.world.android;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class WordAdapter extends ArrayAdapter<Word> {

	private Context mContext = null;
	private LayoutInflater mLayoutInflater = null;
	private ViewHolder mViewHolder = null;
    
    //リスト表示を速くするためのViewHolder
    private static class ViewHolder{
    	TextView question_tv;
    	TextView accuracyRate_tv;
    	TextView answer_tv;
    }
    
    /**コンストラクタ*/
    public WordAdapter(Context context, ArrayList<Word> wordArray) {
        super(context,R.layout.three_text_inflater_1, wordArray);
        mContext = context;
        //Listのレイアウトを作るのに必要
        mLayoutInflater = LayoutInflater.from(context);
    }
    
    //このクラスの肝であるconvertViewはデータもレイアウトも含め終わったview
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final Word word = getItem(position);//position行のwordを取得
		
	    if (convertView == null){//初めて表示される単語のとき
	    	convertView = mLayoutInflater.inflate(
	    								R.layout.three_text_inflater_1, null);
	    	mViewHolder = new ViewHolder();
	    	//各idの項目に値をセット
		    mViewHolder.question_tv =(TextView)convertView.findViewById(
		    							R.id.threeTextInf_1_TV1);
		    mViewHolder.accuracyRate_tv =(TextView)convertView.findViewById(
		    							R.id.threeTextInf_1_TV2);
		    mViewHolder.answer_tv =(TextView)convertView.findViewById(
		    							R.id.threeTextInf_1_TV3);
		    //convertViewにViewに関するタグ（情報）を添付しておく
		    convertView.setTag(mViewHolder);
	    }else{	//すでに表示された単語のとき、IDは取得したから、二度目以降は必要ない
	    	//タグ付けした情報を使う
	    	mViewHolder = (ViewHolder)convertView.getTag();
	    }
	    
	    if(mViewHolder != null){
		    //表示データの設定
			mViewHolder.question_tv.setText(word.getQuestion());
			int accuracyRate = Utils.returnAccuracyRate(word.getWrongCount(),
														word.getTestCount());
		    mViewHolder.accuracyRate_tv.setText(
		    	String.format( mContext.getString(
		    		R.string.word_adapter_accuracy_rate),accuracyRate));
		    mViewHolder.answer_tv.setText(
		    	String.format( mContext.getString(
		    		R.string.word_adapter_answer), word.getAnswer()));
	    }
		return convertView;
	}
	
}
