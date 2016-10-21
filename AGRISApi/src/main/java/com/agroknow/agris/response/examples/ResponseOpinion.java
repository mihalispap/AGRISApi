package com.agroknow.agris.response.examples;

import java.util.Date;

public class ResponseOpinion {

	/*
	 	{
	 		"tid": 706879573380747300,
			"user_group": "123",
			"created_at": "2016-03-17",
			"absa": [
			
			]
	 * */
	
	public long tid;
	public String user_group;
	public Date created_at;
	
	public ResponseABSAObject absa[];
	public ResponseTweet tweets[];
	
	public ResponseOpinion() {
		// TODO Auto-generated constructor stub
	}

}
