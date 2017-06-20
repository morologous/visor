package net.yankus.visor


import org.junit.AfterClass

import static org.junit.Assert.*

import org.junit.BeforeClass
import org.junit.Test

import groovy.lang.Closure
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.yankus.visor.Visor


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

        def deleteR = val1.delete()
        deleteR.response '5s'

        SearchEngineTestHelper.refresh(val1)

        searchR = val1.search()
        assertEquals 0, searchR.count
    }

    @Test
    void testReDelete() {
        def searchR = val2.search()
        assertEquals 1, searchR.count
        assertEquals val2, searchR.list[0]

        def deleteR = val2.delete()
        deleteR.response '5s'

        SearchEngineTestHelper.refresh(val2)

        searchR = val2.search()
        assertEquals 0, searchR.count

        deleteR = val2.delete()
        deleteR.response '5s'

        SearchEngineTestHelper.refresh(val2)

        searchR = val2.search() 
        assertEquals 0, searchR.count
    }

    @Visor(index='test', settings = { SearchEngineTestHelper.testESSettings.rehydrate(getDelegate(), getOwner(), getThisObject()).call() } )
    @ToString
    @EqualsAndHashCode(excludes="score, snippets")
    static class EngineDeleteTestBean {
        @Id
        def id

        @Field
        def val
    }

}