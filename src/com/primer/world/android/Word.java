package com.primer.world.android;

import java.io.Serializable;

/**単語のクラス。*/
public class Word implements Serializable{
	
	private static final long serialVersionUID = 1L;	//シリアルを設定
	private int mWordID = -1;
	private int mWordBookID = -1;
	private String mQuestion = "EMPTY";
	private String mAnswer = "EMPTY";
	private String mExplanation = "EMPTY";
	private int mTestCount = 0;
	private int mWrongCount = 0;
	/**大雑把な正答率で、25,50,75,100しかない。<br>
	 * 初期値を25にしているのは、正答率が0%のとき25%以下にくくれるから。*/
	private int mAccuracyRate = 25;

	public int getID() {
		return mWordID;
	}
	public void setID(int wordID) {
		mWordID = wordID;
	}

	public int getWordBookID() {
		return mWordBookID;
	}
	public void setWordBookID(int wordBookID) {
		mWordBookID = wordBookID;
	}

	public String getQuestion() {
		return mQuestion;
	}
	public void setQuestion(String question) {
		mQuestion = question;
	}

	public String getAnswer() {
		return mAnswer;
	}
	public void setAnswer(String answer) {
		mAnswer = answer;
	}

	public String getExplanation() {
		return mExplanation;
	}
	public void setExplanation(String explanation) {
		mExplanation = explanation;
	}

	public int getTestCount() {
		return mTestCount;
	}
	public void setTestCount(int testCount) {
		mTestCount = testCount;
	}

	public int getWrongCount() {
		return mWrongCount;
	}
	public void setWrongCount(int wrongCount) {
		mWrongCount = wrongCount;
	}

	public int getAccuracyRate() {
		return mAccuracyRate;
	}
	public void setAccuracyRate(int accuracyRate) {
		mAccuracyRate = accuracyRate;
	}

	//wordIDは正(-1は駄目)ならオッケー
	public Boolean hasID(){
		return mWordID > 0;
	}
}
