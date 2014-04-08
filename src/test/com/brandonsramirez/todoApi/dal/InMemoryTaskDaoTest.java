package com.brandonsramirez.todoApi.dal;

import com.brandonsramirez.todoApi.DuplicateTaskException;
import com.brandonsramirez.todoApi.PaginatedSearchResults;
import com.brandonsramirez.todoApi.Task;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class InMemoryTaskDaoTest {
  private InMemoryTaskDao dao = new InMemoryTaskDao();

  @Before
  public void setUp() {
    Task t1 = new Task();
    t1.setTaskId("t1");
    t1.setTitle("Write a unit test");
    t1.setBody("Make sure the DAO methods are covered with test cases.");

    Task t2 = new Task();
    t2.setTaskId("t2");
    t2.setTitle("Run tests");
    t2.setBody("An initial run should be done");

    Task t3 = new Task();
    t3.setTaskId("t3");
    t3.setTitle("Verify failure");
    t3.setBody("Ensure the test fails");

    Task t4 = new Task();
    t4.setTaskId("t4");
    t4.setTitle("Write implementing code");
    t4.setBody("Implement the functionality under test");

    Task t5 = new Task();
    t5.setTaskId("t5");
    t5.setTitle("Run test");
    t5.setBody("Second run of the test cases");

    Task t6 = new Task();
    t6.setTaskId("t6");
    t6.setTitle("Verify success");
    t6.setBody("Tests should pass now.  Otherwise, we are sad.");

    try {
      dao.createTask(t1);
      dao.createTask(t2);
      dao.createTask(t3);
      dao.createTask(t4);
      dao.createTask(t5);
      dao.createTask(t6);
    }
    catch (DuplicateTaskException e) {
      // This is a unit test.  I don't know what we could do if creating a task as part of test setup failed in a new store.
      throw new RuntimeException(e);
    }
  }

  @After
  public void tearDown() {
    dao.clear();
  }

  @Test
  public void testListTasks() {
    PaginatedSearchResults<Task> tasks = dao.listTasks(0, 10);
    assertNotNull(tasks);
    assertSame(6, tasks.getTotalCount());
    assertSame(6, tasks.getResults().size());

    tasks = dao.listTasks(2, 2);
    assertNotNull(tasks);
    assertSame(6, tasks.getTotalCount());
    assertSame(2, tasks.getResults().size());
    assertEquals("t3", tasks.getResults().get(0).getTaskId());
  }

  @Test
  public void testGetTask() {
    Task task = dao.getTask("t1");
    assertNotNull("Previously created task missing", task);
    assertEquals("Title does not match when retrieved.", "Write a unit test", task.getTitle());
  }

  @Test
  public void testCreateTask() {
    Task t = new Task();
    t.setTaskId("hello");
    t.setTitle("did we save this successfully?");
    try {
      dao.createTask(t);
    }
    catch (DuplicateTaskException e) {
      fail("Failed to create a task that we know does not yet exist.");
    }
    
    t = dao.getTask("hello");
    assertNotNull(t);
  }

  @Test
  public void testUpdateTask() {
    Task t = dao.getTask("t3");
    t.setTitle("This is some other task.");
    dao.updateTask(t);
    assertEquals("A title update did not hold.", "This is some other task.", dao.getTask("t3").getTitle());
  }

  @Test
  public void testDeleteTask() {
    assertNotNull(dao.getTask("t5"));
    dao.deleteTask("t5");
    assertNull(dao.getTask("t5"));
  }
}
