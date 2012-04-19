package net.yankus.visor

import groovy.util.Expando
import org.elasticsearch.groovy.node.GNode
import org.elasticsearch.groovy.node.GNodeBuilder
import static org.elasticsearch.groovy.node.GNodeBuilder.*

class ElasticSearchClientFactory {
    
    static def create = { context -> 

        def datasource = new Expando()
        datasource.nodeBuilder = nodeBuilder()

        def settingsClosure = context.settings.newInstance(datasource.nodeBuilder.getSettings(), datasource.nodeBuilder.getSettings())

        datasource.nodeBuilder.settings(settingsClosure) 
        
        datasource.node = datasource.nodeBuilder.node()
        datasource.client = datasource.node.client

        datasource.close = { datasource.node.close() }

        datasource
    }
}