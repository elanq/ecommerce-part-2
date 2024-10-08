package com.fastcampus.ecommerce.model;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchResponse<T> {

  private List<T> data;
  private long totalHits;
  private Map<String, List<FacetEntry>> facets;

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class FacetEntry {

    private String key;
    private Long docCount;
  }
}
