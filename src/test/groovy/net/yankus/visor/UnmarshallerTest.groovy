package net.yankus.visor

import static org.junit.Assert.*

import org.elasticsearch.common.text.Text
import org.elasticsearch.search.SearchHit
import org.elasticsearch.search.SearchHitField
import org.elasticsearch.search.highlight.HighlightField
import org.junit.Before
import org.junit.Test

class UnmarshallerTest {
	
	def source = [foo:'foo', bar:'bar', gazonk:'gazonk']
	def searchHit
	def searchHitField
	def context

	@Before
	public void setUp() {
		searchHitField = [getValues:{[source]}] as SearchHitField
		def highlightField =  new HighlightField('text', [new Text('foo')] as Text[]) 
		searchHit = [getSource:{source}, field:{searchHitField}, getScore: { 0.75f }, highlightFields:{ [text:highlightField] }] as SearchHit
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