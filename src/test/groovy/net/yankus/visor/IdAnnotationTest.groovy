package net.yankus.visor


import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import static org.junit.Assert.*
import net.yankus.visor.Visor

class IdAnnotationTest {
    
    static def foo
    static def bar
    static def baz

    @BeforeClass
    static void setUp() {
        foo = new SingleIdAnnotationTestBean(id:'aaa', name:'aaa')
        bar = new MultipleIdAnnotationTestBean(id:'bbb', id2:'bbb', name:'bbb')
        baz = new NoIdAnnotationTestBean(id:'ccc', name:'ccc')

        SearchEngineTestHelper.index foo
        SearchEngineTestHelper.index bar
        SearchEngineTestHelper.index baz

        SearchEngineTestHelper.refresh foo
    }

    @AfterClass
    static void tearDown() {
        SearchEngineTestHelper.delete foo 
        SearchEngineTestHelper.delete bar
        SearchEngineTestHelper.delete baz
    }

    @Test
    void testCoerceIdToStringInEngine() {
        def gazonk = new SingleIdAnnotationTestBean(id:123L, name:'gazonk')
        gazonk.index()

        SearchEngineTestHelper.refresh(gazonk)

        def results = new SingleIdAnnotationTestBean(name:'gazonk').search()
        assertEquals 1, results.count
        assertEquals gazonk.id.toString(), results.list[0].id

        gazonk.delete()
    }

    @Test
    void testSearchEach() {
        def results = new SingleIdAnnotationTestBean(name:'aaa').search()

        assertEquals 1, results.count
        assertEquals foo, results.list[0]

        try {
            results = new MultipleIdAnnotationTestBean(name:'bbb').search()
            assertFalse true
        } catch (IllegalStateException ise) {
                // expected
        }

        results = new NoIdAnnotationTestBean(name:'ccc').search()
        assertEquals 1, results.count
        assertEquals new NoIdAnnotationTestBean(name:'ccc'), results.list[0]
    }

    @Visor(index='test', settings={ SearchEngineTestHelper.testESSettings(it) }  )
    @ToString
    @EqualsAndHashCode(excludes="score, snippets")
    static class SingleIdAnnotationTestBean {
        @Id
        def id
        @Field
        def name
    }

    @Visor(index='test', settings={ SearchEngineTestHelper.testESSettings(it) }  )
    @ToString
    @EqualsAndHashCode(excludes="score, snippets")
    static class MultipleIdAnnotationTestBean {
        @Id
        def id

        @Id
        def id2

        @Field
        def name
    }

    @Visor(index='test', settings={ SearchEngineTestHelper.testESSettings(it) }  )
    @ToString
    @EqualsAndHashCode(excludes="score, snippets")
    static class NoIdAnnotationTestBean {
        def id
        @Field
        def name
    }

}