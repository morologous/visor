package net.yankus.visor

import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress

import groovy.util.logging.Log4j 


@Log4j
class TransportClientBuilder {
	
	def settings
	def remoteAddresses

	def build() {		
		log.info "Creating transportClient for ${remoteAddresses}"
        def settings = Settings.settingsBuilder()
		
		settings(settings)

		def datasource = new Expando()
        datasource.transportClient =  TransportClient.builder().settings(settings).build()
        remoteAddresses.each {
        	def (host, port) = it.split(':')
            datasource.transportClient.addTransportAddress(new InetSocketTransportAddress(host, port as int)) 
        }

        datasource.client = transportClient //new GClient(datasource.transportClient)
        datasource.close = { client.close() }

        datasource
	}

}