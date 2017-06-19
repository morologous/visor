package net.yankus.visor

import org.junit.Test
import static org.junit.Assert.*


class MarshallerTest  {
	
	@Test
	public void testCompose() {
		def testBean = new TestBean([t1:'foo', t2:'bar', t3:'baz'])

		def result = Marshaller.marshall(testBean)
		assertNotNull (result['t1'])
		assertNotNull (result['t2'])
		assertNull (result['t3'])
	}

	public class TestBean {
		@Field
		def t1
		@Field
		def t2
		def t3
	}
}