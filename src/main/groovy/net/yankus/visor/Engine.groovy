package net.yankus.visor

import org.elasticsearch.search.SearchHit
import groovy.util.Expando
import org.elasticsearch.action.index.IndexResponse
import groovy.util.logging.Log4j 

@Log4j
class Engine {

    static def doInElasticSearch = { context, operation ->
        def datasource = ElasticSearchClientFactory.create context
        return operation.call(datasource.client)
    }

    static def search = { queryParam ->
        def context = ContextBuilder.build queryParam
        def queryParams = ElasticSearchMarshaller.marshallSearchParameters(Marshaller.marshall(queryParam))
        log.info "Searching $context.index for type $context.returnType.simpleName"

        log.debug "Marshalled query parameters: $queryParams"

        def queryStrVal = queryParam['queryString']
        if (queryStrVal) { log.debug "Detected query_string param: $queryStrVal" }
        
        Engine.doInElasticSearch(context) { client ->
            def search = client.search (({
                indices context.index
                types context.returnType.simpleName
                source {
                    query {
                        filtered {
                            query {
                                if (queryStrVal) {
                                    log.debug "Applying query_string $queryStrVal"
                                    query_string (query:queryStrVal)
                                }
                                queryParams.entrySet().each { entry ->
                                    log.debug "Adding query parameter $entry.key : $entry.value.value"
                                    entry.value
                                         .annotation
                                         .applyToQuery()
                                         .newInstance(null, null)
                                         .rehydrate(delegate, owner, thisObject)
                                         .call(entry.key, entry.value.value)
                                }            
                            }
                            filter = context.filters
                                            .newInstance(null, null)
                                            .rehydrate(delegate, owner, thisObject)
                        }
                    }
                }  
            }))

            def results = new Expando()
            log.debug search.response
            results.response = search.response '5s'
            results.count = search.response.hits().totalHits()

            log.debug "Found $results.count hits."

            results.list = ElasticSearchMarshaller.unmarshallAll(search.response.hits, context)

            results
        }
    }

    static def index = { target -> 
        def context = ContextBuilder.build(target)

        def indexParams = Marshaller.marshall(target, 'INDEX')
        def targetIdField = ElasticSearchMarshaller.findIdField(target)
        if (!targetIdField) {
            throw new IllegalArgumentException('Bean must have Id-annotated field to be stored in search index')
        }
        def targetId = target[targetIdField.name]
        if (!targetId) {
            throw new IllegalArgumentException('Bean must have populated Id-annotated field to be stored in search index.')
        }
        log.info "Indexing to $context.index id $targetId"
        log.debug "Indexed values: $indexParams"
        Engine.doInElasticSearch(context) { client -> 
            def result = client.index {
                index context.index
                type context.returnType.simpleName
                id "$targetId"
                source indexParams
            }
            
            result
        }

    }

    static def delete = { target ->
        def context = ContextBuilder.build target 
        def idField = ElasticSearchMarshaller.findIdField target
        def idValue = target[idField.name]
        log.info "Deleting $context.index id $idValue"
        if (idField && target[idField.name]) {
            Engine.doInElasticSearch(context) { client ->
                def result = client.delete {
                    index context.index
                    type context.returnType.simpleName
                    id "$idValue"
                }

                result 
            }            
        }
    }

}