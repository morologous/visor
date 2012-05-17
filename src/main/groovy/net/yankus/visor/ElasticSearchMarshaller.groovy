package net.yankus.visor

import org.elasticsearch.search.SearchHits
import org.elasticsearch.search.SearchHit

class ElasticSearchMarshaller {
    
    static def unmarshall = { SearchHit hit, context ->
        Marshaller.unmarshall(hit.source, context.returnType)
    }

    static def unmarshallAll = { SearchHits hits, context -> 
        def unmarshalled = []
        hits.each { hit ->
            unmarshalled << ElasticSearchMarshaller.unmarshall(hit, context)
        }

        unmarshalled
    }
}