package net.yankus.visor

import groovy.transform.ToString
import groovy.transform.EqualsAndHashCode
import static org.elasticsearch.index.query.FilterBuilders.*

@Visor(filters = { inFilter('security', ['low', 'none'] as String[]) }, 
       index = "test",
       settings = { SearchEngineTestHelper.testESSettings.rehydrate(getDelegate(), getOwner(), getThisObject()).call() } )
@ToString
@EqualsAndHashCode(excludes="score")
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