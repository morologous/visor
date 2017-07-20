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
    public Class filters() default { context -> context.root.filter(QueryBuilders.matchAllQuery()) } 
    public Class settings() default { settings ->
		settings.put('node.local',true)
		settings.put('http.enabled', false)
		settings.put('path.data','./')
		settings.put('path.home','./')
    };
    public String index()
    public Class remoteAddresses() default { [] }    
    public String defaultTimeout() default "300s"
    public Class connectionFactory() default {
    	new ThreadLocalClientFactory(context:it)
    }
}