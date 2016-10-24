package com.agroknow.agris.controllers;

import javax.servlet.http.HttpServletRequest;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.facet.terms.TermsFacet;
import org.elasticsearch.search.facet.terms.TermsFacetBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.agroknow.agris.utils.AGRISMongoDB;
import com.agroknow.agris.utils.BuildSearchResponse;
import com.agroknow.agris.utils.ESClient;
import com.agroknow.agris.utils.ParseGET;
import com.agroknow.agris.utils.ToXML;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@CrossOrigin(origins = "*")
@RestController
public class FacetEndpoint {

	private int facet_size=15;
	
	@RequestMapping(value="/facet/resource-types", method={RequestMethod.GET},produces="text/plain")
	@ApiOperation(value = "Facet for all record types")
	@ApiImplicitParams({
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
	ResponseEntity<String> getAllT(HttpServletRequest request) {
		

		double init_time=(double)System.currentTimeMillis()/1000;
		
		ParseGET parser=new ParseGET();
		
		String apikey=parser.parseApiKey(request);
		int page=parser.parsePage(request);
		
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
				return new ResponseEntity<String>("{\"error\":\"Api validation Error\"}", response_head, HttpStatus.CREATED);
			}
			
			mongodb.addPoints(apikey, "facets", request);
			
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
		

		Client client = ESClient.client;
		String results="";

			SearchResponse response=
					client
					.prepareSearch("agris")
					.setTypes("resource")
					.setQuery(QueryBuilders.matchAllQuery())
	                .addAggregation(AggregationBuilders.terms("types")
	                		.field("dct:type.raw")
	                		.size(page*facet_size+facet_size)
	                		.order(Terms.Order.count(false)))
					.execute()
					.actionGet();
			
			
			BuildSearchResponse builder=new BuildSearchResponse();
			

			results="{\"total\":1"
					+",\"page\":"+page
					+",\"page_size\":"+facet_size
					+",\"time_elapsed\":"+(double)response.getTookInMillis()/1000
					+",\"facets\":"
					+"{"+builder.buildFacet(response, "types", page)
					+"}";
				
		
		if(format.equals("xml"))
		{
			ToXML converter=new ToXML();
			results=converter.convertToXMLFacet(results);
		}
		
		if(mongo_up)
			mongodb.cacheResponse(request, results);
		
		return new ResponseEntity<String>(results, response_head, HttpStatus.CREATED);
        
    } 

	@RequestMapping(value="/facet/sources", method={RequestMethod.GET},produces="text/plain")
	@ApiOperation(value = "Facet for all sources")
	@ApiImplicitParams({
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
	ResponseEntity<String> getSources(HttpServletRequest request) {
		

		double init_time=(double)System.currentTimeMillis()/1000;
		
		ParseGET parser=new ParseGET();
		
		String apikey=parser.parseApiKey(request);
		int page=parser.parsePage(request);
		
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
				return new ResponseEntity<String>("{\"error\":\"Api validation Error\"}", response_head, HttpStatus.CREATED);
			}
			
			mongodb.addPoints(apikey, "facets", request);
			
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
		

		Client client = ESClient.client;
		String results="";
			
			SearchResponse response=
					client
					.prepareSearch("agris")
					.setTypes("resource")
					.setQuery(QueryBuilders.matchAllQuery())
	                .addAggregation(AggregationBuilders.terms("sources")
	                		.field("dct:source.rdf:resource.raw")
	                		.size(page*facet_size+facet_size)
	                		.order(Terms.Order.count(false)))
					.execute()
					.actionGet();
			
			
			BuildSearchResponse builder=new BuildSearchResponse();
			

			results="{\"total\":1"
					+",\"page\":"+page
					+",\"page_size\":"+facet_size
					+",\"time_elapsed\":"+(double)response.getTookInMillis()/1000
					+",\"facets\":"
					+"{"+builder.buildFacet(response, "sources", page)
					+"}";
				
		
		if(format.equals("xml"))
		{
			ToXML converter=new ToXML();
			results=converter.convertToXMLFacet(results);
		}
		
		if(mongo_up)
			mongodb.cacheResponse(request, results);
		
		return new ResponseEntity<String>(results, response_head, HttpStatus.CREATED);
        
    } 


	@RequestMapping(value="/facet/subjects", method={RequestMethod.GET},produces="text/plain")
	@ApiOperation(value = "Facet for all subjects")
	@ApiImplicitParams({
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
	ResponseEntity<String> getSubjects(HttpServletRequest request) {
		

		double init_time=(double)System.currentTimeMillis()/1000;
		
		ParseGET parser=new ParseGET();
		
		String apikey=parser.parseApiKey(request);
		int page=parser.parsePage(request);
		
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
				return new ResponseEntity<String>("{\"error\":\"Api validation Error\"}", response_head, HttpStatus.CREATED);
			}
			
			mongodb.addPoints(apikey, "facets", request);
			
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
		

		Client client = ESClient.client;
		String results="";
			
			SearchResponse response=
					client
					.prepareSearch("agris")
					.setTypes("resource")
					.setQuery(QueryBuilders.matchAllQuery())
	                .addAggregation(AggregationBuilders.terms("subjects")
	                		.field("dc:subject.value.raw")
	                		.size(page*facet_size+facet_size)
	                		.order(Terms.Order.count(false)))
					.execute()
					.actionGet();
			
			
			BuildSearchResponse builder=new BuildSearchResponse();
			

			results="{\"total\":1"
					+",\"page\":"+page
					+",\"page_size\":"+facet_size
					+",\"time_elapsed\":"+(double)response.getTookInMillis()/1000
					+",\"facets\":"
					+"{"+builder.buildFacet(response, "subjects", page)
					+"}";
				
		
		if(format.equals("xml"))
		{
			ToXML converter=new ToXML();
			results=converter.convertToXMLFacet(results);
		}
		
		if(mongo_up)
			mongodb.cacheResponse(request, results);
		
		return new ResponseEntity<String>(results, response_head, HttpStatus.CREATED);
        
    } 


	@RequestMapping(value="/facet/languages", method={RequestMethod.GET},produces="text/plain")
	@ApiOperation(value = "Facet for languages")
	@ApiImplicitParams({
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
	ResponseEntity<String> getLangs(HttpServletRequest request) {
		

		double init_time=(double)System.currentTimeMillis()/1000;
		
		ParseGET parser=new ParseGET();
		
		String apikey=parser.parseApiKey(request);
		int page=parser.parsePage(request);
		
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
				return new ResponseEntity<String>("{\"error\":\"Api validation Error\"}", response_head, HttpStatus.CREATED);
			}
			
			mongodb.addPoints(apikey, "facets", request);
			
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
		

		Client client = ESClient.client;
		String results="";
			
			SearchResponse response=
					client
					.prepareSearch("agris")
					.setTypes("resource")
					.setQuery(QueryBuilders.matchAllQuery())
	                .addAggregation(AggregationBuilders.terms("langs")
	                		.field("dct:language")
	                		.size(page*facet_size+facet_size)
	                		.order(Terms.Order.count(false)))
					.execute()
					.actionGet();
			
			
			BuildSearchResponse builder=new BuildSearchResponse();
			

			results="{\"total\":1"
					+",\"page\":"+page
					+",\"page_size\":"+facet_size
					+",\"time_elapsed\":"+(double)response.getTookInMillis()/1000
					+",\"facets\":"
					+"{"+builder.buildFacet(response, "langs", page)
					+"}";
				
		
		if(format.equals("xml"))
		{
			ToXML converter=new ToXML();
			results=converter.convertToXMLFacet(results);
		}
		
		if(mongo_up)
			mongodb.cacheResponse(request, results);
		
		return new ResponseEntity<String>(results, response_head, HttpStatus.CREATED);
        
    } 


	@RequestMapping(value="/facet/authors", method={RequestMethod.GET},produces="text/plain")
	@ApiOperation(value = "Facet for all authors")
	@ApiImplicitParams({
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
	ResponseEntity<String> getAuthors(HttpServletRequest request) {
		

		double init_time=(double)System.currentTimeMillis()/1000;
		
		ParseGET parser=new ParseGET();
		
		String apikey=parser.parseApiKey(request);
		int page=parser.parsePage(request);
		
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
				return new ResponseEntity<String>("{\"error\":\"Api validation Error\"}", response_head, HttpStatus.CREATED);
			}
			
			mongodb.addPoints(apikey, "facets", request);
			
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
		

		Client client = ESClient.client;
		String results="";

			SearchResponse response=
					client
					.prepareSearch("agris")
					.setTypes("resource")
					.setQuery(QueryBuilders.matchAllQuery())
	                .addAggregation(AggregationBuilders.terms("authors")
	                		.field("dct:creator.foaf:Person.foaf:name.raw")
	                		.size(page*facet_size+facet_size)
	                		.order(Terms.Order.count(false)))
					.execute()
					.actionGet();
			
			
			BuildSearchResponse builder=new BuildSearchResponse();
			

			results="{\"total\":1"
					+",\"page\":"+page
					+",\"page_size\":"+facet_size
					+",\"time_elapsed\":"+(double)response.getTookInMillis()/1000
					+",\"facets\":"
					+"{"+builder.buildFacet(response, "authors", page)
					+"}";
				
		
		if(format.equals("xml"))
		{
			ToXML converter=new ToXML();
			results=converter.convertToXMLFacet(results);
		}
		
		if(mongo_up)
			mongodb.cacheResponse(request, results);
		
		return new ResponseEntity<String>(results, response_head, HttpStatus.CREATED);
        
    } 

/*
	@RequestMapping(value="/facet/locations-ENR", method={RequestMethod.GET},produces="text/plain")
	@ApiOperation(value = "Facet for all locations")
	@ApiImplicitParams({
        @ApiImplicitParam(
    			name = "format", 
    			value = "output format", 
    			required = true, 
    			dataType = "string", 
    			paramType = "query",
    			defaultValue="json")
      })
	String getAllLocations(HttpServletRequest request) {
        
    	Settings settings = ImmutableSettings.settingsBuilder()
		        .put("cluster.name", "agroknow").build();
    	
    	Client client = new TransportClient(settings)
		        .addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
		        //.addTransportAddress(new InetSocketTransportAddress("host2", 9300));
		System.out.println("Status:"+client.settings().toString());
		// on shutdown
		String results="";
				
			TermsFacetBuilder facet =
					FacetBuilders.termsFacet("locations").field("location.value.raw").size(99999);
			
			SearchResponse response=
					client.prepareSearch("cimmyt")
					.setTypes("person", "organization", 
							"resource", "dataset_software")
					.setSearchType(SearchType.SCAN)
					.setScroll(new TimeValue(60000))
					.setQuery(QueryBuilders.matchAllQuery())
					.addFacet(facet)
					.execute().actionGet();
			
			TermsFacet f=(TermsFacet) response.getFacets()
					.facetsAsMap().get("locations");
			String facet_name="locations";
			
			BuildSearchResponse builder=new BuildSearchResponse();
			results=builder.buildFrom(client,f, response, facet_name);
		
		client.close();
		

		String format;
		ParseGET parser=new ParseGET();
		format=parser.parseFormat(request);
		if(format.equals("xml"))
		{
			ToXML converter=new ToXML();
			results=converter.convertToXMLFacet(results);
		}
		
		//results="";
    	return results;
        
    }


	@RequestMapping(value="/facet/relations-ENR", method={RequestMethod.GET},produces="text/plain")
	@ApiOperation(value = "Facet for all relations")
	@ApiImplicitParams({
        @ApiImplicitParam(
    			name = "format", 
    			value = "output format", 
    			required = true, 
    			dataType = "string", 
    			paramType = "query",
    			defaultValue="json")
      })
	String getAllRelations(HttpServletRequest request) {
        
    	Settings settings = ImmutableSettings.settingsBuilder()
		        .put("cluster.name", "agroknow").build();
    	
    	Client client = new TransportClient(settings)
		        .addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
		        //.addTransportAddress(new InetSocketTransportAddress("host2", 9300));
		System.out.println("Status:"+client.settings().toString());
		// on shutdown
		String results="";
			
			TermsFacetBuilder facet =
					FacetBuilders.termsFacet("relations").field("relation.raw").size(99999);
			
			SearchResponse response=
					client.prepareSearch("cimmyt")
					.setTypes("person", "organization", 
							"resource", "dataset_software")
					.setSearchType(SearchType.SCAN)
					.setScroll(new TimeValue(60000))
					.setQuery(QueryBuilders.matchAllQuery())
					.addFacet(facet)
					.execute().actionGet();
			
			TermsFacet f=(TermsFacet) response.getFacets()
					.facetsAsMap().get("relations");
			String facet_name="relations";
			
			BuildSearchResponse builder=new BuildSearchResponse();
			results=builder.buildFrom(client,f, response, facet_name);
		
		client.close();

		String format;
		ParseGET parser=new ParseGET();
		format=parser.parseFormat(request);
		if(format.equals("xml"))
		{
			ToXML converter=new ToXML();
			results=converter.convertToXMLFacet(results);
		}
		
		//results="";
    	return results;
        
    }
*/
	
	
}
