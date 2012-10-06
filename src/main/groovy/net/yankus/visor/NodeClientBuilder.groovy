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
class NodeClientBuilder {
	
	def settings

	def build() {
		log.info 'Creating nodeBuilder client.'
        def datasource = new Expando()
        datasource.nodeBuilder = nodeBuilder()

        def settingsClosure = settings.rehydrate(datasource.nodeBuilder.getSettings(), datasource.nodeBuilder.getSettings(), datasource.nodeBuilder.getSettings())

        datasource.nodeBuilder.settings(settingsClosure) 
             
        datasource.node = datasource.nodeBuilder.node()
        datasource.client = datasource.node.client

        datasource.close = { datasource.node.stop().close() }  

        datasource
	}
}