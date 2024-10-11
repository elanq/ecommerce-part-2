package com.fastcampus.ecommerce.service;


import java.util.List;

public interface CachedProductAutocompleteService {

  List<String> getAutocomplete(String query);

  List<String> getNgramAutocomplete(String query);

  List<String> getFuzzyAutocomplete(String query);

  List<String> combinedAutocomplete(String query);

}
