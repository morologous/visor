package net.yankus.visor

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target
import java.lang.annotation.ElementType
import org.codehaus.groovy.transform.GroovyASTTransformationClass
import static org.elasticsearch.index.query.FilterBuilders.*

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@GroovyASTTransformationClass(["net.yankus.visor.VisorASTTransformation"])
public @interface Visor { 
    public Class filters() default { matchAllFilter() } 
    public Class settings() default { 
        node { local = true } 
        http { enabled = false }
    };
    public String index()
    public Class remoteAddresses() default { [] }    
    public String defaultTimeout() default "300s"
}