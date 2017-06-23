package net.yankus.visor


import static org.junit.Assert.*
import net.yankus.visor.Visor

import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString


class EngineDeleteTest {
    

    static def val1
    static def val2

    @BeforeClass
    static void setUp() {
        val1 = new EngineDeleteTestBean(id:'' + 'val1'.hashCode(), val:'val1')
        val2 = new EngineDeleteTestBean(id:'' + 'val2'.hashCode(), val:'val2')

        SearchEngineTestHelper.index val1
        SearchEngineTestHelper.index val2
    }

    @AfterClass
    static void tearDown() {
        SearchEngineTestHelper.delete val1
        SearchEngineTestHelper.delete val2
    }

    @Test
    void testDelete() {
        def searchR = val1.search()
        assertEquals 1, searchR.count
        assertEquals val1, searchR.list[0]

        def response = val1.delete()        

        SearchEngineTestHelper.refresh(val1)

        searchR = val1.search()
        assertEquals 0, searchR.count
    }

    @Test
    void testReDelete() {
        def searchR = val2.search()
        assertEquals 1, searchR.count
        assertEquals val2, searchR.list[0]

        def response = val2.delete()        

        SearchEngineTestHelper.refresh(val2)

        searchR = val2.search()
        assertEquals 0, searchR.count

        response = val2.delete()       

        SearchEngineTestHelper.refresh(val2)

        searchR = val2.search() 
        assertEquals 0, searchR.count
    }

    @Visor(index='test', settings={ SearchEngineTestHelper.testESSettings(it) }  )
    @ToString
    @EqualsAndHashCode(excludes="score, snippets")
    static class EngineDeleteTestBean {
        @Id
        def id

        @Field
        def val
    }

}