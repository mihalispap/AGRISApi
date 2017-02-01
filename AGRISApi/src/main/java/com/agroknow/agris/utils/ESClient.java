package com.agroknow.agris.utils;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

public class ESClient {
	
		//public static String server="localhost";
		public static String server1="83.212.100.195";
		public static String server2="83.212.101.19";
		public static String server3="52.18.30.225";
		
		public static Settings settings = ImmutableSettings.settingsBuilder()
	    	.put("cluster.name", "agroknow").build();
		public void ESClient()
		{

			settings = ImmutableSettings.settingsBuilder()
			        .put("cluster.name", "agroknow").build();
			client = new TransportClient(settings)
			        .addTransportAddress(new InetSocketTransportAddress(server1, 9300))
			        .addTransportAddress(new InetSocketTransportAddress(server2, 9300))			        
			        .addTransportAddress(new InetSocketTransportAddress(server3, 9300))
			        ;
		}
		
		public static Client client = new TransportClient(settings)
		        .addTransportAddress(new InetSocketTransportAddress(server1, 9300))
		        .addTransportAddress(new InetSocketTransportAddress(server2, 9300))
		        .addTransportAddress(new InetSocketTransportAddress(server3, 9300))
		        ;
	
}
