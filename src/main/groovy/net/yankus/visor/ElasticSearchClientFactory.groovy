package net.yankus.visor

import org.elasticsearch.client.Client

public interface ElasticSearchClientFactory {
	
	Client create()

}