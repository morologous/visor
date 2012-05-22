package net.yankus.visor

import org.joda.time.format.ISODateTimeFormat
import groovy.util.logging.Log4j 

@Log4j
class FieldUtils {
    
    public static def marshallDate = {
        if (it.targetBean[it.fieldName] instanceof Date) {
            it.targetBean[it.fieldName].time
        } else {
            def rangeQ = it.targetBean[it.fieldName]
            rangeQ.fromDt = rangeQ.fromDt?.time
            rangeQ.toDt = rangeQ.toDt?.time
            log.debug rangeQ
            rangeQ
        }
    }

    public static def unmarshallDate = {
        //def dateTime = ISODateTimeFormat.dateTimeParser().parseDateTime(it.fieldValue)
        //it.targetBean[it.fieldName] = dateTime?.toDate()
        it.targetBean[it.fieldName] = new Date(it.fieldValue)
    } 

    public static def applyToQueryDate = { key, value ->
        log.debug "$key : $value"
        if (value instanceof Expando) {
            range {
                "$key" {
                    from: value.fromDt
                    to: value.toDt                            
                }
            }  
        } else {
            field ((key):value)
        }
    }

    public static def marshallCollection = { ctx ->
        def coll = []
        ctx.targetBean[ctx.fieldName].each {
            coll << Marshaller.marshall(it, ctx.mode)            
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