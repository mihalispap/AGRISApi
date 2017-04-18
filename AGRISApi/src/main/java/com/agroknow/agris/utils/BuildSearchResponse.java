package com.agroknow.agris.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.joda.time.DateTime;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.facet.Facet;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.facet.terms.TermsFacet;
import org.elasticsearch.search.facet.terms.TermsFacetBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class BuildSearchResponse {

	private int page_size=10;
	private int facet_size=5;
	private int absa_facet_size=99;

	public String buildFrom_betaFacets(Client client, BoolQueryBuilder build_o, 
			BoolQueryBuilder build_child, int page, boolean parent_check,
			BoolQueryBuilder build_enhanced, HttpServletRequest request )
	{

		//System.out.println("STARTING");
		
		SearchRequestBuilder searchRequestBuilder = new SearchRequestBuilder(client)
	    		.setIndices("agris");
		
		QueryBuilder qb = null;

		BoolQueryBuilder bq=QueryBuilders.boolQuery();
		
		
		if(!build_o.hasClauses() && !build_child.hasClauses() && !build_enhanced.hasClauses())
			parent_check=true;
		
		parent_check=false;
		
		if(parent_check)
		{
			qb=QueryBuilders.hasParentQuery("resource",build_o);
			bq.must(qb);
		}
		else
			qb=build_o;
		
		bq.must(build_child);
		bq.must(qb);
		bq.must(build_enhanced);
		
		long ctime=System.currentTimeMillis();
		
		//System.out.println();
		SearchResponse response = 
				searchRequestBuilder
				.setQuery(bq)
				/*.addAggregation(AggregationBuilders.terms("authors")
						.field("dct:creator.foaf:Person.foaf:name.raw")
						.size(page*facet_size+facet_size)
						.order(Terms.Order.count(false)))*/
                .addAggregation(AggregationBuilders.terms("type")
                		.field("dct:type.raw")
                		.size(page*facet_size+facet_size)
                		.order(Terms.Order.count(false)))
	    		.addAggregation(AggregationBuilders.terms("subject")
	    				.field("dc:subject.value.raw")
                		.size(page*facet_size+facet_size)
                		.order(Terms.Order.count(false)))
                /*.addAggregation(AggregationBuilders.terms("source")
	    				.field("dct:source.rdf:resource.raw")
                		.size(page*facet_size+facet_size)
                		.order(Terms.Order.count(false)))
	    		.addAggregation(AggregationBuilders.terms("language")
	    				.field("dct:language")
                		.size(page*facet_size+facet_size)
                		.order(Terms.Order.count(false)))*/
				.setFrom(page*facet_size)
				.setSize(facet_size)
				.execute()
				.actionGet();

		System.out.println("Search response took me:"+(System.currentTimeMillis()-ctime));
		
		ctime=System.currentTimeMillis();
		
		int total=0;
				
		if(response.getHits().getTotalHits()==0)
			return "{\"total\":0,\"page\":0,\"page_size:\":0"
					+",\"time_elapsed\":"+
						((double)response.getTookInMillis()/1000)
					+",\"facets\":[]"
					//+ ",\"results\":[]"
					+ "}";//+bq.toString();
		
		/*
		String input=request.getRequestURI()+"?"+request.getQueryString();
		AESencr aes = new AESencr();
		
		String hashed="";
		try
		{
			hashed=aes.encrypt(input);
			hashed=hashed.replace("\n", "___");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			hashed="error";
		}
		*/
		
		//System.out.println(1);
		String result="{"
				+ "\"total\":2"
				+",\"page\":"+page
				+",\"page_size\":"+facet_size
				+",\"time_elapsed\":"+(double)response.getTookInMillis()/1000
				+",\"facets\":["
					//+ "	\""+hashed+"\""
				//+"{"+buildFacet(response, "type", page)+""
				+"{"+getFacetFromResponse(response, "type", page)+""
				//+",{"+buildFacet(response, "source", page)+""
				//+",{"+buildFacet(response, "authors", page)+""
				//+",{"+buildFacet(response, "subject", page)+""
				+",{"+getFacetFromResponse(response, "subject", page)+""
				///+",{"+buildFacet(response, "language", page)
				;
		 
		System.out.println("Total response generation:"+(System.currentTimeMillis()-ctime));
		
		//result+=hits;
		result+="]}";
		result=result.replace(",]}", "]}");
		//System.out.println(1);
		//result=bq.toString()+result;
		
		//System.out.println(result);
		
		return result;
	}
	
	public String getFacetFromResponse(SearchResponse response, String facet_name, int from)
	{
		long init_time=System.currentTimeMillis()/1000;
		try
		{
			JSONParser parser = new JSONParser();
			
			Terms  terms = response.getAggregations().get(facet_name);
			Collection<Terms.Bucket> tbuckets = terms.getBuckets();
			
			String result="\"total\":"+tbuckets.size()+
					",\"facet_name\":\""+facet_name+"\""+
			",\"results\":[";
				
			Iterator<Terms.Bucket> it = tbuckets.iterator();
			int counter=0;
			
			while(it.hasNext())
			{
				Terms.Bucket bucket = it.next();
				
				if(counter<from*facet_size)
				{
					counter++;
					continue;
				}
				
				result+="{\"count\":"+bucket.getDocCount()+", \"value\":\""+bucket.getKey()+"\"}";
				if(it.hasNext())
					result+=",";
			}

			result+="]}";
			result=result.replace(",]}", "]}");
			
			if(true) return result;
			
			System.out.println(tbuckets.iterator().next().getKey());
			System.out.println(tbuckets.iterator().next().getDocCount());
			System.out.println(tbuckets.toString());
			//assertThat(buckets.size(), equalTo(3));
			
			
			JSONObject obj=(JSONObject) parser.parse(response.getAggregations().toString());
			JSONObject aggr=(JSONObject) obj.get("aggregations");
	
			System.out.println(aggr);
			
			String buckets=((JSONObject)aggr.get(facet_name)).get("buckets").toString();
			
			result="\"total\":"+((JSONArray)((JSONObject)aggr.get(facet_name)).get("buckets")).size()+
					",\"facet_name\":\""+facet_name+"\""+
			",\"results\":"+buckets.replace("\"doc_count\"", "\"count\"").replace("\"key\"","\"value\"");
	
			result+="}";
			result=result.replace(",]}", "]}");
	
			System.out.println("Facet for:"+facet_name+" took:"+(System.currentTimeMillis()/1000-init_time)+"s");
			
			return result;
		}
		catch(Exception e) {
			e.printStackTrace();
			return "}";
		}
	}
	

	public String buildFrom_betaABSA_Facets(Client client, BoolQueryBuilder build_o, 
			BoolQueryBuilder build_child, int page, boolean parent_check,
			BoolQueryBuilder build_enhanced, HttpServletRequest request )
	{

		//System.out.println("STARTING");
		
		SearchRequestBuilder searchRequestBuilder = new SearchRequestBuilder(client)
	    		.setIndices("sa_tweets_combo");
		
		QueryBuilder qb = null;

		BoolQueryBuilder bq=QueryBuilders.boolQuery();
		
		
		if(!build_o.hasClauses() && !build_child.hasClauses() && !build_enhanced.hasClauses())
			parent_check=true;
		
		parent_check=false;
		
		qb=build_o;
		
		bq.must(build_child);
		bq.must(qb);
		bq.must(build_enhanced);
		
		
		//System.out.println();
		SearchResponse response = 
				searchRequestBuilder
				.setQuery(bq)
				.addAggregation(AggregationBuilders.terms("polarity")
						.field("tweet.opinions.polarity")
                		.size(page*absa_facet_size+absa_facet_size)
                		.order(Terms.Order.count(false)))
                /*.addAggregation(AggregationBuilders.terms("usergroup")
                		.field("tweet.opinions.user_group")
                		.size(page*facet_size+facet_size)
                		.order(Terms.Order.count(false)))*/
                .addAggregation(AggregationBuilders.terms("aspect_category")
                		.field("tweet.opinions.aspect_category")
                		.size(page*absa_facet_size+absa_facet_size)
                		.order(Terms.Order.count(false)))
                .addAggregation(AggregationBuilders.dateHistogram("dates")
                		.field("tweet.created_at")
                		.interval(DateHistogram.Interval.YEAR)
                		.format("yyyy")
                		)
				.setFrom(page*page_size)
				.setSize(page_size)
				.execute()
				.actionGet();
		
		if(response.getHits().getTotalHits()==0)
			return "{\"total\":0,\"page\":0,\"page_size:\":0"
					+",\"time_elapsed\":"+
						((double)response.getTookInMillis()/1000)
					+",\"facets\":[]"
					+ ",\"results\":[]"
					+ "}";//+bq.toString();
		
		
		
		String result="{\"total\":3"
				+",\"page\":"+page
				+",\"page_size\":"+facet_size
				+",\"time_elapsed\":"+(double)response.getTookInMillis()/1000
				+",\"facets\":["
					//+ "	\""+hashed+"\""
				+"{"+buildFacet(response, "polarity", page)+""
				//+",{"+buildFacet(response, "usergroup", page)+""
				+",{"+buildFacet(response, "aspect_category", page)+""
				+",{"+buildFacetHistogram(response, "dates")+""
				;
		 
		//System.out.println("Total response generation:"+(System.currentTimeMillis()-ctime));
		
		//result+=hits;
		result+="]}";
		result=result.replace(",]}", "]}");
		
		return result;
	}

	
	public String buildFrom_beta(Client client, BoolQueryBuilder build_o, 
			BoolQueryBuilder build_child, int page, boolean parent_check,
			BoolQueryBuilder build_enhanced, HttpServletRequest request )
	{

		//System.out.println("STARTING");
		
		SearchRequestBuilder searchRequestBuilder = new SearchRequestBuilder(client)
	    		.setIndices("agris");
		
		QueryBuilder qb = null;

		BoolQueryBuilder bq=QueryBuilders.boolQuery();
		
		
		if(!build_o.hasClauses() && !build_child.hasClauses() && !build_enhanced.hasClauses())
			parent_check=true;
		
		parent_check=false;
		
		if(parent_check)
		{
			qb=QueryBuilders.hasParentQuery("resource",build_o);
			bq.must(qb);
		}
		else
			qb=build_o;
		
		bq.must(build_child);
		bq.must(qb);
		bq.must(build_enhanced);
		
		
		//System.out.println();
		SearchResponse response = 
				searchRequestBuilder
				.setQuery(bq)
				/*.addAggregation(AggregationBuilders.terms("authors")
						.field("creator.Person.name.raw")
                		.size(0).order(Terms.Order.count(false)))
                .addAggregation(AggregationBuilders.terms("types")
                		.field("type.raw")
                		.size(0).order(Terms.Order.count(false)))
	    		.addAggregation(AggregationBuilders.terms("resource-types").field("resource.type.raw")
                		.size(0).order(Terms.Order.count(false)))
	    		.addAggregation(AggregationBuilders.terms("subjects").field("subject.value.raw")
                		.size(0).order(Terms.Order.count(false)))
	    		.addAggregation(AggregationBuilders.terms("sources").field("source.resource.raw")
                		.size(0).order(Terms.Order.count(false)))
	    		.addAggregation(AggregationBuilders.terms("langs").field("language")
                		.size(0).order(Terms.Order.count(false)))
                */
				.setFrom(page*page_size)
				.setSize(page_size)
				.execute()
				.actionGet();

		
		int total=0;
		
		String hits="";
		int counter=0;
		
		for(SearchHit hit : response.getHits().getHits())
		{
			
			String id=hit.getId();
			//System.out.println(counter+") is:"+id);
			
			//hits+=specific.getSourceAsString();
			hits+=hit.getSourceAsString();
			hits+=",";
			
			//hits+="UNTIL|||";
			//counter++;
		}

		
		
		if(response.getHits().getTotalHits()==0)
			return "{\"total\":0,\"page\":0,\"page_size:\":0"
					+",\"time_elapsed\":"+
						((double)response.getTookInMillis()/1000)
					+",\"facets\":[]"
					+ ",\"results\":[]"
					+ "}";//+bq.toString();
		
		String input=request.getRequestURI()+"?"+request.getQueryString();
		AESencr aes = new AESencr();
		
		String hashed="";
		try
		{
			hashed=aes.encrypt(input);
			hashed=hashed.replace("\r", "$$$");
			hashed=hashed.replace("\n", "___");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			hashed="error";
		}
		
		//System.out.println(1);
		String result="{"
				+ "\"namespaces\":["
				+ "{\"prefix\":\"dct\",\"value\":\"http://purl.org/dc/terms/\"},"
				+	"{\"prefix\":\"dc\",\"value\":\"http://purl.org/dc/terms/\"},"
				+	"{\"prefix\":\"foaf\",\"value\":\"http://xmlns.com/foaf/0.1/\"},"
				+	"{\"prefix\":\"rdfs\",\"value\":\"http://www.w3.org/2000/01/rdf-schema#\"},"
				+	"{\"prefix\":\"skos\",\"value\":\"http://www.w3.org/2004/02/skos/core#\"},"
				+	"{\"prefix\":\"owl\",\"value\":\"http://www.w3.org/2002/07/owl#\"},"
				+	"{\"prefix\":\"rdf\",\"value\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"},"
				+	"{\"prefix\":\"bibo\",\"value\":\"http://purl.org/ontology/bibo/\"},"
				+	"{\"prefix\":\"edm\",\"value\":\"http://www.europeana.eu/schemas/edm/\"},"
				+	"{\"prefix\":\"prov\",\"value\":\"http://www.w3.org/ns/prov#\"},"
				+	"{\"prefix\":\"schema\",\"value\":\"http://schema.org/\"}"
				+ "],"
				+ "\"total\":"+response.getHits().getTotalHits()
				+",\"page\":"+page
				+",\"page_size\":"+page_size
				+",\"time_elapsed\":"+(double)response.getTookInMillis()/1000
				+",\"facets\":"
					+ "	\""+hashed+"\""
				///+"{"+buildFacet(response, "resource-types")+""
				//+ "	{\"facet_name\":\"sources\",\"fid\":\""+hashed+"so\"}"
				///+"{"+buildFacet(response, "sources")+""
				///+",{"+buildFacet(response, "authors")+""
				//+ "	{\"facet_name\":\"authors\",\"fid\":\""+hashed+"au\"}"
				///+",{"+buildFacet(response, "subjects")+""
				//+ "	{\"facet_name\":\"subjects\",\"fid\":\""+hashed+"su\"}"
				///+",{"+buildFacet(response, "langs")+""
				//+ "	{\"facet_name\":\"langs\",\"fid\":\""+hashed+"la\"}"
				+ ",\"results\":[";
		 
		result+=hits;
		result+="]}";
		result=result.replace(",]}", "]}");
		//System.out.println(1);
		//result=bq.toString()+result;
		
		//System.out.println("I RAN");
		
		return result;
	}


	public String buildFrom_beta_sfth(Client client, BoolQueryBuilder build_o, 
			BoolQueryBuilder build_child, int page, boolean parent_check,
			BoolQueryBuilder build_enhanced, HttpServletRequest request )
	{

		//System.out.println("STARTING");
		
		SearchRequestBuilder searchRequestBuilder = new SearchRequestBuilder(client)
	    		.setIndices("sfth");
		
		QueryBuilder qb = null;

		BoolQueryBuilder bq=QueryBuilders.boolQuery();
		
		
		if(!build_o.hasClauses() && !build_child.hasClauses() && !build_enhanced.hasClauses())
			parent_check=true;
		
		parent_check=false;
		
		if(parent_check)
		{
			qb=QueryBuilders.hasParentQuery("course",build_o);
			bq.must(qb);
		}
		else
			qb=build_o;
		
		bq.must(build_child);
		bq.must(qb);
		bq.must(build_enhanced);
		
		
		System.out.println(bq.toString());
		SearchResponse response = 
				searchRequestBuilder
				.setQuery(bq)
				/*.addAggregation(AggregationBuilders.terms("authors")
						.field("creator.Person.name.raw")
                		.size(0).order(Terms.Order.count(false)))
                .addAggregation(AggregationBuilders.terms("types")
                		.field("type.raw")
                		.size(0).order(Terms.Order.count(false)))
	    		.addAggregation(AggregationBuilders.terms("resource-types").field("resource.type.raw")
                		.size(0).order(Terms.Order.count(false)))
	    		*/
				.addAggregation(AggregationBuilders.terms("training-kind").field("course.training_kind.raw")
                		.size(0).order(Terms.Order.count(false)))
				.addAggregation(AggregationBuilders.terms("industry").field("course.industry.raw")
                		.size(0).order(Terms.Order.count(false)))
				.addAggregation(AggregationBuilders.terms("country").field("course.location.location_country.raw")
                		.size(0).order(Terms.Order.count(false)))
				.addAggregation(AggregationBuilders.terms("state").field("course.location.location_state.raw")
                		.size(0).order(Terms.Order.count(false)))
				.addAggregation(AggregationBuilders.terms("region").field("course.location.location_region.raw")
                		.size(0).order(Terms.Order.count(false)))
				.addAggregation(AggregationBuilders.terms("type").field("course.course_type.raw")
                		.size(0).order(Terms.Order.count(false)))
	    		.addAggregation(AggregationBuilders.terms("topics").field("course.topics.raw")
                		.size(0).order(Terms.Order.count(false)))
	    		.addAggregation(AggregationBuilders.terms("job_competency").field("course.job_competency.raw")
                		.size(0).order(Terms.Order.count(false)))
	    		.addAggregation(AggregationBuilders.terms("language").field("course.language.raw")
                		.size(0).order(Terms.Order.count(false)))
	    		.addAggregation(AggregationBuilders.terms("conference_workshop").field("course.conference_workshop.raw")
                		.size(0).order(Terms.Order.count(false)))
	    		.addAggregation(AggregationBuilders.terms("organisation").field("course.organizer.organisation.name.raw")
                		.size(0).order(Terms.Order.count(false)))
	    		.addAggregation(AggregationBuilders.terms("access_rights").field("course.access_rights.raw")
                		.size(0).order(Terms.Order.count(false)))
                
				.setFrom(page*page_size)
				.setSize(page_size)
				.execute()
				.actionGet();

		
		int total=0;
		
		String hits="";
		int counter=0;
		
		for(SearchHit hit : response.getHits().getHits())
		{
			
			String id=hit.getId();
			//System.out.println(counter+") is:"+id);
			
			//hits+=specific.getSourceAsString();
			hits+=hit.getSourceAsString();
			hits+=",";
			
			//hits+="UNTIL|||";
			//counter++;
		}

		
		
		if(response.getHits().getTotalHits()==0)
			return "{\"total\":0,\"page\":0,\"page_size:\":0"
					+",\"time_elapsed\":"+
						((double)response.getTookInMillis()/1000)
					+",\"facets\":[]"
					+ ",\"results\":[]"
					+ "}";//+bq.toString();
		
		String input=request.getRequestURI()+"?"+request.getQueryString();
		AESencr aes = new AESencr();
		
		String hashed="";
		try
		{
			hashed=aes.encrypt(input);
			hashed=hashed.replace("\r", "$$$");
			hashed=hashed.replace("\n", "___");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			hashed="error";
		}
		
		//System.out.println(1);
		String result="{"
				+ "\"total\":"+response.getHits().getTotalHits()
				+",\"page\":"+page
				+",\"page_size\":"+page_size
				+",\"time_elapsed\":"+(double)response.getTookInMillis()/1000
				+",\"facets\":["
				//	+ "	\""+hashed+"\""
				///+"{"+buildFacet(response, "resource-types")+""
				//+ "	{\"facet_name\":\"sources\",\"fid\":\""+hashed+"so\"}"
				///+"{"+buildFacet(response, "sources")+""
				///+",{"+buildFacet(response, "authors")+""
				//+ "	{\"facet_name\":\"authors\",\"fid\":\""+hashed+"au\"}"
				///+",{"+buildFacet(response, "subjects")+""
				//+ "	{\"facet_name\":\"topics\",\"fid\":\""+hashed+"su\"}"
				+"{"+buildFacet(response, "topics")+""
				+",{"+buildFacet(response, "training-kind")+""
				+",{"+buildFacet(response, "job_competency")+""
				+",{"+buildFacet(response, "type")+""
				+",{"+buildFacet(response, "industry")+""
				+",{"+buildFacet(response, "country")+""
				+",{"+buildFacet(response, "region")+""
				//+ "	{\"facet_name\":\"langs\",\"fid\":\""+hashed+"la\"}"
				+",{"+buildFacet(response, "state")+""
				+",{"+buildFacet(response, "language")+""
				+",{"+buildFacet(response, "conference_workshop")+""
				+",{"+buildFacet(response, "organisation")+""
				+",{"+buildFacet(response, "access_rights")+""
				+ "],\"results\":[";
		 
		result+=hits;
		result+="]}";
		result=result.replace(",]}", "]}");
		//System.out.println(1);
		//result=bq.toString()+result;
		
		//System.out.println("I RAN");
		
		return result;
	}
	

	public String buildFrom_betaABSA(Client client, BoolQueryBuilder build_o, 
			BoolQueryBuilder build_child, int page, boolean parent_check,
			BoolQueryBuilder build_enhanced, HttpServletRequest request )
	{

		//System.out.println("STARTING");
		
		SearchRequestBuilder searchRequestBuilder = new SearchRequestBuilder(client)
	    		.setIndices("sa_tweets_combo");
		
		QueryBuilder qb = null;

		BoolQueryBuilder bq=QueryBuilders.boolQuery();
		
		
		if(!build_o.hasClauses() && !build_child.hasClauses() && !build_enhanced.hasClauses())
			parent_check=true;
		
		parent_check=false;
		
		qb=build_o;
		
		bq.must(build_child);
		bq.must(qb);
		bq.must(build_enhanced);
		
		
		//System.out.println();
		SearchResponse response = 
				searchRequestBuilder
				.setQuery(bq)
				/*.addAggregation(AggregationBuilders.terms("polarity")
						.field("tweet.opinions.polarity")
                		.size(99)
                		.order(Terms.Order.count(false)))
                .addAggregation(AggregationBuilders.terms("aspect_category")
                		.field("tweet.opinions.aspect_category")
                		.size(99)
                		.order(Terms.Order.count(false)))
                .addAggregation(AggregationBuilders.dateHistogram("dates")
                		.field("tweet.created_at")
                		.interval(DateHistogram.Interval.YEAR)
                		.format("yyyy")
                		)*/
				.setFrom(page*page_size)
				.setSize(page_size)
				.execute()
				.actionGet();

		
		int total=0;
		
		String hits="";
		int counter=0;
		
		for(SearchHit hit : response.getHits().getHits())
		{
			
			String id=hit.getId();

			String fulltext="";
			/*
			try 
			{
				JSONObject json = (JSONObject) new JSONParser().parse(hit.getSourceAsString());
				
				String tweet_id=((JSONObject)json.get("opinions")).get("tid").toString();
				
				AGRISMongoDB mongo = new AGRISMongoDB();
				fulltext = mongo.fetchTweet(tweet_id);
				
			} 
			catch (ParseException e) 
			{
			}
			*/
			fulltext=hit.getSourceAsString();
			
			StringBuilder source_value=new StringBuilder(hit.getSourceAsString());
			
			source_value.replace(hit.getSourceAsString().lastIndexOf("}"), 
					hit.getSourceAsString().lastIndexOf("}") + 1,
					",\"score\":"+hit.getScore()+
					",\"detailed\":"+fulltext+
						"}" );
			
			//hits+=hit.getSourceAsString();
			
			hits+=source_value.toString();
			hits+=",";
			
			//hits+="UNTIL|||";
			//counter++;
		}

		
		
		if(response.getHits().getTotalHits()==0)
			return "{\"total\":0,\"page\":0,\"page_size:\":0"
					+",\"time_elapsed\":"+
						((double)response.getTookInMillis()/1000)
					+",\"facets\":[]"
					+ ",\"results\":[]"
					+ "}";//+bq.toString();
		
		String input=request.getRequestURI()+"?"+request.getQueryString();
		AESencr aes = new AESencr();
		
		String hashed="";
		try
		{
			hashed=aes.encrypt(input);
			hashed=hashed.replace("\r", "$$$");
			hashed=hashed.replace("\n", "___");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			hashed="error";
		}
		
		//System.out.println(1);
		String result="{\"total\":"+response.getHits().getTotalHits()
				+",\"page\":"+page
				+",\"page_size\":"+page_size
				+",\"time_elapsed\":"+(double)response.getTookInMillis()/1000
				+",\"facets\":"
					//+"{"+buildFacet(response, "polarity")+","
					//+"{"+buildFacet(response, "aspect_category")+","
					//+"{"+buildFacetHistogram(response, "dates")+""
					+ "	\""+hashed+"\""
				///+"{"+buildFacet(response, "resource-types")+""
				//+ "	{\"facet_name\":\"sources\",\"fid\":\""+hashed+"so\"}"
				///+"{"+buildFacet(response, "sources")+""
				///+",{"+buildFacet(response, "authors")+""
				//+ "	{\"facet_name\":\"authors\",\"fid\":\""+hashed+"au\"}"
				///+",{"+buildFacet(response, "subjects")+""
				//+ "	{\"facet_name\":\"subjects\",\"fid\":\""+hashed+"su\"}"
				///+",{"+buildFacet(response, "langs")+""
				//+ "	{\"facet_name\":\"langs\",\"fid\":\""+hashed+"la\"}"
				+ ",\"results\":[";
		
		result+=hits;
		result+="]}";
		result=result.replace(",]}", "]}");
		//System.out.println(1);
		
		
		//System.out.println("I RAN");
		
		return result;
	}


	public String buildFacet(SearchResponse response, String facet_name)
	{
		Terms  terms = response.getAggregations().get(facet_name);
		List<Bucket> bucketList=new ArrayList<Bucket>();

		int size=0;
		bucketList=terms.getBuckets();
		String fValue="";
		for(int i=0;i<bucketList.size();i++)
		{
			
			if(bucketList.get(i).getKey().equals("") || 
					bucketList.get(i).getKey().isEmpty() ||
					bucketList.get(i).getKey()=="")
				continue;
			
			if(bucketList.get(i).getKey().equals("object"))
					continue;
			
			/*if(facet_name.equals("types"))
			{
				if(bucketList.get(i).getKey().equals("resource_cimmyt")
						||
						bucketList.get(i).getKey().equals("dataset_software")
						||
						bucketList.get(i).getKey().equals("person")
					||
					bucketList.get(i).getKey().equals("organization")
					||
					bucketList.get(i).getKey().equals("collection"))
						continue;
			}*/
						
			fValue+="{\"value\":\""+bucketList.get(i).getKey()+"\", \"count\":"+
					bucketList.get(i).getDocCount()+"},";
			size++;
			
			/*TODO:
			 * 	think about pagination of facets
			 * 
			 * 		perhaps resumption token??
			 * */
			if(size>100)
				break;
		}
		

		String result="\"total\":"+size+
				",\"facet_name\":\""+facet_name+"\""+
		",\"results\":["+fValue;
		
		result+="]}";
		result=result.replace(",]}", "]}");
		
		return result;
	}


	public String buildFacetHistogram(SearchResponse response, String facet_name, int from)
	{
		DateHistogram agg=response.getAggregations().get(facet_name);
		String fValue="";
		int size=-1;
		for(DateHistogram.Bucket entry : agg.getBuckets())
		{
			size++;
			if(size<from*facet_size)
				continue;
			
			String key=entry.getKey();
			DateTime keyAsDate=entry.getKeyAsDate();
			long docCount=entry.getDocCount();
			

			if(key.equals("") || 
					key.isEmpty() ||
					key=="")
				continue;
			
			fValue+="{\"value\":\""+key+"\", \"count\":"+
					docCount+"},";
			
			if(size>(from+1)*facet_size)
				break;
			
		}
		
		String result="\"total\":"+size+
				",\"facet_name\":\""+facet_name+"\""+
		",\"results\":["+fValue;
		
		result+="]}";
		result=result.replace(",]}", "]}");
		
		return result;
		
	}
	

	
	public String buildFacet(SearchResponse response, String facet_name, int from)
	{
		long ctime=System.currentTimeMillis();
		
		Terms  terms = response.getAggregations().get(facet_name);
		List<Bucket> bucketList=new ArrayList<Bucket>();

		int size=0;
		bucketList=terms.getBuckets();
		String fValue="";
		for(int i=from*facet_size;i<bucketList.size();i++)
		{
			
			if(bucketList.get(i).getKey().equals("") || 
					bucketList.get(i).getKey().isEmpty() ||
					bucketList.get(i).getKey()=="")
				continue;
			
			fValue+="{\"value\":\""+bucketList.get(i).getKey()+"\", \"count\":"+
					bucketList.get(i).getDocCount()+"},";
			size++;
			
			/*TODO:
			 * 	think about pagination of facets
			 * 
			 * 		perhaps resumption token??
			 * */
			if(size>=facet_size)
				break;
		}
		

		String result="\"total\":"+size+
				",\"facet_name\":\""+facet_name+"\""+
		",\"results\":["+fValue;
		
		result+="]}";
		result=result.replace(",]}", "]}");
		
		//System.out.println("Facet:"+facet_name+", took:"+(System.currentTimeMillis()-ctime));
		
		return result;
	}
		
	public String buildFrom(Client client, TermsFacet f, SearchResponse response, String facet)
	{
		String result="";
		
		if(!facet.equals("collections"))
		{
		
		//while(true)
		//{
			TermsFacet fac=(TermsFacet) response.getFacets()
					.facet(facet);
					//.facets().get(0);
			
			int size=0;
			for(TermsFacet.Entry entry : fac)
			{
				if(entry.getTerm().string().equals("") || 
						entry.getTerm().string().isEmpty() ||
						entry.getTerm().string()=="")
					continue;
				
				result+="{\"value\":\""+entry.getTerm()+"\",\"count\":"+entry.getCount()+"},";
				size++;
			}
			
			result="{\"total\":"+size+
					",\"facet_name\":\""+f.getName()+"\""+
			",\"results\":["+result;
			
			/*response=client.prepareSearchScroll(response.getScrollId())
					.setScroll(new TimeValue(60000))
					.execute()
					.actionGet();
			
			if(response.getHits().getHits().length==0)
					break;
		}*/

			result+="]}";
			
		}
		else
		{
			result+="{"+buildFacet(client, response, facet)+"";
		}
		result=result.replace(",]}", "]}");
		
		return result;
	}
	
	public String buildFrom(Client client, SearchResponse response, String facet)
	{
		String result="";
		
		//while(true)
		//{
			TermsFacet fac=(TermsFacet) response.getFacets()
					.facet(facet);
					//.facets().get(0);
			
			int size=0;
			for(TermsFacet.Entry entry : fac)
			{
				if(entry.getTerm().string().equals("") || 
						entry.getTerm().string().isEmpty() ||
						entry.getTerm().string()=="")
					continue;
				
				if(entry.getTerm().string().equals("object"))
						continue;
				
				if(facet.equals("type"))
				{
					if(entry.getTerm().string().equals("resource")
							||
						entry.getTerm().string().equals("dataset_software")
							||
						entry.getTerm().string().equals("person")
						||
						entry.getTerm().string().equals("organization")
						||
						entry.getTerm().string().equals("collection"))
							continue;
				}
				
				result+="{\"value\":\""+entry.getTerm()+"\",\"count\":"+entry.getCount()+"},";
				size++;
			}
			
			result="{\"total\":"+size+
					",\"facet_name\":\""+facet+"\""+
			",\"results\":["+result;
			
			/*response=client.prepareSearchScroll(response.getScrollId())
					.setScroll(new TimeValue(60000))
					.execute()
					.actionGet();
			
			if(response.getHits().getHits().length==0)
					break;
		}*/
		
		result+="]}";
		result=result.replace(",]}", "]}");
		
		return result;
	}


	public String buildFacetHistogram(SearchResponse response, String facet_name)
	{
		DateHistogram agg=response.getAggregations().get(facet_name);
		String fValue="";
		int size=0;
		for(DateHistogram.Bucket entry : agg.getBuckets())
		{
			String key=entry.getKey();
			DateTime keyAsDate=entry.getKeyAsDate();
			long docCount=entry.getDocCount();
			

			if(key.equals("") || 
					key.isEmpty() ||
					key=="")
				continue;
			
			fValue+="{\"value\":\""+key+"\", \"count\":"+
					docCount+"},";
			size++;
		}
		
		String result="\"total\":"+size+
				",\"facet_name\":\""+facet_name+"\""+
		",\"results\":["+fValue;
		
		result+="]}";
		result=result.replace(",]}", "]}");
		
		return result;
		/*
		Terms  terms = response.getAggregations().get(facet_name);
		List<Bucket> bucketList=new ArrayList<Bucket>();

		int size=0;
		bucketList=terms.getBuckets();
		String fValue="";
		for(int i=0;i<bucketList.size();i++)
		{
			
			if(bucketList.get(i).getKey().equals("") || 
					bucketList.get(i).getKey().isEmpty() ||
					bucketList.get(i).getKey()=="")
				continue;
			
			if(bucketList.get(i).getKey().equals("object"))
					continue;
			
			if(facet_name.equals("types"))
			{
				if(bucketList.get(i).getKey().equals("resource")
						||
						bucketList.get(i).getKey().equals("dataset_software")
						||
						bucketList.get(i).getKey().equals("person")
					||
					bucketList.get(i).getKey().equals("organization")
					||
					bucketList.get(i).getKey().equals("collection"))
						continue;
			}
						
			fValue+="{\"value\":\""+bucketList.get(i).getKey()+"\", \"count\":"+
					bucketList.get(i).getDocCount()+"},";
			size++;
		}
		

		String result="{\"total\":"+size+
				",\"facet_name\":"+facet_name+
		",\"results\":["+fValue;
		
		result+="]}";
		result=result.replace(",]}", "]}");
		
		return result;*/
	}
	

	public String buildFacet(Client client, SearchResponse response, String facet_name)
	{
		Terms  terms = response.getAggregations().get(facet_name);
		List<Bucket> bucketList=new ArrayList<Bucket>();

		int size=0;
		bucketList=terms.getBuckets();
		String fValue="";
		for(int i=0;i<bucketList.size();i++)
		{
			if(facet_name.equals("collections"))
			{
				GetResponse specific = client
						.prepareGet("cimmyt", "object", bucketList.get(i).getKey())
				        .setFields("title.value")
						.execute()
				        .actionGet();
				String value="";
				try
				{
					value=(String)specific.getField("title.value").getValue();
				}
				catch(java.lang.NullPointerException e)
				{
					break;
				}
				//String value=(String)specific.getField("title.value").getValue();
				//String value=specific.getFields().toString();
				
				/*BoolQueryBuilder build_c =QueryBuilders.boolQuery();
				build_c.must(QueryBuilders.termQuery("object.type", "collection"));
				build_c.must(QueryBuilders.termQuery("object.title.value", "collection"));
								
				SearchResponse response_c=client.prepareSearch("cimmyt")
						.setTypes("object")
						.setSearchType(SearchType.SCAN)
						.setScroll(new TimeValue(60000))
						.setQuery(QueryBuilders.matchQuery("location.value", location))
						.execute().actionGet();*/
				
				fValue+="{\"value\":\""+value+"\", \"count\":"+
						bucketList.get(i).getDocCount()+"},";
				size++;
				
				continue;
			}
			
			if(bucketList.get(i).getKey().equals("") || 
					bucketList.get(i).getKey().isEmpty() ||
					bucketList.get(i).getKey()=="")
				continue;
			
			if(bucketList.get(i).getKey().equals("object"))
					continue;
			
			if(facet_name.equals("types"))
			{
				if(bucketList.get(i).getKey().equals("resource")
						||
						bucketList.get(i).getKey().equals("dataset_software")
						||
						bucketList.get(i).getKey().equals("person")
					||
					bucketList.get(i).getKey().equals("organization")
					||
					bucketList.get(i).getKey().equals("collection"))
						continue;
			}
						
			fValue+="{\"value\":\""+bucketList.get(i).getKey()+"\", \"count\":"+
					bucketList.get(i).getDocCount()+"},";
			size++;
		}
		

		String result="\"total\":"+size+
				",\"facet_name\":\""+facet_name+"\""+
		",\"results\":["+fValue;
		
		result+="]}";
		result=result.replace(",]}", "]}");
		
		return result;
	}
	
	
}
