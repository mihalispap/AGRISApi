package com.agroknow.agris.controllers;

import java.net.UnknownHostException;
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
public class APIKeyEndpoint {


	@ApiOperation(value = "Register APIKey", nickname = "register apikey")
	@RequestMapping( value="/add-apikey", method={RequestMethod.GET},
	/*produces={"application/xml","application/json"}*/
		produces="*/*")
	@ApiImplicitParams({
		@ApiImplicitParam(
				name = "apikey", 
				value = "", 
				required = true, 
				dataType = "string", 
				paramType = "query", 
				defaultValue=""),
		@ApiImplicitParam(
				name = "auth", 
				value = "", 
				required = true, 
				dataType = "string", 
				paramType = "query", 
				defaultValue="")
	})
	ResponseEntity<String> runResource(HttpServletRequest request) throws UnknownHostException {

		String format;
		ParseGET parser=new ParseGET();
		//ParseGET parser=new ParseGET();
		format=parser.parseFormat(request);
		HttpHeaders response_head=new HttpHeaders();
		
		if(format.equals("xml"))
			response_head.setContentType(new MediaType("application","xml"));
		else
			response_head.setContentType(new MediaType("application","json"));
		
		
		String auth=parser.parseAuth(request);
		if(!auth.equals("t@ffagr0key"))
		{
			return new ResponseEntity<String>("{\"error\":\"Api validation Error\"}", response_head, HttpStatus.CREATED);
		}
		
		String apikey=parser.parseApiKey(request);
		
		AGRISMongoDB mongodb = new AGRISMongoDB();
		
		boolean mongo_up=true;

		try
		{
			mongodb.insertAPIKey(apikey);
			
		}
		catch(Exception e)
		{
			return new ResponseEntity<String>("{\"error\":\"Unknown Error\"}", response_head, HttpStatus.CREATED);
		}
		
		return new ResponseEntity<String>("{\"status\":\"Registered\"}", response_head, HttpStatus.CREATED);
    }
    


}
