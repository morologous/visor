package net.yankus.visor

import org.elasticsearch.search.SearchHit
import groovy.util.Expando
import org.elasticsearch.action.index.IndexResponse
import groovy.util.logging.Log4j 
import static org.elasticsearch.index.query.FilterBuilders.*
import static org.elasticsearch.index.query.QueryBuilders.*
import org.elasticsearch.search.sort.SortOrder

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

        def highlights = []
        Marshaller.foreachMappedProperty(queryParam.class) { flattenedFieldName, field, annotation -> 
            if (annotation.highlight()) {
                highlights << flattenedFieldName
            }
        }

        def idField = ElasticSearchMarshaller.findIdField queryParam
        def ids = []
        if (idField && queryParam[idField.name] != null) {
            ids << queryParam[idField.name]
            ids = ids.collect {
                '' + it
            }
            log.debug "Detected IDs for query: $ids"
        }

        // metrics
        def assemblyDoneInstant = new Date().time 

        Engine.doInElasticSearch(context) { client ->

            def query = boolQuery()

            def s = client.prepareSearch(context.index)
                          .setFrom(startingIndex as int)
                          .setSize(pageSize as int)
                          .setTypes(context.returnType.simpleName)

            highlights.each {
                log.debug "Adding highlighted field: $it"
                s.addHighlightedField(it)
            }               

            sortOrder.each {
                if (it instanceof String) {
                    s.addSort(it, SortOrder.ASC)
                } else { // presume map
                    it.entrySet().each {
                        s.addSort(it.key, SortOrder.ASC.toString().equalsIgnoreCase(it.value)? SortOrder.ASC : SortOrder.DESC)
                    }
                }
                    
            }

            def bool = boolQuery()            
            queryParams.entrySet().each { entry -> 
                bool.must(entry.value
                               .annotation
                               .applyToQuery()
                               .newInstance(null, null)
                               .rehydrate(delegate, owner, thisObject)
                               .call(entry.key, entry.value.value))
            }

            if (!ids.isEmpty()) {
                bool.must(idsQuery(context.returnType.simpleName).addIds(ids as String[]))
            }

            if (queryStrVal) {
                log.debug "Applying query_string $queryStrVal"
                bool.must(queryString(queryStrVal))
            }

            s.setQuery(bool)

            def filterClosure = context.filters.newInstance(null, null)            
            s.setFilter filterClosure.call(context)

            def searchR = s.gexecute()

            def queryBuiltInstant = new Date().time
            
            def response = searchR.response '5s'
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

        def targetId = ElasticSearchMarshaller.getIdValueFromBean target
        if (!targetId) {
            throw new IllegalArgumentException('Bean must have populated Id-annotated field to be stored in search index.')
        }

        log.info "Indexing to $context.index id $targetId"
        log.debug "Indexed values: $indexParams"
        Engine.doInElasticSearch(context) { client -> 
            def result = client.index {
                index context.index
                type context.returnType.simpleName
                id targetId
                source indexParams
            }
            
            result
        }

    }

    static def delete = { target ->
        def context = ContextBuilder.build target 
        def idValue = ElasticSearchMarshaller.getIdValueFromBean target
        log.info "Deleting $context.index id $idValue"
        if (idValue) {
            Engine.doInElasticSearch(context) { client ->
                def result = client.delete {
                    index context.index
                    type context.returnType.simpleName
                    id idValue
                }

                result 
            }            
        }
    }

}