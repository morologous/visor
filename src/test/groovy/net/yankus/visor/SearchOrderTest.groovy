package net.yankus.visor


import org.junit.BeforeClass
import org.junit.AfterClass
import org.junit.Test
import static org.junit.Assert.*
import groovy.transform.ToString
import groovy.transform.EqualsAndHashCode


class SearchOrderTest {

    static def testBeans = []
    
    @BeforeClass
    public static void setUp() throws Exception {
        for (i in 0..9) {
            def bean = new SearchOrderTestBean(id:"$i", index:9-i, name:"bean $i")
            bean.index()
            testBeans << bean
        }
        SearchEngineTestHelper.snooze()
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
    }
}