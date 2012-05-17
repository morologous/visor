package net.yankus.visor

import org.junit.Test
import static org.junit.Assert.*

class ContextBuilderTest {
	
	def expected = [
		settings:{foo='bar'}, 
		index:'foo',
		filters:{it == "foo"}, 
		returnType:ContextBuilderTest.class
	]

	@Test
	public void testBuildContextFromAnnotation() {
		def result = ContextBuilder.build(new AnnotationTestBean())
		assertNotNull result
		assertNotNull result.settings 
		assertNotNull result.returnType
		assertNotNull result.index
		assertNotNull result.filters

		assertTrue result.filters.newInstance(null, null).call('foo')
		
		def harness = new Expando()
		harness.foo = 'baz'
		result.settings.newInstance(null, null).call(harness)
		assertEquals 'bar', harness.foo

		assertEquals 'foo', result.index
	}

/*
	@Test
	public void testBuildContextFromStatic() {
		def result = ContextBuilder.build(new StaticTestBean())
		assertNotNull result
		assertEquals expected, result
	}
*/

	@Visor(filters={it=='foo'}, settings={it.foo='bar'}, index='foo')
	public class AnnotationTestBean {

	}
}

