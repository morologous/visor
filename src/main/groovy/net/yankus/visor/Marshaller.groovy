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

    static def marshall = { bean -> 
        def props = [:]
        Marshaller.getProperties(bean).keySet().each {
            def field = bean.class.getDeclaredField it
            def annotation = field.getAnnotation Field
            if (annotation && bean[it]) {
                def marshallContext = new Expando()
                marshallContext.fieldName = it
                marshallContext.targetBean = bean
                props[it] = annotation.marshall().newInstance(null, null).call(marshallContext)
            }
        }
        log.debug props

        props
    }

    static def unmarshall = { data, type -> 
        def targetBean = type.newInstance()
        data.entrySet().each {
            log.debug(it)
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