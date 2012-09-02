package net.yankus.visor

import org.junit.Test
import org.junit.Before
import org.junit.After
import static org.junit.Assert.*
import org.elasticsearch.search.SearchHit
import org.elasticsearch.search.SearchHitField
import groovy.mock.interceptor.MockFor
import groovy.util.Expando

class UnmarshallerTest {
	
	def source = [foo:'foo', bar:'bar', gazonk:'gazonk']
	def searchHit
	def searchHitField
	def context

	@Before
	public void setUp() {
		searchHitField = [getValues:{[source]}] as SearchHitField
		searchHit = [field:{searchHitField}, getScore: { 0.75f }, highlightFields:{ [ text:[fragments:'foo']] }] as SearchHit
		context = new Expando()
		context.returnType = TestBean.class
	}

	@Test
	public void testInflate() { 
		def result = Marshaller.unmarshall(searchHit, context)

		assertNotNull(result)
		assertTrue(result instanceof TestBean)
		assertEquals('foo', result.foo)
		assertEquals('bar', result.bar)
		assertNull(result.baz)
		assertEquals 0.75d, result.score, 0.000001d
		
	}

	@net.yankus.visor.Visor(index='test')
	public class TestBean {
		@Field
		def foo
		@Field
		def bar
		@Field
		def baz
	}
}