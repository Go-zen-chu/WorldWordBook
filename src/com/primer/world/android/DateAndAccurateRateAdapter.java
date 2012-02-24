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

import com.primer.world.android.TestRecordActivity.DateAndAverageAccuracyRate;

/**
 * 日付と平均正答率を表示する際に用いるアダプター<br>
 * TestRecordActivtyのレコードを表示するlistViewで用いられる
 */
public class DateAndAccurateRateAdapter extends
		ArrayAdapter<DateAndAverageAccuracyRate> {

	private LayoutInflater mLayoutInflater = null;
	private DateFormat mDateFormat = null;
	private DateFormat mTimeFormat = null;
	private ViewHolder mHolder = null;

	// findViewの回数を少なくするためのViewHolder
	private class ViewHolder {
		TextView date_tv;
		TextView ave_accuracy_rate_tv;
	}

	/** コンストラクタ */
	public DateAndAccurateRateAdapter(Context context,
			ArrayList<DateAndAverageAccuracyRate> dataArray) {
		super(context, R.layout.two_horizontal_text_inflater, dataArray);
		mLayoutInflater = LayoutInflater.from(context);
		mDateFormat = android.text.format.DateFormat.getDateFormat(context);
		mTimeFormat = android.text.format.DateFormat.getTimeFormat(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// positionにあるデータを取得
		DateAndAverageAccuracyRate data = getItem(position);

		if (convertView == null) {// 初めて表示される単語のとき
			// two_horizontal_text_inflaterから一行のレイアウトを取得
			convertView = mLayoutInflater.inflate(
					R.layout.two_horizontal_text_inflater, null);
			mHolder = new ViewHolder();
			// 各idの項目に値をセット
			mHolder.date_tv = (TextView) convertView
					.findViewById(R.id.twoHorizontalTextInf_TV1);
			mHolder.ave_accuracy_rate_tv = (TextView) convertView
					.findViewById(R.id.twoHorizontalTextInf_TV2);
			// convertViewにViewに関するタグ（情報）を添付しておく
			convertView.setTag(mHolder);
		} else { // すでに表示された単語のときタグの情報を読み取る
			mHolder = (ViewHolder) convertView.getTag();
		}
		if (mHolder != null) {
			// 取得したlong型の日付情報をDate型に変換
			Date d = new Date(data.getDate());
			// 表示データの設定
			mHolder.date_tv.setText(mDateFormat.format(d) + " "
					+ mTimeFormat.format(d));
			mHolder.ave_accuracy_rate_tv.setText(data.getAverageAccuracyRate());
		}
		return convertView;
	}
}
