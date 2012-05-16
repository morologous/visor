package net.yankus.visor

import groovy.util.Expando
import org.elasticsearch.groovy.node.GNode
import org.elasticsearch.groovy.node.GNodeBuilder
import static org.elasticsearch.groovy.node.GNodeBuilder.*
import org.elasticsearch.client.transport.TransportClient

class ElasticSearchClientFactory {
    
    static def create = { context -> 
        // short circuit
        if (ElasticSearchClientHolder.INSTANCE.get().clients[context.returnType]) {
                return ElasticSearchClientHolder.INSTANCE.get().clients[context.returnType]
        }

        def datasource = new Expando()

        //if (context.remote) {
        //        new TransportClient()
        //} else {
                datasource.nodeBuilder = nodeBuilder()

                def settingsClosure = context.settings.newInstance(datasource.nodeBuilder.getSettings(), datasource.nodeBuilder.getSettings())

                datasource.nodeBuilder.settings(settingsClosure) 
                
                datasource.node = datasource.nodeBuilder.node()
                datasource.client = datasource.node.client

                datasource.close = { datasource.node.stop().close() }                        
        //}

        ElasticSearchClientHolder.INSTANCE.get().clients[context.returnType] = datasource

        datasource
    }
}