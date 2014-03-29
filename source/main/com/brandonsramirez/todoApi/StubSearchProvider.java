package com.brandonsramirez.todoApi;

import java.util.Collections;
import java.util.List;

public class StubSearchProvider implements SearchProvider {
  StubSearchProvider() {
  }

  public void addToIndex(Task task) {
    // intentionally left empty
  }

  public void updateInIndex(Task task) {
    // intentionally left empty
  }

  public void removeFromIndex(String taskId) {
    // intentionally left empty
  }

  public PaginatedSearchResults<Task> search(String query, int offset, int max) {
    List<Task> results = Collections.emptyList();
    return new PaginatedSearchResults<Task>(0, results);
  }
}