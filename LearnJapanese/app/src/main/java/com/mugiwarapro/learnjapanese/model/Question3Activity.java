package com.mugiwarapro.learnjapanese.model;


import com.mugiwarapro.learnjapanese.R;

public class Question3Activity extends QuestionN {

	@Override
	protected int getChoicesNum() {
		return 3;
	}
	@Override
	protected void setContentView() {
		setContentView(R.layout.activity_question3);
	}

}
