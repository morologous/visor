package net.yankus.visor

import groovy.util.logging.Log4j

import org.elasticsearch.search.SearchHit
import org.elasticsearch.search.SearchHits
import org.elasticsearch.search.highlight.HighlightField;

@Log4j
class Marshaller {
	
    /** from ElasticSearchMarshaller */
	
	private static def flattenParameter = {key, value ->
		def map = [:]
		log.trace "Flattening $key : $value"
		if (value instanceof Collection) {
			value.each {
				map << Marshaller.flattenParameter(key, it)
			}
		} else if (value instanceof Map) {
			value.entrySet().each {
				map << Marshaller.flattenParameter(key+'.'+it.key, it.value)
			}
		} else if (value instanceof Expando) {
			// if the next step is a collection or map, go on, otherwise stop
			if (value.value instanceof Collection || value.value instanceof Map) {
				map << Marshaller.flattenParameter(key, value.value)
			} else {
				map << [(key):value]
			}
		} else {
			throw  new IllegalArgumentException("Value must be Expando or Collection or Map but was $value")
		}
		map
	}

	static def marshallSearchParameters = { parameters ->
		def map = [:]
		log.trace "Marshalling search parameters: $parameters"
		parameters.entrySet().each {
			map << Marshaller.flattenParameter(it.key, it.value)
		}
		map
	}

	static def findIdField = { bean ->
		def fields = Marshaller.findFieldWithAnnotation(Id, bean)
		if (fields.size() > 1) {
			def className = bean.getClass()
			throw new IllegalStateException("Bean $className has more than one @Id field annotation.")
		}
		if (fields.size() == 1) {
			return fields[0]
		}
		return null
	}

	static def getIdValueFromBean = { bean ->
		def field = Marshaller.findIdField bean
		def rawValue = bean[field.name]

		rawValue.toString()
	}

	static def unmarshall = { SearchHit hit, context ->
		log.trace "Unmarshalling hit"
		def unmarshalled = Marshaller.unmarshallMap(hit.getSource(), context.returnType)
		
		// detect and set Id
		def idField = Marshaller.findIdField(unmarshalled)
		if (idField) {
			unmarshalled[idField.name] = hit.id
		}

		log.trace "Hit score: $hit.score"
		unmarshalled.score = hit.score
		log.trace "hit highlights: ${hit.highlightFields()}"
		unmarshalled.snippets = [:]
		hit.highlightFields().keySet().each { fieldName ->
			HighlightField highlightField = hit.highlightFields()[(fieldName)]
			def snippets = []
			highlightField.fragments.each { fragment ->
				snippets.add(fragment.toString())
			}
			def highlights = new Expando()
			highlights.fragments = snippets
			unmarshalled.snippets.put(fieldName, highlights)
		}
		log.trace "unmarshalled snippets: ${unmarshalled.snippets}"
		unmarshalled
	}

	static def unmarshallAll = { SearchHits hits, context ->
		def unmarshalled = []
		hits.each { hit ->
			unmarshalled << Marshaller.unmarshall(hit, context)
		}

		unmarshalled
	}
	
	/** End ElasticSearchMarshaller */
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

    static def findFieldWithAnnotation = { annotationType, o -> 
        if (o instanceof Class) {
            def fields = []
            o.declaredFields.each {
                def annotation = it.getAnnotation annotationType
                if (annotation) {
                    fields << it
                }
            }
            fields
        } else {
            Marshaller.findFieldWithAnnotation(annotationType, o.class)
        }
    }

    static boolean isChildBean (o) {
        return !Marshaller.findFieldWithAnnotation(Field, o).isEmpty()
    }

    static def foreachProperty = { bean, notNull=false, callback ->
        Marshaller.getProperties(bean).keySet().each {
            def field = bean.class.getDeclaredField it
            def annotation = field.getAnnotation Field
            if (annotation && (notNull && bean[it] != null)) {
                callback(field, annotation)
            }
        }
    }

    static def foreachMappedProperty = { type, fieldPrefix='', callback ->
        type.getDeclaredFields().each {
            def annotation = it.getAnnotation Field
            if (annotation != null) {
                callback(fieldPrefix + it.name, it, annotation)
                if (isChildBean(annotation.type())) {
                    Marshaller.foreachMappedProperty(annotation.type(), it.name + '.', callback)
                }                
            }
        }
    }
    
    static def marshall = { bean, mode='QUERY' -> 
        def props = [:]
        log.trace "Marshalling mode: $mode"

        Marshaller.foreachProperty(bean, true) { field, annotation ->
            def marshallContext = new Expando()
        
            marshallContext.fieldName = field.name
            marshallContext.targetBean = bean
            marshallContext.mode = mode
            marshallContext.annotation = annotation

            def value = annotation.marshall().newInstance(null, null).call(marshallContext)

            log.trace "Marshalled $marshallContext.fieldName to $value"
            
            def prop
            // this is sorta kludgey.  We need to add extra info for QUERY operations
            // but keep the datamodel simple for complex datatypes.  This could probably
            // be done better with a closure at index time extracting only the value
            // but for today, my brain hurts too much to write that.
            if (mode == 'INDEX') {
                prop = value
            } else {
                prop = new Expando()
                prop.annotation = annotation
                prop.field = field
                prop.value = value                                 
            }
            props[field.name] = prop
        }
        
        props
    }

    static def unmarshallMap = { data, type -> 
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