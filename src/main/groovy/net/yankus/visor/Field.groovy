package net.yankus.visor

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Field { 
    Class marshall() default { it.targetBean[it.fieldName] }
    Class unmarshall() default { it.targetBean[it.fieldName] = it.fieldValue }
    Class type() default java.lang.String 
}