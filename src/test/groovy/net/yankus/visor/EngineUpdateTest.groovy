package net.yankus.visor

import static org.junit.Assert.*
import net.yankus.visor.Visor

import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

class EngineUpdateTest {
    

    static def foo

    @BeforeClass
    static void setUp() {
        foo = new EngineUpdateTestBean (id:'' + 'foo'.hashCode(), name:'foo', num:100)
        SearchEngineTestHelper.index foo
    }

    @AfterClass
    static void tearDown() {
        SearchEngineTestHelper.delete foo
    }

    @Test
    void testUpdate() {
        def results = new EngineUpdateTestBean(num:100).search()
        assertEquals 1, results.count
        assertEquals foo, results.list[0]

        def newFoo = results.list[0]
        newFoo.name = 'bar'
        newFoo.update()

        SearchEngineTestHelper.refresh(newFoo)

        results = new EngineUpdateTestBean(num:100).search()
        assertEquals 1, results.count
        assertEquals newFoo, results.list[0]


    }

    @Visor(index='test', settings={ SearchEngineTestHelper.testESSettings(it) }  )
    @ToString
    @EqualsAndHashCode(excludes="score, snippets")
    static class EngineUpdateTestBean {
        @Id
        def id
        @Field
        def name
        @Field
        def num
    }

}