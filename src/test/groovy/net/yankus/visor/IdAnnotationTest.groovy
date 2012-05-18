package net.yankus.visor


import org.junit.BeforeClass
import org.junit.AfterClass
import org.junit.Test
import static org.junit.Assert.*
import groovy.transform.ToString
import groovy.transform.EqualsAndHashCode

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
    }

    @AfterClass
    static void tearDown() {
        SearchEngineTestHelper.delete foo 
        SearchEngineTestHelper.delete bar
        SearchEngineTestHelper.delete baz
    }

    @Test
    void testSearchEach() {
        def engine = new Engine()
        def results = engine.doQuery(new SingleIdAnnotationTestBean(name:'aaa'))

        assertEquals 1, results.count
        assertEquals foo, results.list[0]

        try {
            results = engine.doQuery(new MultipleIdAnnotationTestBean(name:'bbb'))
            assertFalse true
        } catch (IllegalStateException ise) {
                // expected
        }

        results = engine.doQuery(new NoIdAnnotationTestBean(name:'ccc'))
        assertEquals 1, results.count
        assertEquals new NoIdAnnotationTestBean(name:'ccc'), results.list[0]
    }

    @Visor(index='test')
    @ToString
    @EqualsAndHashCode
    static class SingleIdAnnotationTestBean {
        @Id
        def id
        @Field
        def name
    }

    @Visor(index='test')
    @ToString
    @EqualsAndHashCode
    static class MultipleIdAnnotationTestBean {
        @Id
        def id

        @Id
        def id2

        @Field
        def name
    }

    @Visor(index='test')
    @ToString
    @EqualsAndHashCode
    static class NoIdAnnotationTestBean {
        def id
        @Field
        def name
    }

}