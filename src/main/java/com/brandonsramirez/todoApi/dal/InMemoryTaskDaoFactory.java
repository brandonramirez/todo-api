package com.brandonsramirez.todoApi.dal;

import com.brandonsramirez.todoApi.DaoFactory;
import com.brandonsramirez.todoApi.TaskDao;

public class InMemoryTaskDaoFactory implements DaoFactory {
  TaskDao dao = new InMemoryTaskDao();

  @Override
  public TaskDao createTaskDao() {
    return dao;
  }
}
