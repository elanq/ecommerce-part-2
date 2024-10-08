package com.fastcampus.ecommerce.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import com.fastcampus.ecommerce.entity.Category;
import com.fastcampus.ecommerce.entity.Product;
import com.fastcampus.ecommerce.model.ProductDocument;
import io.github.resilience4j.retry.Retry;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class ProductIndexServiceImpl implements
    ProductIndexService {

  private static final String INDEX_NAME = "products";
  private final ElasticsearchClient elasticsearchClient;
  private final CategoryService categoryService;
  private final Retry elasticsearchIndexRetrier;

  @Override
  @Async
  public void reindexProduct(Product product) {
    List<Category> categoryList = categoryService.getProductCategories(product.getProductId());
    ProductDocument productDocument = ProductDocument.fromProductAndCategories(product,
        categoryList);

    IndexRequest<ProductDocument> request = IndexRequest.of(builder ->
        builder.index(INDEX_NAME)
            .id(String.valueOf(product.getProductId()))
            .document(productDocument));

    try {
      elasticsearchIndexRetrier.executeCallable(() -> {
        elasticsearchClient.index(request);
        return null;
      });
    } catch (IOException ex) {
      log.error("Error while reindex product with id " + product.getProductId() + " error: "
          + ex.getMessage());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }

  @Override
  @Async
  public void deleteProduct(Product product) {
    DeleteRequest deleteRequest = DeleteRequest.of(builder ->
        builder.index(indexName())
            .id(String.valueOf(product.getProductId())));

    try {
      elasticsearchIndexRetrier.executeCallable(() -> {
        elasticsearchClient.delete(deleteRequest);
        return null;
      });
    } catch (IOException ex) {
      log.error("Error while deleting product with id " + product.getProductId() + " error: "
          + ex.getMessage());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }

  @Override
  public String indexName() {
    return INDEX_NAME;
  }
}
