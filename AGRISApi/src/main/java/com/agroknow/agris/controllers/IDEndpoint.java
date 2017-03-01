package com.agroknow.agris.controllers;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.HasParentQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermFilterBuilder;
import org.elasticsearch.search.SearchHit;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.agroknow.agris.utils.AGRISMongoDB;
import com.agroknow.agris.utils.ESClient;
import com.agroknow.agris.utils.ParseGET;
import com.agroknow.agris.utils.ToXML;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import org.springframework.web.bind.annotation.RequestMethod;

@CrossOrigin(origins = "*")
@RestController
public class IDEndpoint {


	@ApiOperation(value = "Get resource by Id", nickname = "find entity values by id")
    @RequestMapping(method = RequestMethod.GET, path="/resource/{id}",produces="text/plain"
    /*, produces = {"application/json","application/xml"}*/)
	@ApiImplicitParams({
        @ApiImplicitParam(
        			name = "id", 
        			value = "resource's id", 
        			required = true, 
        			dataType = "string", 
        			paramType = "path", 
        			defaultValue="BR2015202788"),
        @ApiImplicitParam(
    			name = "format", 
    			value = "output format", 
    			required = true, 
    			dataType = "string", 
    			paramType = "query",
    			defaultValue="json"),
		@ApiImplicitParam(
    	    	name = "apikey", 
    	    	value = "apikey", 
    	    	required = true, 
    	    	dataType = "string", 
    	    	paramType = "query", 
    	    	defaultValue="agroknow"),
		@ApiImplicitParam(
    			name = "cache", 
    			value = "use cache", 
    			required = true, 
    			dataType = "boolean", 
    			paramType = "query",
    			defaultValue="true")
      })	
	ResponseEntity<String> runResource(@PathVariable String id, HttpServletRequest request) {
		
		ParseGET parser=new ParseGET();
		
		String apikey=parser.parseApiKey(request);
		
		AGRISMongoDB mongodb = new AGRISMongoDB();
		
		boolean mongo_up=true;
		
		String format;
		//ParseGET parser=new ParseGET();
		format=parser.parseFormat(request);
		HttpHeaders response_head=new HttpHeaders();
		
		if(format.equals("xml"))
			response_head.setContentType(new MediaType("application","xml"));
		else
			response_head.setContentType(new MediaType("application","json"));
		
		try
		{
			if(!mongodb.isValidApiKey(apikey))
			{
				//return "{\"error\":\"Api validation Error\"}";
				return new ResponseEntity<String>("{\"error\":\"Api validation Error\"}", response_head, HttpStatus.CREATED);
				
			}
			
			mongodb.addPoints(apikey, "id", request);
			
			try
			{
				boolean use_cache=parser.parseCache(request);			
				
				if(use_cache)
				{
					String cached = mongodb.checkCache(request);
					if(!cached.isEmpty())
					{
						System.out.println("CACHE HIT!");			
						double end_time=(double)System.currentTimeMillis()/1000;
						//System.out.println("Took:"+(double)(end_time-init_time));
						//return cached;
						
						return new ResponseEntity<String>(cached, response_head, HttpStatus.CREATED);
						
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		catch(Exception e)
		{
			mongo_up=false;
		}
		
		
    	Settings settings = ImmutableSettings.settingsBuilder()
		        .put("cluster.name", "agroknow").build();
    	
    	Client client = ESClient.client;
    	
		//System.out.println("Status:"+client.settings().toString());
		// on shutdown
		
		GetResponse response = client.prepareGet("agris", "resource", id)
		        .execute()
		        .actionGet();
		
		BoolQueryBuilder build_o =QueryBuilders.boolQuery();
		build_o.must(QueryBuilders.termQuery("resource.dct:identifier", id));
		/*HasParentQueryBuilder qb=QueryBuilders.hasParentQuery("object",
					QueryBuilders.matchQuery("appid", id));*/

		SearchResponse responseSpecific=client
				.prepareSearch("agris")
				.setQuery(build_o)
				//.setSize(1)
				.execute()
				.actionGet();

		
		String results="";
		
		int size=0;
		try
		{
			if(!response.getSourceAsString().isEmpty())
				size=1;
		}
		catch(java.lang.NullPointerException e)
		{

			return new ResponseEntity<String>("{\"total\":0,"
					+ ",\"results\":[]"
					+ "}", response_head, HttpStatus.CREATED);
		}

		

		String specific_source="";
		
		for(SearchHit hit : responseSpecific.getHits().getHits())
		{
			specific_source=hit.getSourceAsString();
			break;
		}
		
		

		results+="{\"total\":"+size+",\"results\":[{"
				+ "\"resource\":"+response.getSourceAsString()+"}";
						//+ "\"detailed\":"+responseSpecific.getSourceAsString()+"";
		
		
		
		results+="]}";
		
		if(format.equals("xml"))
		{
			ToXML converter=new ToXML();
			results=converter.convertToXMLID(results);
		}
		

		if(mongo_up)
			mongodb.cacheResponse(request, results);
		
    	//return results;
    	return new ResponseEntity<String>(results, response_head, HttpStatus.CREATED);
    }
    

	@ApiOperation(value = "Get raw tweet by Id", nickname = "find tweet by id")
    @RequestMapping(method = RequestMethod.GET, path="/tweet/{id}",produces="text/plain"
    /*, produces = {"application/json","application/xml"}*/)
	@ApiImplicitParams({
        @ApiImplicitParam(
        			name = "id", 
        			value = "tweet's id", 
        			required = true, 
        			dataType = "string", 
        			paramType = "path", 
        			defaultValue="706879573380747300"),
        @ApiImplicitParam(
    			name = "format", 
    			value = "output format", 
    			required = true, 
    			dataType = "string", 
    			paramType = "query",
    			defaultValue="json"),
		@ApiImplicitParam(
    	    	name = "apikey", 
    	    	value = "apikey", 
    	    	required = true, 
    	    	dataType = "string", 
    	    	paramType = "query", 
    	    	defaultValue="agroknow"),
		@ApiImplicitParam(
    			name = "cache", 
    			value = "use cache", 
    			required = true, 
    			dataType = "boolean", 
    			paramType = "query",
    			defaultValue="true")
      })	
	ResponseEntity<String> runTweet(@PathVariable String id, HttpServletRequest request) {
		
		ParseGET parser=new ParseGET();
		
		String apikey=parser.parseApiKey(request);
		
		AGRISMongoDB mongodb = new AGRISMongoDB();
		
		String format;
		//ParseGET parser=new ParseGET();
		format=parser.parseFormat(request);
		HttpHeaders response_head=new HttpHeaders();
		
		if(format.equals("xml"))
			response_head.setContentType(new MediaType("application","xml"));
		else
			response_head.setContentType(new MediaType("application","json"));
		
		
		boolean mongo_up=true;
		
		try
		{
			if(!mongodb.isValidApiKey(apikey))
			{
				return new ResponseEntity<String>("{\"error\":\"Api validation Error\"}", response_head, HttpStatus.CREATED);
			}
			
			mongodb.addPoints(apikey, "id", request);
			
			try
			{
				boolean use_cache=parser.parseCache(request);			
				
				if(use_cache)
				{
					String cached = mongodb.checkCache(request);
					if(!cached.isEmpty())
					{
						System.out.println("CACHE HIT!");			
						double end_time=(double)System.currentTimeMillis()/1000;
						//System.out.println("Took:"+(double)(end_time-init_time));
						//return cached;
						
						return new ResponseEntity<String>(cached, response_head, HttpStatus.CREATED);
						
					}
				}
			}
			catch(Exception e)
			{
				//e.printStackTrace();
			}
		}
		catch(Exception e)
		{
			mongo_up=false;
		}
		
		String results="";
		
		try
		{
			results = mongodb.fetchTweet(id);
		}
		catch(Exception e)
		{
			results="{\"error\":\"the id could not be found\"}";
		}

		if(format.equals("xml"))
		{
			ToXML converter=new ToXML();
			results=converter.convertToXMLID(results);
		}
		

		if(mongo_up)
			mongodb.cacheResponse(request, results);
		
    	//return results;
		return new ResponseEntity<String>(results, response_head, HttpStatus.CREATED);
		
    }
    

	@ApiOperation(value = "Get course by ID", nickname = "find entity values by ID")
    @RequestMapping(method = RequestMethod.GET, path="/course/{id}",produces="text/plain"
    /*, produces = {"application/json","application/xml"}*/)
	@ApiImplicitParams({
        @ApiImplicitParam(
        			name = "id", 
        			value = "course ID", 
        			required = true, 
        			dataType = "string", 
        			paramType = "path", 
        			defaultValue="8"),
        @ApiImplicitParam(
    			name = "format", 
    			value = "output format", 
    			required = true, 
    			dataType = "string", 
    			paramType = "query",
    			defaultValue="json"),
		@ApiImplicitParam(
    	    	name = "apikey", 
    	    	value = "apikey", 
    	    	required = true, 
    	    	dataType = "string", 
    	    	paramType = "query", 
    	    	defaultValue="sfth-dev"),
		@ApiImplicitParam(
    			name = "cache", 
    			value = "use cache", 
    			required = true, 
    			dataType = "boolean", 
    			paramType = "query",
    			defaultValue="true")
      })	
	ResponseEntity<String> runCourse(@PathVariable String id, HttpServletRequest request) {
		
		ParseGET parser=new ParseGET();
		
		String apikey=parser.parseApiKey(request);
		
		AGRISMongoDB mongodb = new AGRISMongoDB();
		
		boolean mongo_up=true;
		
		String format;
		//ParseGET parser=new ParseGET();
		format=parser.parseFormat(request);
		HttpHeaders response_head=new HttpHeaders();
		
		if(format.equals("xml"))
			response_head.setContentType(new MediaType("application","xml"));
		else
			response_head.setContentType(new MediaType("application","json"));
		
		try
		{
			if(!mongodb.isValidApiKey(apikey))
			{
				//return "{\"error\":\"Api validation Error\"}";
				return new ResponseEntity<String>("{\"error\":\"Api validation Error\"}", response_head, HttpStatus.CREATED);
				
			}
			
			mongodb.addPoints(apikey, "id", request);
			
			try
			{
				boolean use_cache=parser.parseCache(request);			
				
				if(use_cache)
				{
					String cached = mongodb.checkCache(request);
					if(!cached.isEmpty())
					{
						System.out.println("CACHE HIT!");			
						double end_time=(double)System.currentTimeMillis()/1000;
						//System.out.println("Took:"+(double)(end_time-init_time));
						//return cached;
						
						return new ResponseEntity<String>(cached, response_head, HttpStatus.CREATED);
						
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		catch(Exception e)
		{
			mongo_up=false;
		}
		
		
    	Settings settings = ImmutableSettings.settingsBuilder()
		        .put("cluster.name", "agroknow").build();
    	
    	Client client = ESClient.client;
    	
		//System.out.println("Status:"+client.settings().toString());
		// on shutdown
		
		GetResponse response = client.prepareGet("sfth", "course", id)
		        .execute()
		        .actionGet();
		
		BoolQueryBuilder build_o =QueryBuilders.boolQuery();
		build_o.must(QueryBuilders.termQuery("course.identifier", id));
		/*HasParentQueryBuilder qb=QueryBuilders.hasParentQuery("object",
					QueryBuilders.matchQuery("appid", id));*/

		SearchResponse responseSpecific=client
				.prepareSearch("sfth")
				.setQuery(build_o)
				//.setSize(1)
				.execute()
				.actionGet();

		
		String results="";
		
		/*int size=0;
		try
		{
			if(!response.getSourceAsString().isEmpty())
				size=1;
		}
		catch(java.lang.NullPointerException e)
		{

			return new ResponseEntity<String>("{\"total\":0"
					+ ",\"results\":[]"
					+ "}", response_head, HttpStatus.CREATED);
		}
		 */
		
		int size=0;
		String specific_source="";
		
		for(SearchHit hit : responseSpecific.getHits().getHits())
		{
			specific_source=hit.getSourceAsString();
			size++;
			break;
		}
		
		

		results+="{\"total\":"+size+",\"results\":[{"
				+ "\"course\":"+specific_source+"}";
						//+ "\"detailed\":"+responseSpecific.getSourceAsString()+"";
		
		
		
		results+="]}";
		
		if(format.equals("xml"))
		{
			ToXML converter=new ToXML();
			results=converter.convertToXMLID(results);
		}
		

		if(mongo_up)
			mongodb.cacheResponse(request, results);
		
    	//return results;
    	return new ResponseEntity<String>(results, response_head, HttpStatus.CREATED);
    }


}
