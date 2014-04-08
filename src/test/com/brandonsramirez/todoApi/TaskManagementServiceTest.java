package com.brandonsramirez.todoApi;

import java.util.ArrayList;

import com.brandonsramirez.todoApi.dal.InMemoryTaskDaoFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TaskManagementServiceTest {
  private TaskManagementService service;

  @Before
  public void setUp() {
    service = new TaskManagementService();
    service.setDaoFactory(new InMemoryTaskDaoFactory());
    service.setSearchProvider(new StubSearchProvider());
    service.setSmsNotifier(new StubSmsNotifier());
  }

  @After
  public void tearDown() {
    // Nothing to do at this time, but subject to change of course.
  }

  @Test
  public void testCreateTask() {
    Task t = new Task();
    t.setTitle("test");
    t.setBody("testing testing 1 2 3");
    try {
      service.createTask(t);
    }
    catch (DuplicateTaskException e) {
      fail("Creating a task for the first time threw a DuplicateTaskException.");
    }

    // we implicitly test that the operation "succeeded" by virtue of no exceptions being thrown.

    assertNotNull(t.getTaskId());
  }

  @Test
  public void testDuplicateTasks() {
    Task t1 = new Task();
    t1.setTitle("test");
    t1.setBody("testing testing 1 2 3");
    try {
      service.createTask(t1);
    }
    catch (DuplicateTaskException e) {
      fail("Creating a task for the first time threw a DuplicateTaskException.");
    }

    Task t2 = new Task();
    t2.setTitle("test");
    t2.setBody("this is a dup");

    try {
      service.createTask(t2);
      fail("Creating a duplicate task did not result in a DuplicateTaskException being thrown.");
    }
    catch (DuplicateTaskException e) {
      // test passes
    }
  }

  @Test
  public void testListTasks() throws DuplicateTaskException {
    Task t1 = new Task();
    t1.setTitle("go to the store");
    service.createTask(t1);

    Task t2 = new Task();
    t2.setTitle("pick up some milk");
    service.createTask(t2);

    Task t3 = new Task();
    t3.setTitle("find a checkout isle");
    service.createTask(t3);

    Task t4 = new Task();
    t4.setTitle("pay cashier");
    service.createTask(t4);

    PaginatedSearchResults<Task> tasks = service.listTasks(0 /* offset */, 10 /* max */);
    assertEquals(tasks.getTotalCount(), 4);
    assertEquals(tasks.getResults().size(), 4);

    tasks = service.listTasks(0, 2);
    assertEquals(tasks.getTotalCount(), 4);
    assertEquals(tasks.getResults().size(), 2);

    tasks = service.listTasks(2, 2);
    assertEquals(tasks.getTotalCount(), 4);
    assertEquals(tasks.getResults().size(), 2);

    tasks = service.listTasks(4, 2);
    assertEquals(tasks.getTotalCount(), 4);
    assertEquals(tasks.getResults().size(), 0);
  }

  @Test
  public void testUpdateNonExistentTask() {
    Task task = new Task();
    task.setTaskId("blah");
    task.setTitle("something that does not yet exist");

    try {
      service.updateTask(task);
      fail("Updating a task which does not already exist failed to report an error.");
    }
    catch (UnknownTaskException e) {
      // test passes
    }
  }

  @Test
  public void testSmsNotification() throws DuplicateTaskException, UnknownTaskException {
    final BooleanHolder notificationTriggered = new BooleanHolder(false);

    service.setSmsNotifier(new SmsNotifier() {
      @Override
      public void notifyUserOfTaskCompletion(Task task) {
        notificationTriggered.val = true;
      }
    });

    Task t1 = new Task();
    t1.setTitle("some urgent stuff I need to do");
    t1.setBody("it's a secret so I can't tell you");
    t1.setDone(false);
    service.createTask(t1);

    Task t2 = new Task();
    t2.setTaskId(t1.getTaskId());
    t2.setTitle("some urgent stuff I need to do");
    t2.setBody("it's a secret so I can't tell you");
    t2.setDone(true);
    service.updateTask(t2);

    assertTrue("Notification was never triggered when marking a task complete.", notificationTriggered.val);
  }

  @Test
  public void verifySearchIndexingTriggered() throws DuplicateTaskException, UnknownTaskException {
    final BooleanHolder addedToIndex = new BooleanHolder(false);
    final BooleanHolder updatedIndex = new BooleanHolder(false);
    final BooleanHolder removedFromIndex = new BooleanHolder(false);

    service.setSearchProvider(new SearchProvider() {
      public void addToIndex(Task task) {
        addedToIndex.val = true;
      }
      public void updateInIndex(Task task) {
        updatedIndex.val = true;
      }
      public void removeFromIndex(String taskId) {
        removedFromIndex.val = true;
      }
      public PaginatedSearchResults<Task> search(String query, int offset, int max) {
        return new PaginatedSearchResults<Task>(0, new ArrayList<Task>(0));
      }
    });

    // Baseline sanity checks
    assertFalse(addedToIndex.val);
    assertFalse(updatedIndex.val);
    assertFalse(removedFromIndex.val);

    Task task = new Task();
    task.setTitle("my first task");
    task.setBody("");
    task.setDone(false);
    service.createTask(task);

    assertTrue("When creating a task, it was not pushed to the search index.", addedToIndex.val);

    Task updated = new Task();
    updated.setTaskId(task.getTaskId());
    updated.setTitle(task.getTitle());
    updated.setBody("valuable information");
    updated.setDone(false);
    service.updateTask(updated);

    assertTrue("After updating an existing task, the call to update the search index was not made.", updatedIndex.val);

    service.deleteTask(task);

    assertTrue("Deleting a task did not result in a call to remove it from the search index.", removedFromIndex.val);
  }

  /**
   * Work-around for inner classes only being able to see final local variables from outer classes.
   */
  private static class BooleanHolder {
    boolean val;

    BooleanHolder() {
      this(false);
    }
    BooleanHolder(boolean val) {
      this.val = val;
    }
  }
}
