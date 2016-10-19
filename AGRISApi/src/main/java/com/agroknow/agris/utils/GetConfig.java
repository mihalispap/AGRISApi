package com.agroknow.agris.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

public class GetConfig 
{

	public String getValue(String prop_name) throws IOException
	{
		String absolute_path=System.getProperty("user.dir")+System.getProperty("file.separator")+""
				+ "configuration"+System.getProperty("file.separator");
				
		if(!(new File(absolute_path).exists()))
				absolute_path=System.getProperty("user.dir")+System.getProperty("file.separator")+"";
		
		Properties prop = new Properties();
		InputStream input = null;
		input = new FileInputStream(absolute_path+"config.properties");

		// load a properties file
		prop.load(input);
		
		String value=prop.getProperty(prop_name);
		//System.out.println(value);

		return value;
		
	}
	
}
