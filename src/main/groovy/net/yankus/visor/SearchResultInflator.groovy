package net.yankus.visor

import org.elasticsearch.search.SearchHit
import org.elasticsearch.search.SearchHits

class SearchResultInflator {
    
    def context

    def inflate = { SearchHit hit -> 
        def result = context.returnType.newInstance()
        hit.source.entrySet().each {
            println it
            if (result.class.declaredFields.name.contains(it.key) && !['metaClass', 'class'].contains(it.key)) {
                result[it.key] = it.value
            }
/**
            def key = it.key
            def value = it.value
            def setterName = 'set' + key[0].toUpperCase() + key[1..-1]
            def setter = context.returnType.methods.find { it.name == setterName }
            if (setter && value) {
                setter.invoke(result, value)
            }
**/
        }
        result
    }

    def inflateAll = { SearchHits hits -> 
        def inflated = []
        hits.each { hit ->
            inflated << inflate(hit)
        }

        inflated
    }

}