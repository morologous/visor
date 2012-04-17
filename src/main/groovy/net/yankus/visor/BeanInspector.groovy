package net.yankus.visor

class BeanInspector {
	
	static def inspect = { queryBean -> 
		def queryMap = [:]

		def getters = queryBean.class.methods.findAll{ it.name =~ /^get[A-Z]/ }
		getters.each { method ->
			def fieldName = method.name[3].toLowerCase() + method.name[4..-1]
			def field = queryBean.class.declaredFields.find { it.name == fieldName }
			if (field) {
				if (field.declaredAnnotations.find {it instanceof QueryParam}) {
					def value = method.invoke(queryBean)
					if (value) {
						queryMap << [(fieldName):value]
					}
				}				
			}				
		}
		queryMap
	}

}