package net.yankus.visor

import groovy.util.logging.Log4j 
import groovy.util.Expando
@Log4j
class BeanInspector {
	
	private static def getProperties = { bean ->
		def props = bean.getProperties()

		props.remove 'metaClass'
		props.remove 'class'

		props
	}

	static def inspect = { bean -> 
		def props = [:]
		BeanInspector.getProperties(bean).keySet().each {
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

}