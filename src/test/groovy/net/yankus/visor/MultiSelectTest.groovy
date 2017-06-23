package net.yankus.visor

import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import static org.junit.Assert.*
import net.yankus.visor.Visor

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

class MultiSelectTest {
    
    static def a
    static def b
    static def c
    static def alpha
    static def beta
    static def gamma
    static def i
    static def ii
    static def iii

    @BeforeClass
    public static void setUp() {
        a = new MultiSelectTestBean(id:'a', name:'a', type:'alphanumeric')
        alpha = new MultiSelectTestBean(id:'alpha', name:'alpha', type:'greek')
        i = new MultiSelectTestBean(id:'i', name:'i', type:'roman')
    

        SearchEngineTestHelper.index a
        SearchEngineTestHelper.index alpha
        SearchEngineTestHelper.index i

        SearchEngineTestHelper.refresh(a)
    }
    @AfterClass
    public static void tearDown() {
        SearchEngineTestHelper.delete a
        SearchEngineTestHelper.delete alpha
        SearchEngineTestHelper.delete i
    }

    @Test
    public void testMultiSelect() {
        def bean = new MultiSelectTestBean(type:new MultiSelect(values:['roman', 'greek']))
        def results = bean.search()

        assertEquals 2, results.count
        assertTrue results.list.contains(alpha)
        assertTrue results.list.contains(i)
    }
    

     @Visor(index='test', settings={ SearchEngineTestHelper.testESSettings(it) }  )
    @ToString
    @EqualsAndHashCode(excludes="score, snippets")
    static class MultiSelectTestBean {
        @Id
        def id
        @Field
        def name
        @Field
        def type
    }

}