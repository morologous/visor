package net.yankus.visor

import java.lang.annotation.Retention

import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target
import java.lang.annotation.ElementType
import static org.elasticsearch.index.query.QueryBuilders.*

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Field { 
    Class marshall() default { 
        def value = it.targetBean[it.fieldName]
        AnnotationDefaultClosureLogger.trace "Attempting to marshall $it"
        if (value instanceof Date) {
            value?.time
        } else if (value instanceof DateRange) {
                AnnotationDefaultClosureLogger.trace 'Marshalling to DateRange.'
                new DateRange(from:value?.from?.time, to:value?.to?.time)
        } else if (value instanceof Collection) {
            AnnotationDefaultClosureLogger.trace 'Marshalling to Collection.'
            def coll = []
            value.each { val -> 
                coll << Marshaller.marshall(val, it.mode)            
            }
            AnnotationDefaultClosureLogger.trace 'Marshalled Collection: $coll'
            coll
        } else if (Marshaller.isChildBean(value)) {
            AnnotationDefaultClosureLogger.trace 'Marshalling complex child type: ' + value.class
            Marshaller.marshall(value, it.mode)        
        } else {
            AnnotationDefaultClosureLogger.trace 'Performing default marshalling.'
            value
        }
    }
    Class unmarshall() default { 
        AnnotationDefaultClosureLogger.trace "Attempting to unmarshall $it"
        if (it.annotation?.type() == Date) {
            AnnotationDefaultClosureLogger.trace 'Unmarshalling to Date.'
            it.targetBean[it.fieldName] = new Date(it.fieldValue)
        } else if (it.fieldValue instanceof Collection) {
            AnnotationDefaultClosureLogger.trace 'Unmarshalling to Collection.'
            def coll = []
            it.fieldValue.each { val ->
                coll << Marshaller.unmarshallMap(val, it.annotation.type())
            }
            it.targetBean[it.fieldName] = coll
        } else if (Marshaller.isChildBean(it.annotation?.type().newInstance())) {
            AnnotationDefaultClosureLogger.trace '' + it.annotation.type() + ' is child bean -- performing Map unmarshalling on object type.'
            it.targetBean[it.fieldName] = Marshaller.unmarshallMap(it.fieldValue, it.annotation.type())
        } else {
            AnnotationDefaultClosureLogger.trace 'Performing default unmarshalling.'
            it.targetBean[it.fieldName] = it.fieldValue 
        }
        AnnotationDefaultClosureLogger.trace 'Unmarshalled value: ' + it.targetBean[it.fieldName]
    }
    Class applyToQuery() default { key, value, annotation ->
        AnnotationDefaultClosureLogger.trace "Applying $key : $value"
        if (value instanceof MultiSelect) {
            AnnotationDefaultClosureLogger.trace 'Applying MultiSelect'
            termsQuery key + annotation.inQueryFieldSuffix(), value.values as Object[]        
        } else if (value instanceof DateRange) {
            AnnotationDefaultClosureLogger.trace 'Applying DateRange'
            rangeQuery(key)
                .from(value.from)
                .to(value.to)
        } else {
            AnnotationDefaultClosureLogger.trace 'Performing default apply.'            
            def query = queryStringQuery '' + value
            query.defaultField = key
            query.analyzeWildcard = true

            query
        }
    }
    Class type() default java.lang.String 
    boolean highlight() default false
    String inQueryFieldSuffix() default ''
    boolean excludeFromResults() default false
}