package com.agroknow.agris.utils;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;

public class ToXML 
{
	public String convertToXMLFacet(String json_input)
	{
		String xml="";
		try
		{
			JSONObject json = null;
			
			json = (JSONObject)new JSONParser().parse(json_input);

	
			xml+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
					+ "	<result xmlns:dc=\"http://purl.org/dc/elements/1.1/\" "
					+ "xmlns:bibo=\"http://purl.org/ontology/bibo/\" "
					+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
					+ "xmlns:ore=\"http://www.openarchives.org/ore/terms/\" "
					+ "xmlns:edm=\"http://www.europeana.eu/schemas/edm/\" "
					+ "xmlns:schema=\"http://schema.org/\" "
					+ "xmlns:foaf=\"http://xmlns.com/foaf/0.1/\" "
					+ "xmlns:dcterms=\"http://purl.org/dc/terms/\" "+
						"xmlns:oa=\"http://www.openannotation.org/spec/core\" "+
						"xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" "
					+ ">";
			xml+="<total>"+json.get("total")+"</total>";
			xml+="<facet_name>"+json.get("facet_name")+"</facet_name>";
			
			JSONArray jresults=(JSONArray)json.get("results");
			Iterator<JSONObject> iterator=jresults.iterator();
			
			while(iterator.hasNext())
			{
				JSONObject obj=iterator.next();
				
				Set set=obj.entrySet();
				
				String value="";
				String count="";
				
				Iterator keys_iterator=set.iterator();
				while(keys_iterator.hasNext())
				{
					Map.Entry entry=(Map.Entry)keys_iterator.next();
					
					if(entry.getKey().equals("value"))
						value=entry.getValue().toString();
					if(entry.getKey().equals("count"))
						count=entry.getValue().toString();
					
					//System.out.println(entry.getKey()+":"+entry.getValue());
				}

				xml+="<dc:title count=\""+count+"\"><![CDATA["+value+"]]></dc:title>";
				
				//System.out.println("Key values?:"+iterator.next().keySet());
				
				
			}		
		}
		catch(java.lang.Exception e)
		{
			e.printStackTrace();
		}
		
		xml+="</result>";
		return xml;
	}
	
	public String convertToXMLFreeText(String json_input)
	{
		String xml="";
		try
		{
			JSONObject json = null;
			
			json = (JSONObject)new JSONParser().parse(json_input);

	
			xml+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
					+ "	<result xmlns:dc=\"http://purl.org/dc/elements/1.1/\" "
					+ "xmlns:bibo=\"http://purl.org/ontology/bibo/\" "
					+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
					+ "xmlns:ore=\"http://www.openarchives.org/ore/terms/\" "
					+ "xmlns:edm=\"http://www.europeana.eu/schemas/edm/\" "
					+ "xmlns:schema=\"http://schema.org/\" "
					+ "xmlns:foaf=\"http://xmlns.com/foaf/0.1/\" "
					+ "xmlns:dcterms=\"http://purl.org/dc/terms/\" "+
						"xmlns:oa=\"http://www.openannotation.org/spec/core\" "+
						"xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" "
					+ ">";
			xml+="<total>"+json.get("total")+"</total>";
			xml+="<page>"+json.get("page")+"</page>";
			xml+="<page_size>"+json.get("page_size")+"</page_size>";
			xml+="<elapsed>"+json.get("time_elapsed")+"</elapsed>";
			
			
			JSONArray jresults=(JSONArray)json.get("results");
			Iterator<JSONObject> iterator=jresults.iterator();
			
			while(iterator.hasNext())
			{
				JSONObject obj=iterator.next();
				
				Set set=obj.entrySet();
				
				xml+="<object>";
				xml+=convertObject(obj.toString());
				xml+="</object>";
			}
			
			jresults=(JSONArray)json.get("facets");
			iterator=jresults.iterator();
			
			xml+="<facets>";
				while(iterator.hasNext())
				{
					JSONObject obj=iterator.next();
					String total="";
					String name="";
					
					total=(String)obj.get("total").toString();
					name=(String)obj.get("facet_name").toString();
					
					xml+="<facet count=\""+total+"\" name=\""+name+"\">";
						
						JSONArray fresults=(JSONArray)obj.get("results");
						Iterator<JSONObject> fiterator=fresults.iterator();
						
						while(fiterator.hasNext())
						{
							JSONObject objR=fiterator.next();
							
							String value="";
							String count="";
							
							//System.out.println(objR.toString());
							//if(true)
							//	continue;
							
							value=(String)objR.get("value").toString();
							count=(String)objR.get("count").toString();
							
							if(!count.isEmpty())
								count=" count=\""+count+"\"";
							if(!value.isEmpty())
								xml+="<dc:title"+count+"><![CDATA["+value+"]]></dc:title>";
						}
						
					xml+="</facet>";
				}
			xml+="</facets>";
		}
		catch(java.lang.Exception e)
		{
			e.printStackTrace();
		}
		
		xml+="</result>";
		return xml;
	}
	public String convertToXMLID(String json_input)
	{
		String xml="";
		try
		{
			JSONObject json = null;
			
			json = (JSONObject)new JSONParser().parse(json_input);

			xml+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
					+ "	<result xmlns:dc=\"http://purl.org/dc/elements/1.1/\" "
					+ "xmlns:bibo=\"http://purl.org/ontology/bibo/\" "
					+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
					+ "xmlns:ore=\"http://www.openarchives.org/ore/terms/\" "
					+ "xmlns:edm=\"http://www.europeana.eu/schemas/edm/\" "
					+ "xmlns:schema=\"http://schema.org/\" "
					+ "xmlns:foaf=\"http://xmlns.com/foaf/0.1/\" "
					+ "xmlns:dcterms=\"http://purl.org/dc/terms/\" "+
						"xmlns:oa=\"http://www.openannotation.org/spec/core\" "+
						"xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" "
						+ ""
					+ ">";
			xml+="<total>"+json.get("total")+"</total>";
			
			
			JSONArray jresults=(JSONArray)json.get("results");
			Iterator<JSONObject> iterator=jresults.iterator();
			
			while(iterator.hasNext())
			{
				JSONObject obj=iterator.next();
				
				Set set=obj.entrySet();
				
				Iterator keys_iterator=set.iterator();
				while(keys_iterator.hasNext())
				{
					Map.Entry entry=(Map.Entry)keys_iterator.next();
					
					if(entry.getKey().toString().equals("object"))
					{
						xml+="<object>";
						xml+=convertObject(entry.getValue().toString());
						xml+="</object>";
					}
					if(entry.getKey().toString().equals("detailed"))
					{
						xml+="<detailed>";
						xml+=convertDetailed(entry.getValue().toString());
						xml+="</detailed>";
					}
					//System.out.println(entry.getKey()+":"+entry.getValue());
				}

				
				//System.out.println("Key values?:"+iterator.next().keySet());
				
				
			}		
		}
		catch(java.lang.Exception e)
		{
			e.printStackTrace();
		}
		
		xml+="</result>";
		return xml;
	}
	
	public String convertObject(String json_input)
	{
		String xml="";
		try
		{
			JSONObject json = null;
			
			json = (JSONObject)new JSONParser().parse(json_input);
			Set set=json.entrySet();
			
			Iterator keys_iterator=set.iterator();
			while(keys_iterator.hasNext())
			{
				Map.Entry entry=(Map.Entry)keys_iterator.next();

				if(entry.getKey().toString().equals("title"))
				{
					try
					{
						JSONArray jresults=(JSONArray)json.get("title");
						Iterator<JSONObject> iterator=jresults.iterator();
						
						while(iterator.hasNext())
						{
							JSONObject obj=iterator.next();
							xml+=parseTitle(obj);							
						}
					}
					catch(java.lang.ClassCastException e)
					{
						JSONObject obj=(JSONObject)json.get("title");
						
						xml+=parseTitle(obj);
					}
				}

				if(entry.getKey().toString().equals("subject"))
				{
					try
					{
						JSONArray jresults=(JSONArray)json.get("subject");
						Iterator<JSONObject> iterator=jresults.iterator();
						
						while(iterator.hasNext())
						{
							JSONObject obj=iterator.next();
							xml+=parseSubject(obj);							
						}
					}
					catch(java.lang.ClassCastException e)
					{
						e.printStackTrace();
						
						JSONObject obj=(JSONObject)json.get("subject");
						
						xml+=parseSubject(obj);
					}
				}

				if(entry.getKey().toString().equals("description"))
				{
					try
					{
						JSONArray jresults=(JSONArray)json.get("description");
						Iterator<JSONObject> iterator=jresults.iterator();
						
						while(iterator.hasNext())
						{
							JSONObject obj=iterator.next();
							xml+=parseDescription(obj);							
						}
					}
					catch(java.lang.ClassCastException e)
					{
						JSONObject obj=(JSONObject)json.get("description");
						
						xml+=parseDescription(obj);
					}
				}

				if(entry.getKey().toString().equals("updated"))
				{
					try
					{
						JSONArray jresults=(JSONArray)json.get("updated");
						Iterator<String> iterator=jresults.iterator();
						
						while(iterator.hasNext())
						{
							String obj=iterator.next();
							xml+=parseDate(obj);		
						}
					}
					catch(java.lang.ClassCastException e)
					{
						String obj=(String)json.get("updated");
						
						xml+=parseDate(obj);
					}
				}
				
				if(entry.getKey().toString().equals("created"))
				{
					try
					{
						JSONArray jresults=(JSONArray)json.get("created");
						Iterator<String> iterator=jresults.iterator();
						
						while(iterator.hasNext())
						{
							String obj=iterator.next();
							xml+=parseCreated(obj);		
						}
					}
					catch(java.lang.ClassCastException e)
					{
						String obj=(String)json.get("created");
						
						xml+=parseCreated(obj);
					}
				}

				if(entry.getKey().toString().equals("appuri"))
				{
					
						String obj=(String)json.get("appuri");
						
						xml+=parseAppURI(obj,false);
				}
				
				if(entry.getKey().toString().equals("language"))
				{
					try
					{
						JSONArray jresults=(JSONArray)json.get("language");
						Iterator<JSONObject> iterator=jresults.iterator();
						
						while(iterator.hasNext())
						{
							JSONObject obj=iterator.next();
							xml+=parseLang(obj);							
						}
					}
					catch(java.lang.ClassCastException e)
					{
						JSONObject obj=(JSONObject)json.get("language");
						
						xml+=parseLang(obj);
					}
				}

				System.out.println(entry.getKey()+"|||"+entry.getValue());
			}
			
		}
		catch(java.lang.Exception e)
		{
			e.printStackTrace();
		}
	
		return xml;
	}
	
	
	public String parseLang(JSONObject obj)
	{
		String xml="";
		
		String value="", uri="";
		value=obj.get("value").toString();
		uri=obj.get("uri").toString();
		
		xml+="<dc:language";
		
		if(!uri.isEmpty())
			xml+=" rdf:resource=\""+uri+"\"";
		
		xml+="><![CDATA[";

		xml+=value+"]]></dc:language>";
		
		return xml;
		
	}
	
	
	public String parseTitle(JSONObject obj)
	{
		String xml="";
		
		String lang="", title="";
		lang=obj.get("lang").toString();
		title=obj.get("value").toString();
		
		xml+="<dc:title";
		
		if(!lang.isEmpty())
			xml+=" xml:lang=\""+lang+"\"";
		
		xml+="><![CDATA[";

		xml+=title+"]]></dc:title>";
		
		return xml;
		
	}
	
	public String parseAppURI(String obj, boolean only_uri)
	{
		String xml="";
		
		String[] arr=obj.split("/");
		
		xml+="<oa:hasTarget xml:id=\""+arr[arr.length-1]+"\">"+obj+"</oa:hasTarget>";
		
		if(!only_uri)
			xml+="<dc:type>"+arr[2]+"</dc:type>";
		
		
		return xml;
		
	}

	public String parseDescription(JSONObject obj)
	{
		String xml="";
		
		String lang="", title="";
		lang=obj.get("lang").toString();
		title=obj.get("value").toString();
		
		xml+="<dc:description";
		
		if(!lang.isEmpty())
			xml+=" xml:lang=\""+lang+"\"";
		
		xml+="><![CDATA[";

		title=Jsoup.parse(title).text();
		xml+=title+"]]></dc:description>";
		
		return xml;
		
	}

	public String parseCreator(JSONObject obj)
	{
		String xml="";
		
		String id="", value="", type="", uri="";
		id=obj.get("id").toString();
		value=obj.get("value").toString();
		type=obj.get("type").toString();
		uri=obj.get("uri").toString();
		
		xml+="<dc:creator";

		if(!id.isEmpty())
			xml+=" xml:id=\""+id+"\"";
		if(!type.isEmpty())
			xml+=" type=\""+type+"\"";
		if(!uri.isEmpty())
			xml+=" rdf:resource=\""+uri+"\"";
		
		xml+="><![CDATA[";

		xml+=value+"]]></dc:creator>";
		
		return xml;
		
	}

	public String parseContributor(JSONObject obj)
	{
		String xml="";
		
		String id="", value="", type="", uri="";
		id=obj.get("id").toString();
		value=obj.get("value").toString();
		type=obj.get("type").toString();
		uri=obj.get("uri").toString();
		
		xml+="<dc:contributor";

		if(!id.isEmpty())
			xml+=" xml:id=\""+id+"\"";
		if(!type.isEmpty())
			xml+=" type=\""+type+"\"";
		if(!uri.isEmpty())
			xml+=" rdf:resource=\""+uri+"\"";
		
		xml+="><![CDATA[";

		xml+=value+"]]></dc:contributor>";
		
		return xml;
		
	}

	public String parseURL(JSONObject obj)
	{
		String xml="";
		
		String broken="", value="";
		broken=obj.get("broken").toString();
		value=obj.get("value").toString();

		
		xml+="<dc:identifier";

		if(!broken.isEmpty())
			xml+=" broken=\""+broken+"\"";
		
		xml+="><![CDATA[";

		xml+=value+"]]></dc:identifier>";
		
		return xml;
		
	}

	public String parseLocation(JSONObject obj)
	{
		String xml="";
		
		String value="";
		String voc="";
		String uri="";
		
		value=obj.get("value").toString();

		xml+="<dcterms:spatial xsi:type=\"TGN\"";
		try
		{
			JSONArray jvocs=(JSONArray)obj.get("vocabulary");
			Iterator<String> iterator=jvocs.iterator();
			
			xml+=" vocab=\"";
			
			while(iterator.hasNext())
			{
				String v=iterator.next();
				
				if(!v.isEmpty())
					xml+=""+v+",";		
			}
			
			xml+="\"";
			xml=xml.replace(",\"","\"");
		}
		catch(java.lang.ClassCastException e)
		{
			voc=obj.get("vocabulary").toString();
			if(!voc.isEmpty())
				xml+=" vocab=\""+voc+"\"";
		}

		xml+=">";

		try
		{
			JSONArray jvocs=(JSONArray)obj.get("uri");
			Iterator<String> iterator=jvocs.iterator();
			
			while(iterator.hasNext())
			{
				String v=iterator.next();
				
				if(!v.isEmpty())
					xml+="<rdf:value><![CDATA["+v+"]]></rdf:value>";		
			}
		}
		catch(java.lang.ClassCastException e)
		{
			uri=obj.get("uri").toString();
			if(!uri.isEmpty())
				xml+="<rdf:value><![CDATA["+uri+"]]></rdf:value>";
		}		
		
		
		xml+="<rdf:label><![CDATA["+value+"]]></rdf:label>";

		xml+="</dcterms:spatial>";
		
		return xml;
	}


	public String parsePublisher(JSONObject obj)
	{
		String xml="";
		
		String id="", value="", type="", uri="";
		id=obj.get("id").toString();
		value=obj.get("value").toString();
		type=obj.get("type").toString();
		uri=obj.get("uri").toString();
		
		xml+="<dc:publisher";

		if(!id.isEmpty())
			xml+=" xml:id=\""+id+"\"";
		if(!type.isEmpty())
			xml+=" type=\""+type+"\"";
		if(!uri.isEmpty())
			xml+=" rdf:resource=\""+uri+"\"";
		
		xml+="><![CDATA[";

		xml+=value+"]]></dc:publisher>";
		
		return xml;
		
	}

	public String parseSubject(JSONObject obj)
	{
		String xml="";
		
		String value="";
		String voc="";
		String uri="";
		String score="";
		
		value=obj.get("value").toString();

		xml+="<dc:subject";
		try
		{
			JSONArray jvocs=(JSONArray)obj.get("vocabulary");
			Iterator<String> iterator=jvocs.iterator();
			
			xml+=" vocab=\"";
			
			while(iterator.hasNext())
			{
				String v=iterator.next();
				
				if(!v.isEmpty())
					xml+=""+v+",";		
			}
			
			xml+="\"";
			xml=xml.replace(",\"","\"");
		}
		catch(java.lang.ClassCastException e)
		{
			voc=obj.get("vocabulary").toString();
			if(!voc.isEmpty())
				xml+=" vocab=\""+voc+"\"";
		}
		try
		{
			JSONArray jvocs=(JSONArray)obj.get("score");
			Iterator<String> iterator=jvocs.iterator();
			
			while(iterator.hasNext())
			{
				String v=String.valueOf(iterator.next());
				
				if(!v.isEmpty())
					xml+=" score=\""+v+"\"";		
			}
		}
		catch(java.lang.ClassCastException e)
		{
			//e.printStackTrace();
			score=obj.get("score").toString();
			if(!score.isEmpty())
				xml+=" score=\""+score+"\"";
		}	
		
		try
		{
			JSONArray jvocs=(JSONArray)obj.get("uri");
			Iterator<String> iterator=jvocs.iterator();
			
			while(iterator.hasNext())
			{
				String v=iterator.next();
				
				if(!v.isEmpty())
					xml+=" rdf:resource=\""+v+"\"";		
			}
		}
		catch(java.lang.ClassCastException e)
		{
			uri=obj.get("uri").toString();
			if(!uri.isEmpty())
				xml+=" rdf:resource=\""+voc+"\"";
		}		
		
		
		xml+="><![CDATA[";

		xml+=value+"]]></dc:subject>";
		
		return xml;
		
	}


	public String parseDate(String obj)
	{
		String xml="";
		
		xml+="<dc:date";
		xml+="><![CDATA[";
		xml+=obj+"]]></dc:date>";
		
		return xml;
		
	}

	public String parseCreated(String obj)
	{
		String xml="";
		
		xml+="<dcterms:created";
		xml+="><![CDATA[";
		xml+=obj+"]]></dcterms:created>";
		
		return xml;
		
	}
	
	public String convertDetailed(String json_input)
	{
		String xml="";
		try
		{
			JSONObject json = null;
			
			json = (JSONObject)new JSONParser().parse(json_input);
			Set set=json.entrySet();
			
			Iterator keys_iterator=set.iterator();
			while(keys_iterator.hasNext())
			{
				Map.Entry entry=(Map.Entry)keys_iterator.next();

				if(entry.getKey().toString().equals("citation"))
				{
					String obj=(String)json.get("citation");					
					xml+=parseCitation(obj);
				}

				if(entry.getKey().toString().equals("appuri"))
				{
						String obj=(String)json.get("appuri");
						xml+=parseAppURI(obj,true);
				}
				
				if(entry.getKey().toString().equals("type"))
				{
						String obj=(String)json.get("type");
						xml+=parseType(obj);
				}

				if(entry.getKey().toString().equals("creator"))
				{
					try
					{
						JSONArray jresults=(JSONArray)json.get("creator");
						Iterator<JSONObject> iterator=jresults.iterator();
						
						while(iterator.hasNext())
						{
							JSONObject obj=iterator.next();
							xml+=parseCreator(obj);							
						}
					}
					catch(java.lang.ClassCastException e)
					{
						JSONObject obj=(JSONObject)json.get("creator");
						
						xml+=parseCreator(obj);
					}
				}
				if(entry.getKey().toString().equals("contributor"))
				{
					try
					{
						JSONArray jresults=(JSONArray)json.get("contributor");
						Iterator<JSONObject> iterator=jresults.iterator();
						
						while(iterator.hasNext())
						{
							JSONObject obj=iterator.next();
							xml+=parseContributor(obj);							
						}
					}
					catch(java.lang.ClassCastException e)
					{
						JSONObject obj=(JSONObject)json.get("contributor");
						
						xml+=parseContributor(obj);
					}
				}
				if(entry.getKey().toString().equals("url"))
				{
					try
					{
						JSONArray jresults=(JSONArray)json.get("url");
						Iterator<JSONObject> iterator=jresults.iterator();
						
						while(iterator.hasNext())
						{
							JSONObject obj=iterator.next();
							xml+=parseURL(obj);							
						}
					}
					catch(java.lang.ClassCastException e)
					{
						JSONObject obj=(JSONObject)json.get("url");
						
						xml+=parseURL(obj);
					}
				}
				
				if(entry.getKey().toString().equals("publisher"))
				{
					try
					{
						JSONArray jresults=(JSONArray)json.get("publisher");
						Iterator<JSONObject> iterator=jresults.iterator();
						
						while(iterator.hasNext())
						{
							JSONObject obj=iterator.next();
							xml+=parsePublisher(obj);							
						}
					}
					catch(java.lang.ClassCastException e)
					{
						JSONObject obj=(JSONObject)json.get("publisher");
						
						xml+=parsePublisher(obj);
					}
				}
				
				if(entry.getKey().toString().equals("date"))
				{
						String obj=(String)json.get("date");
						xml+=parseDate(obj);
				}

				if(entry.getKey().toString().equals("updatedDate"))
				{
						String obj=(String)json.get("updatedDate");
						xml+=parseUpdatedDate(obj);
				}

				if(entry.getKey().toString().equals("issn"))
				{
						String obj=(String)json.get("issn");
						xml+=parseISSN(obj);
				}

				if(entry.getKey().toString().equals("isbn"))
				{
						String obj=(String)json.get("isbn");
						xml+=parseISBN(obj);
				}

				if(entry.getKey().toString().equals("location"))
				{
					try
					{
						JSONArray jresults=(JSONArray)json.get("location");
						Iterator<JSONObject> iterator=jresults.iterator();
						
						while(iterator.hasNext())
						{
							JSONObject obj=iterator.next();
							xml+=parseLocation(obj);							
						}
					}
					catch(java.lang.ClassCastException e)
					{
						JSONObject obj=(JSONObject)json.get("location");
						
						xml+=parseLocation(obj);
					}
				}

				if(entry.getKey().toString().equals("focus"))
				{
						String obj=(String)json.get("focus");
						xml+=parseFocus(obj);
				}


				if(entry.getKey().toString().equals("place"))
				{
						String obj=(String)json.get("place");
						xml+=parsePlace(obj);
				}

				if(entry.getKey().toString().equals("coverage"))
				{
					try
					{
						String obj=(String)json.get("coverage");
						xml+=parsePlace(obj);
					}
					catch(java.lang.ClassCastException e)
					{
						JSONArray objA=(JSONArray)json.get("coverage");
						
						Iterator<String> iterator=objA.iterator();
						while(iterator.hasNext())
						{
							String obj=iterator.next();
							xml+=parsePlace(obj);						
						}
						
					}	
				}

				if(entry.getKey().toString().equals("rights"))
				{
					try
					{
						String obj=(String)json.get("rights");
						xml+=parseRights(obj);
					}
					catch(java.lang.ClassCastException e)
					{
						JSONArray objA=(JSONArray)json.get("rights");
						
						Iterator<String> iterator=objA.iterator();
						while(iterator.hasNext())
						{
							String obj=iterator.next();
							xml+=parseRights(obj);						
						}
						
					}			
						
				}

				if(entry.getKey().toString().equals("extent"))
				{
						String obj=(String)json.get("extent");
						xml+=parseSeries(obj);
				}

				if(entry.getKey().toString().equals("page"))
				{
						String obj=(String)json.get("page");
						xml+=parsePages(obj);
				}

				if(entry.getKey().toString().equals("relation"))
				{
					try
					{
						String obj=(String)json.get("relation");
						xml+=parseRelation(obj);
					}
					catch(java.lang.ClassCastException e)
					{
						JSONArray objA=(JSONArray)json.get("relation");
						
						Iterator<String> iterator=objA.iterator();
						while(iterator.hasNext())
						{
							String obj=iterator.next();
							xml+=parseRelation(obj);						
						}
						
						
					}
				}

				if(entry.getKey().toString().equals("timePeriod"))
				{
					try
					{
						String obj=(String)json.get("timePeriod");
						xml+=parseTimePeriod(obj);
					}
					catch(java.lang.ClassCastException e)
					{
						JSONArray objA=(JSONArray)json.get("timePeriod");
						
						Iterator<String> iterator=objA.iterator();
						while(iterator.hasNext())
						{
							String obj=iterator.next();
							xml+=parseTimePeriod(obj);						
						}
						
						
					}
				}

				if(entry.getKey().toString().equals("kindOfData"))
				{
					try
					{
						String obj=(String)json.get("kindOfData");
						xml+=parseKindOfData(obj);
					}
					catch(java.lang.ClassCastException e)
					{
						JSONArray objA=(JSONArray)json.get("kindOfData");
						
						Iterator<String> iterator=objA.iterator();
						while(iterator.hasNext())
						{
							String obj=iterator.next();
							xml+=parseKindOfData(obj);						
						}
						
						
					}
				}

				if(entry.getKey().toString().equals("notes"))
				{
					try
					{
						String obj=(String)json.get("notes");
						xml+=parseNotes(obj);
					}
					catch(java.lang.ClassCastException e)
					{
						JSONArray objA=(JSONArray)json.get("notes");
						
						Iterator<String> iterator=objA.iterator();
						while(iterator.hasNext())
						{
							String obj=iterator.next();
							xml+=parseNotes(obj);						
						}
						
						
					}
				}
				if(entry.getKey().toString().equals("funding"))
				{
					try
					{
						String obj=(String)json.get("funding");
						xml+=parseFunding(obj);
					}
					catch(java.lang.ClassCastException e)
					{
						JSONArray objA=(JSONArray)json.get("funding");
						
						Iterator<String> iterator=objA.iterator();
						while(iterator.hasNext())
						{
							String obj=iterator.next();
							xml+=parseFunding(obj);						
						}
						
						
					}
				}

				if(entry.getKey().toString().equals("doi"))
				{
						String obj=(String)json.get("doi");
						xml+=parseDOI(obj);
				}

				if(entry.getKey().toString().equals("format"))
				{
						String obj=(String)json.get("format");
						xml+=parseFormat(obj);
				}

				if(entry.getKey().toString().equals("quality"))
				{
						String obj=(String)json.get("quality");
						xml+=parseQuality(obj);
				}

				if(entry.getKey().toString().equals("fullName"))
				{
						String obj=(String)json.get("fullName");
						xml+=parseFullName(obj);
				}

				if(entry.getKey().toString().equals("viaf"))
				{
						String obj=(String)json.get("viaf");
						xml+=parseVIAF(obj);
				}

				if(entry.getKey().toString().equals("contact"))
				{
						String obj=(String)json.get("contact");
						xml+=parseContact(obj);
				}

				if(entry.getKey().toString().equals("logo"))
				{
						String obj=(String)json.get("logo");
						xml+=parseLogo(obj);
				}

				if(entry.getKey().toString().equals("address"))
				{
						String obj=(String)json.get("address");
						xml+=parseAddress(obj);
				}

				if(entry.getKey().toString().equals("firstName"))
				{
						String obj=(String)json.get("firstName");
						xml+=parseFirstName(obj);
				}

				if(entry.getKey().toString().equals("lastName"))
				{
						String obj=(String)json.get("lastName");
						xml+=parseLastName(obj);
				}

				if(entry.getKey().toString().equals("orcid"))
				{
						String obj=(String)json.get("orcid");
						xml+=parseORCID(obj);
				}

				if(entry.getKey().toString().equals("photo"))
				{
						String obj=(String)json.get("photo");
						xml+=parsePhoto(obj);
				}

				if(entry.getKey().toString().equals("shortBio"))
				{
						String obj=(String)json.get("shortBio");
						xml+=parseShortBio(obj);
				}

				if(entry.getKey().toString().equals("name"))
				{
						String obj=(String)json.get("name");
						xml+=parseName(obj);
				}

				if(entry.getKey().toString().equals("spec"))
				{
						String obj=(String)json.get("spec");
						xml+=parseSpec(obj);
				}

				if(entry.getKey().toString().equals("aggregation"))
				{
						JSONObject obj=(JSONObject)json.get("aggregation");
						xml+=parseAggregation(obj);
				}

				if(entry.getKey().toString().equals("affiliation"))
				{
						JSONObject obj=(JSONObject)json.get("affiliation");
						xml+=parseAffiliation(obj);
				}

				if(entry.getKey().toString().equals("about"))
				{
						JSONObject obj=(JSONObject)json.get("about");
						xml+=parseAbout(obj);
				}

				if(entry.getKey().toString().equals("collection"))
				{
					try
					{
						JSONArray jresults=(JSONArray)json.get("collection");
						Iterator<JSONObject> iterator=jresults.iterator();
						
						while(iterator.hasNext())
						{
							JSONObject obj=iterator.next();
							xml+=parseCollection(obj);							
						}
					}
					catch(java.lang.ClassCastException e)
					{
						JSONObject obj=(JSONObject)json.get("collection");
						
						xml+=parseCollection(obj);
					}
				}


			}
		}
		catch(java.lang.Exception e)
		{
			e.printStackTrace();
		}
		
		return xml;
		
	}
	
	public String parseAggregation(JSONObject obj)
	{
		String xml="";

		xml+="<ore:Aggregation>";
		
		try
		{
			JSONArray jresults=(JSONArray)obj.get("shownAt");
			Iterator<JSONObject> iterator=jresults.iterator();
			
			while(iterator.hasNext())
			{
				JSONObject objS=iterator.next();
				xml+=parseShownAt(objS);							
			}
		}
		catch(java.lang.ClassCastException e)
		{
			JSONObject objS=(JSONObject)obj.get("shownAt");
			
			xml+=parseShownAt(objS);
		}

		try
		{
			JSONArray jresults=(JSONArray)obj.get("shownBy");
			Iterator<JSONObject> iterator=jresults.iterator();
			
			while(iterator.hasNext())
			{
				JSONObject objS=iterator.next();
				xml+=parseShownBy(objS);							
			}
		}
		catch(java.lang.ClassCastException e)
		{
			JSONObject objS=(JSONObject)obj.get("shownBy");
			
			xml+=parseShownBy(objS);
		}

		try
		{
			JSONArray jresults=(JSONArray)obj.get("linkToResource");
			Iterator<JSONObject> iterator=jresults.iterator();
			
			while(iterator.hasNext())
			{
				JSONObject objS=iterator.next();
				xml+=parseLinkToResource(objS);							
			}
		}
		catch(java.lang.ClassCastException e)
		{
			JSONObject objS=(JSONObject)obj.get("linkToResource");
			
			xml+=parseLinkToResource(objS);
		}
		xml+="</ore:Aggregation>";
		
		return xml;
	}

	public String parseShownAt(JSONObject obj)
	{
		String xml="";
		
		String value="";
		String broken="";
		
		//System.out.println(obj.toString());
		
		value=(String)obj.get("value").toString();
		broken=(String)obj.get("broken").toString();
		
		xml+="<edm:isShownAt";
		
		if(!value.isEmpty())
			xml+=" rdf:resource=\""+value+"\"";
		if(!broken.isEmpty())
			xml+=" broken=\""+broken+"\"";
		
		xml+="/>";
		
		return xml;
	}

	public String parseLinkToResource(JSONObject obj)
	{
		String xml="";
		
		String value="";
		String label="";
		String category="";
		String size="";
		String type="";
		
		value=(String)obj.get("value").toString();
		label=(String)obj.get("label").toString();
		category=(String)obj.get("category").toString();
		size=(String)obj.get("size").toString();
		type=(String)obj.get("type").toString();
		
		xml+="<edm:hasView";
		
		if(!value.isEmpty())
			xml+=" rdf:resource=\""+value+"\"";
		if(!size.isEmpty())
			xml+=" size=\""+size+"\"";
		if(!category.isEmpty())
			xml+=" category=\""+category+"\"";
		xml+=">";
		
		if(!value.isEmpty())
			xml+="<edm:isShownAt><![CDATA["+value+"]]></edm:isShownAt>";
		if(!type.isEmpty())
			xml+="<dc:format><![CDATA["+type+"]]></dc:format>";
		if(!label.isEmpty())
			xml+="<rdf:label>"+label+"</rdf:label>";
		
		xml+="</edm:hasView>";
		return xml;
	}

	public String parseShownBy(JSONObject obj)
	{
		String xml="";
		
		String value="";
		String broken="";
		
		value=(String)obj.get("value").toString();
		broken=(String)obj.get("broken").toString();
		
		xml+="<edm:isShownBy";
		
		if(!value.isEmpty())
			xml+=" rdf:resource=\""+value+"\"";
		if(!broken.isEmpty())
			xml+=" broken=\""+broken+"\"";
		
		xml+="/>";
		
		return xml;
	}
	
	public String parseCitation(String obj)
	{
		String xml="";
		
		xml+="<dcterms:bibliographicCitation><![CDATA["+obj+"]]></dcterms:bibliographicCitation>";
		return xml;
	}

	public String parseType(String obj)
	{
		String xml="";
		
		xml+="<dc:type><![CDATA["+obj+"]]></dc:type>";
		return xml;
	}

	public String parseFullName(String obj)
	{
		String xml="";
		
		xml+="<foaf:name><![CDATA["+obj+"]]></foaf:name>";
		return xml;
	}

	public String parseVIAF(String obj)
	{
		String xml="";
		
		xml+="<dc:identifier scheme=\"dcterms:URI\" vocab=\"VIAF\"><![CDATA["+obj+"]]></foaf:name>";
		return xml;
	}

	public String parseLogo(String obj)
	{
		String xml="";
		
		xml+="<foaf:logo><![CDATA["+obj+"]]></foaf:logo>";
		return xml;
	}

	public String parseContact(String obj)
	{
		String xml="";
		
		xml+="<foaf:mbox><![CDATA["+obj+"]]></foaf:mbox>";
		return xml;
	}

	public String parseAddress(String obj)
	{
		String xml="";
		
		xml+="<schema:address><![CDATA["+obj+"]]></schema:address>";
		return xml;
	}

	
	public String parseUpdatedDate(String obj)
	{
		String xml="";
		
		xml+="<dcterms:updated><![CDATA["+obj+"]]></dcterms:updated>";
		return xml;
	}

	public String parseISSN(String obj)
	{
		String xml="";
		
		xml+="<bibo:issn><![CDATA["+obj+"]]></bibo:issn>";
		return xml;
	}

	public String parsePlace(String obj)
	{
		String xml="";
		
		xml+="<dc:coverage><![CDATA["+obj+"]]></dc:coverage>";
		return xml;
	}

	public String parseFirstName(String obj)
	{
		String xml="";
		
		xml+="<foaf:firstName><![CDATA["+obj+"]]></foaf:firstName>";
		return xml;
	}

	public String parseLastName(String obj)
	{
		String xml="";
		
		xml+="<foaf:lastName><![CDATA["+obj+"]]></foaf:lastName>";
		return xml;
	}

	public String parseORCID(String obj)
	{
		String xml="";
		
		xml+="<dc:identifier scheme=\"dcterms:URI\" vocab=\"ORCID\"><![CDATA["+obj+"]]></dc:identifier>";
		return xml;
	}

	public String parsePhoto(String obj)
	{
		String xml="";
		
		xml+="<foaf:img><![CDATA["+obj+"]]></foaf:img>";
		return xml;
	}

	public String parseShortBio(String obj)
	{
		String xml="";
		
		xml+="<dc:description><![CDATA["+obj+"]]></dc:description>";
		return xml;
	}

	public String parseFocus(String obj)
	{
		String xml="";
		
		xml+="<dc:coverage><![CDATA["+obj+"]]></dc:coverage>";
		return xml;
	}
	
	public String parseRelation(String obj)
	{
		String xml="";
		
		xml+="<dc:relation><![CDATA["+obj+"]]></dc:relation>";
		return xml;
	}
	
	public String parsePages(String obj)
	{
		String xml="";
		
		xml+="<bibo:numPages><![CDATA["+obj+"]]></bibo:numPages>";
		return xml;
	}
	
	public String parseSeries(String obj)
	{
		String xml="";
		
		xml+="<dcterms:extent><![CDATA["+obj+"]]></dcterms:extent>";
		return xml;
	}
	
	public String parseRights(String obj)
	{
		String xml="";
		
		xml+="<dc:rights><![CDATA["+obj+"]]></dc:rights>";
		return xml;
	}

	public String parseISBN(String obj)
	{
		String xml="";
		
		xml+="<bibo:isbn><![CDATA["+obj+"]]></bibo:isbn>";
		return xml;
	}
	
	public String parseDOI(String obj)
	{
		String xml="";
		
		xml+="<bibo:doi><![CDATA["+obj+"]]></bibo:doi>";
		return xml;
	}
	
	public String parseFormat(String obj)
	{
		String xml="";
		
		xml+="<dcterms:format><![CDATA["+obj+"]]></dcterms:format>";
		return xml;
	}

	public String parseQuality(String obj)
	{
		String xml="";
		if(!obj.isEmpty())
			xml+="<schema:videoQuality><![CDATA["+obj+"]]></schema:videoQuality>";
		return xml;
	}

	public String parseName(String obj)
	{
		String xml="";
		if(!obj.isEmpty())
			xml+="<dc:title><![CDATA["+obj+"]]></dc:title>";
		return xml;
	}

	public String parseTimePeriod(String obj)
	{
		String xml="";
		if(!obj.isEmpty())
			xml+="<dc:coverage><![CDATA["+obj+"]]></dc:coverage>";
		return xml;
	}

	public String parseFunding(String obj)
	{
		String xml="";
		if(!obj.isEmpty())
			xml+="<foaf:fundedBy><![CDATA["+obj+"]]></foaf:fundedBy>";
		return xml;
	}

	public String parseKindOfData(String obj)
	{
		String xml="";
		if(!obj.isEmpty())
			xml+="<dc:subject><![CDATA["+obj+"]]></dc:subject>";
		return xml;
	}

	public String parseNotes(String obj)
	{
		String xml="";
		if(!obj.isEmpty())
			xml+="<dc:description><![CDATA["+obj+"]]></dc:description>";
		return xml;
	}

	public String parseSpec(String obj)
	{
		String xml="";
		if(!obj.isEmpty())
			xml+="<dc:identifier><![CDATA["+obj+"]]></dc:identifier>";
		return xml;
	}

	public String parseCollection(JSONObject obj)
	{
		String xml="";

		String id="";
		String uri="";
		String type="";
		
		id=(String)obj.get("id").toString();
		uri=(String)obj.get("uri").toString();
		type=(String)obj.get("type").toString();
		

		xml+="<dcterms:isPartOf";

			if(!id.isEmpty())
				xml+=" xml:id=\""+id+"\"";
			if(!uri.isEmpty())
				xml+=" rdf:resource=\""+uri+"\"";
			if(!type.isEmpty())
				xml+=" type=\""+type+"\"";
			
		xml+="/>";
		return xml;
	}

	public String parseAffiliation(JSONObject obj)
	{
		String xml="";

		String id="";
		String uri="";
		String value="";
		String type="";
		
		id=(String)obj.get("id").toString();
		uri=(String)obj.get("uri").toString();
		type=(String)obj.get("type").toString();
		value=(String)obj.get("value").toString();

		xml+="<schema:affiliation";

			if(!id.isEmpty())
				xml+=" xml:id=\""+id+"\"";
			if(!uri.isEmpty())
				xml+=" rdf:resource=\""+uri+"\"";
			if(!type.isEmpty())
				xml+=" type=\""+type+"\"";
			
		xml+="><![CDATA["+value+"]]></schema:affiliation>";
		return xml;
	}
	
	
	public String parseAbout(JSONObject obj)
	{
		String xml="";

		xml+="<about>";
		
		try
		{
			JSONArray jresults=(JSONArray)obj.get("metadataSchema");
			Iterator<JSONObject> iterator=jresults.iterator();
			
			while(iterator.hasNext())
			{
				JSONObject objS=iterator.next();
				
				System.out.println("METADATASCHEA:"+objS.toString());
				
				xml+=parseMetadataSchema(objS);							
			}
		}
		catch(java.lang.ClassCastException e)
		{
			JSONObject objS=(JSONObject)obj.get("metadataSchema");
			
			xml+=parseMetadataSchema(objS);
		}

		String software="";
		String repo_name="";
		String handler="";
		
		software=(String)obj.get("software").toString();
		repo_name=(String)obj.get("repoName").toString();
		handler=(String)obj.get("handler").toString();
		
		if(!software.isEmpty())
			software=" software=\""+software+"\"";
		if(!repo_name.isEmpty())
			xml+="<dc:title><![CDATA["+repo_name+"]]></dc:title>";
		if(!handler.isEmpty())
			xml+="<dc:identifier scheme=\"dcterms:URI\""+software+"><![CDATA["+handler+"]]></dc:identifier>";
		
		xml+="</about>";
		
		return xml;
	}
	

	public String parseMetadataSchema(JSONObject obj)
	{
		String xml="";
		
		String value="";
		String uri="";
		
		value=(String)obj.get("name").toString();
		uri=(String)obj.get("uri").toString();
		
		xml+="<metadataNamespace";
		
		if(!value.isEmpty())
			xml+=" name=\""+value+"\"";
		

		xml+="><![CDATA["+uri+"]]></metadataNamespace>";
		
		return xml;
	}
	

	
}














