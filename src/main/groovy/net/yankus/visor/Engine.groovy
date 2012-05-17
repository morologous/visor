package net.yankus.visor

import org.elasticsearch.search.SearchHit
import groovy.util.Expando
import org.elasticsearch.action.index.IndexResponse
import groovy.util.logging.Log4j 

@Log4j
class Engine {

    def doInElasticSearch = { context, operation ->
        def datasource = ElasticSearchClientFactory.create context
        return operation.call(datasource.client)
    }

    def doQuery = { queryParam ->
        def context = ContextBuilder.build queryParam
        def flattenedParams = ElasticSearchMarshaller.marshallSearchParameters(context.parameters)
        log.debug flattenedParams
        doInElasticSearch(context) { client ->
            def search = client.search (({
                indices context.index
                types context.returnType.simpleName
                source {
                    query {
                        filtered {
                            query {
                                def reqs = []
                                flattenedParams.entrySet().each { entry ->
                                    reqs << field((entry.key): entry.value)
                                }      
                                must: reqs                          
                            }
                            filter = context.filters.newInstance(null, null).rehydrate(delegate, owner, thisObject)
                        }
                    }
                }  
            }))

            def results = new Expando()
            log.debug search.response
            results.response = search.response '5s'
            results.count = search.response.hits().totalHits()
            results.list = ElasticSearchMarshaller.unmarshallAll(search.response.hits, context)

            results
        }
    }

    def doIndex = { target -> 
        def context = ContextBuilder.build(target)
        log.debug "Indexing $context.parameters as id $target.id of type $context.returnType.simpleName into $context.index"
        doInElasticSearch(context) { client -> 
            def result = client.index {
                index context.index
                type context.returnType.simpleName
                id "$target.id"
                source context.parameters
            }
            
            result
        }

    }

}