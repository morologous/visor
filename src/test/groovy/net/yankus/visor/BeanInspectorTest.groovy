package net.yankus.visor

import org.junit.Test
import static org.junit.Assert.*

class BeanToQueryComposerTest  {
	
	@Test
	public void testCompose() {
		def testBean = new TestBean([t1:'foo', t2:'bar', t3:'baz'])

		def result = BeanInspector.inspect(testBean)
		println(result)
		assertNotNull (result['t1'])
		assertNotNull (result['t2'])
		assertNull (result['t3'])
	}

	public class TestBean {
		@QueryParam
		def t1
		@QueryParam
		def t2
		def t3
	}
}