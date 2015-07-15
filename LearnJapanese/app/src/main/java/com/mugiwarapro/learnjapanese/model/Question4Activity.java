package com.mugiwarapro.learnjapanese.model;


import com.mugiwarapro.learnjapanese.R;

public class Question4Activity extends QuestionN {

	@Override
	protected int getChoicesNum() {
		return 4;
	}
	@Override
	protected void setContentView() {
		setContentView(R.layout.activity_question4);
	}

}
