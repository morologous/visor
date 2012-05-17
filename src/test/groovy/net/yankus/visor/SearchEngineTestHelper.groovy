package net.yankus.visor

import static org.junit.Assert.*
import groovy.util.logging.Log4j 

@Log4j
class SearchEngineTestHelper {
    
    static def index = { bean ->
        def context = ContextBuilder.build(bean)
        def datasource = ElasticSearchClientFactory.create(context) 

        // TODO: detect ID property
        def indexR = datasource.client.index {
            index context.index
            type context.returnType.simpleName
            id bean.id
            source context.parameters
        }

        def response = indexR.response '5s'
        log.debug ("index response: $response.index/$response.type/$response.id")
        assertEquals bean.id, response.id

        snooze()

        response
    }

    static def snooze(time=1000) {
        try {
            Thread.sleep(time)
        } catch (InterruptedException ie) {
            log.warn 'Was rudely awakened.'
        }
    }

    static def get = { bean -> 
        def context = ContextBuilder.build(bean)
        def datasource = ElasticSearchClientFactory.create(context)

        // TODO: detect ID property
        def getR = datasource.client.get {
            index context.index
            type context.returnType.simpleName
            id bean.id
        }

        def response = getR.response '5s'
        assertEquals bean.id, response.id
        assertNotNull response.source 
        log.debug "get: $response.source"
        response
    }

    static def delete = { bean -> 
        def context = ContextBuilder.build(bean)
        def datasource = ElasticSearchClientFactory.create(context)

        // TODO detect id
        def deleteR = datasource.client.delete {
            index context.index
            type context.returnType.simpleName
            id bean.id
        }

        def response = deleteR.response '5s'
        assertEquals bean.id, response.id


        response
    }

    static def search = { bean -> 
        def context = ContextBuilder.build(bean)
        def datasource = ElasticSearchClientFactory.create(context)

        def searchR = datasource.client.search {
            indices context.index
            types context.returnType.simpleName
            source {
                query {
                    ids(values:[bean.id])
                }
            }
        } 

        def response = searchR.response '5s'       
        log.debug("get: $response")
    }

    static def showAll = { beanType ->
        def bean = beanType.newInstance()
        def context = ContextBuilder.build bean
        def datasource = ElasticSearchClientFactory.create context

        def searchR = datasource.client.search {
            indices context.index
            types context.returnType.simpleName
            source {
                query {
                    match_all { }
                }
            }
        }

        def response = searchR.response '5s'
        log.debug "showAll: $response"
    }

}