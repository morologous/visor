package net.yankus.visor

import static org.junit.Assert.*
import net.yankus.visor.Visor

import org.junit.Test

class ContextBuilderTest {
	
	def expected = [
		settings:{foo='bar'}, 
		index:'foo',
		filters:{it == "foo"}, 
		returnType:ContextBuilderTest.class
	]

	@Test
	public void testBuildContextFromAnnotation() {
		def result = ContextBuilder.build(new AnnotationTestBean(test:'foo'))
		assertNotNull result
		assertNotNull result.settings 
		assertNotNull result.returnType
		assertNotNull result.index
		assertNotNull result.filters
		assertNotNull result.visorOpts
		assertEquals '300s', result.defaultTimeout
		assertTrue result.filters.newInstance(null, null).call('foo')
		
		def harness = new Expando()
		harness.foo = 'baz'
		result.settings.newInstance(null, null).call(harness)
		assertEquals 'bar', harness.foo

		assertEquals 'foo', result.index

	}

	@Visor(filters={it=='foo'}, settings={it.foo='bar'}, index='foo')
	public class AnnotationTestBean {
		@Field
		def test
	}
}

