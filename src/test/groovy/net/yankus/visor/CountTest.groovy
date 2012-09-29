package net.yankus.visor

import org.junit.Before
import org.junit.After
import org.junit.Test
import static org.junit.Assert.*
import groovy.transform.ToString
import groovy.transform.EqualsAndHashCode

class CountTest {
	
	def o1
	def o2
	def o3

	@Before
	public void before() {
		o1 = new TestBean(id:'o1', value:'o1', num:123, security:'none')
		o2 = new TestBean(id:'o2', value:'o2', num:123, security:'none')
		o3 = new TestBean(id:'o3', value:'o3', num:123, security:'high')

		SearchEngineTestHelper.index o1
		SearchEngineTestHelper.index o2
		SearchEngineTestHelper.index o3

		SearchEngineTestHelper.snooze()
	}

	@After
	public void after() {
		SearchEngineTestHelper.delete o1
		SearchEngineTestHelper.delete o2
		SearchEngineTestHelper.delete o3
	}

	@Test
	void testCount() {
		def	results = new TestBean(num:123).count()

		assertNotNull results
		assertEquals 2, results.count
	}

}