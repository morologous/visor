package net.yankus.visor

import groovy.transform.ToString
import java.io.Serializable

@ToString
class DateRange implements Serializable {
    
	public static final serialVersionUID = 1L;

    def from
    def to

}