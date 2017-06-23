package net.yankus.visor

import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import static org.junit.Assert.*
import net.yankus.visor.Visor

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

class QueryStringTest {
    
    static def qstr1
    static def qstr2
    static def qstr3

    @BeforeClass
    static void setUp() {
        qstr1 = new QueryStringTestBean(id:'1', category:'geography', text:'Mount Etna, is an active stratovolcano on the east coast of Sicily, close to Messina and Catania. It lies above the convergent plate margin between the African Plate and the Eurasian Plate. It is the tallest active volcano in Europe, currently standing 3,329 m (10,922 ft) high, though this varies with summit eruptions. It is the highest mountain in Italy south of the Alps. Etna covers an area of 1,190 km (459 sq mi) with a basal circumference of 140 km. This makes it by far the largest of the three active volcanoes in Italy, being about two and a half times the height of the next largest, Mount Vesuvius. Only Mount Teide in Tenerife surpasses it in the whole of the European-North-African region.[2] In Greek Mythology, the deadly monster Typhon was trapped under this mountain by Zeus, the god of the sky, and the forges of Hephaestus were said to also be located underneath it.')
        qstr2 = new QueryStringTestBean(id:'2', category:'food', text:'Spaghetti is a long, thin, cylindrical pasta of Italian origin.[1] Spaghetti is made of semolina or flour and water. Italian dried spaghetti is made from durum wheat semolina, but outside of Italy it may be made with other kinds of flour. Traditionally, most spaghetti was 50 cm (20 in) long, but shorter lengths gained in popularity during the latter half of the 20th century and now spaghetti is most commonly available in 2530 cm (1012 in) lengths. A variety of pasta dishes are based on it, from spaghetti alla Carbonara or garlic and oil to a spaghetti with tomato sauce, meat and other sauces.')
        qstr3 = new QueryStringTestBean(id:'3', category:'cars', text:'An automobile, autocar, motor car or car is a <em>wheeled motor vehicle</em> used for transporting passengers, which also carries its own engine or motor.')
        SearchEngineTestHelper.index qstr1
        SearchEngineTestHelper.index qstr2
        SearchEngineTestHelper.index qstr3

        SearchEngineTestHelper.search qstr1
    }

    @AfterClass
    static void tearDown() {
        SearchEngineTestHelper.delete qstr1
        SearchEngineTestHelper.delete qstr2
        SearchEngineTestHelper.delete qstr3
    }

    @Test
    public void testTwoParams() {
        def results = new QueryStringTestBean(queryString:'italy', category:'food').search()
        assertEquals 1, results.count
        assertTrue results.list.contains(qstr2)
    }

    @Test
    public void testSearchItaly() {
        def results = new QueryStringTestBean(queryString:'italy').search()
        assertEquals 2, results.count
        assertTrue results.list.contains(qstr1)
        assertTrue results.list.contains(qstr2)

    }

    @Test
    public void testSearchWheat() {
        def results = new QueryStringTestBean(queryString:'wheat').search()
        assertEquals 1, results.count
        assertTrue results.list.contains(qstr2)
        assertNotNull(results.list[0].snippets)
        assertTrue results.list[0].snippets.text.fragments[0].contains('made from durum <strong>wheat</strong> semolina, but outside')
    }

    @Test
    public void testSearchHighlightsDisabled() {
        def results = new QueryStringTestBean(queryString:'wheat', visorOpts:['visor.highlight.disabled':true]).search()
        assertEquals 1, results.count
        assertTrue results.list.contains(qstr2)
        assertEquals ([:], results.list[0].snippets)
    }

    @Test
    public void testSearchItalyAndVolcano() {
        def results = new QueryStringTestBean(queryString:'italy AND volcano').search()
        assertNotNull results
        assertEquals 1, results.count
        assertTrue results.list.contains(qstr1)        
    }

    @Test
    public void testTextIsExcluded() {
        def results = new QueryStringTestBean(queryString:'italy AND volcano').search()
        assertNotNull results
        assertEquals 1, results.count
        assertTrue results.list.contains(qstr1)        
        assertNull results.list[0].text
    }

    @Test
    public void testHighlighterStrong() {
        def results = new QueryStringTestBean(queryString:'wheat').search()
        assertEquals 1, results.count
        assertTrue results.list.contains(qstr2)
        assertNotNull(results.list[0].snippets)
        assertTrue(results.list[0].snippets.text.fragments[0].contains('<strong>'))
        assertTrue(results.list[0].snippets.text.fragments[0].contains('</strong>'))
    }

    @Test
    public void testHighlighterEscapesOtherHtml() {
        def results = new QueryStringTestBean(queryString:'vehicle').search()
        assertEquals 1, results.count
        assertTrue results.list.contains(qstr3)
        assertNotNull(results.list[0].snippets)
        println results.list.snippets.text
        assertTrue(results.list[0].snippets.text.fragments[0].contains('<strong>'))
        assertTrue(results.list[0].snippets.text.fragments[0].contains('</strong>'))
        assertFalse(results.list[0].snippets.text.fragments[0].contains('</em>'))
        assertFalse(results.list[0].snippets.text.fragments[0].contains('<em>'))
    }

    @Visor(index='test', settings={ SearchEngineTestHelper.testESSettings(it) }  )
    @ToString
    @EqualsAndHashCode(excludes="score, snippets, text")
    static class QueryStringTestBean {
        @Id
        def id
        @Field(highlight=true, excludeFromResults=true)
        def text
        @Field()
        def category
    }
}