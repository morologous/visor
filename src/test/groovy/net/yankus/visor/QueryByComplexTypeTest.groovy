package net.yankus.visor

import org.junit.BeforeClass
import org.junit.AfterClass
import org.junit.Test
import static org.junit.Assert.*
import groovy.transform.ToString
import groovy.transform.EqualsAndHashCode

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
        assertEquals '<em>child</em>', result.snippets['children.name'].fragments[0]
    }

    @net.yankus.visor.Visor ( index = 'test',
           settings = { SearchEngineTestHelper.testESSettings.rehydrate(getDelegate(), getOwner(), getThisObject()).call() } )

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