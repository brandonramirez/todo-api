package com.brandonsramirez.todoApi;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;

import com.brandonsramirez.rest.JsonPatchOperation;
import com.brandonsramirez.todoApi.dal.InMemoryTaskDao;
import com.brandonsramirez.todoApi.dal.InMemoryTaskDaoFactory;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.servlet.ServletRegistration;
import org.glassfish.grizzly.servlet.WebappContext;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TaskResourceTest {
  private static final int TEST_HTTP_PORT = Integer.parseInt(System.getProperty("test.http.port", "8080"));
  private static final String TEST_LISTEN_HOST = System.getProperty("test.listen.host", "localhost");
  public static final URI BASE_URI = UriBuilder.fromUri("http://" + TEST_LISTEN_HOST + "/").port(TEST_HTTP_PORT).build();

  private InMemoryTaskDaoFactory daoFactory = new InMemoryTaskDaoFactory();
  private StubSearchProvider searchProvider = new StubSearchProvider();
  private StubSmsNotifier smsNotifier = new StubSmsNotifier();

  private HttpServer server;
  private TaskManagementService service = new TaskManagementService();
  private WebTarget target;

  @Before
  public void setUp() throws IOException {
    service.setDaoFactory(daoFactory);
    service.setSearchProvider(searchProvider);
    service.setSmsNotifier(smsNotifier);

    WebappContext app = new WebappContext("todo-api", "/");
    ServletRegistration servletRegistration = app.addServlet("Jersey", org.glassfish.jersey.servlet.ServletContainer.class);
    servletRegistration.addMapping("/rest/*");
    servletRegistration.setInitParameter("jersey.config.server.provider.packages", "com.brandonsramirez.todoApi");
    servletRegistration.setInitParameter("javax.ws.rs.Application", "com.brandonsramirez.todoApi.ApiDeployment");

    app.setAttribute(ServiceLocator.CONTEXT_ATTRIBUTE_NAME, service);

    server = HttpServer.createSimpleServer(null, TEST_LISTEN_HOST, TEST_HTTP_PORT);
    app.deploy(server);
    server.start();

    Client c = ClientBuilder.newClient();
    //c.register(com.owlike.genson.ext.jaxrs.GensonJsonConverter.class);
    target = c.target(BASE_URI + app.getContextPath() + "rest").path("/todo");
  }

  @After
  public void tearDown() {
    if (daoFactory != null) {
      ((InMemoryTaskDao) daoFactory.createTaskDao()).clear();
    }

    if (server != null) {
      server.shutdownNow();
      server = null;
    }
  }

  @Test
  public void testCreate() {
    Task task = new Task();
    task.setTitle("urgent task");
    task.setBody("I really need to complete this task ASAP!");

    Response res = target.request(MediaType.APPLICATION_JSON).post(Entity.json(task));
    assertEquals("Creating a task did not return the appropriate success code.", 201, res.getStatus());
    assertTrue("Creating a task worked but did not return the task entity.", res.hasEntity());
    assertNotNull("Creating a task worked but did not return the location of the new entity.", res.getLocation());
  }

  @Test
  public void testGet() throws DuplicateTaskException {
    Task task = new Task();
    task.setTitle("urgent task");
    task.setBody("I really need to complete this task ASAP!");

    String taskId = ((InMemoryTaskDao) daoFactory.createTaskDao()).createTask(task);

    Task res = target.path("/" + taskId).request(MediaType.APPLICATION_JSON).get(Task.class);

    assertEquals(task.getTitle(), res.getTitle());
    assertEquals(task.getBody(),  res.getBody());
    assertEquals(taskId,          res.getTaskId());
    assertEquals(task.isDone(),   res.isDone());
  }

  @Test
  public void testCreateLocationRedirect() {
    Task task = new Task();
    task.setTitle("urgent task");
    task.setBody("I really need to complete this task ASAP!");

    URI loc = target.request(MediaType.APPLICATION_JSON).post(Entity.json(task)).getLocation();
    Task res = ClientBuilder.newClient().target(loc).request(MediaType.APPLICATION_JSON).get(Task.class);

    assertEquals(task.getTitle(), res.getTitle());
    assertEquals(task.getBody(),  res.getBody());
    assertEquals(task.isDone(),   res.isDone());
  }

  @Test
  public void testUpdate() {
    Task task = new Task();
    task.setTitle("urgent task");
    task.setBody("I really need to complete this task ASAP!");

    URI loc = target.request(MediaType.APPLICATION_JSON).post(Entity.json(task)).getLocation();

    task.setBody("My dog acts like a cat.");
    int statusCode = ClientBuilder.newClient().target(loc).request(MediaType.APPLICATION_JSON).put(Entity.json(task)).getStatus();
    assertEquals("Updating an existing task did not return the appropriate status code", Response.Status.NO_CONTENT.getStatusCode(), statusCode);

    Task res = ClientBuilder.newClient().target(loc).request(MediaType.APPLICATION_JSON).get(Task.class);
    assertEquals("Updated body does not take effect.", task.getBody(), res.getBody());
  }

  @Test
  public void markCompleteViaUpdate() {
    final BooleanHolder completionNoticeTriggered = new BooleanHolder(false);

    // Detect completion notifications without actually sending it.
    service.setSmsNotifier(new SmsNotifier() {
      @Override
      public void notifyUserOfTaskCompletion(Task task) {
        completionNoticeTriggered.val = true;
      }
    });

    // Create an incomplete todo item.
    Task task = new Task();
    task.setTitle("urgent task");
    task.setBody("I really need to complete this task ASAP!");
    task.setDone(false);

    URI loc = target.request(MediaType.APPLICATION_JSON).post(Entity.json(task)).getLocation();

    // Mark it complete and push the updated state back to the API.
    task.setDone(true);
    ClientBuilder.newClient().target(loc).request(MediaType.APPLICATION_JSON).put(Entity.json(task)).getStatus();

    assertTrue("Updating a previously incomplete task to completed did not trigger a notification.", completionNoticeTriggered.val);
  }

  @Test
  public void testDelete() throws DuplicateTaskException {
    Task task = new Task();
    task.setTitle("urgent task");
    task.setBody("I really need to complete this task ASAP!");

    String taskId = ((InMemoryTaskDao) daoFactory.createTaskDao()).createTask(task);
    Response.Status status = Response.Status.fromStatusCode(target.path("/" + taskId).request(MediaType.APPLICATION_JSON).delete().getStatus());

    assertEquals("Deleting an existing task returned something other than an HTTP 200 response", Response.Status.NO_CONTENT, status);
  }

  /**
   * Not enabled because the HTTP PATCH method is not supported by the Jersey HTTP client.  It throws a runtime error.
   */
  //@Test
  public void partialUpdateViaPatch() {
    Task task = new Task();
    task.setTitle("urgent task");
    task.setBody("I really need to complete this task ASAP!");

    URI loc = target.request(MediaType.APPLICATION_JSON).post(Entity.json(task)).getLocation();

    JsonPatchOperation titlePatch = new JsonPatchOperation();
    titlePatch.setOp("replace");
    titlePatch.setPath("/title");
    titlePatch.setValue("VERY urgent task");

    JsonPatchOperation bodyPatch = new JsonPatchOperation();
    bodyPatch.setOp("replace");
    bodyPatch.setPath("/body");
    bodyPatch.setValue("we need to get this done");

    Client c = ClientBuilder.newClient();
    c.property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true);

    c.target(loc).request(MediaType.APPLICATION_JSON).method("PATCH", Entity.json(Arrays.asList(titlePatch, bodyPatch))).getStatus();
  }
}
