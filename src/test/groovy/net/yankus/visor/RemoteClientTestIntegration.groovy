package net.yankus.visor

import org.junit.BeforeClass
import org.junit.AfterClass
import org.junit.Test
import static org.junit.Assert.*
import groovy.transform.ToString
import groovy.transform.EqualsAndHashCode

class RemoteClientTestIntegration {
    
    static def alpha 
    static def beta

    @BeforeClass
    public static void setUp() {
        alpha = new RemoteClientTestBean(id:'' + 'alpha'.hashCode(), name:'alpha', num:1000)
        beta = new RemoteClientTestBean(id:'' + 'beta'.hashCode(), name:'beta', num:2000)

        SearchEngineTestHelper.index alpha
    }

    @AfterClass
    public static void tearDown() {

    }

    @Test
    void testRemoteSearch() {
        def results = Engine.search new RemoteClientTestBean(num:1000)
        assertEquals 1, results.count
        assertEquals alpha, results.list[0]
    }

    @Test
    void testRemoteIndex() {
        def indexR = Engine.index beta

        indexR.response '5s'
        SearchEngineTestHelper.snooze()

        def results = Engine.search new RemoteClientTestBean(num:2000)
        assertEquals 1, results.count
        assertEquals beta, results.list[0]
    }

    @Visor ( index = 'test', 
        remoteAddresses = [ 'localhost:9300' ],
        settings = { put('node.client', true)
                     /* put('client.transport.sniff', true) */ }
              )
    @ToString
    @EqualsAndHashCode
    static class RemoteClientTestBean {
        @Id
        def id
        @Field
        def name
        @Field
        def num
    }
}