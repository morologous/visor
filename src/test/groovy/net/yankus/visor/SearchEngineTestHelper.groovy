package net.yankus.visor

import org.elasticsearch.action.ListenableActionFuture
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest
import org.elasticsearch.action.get.GetResponse
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.Client
import org.elasticsearch.client.Requests
import org.elasticsearch.index.query.QueryBuilders

import groovy.util.logging.Log4j
import static org.junit.Assert.*
import net.yankus.visor.Visor

@Log4j
class SearchEngineTestHelper {
    
    static final Closure testESSettings = { settings -> 
		settings.put('node.local',true)
		settings.put('discovery.cluster.name','visorTest')
		settings.put('http.enabled', false)
		settings.put('path.data','./build/data')
		settings.put('path.home','./build')
    }
    

    static def index = { bean ->
        def context = ContextBuilder.build(bean)
        def client = new ThreadLocalClientFactory(context:context).create() 

        def indexParams = Marshaller.marshall(bean, 'INDEX')
        log.debug "indexParams: $indexParams"
        
        def request = Requests.indexRequest(context.index).type(context.returnType.simpleName)
                                                 .id(SearchEngineTestHelper.getId(bean))
                                                 .source(indexParams)

        def indexR = client.index(request)                            

        def response = indexR.get()
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

        ListenableActionFuture<GetResponse> future = client.prepareGet(context.index, context.returnType.simpleName, SearchEngineTestHelper.getId(bean)).execute()

        GetResponse response = future.get()
		
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
        Client client = new ThreadLocalClientFactory(context:context).create()

        def search = client.prepareSearch(context.index)
		.setTypes(context.returnType.simpleName)
		.setQuery(QueryBuilders.idsQuery(bean.id))
		
		search.highlightBuilder().field("*")
		
		def future = search.execute()
		
        def response = future.actionGet()
		log.debug("get: $response")
		
		response
    }

    static def showAll = { beanType ->
        def bean = beanType.newInstance()
        def context = ContextBuilder.build bean
        def client = new ThreadLocalClientFactory(context:context).create()

        def search = client.prepareSearch(context.index)
						    .setTypes(context.returnType.simpleName)
							.execute()
		
		SearchResponse response = search.actionGet()
        
        log.debug "showAll: $response"
		
		response
    }

}