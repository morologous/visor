package net.yankus.visor

import org.elasticsearch.search.SearchHit
import groovy.util.Expando

class Engine {

    def doInElasticSearch = { context, operation ->
        def datasource = ElasticSearchClientFactory.create context
        try {
                return operation.call(datasource.client)
            } finally {
                datasource.close()        
            }
    }

    def doQuery = { queryParam ->
        def context = ContextBuilder.build(queryParam)
        def queryMap = BeanInspector.inspect(queryParam)
        doInElasticSearch(context) { client ->
            def search = client.search (({
                indices context['index']
                types "testData"
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

            results.response = search.response
            results.count = search.response.hits().totalHits()
            results.list = new SearchResultInflator(context:context).inflateAll(search.response.hits)

            results
        }
    }

}