package net.yankus.visor

import static org.junit.Assert.*

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse
import org.elasticsearch.cluster.health.ClusterHealthStatus
import org.junit.BeforeClass
import org.junit.Test

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
        Future<ClusterHealthResponse> future = executor.submit( {
                def innerResponse = Engine.health(zeta)
                while (ClusterHealthStatus.GREEN != innerResponse.status) {                    
                    Thread.sleep(250)
                    innerResponse = Engine.health(zeta)
                }
                return innerResponse
             } as Callable<ClusterHealthResponse>)
		response = future.get(30, TimeUnit.SECONDS)
    	assertEquals ClusterHealthStatus.GREEN, response.status
    }

}