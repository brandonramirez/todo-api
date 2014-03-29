package com.brandonsramirez.todoApi;

import java.util.List;

public class TaskManagementService {
  private static DaoFactory daoFactory;

  /**
   * Configure the DAO to use for persistent storage
   *
   * This is package-protected intentionally.  Please don't change it.
   */
  static void setDaoFactory(DaoFactory taskDaoFactory) {
    daoFactory = taskDaoFactory;
  }

  public static PaginatedSearchResults<Task> listTasks(int offset, int max) {
    return daoFactory.createTaskDao().listTasks(offset, max);
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
    return daoFactory.createTaskDao().createTask(task);
    // @todo search index
  }

  public static void updateTask(Task task) {
    daoFactory.createTaskDao().updateTask(task);
    // @todo search index
  }

  public static void deleteTask(Task task) {
    deleteTask(task.getTaskId());
  }

  public static void deleteTask(String taskId) {
    daoFactory.createTaskDao().deleteTask(taskId);
    // @todo Delete from search index
  }
}
