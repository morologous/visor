package net.yankus.visor

import groovy.util.logging.Log4j 

@Log4j
final class ContextBuilder {

	public static def build = { bean ->
		def context = new Expando()

		def annotation = ContextBuilder.findAnnotation(bean, Visor)
		if (!annotation) {
			throw new IllegalArgumentException('Bean does not have required Visor annotation.')
		}
		context.filters = annotation.filters()
		context.index = annotation.index()
		context.settings = annotation.settings()
		context.remoteAddresses = annotation.remoteAddresses().newInstance(null, null).call()
		context.defaultTimeout = annotation.defaultTimeout()
		
		context.queryBean = bean
		context.returnType = bean.class
		context.visorOpts = bean.visorOpts?:[:]


		context.connectionFactory = annotation.connectionFactory().newInstance(null, null).call(context)

		log.info "visor opts: ${bean.visorOpts}"
		log.debug "Constructed context: $context"

		context
	}

	private static def findAnnotation = { targetObj, annotationClass -> 
		def annotation = targetObj.class.getAnnotation(annotationClass)
		
		annotation
	}

}