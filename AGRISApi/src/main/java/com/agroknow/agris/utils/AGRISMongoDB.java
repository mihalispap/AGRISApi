package com.agroknow.agris.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;

public class AGRISMongoDB {

	//public static String server="83.212.100.195";
	public static String server="localhost";
	
	private MongoClient connect()
	{
		MongoClient mongo = new MongoClient( server , 27017 );
		return mongo;
	}

	public boolean isValidApiKey(String apikey)
	{
		/*TODO:
		 * 	authenticate*/
		//MongoClient mongo = this.connect();
		
		MongoClient mongo = new MongoClient( server , 27017 );
		
		DB db = mongo.getDB("agris");
		DBCollection table = db.getCollection("users");

		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("apikey", apikey);

		DBCursor cursor = table.find(searchQuery);

		int no_results=cursor.count();
		mongo.close();
		
		if(no_results!=1)
			return false;
		return true;
	}

	public String fetchTweet(String id)
	{
		MongoClient mongo = new MongoClient( server , 27017 );
		
		DB db = mongo.getDB("agris");
		DBCollection table = db.getCollection("tweets");

		BasicDBObject searchQuery = new BasicDBObject();
		
		searchQuery.put("id", Long.parseLong(id));
		
		//System.out.println(searchQuery.toString());
		
		BasicDBObject proj = new BasicDBObject();
		proj.put("_id",0);
		proj.put("text",1);
		proj.put("id",1);
		proj.put("retweet_count",1);
		proj.put("geo",1);
		proj.put("lang",1);
		proj.put("user_id",1);
		//proj.put("created_at", 1);		
		
		DBCursor cursor = table.find(searchQuery,proj);

		DBObject response_object=cursor.next();
		String return_value=response_object.toString();
		//System.out.println(cursor.count()+return_value);
		
		int no_results=cursor.count();
		mongo.close();
		
		if(no_results!=0)
			return return_value;
		
		return "";
	}

	public String checkCache(HttpServletRequest request) throws Exception
	{
		/*TODO:
		 * 	authenticate*/
		//MongoClient mongo = this.connect();
		
		MongoClient mongo = new MongoClient( server , 27017 );
		
		DB db = mongo.getDB("agris");
		DBCollection table = db.getCollection("results");

		String input=request.getRequestURI()+"?"+request.getQueryString();
		
		input=input.replace("&cache=true", "");
		input=input.replace("&cache=false", "");
		
		String md5="";
		try
		{
			md5=getMD5(input);
		}
		catch(Exception e)
		{
			md5= String.valueOf(input.hashCode());
		}
		
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("queryMD5", md5);
		
		DBCursor cursor = table.find(searchQuery).sort(new BasicDBObject("timestamp",-1)).limit(1);
		
		int no_results=cursor.count();
		
		//cursor;
		//cursor.limit(1);
		
		//System.out.println(cursor.next().get("timestamp"));
		
		DBObject response_object=cursor.next();
		
		long timestamp = Long.valueOf(response_object.get("timestamp").toString());
		
		//System.out.println("LONGTIMESTAMP:"+timestamp);
		
		mongo.close();
		
		GetConfig config=new GetConfig();
		long clifetime;
		try
		{
			clifetime=Long.valueOf(config.getValue("cache_lifetime"));
		}
		catch(Exception e)
		{
			clifetime=1296000;
		}
			System.out.println("Going to compare:"+
					(System.currentTimeMillis()/1000 - timestamp)+" with:"
					+clifetime);
			
		long time_elapsed=System.currentTimeMillis()/1000 - timestamp;
		if(time_elapsed<clifetime)
			return response_object.get("results").toString();
			
		/*}
		catch(Exception e)
		{
			return "";
		}*/
		
		
		return "";
	}

	public String getFacetQuery(String fid) throws Exception
	{
		/*TODO:
		 * 	authenticate*/
		//MongoClient mongo = this.connect();
		
		MongoClient mongo = new MongoClient( server , 27017 );
		
		DB db = mongo.getDB("agris");
		DBCollection table = db.getCollection("facets_map");

		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("hash", fid.replace("___", "\n").replace("$$$", "\r"));
		
		DBCursor cursor = table.find(searchQuery);

		int no_results=cursor.count();
		cursor.limit(1);
		
		DBObject response_object=cursor.next();
		
		if(no_results!=0)
			return response_object.get("query").toString();
		
		return "";
	}

	public void cacheResponse(HttpServletRequest request, String result)
	{
		//MongoClient mongo = this.connect();
		
		MongoClient mongo = new MongoClient( server , 27017 );
		
		DB db = mongo.getDB("agris");
		DBCollection table = db.getCollection("results");
		
		String input=request.getRequestURI()+"?"+request.getQueryString();

		input=input.replace("&cache=true", "");
		input=input.replace("&cache=false", "");
		
		String md5="";
		try
		{
			md5=getMD5(input);
		}
		catch(Exception e)
		{
			md5= String.valueOf(input.hashCode());
		}
		
		BasicDBObject document = new BasicDBObject();
		document.put("queryMD5", md5);
		document.put("results",result);
		document.put("timestamp",System.currentTimeMillis()/1000);
		table.insert(document);
		
		mongo.close();
	}
	
	public void addPoints(String apikey, String operation, HttpServletRequest request)
	{
		/*TODO:
		 * 	should have points on operations!*/
		//MongoClient mongo = this.connect();
		/*
		MongoClientOptions options = MongoClientOptions.builder()
										.socketTimeout(15000)
										.connectTimeout(60000)
										.build();
		*/
		MongoClient mongo = new MongoClient( server , 27017);
				
		DB db = mongo.getDB("agris");
		DBCollection table = db.getCollection("calls");
		
		BasicDBObject document = new BasicDBObject();
		document.put("apikey", apikey);
		document.put("timestamp", System.currentTimeMillis()/1000);
		document.put("operation", operation);
		document.put("requestURL", request.getRequestURI()+"?"+request.getQueryString());
		document.put("IP", request.getRemoteAddr());
		table.insert(document);
		
		try
		{
			table=db.getCollection("facets_map");
			document=new BasicDBObject();
			String input=request.getRequestURI()+"?"+request.getQueryString();
			AESencr aes = new AESencr();
			
			document.put("hash", aes.encrypt(input));
			document.put("query", input);
			table.insert(document);
		}
		catch(Exception e)
		{}
		
		//System.out.println("input:"+input+" MD5:"+aes.encrypt(input)+" reverseMD5:"+aes.decrypt(aes.encrypt(input)));
		
		
		
		mongo.close();
	}
	
	
	private String getMD5(String input) throws NoSuchAlgorithmException, UnsupportedEncodingException
	{
		byte[] bytes = input.getBytes("UTF-8");
		
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(bytes);
		
		byte[] hash = md.digest();
		
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<hash.length;i++)
			sb.append(Integer.toString((hash[i]&0xff)+0x100,16).substring(1));
		
	
		return sb.toString();
	}
}


















