package com.primer.world.android;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.primer.world.android.ListingRegisteredUrlActivity.SiteNameAndUrl;

/**サイト名やURLの登録、新規作成、削除などを行うクラス*/
public class SetUrlActivity extends ListActivity {

	private Context mAppContext = null;
	private ListView mListView = null;
	/**URLを登録している単語帳*/
	private WordBook mWordBook = null;
	/**設定を変えようとしているURLなどの情報*/
	private SiteNameAndUrl mSiteNameAndUrl = null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_only_layout);
		
		mAppContext = getApplicationContext();
        mListView = getListView();
        if(mListView == null) finish();   
		
		Intent intent = getIntent();
		if(intent != null){
			if(intent.hasExtra(getPackageName() + ".wordBook")
					&& intent.hasExtra(getPackageName() + ".siteNameAndUrl")){
				mWordBook = (WordBook)intent.getSerializableExtra(
										getPackageName() + ".wordBook");
				mSiteNameAndUrl = (SiteNameAndUrl)intent.getSerializableExtra(
										getPackageName() + ".siteNameAndUrl");
				setTitle(mWordBook.getWordBookName());
			}
		}else{
			finish();
		}
		// リストでのジェスチャー実装
		final GestureDetector gestureDetector 
			= new GestureDetector(new ListGestureDetector());
		OnTouchListener gestureListener = new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event); 
			}
		 };
		mListView.setOnTouchListener(gestureListener);
		// スクロールの高速化
		mListView.setFastScrollEnabled(true);
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		//sqliteに保存されたサイトの名前、URLを取得。
		ArrayList<SiteNameAndUrl> dataArray 
							= Utils.getSiteNameAndUrls(mAppContext);
		PickUrlAdapter adapter = new PickUrlAdapter(this, dataArray,
												mWordBook, mSiteNameAndUrl);
		mListView.setAdapter(adapter);
	}
	
	/////////////////////////////////////////////////////////////////////
    
    //メニューの作成(文字と標準のアイコンを加える)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(0, Menu.FIRST, Menu.NONE,
    			getString(R.string.word_book_list_new))
    				.setIcon(android.R.drawable.ic_menu_add);
    	return super.onCreateOptionsMenu(menu);
    }
    
    //メニューのボタンが押された時の動作
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()){
    		case Menu.FIRST:	//新たなサイトとURLを登録
    			enterSiteNameAndUrlDialog(new SiteNameAndUrl());
    	 		break;
    	}
    	return super.onOptionsItemSelected(item);
    }
    
    /////////////////////////////////////////////////////////////////////
    //Dialogs
    
    /**サイト名、URLを編集するダイアログ*/
    private void enterSiteNameAndUrlDialog(
    								final SiteNameAndUrl siteNameAndUrl){
 	  final Context context = this;
 	  AlertDialog.Builder adBuilder = new AlertDialog.Builder(context);
     LayoutInflater factory = LayoutInflater.from(context);
     //dialogにviewを設定
     final View entryView 
     	= factory.inflate(R.layout.enter_site_name_and_url_dialog, null);
     adBuilder.setTitle(context.getString(R.string.set_url_title_of_dlg));
     adBuilder.setView(entryView);
     final EditText site_name_et = (EditText)entryView.findViewById(
     							R.id.enterSiteNameAndUrlDlg_siteNameET);
     final EditText url_et = (EditText)entryView.findViewById(
     							R.id.enterSiteNameAndUrlDlg_urlET);
     //新規作成か編集かどうかは、urlIDを持っているかどうかによる
     if(siteNameAndUrl.hasUrlID()){
     		//編集時：すでに登録されているテキストを表示させる
         site_name_et.setText(siteNameAndUrl.getSiteName());
         url_et.setText(siteNameAndUrl.getUrl());
         //カーソルの位置を最後にする
         site_name_et.setSelection(siteNameAndUrl.getSiteName().length());
     }
     adBuilder.setPositiveButton("OK",
        	new DialogInterface.OnClickListener(){
            	@Override
				public void onClick(DialogInterface dialog, int whichButton){
            		// @word が入っていないもの、未入力は除く
	               if( !site_name_et.getText().toString().equals("")
	                		&& !url_et.getText().toString().equals("")){
	                	if( url_et.getText().toString().contains("@word")){
		               	//siteNameAndUrlに値を代入してやる
		            		siteNameAndUrl.setSiteName(
		            					site_name_et.getText().toString());
		            		siteNameAndUrl.setUrl(
		            					url_et.getText().toString());
		            		if(siteNameAndUrl.hasUrlID()){ //編集の場合、更新
			                	Utils.updateSiteNameAndUrl(
			                			mAppContext, siteNameAndUrl);
		            		} else {	//新規作成の場合、新しいデータを格納
		            			Utils.createSiteNameAndUrl(
		            					mAppContext, siteNameAndUrl);
		               	}
		            		onResume();	//画面の更新
	                	} else {
	                		Utils.showToast(context,
	                				R.string.set_url_use_this_word);
	                	}
	               }else{
	                	Utils.showToast(context,
	                			R.string.set_url_fill_in_box);
	                }
            	}
        	});
        // キャンセルの場合は何もしない
        adBuilder.setNegativeButton("Cancel", null);
        adBuilder.create().show();
    }
    
    /**サイト名、URLの削除を確認するダイアログ*/
    private void deleteSiteNameAndUrlDialog(
    								final SiteNameAndUrl siteNameAndUrl){
    	final Context context = this;
        AlertDialog.Builder adBuilder = new AlertDialog.Builder(context);
        //単語帳に応じたタイトルを設定
        adBuilder.setTitle(String.format(context.getString(
        							R.string.set_url_would_you_delete),
        					siteNameAndUrl.getSiteName()));
        adBuilder.setPositiveButton("yes",
    			new DialogInterface.OnClickListener() {
        			@Override
        			public void onClick(DialogInterface dialog, int which){
        				int urlID = siteNameAndUrl.getUrlID();
        				if(urlID == mSiteNameAndUrl.getUrlID()){
    					//登録しているURLを消そうとしているとき
    					//まず、安全なURL_IDに登録してから、要らないURL_IDを削除する
        					Utils.updateUrlID(mAppContext, mWordBook.getID(),
    							 		Utils.DEFAULT_URL_ID,
    							 		mSiteNameAndUrl.getColumnName());
        				}
    					Utils.deleteSiteNameAndUrl(mAppContext,
							 siteNameAndUrl.getUrlID());
        				onResume();
        			}
				});
        adBuilder.setNegativeButton("no", null);
        adBuilder.create().show();
    }
    
    ///////////////////////////////////////////////////////////////////
    
    private class ListGestureDetector extends SimpleOnGestureListener{

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2,
								float velocityX, float velocityY) {
			
			int pos = mListView.pointToPosition((int)e2.getX(), (int)e2.getY());
			SiteNameAndUrl siteNameAndUrl 
							= (SiteNameAndUrl)mListView.getItemAtPosition(pos);
			if(siteNameAndUrl==null) return false; //リスト以外の場所をフリック時
			if (Math.abs(e1.getY() - e2.getY()) 
					> WordBookListActivity.SWIPE_MAX_OFF_PATH)
				//DO NOTHING
                return false; 
            
			if(e1.getX() - e2.getX() 
					> WordBookListActivity.SWIPE_MIN_DISTANCE
            	&& Math.abs(velocityX) 
            		> WordBookListActivity.SWIPE_THRESHOLD_VELOCITY) { 
                //右から左へのフリック　編集
				enterSiteNameAndUrlDialog(siteNameAndUrl);
            	
            }else if(e2.getX() - e1.getX() 
            			> WordBookListActivity.SWIPE_MIN_DISTANCE 
            		&& Math.abs(velocityX) 
            			> WordBookListActivity.SWIPE_THRESHOLD_VELOCITY) { 
                //左から右へのフリック　finish
            	finish();
	        } 
            return false; 
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
            int pos = mListView.pointToPosition((int)e.getX(), (int)e.getY());
            // 押したとき
            SiteNameAndUrl siteNameAndUrl 
						= (SiteNameAndUrl)mListView.getItemAtPosition(pos);
            if(siteNameAndUrl!=null){
	            if(siteNameAndUrl.hasUrlID() && mSiteNameAndUrl.hasUrlID()){
		            //サイト名とURLを選択したから、それに更新する
	            	Utils.updateUrlID(mAppContext,	mWordBook.getID(),
								 	siteNameAndUrl.getUrlID(),
								 	mSiteNameAndUrl.getColumnName());
		       		//登録したサイトの色を変えたいから、urlIDを更新
		       		mSiteNameAndUrl.setUrlID(siteNameAndUrl.getUrlID());
		       		finish();
	       	 	}
            }
            return false;
		}	
    }

    ///////////////////////////////////////////////////////////////////
    
    private class PickUrlAdapter extends ArrayAdapter<SiteNameAndUrl> {
    	
    	private Context mContext = null;
    	private LayoutInflater mLayoutInflater = null;
    	private WordBook mWordBook = null;
    	private SiteNameAndUrl mSiteNameAndUrl = null;
    	/**登録してある三つのURLのID。文字色を変えるときの判定に用いる*/
    	int[] mRegisteredIDs = null;
        ViewHolder mViewHolder = null;
        
        //findViewの回数を少なくするためのViewHolder
        private class ViewHolder{
        	TextView site_name_tv;
        	TextView url_tv;
        	ImageView edit_imgv;
        	ImageView delete_imgv;
        }
        
        /**コンストラクタ
         * @param context You should pass getApplicationContext*/
        public PickUrlAdapter(Context context,
            				ArrayList<SiteNameAndUrl> dataArray,
            				WordBook wordBook, SiteNameAndUrl siteNameAndUrl){
        	
            super(context,R.layout.two_text_and_icons_inflater,dataArray);
            mContext = context;
            mLayoutInflater = LayoutInflater.from(context);
            mWordBook = wordBook;
            mSiteNameAndUrl = siteNameAndUrl;
            //すでに登録されている三つのURL_IDを取得
    		mRegisteredIDs = Utils.getRegisteredUrlIDs(
    									context, mWordBook.getID());
        }
        
    	@Override
    	public View getView(int position, View convertView, ViewGroup parent){
    		
    		final SiteNameAndUrl siteNameAndUrl = getItem(position);
    		
    	    if (convertView == null){//初めて表示される単語のとき
    	    	//two_text_and_icons_inflaterから1行分のレイアウトを生成
    	    	convertView = mLayoutInflater.inflate(
    	    				R.layout.two_text_and_icons_inflater, null);
    	    	mViewHolder = new ViewHolder();
     		    mViewHolder.site_name_tv = (TextView)convertView.findViewById(
     		    							R.id.twoTextAndIconInf_TV1);
    		    mViewHolder.url_tv = (TextView)convertView.findViewById(
    		    							R.id.twoTextAndIconInf_TV2);
    		    mViewHolder.edit_imgv = (ImageView)convertView.findViewById(
    		    							R.id.twoTextAndIconInf_EditImgV);
    		    mViewHolder.delete_imgv = (ImageView)convertView.findViewById(
    										R.id.twoTextAndIconInf_DelImgV);
    		    mViewHolder.edit_imgv.setImageResource(
    		    						android.R.drawable.ic_menu_edit);
    		    mViewHolder.delete_imgv.setImageResource(
										android.R.drawable.ic_menu_delete);
    		    //convertViewにViewに関するタグ（情報）を添付しておく
    		    convertView.setTag(mViewHolder);
    	    }else{	//すでに表示された単語のときタグの情報を読み取る
    	    	//お前が悪さをしている。
    	    	mViewHolder = (ViewHolder)convertView.getTag();
    	    }
    	    
    	    if(mViewHolder != null){
    	    	//表示データの設定
    			mViewHolder.site_name_tv.setText(siteNameAndUrl.getSiteName());
    		    mViewHolder.url_tv.setText(siteNameAndUrl.getUrl());
    	    	//表示されるサイトのurlIDが登録されているurlIDに一致するかを判定
    	    	int urlID = siteNameAndUrl.getUrlID();
    	    	//同じである場合は、そのテキストの色を変化させる。
    	    	if(urlID == mRegisteredIDs[0] || urlID == mRegisteredIDs[1]
    	    	                       || urlID == mRegisteredIDs[2]){
    	    		mViewHolder.site_name_tv.setTextColor(Color.BLUE);
    	    	} else {
    	    		mViewHolder.site_name_tv.setTextColor(Color.LTGRAY);
    	    	}
    	    	if(urlID == mSiteNameAndUrl.getUrlID()){
    	    		mViewHolder.site_name_tv.setTextColor(Color.CYAN);
    	    	}
    		    mViewHolder.edit_imgv.setOnClickListener(new OnClickListener(){
    				@Override
    				public void onClick(View v) {
    					enterSiteNameAndUrlDialog(siteNameAndUrl);
    				}
    			});
    		    mViewHolder.delete_imgv
    		    			.setOnClickListener(new OnClickListener() {
    				@Override
    				public void onClick(View v) {
    					int urlID = siteNameAndUrl.getUrlID();
    					if(urlID != 1 && urlID != 2 && urlID != 3){
    						//リストに表示されているサイト名、URLを削除
    						deleteSiteNameAndUrlDialog(siteNameAndUrl);
    					}else
    						Utils.showToast(mContext, 
    									R.string.set_url_you_cant_delete);
    					}
    			});
    	    }
    		return convertView;
    	}
    }
	
}
