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
class ElasticSearchClientFactory {
    
    def create = { context -> 
        // short circuit
        if (ElasticSearchClientHolder.INSTANCE.get().clients[context.returnType]) {
                log.debug "Returning pre-existing client for $context.returnType"
                return ElasticSearchClientHolder.INSTANCE.get().clients[context.returnType]
        }

        log.debug "Creating new client for $context.returnType"

        def datasource = new Expando()

        if (context.remoteAddresses.size() > 0) {
                log.info "Creating transportClient for $context.remoteAddresses"
                def settings = ImmutableSettings.settingsBuilder()

                def settingsClosure = context.settings.newInstance(settings, settings).call()

                datasource.transportClient = new TransportClient(settings.build())
                context.remoteAddresses.each {
                        def (host, port) = it.split(':')
                        datasource.transportClient.addTransportAddress(new InetSocketTransportAddress(host, port as int)) 
                }

                datasource.client = new GClient(datasource.transportClient)

                datasource.close = { /* no op? */ }

        } else {
                log.info 'Creating nodeBuilder client.'
                datasource.nodeBuilder = nodeBuilder()

                def settingsClosure = context.settings.newInstance(datasource.nodeBuilder.getSettings(), datasource.nodeBuilder.getSettings())

                datasource.nodeBuilder.settings(settingsClosure) 
                
                datasource.node = datasource.nodeBuilder.node()
                datasource.client = datasource.node.client

                datasource.close = { datasource.node.stop().close() }                        
        }

        ElasticSearchClientHolder.INSTANCE.get().clients[context.returnType] = datasource

        datasource
    }
}