package com.brandonsramirez.todoApi;

import java.util.List;

/**
 * Persistence operations that can be made regarding tasks.
 *
 * @author Brandon Ramirez
 */
public interface TaskDao {
  public PaginatedSearchResults<Task> listTasks(int offset, int max);
  public Task getTask(String taskId);
  public String createTask(Task task) throws DuplicateTaskException;
  public void updateTask(Task task);
  public void deleteTask(String taskId);
}
