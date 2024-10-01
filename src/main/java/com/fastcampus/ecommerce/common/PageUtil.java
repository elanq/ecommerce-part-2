package com.fastcampus.ecommerce.common;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

public class PageUtil {

  public static List<Order> parseSortOrderRequest(String[] sort) {
    List<Sort.Order> orders = new ArrayList<>();
    if (sort[0].contains(",")) {
      for (String sortOrder : sort) {
        // ?sort=product_id,asc&sort=price,desc
        String[] _sort = sortOrder.split(",");
        orders.add(new Sort.Order(getSortDirection(_sort[1]), _sort[0]));
      }
    } else {
      orders.add(new Sort.Order(getSortDirection(sort[1]), sort[0]));
    }
    return orders;
  }

  private static Sort.Direction getSortDirection(String direction) {
    if (direction.equals("asc")) {
      return Direction.ASC;
    } else if (direction.equals("desc")) {
      return Direction.DESC;
    }
    return Direction.ASC;
  }
}
