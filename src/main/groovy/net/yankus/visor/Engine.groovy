package net.yankus.visor

import org.elasticsearch.action.ListenableActionFuture
import org.elasticsearch.action.delete.DeleteResponse
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client
import org.elasticsearch.index.query.*

import groovy.util.logging.Log4j

import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder

import static org.elasticsearch.index.query.QueryBuilders.*

@Log4j
class Engine {
	
	static def inFilter(String field, String[] values) {
		termsQuery(field, Arrays.asList(values))
	}

    static def doInElasticSearch = { context, operation ->
        def client = context.connectionFactory.create()    
        return operation.call(client)
    }

    static def doSearch (context, queryParam, stats = [:], countOnly = false) {

        def queryParams = Marshaller.marshallSearchParameters(Marshaller.marshall(queryParam))
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
        sortOrder <<['_score':'desc'] 
        log.debug "Sorting: ${sortOrder}"

        def highlights = []
        def excludes = []
        Marshaller.foreachMappedProperty(queryParam.class) { flattenedFieldName, field, annotation -> 
            if (annotation.highlight()) {
                highlights << flattenedFieldName
            }
            if (annotation.excludeFromResults()) {
                excludes << flattenedFieldName
            }
        }

        def idField = Marshaller.findIdField queryParam
        def ids = []
        if (idField && queryParam[idField.name] != null) {
            ids << queryParam[idField.name]
            ids = ids.collect {
                '' + it
            }
            log.debug "Detected IDs for query: ${ids}"
        }

        // metrics
        stats.assemblyDoneInstant = new Date().time 

        Engine.doInElasticSearch(context) { client ->

            SearchRequestBuilder s = client.prepareSearch(context.index)            

            def query = new BoolQueryBuilder()

            if (countOnly) {
                s.setSearchType(SearchType.COUNT)
            }

            s.setTypes(context.returnType.simpleName)
             
            if (!countOnly) {
                s.setFetchSource(['*'] as String[], excludes as String[])
                 .setFrom(startingIndex as int)
                 .setSize(pageSize as int)



                sortOrder.each {
                    if (it instanceof String) {
                        s.addSort(it, SortOrder.ASC)
                    } else { // presume map
                        it.entrySet().each {
                            s.addSort(it.key, SortOrder.ASC.toString().equalsIgnoreCase(it.value)? SortOrder.ASC : SortOrder.DESC)
                        }
                    }
                        
                }
            }

            def bool = new BoolQueryBuilder()            
            queryParams.entrySet().each { entry -> 
                bool.must(entry.value
                               .annotation
                               .applyToQuery()
                               .newInstance(null, null)
                               .rehydrate(delegate, owner, thisObject)
                               .call(entry.key, entry.value.value, entry.value.annotation))
            }

            if (!ids.isEmpty()) {
                bool.must(idsQuery(context.returnType.simpleName).addIds(ids as String[]))
            }

            if (queryStrVal) {
                log.debug "Applying query_string ${queryStrVal}"
                bool.must(QueryBuilders.queryStringQuery(queryStrVal))
            }

			
			if (context.visorOpts['visor.highlight.disabled'] == null) {
				HighlightBuilder highlighter = s.highlightBuilder()
					
				highlighter.preTags('<strong>')
						   .postTags('</strong>')
						   .encoder('html')
						   //.fragmentSize(150)
						   //.numOfFragments(4)
						   //.forceSource(true)
						   .requireFieldMatch(false)
				highlights.each {
					log.debug "Adding highlighted field: ${it}"
					highlighter.field(it)
				}
				s.setHighlighterQuery(bool)
			 }

			//s.setExplain(true)			 
			s.setQuery(bool)
			 

            def filterClosure = context.filters.newInstance(null, this)         
			filterClosure.call(bool)

            stats.queryBuiltInstant = new Date().time

            ListenableActionFuture<SearchResponse> response = s.execute()

            response
        }
    }

    static def search(queryParam) {
        def context = ContextBuilder.build queryParam

        def stats = [:]
        stats.startInstant = new Date().time
        
        ListenableActionFuture<SearchResponse> esResult = doSearch(context, queryParam, stats)

        SearchResponse response = esResult.actionGet context.defaultTimeout
        log.trace "Search Response: ${response}"

        stats.responseInstant = new Date().time

        def results = new Expando()
        results.response = response

        results.list = Marshaller.unmarshallAll(response.hits, context)

        stats.unmarshallInstant = new Date().time

        results.count = response.hits.totalHits()
        results.pageSize = results.list.size()
        results.query = queryParam

        log.debug "Search matched ${results.count} hits, returned ${results.pageSize}"

        // TODO: probably don't need an expando here.
        results.stats = assembleStats(response, stats)

        log.debug "Search Execution Stats: ${results.stats}"
        results
    }

    static def count(queryParam) {
        def context = ContextBuilder.build queryParam

        def stats = [:]
        stats.startInstant = new Date().time
        
        def esResult = doSearch(context, queryParam, stats, true)

        def response = esResult.actionGet context.defaultTimeout
        log.debug "Search Response: ${response}"

        stats.responseInstant = new Date().time

        def results = new Expando()
        results.response = response
        results.count = response.getHits().totalHits()

        results
    }

    static def assembleStats(response, stats) {
        if (stats) {
            if (response) {
                stats.engineTook = response.tookInMillis
            }
            stats.detailTime = [:]

            stats.detailTime.assemblyDone = (stats.assemblyDoneInstant?:0) - stats.startInstant?:0
            stats.detailTime.queryBuilt = (stats.queryBuiltInstant?:0) - stats.assemblyDoneInstant?:0
            stats.detailTime.response = (stats.responseInstant?:0) - stats.queryBuiltInstant?:0
            stats.detailTime.unmarshall = (stats.unmarshallInstant?:0) - stats.responseInstant?:0
            stats.detailTime.statsDone = new Date().time - stats.unmarshallInstant?:0
            stats.detailTime.overall = new Date().time - stats.startInstant?:0
        }
        stats
    }

    static def index = { target -> 
        def context = ContextBuilder.build(target)

        def indexParams = Marshaller.marshall(target, 'INDEX')

        def targetId = Marshaller.getIdValueFromBean target
        if (!targetId) {
            throw new IllegalArgumentException('Bean must have populated Id-annotated field to be stored in search index.')
        }

        log.info "Indexing to $context.index id $targetId"
        log.debug "Indexed values: $indexParams"
        Engine.doInElasticSearch(context) { Client client -> 
            IndexRequestBuilder request = client.prepareIndex(context.index, context.returnType.simpleName, targetId)
			request.setSource(indexParams)
            
			ListenableActionFuture<IndexResponse> future = request.execute()
			
			IndexResponse result = future.actionGet()
            result
        }

    }

    static def delete = { target ->
        def context = ContextBuilder.build target 
        def idValue = Marshaller.getIdValueFromBean target
        log.info "Deleting $context.index id $idValue"
        if (idValue) {
            Engine.doInElasticSearch(context) { Client client ->
				ListenableActionFuture<DeleteResponse> future = client.prepareDelete(context.index, context.returnType.simpleName, idValue).execute()

				DeleteResponse result = future.get()
                result 
            }            
        }
    }

    static def health = { target ->
        def context = ContextBuilder.build target
        return Engine.doInElasticSearch(context) { Client client ->
            // gotta dig down for the admin client.
            def adminClient = client.admin()

            def future = adminClient.cluster().prepareHealth().execute()

            return future.actionGet()
        }
    }

}