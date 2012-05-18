package net.yankus.visor

import org.junit.Test
import org.junit.Before
import org.junit.After
import org.junit.BeforeClass
import org.junit.AfterClass
import static org.junit.Assert.*
import org.elasticsearch.groovy.node.GNode
import org.elasticsearch.groovy.node.GNodeBuilder
import static org.elasticsearch.groovy.node.GNodeBuilder.*
import org.elasticsearch.search.SearchHit
import groovy.transform.ToString

class EngineSearchTest {
    
//    static def datasource

    static def testBeans = []
    @BeforeClass
    public static void setUp() throws Exception {
        
        def foo = new TestBean(id:'1', num:1, value:'foo', security:'none')
        def bar = new TestBean(id:'2', num:2, value:'bar', security:'low')
        def baz = new TestBean(id:'3', num:2, value:'baz', security:'medium')

        SearchEngineTestHelper.index foo
        testBeans << foo
        SearchEngineTestHelper.index bar
        testBeans << bar
        SearchEngineTestHelper.index baz
        testBeans << baz

        SearchEngineTestHelper.get foo
        SearchEngineTestHelper.get bar
        SearchEngineTestHelper.get baz

        //println "Indexed $indexR.response.index/$indexR.response.type/$indexR.response.id"
        SearchEngineTestHelper.showAll TestBean.class
    }

    @AfterClass
    public static void tearDown() throws Exception {
        testBeans.each {
            SearchEngineTestHelper.delete it
        }
    }

    @Test
    public void testQuery() {
        def engine = new Engine()
        def results = engine.search(new TestBean(value:'foo'))
        assertEquals 1, results.count
        // results.list.each { println "result $it" }
        results.response.hits.each { SearchHit hit ->
            assertEquals "1", hit.id
        }
        results.list.each {
           assertEquals 'foo', it.value
           assertEquals 1, it.num
        }

    }

    @Test
    public void testMultiFieldQuery() {
        def engine = new Engine()
        def results = engine.search(new TestBean(value:'bar', num:2))
        //results.list.each { println "result $it" }
        assertEquals 1, results.count
        results.response.hits.each { SearchHit hit ->
            assertEquals "2", hit.id
        }
        results.list.each {
           assertEquals 'bar', it.value
           assertEquals 2, it.num
        }
    }

    @Test
    public void testFilters() {
        def engine = new Engine()
        def results = engine.search(new TestBean(value:'b*'))
        assertEquals 1, results.count 
        //results.list.each { println "result $it" }
        results.response.hits.each { SearchHit hit ->
            assertEquals "2", hit.id
        }
        results.list.each {
           assertEquals 'bar', it.value
           assertEquals 2, it.num
        }
    }
    
}