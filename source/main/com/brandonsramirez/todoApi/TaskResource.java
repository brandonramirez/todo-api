package com.brandonsramirez.todoApi;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

@Path("/todo")
@Consumes("application/json")
@Produces("application/json")
public class TaskResource {
  @Context private UriInfo uriInfo;

  @GET
  public Response listTasks(@DefaultValue("0")  @QueryParam("offset") int offset,
                            @DefaultValue("10") @QueryParam("max") int max)
  {
    return Response.ok().entity(restified(TaskManagementService.listTasks(offset, max))).build();
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
    TaskManagementService.createTask(task);
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
    Task existingTask = TaskManagementService.getTask(taskId);
    if (existingTask == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    TaskManagementService.updateTask(task);
    return Response.noContent().build();
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
