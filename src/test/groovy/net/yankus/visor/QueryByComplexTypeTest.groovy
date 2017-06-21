package net.yankus.visor

import org.junit.AfterClass
import static org.junit.Assert.*

import org.junit.BeforeClass
import org.junit.Test

import groovy.lang.Closure
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.yankus.visor.Visor

class QueryByComplexTypeTest { 

    static def foo

    @BeforeClass
    static void setUp() {
        foo = new ComplexTypeParent(id:'foo',
            name:'foo',
            firstChild:new ComplexTypeChild(name:'1kid', num:1),
            children:[new ComplexTypeChild(name:'child', num:2)])

        SearchEngineTestHelper.index(foo)

        SearchEngineTestHelper.showAll (ComplexTypeParent)
    }

    @AfterClass
    static void tearDown() {
        if (foo) {
            SearchEngineTestHelper.delete(foo)
        }
    }
    
    @Test
    public void testChildQuery() {

        def results = new ComplexTypeParent(children:[new ComplexTypeChild(num:2)]).search()
        assertEquals 1, results.count

        assertEquals foo, results.list[0]
    }

    @Test
    public void testChildHighlight() {

        def results = new ComplexTypeParent(queryString:'child').search()
        assertEquals 1, results.count

        def result = results.list[0]
        assertEquals foo, result
        assertNotNull result.snippets
        assertNotNull result.snippets['children.name']
        assertEquals '<strong>child</strong>', result.snippets['children.name'].fragments[0]
    }

    @Visor(index='test', settings={ SearchEngineTestHelper.testESSettings(it) }  )
    @ToString
    @EqualsAndHashCode(excludes="score, snippets")
    static class ComplexTypeParent {
        @Id
        def id
        @Field
        def name
        @Field(type=ComplexTypeChild)
        def firstChild
        @Field(type=ComplexTypeChild)
        def children
    }

    @ToString
    @EqualsAndHashCode
    static class ComplexTypeChild {
        @Field(highlight=true)
        def name
        @Field
        def num
    }

}