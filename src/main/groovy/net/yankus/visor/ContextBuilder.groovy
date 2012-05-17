package net.yankus.visor

import groovy.util.Expando

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
		context.remote = annotation.remote()

		context.returnType = bean.class
		context.parameters = Marshaller.marshall(bean)

		context
	}

	private static def findAnnotation = { targetObj, annotationClass -> 
		def annotation = targetObj.class.getAnnotation(annotationClass)
		
		annotation
	}

}