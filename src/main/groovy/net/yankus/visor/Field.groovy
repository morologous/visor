package net.yankus.visor

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target
import java.lang.annotation.ElementType

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Field { 
    Class marshall() default { 
        //println it
        def value = it.targetBean[it.fieldName]
        if (it.annotation?.type() == Date) {
            if (value instanceof DateRange) {
                new DateRange(from:value?.from?.time, to:value?.to?.time)
            } else {
                value?.time
            }
        } else if (value instanceof Collection) {
            def coll = []
            value.each { val -> 
                coll << Marshaller.marshall(val, it.mode)            
            }
            coll
        } else {
            value
        }
    }
    Class unmarshall() default { 
        if (it.annotation?.type() == Date) {
            it.targetBean[it.fieldName] = new Date(it.fieldValue)
        } else if (it.fieldValue instanceof Collection) {
            def coll = []
            it.fieldValue.each { val ->
                coll << Marshaller.unmarshall(val, it.annotation.type())
            }
            it.targetBean[it.fieldName] = coll
        } else {
            it.targetBean[it.fieldName] = it.fieldValue 
        }
    }
    Class applyToQuery() default { key, value ->
        if (value instanceof MultiSelect) {
            //log.debug "Setting multiselect: $value.values"
            must: terms((key): value.values)
        } else if (value instanceof DateRange) {
            range {
                "$key" {
                    from: value.from
                    to: value.to                          
                }
            }
        } else {
            must: field ((key):value)
        }
    }
    String queryPhase() default 'QUERY'
    Class type() default java.lang.String 
}