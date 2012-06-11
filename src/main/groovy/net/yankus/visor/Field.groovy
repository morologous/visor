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
        AnnotationDefaultClosureLogger.debug "Attempting to marshall $it"
        if (value instanceof Date) {
            value?.time
        } else if (value instanceof DateRange) {
                AnnotationDefaultClosureLogger.debug 'Marshalling to DateRange.'
                new DateRange(from:value?.from?.time, to:value?.to?.time)
        } else if (value instanceof Collection) {
            AnnotationDefaultClosureLogger.debug 'Marshalling to Collection.'
            def coll = []
            value.each { val -> 
                coll << Marshaller.marshall(val, it.mode)            
            }
            AnnotationDefaultClosureLogger.debug 'Marshalled Collection: $coll'
            coll
        } else if (Marshaller.isChildBean(value)) {
            AnnotationDefaultClosureLogger.debug 'Marshalling complex child type: ' + value.class
            Marshaller.marshall(value, it.mode)        
        } else {
            AnnotationDefaultClosureLogger.debug 'Performing default marshalling.'
            value
        }
    }
    Class unmarshall() default { 
        AnnotationDefaultClosureLogger.debug "Attempting to unmarshall $it"
        if (it.annotation?.type() == Date) {
            AnnotationDefaultClosureLogger.debug 'Unmarshalling to Date.'
            it.targetBean[it.fieldName] = new Date(it.fieldValue)
        } else if (it.fieldValue instanceof Collection) {
            AnnotationDefaultClosureLogger.debug 'Unmarshalling to Collection.'
            def coll = []
            it.fieldValue.each { val ->
                coll << Marshaller.unmarshall(val, it.annotation.type())
            }
            it.targetBean[it.fieldName] = coll
        } else if (Marshaller.isChildBean(it.annotation?.type().newInstance())) {
            AnnotationDefaultClosureLogger.debug '' + it.annotation.type() + ' is child bean -- performing Map unmarshalling on object type.'
            it.targetBean[it.fieldName] = Marshaller.unmarshall(it.fieldValue, it.annotation.type())
        } else {
            AnnotationDefaultClosureLogger.debug 'Performing default unmarshalling.'
            it.targetBean[it.fieldName] = it.fieldValue 
        }
        AnnotationDefaultClosureLogger.debug 'Unmarshalled value: ' + it.targetBean[it.fieldName]
    }
    Class applyToQuery() default { key, value ->
        AnnotationDefaultClosureLogger.debug "Applying $key : $value"
        if (value instanceof MultiSelect) {
            AnnotationDefaultClosureLogger.debug 'Applying MultiSelect'
            inQuery key, value.values as Object[]        
        } else if (value instanceof DateRange) {
            AnnotationDefaultClosureLogger.debug 'Applying DateRange'
            rangeQuery(key)
                .from(value.from)
                .to(value.to)
        } else {
            AnnotationDefaultClosureLogger.debug 'Performing default apply.'            
            text key, value
        }
    }
    Class type() default java.lang.String 
    boolean highlight() default false
}