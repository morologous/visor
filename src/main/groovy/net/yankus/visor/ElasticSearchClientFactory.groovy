package net.yankus.visor

import org.elasticsearch.groovy.client.GClient

public interface ElasticSearchClientFactory {
	
	GClient create()

}