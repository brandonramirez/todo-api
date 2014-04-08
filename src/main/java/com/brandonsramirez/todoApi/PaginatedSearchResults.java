package com.brandonsramirez.todoApi;

import java.io.Serializable;
import java.util.List;

public class PaginatedSearchResults<T> implements Serializable {
  private int totalCount;
  private List<T> results;

  public PaginatedSearchResults(int totalCount, List<T> results) {
    setTotalCount(totalCount);
    setResults(results);
  }

  public int getTotalCount() {
    return this.totalCount;
  }

  public void setTotalCount(int totalCount) {
    this.totalCount = totalCount;
  }

  public List<T> getResults() {
    return this.results;
  }

  public void setResults(List<T> results) {
    this.results = results;
  }
}
