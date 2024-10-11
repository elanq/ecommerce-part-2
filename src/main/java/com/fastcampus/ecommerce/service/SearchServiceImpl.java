package com.fastcampus.ecommerce.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.ChildScoreMode;
import co.elastic.clients.elasticsearch._types.query_dsl.FieldValueFactorModifier;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScore;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreMode;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Like;
import co.elastic.clients.elasticsearch._types.query_dsl.MoreLikeThisQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.NestedQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggestOption;
import co.elastic.clients.json.JsonData;
import com.fastcampus.ecommerce.entity.UserActivity;
import com.fastcampus.ecommerce.model.ActivityType;
import com.fastcampus.ecommerce.model.CategoryResponse;
import com.fastcampus.ecommerce.model.ProductDocument;
import com.fastcampus.ecommerce.model.ProductResponse;
import com.fastcampus.ecommerce.model.ProductSearchRequest;
import com.fastcampus.ecommerce.model.SearchResponse;
import com.fastcampus.ecommerce.model.SearchResponse.FacetEntry;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
  private final UserActivityService userActivityService;

  private final int SIMILAR_PRODUCT_COUNT = 10;
  private final int USER_RECOMMENDATION_LIMIT = 10;

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

    FunctionScoreQuery functionScoreQuery = FunctionScoreQuery.of(f ->
        f.query(q -> q.bool(boolQuery.build()))
            .functions(
                FunctionScore.of(fs ->
                    fs.fieldValueFactor(fvf ->
                        fvf.field("viewCount")
                            .factor(1.0)
                            .modifier(FieldValueFactorModifier.Log1p))),
                FunctionScore.of(fs ->
                    fs.fieldValueFactor(fvf ->
                        fvf.field("purchaseCount")
                            .factor(2.0)
                            .modifier(FieldValueFactorModifier.Log1p)))
            )
            .boostMode(FunctionBoostMode.Multiply)
            .scoreMode(FunctionScoreMode.Sum));

    SearchRequest.Builder requestBuilder = new SearchRequest.Builder()
        .index(productIndexService.indexName())
        .query(functionScoreQuery._toQuery());

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
    SearchResponse<ProductResponse> response;
    try {
      co.elastic.clients.elasticsearch.core.SearchResponse<ProductDocument> results = elasticsearchClient.search(
          elasticRequest, ProductDocument.class);
      response = mapSearchResults(results);
    } catch (IOException e) {
      log.error("Error while performing search. error message: {}", e.getMessage());
      throw new RuntimeException(e);
    }

    return response;
  }

  @Override
  public SearchResponse<ProductResponse> similarProducts(Long productId) {
    ProductResponse sourceProduct = productService.findById(productId);

    List<String> categoryNames = sourceProduct.getCategories()
        .stream()
        .map(CategoryResponse::getName)
        .toList();

    MoreLikeThisQuery moreLikeThisQuery = MoreLikeThisQuery.of(m ->
        m.fields("name", "description")
            .like(l ->
                l.document(d ->
                    d.index(productIndexService.indexName())
                        .id(productId.toString())))
            .minTermFreq(1)
            .maxQueryTerms(12)
            .minDocFreq(1));

    List<FieldValue> categoryNameFieldValues = categoryNames.stream().map(FieldValue::of).toList();
    NestedQuery nestedQuery = NestedQuery.of(n ->
        n.path("categories")
            .query(q ->
                q.terms(t ->
                    t.field("categories.name")
                        .terms(t2 -> t2.value(categoryNameFieldValues))))
            .scoreMode(ChildScoreMode.Avg));

    BoolQuery boolQuery = BoolQuery.of(b ->
        b.must(m -> m.moreLikeThis(moreLikeThisQuery))
            .should(s -> s.nested(nestedQuery)));

    FunctionScoreQuery functionScoreQuery = FunctionScoreQuery.of(f ->
        f.query(q -> q.bool(boolQuery))
            .functions(
                FunctionScore.of(fs ->
                    fs.fieldValueFactor(fvf ->
                        fvf.field("viewCount")
                            .factor(1.0)
                            .modifier(FieldValueFactorModifier.Log1p))),
                FunctionScore.of(fs ->
                    fs.fieldValueFactor(fvf ->
                        fvf.field("purchaseCount")
                            .factor(2.0)
                            .modifier(FieldValueFactorModifier.Log1p)))
            )
            .boostMode(FunctionBoostMode.Multiply)
            .scoreMode(FunctionScoreMode.Sum));

    try {
      co.elastic.clients.elasticsearch.core.SearchResponse<ProductDocument> results = elasticsearchClient.search(
          s ->
              s.index(productIndexService.indexName())
                  .query(q -> q.functionScore(functionScoreQuery))
                  .size(SIMILAR_PRODUCT_COUNT)
          , ProductDocument.class);

      return mapSearchResults(results);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  @Override
  public SearchResponse<ProductResponse> userRecommendation(Long userId,
      ActivityType activityType) {
    if (!List.of(ActivityType.PURCHASE, ActivityType.VIEW).contains(activityType)) {
      SearchResponse<ProductResponse> response = new SearchResponse<>();
      response.setFacets(Map.of());
      response.setData(List.of());
      response.setTotalHits(0);
      return response;
    }

    List<UserActivity> userActivities;
    if (activityType.equals(ActivityType.PURCHASE)) {
      userActivities = userActivityService.getLastMonthUserPurchase(userId);
    } else {
      userActivities = userActivityService.getLastMonthUserView(userId);
    }
    List<Long> topProductIds = getTopProductIds(userActivities);
    return productRecommendationOnActivityType(topProductIds, activityType);
  }

  @Override
  public List<String> getAutocomplete(String query) {
    co.elastic.clients.elasticsearch.core.SearchResponse<Void> response;

    try {
      response = elasticsearchClient.search(s ->
              s.index(productIndexService.indexName())
                  .suggest(su ->
                      su.suggesters("name_suggest", fs ->
                          fs.prefix(query)
                              .completion(cs ->
                                  cs.field("nameSuggest")
                                      .skipDuplicates(true)
                                      .size(3)
                              )
                      ))
          , Void.class);

      return response.suggest().get("name_suggest")
          .stream()
          .flatMap(s -> s.completion().options().stream())
          .map(CompletionSuggestOption::text)
          .toList();

    } catch (IOException e) {
      log.error("Error during autocomplete. error message {}", e.getMessage());
      return List.of();
    }
  }

  @Override
  public List<String> getNgramAutocomplete(String query) {
    co.elastic.clients.elasticsearch.core.SearchResponse<ProductDocument> response;

    try {
      response = elasticsearchClient.search(s ->
              s.index(productIndexService.indexName())
                  .query(q ->
                      q.match(m ->
                          m.field("nameNgram")
                              .query(query)
                              .analyzer("ngram_analyzer")
                      )
                  )
                  .size(3)
          , ProductDocument.class);

      return response.hits().hits()
          .stream()
          .map(hit -> hit.source().getName())
          .toList();

    } catch (IOException e) {
      log.error("Error during autocomplete. error message {}", e.getMessage());
      return List.of();
    }
  }

  @Override
  public List<String> getFuzzyAutocomplete(String query) {
    co.elastic.clients.elasticsearch.core.SearchResponse<ProductDocument> response;

    try {
      response = elasticsearchClient.search(s ->
              s.index(productIndexService.indexName())
                  .query(q ->
                      q.fuzzy(f ->
                          f.field("name")
                              .value(query)
                              .fuzziness("AUTO")
                      )
                  )
                  .size(3)
          , ProductDocument.class);

      return response.hits().hits()
          .stream()
          .map(hit -> hit.source().getName())
          .toList();

    } catch (IOException e) {
      log.error("Error during autocomplete. error message {}", e.getMessage());
      return List.of();
    }
  }

  @Override
  public List<String> combinedAutocomplete(String query) {
    List<String> results = new ArrayList<>();

    results.addAll(getAutocomplete(query));

    if (results.size() < 5) {
      results.addAll(getNgramAutocomplete(query));
    }

    if (results.size() < 5) {
      results.addAll(getFuzzyAutocomplete(query));
    }

    return results.stream()
        .distinct()
        .limit(5)
        .toList();
  }

  private SearchResponse<ProductResponse> productRecommendationOnActivityType(List<Long> productIds,
      ActivityType activityType) {
    List<Like> moreLikeThisQueries = productIds.stream()
        .map(productId ->
            Like.of(builder ->
                builder.text(String.valueOf(productId))))
        .toList();

    MoreLikeThisQuery moreLikeThisQuery = MoreLikeThisQuery.of(m ->
        m.fields("name", "description")
            .like(moreLikeThisQueries)
            .minTermFreq(1)
            .maxQueryTerms(12)
            .minDocFreq(1));

    String fieldName = activityType.equals(ActivityType.PURCHASE) ? "purchaseCount" : "viewCount";
    double factorValue = activityType.equals(ActivityType.PURCHASE) ? 2.0 : 1.0;

    FunctionScoreQuery functionScoreQuery = FunctionScoreQuery.of(f ->
        f.query(q -> q.moreLikeThis(moreLikeThisQuery))
            .functions(
                FunctionScore.of(fs ->
                    fs.fieldValueFactor(fvf ->
                        fvf.field(fieldName)
                            .factor(factorValue)
                            .modifier(FieldValueFactorModifier.Log1p))))
            .boostMode(FunctionBoostMode.Multiply)
            .scoreMode(FunctionScoreMode.Sum));

    co.elastic.clients.elasticsearch.core.SearchResponse<ProductDocument> response;

    try {
      response = elasticsearchClient.search(s -> s
              .index(productIndexService.indexName())
              .query(q -> q.functionScore(functionScoreQuery))
              .size(USER_RECOMMENDATION_LIMIT),
          ProductDocument.class
      );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return mapSearchResults(response);
  }

  private List<Long> getTopProductIds(List<UserActivity> activities) {
    return activities.stream()
        .collect(Collectors.groupingBy(UserActivity::getProductId, Collectors.counting()))
        .entrySet().stream()
        .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
        .limit(5)
        .map(Map.Entry::getKey)
        .toList();
  }

  private SearchResponse<ProductResponse> mapSearchResults(
      co.elastic.clients.elasticsearch.core.SearchResponse<ProductDocument> results) {
    List<ProductResponse> productResponses = results.hits().hits()
        .stream()
        .filter(productDocumentHit ->
            productDocumentHit != null && productDocumentHit.id() != null)
        .map(productDocumentHit -> Long.parseLong(productDocumentHit.id()))
        .map(productService::findById)
        .toList();

    SearchResponse<ProductResponse> response = new SearchResponse<>();
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

    return response;
  }
}
