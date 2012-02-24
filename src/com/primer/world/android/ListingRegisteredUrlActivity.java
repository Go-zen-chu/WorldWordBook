package com.primer.world.android;

import java.io.Serializable;
import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/** その単語帳で使うweb辞書を設定する */
public class ListingRegisteredUrlActivity extends ListActivity {

	private Context mAppContext = null;
	private ListView mLV = null;
	private WordBook mWordBook = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_only_layout);

		mAppContext = getApplicationContext();
		mLV = getListView();
		if (mLV == null)
			finish();

		Intent intent = getIntent(); // 　Intentから単語帳名を取得する
		if (intent != null) { // intentの中身がある場合
			mWordBook = (WordBook) intent.getSerializableExtra(getPackageName()
																				+ ".wordBook");
			setTitle(String.format(getString(R.string.set_url_title),
					mWordBook.getWordBookName()));
		}

		// リストでのジェスチャー実装
		final GestureDetector gestureDetector = new GestureDetector(
				new ListGestureDetector());
		OnTouchListener gestureListener = new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}
		};
		mLV.setOnTouchListener(gestureListener);
	}

	// 画面が戻ってきた時に、リストの更新を行う
	@Override
	public void onResume() {
		super.onResume();
		if (mWordBook.getID() > 0) {
			// 選択した単語帳に登録されている調べたいサイト名やURLを取得
			ArrayList<SiteNameAndUrl> dataArray = Utils
					.getRegisteredSiteNameAndUrls(mAppContext, mWordBook.getID());
			RegisteredSiteAdapter adapter = new RegisteredSiteAdapter(mAppContext,
																						dataArray);
			// リスト表示させる
			mLV.setAdapter(adapter);
		}
	}

	// /////////////////////////////////////////////////////////////////
	private class ListGestureDetector extends SimpleOnGestureListener {

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			int pos = mLV.pointToPosition((int) e2.getX(), (int) e2.getY());
			SiteNameAndUrl siteNameAndUrl = (SiteNameAndUrl) mLV
					.getItemAtPosition(pos);
			if (siteNameAndUrl == null)
				return false;
			if (Math.abs(e1.getY() - e2.getY()) 
					> WordBookListActivity.SWIPE_MAX_OFF_PATH)
				// DO NOTHING
				return false;

			if (e1.getX() - e2.getX() > WordBookListActivity.SWIPE_MIN_DISTANCE
					&& Math.abs(velocityX) 
							> WordBookListActivity.SWIPE_THRESHOLD_VELOCITY) {
				// 右から左へのフリック　PickUrlActivity
				Intent intent = new Intent(mAppContext, SetUrlActivity.class);
				intent.putExtra(getPackageName() + ".wordBook", mWordBook);
				intent.putExtra(getPackageName() + ".siteNameAndUrl",
						siteNameAndUrl);
				startActivity(intent);

			} else if (
				e2.getX() - e1.getX() > WordBookListActivity.SWIPE_MIN_DISTANCE
					&& Math.abs(velocityX) 
						> WordBookListActivity.SWIPE_THRESHOLD_VELOCITY) {
				// 左から右へのフリック　finish
				finish();
			}
			return false;
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			int pos = mLV.pointToPosition((int) e.getX(), (int) e.getY());
			// 押したときも　PickUrlActivity
			SiteNameAndUrl siteNameAndUrl = (SiteNameAndUrl) mLV
					.getItemAtPosition(pos);
			if (siteNameAndUrl != null) {
				Intent intent = new Intent(mAppContext, SetUrlActivity.class);
				intent.putExtra(getPackageName() + ".wordBook", mWordBook);
				intent.putExtra(getPackageName() + ".siteNameAndUrl",
						siteNameAndUrl);
				startActivity(intent);
			}
			return false;
		}
	}

	// //////////////////////////////////////////////////////////////

	/** 登録した三つのサイトとそのURLを表示させるアダプター */
	private class RegisteredSiteAdapter extends ArrayAdapter<SiteNameAndUrl> {

		private LayoutInflater mLayoutInflater = null;
		private ViewHolder mViewHolder = null;

		// findViewの回数を少なくするためのViewHolder
		private class ViewHolder {
			TextView order_tv;// 上から何番目に表示されるかを表す
			TextView site_name_tv;
			TextView url_tv;
		}

		/** コンストラクタ */
		public RegisteredSiteAdapter(Context context,
				ArrayList<SiteNameAndUrl> dataArray) {
			super(context, R.layout.three_text_inflater_2, dataArray);
			mLayoutInflater = LayoutInflater.from(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			SiteNameAndUrl siteNameAndUrl = getItem(position);

			if (convertView == null) {// 初めて表示される単語のとき
				convertView = mLayoutInflater.inflate(
						R.layout.three_text_inflater_2, null);
				mViewHolder = new ViewHolder();
				// 各idの項目に値をセット
				mViewHolder.order_tv = (TextView) convertView
						.findViewById(R.id.threeTextInf_2_TV1);
				mViewHolder.site_name_tv = (TextView) convertView
						.findViewById(R.id.threeTextInf_2_TV2);
				mViewHolder.url_tv = (TextView) convertView
						.findViewById(R.id.threeTextInf_2_TV3);
				// convertViewにViewに関するタグ（情報）を添付しておく
				convertView.setTag(mViewHolder);
			} else { // すでに表示された単語のときタグの情報を読み取る
				mViewHolder = (ViewHolder) convertView.getTag();
			}

			if (mViewHolder != null) {
				// 表示データの設定（タッチした部分に関する情報はviewHolderに格納されている）
				// 順番を記すためにpositionを用いる
				mViewHolder.order_tv.setText(Integer.toString(position + 1));
				mViewHolder.site_name_tv.setText(siteNameAndUrl.getSiteName());
				mViewHolder.url_tv.setText(siteNameAndUrl.getUrl());
			}
			return convertView;
		}
	}

	// //////////////////////////////////////////////////////////////////

	/** URLのIDと登録したURLとその名称、登録されているカラム名を格納するクラス */
	public static class SiteNameAndUrl implements Serializable {

		private static final long serialVersionUID = 1L;
		private int mUrlID = -1;
		private String mSiteName = "EMPTY";
		private String mUrl = "EMPTY";// 直接webに繋げるわけではないからString型
		/** WORD_BOOKのカラムURL_ID_1, URL_ID_2, URL_ID_3のどれに含まれているかを格納 */
		private String mColumnName = "EMPTY";

		public int getUrlID() {
			return mUrlID;
		}

		public void setUrlID(int urlID) {
			mUrlID = urlID;
		}

		public String getSiteName() {
			return mSiteName;
		}

		public void setSiteName(String siteName) {
			mSiteName = siteName;
		}

		public String getUrl() {
			return mUrl;
		}

		public void setUrl(String url) {
			mUrl = url;
		}

		public void setColumnName(String columnName) {
			mColumnName = columnName;
		}

		public String getColumnName() {
			return mColumnName;
		}

		public boolean hasUrlID() {
			return mUrlID > 0;
		}
	}

}
