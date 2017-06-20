package net.yankus.visor

import org.elasticsearch.index.query.QueryBuilders;

import groovy.lang.Closure
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.yankus.visor.Visor

@Visor(filters = { it.filter(QueryBuilders.termsQuery('security', ['low', 'none'] as String[])) }, 
       index = "test",
       settings = {settings ->
        settings.put('node.local',true)
        settings.put('discovery.cluster.name','visorTest')
        settings.put('http.enabled', false)
        settings.put('path.data','./build/data')
        settings.put('path.home','./build')}  )
@ToString
@EqualsAndHashCode(excludes="score, snippets")
public class TestBean {
    @Id
    def id

    @Field
    def value

    @Field
    def num

    @Field
    def security
    
    //will throw exception during compile because of collision with AST-added property
    //def score

    //will throw exception during compile because of collision with AST-added index method
    /*
    def index() { // foo 
    }
    */
}