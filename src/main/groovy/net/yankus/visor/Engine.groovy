package net.yankus.visor

import org.elasticsearch.search.SearchHit

class Engine {
    
    def client

    def query = { queryParam ->
        def context = ContextBuilder.build(queryParam)
        def query = BeanInspector.inspect(queryParam)
        def search = client.search {
            indicies context.index
            source {
                query {
                    term(query)
                }
            }
        }

        search.response.hits.each { SearchHit hit ->
            println ("Got hit $hit.id from $hit.index/$hit.type")
        }
    }

}