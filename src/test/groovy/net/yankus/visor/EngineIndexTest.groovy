package net.yankus.visor


import static org.junit.Assert.*
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

class EngineIndexTest {
    
    static def gazonk
    static def flurgle

    @BeforeClass
    static void setUp() {
        gazonk = new EngineIndexTestBean(id:'' + 'gazonk'.hashCode(), name:'gazonk', num:100)
        flurgle = new EngineIndexTestBean(id:'' + 'flurgle'.hashCode(), name:'flurgle', num:200)

        SearchEngineTestHelper.index gazonk

    }

    @AfterClass
    static void tearDown() {
        SearchEngineTestHelper.delete gazonk
        SearchEngineTestHelper.delete flurgle
    }

    @Test
    void testIndex() {
        def indexR = flurgle.index()

        indexR.response '5s'
        SearchEngineTestHelper.refresh(flurgle)

        def result = new EngineIndexTestBean(num:200).search()
        assertEquals 1, result.count
        assertEquals flurgle, result.list[0]
    }
	
	@Test
	void testIndexCallback() {
		def callbackCalled = true
		def blargle = new EngineIndexTestBean(id:''+'blargle'.hashCode(), name:'blargle', num:300) 
		def indexR = blargle.index()
		indexR.success = {
			def result = new EngineIndexTestBean(num:300).search()
			assertEquals 1, result.count
			assertEquals blargle, result.list[0]
			callbackCalled = true
		}
		ExecutorService executor = Executors.newFixedThreadPool(1)
		executor.execute( {
				while (!callbackCalled) {
					Thread.sleep(250)
				}
				return
			 } as Runnable)
		executor.awaitTermination(10, TimeUnit.SECONDS)
		assertTrue callbackCalled
	}

    @net.yankus.visor.Visor(index='test', settings = { SearchEngineTestHelper.testESSettings.rehydrate(getDelegate(), getOwner(), getThisObject()).call() } )
    @ToString
    @EqualsAndHashCode(excludes="score, snippets")
    static class EngineIndexTestBean {
        @Id
        def id
        @Field
        def name
        @Field
        def num

    }

}