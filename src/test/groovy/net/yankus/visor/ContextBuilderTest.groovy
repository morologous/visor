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
		assertNotNull result['settings'] 
		assertNotNull result['returnType']
		assertNotNull result['index']
		assertNotNull result['filters']
	}

/*
	@Test
	public void testBuildContextFromStatic() {
		def result = ContextBuilder.build(new StaticTestBean())
		assertNotNull result
		assertEquals expected, result
	}
*/

	@QueryBean(settings={foo='bar'}, filters={it=='foo'}, index='foo', returnType=ContextBuilderTest.class)
	public class AnnotationTestBean {

	}
}

