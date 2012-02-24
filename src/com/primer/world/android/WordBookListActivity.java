package com.primer.world.android;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;

/**単語帳表示はこのActivityで行う<br>メインアクティビティ*/
public class WordBookListActivity extends ListActivity{

	public static int SWIPE_MIN_DISTANCE = 0; 
	public static int SWIPE_MAX_OFF_PATH = 0;
	public static int SWIPE_THRESHOLD_VELOCITY = 0;
	private Context mAppContext = null;
	private ListView mListView = null;
	
	@Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(!Utils.isSDcardMounted()){
        	// If the SD card isn't mounted properly you can't use
        	// this application.
        	Utils.showToast(getApplicationContext(),
        					R.string.word_book_list_mount_sd);
        	finish();
        }
        setContentView(R.layout.list_only_layout);	//レイアウトを設定
        //ディスプレイ情報を取得。フリック実装への参考値にする
     	DisplayMetrics dm = getResources().getDisplayMetrics();
        SWIPE_MIN_DISTANCE
        		= (int)(70.0f * dm.densityDpi / 160.0f + 0.5); 
        SWIPE_MAX_OFF_PATH
        		= (int)(250.0f * dm.densityDpi / 160.0f + 0.5);
        SWIPE_THRESHOLD_VELOCITY 
        		= (int)(200.0f * dm.densityDpi / 160.0f + 0.5);
     	mAppContext = getApplicationContext();
     	
     	mListView = getListView();	// これでは更新反映されない？
     	if(mListView == null) finish();
     	
     	// リストでのジェスチャー実装
        final GestureDetector gestureDetector 
        		= new GestureDetector(new ListGestureDetector());
        OnTouchListener gestureListener = new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event); 
            }
        };
        mListView.setOnTouchListener(gestureListener);
        // 長押し認識
        mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> lv, View v,
												int position, long id) {
				//長押ししたWordBookを取得
				WordBook wb = (WordBook)mListView.getItemAtPosition(position);
				//コンテキストメニュー
				if(wb.hasID()) showMenu(wb);
				return false;
			}
		});
        // スクロールの高速化
        mListView.setFastScrollEnabled(true);
        setTitle(getString(R.string.word_book_list_title));
    }
    
	
    //画面が戻ってきた時に、リストの更新を行う
    @Override
    public void onResume() {
    	super.onResume();
    	//リスト表示させるアダプターを作成
    	ArrayList<WordBook> wordBookArray = Utils.getWordBooks(mAppContext);
 		WordBookAdapter wordBookAdapter 
 				= new WordBookAdapter(mAppContext, wordBookArray);
 		//リストにwordBookAdapterを適用する
 		mListView.setAdapter(wordBookAdapter);
    }

    ///////////////////////////////////////////////////////////////////////
    //Option Menu
    
    //メニューの作成(文字と標準のアイコンを加える)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(0, Menu.FIRST, Menu.NONE,
    			getString(R.string.word_book_list_new))
    				.setIcon(android.R.drawable.ic_menu_add);
    	menu.add(0, Menu.FIRST + 1, Menu.NONE,
    			getString(R.string.word_book_list_import_sd))
    				.setIcon(R.drawable.ic_menu_import);
    	menu.add(0, Menu.FIRST + 2, Menu.NONE,
    			getString(R.string.word_book_list_import_dbox))
					.setIcon(R.drawable.dropbox);
    	menu.add(0, Menu.FIRST + 3, Menu.NONE,
    			getString(R.string.word_book_list_help))
					.setIcon(android.R.drawable.ic_menu_help);
    	return super.onCreateOptionsMenu(menu);
    }
    
    
    //メニューのボタンが押された時の動作
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()){
    		case Menu.FIRST:	//単語帳の新規作成
    			makeOrEditWordBookDialog(this, null);//単語作成ダイアログ
    	 		break;
    	 	
    	 	case Menu.FIRST + 1:	//SDからインポート
    			Intent sd_intent 
    					= new Intent(this, ImportFromSdCardActivity.class);
    			startActivity(sd_intent);
    	 		break;
    	 		
    	 	case Menu.FIRST + 2:	//ドロップボックスからインポート
    			Intent dbox_intent 
    					= new Intent(this, ManageDropboxActivity.class);
    			dbox_intent.putExtra(getPackageName() + ".mode", Utils.IMPORT);
    			startActivity(dbox_intent);
    	 		break;

    	 	case Menu.FIRST + 3:	//help
    	 		showHelpMenu();
    	 		break;
    	 	
    	}
    	return super.onOptionsItemSelected(item);
    }
    
    ///////////////////////////////////////////////////////////////////////
    //Menu
    
    //メニューを表示する
    private void showMenu(final WordBook wordBook){
    	final Context context = this;
        AlertDialog.Builder adBuilder = new AlertDialog.Builder(context);
        CharSequence[] menu = {
      		getString(R.string.word_book_list_menu_change_name),
      		getString(R.string.word_book_list_menu_pick_url),
      		getString(R.string.word_book_list_menu_export_to_sd),
        		getString(R.string.word_book_list_menu_export_to_dbox),
        		getString(R.string.word_book_list_menu_del_book)
        };
        adBuilder.setTitle("Menu");
        adBuilder.setItems(menu, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch(which){
					case 0:	//単語帳の名前変更
						makeOrEditWordBookDialog(context, wordBook);
						break;
						
					case 1:	//検索用URLの設定
						Intent list_url_intent = new Intent(
								context, ListingRegisteredUrlActivity.class);
						list_url_intent.putExtra(getPackageName() + ".wordBook",
												wordBook);
						startActivity(list_url_intent);
						break;
						
					case 2:	//SDカードにcsvでエクスポート
						Utils.exportWordBookToSD(getApplicationContext(),
												wordBook);
						break;
						
					case 3:	//dropboxにcsvでエクスポート
						Intent dropbox_intent = new Intent(
								context, ManageDropboxActivity.class);
						dropbox_intent.putExtra(getPackageName() + ".mode",
												Utils.EXPORT);
						dropbox_intent.putExtra(getPackageName() + ".wordBook",
												wordBook);
						startActivity(dropbox_intent);
						break;
						
					case 4:	//単語帳の削除
						deleteWordBookDialog(context, wordBook);
						break;
				}
			}
		});
        adBuilder.create().show();
    }
    
    // Help menuを表示する
    private void showHelpMenu(){
     	final Context context = this;
         AlertDialog.Builder adBuilder = new AlertDialog.Builder(context);
         // 1:how to use 2:about this app
         CharSequence[] menu = {
         		getString(R.string.word_book_list_help_1),
         		getString(R.string.word_book_list_help_2)
         		};
         adBuilder.setTitle("Help Menu");
         // 項目を押すとヘルプの内容が表示される仕組み
         adBuilder.setItems(menu, new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				switch(which){
 					case 0:
 						showDetailedHelpDialog(getApplicationContext(),
 								getString(R.string.word_book_list_detailed_help_1));
 						break;
 					case 1:
 						showDetailedHelpDialog(getApplicationContext(),
 								getString(R.string.word_book_list_detailed_help_2));
 						break;
 				}
 			}
 		});
         adBuilder.create().show();
     }
    
    ///////////////////////////////////////////////////////////////////////
    //  Dialogs
    
    /**Dialog for making new WordBook or editing it's name.
     * @param context
     * @param wordBook WordBook which you want to edit it's name 
     * (If you want to make new WordBook, then put null)*/
    private void makeOrEditWordBookDialog(final Context context,
    													final WordBook wordBook){
    	AlertDialog.Builder adBuilder = new AlertDialog.Builder(context);
        LayoutInflater factory = LayoutInflater.from(context);
        final View entryView = factory.inflate(
        						R.layout.enter_word_book_name_dialog, null);
        adBuilder.setTitle(getString(R.string.word_book_list_dialog));
        adBuilder.setView(entryView);
        final EditText et = (EditText)entryView.findViewById(
        								R.id.enterWordBookNameDlg_ET);
        //単語帳名を表示する
        if(wordBook != null){
        	et.setText(wordBook.getWordBookName());
	        //カーソルの位置を最後にする
	        et.setSelection(wordBook.getWordBookName().length());
        }
        adBuilder.setPositiveButton("OK",
        	new DialogInterface.OnClickListener(){
            	@Override
				public void onClick( DialogInterface dialog, int whichButton){
	                String wordBookName = et.getText().toString();
	                if(!wordBookName.equals("")){	//未入力をはじく
	                	//単語帳名の更新
	                	if(wordBook != null){
	                		Utils.updateWordBookName(context, wordBookName,
	                								wordBook.getID());
	                	}else{
	                		Utils.createWordBook(context, wordBookName);
	                	}
	                	onResume();
	                }
            	}
        	});
        adBuilder.setNegativeButton("Cancel", null);
        adBuilder.create().show();
    }
    
    /**単語帳の削除を確認するダイアログ*/
    private void deleteWordBookDialog(final Context context,
   		 										final WordBook wordBook){
   	AlertDialog.Builder adBuilder = new AlertDialog.Builder(this);
		//単語帳に応じたタイトルを設定
		adBuilder.setTitle(
		  	String.format(
		  		getString(R.string.word_book_list_del_word_book_dialog),
		  					wordBook.getWordBookName()));
		adBuilder.setPositiveButton("yes",
				new DialogInterface.OnClickListener() {
		  			@Override
		  			public void onClick( DialogInterface dialog, int which){
		  				Utils.deleteWordBook(context, wordBook.getID());
		  				onResume();
		  			}
		});
		adBuilder.setNegativeButton("no", null);
		adBuilder.create().show();
    }
    
    /**選択したヘルプについての詳細表示するダイアログ*/
    private void showDetailedHelpDialog(final Context context,
														final String detailedHelp) {
   	 AlertDialog.Builder adBuilder = new AlertDialog.Builder(this);
   	 adBuilder.setMessage(detailedHelp);
       adBuilder.setPositiveButton("OK", null);
       adBuilder.create().show();
    }

    //////////////////////////////////////////////////////////////////
    
    /**リストにおいてフリック処理を行うことが出来るGestureListener*/
    private class ListGestureDetector extends SimpleOnGestureListener{

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2,
								float velocityX, float velocityY) {
			
			int pos = mListView.pointToPosition((int)e2.getX(), (int)e2.getY());
			WordBook wb = (WordBook)mListView.getItemAtPosition(pos);
			if(wb==null) return false;
			if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
				//DO NOTHING
                return false;
			}
            
			if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
            	&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) { 
                //右から左へのフリック　WordListActivity
            	if(wb.hasID()){
	            	Intent i = new Intent(mAppContext, WordListActivity.class);
		         	i.putExtra(mAppContext.getPackageName() + ".wordBook", wb);
		         	i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		         	mAppContext.startActivity(i);
	            }
            	
            }else if(e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE 
            			&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) { 
                //左から右へのフリック　TestActivity
            	if(wb.hasID()){
	            	Intent i = new Intent(mAppContext, TestActivity.class);
		        	//intentにwordBookを添付
		         	i.putExtra(mAppContext.getPackageName() + ".wordBook", wb);
		         	i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		         	mAppContext.startActivity(i);
            	}
	        } 
            return false; 
		}
		
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
            int pos = mListView.pointToPosition((int)e.getX(), (int)e.getY());
            // 押したときも　メニューを表示
            WordBook wb = (WordBook)mListView.getItemAtPosition(pos);
        	if(wb != null){
        		if(wb.hasID()) showMenu(wb);
        	}
            return false;
		}	
    }
}

