package com.primer.world.android;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * SDカード内のcsvファイルをリスト表示させる際に用いられるアダプター<br>
 * ImportActivityに用いられる。
 */
public class FileAdapter extends ArrayAdapter<File> {

	private LayoutInflater mLayoutInflater = null;
	private DateFormat mDateFormat = null;
	private ViewHolder mViewHolder = null;

	// findViewの回数を少なくするためのViewHolder
	private class ViewHolder {
		TextView fileName_tv;
		TextView date_tv;
		TextView file_size_tv;
	}

	/** コンストラクタ */
	public FileAdapter(Context context, ArrayList<File> fileArray) {
		super(context, R.layout.three_text_inflater_1, fileArray);
		mLayoutInflater = LayoutInflater.from(context);
		mDateFormat = android.text.format.DateFormat.getDateFormat(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		File file = getItem(position);// position行のfileを取得

		if (convertView == null) {// 初めて表示される単語のとき
			// file_inflater.xmlから1行分のレイアウトを生成
			convertView = mLayoutInflater.inflate(R.layout.three_text_inflater_1,
					null);
			mViewHolder = new ViewHolder();
			// 各idの項目に値をセット
			mViewHolder.fileName_tv = (TextView) convertView
					.findViewById(R.id.threeTextInf_1_TV1);
			mViewHolder.date_tv = (TextView) convertView
					.findViewById(R.id.threeTextInf_1_TV2);
			mViewHolder.file_size_tv = (TextView) convertView
					.findViewById(R.id.threeTextInf_1_TV3);
			// convertViewにViewに関するタグ（情報）を添付しておく
			convertView.setTag(mViewHolder);
		} else { // すでに表示された単語のときタグの情報を読み取る
			mViewHolder = (ViewHolder) convertView.getTag();
		}

		if (mViewHolder != null) {
			// ファイルの変更された日付をDate型に変換
			Date date = new Date(file.lastModified());
			// 表示データの設定（タッチしたfileに関する情報はviewHolderに格納されている）
			mViewHolder.fileName_tv.setText(file.getName());
			mViewHolder.date_tv.setText("date : " + mDateFormat.format(date));
			mViewHolder.file_size_tv.setText("size : " + returnFileSize(file));
		}
		return convertView;
	}

	/** ファイルサイズを表示するメソッド */
	private String returnFileSize(File file) {
		long fileSize = file.length();
		if (0 <= fileSize && fileSize < 1024) {
			return String.valueOf(fileSize) + "B";
		} else if (1024 <= fileSize && fileSize <= 1048576) {
			return String.valueOf(fileSize / 1024) + "KB";
		} else {
			return String.valueOf(fileSize / 1048576) + "MB";
		}
	}
}
