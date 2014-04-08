package com.brandonsramirez.todoApi;

/**
 * Abstract factory for getting a handle to the persistence layer.
 *
 * This abstracts away the access to the persistence layer to make it easy to
 * swap in different versions at runtime, such as for unit testing.
 *
 * It also becomes a useful place to do configuration setup (like DB connection URL, etc.)
 *
 * @author Brandon Ramirez
 */
public interface DaoFactory {
  public TaskDao createTaskDao();
}
