package net.yankus.visor

import groovy.util.Expando
import org.elasticsearch.client.Client
import org.elasticsearch.node.Node
import org.elasticsearch.node.NodeBuilder
import static org.elasticsearch.node.NodeBuilder.*
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.common.settings.Settings
import groovy.util.logging.Log4j 

@Log4j
class TransportClientBuilder {
	
	def settings
	def remoteAddresses

	def build() {
		
		log.info "Creating transportClient for ${remoteAddresses}"
        def immutableSettings = Settings.settingsBuilder()

        def settingsClosure = settings.rehydrate(immutableSettings, immutableSettings, immutableSettings).call()
		def datasource = new Expando()
        datasource.transportClient =  TransportClient.builder().settings(immutableSettings).build()
        remoteAddresses.each {
        	def (host, port) = it.split(':')
            datasource.transportClient.addTransportAddress(new InetSocketTransportAddress(host, port as int)) 
        }

        datasource.client = transportClient //new GClient(datasource.transportClient)
        datasource.close = { client.close() }

        datasource
	}

}