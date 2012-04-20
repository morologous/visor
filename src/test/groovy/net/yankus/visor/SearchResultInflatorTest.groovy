package net.yankus.visor

import org.junit.Test
import org.junit.Before
import org.junit.After
import static org.junit.Assert.*
import org.elasticsearch.search.SearchHit
import groovy.mock.interceptor.MockFor
import groovy.util.Expando

class SearchResultInflatorTest {
	
	def source = [foo:'foo', bar:'bar', gazonk:'gazonk']
	def mock
	def searchHit
	def context

	@Before
	public void setUp() {
		mock = new MockFor(SearchHit)
		mock.demand.getSource() { source }
		searchHit = mock.proxyInstance()
		context = new Expando()
		context.returnType = TestBean.class
	}

	@After
	public void tearDown() {
		mock.verify(searchHit)
	}

	@Test
	public void testInflate() { 
		def result = new SearchResultInflator(context:context).inflate(searchHit)

		assertNotNull(result)
		assertTrue(result instanceof TestBean)
		assertEquals('foo', result.foo)
		assertEquals('bar', result.bar)
		assertNull(result.baz)
		
	}

	public class TestBean {
		def foo
		def bar
		def baz
	}
}