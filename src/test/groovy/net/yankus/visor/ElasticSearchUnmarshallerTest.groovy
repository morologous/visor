package net.yankus.visor

import org.junit.Test
import org.junit.Before
import org.junit.After
import static org.junit.Assert.*
import org.elasticsearch.search.SearchHit
import groovy.mock.interceptor.MockFor
import groovy.util.Expando

class ElasticSearchUnmarshallerTest {
	
	def source = [foo:'foo', bar:'bar', gazonk:'gazonk']
	def mock
	def searchHit
	def context

	@Before
	public void setUp() {
		mock = new MockFor(SearchHit)
		mock.demand.getSource() { source }
		mock.demand.getScore() { 0.75d }
		mock.demand.highlightFields() { [ text:[fragments:'foo']] }
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
		def result = ElasticSearchMarshaller.unmarshall(searchHit, context)

		assertNotNull(result)
		assertTrue(result instanceof TestBean)
		assertEquals('foo', result.foo)
		assertEquals('bar', result.bar)
		assertNull(result.baz)
		assertEquals 0.75d, result.score, 0.000001d
		
	}

	@Visor(index='test')
	public class TestBean {
		@Field
		def foo
		@Field
		def bar
		@Field
		def baz
	}
}