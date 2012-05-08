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

class EngineTest {
    
    static GNode node 
    static GNodeBuilder nodeBuilder

    @BeforeClass
    public static void setUp() throws Exception {
        nodeBuilder = nodeBuilder();
        nodeBuilder.settings {
            node {
                local = true
            }
        }

        node = nodeBuilder.node()

        def client = node.client

        def indexR = client.index {
            index "test"
            type TestBean.class.simpleName
            id "1"
            source {
                value = "foo"
                num = 1
                security = 'none'
            }
        }
        println "Indexed $indexR.response.index/$indexR.response.type/$indexR.response.id"

        indexR = client.index {
            index "test"
            type TestBean.class.simpleName
            id "2"
            source {
                value = "bar"
                num = 2
                security = 'low'
            }
        }

        println "Indexed $indexR.response.index/$indexR.response.type/$indexR.response.id"


        indexR = client.index {
            index "test"
            type TestBean.class.simpleName
            id "3"
            source {
                value = "baz"
                num = 2
                security = 'medium'
            }
        }

        println "Indexed $indexR.response.index/$indexR.response.type/$indexR.response.id"

        indexR = client.index {
            index "test"
            type TestBean.class.simpleName
            id "4"
            source {
                value = "gazonk"
                num = 9
                security = 'low'
            }
        }

        println "Indexed $indexR.response.index/$indexR.response.type/$indexR.response.id"



        def confirmResults = client.search {
            indices "test"
            types TestBean.class.simpleName
            source {
                query {
                    term(value:'bar')
                }
            }
        }

         confirmResults.response.hits.each { SearchHit hit ->
            assertEquals "2", hit.id
        }
       
    }

    @AfterClass
    public static void tearDown() throws Exception {
        def toDelete = node.client.search {
            indices "test"
            types TestBean.class.simpleName
            source {
                query {
                    match_all { }
                }
            }
        }

        toDelete.response.hits.each {
            def toDeleteId = it.id
            node.client.delete { 
                index 'test'
                type TestBean.class.simpleName
                id toDeleteId
            }
        }
        node.stop().close()
    }

    @Test
    public void testQuery() {
        def engine = new Engine()
        def results = engine.doQuery(new TestBean(value:'foo'))
        assertEquals 1, results.count
        results.list.each { println "result $it" }
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
        def results = engine.doQuery(new TestBean(value:'bar', num:2))
        results.list.each { println "result $it" }
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
        def results = engine.doQuery(new TestBean(value:'b*'))
        assertEquals 1, results.count 
        results.list.each { println "result $it" }
        results.response.hits.each { SearchHit hit ->
            assertEquals "2", hit.id
        }
        results.list.each {
           assertEquals 'bar', it.value
           assertEquals 2, it.num
        }
    }

    @Test
    public void testIndex() {
        def engine = new Engine()
        def data = new TestBean(id:99, value:'flurgle', num:9, security:'none')

        def indexResult = engine.doIndex(data)
        def response  = indexResult.response '5s'
        println "Indexed $response.index/$response.type/$response.id"

        def results = engine.doQuery(new TestBean(value:"flurgle"))
        assertEquals 1, results.count 
        results.list.each { println "result $it" }
        results.response.hits.each { SearchHit hit ->
            assertEquals "99", hit.id
        }
        results.list.each {
           assertEquals 'flurgle', it.value
           assertEquals 9, it.num
        }

    }

    @Visor(filters = { terms(security:['low', 'none']) }, returnType = TestBean.class, index = "test", settings = { node { local = true } } )
    @ToString
    public class TestBean {
        @QueryParam
        @Id
        def id

        @QueryParam
        def value

        @QueryParam
        def num

        @QueryParam
        def security
    }
}