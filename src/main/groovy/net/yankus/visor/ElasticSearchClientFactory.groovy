package net.yankus.visor

import groovy.util.Expando
import org.elasticsearch.groovy.client.GClient
import org.elasticsearch.groovy.node.GNode
import org.elasticsearch.groovy.node.GNodeBuilder
import static org.elasticsearch.groovy.node.GNodeBuilder.*
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.common.settings.ImmutableSettings

class ElasticSearchClientFactory {
    
    static def create = { context -> 
        // short circuit
        if (ElasticSearchClientHolder.INSTANCE.get().clients[context.returnType]) {
                return ElasticSearchClientHolder.INSTANCE.get().clients[context.returnType]
        }

        def datasource = new Expando()

        if (context.remoteAddresses.length > 0) {
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