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

    	def response = Engine.health(zeta)
	    
    	assertEquals ClusterHealthStatus.GREEN, response.status
    }

}
