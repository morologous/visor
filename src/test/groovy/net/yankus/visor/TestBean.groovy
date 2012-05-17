package net.yankus.visor

import groovy.transform.ToString

@Visor(filters = { terms(security:['low', 'none']) }, 
       index = "test", 
       settings = { 
        node { local = true} 
        } )
@ToString
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