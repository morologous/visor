package net.yankus.visor


import org.junit.AfterClass
import static org.junit.Assert.*

import org.junit.BeforeClass
import org.junit.Test

import groovy.lang.Closure
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.yankus.visor.Visor


class SearchPagingTest {

    static def testBeans = []
    
    @BeforeClass
    public static void setUp() throws Exception {
        for (i in 0..24) {
            def bean = new SearchPagingTestBean(id:"$i", name:'bean_'+i)
            bean.index().actionGet()
            testBeans << bean
        }
        SearchEngineTestHelper.refresh(testBeans[0])
    }

    @AfterClass
    public static void tearDown() throws Exception {
        testBeans.each {
            SearchEngineTestHelper.delete it
        }
    }

    @Test
    void testPaging() {
        def results =  new SearchPagingTestBean(queryString:'bean*', pageSize:5, startingIndex:10, sortOrder: 'name').search()

        assertEquals 5, results.list.size()
        assertEquals 25, results.count

        def third = results.list[2]

        results = new SearchPagingTestBean(queryString:'bean*', pageSize:5, startingIndex:12, sortOrder: 'name').search()

        assertEquals third, results.list[0]
    }

    @Visor ( index = 'test',
           settings = { SearchEngineTestHelper.testESSettings.rehydrate(getDelegate(), getOwner(), getThisObject()).call() } )
    @ToString
    @EqualsAndHashCode(excludes="score")
    static class SearchPagingTestBean {
        @Id
        def id
        @Field
        def name
    }
}

