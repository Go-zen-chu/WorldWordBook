package com.primer.world.android;

import java.io.Serializable;

/**単語帳のクラス。*/
public class WordBook implements Serializable{
	
	private static final long serialVersionUID = 1L;	//シリアルを設定
	private int mWordBookID = -1;
	private String mWordBookName = "EMPTY";
	private long mTestedDate = System.currentTimeMillis();
	private int mTestMode = TestActivity.NORMAL;
	private int mBookMark = 0;

	public int getID() {
		return mWordBookID;
	}
	public void setID(int wordBookID) {
		mWordBookID = wordBookID;
	}

	public String getWordBookName() {
		return mWordBookName;
	}
	public void setWordBookName(String wordBookName) {
		mWordBookName = wordBookName;
	}

	public long getTestedDate() {
		return mTestedDate;
	}
	public void setTestedDate(long testedDate) {
		mTestedDate = testedDate;
	}

	public int getTestMode() {
		return mTestMode;
	}
	public void setTestMode(int testMode) {
		mTestMode = testMode;
	}

	public int getBookMark() {
		return mBookMark;
	}
	public void setBookMark(int bookMark) {
		mBookMark = bookMark;
	}

	//WordBookIDが正ならオッケー（-1は駄目）
	public Boolean hasID(){
		return mWordBookID > 0;
	}
}

