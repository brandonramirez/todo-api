package com.brandonsramirez.todoApi;

public class TaskManagementService {
  private DaoFactory daoFactory;
  private SearchProvider searchProvider;
  private SmsNotifier smsNotifier;

  /**
   * Configure the DAO to use for persistent storage
   *
   * This is package-protected intentionally.  Please don't change it.
   */
  void setDaoFactory(DaoFactory taskDaoFactory) {
    daoFactory = taskDaoFactory;
  }

  void setSearchProvider(SearchProvider providerToUse) {
    searchProvider = providerToUse;
  }

  void setSmsNotifier(SmsNotifier notifierToUse) {
    smsNotifier = notifierToUse;
  }

  public PaginatedSearchResults<Task> listTasks(int offset, int max) {
    return daoFactory.createTaskDao().listTasks(offset, max);
  }

  public PaginatedSearchResults<Task> search(String query, int offset, int max) {
    return searchProvider.search(query, offset, max);
  }

  public Task getTask(String taskId) {
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
  public String createTask(Task task) throws DuplicateTaskException {
    String id = daoFactory.createTaskDao().createTask(task);
    searchProvider.addToIndex(task);
    return id;
  }

  public void updateTask(Task task) throws UnknownTaskException {
    Task existingTask = getTask(task.getTaskId());
    if (existingTask == null) {
      throw new UnknownTaskException();
    }
    boolean transitionToComplete = !existingTask.isDone() && task.isDone();
    daoFactory.createTaskDao().updateTask(task);
    try {
      searchProvider.updateInIndex(task);
    }
    finally {
      if (transitionToComplete) {
        onTaskCompletion(task);
      }
    }
  }

  public void deleteTask(Task task) {
    deleteTask(task.getTaskId());
  }

  public void deleteTask(String taskId) {
    daoFactory.createTaskDao().deleteTask(taskId);
    searchProvider.removeFromIndex(taskId);
  }

  private void onTaskCompletion(Task task) {
    smsNotifier.notifyUserOfTaskCompletion(task);
  }
}
