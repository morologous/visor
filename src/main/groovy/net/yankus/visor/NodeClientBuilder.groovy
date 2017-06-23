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
class NodeClientBuilder {
	
	def settings

	def build() {
		log.info 'Creating nodeBuilder client.'

        def datasource = new Expando()
        datasource.nodeBuilder = nodeBuilder()

        def nodeSettings = Settings.settingsBuilder()
        settings(nodeSettings)

        datasource.nodeBuilder.settings(nodeSettings) 
     
        datasource.node = datasource.nodeBuilder.node()
        datasource.client = datasource.node.client

        datasource.close = { datasource.node.stop().close() }  

        datasource
	}
}