package net.yankus.visor

import org.elasticsearch.search.SearchHits
import org.elasticsearch.search.SearchHit

class ElasticSearchMarshaller {
    
    private static def flattenParameter = {key, value ->
        def map = [:]
        if (value instanceof Collection) {
            value.each {
                map << ElasticSearchMarshaller.flattenParameter(key, it)                
            }
        } else if (value instanceof Map) {
            value.entrySet().each {
                map << ElasticSearchMarshaller.flattenParameter(key+'.'+it.key, it.value)
            }
        } else {
            map << [(key):value]
        }
        map
    }

    static def marshallSearchParameters = { parameters -> 
        def map = [:]
        parameters.entrySet().each {
            map << ElasticSearchMarshaller.flattenParameter(it.key, it.value)
        }
        map
    }

    static def findIdField = { bean -> 
        def idField
        bean.class.declaredFields.each {
            def annotation = it.getAnnotation Id 
            if (annotation) {
                if (idField) {
                    def className = bean.class
                    throw new IllegalStateException("Bean $className has more than one @Id field annotation.")
                }
                idField = it
            }
        }

        idField
    }

    static def unmarshall = { SearchHit hit, context ->
        def unmarshalled = Marshaller.unmarshall(hit.source, context.returnType)
        
        // detect and set Id
        def idField = ElasticSearchMarshaller.findIdField(unmarshalled)
        if (idField) {
            unmarshalled[idField.name] = hit.id
        }

        unmarshalled
    }

    static def unmarshallAll = { SearchHits hits, context -> 
        def unmarshalled = []
        hits.each { hit ->
            unmarshalled << ElasticSearchMarshaller.unmarshall(hit, context)
        }

        unmarshalled
    }
}