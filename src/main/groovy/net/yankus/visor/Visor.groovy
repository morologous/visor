package net.yankus.visor

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target
import java.lang.annotation.ElementType
import org.codehaus.groovy.transform.GroovyASTTransformationClass

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@GroovyASTTransformationClass(["net.yankus.visor.VisorASTTransformation"])
public @interface Visor { 
    public Class filters() default { match_all { } }
    public Class settings() default { 
        node { local = true } 
        http { enabled = false }
    };
    public String index()
    public Class remoteAddresses() default { [] }


}