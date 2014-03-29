package com.brandonsramirez.todoApi;

/**
 * Define SearchProvider as an interface so that the API may continue to operate if
 * the remote search provider is unavailable.  This way, search can be stubbed out.
 */
public interface SearchProvider {
  public void addToIndex(Task task);
  public void updateInIndex(Task task);
  public void removeFromIndex(String taskId);
  public PaginatedSearchResults<Task> search(String query, int offset, int max);
}