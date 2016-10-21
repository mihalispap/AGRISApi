package com.agroknow.agris.response.examples;

public class ResponseABSA {

	/*
{
	"total": 3,
	"page": 0,
	"page_size": 10,
	"time_elapsed": 0.003,
	"facets": "PSBMSGe1bQzLd5NBt2LoJdCt6DcEr0shmJC6Et0unSX63PwoQOPqc4rRen16W6dgxsUUVgwu/VYl$$$___L6RSZrLEoA==",
	"results": [{
		"opinions": {
			]
		},
		"score": 1.7320508,
		"detailed": {
			
		}
	}]
	}
	* 
	* */
	public int total;
	public int page;
	public int page_size;
	public int time_elapsed;
	public String facets;
	public ResponseOpinion results[];
	public double score;
	
	public ResponseABSA() {
		// TODO Auto-generated constructor stub
	}
	
}
