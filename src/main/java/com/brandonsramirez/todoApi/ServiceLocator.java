package com.brandonsramirez.todoApi;

import javax.servlet.ServletContext;

public class ServiceLocator {
  private static final String CONTEXT_ATTRIBUTE_NAME = "task.management.service";

  @SuppressWarnings("unchecked")
  public static TaskManagementService getTaskManagementService(ServletContext ctx) {
    if (ctx.getAttribute(CONTEXT_ATTRIBUTE_NAME) == null) {
      ctx.setAttribute(CONTEXT_ATTRIBUTE_NAME, new TaskManagementService());
    }
    return (TaskManagementService) ctx.getAttribute(CONTEXT_ATTRIBUTE_NAME);
  }
}