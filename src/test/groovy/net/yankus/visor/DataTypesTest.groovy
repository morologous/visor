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
            d:10.25d, 
            subs: [new DataTypeTestBeanSub(name:'foo', num:1), 
                   new DataTypeTestBeanSub(name:'bar', num:2)])

        SearchEngineTestHelper.index(alpha)
        SearchEngineTestHelper.showAll(DataTypesTestBean)
    }

    @AfterClass
    static void tearDown () {
        SearchEngineTestHelper.delete(alpha)
    }

    @Test
    void testRetrieveTypes() {
        def engine = new Engine()
        def results = new DataTypesTestBean(str:'alpha').search()
        assertEquals 1, results.count
        def retrievedAlpha = results.list[0]
        assertNotNull(retrievedAlpha)
        assertEquals alpha.dt, retrievedAlpha.dt
        assertEquals alpha.d, retrievedAlpha.d, 0.000009
        assertEquals alpha.str, retrievedAlpha.str
        assertEquals alpha.subs, retrievedAlpha.subs
    }

    @Test
    void testDateRange() {
       def fromDt = new Date().parse("M/d/yyyy", '12/01/2009')
       def toDt = new Date().parse("M/d/yyyy", "01/31/2010")
       def dtParam = new Expando()
       dtParam.fromDt = fromDt
       dtParam.toDt = toDt

       def results = new DataTypesTestBean(dt:dtParam).search()
        assertEquals 1, results.count
        def retrievedAlpha = results.list[0]
        assertNotNull(retrievedAlpha)
        assertEquals alpha.dt, retrievedAlpha.dt
        assertEquals alpha.d, retrievedAlpha.d, 0.000009
        assertEquals alpha.str, retrievedAlpha.str
        assertEquals alpha.subs, retrievedAlpha.subs
    }

    @Test
    void testByExactDate() {
        def results = new DataTypesTestBean(dt:alpha.dt).search()
        assertEquals 1, results.count
        def retrievedAlpha = results.list[0]
        assertNotNull(retrievedAlpha)
        assertEquals alpha.dt, retrievedAlpha.dt
        assertEquals alpha.d, retrievedAlpha.d, 0.000009
        assertEquals alpha.str, retrievedAlpha.str
        assertEquals alpha.subs, retrievedAlpha.subs
    }

    @Test
    void testBefore() {
        //def fromDt = new Date().parse("M/d/yyyy", '12/01/2009')
        def toDt = new Date().parse("M/d/yyyy", "01/31/2010")
        def dtParam = new Expando()
        //dtParam.fromDt = fromDt
        dtParam.toDt = toDt

        def results = new DataTypesTestBean(dt:dtParam).search()
        assertEquals 1, results.count
        def retrievedAlpha = results.list[0]
        assertNotNull(retrievedAlpha)
        assertEquals alpha.dt, retrievedAlpha.dt
        assertEquals alpha.d, retrievedAlpha.d, 0.000009
        assertEquals alpha.str, retrievedAlpha.str
        assertEquals alpha.subs, retrievedAlpha.subs
    }

    @Test
    void testAfter() {
        def fromDt = new Date().parse("M/d/yyyy", '12/01/2009')
        //def toDt = new Date().parse("M/d/yyyy", "01/31/2010")
        def dtParam = new Expando()
        dtParam.fromDt = fromDt
        //dtParam.toDt = toDt

        def results = new DataTypesTestBean(dt:dtParam).search()
        assertEquals 1, results.count
        def retrievedAlpha = results.list[0]
        assertNotNull(retrievedAlpha)
        assertEquals alpha.dt, retrievedAlpha.dt
        assertEquals alpha.d, retrievedAlpha.d, 0.000009
        assertEquals alpha.str, retrievedAlpha.str
        assertEquals alpha.subs, retrievedAlpha.subs
    }

    @Visor(index='test', settings = { SearchEngineTestHelper.testESSettings.rehydrate(getDelegate(), getOwner(), getThisObject()).call() } )
    @ToString
    @EqualsAndHashCode
    static class DataTypesTestBean {
        def id
        @Field(marshall = { FieldUtils.marshallDate(it) },
            unmarshall = { FieldUtils.unmarshallDate(it) },
            applyToQuery = { key, value -> FieldUtils.applyToQueryDate.rehydrate(delegate, owner, thisObject).call(key, value) })
        def dt
        @Field
        def str
        @Field
        def d
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