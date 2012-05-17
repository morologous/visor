package net.yankus.visor

import org.joda.time.format.ISODateTimeFormat
import groovy.util.logging.Log4j 

@Log4j
class FieldUtils {
    
    public static def marshallDate = {
        it.targetBean[it.fieldName].time
    }

    public static def unmarshallDate = {
        //def dateTime = ISODateTimeFormat.dateTimeParser().parseDateTime(it.fieldValue)
        //it.targetBean[it.fieldName] = dateTime?.toDate()
        it.targetBean[it.fieldName] = new Date(it.fieldValue)
    } 

    public static def marshallCollection = { 
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