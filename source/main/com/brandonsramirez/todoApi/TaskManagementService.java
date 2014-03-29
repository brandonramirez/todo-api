package com.brandonsramirez.todoApi;

import java.util.List;

public class TaskManagementService {
  private static DaoFactory daoFactory;
  private static SearchProvider searchProvider;

  /**
   * Configure the DAO to use for persistent storage
   *
   * This is package-protected intentionally.  Please don't change it.
   */
  static void setDaoFactory(DaoFactory taskDaoFactory) {
    daoFactory = taskDaoFactory;
  }

  static void setSearchProvider(SearchProvider providerToUse) {
    searchProvider = providerToUse;
  }

  public static PaginatedSearchResults<Task> listTasks(int offset, int max) {
    return daoFactory.createTaskDao().listTasks(offset, max);
  }

  public static PaginatedSearchResults<Task> search(String query, int offset, int max) {
    return searchProvider.search(query, offset, max);
  }

  public static Task getTask(String taskId) {
    return daoFactory.createTaskDao().getTask(taskId);
  }

  /**
   * Persist a new task.  The persistent identifier will be added to the object
   * if not already present.
   *
   * @param task Metadata of proposed new object
   * @return Unique identifier for persistent storage
   * @throws DuplicateTaskException If a task already exists with the same title.
   */
  public static String createTask(Task task) throws DuplicateTaskException {
    String id = daoFactory.createTaskDao().createTask(task);
    searchProvider.addToIndex(task);
    return id;
  }

  public static void updateTask(Task task) {
    daoFactory.createTaskDao().updateTask(task);
    searchProvider.updateInIndex(task);
  }

  public static void deleteTask(Task task) {
    deleteTask(task.getTaskId());
  }

  public static void deleteTask(String taskId) {
    daoFactory.createTaskDao().deleteTask(taskId);
    searchProvider.removeFromIndex(taskId);
  }
}
