package net.yankus.visor

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target
import java.lang.annotation.ElementType

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Field { 
    Class marshall() default { it.targetBean[it.fieldName] }
    Class unmarshall() default { it.targetBean[it.fieldName] = it.fieldValue }
    Class applyToQuery() default { key, value ->
        if (value instanceof MultiSelect) {
            //log.debug "Setting multiselect: $value.values"
            must: terms((key): value.values)
        } else {
            must: field ((key):value)
        }
    }
    String queryPhase() default 'QUERY'
    Class type() default java.lang.String 
}