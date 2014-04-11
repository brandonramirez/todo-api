package com.brandonsramirez.todoApi.dal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.brandonsramirez.todoApi.DuplicateTaskException;
import com.brandonsramirez.todoApi.PaginatedSearchResults;
import com.brandonsramirez.todoApi.Task;
import com.brandonsramirez.todoApi.TaskDao;

public class InMemoryTaskDao implements TaskDao {
  private Map<String, Task> store = new HashMap<String, Task>();

  @Override
  public PaginatedSearchResults<Task> listTasks(int offset, int max) {
    List<Task> tasks = new ArrayList<Task>(store.values());
    Collections.sort(tasks, new Comparator<Task>() {
      public int compare(Task t1, Task t2) {
        return t1.getTitle().compareTo(t2.getTitle());
      }
    });
    return new PaginatedSearchResults<Task>(store.size(), tasks.subList(Math.min(offset, store.size()), Math.min(offset + max, store.size())));
  }

  @Override
  public Task getTask(String taskId) {
    Task t = store.get(taskId);
    if (t == null) {
      return null;
    }
    else {
      Task clone = new Task();
      clone.setTaskId(t.getTaskId());
      clone.setTitle(t.getTitle());
      clone.setBody(t.getBody());
      clone.setDone(t.isDone());
      return clone;
    }
  }

  @Override
  public String createTask(Task task) throws DuplicateTaskException {
    if (task.getTaskId() == null) {
      // Generally speaking, I don't like methods that modify their own inputs.
      // In this case, it's justified because we are acting as a mock database.
      // This line basically assigns a PK similar to a DBMS would do.
      task.setTaskId("" + task.getTitle().hashCode());
    }
    if (store.containsKey(task.getTaskId())) {
      throw new DuplicateTaskException();
    }
    for (Task existingTask : store.values()) {
      if (existingTask.getTitle().equals(task.getTitle())) {
        throw new DuplicateTaskException();
      }
    }
    store.put(task.getTaskId(), task);
    return task.getTaskId();
  }

  @Override
  public void updateTask(Task task) {
    Task persisted = store.get(task.getTaskId());
    if (persisted == null) {
      return;
    }

    persisted.setTitle(task.getTitle());
    persisted.setBody(task.getBody());
    persisted.setDone(task.isDone());
  }

  @Override
  public void deleteTask(String taskId) {
    store.remove(taskId);
  }

  public void clear() {
    store.clear();
  }
}
