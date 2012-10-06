package net.yankus.visor

import groovy.util.Expando
import org.elasticsearch.groovy.client.GClient
import org.elasticsearch.groovy.node.GNode
import org.elasticsearch.groovy.node.GNodeBuilder
import static org.elasticsearch.groovy.node.GNodeBuilder.*
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.common.settings.ImmutableSettings
import groovy.util.logging.Log4j 

@Log4j
class TransportClientBuilder {
	
	def settings
	def remoteAddresses

	def build() {
		
		log.info "Creating transportClient for ${remoteAddresses}"
        def immutableSettings = ImmutableSettings.settingsBuilder()

        def settingsClosure = settings.rehydrate(immutableSettings, immutableSettings, immutableSettings).call()
		def datasource = new Expando()
        datasource.transportClient = new TransportClient(immutableSettings.build())
        remoteAddresses.each {
        	def (host, port) = it.split(':')
            datasource.transportClient.addTransportAddress(new InetSocketTransportAddress(host, port as int)) 
        }

        datasource.client = new GClient(datasource.transportClient)
        datasource.close = { client.close() }

        datasource
	}

}