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

    @Visor ( index = 'test',
           settings = { SearchEngineTestHelper.testESSettings.rehydrate(getDelegate(), getOwner(), getThisObject()).call() } )

    @ToString
    @EqualsAndHashCode(excludes="score")
    static class ComplexTypeParent {
        @Id
        def id
        @Field
        def name
        @Field(type=ComplexTypeChild)
        def children
    }

    @ToString
    @EqualsAndHashCode
    static class ComplexTypeChild {
        @Field
        def name
        @Field
        def num
    }

}