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
        def context = ContextBuilder.INSTANCE.build(queryParam)
        def queryMap = BeanInspector.inspect(queryParam)
        def type = queryParam.class.simpleName
        doInElasticSearch(context) { client ->
            log.debug("Searching for $context.index/$type/$queryMap")
            def search = client.search (({
                indices context.index
                types type
                source {
                    query {
                        filtered {
                            query {
                                def reqs = []
                                queryMap.entrySet().each { entry ->
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
            results.list = new SearchResultInflator(context:context).inflateAll(search.response.hits)

            results
        }
    }

    def doIndex = { target -> 
        def context = ContextBuilder.INSTANCE.build(target)

        doInElasticSearch(context) { client -> 
            def indexValues = [:]

            target.properties.each {
                if (!['id'].contains(it.key)) {
                    indexValues << it
                }
            }
            def result = client.index {
                index context.index
                type target.class.simpleName
                id "$target.id"
                source indexValues
            }
            
            result
        }

    }

}