package net.yankus.visor

import org.junit.Test
import org.junit.Before
import org.junit.After
import org.junit.BeforeClass
import org.junit.AfterClass
import static org.junit.Assert.*
import org.elasticsearch.cluster.health.ClusterHealthStatus
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class HealthTest {

	static def zeta

    @BeforeClass
    static void setUp() {

    	zeta = new TestBean(id:'zeta', value:'zeta', num:400, security:'none')
    	zeta.index()

    }

    static void tearDown() {
    	zeta.delete()
    }

    @Test
    void testHealthRequest() {

    	def response

        ExecutorService executor = Executors.newFixedThreadPool(1)
        executor.execute( {
                response = Engine.health(zeta)
                while (ClusterHealthStatus.GREEN != response.status) {                    
                    Thread.sleep(250)
                    response = Engine.health(zeta)
                }
                return
             } as Runnable)
        executor.awaitTermination(20, TimeUnit.SECONDS)        

    	assertEquals ClusterHealthStatus.GREEN, response.status
    }

}