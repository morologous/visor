package net.yankus.visor

import org.junit.BeforeClass
import org.junit.AfterClass
import org.junit.Test
import static org.junit.Assert.*
import groovy.transform.ToString
import groovy.transform.EqualsAndHashCode

class DataTypesTest {
    
    static def alpha

    @BeforeClass
    static void setUp() {
        println FieldUtils.unmarshallDate
        alpha = new DataTypesTestBean(
            id:'alpha',
            dt:new Date().parse("M/d/yyyy", '01/01/2010'), 
            str:'alpha', 
            d:10.25, 
            subs: [new DataTypeTestBeanSub(name:'foo', num:1), 
                   new DataTypeTestBeanSub(name:'bar', num:2)])

        SearchEngineTestHelper.index(alpha)
    }

    @AfterClass
    static void tearDown () {
        if (alpha) {

        }
        SearchEngineTestHelper.delete(alpha)
    }

    @Test
    void testRetrieveTypes() {
        def engine = new Engine()
        def results = engine.doQuery(new DataTypesTestBean(str:'alpha'))
        assertEquals 1, results.count
        def retrievedAlpha = results.list[0]
        assertNotNull(retrievedAlpha)
        assertEquals alpha.dt, retrievedAlpha.dt
        assertEquals alpha.d, retrievedAlpha.d, 0.000009
        assertEquals alpha.str, retrievedAlpha.str
        assertEquals alpha.subs, retrievedAlpha.subs
    }

    @Visor ( index = 'test' )
    @ToString
    @EqualsAndHashCode
    static class DataTypesTestBean {
        def id
        @Field(marshall = { FieldUtils.marshallDate(it) },
            unmarshall = { FieldUtils.unmarshallDate(it) } )
        Date dt
        @Field
        String str
        @Field
        double d
        @Field(type = DataTypeTestBeanSub,
               marshall = { FieldUtils.marshallCollection(it) },
               unmarshall = { FieldUtils.unmarshallCollection(it) })
        def subs = []
    }

    @ToString
    @EqualsAndHashCode
    static class DataTypeTestBeanSub {
        @Field
        def name
        @Field
        def num
    }

}