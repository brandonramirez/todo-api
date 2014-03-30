package com.brandonsramirez.todoApi;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.brandonsramirez.rest.PATCH;
import com.brandonsramirez.rest.JsonPatchOperation;

@Path("/todo")
@Consumes("application/json")
@Produces("application/json")
public class TaskResource {
  @Context private UriInfo uriInfo;

  @GET
  public Response listTasks(@DefaultValue("0")  @QueryParam("offset") int offset,
                            @DefaultValue("10") @QueryParam("max") int max,
                            @DefaultValue("")   @QueryParam("q") String query)
  {
    if ("".equals(query)) {
      return Response.ok().entity(restified(TaskManagementService.listTasks(offset, max))).build();
    }
    else {
      return Response.ok().entity(restified(TaskManagementService.search(query, offset, max))).build();
    }
  }

  @GET
  @Path("/{taskId}")
  public Response getTask(@PathParam("taskId") String taskId) {
    Task task = TaskManagementService.getTask(taskId);
    if (task == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    return Response.ok().entity(task).build();
  }

  @POST
  public Response createTask(Task task) {
    try {
      TaskManagementService.createTask(task);
    }
    catch (DuplicateTaskException e) {
      return Response.status(Response.Status.CONFLICT).build();
    }

    try {
      return Response.created(new URI("todo/" + task.getTaskId())).entity(task).build();
    }
    catch (URISyntaxException e) {
      // We just use the task id, so not sure what to do here.
      throw new RuntimeException(e);
    }
  }

  @PUT
  @Path("/{taskId}")
  public Response updateTask(@PathParam("taskId") String taskId, Task task) {
    task.setTaskId(taskId);
    try {
      TaskManagementService.updateTask(task);
      return Response.noContent().build();
    }
    catch (UnknownTaskException e) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
  }

  @DELETE
  @Path("/{taskId}")
  public Response deleteTask(@PathParam("taskId") String taskId) {
    Task task = TaskManagementService.getTask(taskId);
    if (task == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    else {
      TaskManagementService.deleteTask(task);
      return Response.noContent().build();
    }
  }

  @PATCH
  @Path("/{taskId}")
  //@Consumes("application/json-patch+json")
  public Response patchTask(@PathParam("taskId") String taskId, List<JsonPatchOperation> patch) {
    Task task = TaskManagementService.getTask(taskId);
    if (task == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    boolean changed = false;

    for (JsonPatchOperation change : patch) {
      // This is home-grown, so for now we only support replace.  I don't think any other op
      // makes sense for our schema.
      if ("replace".equals(change.getOp()) && change.getPath() != null) {
        changed = true;

        switch (change.getPath()) {
          case "/title":
            task.setTitle(change.getValue());
            break;

          case "/body":
            task.setBody(change.getValue());
            break;

          case "/done":
            task.setDone("true".equals(change.getValue()));
            break;
        }
      }
    }

    if (changed) {
      return updateTask(task.getTaskId(), task);
    }
    else {
      return Response.notModified().build();
    }
  }

  /**
   * For clients that do not support the HTTP PATCH method, we support partial updates via POST to the resource URI
   * and accept form-encoded attribute mappings.
   *
   * This method is not purely RESTful because we are supporting idempotent partial updates via POST.  However, it
   * also does not really violate any rules, so it is a useful corner of the standard to insert compatibility support.
   */
  @POST
  @Path("/{taskId}")
  @Consumes("application/x-www-form-urlencoded")
  public Response partialUpdateTask(@PathParam("taskId") String taskId, @FormParam("title") String title, @FormParam("body") String body, @FormParam("done") Boolean done) {
    Task task = TaskManagementService.getTask(taskId);
    if (task == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    if (title == null && body == null && done == null) {
      return Response.notModified().build();
    }

    if (title != null) {
      task.setTitle(title);
    }
    if (body != null) {
      task.setBody(body);
    }
    if (done != null) {
      task.setDone(done.booleanValue());
    }

    return updateTask(task.getTaskId(), task);
  }

  /**
    * Decorate search results with REST-style information.  Specifically, resource URI's.
    */
  private PaginatedSearchResults<Task> restified(final PaginatedSearchResults<Task> bare) {
    PaginatedSearchResults<Task> restified = new PaginatedSearchResults<Task>(bare.getTotalCount(), new ArrayList<Task>(bare.getResults().size()));
    for (Task task : bare.getResults()) {
      restified.getResults().add(new RestifiedTask(task));
    }
    return restified;
  }

  private class RestifiedTask extends Task {
    RestifiedTask(Task task) {
      setTaskId(task.getTaskId());
      setTitle(task.getTitle());
      setBody(task.getBody());
      setDone(task.isDone());
    }

    public URI getLocation() {
      return uriInfo.getBaseUriBuilder().path(TaskResource.class).path(getTaskId()).build();
    }
  }
}
