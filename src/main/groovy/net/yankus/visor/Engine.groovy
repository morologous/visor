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
        def startInstant = new Date().time

        def context = ContextBuilder.build queryParam
        def queryParams = ElasticSearchMarshaller.marshallSearchParameters(Marshaller.marshall(queryParam))
        log.info "Searching $context.index for type $context.returnType.simpleName"

        log.debug "Marshalled query parameters: $queryParams"

        def queryStrVal = queryParam.queryString
        if (queryStrVal) { log.debug "Detected query_string param: $queryStrVal" }

        // Paging
        def pageSize = queryParam.pageSize
        def startingIndex = queryParam.startingIndex
        log.debug "Paging information: pageSize $pageSize startingIndex $startingIndex"

        // sorting
        def sortOrder = []
        if (queryParam.sortOrder) {
            sortOrder << queryParam.sortOrder
        }
        // at the bottom ALWAYS sort by score asc
        sortOrder << "_score" 
        log.debug "Sorting: $sortOrder"

        // metrics
        def assemblyDoneInstant = new Date().time 

        Engine.doInElasticSearch(context) { client ->
            def search = client.search (({
                indices context.index
                types context.returnType.simpleName         
                source {   
                    from = startingIndex
                    size = pageSize
                    sort = sortOrder
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

            def queryBuiltInstant = new Date().time
            
            def response = search.response '5s'
            log.debug "Search Response: $response"

            def responseInstant = new Date().time

            def results = new Expando()
            results.response = response

            results.list = ElasticSearchMarshaller.unmarshallAll(response.hits, context)
            
            def unmarshallInstant = new Date().time

            results.count = response.hits().totalHits()
            results.pageSize = results.list.size()
            results.query = queryParam

            log.debug "Search matched $results.count hits, returned $results.pageSize"

            // TODO: probably don't need an expando here.
            results.stats = new Expando()
            results.stats.engineTook = response.tookInMillis
            //results.stats.shards = response.shards
            //results.stats.timedOut = response.timedOut
            results.stats.maxScore = response.hits().maxScore
            results.stats.detailTime = [:]
            results.stats.detailTime << ['assemblyDone': assemblyDoneInstant - startInstant]
            results.stats.detailTime << ['queryBuilt': queryBuiltInstant - assemblyDoneInstant]
            results.stats.detailTime << ['response': responseInstant - queryBuiltInstant]
            results.stats.detailTime << ['unmarshall': unmarshallInstant - responseInstant]
            results.stats.detailTime << ['statsDone': new Date().time - unmarshallInstant ]
            results.stats.detailTime << ['overall': new Date().time - startInstant ]

            log.debug "Search Execution Stats: $results.stats"
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