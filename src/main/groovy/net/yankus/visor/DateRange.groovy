package net.yankus.visor

import java.io.Serializable

import groovy.transform.ToString

@ToString
class DateRange implements Serializable {
    
	public static final serialVersionUID = 1L;

    def from
    def to

}