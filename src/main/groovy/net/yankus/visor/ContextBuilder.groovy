package net.yankus.visor

final class ContextBuilder {

	private static final ContextBuilder INSTANCE = new ContextBuilder()

	private ContextBuilder() { }

	public def build = {
		def context = buildFromAnnotation(it)

		context
	}

	private def buildFromAnnotation = { 
		def context = [:]

		context << pullAnnotationConfig(findAnnotation(it, Visor), ['filters', 'returnType', 'index', 'settings'])
		
		context
	} 

	private def findAnnotation = { targetObj, annotationClass -> 
		def annotation = targetObj.class.getAnnotation(annotationClass)
		
		annotation
	}

	private def pullAnnotationConfig = { annotationInstance, fields -> 
		def data = [:]
		fields.each {
			data << [(it):annotationInstance?.class.getMethod(it)?.invoke(annotationInstance)]
		}

		data
	}


}