package net.yankus.visor

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

import org.codehaus.groovy.transform.GroovyASTTransformationClass
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilders


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@GroovyASTTransformationClass(["net.yankus.visor.VisorASTTransformation"])
public @interface Visor { 
    public Class filters() default { BoolQueryBuilder qb -> qb.filter(QueryBuilders.matchAllQuery()) } 
    public Class settings() default { 
        node { local = true } 
        http { enabled = false }
    };
    public String index()
    public Class remoteAddresses() default { [] }    
    public String defaultTimeout() default "300s"
    public Class connectionFactory() default {
    	new ThreadLocalClientFactory(context:it)
    }
}