package net.yankus.visor


import org.junit.BeforeClass
import org.junit.AfterClass
import org.junit.Test
import static org.junit.Assert.*
import groovy.transform.ToString
import groovy.transform.EqualsAndHashCode

class EngineIndexTest {
    
    static def gazonk
    static def flurgle

    @BeforeClass
    static void setUp() {
        gazonk = new EngineIndexTestBean(id:'' + 'gazonk'.hashCode(), name:'gazonk', num:100)
        flurgle = new EngineIndexTestBean(id:'' + 'flurgle'.hashCode(), name:'flurgle', num:200)

        SearchEngineTestHelper.index gazonk

    }

    @AfterClass
    static void tearDown() {
        SearchEngineTestHelper.delete gazonk
        SearchEngineTestHelper.delete flurgle
    }

    @Test
    void testIndex() {
        def indexR = flurgle.index()

        indexR.response '5s'
        SearchEngineTestHelper.snooze()

        def result = new EngineIndexTestBean(num:200).search()
        assertEquals 1, result.count
        assertEquals flurgle, result.list[0]
    }

    @Visor(index='test', settings = { SearchEngineTestHelper.testESSettings.rehydrate(getDelegate(), getOwner(), getThisObject()).call() } )
    @ToString
    @EqualsAndHashCode
    static class EngineIndexTestBean {
        @Id
        def id
        @Field
        def name
        @Field
        def num

    }

}