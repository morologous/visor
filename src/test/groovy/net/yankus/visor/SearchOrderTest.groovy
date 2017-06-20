package net.yankus.visor


import org.junit.AfterClass 
import static org.junit.Assert.*

import org.junit.BeforeClass
import org.junit.Test

import groovy.lang.Closure
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.yankus.visor.Visor


class SearchOrderTest {

    static def testBeans = []
    
    @BeforeClass
    public static void setUp() throws Exception {
        for (i in 0..9) {
            def bean = new SearchOrderTestBean(id:"$i", index:9-i, name:"bean $i", content:'foo ' * i)
            def response = SearchEngineTestHelper.index bean

            testBeans << bean
        }
        SearchEngineTestHelper.refresh(testBeans[0])
    }

    @AfterClass
    public static void tearDown() throws Exception {
        testBeans.each {
            SearchEngineTestHelper.delete it
        }
    }


    @Test
    void testOrderByIndex() {
        def results = new SearchOrderTestBean(queryString:"bean", sortOrder:'index').search()

        assertEquals 10, results.count 
        assertEquals 'bean 9', results.list[0].name
    }

    @Test
    void testOrderByIndexRev() {
        def results = new SearchOrderTestBean(queryString:"bean", sortOrder:['index':'desc']).search()

        assertEquals 10, results.count 
        assertEquals 'bean 0', results.list[0].name
    }

    @Test
    void testOrderByScore() {
        def results = new SearchOrderTestBean(queryString:'foo').search()

        def score = 99.0d
        results.list.each {
            println "${it} - ${it.score}"
            assertTrue "${it.score} was not less that ${score}", it.score <= score
            score = it.score
        }
    }

    @Visor ( index = 'test',
           settings = { SearchEngineTestHelper.testESSettings.rehydrate(getDelegate(), getOwner(), getThisObject()).call() } )
    @ToString
    @EqualsAndHashCode(excludes="score")
    static class SearchOrderTestBean {
        @Id
        def id
        @Field
        def index
        @Field
        def name
        @Field
        def content
    }
}