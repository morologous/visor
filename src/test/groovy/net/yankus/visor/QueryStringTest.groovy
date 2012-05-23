package net.yankus.visor

import org.junit.BeforeClass
import org.junit.AfterClass
import org.junit.Test
import static org.junit.Assert.*
import groovy.transform.ToString
import groovy.transform.EqualsAndHashCode

class QueryStringTest {
    
    static def qstr1
    static def qstr2

    @BeforeClass
    static void setUp() {
        qstr1 = new QueryStringTestBean(id:'1', text:'Mount Etna, is an active stratovolcano on the east coast of Sicily, close to Messina and Catania. It lies above the convergent plate margin between the African Plate and the Eurasian Plate. It is the tallest active volcano in Europe, currently standing 3,329 m (10,922 ft) high, though this varies with summit eruptions. It is the highest mountain in Italy south of the Alps. Etna covers an area of 1,190 km² (459 sq mi) with a basal circumference of 140 km. This makes it by far the largest of the three active volcanoes in Italy, being about two and a half times the height of the next largest, Mount Vesuvius. Only Mount Teide in Tenerife surpasses it in the whole of the European-North-African region.[2] In Greek Mythology, the deadly monster Typhon was trapped under this mountain by Zeus, the god of the sky, and the forges of Hephaestus were said to also be located underneath it.')
        qstr2 = new QueryStringTestBean(id:'2', text:'Spaghetti is a long, thin, cylindrical pasta of Italian origin.[1] Spaghetti is made of semolina or flour and water. Italian dried spaghetti is made from durum wheat semolina, but outside of Italy it may be made with other kinds of flour. Traditionally, most spaghetti was 50 cm (20 in) long, but shorter lengths gained in popularity during the latter half of the 20th century and now spaghetti is most commonly available in 25–30 cm (10–12 in) lengths. A variety of pasta dishes are based on it, from spaghetti alla Carbonara or garlic and oil to a spaghetti with tomato sauce, meat and other sauces.')
    
        SearchEngineTestHelper.index qstr1
        SearchEngineTestHelper.index qstr2
    }

    @AfterClass
    static void tearDown() {
        SearchEngineTestHelper.delete qstr1
        SearchEngineTestHelper.delete qstr2
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

    }

    public void testSearchItalyAndVolcano() {
        def results = new QueryStringTestBean(queryString:'italy AND volcano')
        assertEquals 1, results.count
        assertTrue results.list.contains(qstr1)
    }

    @Visor ( index = 'test',
           settings = { SearchEngineTestHelper.testESSettings.rehydrate(getDelegate(), getOwner(), getThisObject()).call() } )
    @ToString
    @EqualsAndHashCode
    static class QueryStringTestBean {
        @Id
        def id
        @Field
        def text
    }
}