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
            def search = client.search {
                indices context['index']
                types "testData"
                source {
                    query {
                       term(queryMap)
                    }
                }
            }

            
            def results = new Expando()

            results.response = search.response
            results.list = new SearchResultInflator(context:context).inflateAll(search.response.hits)

            results
        }
    }

}