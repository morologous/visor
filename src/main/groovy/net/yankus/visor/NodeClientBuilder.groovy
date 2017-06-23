package net.yankus.visor

import org.elasticsearch.common.settings.Settings

import groovy.util.logging.Log4j 

@Log4j
class NodeClientBuilder {
	
	def settings

	def build() {
		log.info 'Creating nodeBuilder client.'

        def datasource = new Expando()
        datasource.nodeBuilder = org.elasticsearch.node.NodeBuilder.nodeBuilder()

        def nodeSettings = Settings.settingsBuilder()
        settings(nodeSettings)

        datasource.nodeBuilder.settings(nodeSettings) 
     
        datasource.node = datasource.nodeBuilder.node()
        datasource.client = datasource.node.client

        datasource.close = { datasource.node.stop().close() }  

        datasource
	}
}