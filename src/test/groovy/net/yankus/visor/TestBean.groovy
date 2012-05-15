package net.yankus.visor

import groovy.transform.ToString

@Visor(filters = { terms(security:['low', 'none']) }, 
       returnType = TestBean.class, 
       index = "test", 
       settings = { 
        node { local = true} 
        } )
@ToString
public class TestBean {
    @Id
    def id

    @QueryParam
    def value

    @QueryParam
    def num

    @QueryParam
    def security
}