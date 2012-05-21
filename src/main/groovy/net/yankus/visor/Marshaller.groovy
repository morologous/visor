package net.yankus.visor

import groovy.util.logging.Log4j 

@Log4j
class Marshaller {
    
    private static def getProperties = { bean ->
        def props = bean.getProperties()

        props.remove 'metaClass'
        props.remove 'class'

        props
    }

    private static def getField = { targetBean, fieldName -> 
        def field
        try {
            field = targetBean.class.getDeclaredField fieldName
            } catch (NoSuchFieldException nsfe) {
                log.warn ("Target bean $targetBean does not have field $fieldName")
            }
        field
    }

    static def foreachProperty = { bean, callback ->
        Marshaller.getProperties(bean).keySet().each {
            def field = bean.class.getDeclaredField it
            def annotation = field.getAnnotation Field
            if (annotation && bean[it]) {
                callback(field, annotation)
            }
        }
    }

    static def marshall = { bean, mode='QUERY' -> 
        def props = [:]
        
        Marshaller.foreachProperty(bean) { field, annotation ->
            def marshallContext = new Expando()
            marshallContext.fieldName = field.name
            marshallContext.targetBean = bean
            marshallContext.mode = mode

            //log.debug "marshallContext $marshallContext"

            def value = annotation.marshall().newInstance(null, null).call(marshallContext)
            def prop
            // this is sorta kludgey.  We need to add extra info for QUERY operations
            // but keep the datamodel simple for complex datatypes.  This could probably
            // be done better with a closure at index time extracting only the value
            // but for today, my brain hurts too much to write that.
            if (mode == 'INDEX') {
                //log.debug 'Marshalling for INDEX operation'
                prop = value
            } else {
                //log.debug "Marshalling for $mode operation"
                prop = new Expando()
                prop.annotation = annotation
                prop.field = field
                prop.value = value                                 
            }
            props[field.name] = prop
        }
        
        props
    }

    static def unmarshall = { data, type -> 
        def targetBean = type.newInstance()
        data.entrySet().each {
            def field = Marshaller.getField(targetBean, it.key)
            if (field) {
                def annotation = field.getAnnotation Field
                if (annotation) {
                    def unmarshallContext = new Expando()
                    unmarshallContext.fieldName = field.name
                    unmarshallContext.targetBean = targetBean
                    unmarshallContext.fieldValue = it.value
                    unmarshallContext.field = field
                    unmarshallContext.annotation = annotation
                    annotation.unmarshall().newInstance(null, null).call(unmarshallContext)
                }
            }
        }

        targetBean
    }

}