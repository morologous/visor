package net.yankus.visor

import org.joda.time.format.ISODateTimeFormat
import groovy.util.logging.Log4j 

@Log4j
class FieldUtils {
    
    public static def unmarshallDate = { // prop, target, value ->
        def dateTime = ISODateTimeFormat.dateTimeParser().parseDateTime(it.fieldValue)
        log.debug("Parsed $it.value to DateTime $dateTime")
        it.targetBean[it.fieldName] = dateTime?.toDate()
    } 

    public static def marshallCollection = { // prop, target -> 
        def coll = []
        it.targetBean[it.fieldName].each {
            coll << Marshaller.marshall(it)            
        }
        coll
    } 

    public static def unmarshallCollection = { context ->
        def coll = []
        context.fieldValue.each {
            coll << Marshaller.unmarshall(it,  context.annotation.type())
        }
        context.targetBean[context.fieldName] = coll
    }

}