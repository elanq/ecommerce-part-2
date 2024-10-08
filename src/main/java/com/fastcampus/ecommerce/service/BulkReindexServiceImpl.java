package com.fastcampus.ecommerce.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import com.fastcampus.ecommerce.entity.Category;
import com.fastcampus.ecommerce.entity.Product;
import com.fastcampus.ecommerce.model.ProductDocument;
import com.fastcampus.ecommerce.repository.CategoryRepository;
import com.fastcampus.ecommerce.repository.ProductCategoryRepository;
import com.fastcampus.ecommerce.repository.ProductRepository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Service
public class BulkReindexServiceImpl implements
    BulkReindexService {

  private final ElasticsearchClient elasticsearchClient;
  private final ProductCategoryRepository productCategoryRepository;
  private final CategoryRepository categoryRepository;
  private final ProductRepository productRepository;
  private final ProductIndexService productIndexService;

  private static final int BATCH_SIZE = 100;

  @Transactional(readOnly = true)
  @Override
  @Async
  public void reindexAllProducts() throws IOException {
    long startTime = System.currentTimeMillis();
    AtomicLong totalIndexed = new AtomicLong();
    List<ProductDocument> batch = new ArrayList<>(BATCH_SIZE);

    try (Stream<Product> products = productRepository.streamAll()) {
      products.forEach(product -> {
        List<Long> categoryIds = productCategoryRepository.findCategoriesByProductId(
                product.getProductId())
            .stream().map(productCategory -> productCategory.getId().getCategoryId()).toList();
        List<Category> categories = categoryRepository.findAllById(categoryIds);
        ProductDocument productDocument = ProductDocument.fromProductAndCategories(product,
            categories);
        batch.add(productDocument);

        if (batch.size() >= BATCH_SIZE) {
          try {
            totalIndexed.addAndGet(indexBatch(batch));
          } catch (IOException e) {
            log.error("Error while reindex. error message: {}", e.getMessage());
            throw new RuntimeException(e);
          }
          batch.clear();
        }
      });
    }

    if (!batch.isEmpty()) {
      totalIndexed.addAndGet(indexBatch(batch));
    }

    long endTime = System.currentTimeMillis();
    log.info("Reindex complete. Total documents indexed: {}. Time taken: {} ms", totalIndexed,
        (endTime - startTime));
  }

  private long indexBatch(List<ProductDocument> batch) throws IOException {
    BulkRequest.Builder builder = new BulkRequest.Builder();

    for (ProductDocument document : batch) {
      builder.operations(op ->
          op.update(upd ->
              upd.index(productIndexService.indexName())
                  .id(document.getId())
                  .action(act ->
                      act.docAsUpsert(true)
                          .doc(document))));
    }

    BulkResponse result = elasticsearchClient.bulk(builder.build());

    if (result.errors()) {
      log.error("Error while performing bulk operations");
      for (BulkResponseItem item : result.items()) {
        if (item.error() != null) {
          log.error(item.error().reason());
        }
      }
    }

    return batch.size();
  }
}
