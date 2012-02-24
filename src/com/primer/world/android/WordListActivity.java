package com.primer.world.android;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

/**単語帳に登録された単語リストを表示させる。 */
public class WordListActivity extends ListActivity {
	
	private Context mAppContext = null;
	private ListView mListView = null;
	private WordBook mWordBook = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_only_layout);
        
        mAppContext = getApplicationContext();
        mListView = getListView();
        if(mListView == null) finish();    
        
        // Intentの取得
        Intent intent = getIntent();	//　Intentから単語帳名を取得する        
        if(intent != null){				// intentの中身がある場合
    		mWordBook = (WordBook)intent.getSerializableExtra(
    									getPackageName() + ".wordBook");
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
        // 長押し認識
        mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> lv, View v,
												int position, long id) {
				Word w = (Word)mListView.getItemAtPosition(position);
				showMenu(w);
				return false;
			}
		});
        mListView.setFastScrollEnabled(true);// スクロールの高速化
        setTitle(mWordBook.getWordBookName());
    }
    
    //画面が戻ってきた時に、リストの更新を行う
    @Override
    protected void onResume() {
    	super.onResume();
    	ArrayList<Word> wordArray = Utils.getWords(mAppContext,
    												mWordBook.getID());
		WordAdapter wordAdapter = new WordAdapter(mAppContext, wordArray);
		//リストにwordAdapterを適用する
		getListView().setAdapter(wordAdapter);
    }

    
    ///////////////////////////////////////////////////////////////////
    
    //メニューの作成(文字と標準のアイコンを加える)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(0, Menu.FIRST, Menu.NONE, R.string.word_list_new)
    							.setIcon(android.R.drawable.ic_menu_add);
    	menu.add(0, Menu.FIRST + 1, Menu.NONE, R.string.word_list_record)
    							.setIcon(android.R.drawable.ic_menu_myplaces);
    	menu.add(0, Menu.FIRST + 2, Menu.NONE, R.string.word_list_del_record)
								.setIcon(R.drawable.ic_menu_del_record);
    	return super.onCreateOptionsMenu(menu);
    }
    
    //メニューのボタンが押された時の動作
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	switch(item.getItemId()){
    		case Menu.FIRST:	//new word(単語の新規作成)
    			Intent e_w_intent = new Intent(getApplicationContext(),
    					EditWordActivity.class);
    			e_w_intent.putExtra(getPackageName() + ".mode", Utils.NEW);
    			e_w_intent.putExtra(getPackageName() + ".wordBookID",
    								mWordBook.getID());
    	 		startActivityForResult(e_w_intent, 0);
    	 		break;
    	 		
    		case Menu.FIRST + 1:	//record
    	 		Intent t_r_intent = new Intent(getApplicationContext(),
    	 				TestRecordActivity.class);
    	 		t_r_intent.putExtra(getPackageName() + ".wordBook", mWordBook);
    	 		startActivityForResult(t_r_intent, 0);
    	 		break;
    	 	
    	 	case Menu.FIRST + 2:	//delete record
    	 		deleteTestRecordDialog(getApplicationContext(), mWordBook);
    	 		break;
    	}
    	return super.onOptionsItemSelected(item);
    }

    
    ///////////////////////////////////////////////////////////////////
    
    private class ListGestureDetector extends SimpleOnGestureListener{

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2,
								float velocityX, float velocityY) {
			
			int pos = mListView.pointToPosition((int)e2.getX(), (int)e2.getY());
			Word w = (Word)mListView.getItemAtPosition(pos);
			if(w==null) return false; //リスト以外の場所をフリックしたとき
			if (Math.abs(e1.getY() - e2.getY()) 
					> WordBookListActivity.SWIPE_MAX_OFF_PATH)
				//DO NOTHING
                return false; 
            
			if(e1.getX() - e2.getX() 
					> WordBookListActivity.SWIPE_MIN_DISTANCE
            	&& Math.abs(velocityX) 
            		> WordBookListActivity.SWIPE_THRESHOLD_VELOCITY) { 
                //右から左へのフリック　EditWordActivity
                if(w.hasID()){
    	           	 //TEditActivityを起動（mode 編集）
    	           	Intent intent = new Intent(
    	           			mAppContext, EditWordActivity.class);
    	           	intent.putExtra( 
    	           			mAppContext.getPackageName() + ".mode",Utils.EDIT);
            		intent.putExtra(mAppContext.getPackageName() + ".word", w);
            		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            		mAppContext.startActivity(intent);
           	 	}
            	
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
            // 押したときも　EditWordActivity
            Word w = (Word)mListView.getItemAtPosition(pos);
            if(w!=null) {
            	if(w.hasID()) showMenu(w);
            }
            return false;
		}	
    }

    
    ///////////////////////////////////////////////////////////////
    
    //メニューを表示する
    private void showMenu(final Word word) {
    	final Context context = this;
        AlertDialog.Builder adBuilder = new AlertDialog.Builder(context);
        CharSequence[] menu = {
      		  getString(R.string.word_list_menu_reset_wrong_count),
      		  getString(R.string.word_list_menu_del_word)};
        adBuilder.setTitle("Menu");
        adBuilder.setItems(menu, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch(which){						
					case 0:	//間違えた回数のリセット
						Utils.updateWrongCount(context, word.getID(), 0);
						break;
						
					case 1:	//単語の削除
						showWordDeleteDialog(context, word);
						break;
				}
			}
		});
        adBuilder.create().show();
    }
    
    /////////////////////////////////////////////////////////////////////
    //以下、ダイアログ
    
    /**レコードの削除を確認するダイアログ*/
    private void deleteTestRecordDialog(final Context context,
    													final WordBook wordBook){
    	AlertDialog.Builder adBuilder = new AlertDialog.Builder(this);
        adBuilder.setTitle(
        	String.format(getString(R.string.word_book_list_del_record_dialog),
        					wordBook.getWordBookName()));
        adBuilder.setPositiveButton("yes",
    			new DialogInterface.OnClickListener() {
        			@Override
        			public void onClick( DialogInterface dialog, int which){
        				Utils.deleteRecord(context, wordBook.getID());
        				onResume();
        			}
				});
        adBuilder.setNegativeButton("no", null);
        adBuilder.create().show();
    }
    
    /**単語の削除を確認するダイアログ*/
    private void showWordDeleteDialog(final Context context,
    									final Word word){
        AlertDialog.Builder adBuilder = new AlertDialog.Builder(this);
        adBuilder.setTitle(String.format(
       			getString(R.string.word_list_menu_del_dialog), 
       				word.getQuestion()));
        adBuilder.setPositiveButton("yes",
    			new DialogInterface.OnClickListener() {
        			@Override
        			public void onClick( DialogInterface dialog, int which){
        				Utils.deleteWord(context, word.getID());
        				onResume();
        			}
				});
        adBuilder.setNegativeButton("no", null);
        adBuilder.create().show();
    }
}
