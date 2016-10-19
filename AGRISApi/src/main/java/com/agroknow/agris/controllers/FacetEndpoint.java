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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.agroknow.agris.utils.AGRISMongoDB;
import com.agroknow.agris.utils.BuildSearchResponse;
import com.agroknow.agris.utils.ParseGET;
import com.agroknow.agris.utils.ToXML;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@CrossOrigin
@RestController
public class FacetEndpoint {

	
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
    	    	defaultValue="agroknow")
      })
    String getAllT(HttpServletRequest request) {
		
		ParseGET parser=new ParseGET();
		
		String apikey=parser.parseApiKey(request);
		
		AGRISMongoDB mongodb = new AGRISMongoDB();
		
		if(!mongodb.isValidApiKey(apikey))
		{
			return "{\"error\":\"Api validation Error\"}";
		}
		
		mongodb.addPoints(apikey, "facet", request);
		
		
    	Settings settings = ImmutableSettings.settingsBuilder()
		        .put("cluster.name", "agroknow").build();
    	
    	Client client = new TransportClient(settings)
		        .addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
		        //.addTransportAddress(new InetSocketTransportAddress("host2", 9300));
		System.out.println("Status:"+client.settings().toString());
		// on shutdown
		String results="";
				
			TermsFacetBuilder facet =
					FacetBuilders.termsFacet("types").field("type.raw").size(9999);
			
			SearchResponse response=
					client.prepareSearch("agris")
					.setTypes("resource")
					.setSearchType(SearchType.SCAN)
					.setScroll(new TimeValue(60000))
					.setQuery(QueryBuilders.matchAllQuery())
					.addFacet(facet)
					.execute().actionGet();
			
			TermsFacet f=(TermsFacet) response.getFacets()
					.facetsAsMap().get("types");
			String facet_name="types";
			
			BuildSearchResponse builder=new BuildSearchResponse();
			results=builder.buildFrom(client,f, response, facet_name);
		
		client.close();
		
		String format;

		format=parser.parseFormat(request);
		if(format.equals("xml"))
		{
			ToXML converter=new ToXML();
			results=converter.convertToXMLFacet(results);
		}
		

    	return results;
        
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
    	    	defaultValue="agroknow")
      })
	String getAllSources(HttpServletRequest request) {
        		
		ParseGET parser=new ParseGET();
		
		String apikey=parser.parseApiKey(request);
		
		AGRISMongoDB mongodb = new AGRISMongoDB();
		
		if(!mongodb.isValidApiKey(apikey))
		{
			return "{\"error\":\"Api validation Error\"}";
		}
		
		mongodb.addPoints(apikey, "facet", request);
		
    	Settings settings = ImmutableSettings.settingsBuilder()
		        .put("cluster.name", "agroknow").build();
    	
    	Client client = new TransportClient(settings)
		        .addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
		        //.addTransportAddress(new InetSocketTransportAddress("host2", 9300));
		System.out.println("Status:"+client.settings().toString());
		// on shutdown
		String results="";
				
			TermsFacetBuilder facet =
					FacetBuilders.termsFacet("sources").field("source.resource.raw").size(9999);
			
			SearchResponse response=
					client.prepareSearch("agris")
					.setTypes("resource")
					.setSearchType(SearchType.SCAN)
					.setScroll(new TimeValue(60000))
					.setQuery(QueryBuilders.matchAllQuery())
					.addFacet(facet)
					.execute().actionGet();
			
			TermsFacet f=(TermsFacet) response.getFacets()
					.facetsAsMap().get("sources");
			String facet_name="sources";
			
			BuildSearchResponse builder=new BuildSearchResponse();
			results=builder.buildFrom(client,f, response, facet_name);
		
		client.close();
		
		String format;

		format=parser.parseFormat(request);
		if(format.equals("xml"))
		{
			ToXML converter=new ToXML();
			results=converter.convertToXMLFacet(results);
		}
				
    	return results;
        
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
    	    	defaultValue="agroknow")
      })
	String getAllSubjects(HttpServletRequest request) {
		
		ParseGET parser=new ParseGET();
		
		String apikey=parser.parseApiKey(request);
		
		AGRISMongoDB mongodb = new AGRISMongoDB();
		
		if(!mongodb.isValidApiKey(apikey))
		{
			return "{\"error\":\"Api validation Error\"}";
		}
		
		mongodb.addPoints(apikey, "facet", request);
		
    	Settings settings = ImmutableSettings.settingsBuilder()
		        .put("cluster.name", "agroknow").build();
    	
    	Client client = new TransportClient(settings)
		        .addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
		        //.addTransportAddress(new InetSocketTransportAddress("host2", 9300));
		System.out.println("Status:"+client.settings().toString());
		// on shutdown
		String results="";
				
			TermsFacetBuilder facet =
					FacetBuilders.termsFacet("subjects").field("subject.value.raw").size(9999);
			
			SearchResponse response=
					client.prepareSearch("agris")
					.setTypes("resource")
					.setSearchType(SearchType.SCAN)
					.setScroll(new TimeValue(60000))
					.setQuery(QueryBuilders.matchAllQuery())
					.addFacet(facet)
					.execute().actionGet();
			
			TermsFacet f=(TermsFacet) response.getFacets()
					.facetsAsMap().get("subjects");
			String facet_name="subjects";
			
			BuildSearchResponse builder=new BuildSearchResponse();
			results=builder.buildFrom(client,f, response, facet_name);
		
		client.close();
		
		String format;

		format=parser.parseFormat(request);
		if(format.equals("xml"))
		{
			ToXML converter=new ToXML();
			results=converter.convertToXMLFacet(results);
		}
				
    	return results;
        
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
    	    	defaultValue="agroknow")
      })
	String getAllLangs(HttpServletRequest request) {
		
		ParseGET parser=new ParseGET();
		
		String apikey=parser.parseApiKey(request);
		
		AGRISMongoDB mongodb = new AGRISMongoDB();
		
		if(!mongodb.isValidApiKey(apikey))
		{
			return "{\"error\":\"Api validation Error\"}";
		}
		
		mongodb.addPoints(apikey, "facet", request);
		
    	Settings settings = ImmutableSettings.settingsBuilder()
		        .put("cluster.name", "agroknow").build();
    	
    	Client client = new TransportClient(settings)
		        .addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
		        //.addTransportAddress(new InetSocketTransportAddress("host2", 9300));
		System.out.println("Status:"+client.settings().toString());
		// on shutdown
		String results="";
				
			TermsFacetBuilder facet =
					FacetBuilders.termsFacet("langs").field("language").size(9999);
			
			SearchResponse response=
					client.prepareSearch("agris")
					.setTypes("resource")
					.setSearchType(SearchType.SCAN)
					.setScroll(new TimeValue(60000))
					.setQuery(QueryBuilders.matchAllQuery())
					.addFacet(facet)
					.execute().actionGet();
			
			TermsFacet f=(TermsFacet) response.getFacets()
					.facetsAsMap().get("langs");
			String facet_name="langs";
			
			BuildSearchResponse builder=new BuildSearchResponse();
			results=builder.buildFrom(client,f, response, facet_name);
		
		client.close();

		String format;

		format=parser.parseFormat(request);
		if(format.equals("xml"))
		{
			ToXML converter=new ToXML();
			results=converter.convertToXMLFacet(results);
		}
		

		return results;
        
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
    	    	defaultValue="agroknow")
      })
	String getAllAuthors(HttpServletRequest request) {
		
		ParseGET parser=new ParseGET();
		
		String apikey=parser.parseApiKey(request);
		
		AGRISMongoDB mongodb = new AGRISMongoDB();
		
		if(!mongodb.isValidApiKey(apikey))
		{
			return "{\"error\":\"Api validation Error\"}";
		}
		
		mongodb.addPoints(apikey, "facet", request);
		
    	Settings settings = ImmutableSettings.settingsBuilder()
		        .put("cluster.name", "agroknow").build();
    	
    	Client client = new TransportClient(settings)
		        .addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
		        //.addTransportAddress(new InetSocketTransportAddress("host2", 9300));
		System.out.println("Status:"+client.settings().toString());
		// on shutdown
		String results="";
				
			TermsFacetBuilder facet =
					FacetBuilders.termsFacet("authors").field("creator.Person.name.raw").size(99999);
			
			SearchResponse response=
					client.prepareSearch("agris")
					.setTypes("resource")
					.setSearchType(SearchType.SCAN)
					.setScroll(new TimeValue(60000))
					.setQuery(QueryBuilders.matchAllQuery())
					.addFacet(facet)
					.execute().actionGet();
			
			TermsFacet f=(TermsFacet) response.getFacets()
					.facetsAsMap().get("authors");
			String facet_name="authors";
			
			BuildSearchResponse builder=new BuildSearchResponse();
			results=builder.buildFrom(client,f, response, facet_name);
		
		client.close();

		String format;

		format=parser.parseFormat(request);
		if(format.equals("xml"))
		{
			ToXML converter=new ToXML();
			results=converter.convertToXMLFacet(results);
		}
		
		//results="";
    	return results;
        
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
