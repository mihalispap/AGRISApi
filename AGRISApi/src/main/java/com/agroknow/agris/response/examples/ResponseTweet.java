package com.agroknow.agris.response.examples;

public class ResponseTweet {

	/*
	 	{
			"text": "RT @EGrettou: Όχι μωρό μου δεν θέλω σοκολάτα με στέβια,αρκετά προβλήματα έχω..., ζήτω η ελληνική στέβια είναι γαμάτη",
			"id": 706879573380747300,
			"user_id": 310241015,
			"retweet_count": 56,
			"geo": null,
			"lang": "el"
		}
	 * */
	
	public String text;
	public long id;
	public String user_id;
	public int retweet_count;
	public Object geo;
	public String lang;
	
	public ResponseTweet() 
	{
		// TODO Auto-generated constructor stub
	}

}
