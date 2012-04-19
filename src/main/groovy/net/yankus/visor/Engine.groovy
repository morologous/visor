package net.yankus.visor

import org.elasticsearch.search.SearchHit

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

            
            return search.response          

        }
    }

}