package net.yankus.visor

class ContextBuilder {

	public static def build = {
		def buildFromAnnotation = {  
			def typeAnnotation = it.class.getAnnotation(QueryBean)
			def context = null
			if (typeAnnotation) {
				context = [:]
				context << ['settings':typeAnnotation.settings()]
				context << ['index':typeAnnotation.index()]
				context << ['filters':typeAnnotation.filters()]
				context << ['returnType':typeAnnotation.returnType()]				
			}

			context
		} 
		def buildFromClosure = { 
			it.class.getField('visor')
		}
		def context = buildFromAnnotation(it)
		/*buildFromClosure(it) 
		if (!context) 
			context = 
		*/
		context
	}


}