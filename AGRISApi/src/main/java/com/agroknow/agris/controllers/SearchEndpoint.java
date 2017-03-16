package com.agroknow.agris.controllers;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.HasChildQueryBuilder;
import org.elasticsearch.index.query.HasParentQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermFilterBuilder;
import org.elasticsearch.index.query.TermsFilterBuilder;
import org.elasticsearch.search.SearchHit;
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

import com.agroknow.agris.response.examples.ResponseABSA;
import com.agroknow.agris.utils.AESencr;
import com.agroknow.agris.utils.AGRISMongoDB;
import com.agroknow.agris.utils.BuildSearchResponse;
import com.agroknow.agris.utils.ESClient;
import com.agroknow.agris.utils.GetConfig;
import com.agroknow.agris.utils.ParseGET;
import com.agroknow.agris.utils.ToXML;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin(origins = "*")
@RestController
public class SearchEndpoint {

	@ApiOperation(value = "Search entities containing keyword")
	@RequestMapping( value="/search", method={RequestMethod.GET},
		/*produces={"application/xml","application/json"}*/
			produces="*/*")
	@ApiImplicitParams({
		@ApiImplicitParam(
    			name = "freetext", 
    			value = "search entities based on: title, abstract, author, location, subject", 
    			required = false, 
    			dataType = "string", 
    			paramType = "query", 
    			defaultValue="quantitativa"),
		@ApiImplicitParam(
    			name = "keyword", 
    			value = "keyword to search entities against", 
    			required = false, 
    			dataType = "string", 
    			paramType = "query", 
    			defaultValue="quantitativa"),
		/*@ApiImplicitParam(
    			name = "entity-type-ENR", 
    			value = "limit results by entity type (eg. resource, dataset_software, person, organization, collection)", 
    			required = false, 
    			dataType = "string", 
    			paramType = "query", 
    			defaultValue=""),*/
		@ApiImplicitParam(
    			name = "type", 
    			value = "filter results by type (for resources and datasets/softwares)", 
    			required = false, 
    			dataType = "string", 
    			paramType = "query", 
    			defaultValue="Other"),
		@ApiImplicitParam(
    	    	name = "source", 
    	    	value = "filter results by source (eg: from CIARD RING, or PubMed)", 
    	    	required = false, 
    	    	dataType = "string", 
    	    	paramType = "query", 
    	    	defaultValue="ring.ciard.net"),
		/*@ApiImplicitParam(
    			name = "from-ENR", 
    			value = "filter results by those created after this date", 
    			required = false, 
    			dataType = "string", 
    			paramType = "query", 
    			defaultValue=""),
		@ApiImplicitParam(
    			name = "to-ENR", 
    			value = "filter results by those before this date", 
    			required = false, 
    			dataType = "string", 
    			paramType = "query", 
    			defaultValue=""),*/
		@ApiImplicitParam(
    			name = "subject", 
    			value = "limit results to those having the specified subject(s)", 
    			required = false, 
    			dataType = "string", 
    			paramType = "query", 
    			defaultValue="Estévia"),
		/*@ApiImplicitParam(
    			name = "collection",
    			value = "limit results to those belonging in this collection(s)", 
    			required = false, 
    			dataType = "string", 
    			paramType = "query", 
    			defaultValue="Genetic Resources"),*/
		@ApiImplicitParam(
    			name = "author", 
    			value = "return results with the specified author", 
    			required = false, 
    			dataType = "string", 
    			paramType = "query", 
    			defaultValue="FILHO"),
		@ApiImplicitParam(
    			name = "language", 
    			value = "language of the results", 
    			required = false, 
    			dataType = "string", 
    			paramType = "query", 
    			defaultValue="por"),
		@ApiImplicitParam(
    			name = "fulltext", 
    			value = "fulltext access type ("
    					+ "leave empty to disregard, "
    					+ "any - any kind of http access, "
    					+ "pdf - must have pdf link, "
    					+ "doi - must have doi link)", 
    			required = false, 
    			dataType = "string", 
    			paramType = "query",
    			allowableValues ="any,pdf,doi",
    			defaultValue="any"),
		/*@ApiImplicitParam(
    			name = "location-ENR", 
    			value = "limit the results by a specific location(s)", 
    			required = false, 
    			dataType = "string", 
    			paramType = "query", 
    			defaultValue=""),
		@ApiImplicitParam(
    			name = "relation-ENR", 
    			value = "filter results having this relation", 
    			required = false, 
    			dataType = "string", 
    			paramType = "query", 
    			defaultValue=""),*/
		@ApiImplicitParam(
    			name = "page", 
    			value = "page of the results (0,1...)", 
    			required = false, 
    			dataType = "int", 
    			paramType = "query", 
    			defaultValue="0"),
		@ApiImplicitParam(
    	    	name = "apikey", 
    	    	value = "apikey", 
    	    	required = true, 
    	    	dataType = "string", 
    	    	paramType = "query", 
    	    	defaultValue="agroknow"),
		@ApiImplicitParam(
    			name = "format", 
    			value = "output format", 
    			required = true, 
    			dataType = "string", 
    			paramType = "query",
    			defaultValue="json"),
		@ApiImplicitParam(
    			name = "cache", 
    			value = "use cache", 
    			required = true, 
    			dataType = "boolean", 
    			paramType = "query",
    			defaultValue="true")
	})
	ResponseEntity<String> search_pubag(HttpServletRequest request) throws UnknownHostException { 
		
		double init_time=(double)System.currentTimeMillis()/1000;
		
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
				return new ResponseEntity<String>("{\"error\":\"Api validation Error\"}", response_head, HttpStatus.CREATED);
			}
			
			mongodb.addPoints(apikey, "search", request);
			
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
			
		Client client = ESClient.client;/*new TransportClient(settings)
				.addTransportAddress(new InetSocketTransportAddress("localhost", 9300));*/
		        //.addTransportAddress(new InetSocketTransportAddress("host2", 9300));
		
		String results="";
				
			int page=parser.parsePage(request);
			
			boolean search_parent=false;
			
			BoolQueryBuilder build =QueryBuilders.boolQuery();
			
			QueryBuilder query = null;


			BoolQueryBuilder build_o =QueryBuilders.boolQuery();
			BoolQueryBuilder build_child =QueryBuilders.boolQuery();
			BoolQueryBuilder build_enhanced=QueryBuilders.boolQuery();
			
			
		    List<FilterBuilder> filters=new LinkedList<>();
		    
		    GetConfig config=new GetConfig();
			int fuzzy;
			
			
			float similarity;
			try{
				similarity=Double.valueOf(config.getValue("similarity")).floatValue();
			}
			catch(Exception e)
			{
				similarity=(float) 0.75;
			}
				

			String keywordE=parser.parseKeywordEnhanced(request);
			if(!keywordE.isEmpty())
			{
				//search_parent=true;
				
				int fuzzy_not=0;

				try {
					fuzzy_not = Integer.valueOf(config.getValue("fuzzy_not"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				}
				
				
				try {
					fuzzy = Integer.valueOf(config.getValue("fuzzy_keyword"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy=1;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy=1;
				}
				
				BoolQueryBuilder bool_qN=QueryBuilders.boolQuery();
				/*bool_qN.must(QueryBuilders
						.fuzzyLikeThisQuery("author.person.name",
								"resource.title.value")
						.likeText(keywordE)
						.maxQueryTerms(2));*/
				/*
				bool_qN.should(QueryBuilders
						.fuzzyLikeThisQuery("title.value")
						.likeText(keywordE)
						.maxQueryTerms(2));

				bool_qN.should(QueryBuilders
						.fuzzyLikeThisQuery("abstract.value")
						.likeText(keywordE)
						.maxQueryTerms(2));
				
				bool_qN.should(QueryBuilders
						.fuzzyLikeThisQuery("author.person.name")
						.likeText(keywordE)
						.maxQueryTerms(2));

				bool_qN.should(QueryBuilders
						.fuzzyLikeThisQuery("location.value")
						.likeText(keywordE)
						.maxQueryTerms(2));
				
				bool_qN.should(QueryBuilders
						.fuzzyLikeThisQuery("subject.value")
						.likeText(keywordE)
						.maxQueryTerms(2));
				
				
				build_enhanced.must(bool_qN);
				
				System.out.println(build_enhanced.toString());
				*/
				String or_values[]=keywordE.split("OR");
				BoolQueryBuilder bool_q=QueryBuilders.boolQuery();
				
				for(int j=0;j<or_values.length;j++)
				{
					BoolQueryBuilder bool_inner=QueryBuilders.boolQuery();
					String values[]=or_values[j].split("AND");
					
					for(int i=0;i<values.length;i++)
					{

						//System.out.println(values[i]);
						boolean has_not=false;
						
						BoolQueryBuilder bool_beta=QueryBuilders.boolQuery();
						
						if(values[i].contains("NOT"))
						{
							has_not=true;
							values[i]=values[i].replace("NOT", "");
						}
						
						if(!has_not)
						{
							if(fuzzy==1)
							{
								bool_beta.should(QueryBuilders
										.fuzzyLikeThisQuery("resource.dct:title.value")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(values[i])
										.maxQueryTerms(2));

								bool_beta.should(QueryBuilders
										.fuzzyLikeThisQuery("resource.bibo:abstract.value")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(values[i])
										.maxQueryTerms(2));
								
								bool_beta.should(QueryBuilders
										.fuzzyLikeThisQuery("creator.foaf:Person.name")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(values[i])
										.maxQueryTerms(2));

								bool_beta.should(QueryBuilders
										.fuzzyLikeThisQuery("location.value")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(values[i])
										.maxQueryTerms(2));
								
								bool_beta.should(QueryBuilders
										.fuzzyLikeThisQuery("dc:subject.value")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(values[i])
										.maxQueryTerms(2));
								
								
								bool_inner.must(bool_beta);
								
							}
							else
							{
								bool_beta.should(QueryBuilders
										.queryString(values[i])
										.field("resource.dct:title.value"));

								bool_beta.should(QueryBuilders
										.queryString(values[i])
										.field("resource.bibo:abstract.value"));
								
								bool_beta.should(QueryBuilders
										.queryString(values[i])
										.field("creator.foaf:Person.name"));

								bool_beta.should(QueryBuilders
										.queryString(values[i])
										.field("location.value"));
								
								bool_beta.should(QueryBuilders
										.queryString(values[i])
										.field("dc:subject.value"));
								
								
								bool_inner.must(bool_beta);
								
							}
						}
						else
						{
							if(fuzzy_not==1)
							{
								
								bool_beta.mustNot(QueryBuilders
										.fuzzyLikeThisQuery("resource.dct:title.value")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(values[i])
										.maxQueryTerms(2));

								bool_beta.mustNot(QueryBuilders
										.fuzzyLikeThisQuery("resource.bibo:abstract.value")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(values[i])
										.maxQueryTerms(2));
								
								bool_beta.mustNot(QueryBuilders
										.fuzzyLikeThisQuery("creator.foaf:Person.name")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(values[i])
										.maxQueryTerms(2));

								bool_beta.mustNot(QueryBuilders
										.fuzzyLikeThisQuery("location.value")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(values[i])
										.maxQueryTerms(2));
								
								bool_beta.mustNot(QueryBuilders
										.fuzzyLikeThisQuery("dc:subject.value")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(values[i])
										.maxQueryTerms(2));
								
								
								bool_inner.must(bool_beta);
								
							}
							else
							{

								bool_beta.mustNot(QueryBuilders
										.queryString(values[i])
										.field("resource.dct:title.value"));

								bool_beta.mustNot(QueryBuilders
										.queryString(values[i])
										.field("resource.bibo:abstract.value"));
								
								bool_beta.mustNot(QueryBuilders
										.queryString(values[i])
										.field("creator.foaf:Person.name"));

								bool_beta.mustNot(QueryBuilders
										.queryString(values[i])
										.field("location.value"));
								
								bool_beta.mustNot(QueryBuilders
										.queryString(values[i])
										.field("dc:subject.value"));
								
								
								bool_inner.must(bool_beta);
							}
						}
						
					}
					
					bool_q.should(bool_inner);
				}
				//build_o.must(bool_q);
				
				//System.out.println(bool_q.toString());
				
				build_enhanced.must(bool_q);
			}

			String keyword=parser.parseKeyword(request);
			if(!keyword.isEmpty())
			{
				search_parent=true;
				
				int fuzzy_not=0;

				try {
					fuzzy_not = Integer.valueOf(config.getValue("fuzzy_not"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				}
				
				
				try {
					fuzzy = Integer.valueOf(config.getValue("fuzzy_keyword"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy=1;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy=1;
				}
				
				String or_values[]=keyword.split("OR");
				BoolQueryBuilder bool_q=QueryBuilders.boolQuery();
				
				for(int j=0;j<or_values.length;j++)
				{
					BoolQueryBuilder bool_inner=QueryBuilders.boolQuery();
					String values[]=or_values[j].split("AND");
					
					for(int i=0;i<values.length;i++)
					{

						boolean has_not=false;
						
						if(values[i].contains("NOT"))
						{
							has_not=true;
							values[i]=values[i].replace("NOT", "");
						}
						
						if(!has_not)
						{
							if(fuzzy==1)
							{
								bool_inner.must(QueryBuilders
										.fuzzyLikeThisQuery("resource.dct:title.value",
												"resource.bibo:abstract.value")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(values[i])
										.maxQueryTerms(2));
								
							}
							else
							{
								bool_inner.must(QueryBuilders
										.queryString(values[i])
										.field("resource.dct:title.value")
										.field("resource.bibo:abstract.value"));
							}
						}
						else
						{
							if(fuzzy_not==1)
							{
								bool_inner.mustNot(QueryBuilders
										.fuzzyLikeThisQuery("resource.dct:title.value",
												"resource.abstract.value")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(values[i])
										.maxQueryTerms(2));
							}
							else
							{
								bool_inner.mustNot(QueryBuilders
										.queryString(values[i])
										.field("resource.dct:title.value")
										.field("resource.bibo:abstract.value"));
							}
						}
						/*build_o.must(QueryBuilders
							.queryString(values[i])
							.field("resource.title.value")
							.field("resource.abstract.value")
							);*/
					}
					
					bool_q.should(bool_inner);
				}
				build_o.must(bool_q);
			}
			
			String entity_type=parser.parseEntityType(request);
			if(!entity_type.isEmpty())
			{
				search_parent=true;
				
				BoolQueryBuilder bool_q=QueryBuilders.boolQuery();
				String or_values[]=entity_type.split("OR");
				for(int i=0;i<or_values.length;i++)
				{
					String and_values[]=or_values[i].split("AND");
					BoolQueryBuilder bool_inner=QueryBuilders.boolQuery();
					
					for(int j=0;j<and_values.length;j++)
					{
						bool_inner.must(QueryBuilders.termQuery("resource.dct:type", and_values[j]));
					}
					bool_q.should(bool_inner);
				}
				build_o.must(bool_q);
				
				/*String values[]=entity_type.split("AND");
				
				for(int i=0;i<values.length;i++)
					build_o.must(QueryBuilders.termQuery("resource.type", values[i]));*/
			}

			String type=parser.parseType(request);
			if(!type.isEmpty())
			{
				try {
					fuzzy = Integer.valueOf(config.getValue("fuzzy_type"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy=1;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy=1;
				}

				int fuzzy_not=0;

				try {
					fuzzy_not = Integer.valueOf(config.getValue("fuzzy_not"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				}
				
				
				BoolQueryBuilder bool_q=QueryBuilders.boolQuery();
				String or_values[]=type.split("OR");
				for(int i=0;i<or_values.length;i++)
				{
					String and_values[]=or_values[i].split("AND");
					BoolQueryBuilder bool_inner=QueryBuilders.boolQuery();
					
					for(int j=0;j<and_values.length;j++)
					{
						
						boolean has_not=false;
						
						if(and_values[j].contains("NOT"))
						{
							has_not=true;
							and_values[j]=and_values[j].replace("NOT", "");
						}
						
						if(!has_not)
						{
							if(fuzzy!=1)
								bool_inner.must(QueryBuilders.termQuery("dct:type", and_values[j]));
							else
								bool_inner.must(QueryBuilders
									.fuzzyLikeThisQuery("dct:type")
									.fuzziness(Fuzziness.fromSimilarity((float) similarity))
									.likeText(and_values[j])
									.maxQueryTerms(2)
									);
						}
						else
						{
							if(fuzzy_not!=1)
								bool_inner.mustNot(QueryBuilders.termQuery("dct:type", and_values[j]));
							else
								bool_inner.mustNot(QueryBuilders
									.fuzzyLikeThisQuery("dct:type")
									.fuzziness(Fuzziness.fromSimilarity((float) similarity))
									.likeText(and_values[j])
									.maxQueryTerms(2)
									);
						}
						
					}
					bool_q.should(bool_inner);
				}
				build_child.must(bool_q);
				
			}
			String source=parser.parseSource(request);
			if(!source.isEmpty())
			{
				try {
					fuzzy = Integer.valueOf(config.getValue("fuzzy_source"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy=1;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy=1;
				}

				int fuzzy_not=0;

				try {
					fuzzy_not = Integer.valueOf(config.getValue("fuzzy_not"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				}
				
				
				BoolQueryBuilder bool_q=QueryBuilders.boolQuery();
				String or_values[]=source.split("OR");
				for(int i=0;i<or_values.length;i++)
				{
					String and_values[]=or_values[i].split("AND");
					BoolQueryBuilder bool_inner=QueryBuilders.boolQuery();
					
					for(int j=0;j<and_values.length;j++)
					{
						
						boolean has_not=false;
						
						if(and_values[j].contains("NOT"))
						{
							has_not=true;
							and_values[j]=and_values[j].replace("NOT", "");
						}
						
						if(!has_not)
						{
							if(fuzzy!=1)
								bool_inner.must(QueryBuilders.termQuery("dct:source.rdf:resource", and_values[j]));
							else
								bool_inner.must(QueryBuilders
									.fuzzyLikeThisQuery("dct:source.rdf:resource")
									.fuzziness(Fuzziness.fromSimilarity((float) similarity))
									.likeText(and_values[j])
									.maxQueryTerms(2)
									);
						}
						else
						{
							if(fuzzy_not!=1)
								bool_inner.mustNot(QueryBuilders.termQuery("dct:source.rdf:resource", and_values[j]));
							else
								bool_inner.mustNot(QueryBuilders
									.fuzzyLikeThisQuery("dct:source.rdf:resource")
									.fuzziness(Fuzziness.fromSimilarity((float) similarity))
									.likeText(and_values[j])
									.maxQueryTerms(2)
									);
						}
						
					}
					bool_q.should(bool_inner);
				}
				build_child.must(bool_q);
				
			}
				

			String author=parser.parseAuthor(request);
			if(!author.isEmpty())
			{
				try {
					fuzzy = Integer.valueOf(config.getValue("fuzzy_author"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy=1;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy=1;
				}
				
				int fuzzy_not=0;

				try {
					fuzzy_not = Integer.valueOf(config.getValue("fuzzy_not"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				}
				
				BoolQueryBuilder bool_q=QueryBuilders.boolQuery();
				String or_values[]=author.split("OR");
				for(int i=0;i<or_values.length;i++)
				{
					String and_values[]=or_values[i].split("AND");
					BoolQueryBuilder bool_inner=QueryBuilders.boolQuery();
					
					for(int j=0;j<and_values.length;j++)
					{

						boolean has_not=false;
						
						if(and_values[j].contains("NOT"))
						{
							has_not=true;
							and_values[j]=and_values[j].replace("NOT", "");
						}
						
						if(!has_not)
						{
							if(fuzzy!=1)
								bool_inner.must(QueryBuilders.termQuery("dct:creator.foaf:Person.foaf:name", 
										and_values[j]));
							else
								bool_inner.must(QueryBuilders
									.fuzzyLikeThisQuery("dct:creator.foaf:Person.foaf:name")
									.fuzziness(Fuzziness.fromSimilarity((float) similarity))
									.likeText(and_values[j])
									.maxQueryTerms(25)
									);
						}
						else
						{
							if(fuzzy_not==0)
								bool_inner.mustNot(QueryBuilders.termQuery("dct:creator.foaf:Person.foaf:name", and_values[j]));
							else
								bool_inner.mustNot(QueryBuilders
									.fuzzyLikeThisQuery("dct:creator.foaf:Person.foaf:name")
									.fuzziness(Fuzziness.fromSimilarity((float) similarity))
									.likeText(and_values[j])
									.maxQueryTerms(2)
									);
						}
					}
					bool_q.should(bool_inner);
				}
				build_child.must(bool_q);
				
				
				/*String values[]=author.split("AND");
				
				for(int i=0;i<values.length;i++)
					filters.add(FilterBuilders.termFilter("author.person.name",values[i]));*/
			}	
			
			String subject=parser.parseSubject(request);
			if(!subject.isEmpty())
			{
				search_parent=true;
				
				try {
					fuzzy = Integer.valueOf(config.getValue("fuzzy_subject"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy=1;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy=1;
				}

				int fuzzy_not=0;

				try {
					fuzzy_not = Integer.valueOf(config.getValue("fuzzy_not"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				}
				
				BoolQueryBuilder bool_q=QueryBuilders.boolQuery();
				String or_values[]=subject.split("OR");
				for(int i=0;i<or_values.length;i++)
				{
					String and_values[]=or_values[i].split("AND");
					BoolQueryBuilder bool_inner=QueryBuilders.boolQuery();
					
					for(int j=0;j<and_values.length;j++)
					{
						boolean has_not=false;
						
						if(and_values[j].contains("NOT"))
						{
							has_not=true;
							and_values[j]=and_values[j].replace("NOT", "");
						}
						
						if(!has_not)
						{
							if(fuzzy!=1)
								bool_inner.must(QueryBuilders.termQuery("dc:subject.value", and_values[j]));
							else
								bool_inner.must(QueryBuilders
									.fuzzyLikeThisQuery("dc:subject.value")
									.fuzziness(Fuzziness.fromSimilarity((float) similarity))
									.likeText(and_values[j])
									.maxQueryTerms(2)
									);
						}
						else
						{
							if(fuzzy_not!=1)
								bool_inner.mustNot(QueryBuilders.termQuery("dc:subject.value", and_values[j]));
							else
								bool_inner.mustNot(QueryBuilders
									.fuzzyLikeThisQuery("dc:subject.value")
									.fuzziness(Fuzziness.fromSimilarity((float) similarity))
									.likeText(and_values[j])
									.maxQueryTerms(2)
									);
						}
					}
					bool_q.should(bool_inner);
				}
				build_o.must(bool_q);
				
				/*
				String values[]=subject.split("AND");
				
				for(int i=0;i<values.length;i++)
					build_o.must(QueryBuilders.termQuery("subject.value", values[i]));*/
			}
				//build_o.must(QueryBuilders.matchQuery("subject.value", subject));
			
			String lang=parser.parseLanguage(request);
			if(!lang.isEmpty())
			{
				search_parent=true;
				
				BoolQueryBuilder bool_q=QueryBuilders.boolQuery();
				String or_values[]=lang.split("OR");
				for(int i=0;i<or_values.length;i++)
				{
					String and_values[]=or_values[i].split("AND");
					BoolQueryBuilder bool_inner=QueryBuilders.boolQuery();
					
					for(int j=0;j<and_values.length;j++)
					{
						bool_inner.must(QueryBuilders.termQuery("dct:language", and_values[j]));
					}
					bool_q.should(bool_inner);
				}
				build_o.must(bool_q);
				
				/*String values[]=lang.split("AND");
				
				for(int i=0;i<values.length;i++)
					build_o.must(QueryBuilders.termQuery("language.value", values[i]));*/
			}
				//build_o.must(QueryBuilders.matchQuery("language.value", lang));
			
			String location=parser.parseLocation(request);
			if(!location.isEmpty())
			{
				try {
					fuzzy = Integer.valueOf(config.getValue("fuzzy_location"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy=1;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy=1;
				}

				int fuzzy_not=0;

				try {
					fuzzy_not = Integer.valueOf(config.getValue("fuzzy_not"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				}
				
				BoolQueryBuilder bool_q=QueryBuilders.boolQuery();
				String or_values[]=location.split("OR");
				for(int i=0;i<or_values.length;i++)
				{
					String and_values[]=or_values[i].split("AND");
					BoolQueryBuilder bool_inner=QueryBuilders.boolQuery();
					
					for(int j=0;j<and_values.length;j++)
					{
						boolean has_not=false;
						
						if(and_values[j].contains("NOT"))
						{
							has_not=true;
							and_values[j]=and_values[j].replace("NOT", "");
						}

						if(!has_not)
						{
							if(fuzzy!=1)
								bool_inner.must(QueryBuilders.termQuery("location.value", and_values[j]));
							else
								bool_inner.must(QueryBuilders
									.fuzzyLikeThisQuery("location.value")
									.fuzziness(Fuzziness.fromSimilarity((float) similarity))
									.likeText(and_values[j])
									.maxQueryTerms(2)
									);
						}
						else
						{
							if(fuzzy_not!=1)
								bool_inner.mustNot(QueryBuilders.termQuery("location.value", and_values[j]));
							else
								bool_inner.mustNot(QueryBuilders
									.fuzzyLikeThisQuery("location.value")
									.fuzziness(Fuzziness.fromSimilarity((float) similarity))
									.likeText(and_values[j])
									.maxQueryTerms(2)
									);
						}
					}
					bool_q.should(bool_inner);
				}
				build_child.must(bool_q);
				
				
				/*String values[]=location.split("AND");
				
				for(int i=0;i<values.length;i++)
					filters.add(FilterBuilders.termFilter("location.value",values[i]));*/
			}
				//filters.add(FilterBuilders.termFilter("location.value",location));
				//build.must(QueryBuilders.matchQuery("location.value", location));
			
			String relation=parser.parseRelation(request);
			if(!relation.isEmpty())
			{
				try {
					fuzzy = Integer.valueOf(config.getValue("fuzzy_relation"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy=1;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy=1;
				}
				
				BoolQueryBuilder bool_q=QueryBuilders.boolQuery();
				String or_values[]=relation.split("OR");
				for(int i=0;i<or_values.length;i++)
				{
					String and_values[]=or_values[i].split("AND");
					BoolQueryBuilder bool_inner=QueryBuilders.boolQuery();
					
					for(int j=0;j<and_values.length;j++)
					{
						if(fuzzy==1)
							bool_inner.must(QueryBuilders
								.fuzzyLikeThisQuery("relation")
								.fuzziness(Fuzziness.fromSimilarity((float) similarity))
								.likeText(and_values[j])
								.maxQueryTerms(2)
								);
						else
							bool_inner.must(QueryBuilders.termQuery("relation", and_values[j]));
					}
					bool_q.should(bool_inner);
				}
				build_o.must(bool_q);
				
				
				/*String values[]=relation.split("AND");
				
				for(int i=0;i<values.length;i++)
					filters.add(FilterBuilders.termFilter("relation",values[i]));*/
			}
				//filters.add(FilterBuilders.termFilter("relation",relation));
				//build.must(QueryBuilders.matchQuery("relation", relation));

			String from_date=parser.parseFromDate(request);
			String to_date=parser.parseToDate(request);
			if(!from_date.isEmpty() || !to_date.isEmpty())
			{
								
				/*if(from_date.isEmpty())
					from_date=to_date;
				if(to_date.isEmpty())
					to_date=from_date;*/
				
				if(from_date.isEmpty())
					from_date="50";
				if(to_date.isEmpty())
					to_date="9999";
				
				//if(from_date.equals(to_date))
				//{
					from_date+="-01-01";
					to_date+="-12-31";
				//}
				
				build_child.must(
						QueryBuilders
						.rangeQuery("date")
						.gte(from_date)
						.lte(to_date)
						);	
					
				/*filters.add(FilterBuilders
						.rangeFilter("date")
						.gte(from_date)
						.lte(to_date));*/
			}
			  

			String fulltext=parser.parseFullText(request);
			if(!fulltext.isEmpty())
			{
				search_parent=true;
				
				//System.out.println(fulltext);
				
				BoolQueryBuilder bool_q=QueryBuilders.boolQuery();
				

					BoolQueryBuilder bool_inner=QueryBuilders.boolQuery();
					
					
						if(fulltext.equals(("any")))
						{
								bool_inner.must(
										QueryBuilders.wildcardQuery("bibo:uri", "http*")
										);
						}
						else if(fulltext.equals("pdf"))
						{
							bool_inner.must(
									QueryBuilders.wildcardQuery("bibo:uri", "*pdf")
									);
						}
						else if(fulltext.equals("doi"))
						{
							bool_inner.must(
									QueryBuilders.wildcardQuery("bibo:doi", "*")
									);
						}
					
					bool_q.should(bool_inner);
				
				build_o.must(bool_q);
			}
				//build_o.must(QueryBuilders.matchQuery("language.value", lang));
			
			
			
			
			
			BuildSearchResponse builder=new BuildSearchResponse();
			//results=builder.buildFrom(client,build_o,filters,page,search_parent);
			
			results=builder.buildFrom_beta(client,build_o,build_child,
					page,search_parent, build_enhanced, request);

		//client.close();
		
		if(format.equals("xml"))
		{
			ToXML converter=new ToXML();
			results=converter.convertToXMLFreeText(results);
		}
		
		if(mongo_up)
			mongodb.cacheResponse(request, results);
		
		//mongodb.
		
		//results="";
		//return results;
		
		return new ResponseEntity<String>(results, response_head, HttpStatus.CREATED);
	    
	}
	
	@ApiOperation(value = "Get facets of search query")
	@RequestMapping( value="/search-facets", method={RequestMethod.GET},produces="text/plain")
	@ApiImplicitParams({
		@ApiImplicitParam(
    			name = "fid", 
    			value = "the facet id returned by the search endpoint", 
    			required = true, 
    			dataType = "string", 
    			paramType = "query", 
    			defaultValue="woLr1W9KFEhns1jCYpjkTjg8YCb6nG8T4mcTjU1QsTCduanDejr7DLYFNcgrFxSIXZdHR3tH/y30Epr6spOqlg=="),
		/*@ApiImplicitParam(
    			name = "fulltext", 
    			value = "fulltext access type ("
    					+ "leave empty to disregard, "
    					+ "any - any kind of http access, "
    					+ "pdf - must have pdf link, "
    					+ "doi - must have doi link)", 
    			required = false, 
    			dataType = "string", 
    			paramType = "query",
    			allowableValues ="any,pdf,doi",
    			defaultValue="any"),*/
		@ApiImplicitParam(
    			name = "page", 
    			value = "page of the results (0,1...)", 
    			required = false, 
    			dataType = "int", 
    			paramType = "query", 
    			defaultValue="0"),
		@ApiImplicitParam(
    	    	name = "apikey", 
    	    	value = "apikey", 
    	    	required = true, 
    	    	dataType = "string", 
    	    	paramType = "query", 
    	    	defaultValue="agroknow"),
		@ApiImplicitParam(
    			name = "format", 
    			value = "output format", 
    			required = true, 
    			dataType = "string", 
    			paramType = "query",
    			defaultValue="json"),
		@ApiImplicitParam(
    			name = "cache", 
    			value = "use cache", 
    			required = true, 
    			dataType = "boolean", 
    			paramType = "query",
    			defaultValue="true")
	})
	ResponseEntity<String> search_facets(HttpServletRequest request) throws UnknownHostException { 
		
		double init_time=(double)System.currentTimeMillis()/1000;
		
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
				return new ResponseEntity<String>(
						"{\"error\":\"Api validation Error\"}",response_head,HttpStatus.CREATED);
			}
			
			mongodb.addPoints(apikey, "search-facets", request);
			
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
						
						return new ResponseEntity<String>(
								cached,response_head,HttpStatus.CREATED);
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
			//return "{\"error\":\"unable to process request please try again later\"}";
			
			return new ResponseEntity<String>(
					"{\"error\":\"unable to process request please try again later\"}",response_head,HttpStatus.CREATED);
		}
		
		String fid="";
		String search_query="";
		try
		{
			fid = parser.parseFID(request);
			search_query=mongodb.getFacetQuery(fid);
			
			System.out.println("Search query1:"+search_query);
			
		}
		catch(Exception e)
		{
			AESencr aes = new AESencr();
			try 
			{
				search_query=aes.decrypt(fid);
				System.out.println("Search query2:"+search_query);
			} 
			catch (Exception e1) 
			{
				return new ResponseEntity<String>(
						"{\"error\":\"unable to process request. have you done such a search query?\"}",response_head,HttpStatus.CREATED);
			}
			//return "{\"error\":\"unable to process request. have you done such a search query?\"}";
		}
		
		//System.out.println("i reached here");
		
		Settings settings = ImmutableSettings.settingsBuilder()
		        .put("cluster.name", "agroknow").build();
			
		Client client = ESClient.client;/*new TransportClient(settings)
				.addTransportAddress(new InetSocketTransportAddress("localhost", 9300));*/
		        //.addTransportAddress(new InetSocketTransportAddress("host2", 9300));
		
		String results="";
				
			int page=parser.parsePage(request);
			
			boolean search_parent=false;
			
			BoolQueryBuilder build =QueryBuilders.boolQuery();
			
			QueryBuilder query = null;

			BoolQueryBuilder build_o =QueryBuilders.boolQuery();
			BoolQueryBuilder build_child =QueryBuilders.boolQuery();
			BoolQueryBuilder build_enhanced=QueryBuilders.boolQuery();
			
		    List<FilterBuilder> filters=new LinkedList<>();
		    
		    GetConfig config=new GetConfig();
			int fuzzy;
			
			
			float similarity;
			try{
				similarity=Double.valueOf(config.getValue("similarity")).floatValue();
			}
			catch(Exception e)
			{
				similarity=(float) 0.75;
			}				

			String keywordE=parser.parseKeywordEnhanced(search_query);
			if(!keywordE.isEmpty())
			{
				//search_parent=true;
				
				int fuzzy_not=0;

				try {
					fuzzy_not = Integer.valueOf(config.getValue("fuzzy_not"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				}
				
				
				try {
					fuzzy = Integer.valueOf(config.getValue("fuzzy_keyword"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy=1;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy=1;
				}
				
				BoolQueryBuilder bool_qN=QueryBuilders.boolQuery();
				
				String or_values[]=keywordE.split("OR");
				BoolQueryBuilder bool_q=QueryBuilders.boolQuery();
				
				for(int j=0;j<or_values.length;j++)
				{
					BoolQueryBuilder bool_inner=QueryBuilders.boolQuery();
					String values[]=or_values[j].split("AND");
					
					for(int i=0;i<values.length;i++)
					{

						//System.out.println(values[i]);
						boolean has_not=false;
						
						BoolQueryBuilder bool_beta=QueryBuilders.boolQuery();
						
						if(values[i].contains("NOT"))
						{
							has_not=true;
							values[i]=values[i].replace("NOT", "");
						}
						
						if(!has_not)
						{
							if(fuzzy==1)
							{
								bool_beta.should(QueryBuilders
										.fuzzyLikeThisQuery("resource.dct:title.value")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(values[i])
										.maxQueryTerms(2));

								bool_beta.should(QueryBuilders
										.fuzzyLikeThisQuery("resource.bibo:abstract.value")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(values[i])
										.maxQueryTerms(2));
								
								bool_beta.should(QueryBuilders
										.fuzzyLikeThisQuery("creator.foaf:Person.name")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(values[i])
										.maxQueryTerms(2));

								bool_beta.should(QueryBuilders
										.fuzzyLikeThisQuery("location.value")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(values[i])
										.maxQueryTerms(2));
								
								bool_beta.should(QueryBuilders
										.fuzzyLikeThisQuery("dc:subject.value")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(values[i])
										.maxQueryTerms(2));
								
								
								bool_inner.must(bool_beta);
								
							}
							else
							{
								bool_beta.should(QueryBuilders
										.queryString(values[i])
										.field("resource.dct:title.value"));

								bool_beta.should(QueryBuilders
										.queryString(values[i])
										.field("resource.bibo:abstract.value"));
								
								bool_beta.should(QueryBuilders
										.queryString(values[i])
										.field("creator.foaf:Person.name"));

								bool_beta.should(QueryBuilders
										.queryString(values[i])
										.field("location.value"));
								
								bool_beta.should(QueryBuilders
										.queryString(values[i])
										.field("dc:subject.value"));
								
								
								bool_inner.must(bool_beta);
								
							}
						}
						else
						{
							if(fuzzy_not==1)
							{
								
								bool_beta.mustNot(QueryBuilders
										.fuzzyLikeThisQuery("resource.dct:title.value")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(values[i])
										.maxQueryTerms(2));

								bool_beta.mustNot(QueryBuilders
										.fuzzyLikeThisQuery("resource.bibo:abstract.value")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(values[i])
										.maxQueryTerms(2));
								
								bool_beta.mustNot(QueryBuilders
										.fuzzyLikeThisQuery("creator.foaf:Person.name")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(values[i])
										.maxQueryTerms(2));

								bool_beta.mustNot(QueryBuilders
										.fuzzyLikeThisQuery("location.value")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(values[i])
										.maxQueryTerms(2));
								
								bool_beta.mustNot(QueryBuilders
										.fuzzyLikeThisQuery("dc:subject.value")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(values[i])
										.maxQueryTerms(2));
								
								
								bool_inner.must(bool_beta);
								
							}
							else
							{

								bool_beta.mustNot(QueryBuilders
										.queryString(values[i])
										.field("resource.dct:title.value"));

								bool_beta.mustNot(QueryBuilders
										.queryString(values[i])
										.field("resource.bibo:abstract.value"));
								
								bool_beta.mustNot(QueryBuilders
										.queryString(values[i])
										.field("creator.foaf:Person.name"));

								bool_beta.mustNot(QueryBuilders
										.queryString(values[i])
										.field("location.value"));
								
								bool_beta.mustNot(QueryBuilders
										.queryString(values[i])
										.field("dc:subject.value"));
								
								
								bool_inner.must(bool_beta);
							}
						}
						
					}
					
					bool_q.should(bool_inner);
				}
				//build_o.must(bool_q);
				
				//System.out.println(bool_q.toString());
				
				build_enhanced.must(bool_q);
			}

			String keyword=parser.parseKeyword(search_query);
			if(!keyword.isEmpty())
			{
				search_parent=true;
				
				int fuzzy_not=0;

				try {
					fuzzy_not = Integer.valueOf(config.getValue("fuzzy_not"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				}
				
				
				try {
					fuzzy = Integer.valueOf(config.getValue("fuzzy_keyword"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy=1;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy=1;
				}
				
				String or_values[]=keyword.split("OR");
				BoolQueryBuilder bool_q=QueryBuilders.boolQuery();
				
				for(int j=0;j<or_values.length;j++)
				{
					BoolQueryBuilder bool_inner=QueryBuilders.boolQuery();
					String values[]=or_values[j].split("AND");
					
					for(int i=0;i<values.length;i++)
					{

						boolean has_not=false;
						
						if(values[i].contains("NOT"))
						{
							has_not=true;
							values[i]=values[i].replace("NOT", "");
						}
						
						if(!has_not)
						{
							if(fuzzy==1)
							{
								bool_inner.must(QueryBuilders
										.fuzzyLikeThisQuery("resource.dct:title.value",
												"resource.bibo:abstract.value")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(values[i])
										.maxQueryTerms(2));
								
							}
							else
							{
								bool_inner.must(QueryBuilders
										.queryString(values[i])
										.field("resource.dct:title.value")
										.field("resource.bibo:abstract.value"));
							}
						}
						else
						{
							if(fuzzy_not==1)
							{
								bool_inner.mustNot(QueryBuilders
										.fuzzyLikeThisQuery("resource.dct:title.value",
												"resource.abstract.value")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(values[i])
										.maxQueryTerms(2));
							}
							else
							{
								bool_inner.mustNot(QueryBuilders
										.queryString(values[i])
										.field("resource.dct:title.value")
										.field("resource.bibo:abstract.value"));
							}
						}
						/*build_o.must(QueryBuilders
							.queryString(values[i])
							.field("resource.title.value")
							.field("resource.abstract.value")
							);*/
					}
					
					bool_q.should(bool_inner);
				}
				build_o.must(bool_q);
			}
			
			String type=parser.parseType(search_query);
			if(!type.isEmpty())
			{
				try {
					fuzzy = Integer.valueOf(config.getValue("fuzzy_type"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy=1;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy=1;
				}

				int fuzzy_not=0;

				try {
					fuzzy_not = Integer.valueOf(config.getValue("fuzzy_not"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				}
				
				
				BoolQueryBuilder bool_q=QueryBuilders.boolQuery();
				String or_values[]=type.split("OR");
				for(int i=0;i<or_values.length;i++)
				{
					String and_values[]=or_values[i].split("AND");
					BoolQueryBuilder bool_inner=QueryBuilders.boolQuery();
					
					for(int j=0;j<and_values.length;j++)
					{
						
						boolean has_not=false;
						
						if(and_values[j].contains("NOT"))
						{
							has_not=true;
							and_values[j]=and_values[j].replace("NOT", "");
						}
						
						if(!has_not)
						{
							if(fuzzy!=1)
								bool_inner.must(QueryBuilders.termQuery("dct:type", and_values[j]));
							else
								bool_inner.must(QueryBuilders
									.fuzzyLikeThisQuery("dct:type")
									.fuzziness(Fuzziness.fromSimilarity((float) similarity))
									.likeText(and_values[j])
									.maxQueryTerms(2)
									);
						}
						else
						{
							if(fuzzy_not!=1)
								bool_inner.mustNot(QueryBuilders.termQuery("dct:type", and_values[j]));
							else
								bool_inner.mustNot(QueryBuilders
									.fuzzyLikeThisQuery("dct:type")
									.fuzziness(Fuzziness.fromSimilarity((float) similarity))
									.likeText(and_values[j])
									.maxQueryTerms(2)
									);
						}
						
					}
					bool_q.should(bool_inner);
				}
				build_child.must(bool_q);
				
			}
			String source=parser.parseSource(search_query);
			if(!source.isEmpty())
			{
				try {
					fuzzy = Integer.valueOf(config.getValue("fuzzy_source"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy=1;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy=1;
				}

				int fuzzy_not=0;

				try {
					fuzzy_not = Integer.valueOf(config.getValue("fuzzy_not"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				}
				
				
				BoolQueryBuilder bool_q=QueryBuilders.boolQuery();
				String or_values[]=source.split("OR");
				for(int i=0;i<or_values.length;i++)
				{
					String and_values[]=or_values[i].split("AND");
					BoolQueryBuilder bool_inner=QueryBuilders.boolQuery();
					
					for(int j=0;j<and_values.length;j++)
					{
						
						boolean has_not=false;
						
						if(and_values[j].contains("NOT"))
						{
							has_not=true;
							and_values[j]=and_values[j].replace("NOT", "");
						}
						
						if(!has_not)
						{
							if(fuzzy!=1)
								bool_inner.must(QueryBuilders.termQuery("dct:source.rdf:resource", and_values[j]));
							else
								bool_inner.must(QueryBuilders
									.fuzzyLikeThisQuery("dct:source.rdf:resource")
									.fuzziness(Fuzziness.fromSimilarity((float) similarity))
									.likeText(and_values[j])
									.maxQueryTerms(2)
									);
						}
						else
						{
							if(fuzzy_not!=1)
								bool_inner.mustNot(QueryBuilders.termQuery("dct:source.rdf:resource", and_values[j]));
							else
								bool_inner.mustNot(QueryBuilders
									.fuzzyLikeThisQuery("dct:source.rdf:resource")
									.fuzziness(Fuzziness.fromSimilarity((float) similarity))
									.likeText(and_values[j])
									.maxQueryTerms(2)
									);
						}
						
					}
					bool_q.should(bool_inner);
				}
				build_child.must(bool_q);
				
			}
				

			String author=parser.parseAuthor(search_query);
			if(!author.isEmpty())
			{
				try {
					fuzzy = Integer.valueOf(config.getValue("fuzzy_author"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy=1;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy=1;
				}
				
				int fuzzy_not=0;

				try {
					fuzzy_not = Integer.valueOf(config.getValue("fuzzy_not"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				}
				
				BoolQueryBuilder bool_q=QueryBuilders.boolQuery();
				String or_values[]=author.split("OR");
				for(int i=0;i<or_values.length;i++)
				{
					String and_values[]=or_values[i].split("AND");
					BoolQueryBuilder bool_inner=QueryBuilders.boolQuery();
					
					for(int j=0;j<and_values.length;j++)
					{

						boolean has_not=false;
						
						if(and_values[j].contains("NOT"))
						{
							has_not=true;
							and_values[j]=and_values[j].replace("NOT", "");
						}
						
						if(!has_not)
						{
							if(fuzzy!=1)
								bool_inner.must(QueryBuilders.termQuery("dct:creator.foaf:Person.foaf:name", 
										and_values[j]));
							else
								bool_inner.must(QueryBuilders
									.fuzzyLikeThisQuery("dct:creator.foaf:Person.foaf:name")
									.fuzziness(Fuzziness.fromSimilarity((float) similarity))
									.likeText(and_values[j])
									.maxQueryTerms(25)
									);
						}
						else
						{
							if(fuzzy_not==0)
								bool_inner.mustNot(QueryBuilders.termQuery("dct:creator.foaf:Person.foaf:name", and_values[j]));
							else
								bool_inner.mustNot(QueryBuilders
									.fuzzyLikeThisQuery("dct:creator.foaf:Person.foaf:name")
									.fuzziness(Fuzziness.fromSimilarity((float) similarity))
									.likeText(and_values[j])
									.maxQueryTerms(2)
									);
						}
					}
					bool_q.should(bool_inner);
				}
				build_child.must(bool_q);
				
				
				/*String values[]=author.split("AND");
				
				for(int i=0;i<values.length;i++)
					filters.add(FilterBuilders.termFilter("author.person.name",values[i]));*/
			}	

			String subject=parser.parseSubject(search_query);
			if(!subject.isEmpty())
			{
				search_parent=true;
				
				try {
					fuzzy = Integer.valueOf(config.getValue("fuzzy_subject"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy=1;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy=1;
				}

				int fuzzy_not=0;

				try {
					fuzzy_not = Integer.valueOf(config.getValue("fuzzy_not"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				}
				
				BoolQueryBuilder bool_q=QueryBuilders.boolQuery();
				String or_values[]=subject.split("OR");
				for(int i=0;i<or_values.length;i++)
				{
					String and_values[]=or_values[i].split("AND");
					BoolQueryBuilder bool_inner=QueryBuilders.boolQuery();
					
					for(int j=0;j<and_values.length;j++)
					{
						boolean has_not=false;
						
						if(and_values[j].contains("NOT"))
						{
							has_not=true;
							and_values[j]=and_values[j].replace("NOT", "");
						}
						
						if(!has_not)
						{
							if(fuzzy!=1)
								bool_inner.must(QueryBuilders.termQuery("dc:subject.value", and_values[j]));
							else
								bool_inner.must(QueryBuilders
									.fuzzyLikeThisQuery("dc:subject.value")
									.fuzziness(Fuzziness.fromSimilarity((float) similarity))
									.likeText(and_values[j])
									.maxQueryTerms(2)
									);
						}
						else
						{
							if(fuzzy_not!=1)
								bool_inner.mustNot(QueryBuilders.termQuery("dc:subject.value", and_values[j]));
							else
								bool_inner.mustNot(QueryBuilders
									.fuzzyLikeThisQuery("dc:subject.value")
									.fuzziness(Fuzziness.fromSimilarity((float) similarity))
									.likeText(and_values[j])
									.maxQueryTerms(2)
									);
						}
					}
					bool_q.should(bool_inner);
				}
				build_o.must(bool_q);
				
				/*
				String values[]=subject.split("AND");
				
				for(int i=0;i<values.length;i++)
					build_o.must(QueryBuilders.termQuery("subject.value", values[i]));*/
			}
				//build_o.must(QueryBuilders.matchQuery("subject.value", subject));
			
			String lang=parser.parseLanguage(search_query);
			if(!lang.isEmpty())
			{
				search_parent=true;
				
				BoolQueryBuilder bool_q=QueryBuilders.boolQuery();
				String or_values[]=lang.split("OR");
				for(int i=0;i<or_values.length;i++)
				{
					String and_values[]=or_values[i].split("AND");
					BoolQueryBuilder bool_inner=QueryBuilders.boolQuery();
					
					for(int j=0;j<and_values.length;j++)
					{
						bool_inner.must(QueryBuilders.termQuery("dct:language", and_values[j]));
					}
					bool_q.should(bool_inner);
				}
				build_o.must(bool_q);
				
				/*String values[]=lang.split("AND");
				
				for(int i=0;i<values.length;i++)
					build_o.must(QueryBuilders.termQuery("language.value", values[i]));*/
			}

			//System.out.println("i reached here22");
			  

			String fulltext=parser.parseFullText(search_query);
			if(!fulltext.isEmpty())
			{
				search_parent=true;
				
				//System.out.println(fulltext);
				
				BoolQueryBuilder bool_q=QueryBuilders.boolQuery();
				

					BoolQueryBuilder bool_inner=QueryBuilders.boolQuery();
					
					
						if(fulltext.equals(("any")))
						{
								bool_inner.must(
										QueryBuilders.wildcardQuery("bibo:uri", "http*")
										);
						}
						else if(fulltext.equals("pdf"))
						{
							bool_inner.must(
									QueryBuilders.wildcardQuery("bibo:uri", "*pdf")
									);
						}
						else if(fulltext.equals("doi"))
						{
							bool_inner.must(
									QueryBuilders.wildcardQuery("bibo:doi", "*")
									);
						}
					
					bool_q.should(bool_inner);
				
				build_o.must(bool_q);
			}
				//build_o.must(QueryBuilders.matchQuery("language.value", lang));
			
			
			
			BuildSearchResponse builder=new BuildSearchResponse();
			
			//System.out.println(lang+"\n"+author+"\n"
			//		+subject+"\n"+source+"\n"+type+"\n"+keyword+"\n"
			//		+keywordE);

			results=builder.buildFrom_betaFacets(client,build_o,build_child,
					page,search_parent, build_enhanced, request);

		//client.close();

			//System.out.println("i reached here33");

		if(format.equals("xml"))
		{
			ToXML converter=new ToXML();
			results=converter.convertToXMLFreeText(results);
		}
		
		if(mongo_up)
			mongodb.cacheResponse(request, results);
		
		//mongodb.
		
		//results="";
		//return results;
		
		return new ResponseEntity<String>(
				results,response_head,HttpStatus.CREATED);
	
	    
	}
	
	@ApiOperation(value = "Search sentiment analyzed tweets")
	@ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Success", response = ResponseABSA.class),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Failure")}) 
	@RequestMapping( value="/search-absa", method={RequestMethod.GET},
		/*produces={"application/xml","application/json"}*/
			produces="*/*")
	@ApiImplicitParams({
		@ApiImplicitParam(
    			name = "aspect_category", 
    			value = "search for specific absa category", 
    			required = false, 
    			dataType = "string", 
    			paramType = "query", 
    			defaultValue="general"),
		/*@ApiImplicitParam(
    			name = "user_id", 
    			value = "specific user's id", 
    			required = false, 
    			dataType = "string", 
    			paramType = "query", 
    			defaultValue="2154405297"),*/
		@ApiImplicitParam(
    			name = "username", 
    			value = "specific user's name", 
    			required = false, 
    			dataType = "string", 
    			paramType = "query", 
    			defaultValue="seleogr"),
		@ApiImplicitParam(
    			name = "polarity", 
    			value = "filter results by polarity (positive, neutral, negative)", 
    			required = false, 
    			dataType = "string", 
    			paramType = "query", 
    			defaultValue="positiveORnegative"),
		@ApiImplicitParam(
    			name = "from", 
    			value = "tweets >= this date, format YYYY-MM-DD (-MM-DD, are optional)", 
    			required = false, 
    			dataType = "string", 
    			paramType = "query", 
    			defaultValue="2014"),
		@ApiImplicitParam(
    			name = "to", 
    			value = "tweets <= this date, format YYYY-MM-DD (-MM-DD, are optional)", 
    			required = false, 
    			dataType = "string", 
    			paramType = "query", 
    			defaultValue="2015"),
		/*@ApiImplicitParam(
    			name = "user-group", 
    			value = "limit results to those posted by user group(s)", 
    			required = false, 
    			dataType = "string", 
    			paramType = "query", 
    			defaultValue="123OR456"),*/
		@ApiImplicitParam(
    			name = "page", 
    			value = "page of the results (0,1...)", 
    			required = false, 
    			dataType = "int", 
    			paramType = "query", 
    			defaultValue="0"),
		@ApiImplicitParam(
    	    	name = "apikey", 
    	    	value = "apikey", 
    	    	required = true, 
    	    	dataType = "string", 
    	    	paramType = "query", 
    	    	defaultValue="agroknow"),
		@ApiImplicitParam(
    			name = "format", 
    			value = "output format", 
    			required = true, 
    			dataType = "string", 
    			paramType = "query",
    			defaultValue="json"),
		@ApiImplicitParam(
    			name = "cache", 
    			value = "use cache", 
    			required = true, 
    			dataType = "boolean", 
    			paramType = "query",
    			defaultValue="true")
	})
	ResponseEntity<String> search_absa(HttpServletRequest request) throws UnknownHostException { 
		
		double init_time=(double)System.currentTimeMillis()/1000;
		
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
				return new ResponseEntity<String>("{\"error\":\"Api validation Error\"}", response_head, HttpStatus.CREATED);
			}
			
			mongodb.addPoints(apikey, "search", request);
			
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
		
		String results="";

		
		BoolQueryBuilder build =QueryBuilders.boolQuery();
		
		QueryBuilder query = null;
		boolean search_parent=false;
		BoolQueryBuilder build_o =QueryBuilders.boolQuery();
		BoolQueryBuilder build_child =QueryBuilders.boolQuery();
		BoolQueryBuilder build_enhanced=QueryBuilders.boolQuery();
				
			int page=parser.parsePage(request);
			//int fuzzy;
			GetConfig config = new GetConfig();
			//double similarity;
			

			String absa_category=parser.parseABSACategory(request);
			if(!absa_category.isEmpty())
			{		
				BoolQueryBuilder bool_q=QueryBuilders.boolQuery();
				String or_values[]=absa_category.split("OR");
				for(int i=0;i<or_values.length;i++)
				{
					String and_values[]=or_values[i].split("AND");
					BoolQueryBuilder bool_inner=QueryBuilders.boolQuery();
					
					for(int j=0;j<and_values.length;j++)
					{
						
						boolean has_not=false;
						
						if(and_values[j].contains("NOT"))
						{
							has_not=true;
							and_values[j]=and_values[j].replace("NOT", "");
						}
						
						if(!has_not)
						{
							bool_inner.must(QueryBuilders.termQuery("tweet.opinions.aspect_category", and_values[j]));
						}
						else
						{
							bool_inner.mustNot(QueryBuilders.termQuery("tweet.opinions.aspect_category", and_values[j]));
							
						}
						
					}
					bool_q.should(bool_inner);
				}
				build_child.must(bool_q);
				
			}

			String user_id=parser.parseUserID(request);
			if(!user_id.isEmpty())
			{		
				BoolQueryBuilder bool_q=QueryBuilders.boolQuery();
				String or_values[]=user_id.split("OR");
				for(int i=0;i<or_values.length;i++)
				{
					String and_values[]=or_values[i].split("AND");
					BoolQueryBuilder bool_inner=QueryBuilders.boolQuery();
					
					for(int j=0;j<and_values.length;j++)
					{
						
						boolean has_not=false;
						
						if(and_values[j].contains("NOT"))
						{
							has_not=true;
							and_values[j]=and_values[j].replace("NOT", "");
						}
						
						if(!has_not)
						{
							bool_inner.must(QueryBuilders.termQuery("tweet.id", and_values[j]));
						}
						else
						{
							bool_inner.mustNot(QueryBuilders.termQuery("tweet.id", and_values[j]));
							
						}
						
					}
					bool_q.should(bool_inner);
				}
				build_child.must(bool_q);
				
			}

			String username=parser.parseUserName(request);
			
			System.out.println("USERNAME:"+username);
			
			if(!username.isEmpty())
			{		
				BoolQueryBuilder bool_q=QueryBuilders.boolQuery();
				String or_values[]=username.split("OR");
				for(int i=0;i<or_values.length;i++)
				{
					String and_values[]=or_values[i].split("AND");
					BoolQueryBuilder bool_inner=QueryBuilders.boolQuery();
					
					for(int j=0;j<and_values.length;j++)
					{
						
						boolean has_not=false;
						
						if(and_values[j].contains("NOT"))
						{
							has_not=true;
							and_values[j]=and_values[j].replace("NOT", "");
						}
						
						and_values[j]=and_values[j].toLowerCase();
						
						if(!has_not)
						{
							bool_inner.must(QueryBuilders.termQuery("tweet.user.screen_name", and_values[j]));
						}
						else
						{
							bool_inner.mustNot(QueryBuilders.termQuery("tweet.user.screen_name", and_values[j]));
							
						}
						
					}
					bool_q.should(bool_inner);
				}
				build_child.must(bool_q);
				
			}

			String polarity=parser.parsePolarity(request);
			if(!polarity.isEmpty())
			{		
				BoolQueryBuilder bool_q=QueryBuilders.boolQuery();
				String or_values[]=polarity.split("OR");
				for(int i=0;i<or_values.length;i++)
				{
					String and_values[]=or_values[i].split("AND");
					BoolQueryBuilder bool_inner=QueryBuilders.boolQuery();
					
					for(int j=0;j<and_values.length;j++)
					{
						
						boolean has_not=false;
						
						if(and_values[j].contains("NOT"))
						{
							has_not=true;
							and_values[j]=and_values[j].replace("NOT", "");
						}
						
						if(!has_not)
						{
							bool_inner.must(QueryBuilders.termQuery("tweet.opinions.polarity", and_values[j]));
						}
						else
						{
							bool_inner.mustNot(QueryBuilders.termQuery("tweet.opinions.polarity", and_values[j]));
							
						}
						
					}
					bool_q.should(bool_inner);
				}
				build_child.must(bool_q);
				
			}
			
			String user_group=parser.parseUserGroup(request);
			
			//System.out.println("UG:"+user_group);
			
			if(!user_group.isEmpty())
			{		
				BoolQueryBuilder bool_q=QueryBuilders.boolQuery();
				String or_values[]=user_group.split("OR");
				for(int i=0;i<or_values.length;i++)
				{
					//System.out.println(or_values[i]);
					
					String and_values[]=or_values[i].split("AND");
					BoolQueryBuilder bool_inner=QueryBuilders.boolQuery();
					
					for(int j=0;j<and_values.length;j++)
					{
						
						boolean has_not=false;
						
						if(and_values[j].contains("NOT"))
						{
							has_not=true;
							and_values[j]=and_values[j].replace("NOT", "");
						}
						
						if(!has_not)
						{
							bool_inner.must(QueryBuilders.termQuery("tweet.opinions.user_group", and_values[j]));
						}
						else
						{
							bool_inner.mustNot(QueryBuilders.termQuery("tweet.opinions.user_group", and_values[j]));
							
						}
						
					}
					bool_q.should(bool_inner);
				}
				build_child.must(bool_q);
				
			}
			
			String from_date=parser.parseFromDate(request);
			String to_date=parser.parseToDate(request);
			if(!from_date.isEmpty() || !to_date.isEmpty())
			{
								
				/*if(from_date.isEmpty())
					from_date=to_date;
				if(to_date.isEmpty())
					to_date=from_date;*/
				
				if(from_date.isEmpty())
					from_date="50";
				if(to_date.isEmpty())
					to_date="9999";

				if(from_date.length()<10)
				{
					if(from_date.length()==4)
						from_date+="/01/01";
					else if(from_date.length()==7)
						from_date+="01";
				}
				
				if(to_date.length()<10)
				{
					if(to_date.length()==4)
						to_date+="/12/31";
					else if(to_date.length()==7)
						to_date+="31";
				}
								
				build_child.must(
						QueryBuilders
						.rangeQuery("tweet.created_at")
						.gte(from_date)
						.lte(to_date)
						);	
					
				/*filters.add(FilterBuilders
						.rangeFilter("date")
						.gte(from_date)
						.lte(to_date));*/
			}
			  
			
			
			
			
			 
			BuildSearchResponse builder=new BuildSearchResponse();
			//results=builder.buildFrom(client,build_o,filters,page,search_parent);
			
			results=builder.buildFrom_betaABSA(client,build_o,build_child,
					page,search_parent, build_enhanced, request);

		//client.close();
		
		if(format.equals("xml"))
		{
			ToXML converter=new ToXML();
			results=converter.convertToXMLFreeText(results);
		}
		
		if(mongo_up)
			mongodb.cacheResponse(request, results);
		
		//mongodb.
		
		//results="";
		//return results;
		
		return new ResponseEntity<String>(results, response_head, HttpStatus.CREATED);
	    
			
			
			
	}
	
	@ApiOperation(value = "Facets on ABSA")
	/*@ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Success", response = ResponseABSA.class),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Failure")}) */
	@RequestMapping( value="/search-absa-facets", method={RequestMethod.GET},
		/*produces={"application/xml","application/json"}*/
			produces="*/*")
	@ApiImplicitParams({
		@ApiImplicitParam(
    			name = "fid", 
    			value = "facet hash id return by the searc-absa endpoint", 
    			required = false, 
    			dataType = "string", 
    			paramType = "query", 
    			defaultValue="S2esJNLTxVTmUYAKwzKNOjR8Ui6PWiyY/FmKFvIRtxEt6nRe+d7tMscxv53PJY3NcNbks89cHlN8$$$___wiF9CIVHI8Kobusb84QW4IUpP27dlpI="),
		@ApiImplicitParam(
    			name = "page", 
    			value = "page of the results (0,1...)", 
    			required = false, 
    			dataType = "int", 
    			paramType = "query", 
    			defaultValue="0"),
		@ApiImplicitParam(
    	    	name = "apikey", 
    	    	value = "apikey", 
    	    	required = true, 
    	    	dataType = "string", 
    	    	paramType = "query", 
    	    	defaultValue="agroknow"),
		@ApiImplicitParam(
    			name = "format", 
    			value = "output format", 
    			required = true, 
    			dataType = "string", 
    			paramType = "query",
    			defaultValue="json"),
		@ApiImplicitParam(
    			name = "cache", 
    			value = "use cache", 
    			required = true, 
    			dataType = "boolean", 
    			paramType = "query",
    			defaultValue="true")
	})
	ResponseEntity<String> search_absa_facets(HttpServletRequest request) throws UnknownHostException { 
		
		double init_time=(double)System.currentTimeMillis()/1000;
		
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
				return new ResponseEntity<String>("{\"error\":\"Api validation Error\"}", response_head, HttpStatus.CREATED);
			}
			
			mongodb.addPoints(apikey, "search-facet", request);
			
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
		

		String fid="";
		String search_query="";
		try
		{
			fid = parser.parseFID(request);
			search_query=mongodb.getFacetQuery(fid);
			
			//System.out.println(fid+" matches to:"+search_query);
			
		}
		catch(Exception e)
		{
			AESencr aes = new AESencr();
			try 
			{
				search_query=aes.decrypt(fid);
			} 
			catch (Exception e1) 
			{
				return new ResponseEntity<String>(
						"{\"error\":\"unable to process request. have you done such a search query?\"}",response_head,HttpStatus.CREATED);
			}
			//return "{\"error\":\"unable to process request. have you done such a search query?\"}";
		}
		
		
		
		Settings settings = ImmutableSettings.settingsBuilder()
		        .put("cluster.name", "agroknow").build();
			
		Client client = ESClient.client;
		
		String results="";

		
		BoolQueryBuilder build =QueryBuilders.boolQuery();
		
		QueryBuilder query = null;
		boolean search_parent=false;
		BoolQueryBuilder build_o =QueryBuilders.boolQuery();
		BoolQueryBuilder build_child =QueryBuilders.boolQuery();
		BoolQueryBuilder build_enhanced=QueryBuilders.boolQuery();
				
			int page=parser.parsePage(request);
			//int fuzzy;
			GetConfig config = new GetConfig();
			//double similarity;
			


			String absa_category=parser.parseABSACategory(search_query);
			if(!absa_category.isEmpty())
			{		
				BoolQueryBuilder bool_q=QueryBuilders.boolQuery();
				String or_values[]=absa_category.split("OR");
				for(int i=0;i<or_values.length;i++)
				{
					String and_values[]=or_values[i].split("AND");
					BoolQueryBuilder bool_inner=QueryBuilders.boolQuery();
					
					for(int j=0;j<and_values.length;j++)
					{
						
						boolean has_not=false;
						
						if(and_values[j].contains("NOT"))
						{
							has_not=true;
							and_values[j]=and_values[j].replace("NOT", "");
						}
						
						if(!has_not)
						{
							bool_inner.must(QueryBuilders.termQuery("tweet.opinions.aspect_category", and_values[j]));
						}
						else
						{
							bool_inner.mustNot(QueryBuilders.termQuery("tweet.opinions.aspect_category", and_values[j]));
							
						}
						
					}
					bool_q.should(bool_inner);
				}
				build_child.must(bool_q);
				
			}


			String username=parser.parseUserName(search_query);
			if(!username.isEmpty())
			{		
				BoolQueryBuilder bool_q=QueryBuilders.boolQuery();
				String or_values[]=username.split("OR");
				for(int i=0;i<or_values.length;i++)
				{
					String and_values[]=or_values[i].split("AND");
					BoolQueryBuilder bool_inner=QueryBuilders.boolQuery();
					
					for(int j=0;j<and_values.length;j++)
					{
						
						boolean has_not=false;
						
						if(and_values[j].contains("NOT"))
						{
							has_not=true;
							and_values[j]=and_values[j].replace("NOT", "");
						}
						and_values[j]=and_values[j].toLowerCase();
						if(!has_not)
						{
							bool_inner.must(QueryBuilders.termQuery("tweet.user.screen_name", and_values[j]));
						}
						else
						{
							bool_inner.mustNot(QueryBuilders.termQuery("tweet.user.screen_name", and_values[j]));
							
						}
						
					}
					bool_q.should(bool_inner);
				}
				build_child.must(bool_q);
				
			}



			String user_id=parser.parseUserID(search_query);
			if(!user_id.isEmpty())
			{		
				BoolQueryBuilder bool_q=QueryBuilders.boolQuery();
				String or_values[]=user_id.split("OR");
				for(int i=0;i<or_values.length;i++)
				{
					String and_values[]=or_values[i].split("AND");
					BoolQueryBuilder bool_inner=QueryBuilders.boolQuery();
					
					for(int j=0;j<and_values.length;j++)
					{
						
						boolean has_not=false;
						
						if(and_values[j].contains("NOT"))
						{
							has_not=true;
							and_values[j]=and_values[j].replace("NOT", "");
						}
						
						if(!has_not)
						{
							bool_inner.must(QueryBuilders.termQuery("tweet.id", and_values[j]));
						}
						else
						{
							bool_inner.mustNot(QueryBuilders.termQuery("tweet.id", and_values[j]));
							
						}
						
					}
					bool_q.should(bool_inner);
				}
				build_child.must(bool_q);
				
			}


			String polarity=parser.parsePolarity(search_query);
			if(!polarity.isEmpty())
			{		
				BoolQueryBuilder bool_q=QueryBuilders.boolQuery();
				String or_values[]=polarity.split("OR");
				for(int i=0;i<or_values.length;i++)
				{
					String and_values[]=or_values[i].split("AND");
					BoolQueryBuilder bool_inner=QueryBuilders.boolQuery();
					
					for(int j=0;j<and_values.length;j++)
					{
						
						boolean has_not=false;
						
						if(and_values[j].contains("NOT"))
						{
							has_not=true;
							and_values[j]=and_values[j].replace("NOT", "");
						}
						
						if(!has_not)
						{
							bool_inner.must(QueryBuilders.termQuery("tweet.opinions.polarity", and_values[j]));
						}
						else
						{
							bool_inner.mustNot(QueryBuilders.termQuery("tweet.opinions.polarity", and_values[j]));
							
						}
						
					}
					bool_q.should(bool_inner);
				}
				build_child.must(bool_q);
				
			}
			
			String user_group=parser.parseUserGroup(search_query);
			
			//System.out.println("UG:"+user_group);
			
			if(!user_group.isEmpty())
			{		
				BoolQueryBuilder bool_q=QueryBuilders.boolQuery();
				String or_values[]=user_group.split("OR");
				for(int i=0;i<or_values.length;i++)
				{
					//System.out.println(or_values[i]);
					
					String and_values[]=or_values[i].split("AND");
					BoolQueryBuilder bool_inner=QueryBuilders.boolQuery();
					
					for(int j=0;j<and_values.length;j++)
					{
						
						boolean has_not=false;
						
						if(and_values[j].contains("NOT"))
						{
							has_not=true;
							and_values[j]=and_values[j].replace("NOT", "");
						}
						
						if(!has_not)
						{
							bool_inner.must(QueryBuilders.termQuery("tweet.opinions.user_group", and_values[j]));
						}
						else
						{
							bool_inner.mustNot(QueryBuilders.termQuery("tweet.opinions.user_group", and_values[j]));
							
						}
						
					}
					bool_q.should(bool_inner);
				}
				build_child.must(bool_q);
				
			}
			
			String from_date=parser.parseFromDate(search_query);
			String to_date=parser.parseToDate(search_query);
			if(!from_date.isEmpty() || !to_date.isEmpty())
			{
								
				/*if(from_date.isEmpty())
					from_date=to_date;
				if(to_date.isEmpty())
					to_date=from_date;*/
				
				if(from_date.isEmpty())
					from_date="50";
				if(to_date.isEmpty())
					to_date="9999";

				if(from_date.length()<10)
				{
					if(from_date.length()==4)
						from_date+="/01/01";
					else if(from_date.length()==7)
						from_date+="01";
				}
				
				if(to_date.length()<10)
				{
					if(to_date.length()==4)
						to_date+="/12/31";
					else if(to_date.length()==7)
						to_date+="31";
				}
								
				build_child.must(
						QueryBuilders
						.rangeQuery("tweet.created_at")
						.gte(from_date)
						.lte(to_date)
						);	
					
				/*filters.add(FilterBuilders
						.rangeFilter("date")
						.gte(from_date)
						.lte(to_date));*/
			}
			  
			
			
			
			
			 
			BuildSearchResponse builder=new BuildSearchResponse();
			//results=builder.buildFrom(client,build_o,filters,page,search_parent);
			
			results=builder.buildFrom_betaABSA_Facets(client,build_o,build_child,
					page,search_parent, build_enhanced, request);

		//client.close();
		
		if(format.equals("xml"))
		{
			ToXML converter=new ToXML();
			results=converter.convertToXMLFreeText(results);
		}
		
		if(mongo_up)
			mongodb.cacheResponse(request, results);
		
		//mongodb.
		
		//results="";
		//return results;
		
		return new ResponseEntity<String>(results, response_head, HttpStatus.CREATED);
	    
			
			
			
	}
	
	@ApiOperation(value = "Search for training material")
	@RequestMapping( value="/search-sfth", method={RequestMethod.GET},
		/*produces={"application/xml","application/json"}*/
			produces="*/*")
	@ApiImplicitParams({
		@ApiImplicitParam(
    			name = "freetext", 
    			value = "search entities based on title and description", 
    			required = false, 
    			dataType = "string", 
    			paramType = "query", 
    			defaultValue="HACCP"),
	
		@ApiImplicitParam(
    			name = "organizer", 
    			value = "return courses organized by a specific entity", 
    			required = false, 
    			dataType = "string", 
    			paramType = "query", 
    			defaultValue="Canadian Food Processors Institute"),
		
		@ApiImplicitParam(
    			name = "course_type", 
    			value = "return courses of a specific type", 
    			required = false, 
    			dataType = "string", 
    			paramType = "query", 
    			defaultValue="online course"),
		
		@ApiImplicitParam(
    			name = "conference_workshop", 
    			value = "return courses with topics (multiple topics separated with an OR statement)", 
    			required = false, 
    			dataType = "string", 
    			paramType = "query", 
    			defaultValue=""),
		@ApiImplicitParam(
    			name = "industry", 
    			value = "", 
    			required = false, 
    			dataType = "string", 
    			paramType = "query", 
    			defaultValue=""),
		@ApiImplicitParam(
    			name = "training_kind", 
    			value = "", 
    			required = false, 
    			dataType = "string", 
    			paramType = "query", 
    			defaultValue=""),
		@ApiImplicitParam(
    			name = "topics", 
    			value = "", 
    			required = false, 
    			dataType = "string", 
    			paramType = "query", 
    			defaultValue=""),
		@ApiImplicitParam(
    			name = "location_country", 
    			value = "", 
    			required = false, 
    			dataType = "string", 
    			paramType = "query", 
    			defaultValue=""),
		@ApiImplicitParam(
    			name = "location_state", 
    			value = "", 
    			required = false, 
    			dataType = "string", 
    			paramType = "query", 
    			defaultValue=""),
		@ApiImplicitParam(
    			name = "location_region", 
    			value = "", 
    			required = false, 
    			dataType = "string", 
    			paramType = "query", 
    			defaultValue=""),
		@ApiImplicitParam(
    			name = "language", 
    			value = "", 
    			required = false, 
    			dataType = "string", 
    			paramType = "query", 
    			defaultValue=""),
		@ApiImplicitParam(
    			name = "access_rights", 
    			value = "", 
    			required = false, 
    			dataType = "string", 
    			paramType = "query", 
    			defaultValue=""),
		
		@ApiImplicitParam(
    			name = "start_date", 
    			value = "return courses that start on a specific date (format: yyyy-mm-dd)", 
    			required = false, 
    			dataType = "string", 
    			paramType = "query", 
    			defaultValue=""),
		
		@ApiImplicitParam(
    			name = "page", 
    			value = "page of the results (0,1...)", 
    			required = false, 
    			dataType = "int", 
    			paramType = "query", 
    			defaultValue="0"),
		@ApiImplicitParam(
    	    	name = "apikey", 
    	    	value = "apikey", 
    	    	required = true, 
    	    	dataType = "string", 
    	    	paramType = "query", 
    	    	defaultValue="sfth-dev"),
		/*@ApiImplicitParam(
    			name = "format", 
    			value = "output format", 
    			required = true, 
    			dataType = "string", 
    			paramType = "query",
    			defaultValue="json"),*/
		@ApiImplicitParam(
    			name = "cache", 
    			value = "use cache", 
    			required = true, 
    			dataType = "boolean", 
    			paramType = "query",
    			defaultValue="true")
	})
	
	ResponseEntity<String> search_sfth(HttpServletRequest request) throws UnknownHostException { 
		
		double init_time=(double)System.currentTimeMillis()/1000;
		
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
				return new ResponseEntity<String>("{\"error\":\"Api validation Error\"}", response_head, HttpStatus.CREATED);
			}
			
			mongodb.addPoints(apikey, "search", request);
			
			try
			{
				//boolean use_cache=parser.parseCache(request);			
				boolean use_cache = false;
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
			
		Client client = ESClient.client;/*new TransportClient(settings)
				.addTransportAddress(new InetSocketTransportAddress("localhost", 9300));*/
		        //.addTransportAddress(new InetSocketTransportAddress("host2", 9300));
		
		String results="";
				
			int page=parser.parsePage(request);
			
			boolean search_parent=false;
			
			BoolQueryBuilder build =QueryBuilders.boolQuery();
			
			QueryBuilder query = null;


			BoolQueryBuilder build_o =QueryBuilders.boolQuery();
			BoolQueryBuilder build_child =QueryBuilders.boolQuery();
			BoolQueryBuilder build_enhanced=QueryBuilders.boolQuery();
			
			
		    List<FilterBuilder> filters=new LinkedList<>();
		    
		    GetConfig config=new GetConfig();
			int fuzzy;
			
			
			float similarity;
			try{
				similarity=Double.valueOf(config.getValue("similarity")).floatValue();
			}
			catch(Exception e)
			{
				similarity=(float) 0.75;
			}
				

			String keywordE=parser.parseKeywordEnhanced(request);
			if(!keywordE.isEmpty())
			{
				//search_parent=true;
				
				int fuzzy_not=0;

				try {
					fuzzy_not = Integer.valueOf(config.getValue("fuzzy_not"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				}
				
				
				try {
					fuzzy = Integer.valueOf(config.getValue("fuzzy_keyword"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy=1;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy=1;
				}
				
				BoolQueryBuilder bool_qN=QueryBuilders.boolQuery();
				/*bool_qN.must(QueryBuilders
						.fuzzyLikeThisQuery("author.person.name",
								"resource.title.value")
						.likeText(keywordE)
						.maxQueryTerms(2));*/
				/*
				bool_qN.should(QueryBuilders
						.fuzzyLikeThisQuery("title.value")
						.likeText(keywordE)
						.maxQueryTerms(2));

				bool_qN.should(QueryBuilders
						.fuzzyLikeThisQuery("abstract.value")
						.likeText(keywordE)
						.maxQueryTerms(2));
				
				bool_qN.should(QueryBuilders
						.fuzzyLikeThisQuery("author.person.name")
						.likeText(keywordE)
						.maxQueryTerms(2));

				bool_qN.should(QueryBuilders
						.fuzzyLikeThisQuery("location.value")
						.likeText(keywordE)
						.maxQueryTerms(2));
				
				bool_qN.should(QueryBuilders
						.fuzzyLikeThisQuery("subject.value")
						.likeText(keywordE)
						.maxQueryTerms(2));
				
				
				build_enhanced.must(bool_qN);
				
				System.out.println(build_enhanced.toString());
				*/
				String or_values[]=keywordE.split("OR");
				BoolQueryBuilder bool_q=QueryBuilders.boolQuery();
				
				for(int j=0;j<or_values.length;j++)
				{
					BoolQueryBuilder bool_inner=QueryBuilders.boolQuery();
					String values[]=or_values[j].split("AND");
					
					for(int i=0;i<values.length;i++)
					{

						//System.out.println(values[i]);
						boolean has_not=false;
						
						BoolQueryBuilder bool_beta=QueryBuilders.boolQuery();
						
						if(values[i].contains("NOT"))
						{
							has_not=true;
							values[i]=values[i].replace("NOT", "");
						}
						
						if(!has_not)
						{
							if(fuzzy==1)
							{
								bool_beta.should(QueryBuilders
										.fuzzyLikeThisQuery("course.title")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(values[i])
										.maxQueryTerms(2));

								bool_beta.should(QueryBuilders
										.fuzzyLikeThisQuery("course.description")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(values[i])
										.maxQueryTerms(2));
								
								bool_beta.should(QueryBuilders
										.fuzzyLikeThisQuery("course.organizer.person.name")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(values[i])
										.maxQueryTerms(2));

								bool_beta.should(QueryBuilders
										.fuzzyLikeThisQuery("course.organizer.organisation.name")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(values[i])
										.maxQueryTerms(2));
								
								bool_beta.should(QueryBuilders
										.fuzzyLikeThisQuery("course.location.location_name")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(values[i])
										.maxQueryTerms(2));
								
								bool_beta.should(QueryBuilders
										.fuzzyLikeThisQuery("course.topics.value")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(values[i])
										.maxQueryTerms(2));
								
								
								bool_inner.must(bool_beta);
								
							}
							else
							{
								bool_beta.should(QueryBuilders
										.queryString(values[i])
										.field("course.title"));

								bool_beta.should(QueryBuilders
										.queryString(values[i])
										.field("course.description"));
								
								bool_beta.should(QueryBuilders
										.queryString(values[i])
										.field("course.organizer.person.name"));

								bool_beta.should(QueryBuilders
										.queryString(values[i])
										.field("course.organizer.organization.name"));
								
								bool_beta.should(QueryBuilders
										.queryString(values[i])
										.field("course.location.location_name"));
								
								bool_beta.should(QueryBuilders
										.queryString(values[i])
										.field("course.topics.value"));
								
								
								bool_inner.must(bool_beta);
								
							}
						}
						else
						{
							if(fuzzy_not==1)
							{
								
								bool_beta.mustNot(QueryBuilders
										.fuzzyLikeThisQuery("course.title")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(values[i])
										.maxQueryTerms(2));

								bool_beta.mustNot(QueryBuilders
										.fuzzyLikeThisQuery("course.description")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(values[i])
										.maxQueryTerms(2));
								
								bool_beta.mustNot(QueryBuilders
										.fuzzyLikeThisQuery("course.organizer.person.name")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(values[i])
										.maxQueryTerms(2));
								
								bool_beta.mustNot(QueryBuilders
										.fuzzyLikeThisQuery("course.organizer.organisation.name")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(values[i])
										.maxQueryTerms(2));

								bool_beta.mustNot(QueryBuilders
										.fuzzyLikeThisQuery("course.location.location_name")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(values[i])
										.maxQueryTerms(2));
								
								bool_beta.mustNot(QueryBuilders
										.fuzzyLikeThisQuery("course.topics.value")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(values[i])
										.maxQueryTerms(2));
								
								
								bool_inner.must(bool_beta);
								
							}
							else
							{

								bool_beta.mustNot(QueryBuilders
										.queryString(values[i])
										.field("course.title"));

								bool_beta.mustNot(QueryBuilders
										.queryString(values[i])
										.field("course.description"));
								
								bool_beta.mustNot(QueryBuilders
										.queryString(values[i])
										.field("course.organizer.organisation.name"));

								bool_beta.mustNot(QueryBuilders
										.queryString(values[i])
										.field("course.organizer.person.name"));
								
								bool_beta.mustNot(QueryBuilders
										.queryString(values[i])
										.field("course.location.location_name"));
								
								bool_beta.mustNot(QueryBuilders
										.queryString(values[i])
										.field("course.topics.value"));
								
								
								bool_inner.must(bool_beta);
							}
						}
						
					}
					
					bool_q.should(bool_inner);
				}
				//build_o.must(bool_q);
				
				//System.out.println(bool_q.toString());
				
				build_enhanced.must(bool_q);
			}

			String organizer=parser.parseOrganizer(request);
			System.out.println(organizer);
			
			if(!organizer.isEmpty())
			{
				try {
					fuzzy = Integer.valueOf(config.getValue("fuzzy_author"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy=0;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy=0;
				}
				
				int fuzzy_not=0;

				try {
					fuzzy_not = Integer.valueOf(config.getValue("fuzzy_not"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				}
				
				BoolQueryBuilder bool_q=QueryBuilders.boolQuery();
				String or_values[]=organizer.split("OR");
				for(int i=0;i<or_values.length;i++)
				{
					String and_values[]=or_values[i].split("AND");
					BoolQueryBuilder bool_inner=QueryBuilders.boolQuery();
					
					for(int j=0;j<and_values.length;j++)
					{

						boolean has_not=false;
						
						if(and_values[j].contains("NOT"))
						{
							has_not=true;
							and_values[j]=and_values[j].replace("NOT", "");
						}
						
						if(!has_not)
						{
							if(fuzzy!=1) {
								bool_inner.should(QueryBuilders.termQuery("organizer.organisation.name.raw", 
										and_values[j]));
								bool_inner.should(QueryBuilders.termQuery("organizer.person.name", 
										and_values[j]));
							}
							else {
								bool_inner.must(QueryBuilders
									.fuzzyLikeThisQuery("course.organizer.organisation.name.raw")
									.fuzziness(Fuzziness.fromSimilarity((float) similarity))
									.likeText(and_values[j])
									.maxQueryTerms(25)
									);
								bool_inner.must(QueryBuilders
										.fuzzyLikeThisQuery("course.organizer.person.name")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(and_values[j])
										.maxQueryTerms(25)
										);
							}
						}
						else
						{
							if(fuzzy_not==0) {
								bool_inner.mustNot(QueryBuilders.termQuery("course.organizer.organisation.name", and_values[j]));
								bool_inner.mustNot(QueryBuilders.termQuery("course.organizer.person.name", and_values[j]));
							}
							else {
								bool_inner.mustNot(QueryBuilders
									.fuzzyLikeThisQuery("course.organizer.organisation.name")
									.fuzziness(Fuzziness.fromSimilarity((float) similarity))
									.likeText(and_values[j])
									.maxQueryTerms(2)
									);
								bool_inner.mustNot(QueryBuilders
										.fuzzyLikeThisQuery("course.organizer.person.name")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(and_values[j])
										.maxQueryTerms(2)
										);
							}
						}
					}
					bool_q.should(bool_inner);
				}
				build_child.must(bool_q);
				
				
				/*String values[]=author.split("AND");
				
				for(int i=0;i<values.length;i++)
					filters.add(FilterBuilders.termFilter("author.person.name",values[i]));*/
			}	

			String course_type=parser.parseCourseType(request);
			
			
			if(!course_type.isEmpty())
			{
				try {
					fuzzy = Integer.valueOf(config.getValue("fuzzy_author"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy=0;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy=0;
				}
				
				int fuzzy_not=0;

				try {
					fuzzy_not = Integer.valueOf(config.getValue("fuzzy_not"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				}
				
				BoolQueryBuilder bool_q=QueryBuilders.boolQuery();
				String or_values[]=course_type.split("OR");
				for(int i=0;i<or_values.length;i++)
				{
					String and_values[]=or_values[i].split("AND");
					BoolQueryBuilder bool_inner=QueryBuilders.boolQuery();
					
					for(int j=0;j<and_values.length;j++)
					{

						boolean has_not=false;
						
						if(and_values[j].contains("NOT"))
						{
							has_not=true;
							and_values[j]=and_values[j].replace("NOT", "");
						}
						
						if(!has_not)
						{
							if(fuzzy!=1) {
								bool_inner.must(QueryBuilders.termQuery("course.course_type.raw", 
										and_values[j]));
							}
							else {
								bool_inner.must(QueryBuilders
									.fuzzyLikeThisQuery("course.course_type.raw")
									.fuzziness(Fuzziness.fromSimilarity((float) similarity))
									.likeText(and_values[j])
									.maxQueryTerms(25)
									);
							}
						}
						else
						{
							if(fuzzy_not==0) {
								bool_inner.mustNot(QueryBuilders.termQuery("course.course_type.raw", and_values[j]));
							}
							else {
								bool_inner.mustNot(QueryBuilders
									.fuzzyLikeThisQuery("course.course_type.raw")
									.fuzziness(Fuzziness.fromSimilarity((float) similarity))
									.likeText(and_values[j])
									.maxQueryTerms(2)
									);
								bool_inner.mustNot(QueryBuilders
										.fuzzyLikeThisQuery("course.course_type.raw")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(and_values[j])
										.maxQueryTerms(2)
										);
							}
						}
					}
					bool_q.should(bool_inner);
				}
				build_child.must(bool_q);
				
				
				/*String values[]=author.split("AND");
				
				for(int i=0;i<values.length;i++)
					filters.add(FilterBuilders.termFilter("author.person.name",values[i]));*/
			}			
			
			String course_topic=parser.parseCourseTopic(request);
			
			
			if(!course_topic.isEmpty())
			{
				try {
					fuzzy = Integer.valueOf(config.getValue("fuzzy_author"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy=0;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy=0;
				}
				
				int fuzzy_not=0;

				try {
					fuzzy_not = Integer.valueOf(config.getValue("fuzzy_not"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				}
				
				BoolQueryBuilder bool_q=QueryBuilders.boolQuery();
				String or_values[]=course_topic.split("OR");
				for(int i=0;i<or_values.length;i++)
				{
					String and_values[]=or_values[i].split("AND");
					BoolQueryBuilder bool_inner=QueryBuilders.boolQuery();
					
					for(int j=0;j<and_values.length;j++)
					{

						boolean has_not=false;
						
						if(and_values[j].contains("NOT"))
						{
							has_not=true;
							and_values[j]=and_values[j].replace("NOT", "");
						}
						
						if(!has_not)
						{
							if(fuzzy!=1) {
								bool_inner.must(QueryBuilders.termQuery("course.topics.value", 
										and_values[j]));
							}
							else {
								bool_inner.must(QueryBuilders
									.fuzzyLikeThisQuery("course.topics.value")
									.fuzziness(Fuzziness.fromSimilarity((float) similarity))
									.likeText(and_values[j])
									.maxQueryTerms(25)
									);
							}
						}
						else
						{
							if(fuzzy_not==0) {
								bool_inner.mustNot(QueryBuilders.termQuery("course.topics.value", and_values[j]));
							}
							else {
								bool_inner.mustNot(QueryBuilders
									.fuzzyLikeThisQuery("course.topics.value")
									.fuzziness(Fuzziness.fromSimilarity((float) similarity))
									.likeText(and_values[j])
									.maxQueryTerms(2)
									);
								bool_inner.mustNot(QueryBuilders
										.fuzzyLikeThisQuery("course.topics.value")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(and_values[j])
										.maxQueryTerms(2)
										);
							}
						}
					}
					bool_q.should(bool_inner);
				}
				build_child.must(bool_q);
				
				
				/*String values[]=author.split("AND");
				
				for(int i=0;i<values.length;i++)
					filters.add(FilterBuilders.termFilter("author.person.name",values[i]));*/
			}			
			

			String course_date=parser.parseCourseStartDate(request);
			
			
			if(!course_date.isEmpty())
			{
				try {
					fuzzy = Integer.valueOf(config.getValue("fuzzy_author"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy=0;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy=0;
				}
				
				int fuzzy_not=0;

				try {
					fuzzy_not = Integer.valueOf(config.getValue("fuzzy_not"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				}
				
				BoolQueryBuilder bool_q=QueryBuilders.boolQuery();
				String or_values[]=course_date.split("OR");
				for(int i=0;i<or_values.length;i++)
				{
					String and_values[]=or_values[i].split("AND");
					BoolQueryBuilder bool_inner=QueryBuilders.boolQuery();
					
					for(int j=0;j<and_values.length;j++)
					{

						boolean has_not=false;
						
						if(and_values[j].contains("NOT"))
						{
							has_not=true;
							and_values[j]=and_values[j].replace("NOT", "");
						}
						
						if(!has_not)
						{
							if(fuzzy!=1) {
								bool_inner.must(QueryBuilders.termQuery("course.start_date", 
										and_values[j]));
							}
							else {
								bool_inner.must(QueryBuilders
									.fuzzyLikeThisQuery("course.start_date")
									.fuzziness(Fuzziness.fromSimilarity((float) similarity))
									.likeText(and_values[j])
									.maxQueryTerms(25)
									);
							}
						}
						else
						{
							if(fuzzy_not==0) {
								bool_inner.mustNot(QueryBuilders.termQuery("course.start_date", and_values[j]));
							}
							else {
								bool_inner.mustNot(QueryBuilders
									.fuzzyLikeThisQuery("course.start_date")
									.fuzziness(Fuzziness.fromSimilarity((float) similarity))
									.likeText(and_values[j])
									.maxQueryTerms(2)
									);
							}
						}
					}
					bool_q.should(bool_inner);
				}
				build_child.must(bool_q);
				
				
				/*String values[]=author.split("AND");
				
				for(int i=0;i<values.length;i++)
					filters.add(FilterBuilders.termFilter("author.person.name",values[i]));*/
			}

			String tkind=parser.parseTKind(request);			
			if(!tkind.isEmpty())
			{
				try {
					fuzzy = Integer.valueOf(config.getValue("fuzzy_author"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy=0;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy=0;
				}
				
				int fuzzy_not=0;

				try {
					fuzzy_not = Integer.valueOf(config.getValue("fuzzy_not"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				}
				
				BoolQueryBuilder bool_q=QueryBuilders.boolQuery();
				String or_values[]=tkind.split("OR");
				for(int i=0;i<or_values.length;i++)
				{
					String and_values[]=or_values[i].split("AND");
					BoolQueryBuilder bool_inner=QueryBuilders.boolQuery();
					
					for(int j=0;j<and_values.length;j++)
					{

						boolean has_not=false;
						
						if(and_values[j].contains("NOT"))
						{
							has_not=true;
							and_values[j]=and_values[j].replace("NOT", "");
						}
						
						if(!has_not)
						{
							if(fuzzy!=1) {
								bool_inner.must(QueryBuilders.termQuery("course.training_kind.raw", 
										and_values[j]));
							}
							else {
								bool_inner.must(QueryBuilders
									.fuzzyLikeThisQuery("course.training_kind.raw")
									.fuzziness(Fuzziness.fromSimilarity((float) similarity))
									.likeText(and_values[j])
									.maxQueryTerms(25)
									);
							}
						}
						else
						{
							if(fuzzy_not==0) {
								bool_inner.mustNot(QueryBuilders.termQuery("course.training_kind.raw", and_values[j]));
							}
							else {
								bool_inner.mustNot(QueryBuilders
									.fuzzyLikeThisQuery("course.training_kind.raw")
									.fuzziness(Fuzziness.fromSimilarity((float) similarity))
									.likeText(and_values[j])
									.maxQueryTerms(2)
									);
								bool_inner.mustNot(QueryBuilders
										.fuzzyLikeThisQuery("course.training_kind.raw")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(and_values[j])
										.maxQueryTerms(2)
										);
							}
						}
					}
					bool_q.should(bool_inner);
				}
				build_child.must(bool_q);
			}			
			

			String industry=parser.parseIndustry(request);			
			if(!industry.isEmpty())
			{
				try {
					fuzzy = Integer.valueOf(config.getValue("fuzzy_author"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy=0;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy=0;
				}
				
				int fuzzy_not=0;

				try {
					fuzzy_not = Integer.valueOf(config.getValue("fuzzy_not"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				}
				
				BoolQueryBuilder bool_q=QueryBuilders.boolQuery();
				String or_values[]=industry.split("OR");
				for(int i=0;i<or_values.length;i++)
				{
					String and_values[]=or_values[i].split("AND");
					BoolQueryBuilder bool_inner=QueryBuilders.boolQuery();
					
					for(int j=0;j<and_values.length;j++)
					{

						boolean has_not=false;
						
						if(and_values[j].contains("NOT"))
						{
							has_not=true;
							and_values[j]=and_values[j].replace("NOT", "");
						}
						
						if(!has_not)
						{
							if(fuzzy!=1) {
								bool_inner.must(QueryBuilders.termQuery("course.industry.raw", 
										and_values[j]));
							}
							else {
								bool_inner.must(QueryBuilders
									.fuzzyLikeThisQuery("course.industry.raw")
									.fuzziness(Fuzziness.fromSimilarity((float) similarity))
									.likeText(and_values[j])
									.maxQueryTerms(25)
									);
							}
						}
						else
						{
							if(fuzzy_not==0) {
								bool_inner.mustNot(QueryBuilders.termQuery("course.industry.raw", and_values[j]));
							}
							else {
								bool_inner.mustNot(QueryBuilders
									.fuzzyLikeThisQuery("course.industry.raw")
									.fuzziness(Fuzziness.fromSimilarity((float) similarity))
									.likeText(and_values[j])
									.maxQueryTerms(2)
									);
								bool_inner.mustNot(QueryBuilders
										.fuzzyLikeThisQuery("course.industry.raw")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(and_values[j])
										.maxQueryTerms(2)
										);
							}
						}
					}
					bool_q.should(bool_inner);
				}
				build_child.must(bool_q);
			}			
			

			String lcountry=parser.parseLCountry(request);			
			if(!lcountry.isEmpty())
			{
				try {
					fuzzy = Integer.valueOf(config.getValue("fuzzy_author"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy=0;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy=0;
				}
				
				int fuzzy_not=0;

				try {
					fuzzy_not = Integer.valueOf(config.getValue("fuzzy_not"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				}
				
				BoolQueryBuilder bool_q=QueryBuilders.boolQuery();
				String or_values[]=lcountry.split("OR");
				for(int i=0;i<or_values.length;i++)
				{
					String and_values[]=or_values[i].split("AND");
					BoolQueryBuilder bool_inner=QueryBuilders.boolQuery();
					
					for(int j=0;j<and_values.length;j++)
					{

						boolean has_not=false;
						
						if(and_values[j].contains("NOT"))
						{
							has_not=true;
							and_values[j]=and_values[j].replace("NOT", "");
						}
						
						if(!has_not)
						{
							if(fuzzy!=1) {
								bool_inner.must(QueryBuilders.termQuery("course.location.location_country.raw", 
										and_values[j]));
							}
							else {
								bool_inner.must(QueryBuilders
									.fuzzyLikeThisQuery("course.location.location_country.raw")
									.fuzziness(Fuzziness.fromSimilarity((float) similarity))
									.likeText(and_values[j])
									.maxQueryTerms(25)
									);
							}
						}
						else
						{
							if(fuzzy_not==0) {
								bool_inner.mustNot(QueryBuilders.termQuery("course.location.location_country.raw", and_values[j]));
							}
							else {
								bool_inner.mustNot(QueryBuilders
									.fuzzyLikeThisQuery("course.location.location_country.raw")
									.fuzziness(Fuzziness.fromSimilarity((float) similarity))
									.likeText(and_values[j])
									.maxQueryTerms(2)
									);
								bool_inner.mustNot(QueryBuilders
										.fuzzyLikeThisQuery("course.location.location_country.raw")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(and_values[j])
										.maxQueryTerms(2)
										);
							}
						}
					}
					bool_q.should(bool_inner);
				}
				build_child.must(bool_q);
			}			
			


			String lregion=parser.parseLRegion(request);			
			if(!lregion.isEmpty())
			{
				try {
					fuzzy = Integer.valueOf(config.getValue("fuzzy_author"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy=0;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy=0;
				}
				
				int fuzzy_not=0;

				try {
					fuzzy_not = Integer.valueOf(config.getValue("fuzzy_not"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				}
				
				BoolQueryBuilder bool_q=QueryBuilders.boolQuery();
				String or_values[]=lregion.split("OR");
				for(int i=0;i<or_values.length;i++)
				{
					String and_values[]=or_values[i].split("AND");
					BoolQueryBuilder bool_inner=QueryBuilders.boolQuery();
					
					for(int j=0;j<and_values.length;j++)
					{

						boolean has_not=false;
						
						if(and_values[j].contains("NOT"))
						{
							has_not=true;
							and_values[j]=and_values[j].replace("NOT", "");
						}
						
						if(!has_not)
						{
							if(fuzzy!=1) {
								bool_inner.must(QueryBuilders.termQuery("course.location.location_region.raw", 
										and_values[j]));
							}
							else {
								bool_inner.must(QueryBuilders
									.fuzzyLikeThisQuery("course.location.location_region.raw")
									.fuzziness(Fuzziness.fromSimilarity((float) similarity))
									.likeText(and_values[j])
									.maxQueryTerms(25)
									);
							}
						}
						else
						{
							if(fuzzy_not==0) {
								bool_inner.mustNot(QueryBuilders.termQuery("course.location.location_region.raw", and_values[j]));
							}
							else {
								bool_inner.mustNot(QueryBuilders
									.fuzzyLikeThisQuery("course.location.location_region.raw")
									.fuzziness(Fuzziness.fromSimilarity((float) similarity))
									.likeText(and_values[j])
									.maxQueryTerms(2)
									);
								bool_inner.mustNot(QueryBuilders
										.fuzzyLikeThisQuery("course.location.location_region.raw")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(and_values[j])
										.maxQueryTerms(2)
										);
							}
						}
					}
					bool_q.should(bool_inner);
				}
				build_child.must(bool_q);
			}			


			String lstate=parser.parseLState(request);			
			if(!lstate.isEmpty())
			{
				try {
					fuzzy = Integer.valueOf(config.getValue("fuzzy_author"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy=0;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy=0;
				}
				
				int fuzzy_not=0;

				try {
					fuzzy_not = Integer.valueOf(config.getValue("fuzzy_not"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				}
				
				BoolQueryBuilder bool_q=QueryBuilders.boolQuery();
				String or_values[]=lstate.split("OR");
				for(int i=0;i<or_values.length;i++)
				{
					String and_values[]=or_values[i].split("AND");
					BoolQueryBuilder bool_inner=QueryBuilders.boolQuery();
					
					for(int j=0;j<and_values.length;j++)
					{

						boolean has_not=false;
						
						if(and_values[j].contains("NOT"))
						{
							has_not=true;
							and_values[j]=and_values[j].replace("NOT", "");
						}
						
						if(!has_not)
						{
							if(fuzzy!=1) {
								bool_inner.must(QueryBuilders.termQuery("course.location.location_state.raw", 
										and_values[j]));
							}
							else {
								bool_inner.must(QueryBuilders
									.fuzzyLikeThisQuery("course.location.location_region.raw")
									.fuzziness(Fuzziness.fromSimilarity((float) similarity))
									.likeText(and_values[j])
									.maxQueryTerms(25)
									);
							}
						}
						else
						{
							if(fuzzy_not==0) {
								bool_inner.mustNot(QueryBuilders.termQuery("course.location.location_state.raw", and_values[j]));
							}
							else {
								bool_inner.mustNot(QueryBuilders
									.fuzzyLikeThisQuery("course.location.location_state.raw")
									.fuzziness(Fuzziness.fromSimilarity((float) similarity))
									.likeText(and_values[j])
									.maxQueryTerms(2)
									);
								bool_inner.mustNot(QueryBuilders
										.fuzzyLikeThisQuery("course.location.location_state.raw")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(and_values[j])
										.maxQueryTerms(2)
										);
							}
						}
					}
					bool_q.should(bool_inner);
				}
				build_child.must(bool_q);
			}			
			


			String language=parser.parseLanguage(request);			
			if(!language.isEmpty())
			{
				try {
					fuzzy = Integer.valueOf(config.getValue("fuzzy_author"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy=0;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy=0;
				}
				
				int fuzzy_not=0;

				try {
					fuzzy_not = Integer.valueOf(config.getValue("fuzzy_not"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				}
				
				BoolQueryBuilder bool_q=QueryBuilders.boolQuery();
				String or_values[]=language.split("OR");
				for(int i=0;i<or_values.length;i++)
				{
					String and_values[]=or_values[i].split("AND");
					BoolQueryBuilder bool_inner=QueryBuilders.boolQuery();
					
					for(int j=0;j<and_values.length;j++)
					{

						boolean has_not=false;
						
						if(and_values[j].contains("NOT"))
						{
							has_not=true;
							and_values[j]=and_values[j].replace("NOT", "");
						}
						
						if(!has_not)
						{
							if(fuzzy!=1) {
								bool_inner.must(QueryBuilders.termQuery("course.language.raw", 
										and_values[j]));
							}
							else {
								bool_inner.must(QueryBuilders
									.fuzzyLikeThisQuery("course.language.raw")
									.fuzziness(Fuzziness.fromSimilarity((float) similarity))
									.likeText(and_values[j])
									.maxQueryTerms(25)
									);
							}
						}
						else
						{
							if(fuzzy_not==0) {
								bool_inner.mustNot(QueryBuilders.termQuery("course.language.raw", and_values[j]));
							}
							else {
								bool_inner.mustNot(QueryBuilders
									.fuzzyLikeThisQuery("course.language.raw")
									.fuzziness(Fuzziness.fromSimilarity((float) similarity))
									.likeText(and_values[j])
									.maxQueryTerms(2)
									);
								bool_inner.mustNot(QueryBuilders
										.fuzzyLikeThisQuery("course.language.raw")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(and_values[j])
										.maxQueryTerms(2)
										);
							}
						}
					}
					bool_q.should(bool_inner);
				}
				build_child.must(bool_q);
			}			


			String access_rights=parser.parseARights(request);			
			if(!access_rights.isEmpty())
			{
				try {
					fuzzy = Integer.valueOf(config.getValue("fuzzy_author"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy=0;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy=0;
				}
				
				int fuzzy_not=0;

				try {
					fuzzy_not = Integer.valueOf(config.getValue("fuzzy_not"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				}
				
				BoolQueryBuilder bool_q=QueryBuilders.boolQuery();
				String or_values[]=access_rights.split("OR");
				for(int i=0;i<or_values.length;i++)
				{
					String and_values[]=or_values[i].split("AND");
					BoolQueryBuilder bool_inner=QueryBuilders.boolQuery();
					
					for(int j=0;j<and_values.length;j++)
					{

						boolean has_not=false;
						
						if(and_values[j].contains("NOT"))
						{
							has_not=true;
							and_values[j]=and_values[j].replace("NOT", "");
						}
						
						if(!has_not)
						{
							if(fuzzy!=1) {
								bool_inner.must(QueryBuilders.termQuery("course.access_rights.raw", 
										and_values[j]));
							}
							else {
								bool_inner.must(QueryBuilders
									.fuzzyLikeThisQuery("course.access_rights.raw")
									.fuzziness(Fuzziness.fromSimilarity((float) similarity))
									.likeText(and_values[j])
									.maxQueryTerms(25)
									);
							}
						}
						else
						{
							if(fuzzy_not==0) {
								bool_inner.mustNot(QueryBuilders.termQuery("course.access_rights.raw", and_values[j]));
							}
							else {
								bool_inner.mustNot(QueryBuilders
									.fuzzyLikeThisQuery("course.access_rights.raw")
									.fuzziness(Fuzziness.fromSimilarity((float) similarity))
									.likeText(and_values[j])
									.maxQueryTerms(2)
									);
								bool_inner.mustNot(QueryBuilders
										.fuzzyLikeThisQuery("course.access_rights.raw")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(and_values[j])
										.maxQueryTerms(2)
										);
							}
						}
					}
					bool_q.should(bool_inner);
				}
				build_child.must(bool_q);
			}			


			String con_workshop=parser.parseCWorkshop(request);			
			if(!con_workshop.isEmpty())
			{
				try {
					fuzzy = Integer.valueOf(config.getValue("fuzzy_author"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy=0;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy=0;
				}
				
				int fuzzy_not=0;

				try {
					fuzzy_not = Integer.valueOf(config.getValue("fuzzy_not"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fuzzy_not=0;
				}
				
				BoolQueryBuilder bool_q=QueryBuilders.boolQuery();
				String or_values[]=con_workshop.split("OR");
				for(int i=0;i<or_values.length;i++)
				{
					String and_values[]=or_values[i].split("AND");
					BoolQueryBuilder bool_inner=QueryBuilders.boolQuery();
					
					for(int j=0;j<and_values.length;j++)
					{

						boolean has_not=false;
						
						if(and_values[j].contains("NOT"))
						{
							has_not=true;
							and_values[j]=and_values[j].replace("NOT", "");
						}
						
						if(!has_not)
						{
							if(fuzzy!=1) {
								bool_inner.must(QueryBuilders.termQuery("course.conference_workshop.raw", 
										and_values[j]));
							}
							else {
								bool_inner.must(QueryBuilders
									.fuzzyLikeThisQuery("course.conference_workshop.raw")
									.fuzziness(Fuzziness.fromSimilarity((float) similarity))
									.likeText(and_values[j])
									.maxQueryTerms(25)
									);
							}
						}
						else
						{
							if(fuzzy_not==0) {
								bool_inner.mustNot(QueryBuilders.termQuery("course.conference_workshop.raw", and_values[j]));
							}
							else {
								bool_inner.mustNot(QueryBuilders
									.fuzzyLikeThisQuery("course.conference_workshop.raw")
									.fuzziness(Fuzziness.fromSimilarity((float) similarity))
									.likeText(and_values[j])
									.maxQueryTerms(2)
									);
								bool_inner.mustNot(QueryBuilders
										.fuzzyLikeThisQuery("course.conference_workshop.raw")
										.fuzziness(Fuzziness.fromSimilarity((float) similarity))
										.likeText(and_values[j])
										.maxQueryTerms(2)
										);
							}
						}
					}
					bool_q.should(bool_inner);
				}
				build_child.must(bool_q);
			}			
			
			
			
			String from_date=parser.parseFromDate(request);
			String to_date=parser.parseToDate(request);
			if(!from_date.isEmpty() || !to_date.isEmpty())
			{
								
				/*if(from_date.isEmpty())
					from_date=to_date;
				if(to_date.isEmpty())
					to_date=from_date;*/
				
				if(from_date.isEmpty())
					from_date="50";
				if(to_date.isEmpty())
					to_date="9999";
				
				//if(from_date.equals(to_date))
				//{
					from_date+="-01-01";
					to_date+="-12-31";
				//}
				
				build_child.must(
						QueryBuilders
						.rangeQuery("date")
						.gte(from_date)
						.lte(to_date)
						);	
					
				/*filters.add(FilterBuilders
						.rangeFilter("date")
						.gte(from_date)
						.lte(to_date));*/
			}
			  
			
			
			
			BuildSearchResponse builder=new BuildSearchResponse();
			//results=builder.buildFrom(client,build_o,filters,page,search_parent);
			
			results=builder.buildFrom_beta_sfth(client,build_o,build_child,
					page,search_parent, build_enhanced, request);

		//client.close();
		
		if(format.equals("xml"))
		{
			ToXML converter=new ToXML();
			results=converter.convertToXMLFreeText(results);
		}
		
		if(mongo_up)
			mongodb.cacheResponse(request, results);
		
		//mongodb.
		
		//results="";
		//return results;
		
		return new ResponseEntity<String>(results, response_head, HttpStatus.CREATED);
	    
	}

}
