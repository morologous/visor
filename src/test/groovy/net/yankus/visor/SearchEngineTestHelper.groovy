package net.yankus.visor

import static org.junit.Assert.*
import groovy.util.logging.Log4j 

@Log4j
class SearchEngineTestHelper {
    
    static def testESSettings = { 
        node { local = true } 
        http { enabled = false }
        path { data = './build/data' }
    }

    static def index = { bean ->
        def context = ContextBuilder.build(bean)
        def datasource = new ElasticSearchClientFactory().create(context) 

        def indexParams = Marshaller.marshall(bean, 'INDEX')
        log.debug "indexParams: $indexParams"
        def indexR = datasource.client.index {
            index context.index
            type context.returnType.simpleName
            id SearchEngineTestHelper.getId(bean)
            source indexParams
        }

        def response = indexR.response '5s'
        log.debug ("index response: $response.index/$response.type/$response.id")
        assertEquals bean.id, response.id

        snooze()

        response
    }

    static def snooze(time=2000) {
        try {
            Thread.sleep(time)
        } catch (InterruptedException ie) {
            log.warn 'Was rudely awakened.'
        }
    }

    static def get = { bean -> 
        def context = ContextBuilder.build(bean)
        def datasource = new ElasticSearchClientFactory().create(context)

        def getR = datasource.client.get {
            index context.index
            type context.returnType.simpleName
            id SearchEngineTestHelper.getId(bean)
        }

        def response = getR.response '5s'
        assertEquals bean.id, response.id
        assertNotNull response.source 
        log.debug "get: $response.source"
        response
    }

    static def delete = { bean -> 
        if (bean) {            
            def context = ContextBuilder.build(bean)
            def datasource = new ElasticSearchClientFactory().create(context)

            // TODO detect id
            def deleteR = datasource.client.delete {
                index context.index
                type context.returnType.simpleName
                id SearchEngineTestHelper.getId(bean)
            }

            def response = deleteR.response '5s'
            assertEquals '' + bean.id, response.id


            response
        }
    }

    private static def getId = { bean -> 
        def id = '' + bean.hashCode()
        def idField
        // intentionally naive about the possibility of multiple Id field annotation, for test purposes.
        bean.class.declaredFields.each {
            if (!idField && it.getAnnotation(Id)) {
                idField = it
            }
        }
        if (idField) {
            id = '' + bean[idField.name]
        } else if (bean.class.declaredFields.name.contains('id')) {
            id = '' + bean.id
        } 

        id
    }

    static def search = { bean -> 
        def context = ContextBuilder.build(bean)
        def datasource = new ElasticSearchClientFactory().create(context)

        def searchR = datasource.client.search {
            indices context.index
            types context.returnType.simpleName
            source {
                query {
                    ids(values:[bean.id])
                }
                highlight {
                    fields {
                        all { }
                    }
                }
            }
        } 

        def response = searchR.response '5s'       
        log.debug("get: $response")
    }

    static def showAll = { beanType ->
        def bean = beanType.newInstance()
        def context = ContextBuilder.build bean
        def datasource = new ElasticSearchClientFactory().create context

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