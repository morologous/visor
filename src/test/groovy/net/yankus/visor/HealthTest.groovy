package net.yankus.visor

import org.junit.Test
import org.junit.Before
import org.junit.After
import org.junit.BeforeClass
import org.junit.AfterClass
import static org.junit.Assert.*
import org.elasticsearch.action.admin.cluster.health.ClusterHealthStatus

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