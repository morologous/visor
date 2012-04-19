package net.yankus.visor

import org.junit.Test
import org.junit.Before
import org.junit.After
import static org.junit.Assert.*
import org.elasticsearch.groovy.node.GNode
import org.elasticsearch.groovy.node.GNodeBuilder
import static org.elasticsearch.groovy.node.GNodeBuilder.*
import org.elasticsearch.search.SearchHit
import groovy.transform.ToString

class EngineTest {
    
    GNode node 
    GNodeBuilder nodeBuilder

    @Before
    public void setUp() throws Exception {
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
            type "testData"
            id "1"
            source {
                value = "foo"

            }
        }
        println "Indexed $indexR.response.index/$indexR.response.type/$indexR.response.id"

        indexR = client.index {
            index "test"
            type "testData"
            id "2"
            source {
                value = "bar"
            }
        }
        println "Indexed $indexR.response.index/$indexR.response.type/$indexR.response.id"

        indexR = client.index {
            index "test"
            type "testData"
            id "3"
            source {
                value = "baz"
            }
        }
        println "Indexed $indexR.response.index/$indexR.response.type/$indexR.response.id"

        def confirmResults = client.search {
            indices "test"
            types "testData"
            source {
                query {
                    term(value:'bar')
                }
            }
        }

         confirmResults.response.hits.each { SearchHit hit ->
            println ("Got hit $hit.id from $hit.index/$hit.type")
            assertEquals "2", hit.id
        }

    }

    @After
    public void tearDown() throws Exception {
        node.stop().close()
    }

    @Test
    public void testQuery() {
        def engine = new Engine()
        def results = engine.doQuery(new TestBean(value:'foo'))
        results.response.hits.each { SearchHit hit ->
            assertEquals "1", hit.id
        }
        results.list.each {
           assertEquals 'foo', it.value
        }

    }

    @QueryBean(index = "test", settings = { node { local = true } }, filters = { }, returnType = TestBean.class)
    @ToString
    public class TestBean {
        @QueryParam
        def value
    }
}