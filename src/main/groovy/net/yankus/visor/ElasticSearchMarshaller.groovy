package net.yankus.visor

import org.elasticsearch.search.SearchHits
import org.elasticsearch.search.SearchHit
import groovy.util.logging.Log4j 

@Log4j
class ElasticSearchMarshaller {
    
    private static def flattenParameter = {key, value ->
        def map = [:]
        log.debug "Flattening $key : $value"
        //log.debug 'value is ' + value.getClass()
        if (value instanceof Collection) {
            value.each {
                map << ElasticSearchMarshaller.flattenParameter(key, it)                
            }
        } else if (value instanceof Map) {
            value.entrySet().each {
                map << ElasticSearchMarshaller.flattenParameter(key+'.'+it.key, it.value)
            }
        } else if (value instanceof Expando) {
            //log.debug 'value.value is ' + value.value.getClass()
            // if the next step is a collection or map, go on, otherwise stop
            if (value.value instanceof Collection || value.value instanceof Map) {
                map << ElasticSearchMarshaller.flattenParameter(key, value.value)
            } else {
                map << [(key):value]
            }
        } else {
            //map << [(key):value]
            throw  new IllegalArgumentException("Value must be Expando or Collection or Map but was $value")
        }
        map
    }

    static def marshallSearchParameters = { parameters -> 
        def map = [:]
        log.debug "Marshalling search parameters: $parameters"
        parameters.entrySet().each {
            map << ElasticSearchMarshaller.flattenParameter(it.key, it.value)
        }
        map
    }

    static def findIdField = { bean -> 
        def fields = Marshaller.findFieldWithAnnotation(Id, bean) 
        if (fields.size() > 1) {
            def className = bean.getClass()
            throw new IllegalStateException("Bean $className has more than one @Id field annotation.")
        }
        if (fields.size() == 1) {
            return fields[0]
        } 
        return null
    }

    static def getIdValueFromBean = { bean ->
        def field = ElasticSearchMarshaller.findIdField bean
        def rawValue = bean[field.name]

        rawValue.toString()
    }

    static def unmarshall = { SearchHit hit, context ->
        log.debug "Unmarshalling hit: $hit"
        def unmarshalled = Marshaller.unmarshall(hit.source, context.returnType)
        
        // detect and set Id
        def idField = ElasticSearchMarshaller.findIdField(unmarshalled)
        if (idField) {
            unmarshalled[idField.name] = hit.id
        }

        unmarshalled.score = hit.score        


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