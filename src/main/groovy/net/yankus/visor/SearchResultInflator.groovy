package net.yankus.visor

import org.elasticsearch.search.SearchHit
import org.elasticsearch.search.SearchHits
import org.joda.time.format.ISODateTimeFormat
import groovy.util.logging.Log4j 
import groovy.util.Expando

@Log4j
class SearchResultInflator {
    
    def context

    private static def getField = { targetBean, fieldName -> 
        def field
        try {
            field = targetBean.class.getDeclaredField fieldName
            } catch (NoSuchFieldException nsfe) {
                log.warn ("Target bean $targetBean does not have field $fieldName")
            }
        field
    }

    static def inflateMap = { map, targetBean -> 
        map.entrySet().each {
            log.debug(it)
            def field = SearchResultInflator.getField(targetBean, it.key)
            if (field) {
                def annotation = field.getAnnotation Field
                if (annotation) {
                    def unmarshallContext = new Expando()
                    unmarshallContext.fieldName = field.name
                    unmarshallContext.targetBean = targetBean
                    unmarshallContext.fieldValue = it.value
                    unmarshallContext.field = field
                    unmarshallContext.annotation = annotation
                    annotation.unmarshall().newInstance(null, null).call(unmarshallContext)
                }
            }
        }
    }

    static def inflate = { SearchHit hit, context -> 
        def targetBean = context.returnType.newInstance()
        
        SearchResultInflator.inflateMap(hit.source, targetBean)

        targetBean
    }

    static def inflateAll = { SearchHits hits, context -> 
        def inflated = []
        hits.each { hit ->
            inflated << SearchResultInflator.inflate(hit, context)
        }

        inflated
    }

}