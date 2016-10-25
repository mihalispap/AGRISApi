package com.agroknow.agris.utils;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

public class ESClient {
	
	public static String server="localhost";
	//public static String server="83.212.100.195";
	public static Settings settings = ImmutableSettings.settingsBuilder()
    	.put("cluster.name", "agroknow").build();
	public void ESClient()
	{

		settings = ImmutableSettings.settingsBuilder()
		        .put("cluster.name", "agroknow").build();
		client = new TransportClient(settings)
		        .addTransportAddress(new InetSocketTransportAddress(server, 9300));
	}
	
	public static Client client = new TransportClient(settings)
	        .addTransportAddress(new InetSocketTransportAddress(server, 9300));
	
}
