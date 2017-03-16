package com.agroknow.agris.utils;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

//import org.apache.commons.lang.StringUtils;

public class ParseGET {

	public String parseFormat(HttpServletRequest request)
	{
		Enumeration<String> params=request.getParameterNames();
		String param="", param_value="";
		
		String title="";
		
		while(params.hasMoreElements())
		{
			param=params.nextElement();
			param_value=request.getParameter(param);
			
			if(param.equalsIgnoreCase("format"))
			{
				//title=StringUtils.trim(param_value);
				return param_value;
			}
		}
		
		return "";
	}

	public String parseAuth(HttpServletRequest request)
	{
		Enumeration<String> params=request.getParameterNames();
		String param="", param_value="";
		
		String title="";
		
		while(params.hasMoreElements())
		{
			param=params.nextElement();
			param_value=request.getParameter(param);
			
			if(param.equalsIgnoreCase("auth"))
			{
				//title=StringUtils.trim(param_value);
				return param_value;
			}
		}
		
		return "";
	}

	public String parseABSACategory(HttpServletRequest request)
	{
		Enumeration<String> params=request.getParameterNames();
		String param="", param_value="";
		
		String title="";
		
		while(params.hasMoreElements())
		{
			param=params.nextElement();
			param_value=request.getParameter(param);
			
			if(param.equalsIgnoreCase("aspect_category"))
			{
				//title=StringUtils.trim(param_value);
				return param_value;
			}
		}
		
		return "";
	}

	public String parseUserID(HttpServletRequest request)
	{
		Enumeration<String> params=request.getParameterNames();
		String param="", param_value="";
		
		String title="";
		
		while(params.hasMoreElements())
		{
			param=params.nextElement();
			param_value=request.getParameter(param);
			
			if(param.equalsIgnoreCase("user_id"))
			{
				//title=StringUtils.trim(param_value);
				return param_value;
			}
		}
		
		return "";
	}

	public String parseUserName(HttpServletRequest request)
	{
		Enumeration<String> params=request.getParameterNames();
		String param="", param_value="";
		
		String title="";
		
		while(params.hasMoreElements())
		{
			param=params.nextElement();
			param_value=request.getParameter(param);
			
			if(param.equalsIgnoreCase("username"))
			{
				//title=StringUtils.trim(param_value);
				return param_value;
			}
		}
		
		return "";
	}

	public String parseFullText(HttpServletRequest request)
	{
		Enumeration<String> params=request.getParameterNames();
		String param="", param_value="";
		
		String title="";
		
		while(params.hasMoreElements())
		{
			param=params.nextElement();
			param_value=request.getParameter(param);
			
			if(param.equalsIgnoreCase("fulltext"))
			{
				//title=StringUtils.trim(param_value);
				return param_value;
			}
		}
		
		return "";
	}

	public String parseUserGroup(HttpServletRequest request)
	{
		Enumeration<String> params=request.getParameterNames();
		String param="", param_value="";
		
		String title="";
		
		while(params.hasMoreElements())
		{
			param=params.nextElement();
			param_value=request.getParameter(param);
			
			if(param.equalsIgnoreCase("user-group"))
			{
				//title=StringUtils.trim(param_value);
				return param_value;
			}
		}
		
		return "";
	}

	public String parsePolarity(HttpServletRequest request)
	{
		Enumeration<String> params=request.getParameterNames();
		String param="", param_value="";
		
		String title="";
		
		while(params.hasMoreElements())
		{
			param=params.nextElement();
			param_value=request.getParameter(param);
			
			if(param.equalsIgnoreCase("polarity"))
			{
				//title=StringUtils.trim(param_value);
				return param_value;
			}
		}
		
		return "";
	}

	public String parseFID(HttpServletRequest request)
	{
		Enumeration<String> params=request.getParameterNames();
		String param="", param_value="";
		
		String title="";
		
		while(params.hasMoreElements())
		{
			param=params.nextElement();
			param_value=request.getParameter(param);
			
			if(param.equalsIgnoreCase("fid"))
			{
				//title=StringUtils.trim(param_value);
				return param_value;
			}
		}
		
		return "";
	}

	public String parseApiKey(HttpServletRequest request)
	{
		Enumeration<String> params=request.getParameterNames();
		String param="", param_value="";
		
		String title="";
		
		while(params.hasMoreElements())
		{
			param=params.nextElement();
			param_value=request.getParameter(param);
			
			if(param.equalsIgnoreCase("apikey"))
			{
				//title=StringUtils.trim(param_value);
				return param_value;
			}
		}
		
		return "";
	}

	public String parseSource(HttpServletRequest request)
	{
		Enumeration<String> params=request.getParameterNames();
		String param="", param_value="";
		
		String title="";
		
		while(params.hasMoreElements())
		{
			param=params.nextElement();
			param_value=request.getParameter(param);
			
			if(param.equalsIgnoreCase("source"))
			{
				//title=StringUtils.trim(param_value);
				return param_value;
			}
		}
		
		return "";
	}

	public String parseSource(String input)
	{
		String value="";
		
		input=input.replace("/search?", "");
		input=input.replace("/agris-1.0", "");
		
		String values[] = input.split("&");
		
		for(int i=0;i<values.length;i++)
		{
			String inner[]=values[i].split("=");
			
			if(inner[0].equals("source"))
			{
				if(inner.length>1)
					return inner[1];
				return "";
			}
		}
		
		return "";
	}

	public String parseABSACategory(String input)
	{
		String value="";
		
		input=input.replace("/search?", "");
		input=input.replace("/agris-1.0", "");
		
		String values[] = input.split("&");
		
		for(int i=0;i<values.length;i++)
		{
			String inner[]=values[i].split("=");
			
			if(inner[0].equals("aspect_category"))
			{
				if(inner.length>1)
					return inner[1];
				return "";
			}
		}
		
		return "";
	}

	public String parseUserID(String input)
	{
		String value="";
		
		input=input.replace("/search?", "");
		input=input.replace("/agris-1.0", "");
		
		String values[] = input.split("&");
		
		for(int i=0;i<values.length;i++)
		{
			String inner[]=values[i].split("=");
			
			if(inner[0].equals("user_id"))
			{
				if(inner.length>1)
					return inner[1];
				return "";
			}
		}
		
		return "";
	}

	public String parseUserName(String input)
	{
		String value="";
		
		input=input.replace("/search?", "");
		input=input.replace("/agris-1.0", "");
		
		String values[] = input.split("&");
		
		for(int i=0;i<values.length;i++)
		{
			String inner[]=values[i].split("=");
			
			if(inner[0].equals("username"))
			{
				if(inner.length>1)
					return inner[1];
				return "";
			}
		}
		
		return "";
	}

	public String parseFullText(String input)
	{
		String value="";
		
		input=input.replace("/search?", "");
		input=input.replace("/agris-1.0", "");
		
		String values[] = input.split("&");
		
		for(int i=0;i<values.length;i++)
		{
			String inner[]=values[i].split("=");
			
			if(inner[0].equals("fulltext"))
			{
				if(inner.length>1)
					return inner[1];
				return "";
			}
		}
		
		return "";
	}

	public String parsePolarity(String input)
	{
		String value="";
		
		input=input.replace("/search-absa?", "");
		input=input.replace("/agris-1.0", "");
		
		String values[] = input.split("&");
		
		for(int i=0;i<values.length;i++)
		{
			String inner[]=values[i].split("=");
			
			if(inner[0].equals("polarity"))
			{
				if(inner.length>1)
					return inner[1];
				return "";
			}
		}
		
		return "";
	}

	public String parseUserGroup(String input)
	{
		String value="";
		
		input=input.replace("/search-absa?", "");
		input=input.replace("/agris-1.0", "");
		
		String values[] = input.split("&");
		
		for(int i=0;i<values.length;i++)
		{
			String inner[]=values[i].split("=");
			
			if(inner[0].equals("user_group"))
			{
				if(inner.length>1)
					return inner[1];
				return "";
			}
		}
		
		return "";
	}

	public String parseToDate(String input)
	{
		String value="";
		
		input=input.replace("/search-absa?", "");
		input=input.replace("/agris-1.0", "");
		
		String values[] = input.split("&");
		
		for(int i=0;i<values.length;i++)
		{
			String inner[]=values[i].split("=");
			
			if(inner[0].equals("to"))
			{
				if(inner.length>1)
					return inner[1];
				return "";
			}
		}
		
		return "";
	}
	

	public String parseFromDate(String input)
	{
		String value="";
		
		input=input.replace("/search-absa?", "");
		input=input.replace("/agris-1.0", "");
		
		String values[] = input.split("&");
		
		for(int i=0;i<values.length;i++)
		{
			String inner[]=values[i].split("=");
			
			if(inner[0].equals("from"))
			{
				if(inner.length>1)
					return inner[1];
				return "";
			}
		}
		
		return "";
	}
	
	public String parseAll(HttpServletRequest request)
	{

		Enumeration<String> params=request.getParameterNames();
		String param="", param_value="";
		
		String title="";
		String type="";
		
		while(params.hasMoreElements())
		{
			param=params.nextElement();
			param_value=request.getParameter(param);
			
			if(param.equalsIgnoreCase("title"))
				//title=StringUtils.trim(param_value);
				title=param_value;
			else if(param.equalsIgnoreCase("type"))
				//type=StringUtils.trim(param_value);
				type=param_value;
		}
		
		return "";
	}

	public String parseTitle(HttpServletRequest request)
	{
		Enumeration<String> params=request.getParameterNames();
		String param="", param_value="";
		
		String title="";
		
		while(params.hasMoreElements())
		{
			param=params.nextElement();
			param_value=request.getParameter(param);
			
			if(param.equalsIgnoreCase("title"))
			{
				//title=StringUtils.trim(param_value);
				return param_value;
			}
		}
		
		return "";
		
	}

	public String parseEntityType(HttpServletRequest request)
	{
		Enumeration<String> params=request.getParameterNames();
		String param="", param_value="";
		
		String title="";
		
		while(params.hasMoreElements())
		{
			param=params.nextElement();
			param_value=request.getParameter(param);
			
			if(param.equalsIgnoreCase("entity-type"))
			{
				//title=StringUtils.trim(param_value);
				return param_value;
			}
		}
		
		return "";
		
	}

	public String parseCollection(HttpServletRequest request)
	{
		Enumeration<String> params=request.getParameterNames();
		String param="", param_value="";
		
		String title="";
		
		while(params.hasMoreElements())
		{
			param=params.nextElement();
			param_value=request.getParameter(param);
			
			if(param.equalsIgnoreCase("collection"))
			{
				//title=StringUtils.trim(param_value);
				return param_value;
			}
		}
		
		return "";
		
	}

	public String parseOrganizer(HttpServletRequest request)
	{
		Enumeration<String> params=request.getParameterNames();
		String param="", param_value="";
		
		String title="";
		
		while(params.hasMoreElements())
		{
			param=params.nextElement();
			param_value=request.getParameter(param);
			
			if(param.equalsIgnoreCase("organizer"))
			{
				//title=StringUtils.trim(param_value);
				return param_value;
			}
		}
		
		return "";
		
	}
	
	public String parseCourseType(HttpServletRequest request)
	{
		Enumeration<String> params=request.getParameterNames();
		String param="", param_value="";
		
		String title="";
		
		while(params.hasMoreElements())
		{
			param=params.nextElement();
			param_value=request.getParameter(param);
			
			if(param.equalsIgnoreCase("course_type"))
			{
				//title=StringUtils.trim(param_value);
				return param_value;
			}
		}
		
		return "";
		
	}
	
	public String parseCourseTopic(HttpServletRequest request)
	{
		System.out.println(request.getParameterNames());
		Enumeration<String> params=request.getParameterNames();
		String param="", param_value="";
		
		String title="";
		
		while(params.hasMoreElements())
		{
			param=params.nextElement();
			param_value=request.getParameter(param);
			
			if(param.equalsIgnoreCase("topics"))
			{
				//title=StringUtils.trim(param_value);
				return param_value;
			}
		}
		
		return "";
		
	}
	
	
	public String parseCourseStartDate(HttpServletRequest request)
	{
		Enumeration<String> params=request.getParameterNames();
		String param="", param_value="";
		
		String title="";
		
		while(params.hasMoreElements())
		{
			param=params.nextElement();
			param_value=request.getParameter(param);
			
			if(param.equalsIgnoreCase("start_date"))
			{
				//title=StringUtils.trim(param_value);
				return param_value;
			}
		}
		
		return "";
		
	}
	
	public String parseAuthor(HttpServletRequest request)
	{
		Enumeration<String> params=request.getParameterNames();
		String param="", param_value="";
		
		String title="";
		
		while(params.hasMoreElements())
		{
			param=params.nextElement();
			param_value=request.getParameter(param);
			
			if(param.equalsIgnoreCase("author"))
			{
				//title=StringUtils.trim(param_value);
				return param_value;
			}
		}
		
		return "";
		
	}


	public String parseAuthor(String input)
	{
		String value="";
		
		input=input.replace("/search?", "");
		input=input.replace("/agris-1.0", "");
		
		String values[] = input.split("&");
		
		for(int i=0;i<values.length;i++)
		{
			String inner[]=values[i].split("=");
			
			if(inner[0].equals("author"))
			{
				if(inner.length>1)
					return inner[1];
				return "";
			}
		}
		
		return "";
	}
	

	public String parseLanguage(HttpServletRequest request)
	{
		Enumeration<String> params=request.getParameterNames();
		String param="", param_value="";
		
		String title="";
		
		while(params.hasMoreElements())
		{
			param=params.nextElement();
			param_value=request.getParameter(param);
			
			if(param.equalsIgnoreCase("language"))
			{
				//title=StringUtils.trim(param_value);
				return param_value;
			}
		}
		
		return "";
		
	}

	public String parseTKind(HttpServletRequest request)
	{
		Enumeration<String> params=request.getParameterNames();
		String param="", param_value="";
		
		String title="";
		
		while(params.hasMoreElements())
		{
			param=params.nextElement();
			param_value=request.getParameter(param);
			
			if(param.equalsIgnoreCase("training_kind"))
			{
				//title=StringUtils.trim(param_value);
				return param_value;
			}
		}
		
		return "";
		
	}

	public String parseIndustry(HttpServletRequest request)
	{
		Enumeration<String> params=request.getParameterNames();
		String param="", param_value="";
		
		String title="";
		
		while(params.hasMoreElements())
		{
			param=params.nextElement();
			param_value=request.getParameter(param);
			
			if(param.equalsIgnoreCase("industry"))
			{
				//title=StringUtils.trim(param_value);
				return param_value;
			}
		}
		
		return "";
		
	}

	public String parseLCountry(HttpServletRequest request)
	{
		Enumeration<String> params=request.getParameterNames();
		String param="", param_value="";
		
		String title="";
		
		while(params.hasMoreElements())
		{
			param=params.nextElement();
			param_value=request.getParameter(param);
			
			if(param.equalsIgnoreCase("location_country"))
			{
				//title=StringUtils.trim(param_value);
				return param_value;
			}
		}
		
		return "";
		
	}

	public String parseARights(HttpServletRequest request)
	{
		Enumeration<String> params=request.getParameterNames();
		String param="", param_value="";
		
		String title="";
		
		while(params.hasMoreElements())
		{
			param=params.nextElement();
			param_value=request.getParameter(param);
			
			if(param.equalsIgnoreCase("access_rights"))
			{
				//title=StringUtils.trim(param_value);
				return param_value;
			}
		}
		
		return "";
		
	}

	public String parseCWorkshop(HttpServletRequest request)
	{
		Enumeration<String> params=request.getParameterNames();
		String param="", param_value="";
		
		String title="";
		
		while(params.hasMoreElements())
		{
			param=params.nextElement();
			param_value=request.getParameter(param);
			
			if(param.equalsIgnoreCase("conference_workshop"))
			{
				//title=StringUtils.trim(param_value);
				return param_value;
			}
		}
		
		return "";
		
	}

	public String parseLRegion(HttpServletRequest request)
	{
		Enumeration<String> params=request.getParameterNames();
		String param="", param_value="";
		
		String title="";
		
		while(params.hasMoreElements())
		{
			param=params.nextElement();
			param_value=request.getParameter(param);
			
			if(param.equalsIgnoreCase("location_region"))
			{
				//title=StringUtils.trim(param_value);
				return param_value;
			}
		}
		
		return "";
		
	}

	public String parseLState(HttpServletRequest request)
	{
		Enumeration<String> params=request.getParameterNames();
		String param="", param_value="";
		
		String title="";
		
		while(params.hasMoreElements())
		{
			param=params.nextElement();
			param_value=request.getParameter(param);
			
			if(param.equalsIgnoreCase("location_state"))
			{
				//title=StringUtils.trim(param_value);
				return param_value;
			}
		}
		
		return "";
		
	}

	public String parseLanguage(String input)
	{
		String value="";
		
		input=input.replace("/search?", "");
		input=input.replace("/agris-1.0", "");
		
		String values[] = input.split("&");
		
		for(int i=0;i<values.length;i++)
		{
			String inner[]=values[i].split("=");
			
			if(inner[0].equals("language"))
			{
				if(inner.length>1)
					return inner[1];
				return "";
			}
		}
		
		return "";
	}
	

	public String parseLocation(HttpServletRequest request)
	{
		Enumeration<String> params=request.getParameterNames();
		String param="", param_value="";
		
		String title="";
		
		while(params.hasMoreElements())
		{
			param=params.nextElement();
			param_value=request.getParameter(param);
			
			if(param.equalsIgnoreCase("location"))
			{
				//title=StringUtils.trim(param_value);
				return param_value;
			}
		}
		
		return "";
		
	}

	public String parseRelation(HttpServletRequest request)
	{
		Enumeration<String> params=request.getParameterNames();
		String param="", param_value="";
		
		String title="";
		
		while(params.hasMoreElements())
		{
			param=params.nextElement();
			param_value=request.getParameter(param);
			
			if(param.equalsIgnoreCase("relation"))
			{
				//title=StringUtils.trim(param_value);
				return param_value;
			}
		}
		
		return "";
		
	}
	public int parsePage(HttpServletRequest request)
	{
		Enumeration<String> params=request.getParameterNames();
		String param="", param_value="";
		
		String title="";
		
		while(params.hasMoreElements())
		{
			param=params.nextElement();
			param_value=request.getParameter(param);
			
			if(param.equalsIgnoreCase("page"))
			{
				//title=StringUtils.trim(param_value);
				return Integer.valueOf(param_value);
			}
		}
		
		return 0;
		
	}

	public boolean parseCache(HttpServletRequest request)
	{
		Enumeration<String> params=request.getParameterNames();
		String param="", param_value="";
		
		String title="";
		
		while(params.hasMoreElements())
		{
			param=params.nextElement();
			param_value=request.getParameter(param);
			
			if(param.equalsIgnoreCase("cache"))
			{
				//title=StringUtils.trim(param_value);
				return Boolean.valueOf(param_value);
			}
		}
		
		return true;
		
	}

	public String parseKeyword(HttpServletRequest request)
	{
		Enumeration<String> params=request.getParameterNames();
		String param="", param_value="";
		
		String keyword="";
		
		while(params.hasMoreElements())
		{
			param=params.nextElement();
			param_value=request.getParameter(param);
			
			if(param.equalsIgnoreCase("keyword"))
			{
				//title=StringUtils.trim(param_value);
				return param_value;
			}
		}
		
		return "";
		
	}

	public String parseKeyword(String input)
	{
		String value="";
		
		input=input.replace("/search?", "");
		input=input.replace("/agris-1.0", "");
		
		String values[] = input.split("&");
		
		for(int i=0;i<values.length;i++)
		{
			String inner[]=values[i].split("=");
			
			if(inner[0].equals("keyword"))
			{
				if(inner.length>1)
					return inner[1];
				return "";
			}
		}
		
		return "";
	}
	
	public String parseKeywordEnhanced(HttpServletRequest request)
	{
		Enumeration<String> params=request.getParameterNames();
		String param="", param_value="";
		
		String keyword="";
		
		while(params.hasMoreElements())
		{
			param=params.nextElement();
			param_value=request.getParameter(param);
			
			if(param.equalsIgnoreCase("freetext"))
			{
				//title=StringUtils.trim(param_value);
				return param_value;
			}
		}
		
		return "";
		
	}

	public String parseKeywordEnhanced(String input)
	{
		String value="";
		
		input=input.replace("/search?", "");
		input=input.replace("/agris-1.0", "");
		
		String values[] = input.split("&");
		
		for(int i=0;i<values.length;i++)
		{
			String inner[]=values[i].split("=");
			
			if(inner[0].equals("freetext"))
			{
				if(inner.length>1)
					return inner[1];
				return "";
			}
		}
		
		return "";
	}
	
	public String parseCollectionID(HttpServletRequest request)
	{
		Enumeration<String> params=request.getParameterNames();
		String param="", param_value="";
		
		String keyword="";
		
		while(params.hasMoreElements())
		{
			param=params.nextElement();
			param_value=request.getParameter(param);
			
			if(param.equalsIgnoreCase("id"))
			{
				//title=StringUtils.trim(param_value);
				return param_value;
			}
		}
		
		return "";
		
	}


	public String parseID(HttpServletRequest request)
	{
		Enumeration<String> params=request.getParameterNames();
		String param="", param_value="";
		
		String keyword="";
		
		while(params.hasMoreElements())
		{
			param=params.nextElement();
			param_value=request.getParameter(param);
			
			if(param.equalsIgnoreCase("id"))
			{
				//title=StringUtils.trim(param_value);
				return param_value;
			}
		}
		
		return "";
		
	}


	public String parseISO(HttpServletRequest request)
	{
		Enumeration<String> params=request.getParameterNames();
		String param="", param_value="";
		
		String keyword="";
		
		while(params.hasMoreElements())
		{
			param=params.nextElement();
			param_value=request.getParameter(param);
			
			if(param.equalsIgnoreCase("iso"))
			{
				//title=StringUtils.trim(param_value);
				return param_value;
			}
		}
		
		return "";
		
	}


	public String parseFromDate(HttpServletRequest request)
	{
		Enumeration<String> params=request.getParameterNames();
		String param="", param_value="";
		
		String title="";
		
		while(params.hasMoreElements())
		{
			param=params.nextElement();
			param_value=request.getParameter(param);
			
			if(param.equalsIgnoreCase("from"))
			{
				//title=StringUtils.trim(param_value);
				return param_value;
			}
		}
		
		return "";
		
	}
	
	public String parseURI(HttpServletRequest request)
	{
		Enumeration<String> params=request.getParameterNames();
		String param="", param_value="";
		
		String title="";
		
		while(params.hasMoreElements())
		{
			param=params.nextElement();
			param_value=request.getParameter(param);
			
			if(param.equalsIgnoreCase("uri"))
			{
				//title=StringUtils.trim(param_value);
				return param_value;
			}
		}
		
		return "";
		
	}
	public String parseToDate(HttpServletRequest request)
	{
		Enumeration<String> params=request.getParameterNames();
		String param="", param_value="";
		
		String title="";
		
		while(params.hasMoreElements())
		{
			param=params.nextElement();
			param_value=request.getParameter(param);
			
			if(param.equalsIgnoreCase("to"))
			{
				//title=StringUtils.trim(param_value);
				return param_value;
			}
		}
		
		return "";
		
	}
	public String parseSubject(HttpServletRequest request)
	{
		Enumeration<String> params=request.getParameterNames();
		String param="", param_value="";
		
		String title="";
		
		while(params.hasMoreElements())
		{
			param=params.nextElement();
			param_value=request.getParameter(param);
			
			if(param.equalsIgnoreCase("subject"))
			{
				//title=StringUtils.trim(param_value);
				return param_value;
			}
		}
		
		return "";
		
	}
	

	public String parseSubject(String input)
	{
		String value="";
		
		input=input.replace("/search?", "");
		input=input.replace("/agris-1.0", "");
		
		String values[] = input.split("&");
		
		for(int i=0;i<values.length;i++)
		{
			String inner[]=values[i].split("=");
			
			if(inner[0].equals("subject"))
			{
				if(inner.length>1)
					return inner[1];
				return "";
			}
		}
		
		return "";
	}
	
	
	public String parseType(HttpServletRequest request)
	{
		Enumeration<String> params=request.getParameterNames();
		String param="", param_value="";
		
		String type="";
		
		while(params.hasMoreElements())
		{
			param=params.nextElement();
			param_value=request.getParameter(param);
			
			if(param.equalsIgnoreCase("type"))
			{
				//type=StringUtils.trim(param_value);
				return param_value;
			}
		}
		
		return "";
		
	}
	

	public String parseType(String input)
	{
		String value="";
		
		input=input.replace("/search?", "");
		input=input.replace("/agris-1.0", "");
		
		String values[] = input.split("&");
		
		for(int i=0;i<values.length;i++)
		{
			String inner[]=values[i].split("=");
			
			if(inner[0].equals("type"))
			{
				if(inner.length>1)
					return inner[1];
				return "";
			}
		}
		
		return "";
	}
	
	
}
