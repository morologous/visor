package net.yankus.visor

import groovy.util.logging.Log4j 

@Log4j
class BeanInspector {
	
	private static def getProperties = { bean ->
		def props = bean.getProperties()

		props.remove 'metaClass'
		props.remove 'class'

		props
	}

	static def inspect = { bean -> 
		def props = BeanInspector.getProperties bean
		props.clone().keySet().each {
			def field = bean.class.getDeclaredField it
			def annotation = field.getAnnotation Field
			if (!annotation || !props[it]) {
				props.remove it
			} 
		}
		log.debug props
		props

	}

}