package net.yankus.visor

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target
import java.lang.annotation.ElementType

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
            must: terms((key): value.values)
        } else if (value instanceof DateRange) {
            AnnotationDefaultClosureLogger.debug 'Applying DateRange'
            range {
                "$key" {
                    from: value.from
                    to: value.to                          
                }
            }
        } else {
            AnnotationDefaultClosureLogger.debug 'Performing default apply.'
            must: field ((key):value)
        }
    }
    Class type() default java.lang.String 
}