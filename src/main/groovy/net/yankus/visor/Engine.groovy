package net.yankus.visor

import org.elasticsearch.search.SearchHit

class Engine {
    
    def client

    def doQuery = { queryParam ->
        def context = ContextBuilder.build(queryParam)
        println (context)
        def queryMap = BeanInspector.inspect(queryParam)
        println (queryMap)
        def search = client.search {
            indices context['index']
            types "testData"
            source {
                query {
                   term(queryMap)
                }
            }
        }

        search.response.hits.each { SearchHit hit ->
            println ("Got hit $hit.id from $hit.index/$hit.type")
        }
    }

}