package net.yankus.visor

import groovy.transform.ToString
import groovy.transform.EqualsAndHashCode

@Visor(filters = { terms(security:['low', 'none']) }, 
       index = "test",
       settings = { SearchEngineTestHelper.testESSettings.rehydrate(getDelegate(), getOwner(), getThisObject()).call() } )
@ToString
@EqualsAndHashCode
public class TestBean {
    @Id
    def id

    @Field
    def value

    @Field
    def num

    @Field
    def security
}