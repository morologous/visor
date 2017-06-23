package net.yankus.visor

import static org.junit.Assert.*

import org.elasticsearch.search.SearchHit
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

class EngineSearchTest {
    
//    static def datasource
    static def foo
    static def testBeans = []    
    @BeforeClass
    public static void setUp() throws Exception {
        
        foo = new TestBean(id:'1', num:1, value:'foo', security:'none')
        def bar = new TestBean(id:'2', num:2, value:'bar', security:'low')
        def baz = new TestBean(id:'3', num:2, value:'baz', security:'medium')
        
        def gazonk1 = new TestBean(id:'4', num:3, value:'gazonk', security:'none')
        def gazonk2 = new TestBean(id:'5', num:4, value:'gazonk', security:'none')
        
        SearchEngineTestHelper.index foo
        testBeans << foo
        SearchEngineTestHelper.index bar
        testBeans << bar
        SearchEngineTestHelper.index baz
        testBeans << baz
        SearchEngineTestHelper.index gazonk1
        testBeans << gazonk1
        SearchEngineTestHelper.index gazonk2
        testBeans << gazonk2



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
    public void testSearchById() {
        def results = new TestBean(id:'1').search()
        assertEquals 1, results.count
        assertEquals foo, results.list[0]       
    }

    @Test
    public void testQuery() {
        def query = new TestBean(value:'foo')
        def results = query.search()
        assertEquals 1, results.count
        // results.list.each { println "result $it" }
        results.response.hits.each { SearchHit hit ->
            assertEquals "1", hit.id
        }
        results.list.each {
           assertEquals 'foo', it.value
           assertEquals 1, it.num
        }

        assertSame query, results.query
    }



    @Test
    public void testMultiFieldQuery() {
        def results = 
            new TestBean(value:'gazonk', num:3).search()
        //results.list.each { println "result $it" }
        assertEquals 1, results.count
        results.response.hits.each { SearchHit hit ->
            assertEquals "4", hit.id
        }
        results.list.each {
           assertEquals 'gazonk', it.value
           assertEquals 3, it.num
        }
    }

    @Test
    public void testFilters() {
        def results = new TestBean(num:2).search()
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