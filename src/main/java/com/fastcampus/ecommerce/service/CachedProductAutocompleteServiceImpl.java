package com.fastcampus.ecommerce.service;

import com.fasterxml.jackson.core.type.TypeReference;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CachedProductAutocompleteServiceImpl implements
    CachedProductAutocompleteService {

  private final SearchService searchService;
  private final CacheService cacheService;

  @Value("${suggestion.cache.ttl}")
  private Duration ttl;

  @Override
  public List<String> getAutocomplete(String query) {
    String cacheKey = "product:suggestions:" + query;
    return cacheService.get(cacheKey, new TypeReference<List<String>>() {
        })
        .orElseGet(() -> {
          List<String> autocompletes = searchService.getAutocomplete(query);
          cacheService.put(cacheKey, autocompletes, ttl);
          return autocompletes;
        });
  }

  @Override
  public List<String> getNgramAutocomplete(String query) {
    String cacheKey = "product:ngram:suggestions:" + query;
    return cacheService.get(cacheKey, new TypeReference<List<String>>() {
        })
        .orElseGet(() -> {
          List<String> autocompletes = searchService.getNgramAutocomplete(query);
          cacheService.put(cacheKey, autocompletes, ttl);
          return autocompletes;
        });
  }

  @Override
  public List<String> getFuzzyAutocomplete(String query) {
    String cacheKey = "product:fuzzy:suggestions:" + query;
    return cacheService.get(cacheKey, new TypeReference<List<String>>() {
        })
        .orElseGet(() -> {
          List<String> autocompletes = searchService.getFuzzyAutocomplete(query);
          cacheService.put(cacheKey, autocompletes, ttl);
          return autocompletes;
        });
  }

  @Override
  public List<String> combinedAutocomplete(String query) {
    String cacheKey = "product:combined:suggestions:" + query;
    return cacheService.get(cacheKey, new TypeReference<List<String>>() {
        })
        .orElseGet(() -> {
          List<String> autocompletes = searchService.combinedAutocomplete(query);
          cacheService.put(cacheKey, autocompletes, ttl);
          return autocompletes;
        });
  }
}
