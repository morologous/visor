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
class ThreadLocalClientFactory implements ElasticSearchClientFactory {
    
    def context

    GClient create() {
        // short circuit
        if (ElasticSearchClientHolder.INSTANCE.get().clients[context.returnType]) {
                log.debug "Returning pre-existing client for $context.returnType"
                return ElasticSearchClientHolder.INSTANCE.get().clients[context.returnType]
        }

        log.debug "Creating new client for $context.returnType"

        def datasource = new Expando()

        if (context.remoteAddresses.size() > 0) {
               datasource = new TransportClientBuilder(settings:context.settings.newInstance(null, null), remoteAddresses:context.remoteAddresses).build()
        } else {
               datasource = new NodeClientBuilder(settings:context.settings.newInstance(null, null)).build()
        }

        ElasticSearchClientHolder.INSTANCE.get().clients[context.returnType] = datasource.client

        datasource.client
    }
}