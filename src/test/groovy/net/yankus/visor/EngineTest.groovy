package net.yankus.visor

import org.junit.Test
import org.junit.Before
import org.junit.After
import static org.junit.Assert.*
import org.elasticsearch.groovy.node.GNode
import org.elasticsearch.groovy.node.GNodeBuilder
import static org.elasticsearch.groovy.node.GNodeBuilder.*

class EngineTest {
    
    GNode node 
    GNodeBuilder nodeBuilder

    @Before
    public void setUp() throws Exception {
        nodeBuilder = nodeBuilder();
        nodeBuilder.settings {
            node {
                client = true
            }
            cluster {
                name = "test"
            }
        }

        node = nodeBuilder.node()

        def client = node.client

//        client.in

    }

    @After
    public void tearDown() throws Exception {
        node.stop().close()
    }

    @Test
    public void testInject() {
        def engine = new Engine(client:node.client)
        assertNotNull(engine.client)
    }
}