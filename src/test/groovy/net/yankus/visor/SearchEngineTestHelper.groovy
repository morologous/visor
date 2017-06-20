package net.yankus.visor

import groovy.util.logging.Log4j
import static org.junit.Assert.*


import org.elasticsearch.action.admin.indices.refresh.RefreshRequest
import org.elasticsearch.common.settings.Settings;

import static org.elasticsearch.client.Requests.*
import static org.junit.Assert.*

@Log4j
class SearchEngineTestHelper {
    
    static final Closure testESSettings = { settings -> 
		settings.put('node.local',true)
		settings.put('discovery.cluster.name','visorTest')
		settings.put('http.enabled', false)
		settings.put('path.data','./data')
		settings.put('path.home','./build')
    }
    

    static def index = { bean ->
        def context = ContextBuilder.build(bean)
        def client = new ThreadLocalClientFactory(context:context).create() 

        def indexParams = Marshaller.marshall(bean, 'INDEX')
        log.debug "indexParams: $indexParams"
        
        def request = indexRequest(context.index).type(context.returnType.simpleName)
                                                 .id(SearchEngineTestHelper.getId(bean))
                                                 .source(indexParams)

        def indexR = client.index(request)                            

        def response = indexR.actionGet 5000
        log.debug ("index response: $response.index/$response.type/$response.id")
        assertEquals '' + bean.id, response.id
        
        refresh(bean)

        response
    }

    static def refresh(bean) {
        snooze()
        def context = ContextBuilder.build(bean)
        def client = new ThreadLocalClientFactory(context:context).create()
        client.admin().indices().refresh(new RefreshRequest(context.index)).actionGet 5000
    }    

    private static def snooze(time=1000) {        
        try {
            Thread.sleep(time)
        } catch (InterruptedException ie) {
            log.warn 'Was rudely awakened.'
        }
    }

    static def get = { bean -> 
        def context = ContextBuilder.build(bean)
        def client = new ThreadLocalClientFactory(context:context).create()

        def getR = client.get {
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
            def client = new ThreadLocalClientFactory(context:context).create()

            // TODO detect id
            def response = client.prepareDelete(context.index, context.returnType.simpleName, SearchEngineTestHelper.getId(bean)).get()

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
        def client = new ThreadLocalClientFactory(context:context).create()

        def searchR = client.search {
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
        def client = new ThreadLocalClientFactory(context:context).create()

        def searchR = client.search {
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