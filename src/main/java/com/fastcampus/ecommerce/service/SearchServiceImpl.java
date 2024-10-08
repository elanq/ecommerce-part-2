package com.fastcampus.ecommerce.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.NestedQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.json.JsonData;
import com.fastcampus.ecommerce.model.ProductDocument;
import com.fastcampus.ecommerce.model.ProductResponse;
import com.fastcampus.ecommerce.model.ProductSearchRequest;
import com.fastcampus.ecommerce.model.SearchResponse;
import com.fastcampus.ecommerce.model.SearchResponse.FacetEntry;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

  private final ElasticsearchClient elasticsearchClient;
  private final ProductIndexService productIndexService;
  private final ProductService productService;

  @Override
  public SearchResponse<ProductResponse> search(ProductSearchRequest searchRequest) {
    BoolQuery.Builder boolQuery = new BoolQuery.Builder();

    // full text search on name description
    if (searchRequest.getQuery() != null && !searchRequest.getQuery().isEmpty()) {
      boolQuery.must(MultiMatchQuery.of(mm ->
              mm.fields("name", "description")
                  .query(searchRequest.getQuery()))
          ._toQuery());
    }

    // category filter
    if (searchRequest.getCategory() != null && !searchRequest.getCategory().isEmpty()) {
      Query nestedQuery = NestedQuery.of(n ->
          n.path("categories")
              .query(q ->
                  q.term(t ->
                      t.field("categories.name.keyword")
                          .value(searchRequest.getCategory()))))._toQuery();
      boolQuery.filter(nestedQuery);
    }

    // price range filter
    if (searchRequest.getMinPrice() != null || searchRequest.getMaxPrice() != null) {
      RangeQuery.Builder rangeQuery = new RangeQuery.Builder().field("price");
      if (searchRequest.getMinPrice() != null) {
        rangeQuery.gte(JsonData.of(searchRequest.getMinPrice()));
      }
      if (searchRequest.getMaxPrice() != null) {
        rangeQuery.lte(JsonData.of(searchRequest.getMaxPrice()));
      }
      boolQuery.filter(rangeQuery.build()._toQuery());
    }

    SearchRequest.Builder requestBuilder = new SearchRequest.Builder()
        .index(productIndexService.indexName())
        .query(boolQuery.build()._toQuery());

    // add sorting
    requestBuilder.sort(s ->
        s.field(f ->
            f.field(searchRequest.getSortBy())
                .order(
                    "asc".equals(searchRequest.getSortOrder()) ? SortOrder.Asc : SortOrder.Desc)));

    // add pagination
    requestBuilder.from((searchRequest.getPage() - 1) * searchRequest.getSize())
        .size(searchRequest.getSize());

    // aggregations
    requestBuilder.aggregations("categories", a ->
            a.nested(n ->
                    n.path("categories"))
                .aggregations("category_names", sa ->
                    sa.terms(t ->
                        t.field("categories.name.keyword"))))
        .from(searchRequest.getPage() - 1);

    SearchRequest elasticRequest = requestBuilder.build();
    SearchResponse<ProductResponse> response = new SearchResponse<>();
    try {
      co.elastic.clients.elasticsearch.core.SearchResponse<ProductDocument> results = elasticsearchClient.search(
          elasticRequest, ProductDocument.class);
      List<ProductResponse> productResponses = results.hits().hits()
          .stream()
          .filter(productDocumentHit ->
              productDocumentHit != null && productDocumentHit.id() != null)
          .map(productDocumentHit -> Long.parseLong(productDocumentHit.id()))
          .map(productService::findById)
          .toList();

      response.setData(productResponses);
      if (results.hits().total() != null) {
        response.setTotalHits(results.hits().total().value());
      }

      if (results.aggregations() != null) {
        Map<String, List<FacetEntry>> facets = new HashMap<>();
        var categoriesAgg = results.aggregations().get("categories");
        if (categoriesAgg != null && categoriesAgg.nested() != null) {
          var categoryNamesAgg = categoriesAgg.nested().aggregations().get("category_names");
          if (categoryNamesAgg != null && categoryNamesAgg.sterms() != null) {
            List<FacetEntry> categoryFacets = categoryNamesAgg.sterms().buckets().array()
                .stream()
                .map(bucket -> new FacetEntry(bucket.key().stringValue(), bucket.docCount()))
                .toList();
            facets.put("categories", categoryFacets);

          }
        }
        response.setFacets(facets);
      }
    } catch (IOException e) {
      log.error("Error while performing search. error message: {}", e.getMessage());
      throw new RuntimeException(e);
    }

    return response;
  }
}
