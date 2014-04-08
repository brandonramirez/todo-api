package com.brandonsramirez.todoApi.dal;

import java.util.Properties;

import com.brandonsramirez.todoApi.DaoFactory;
import com.brandonsramirez.todoApi.TaskDao;

public class InMemoryTaskDaoFactory implements DaoFactory {
  TaskDao dao = new InMemoryTaskDao();

  @Override
  public void initialize(Properties p) {
    // nothing to do here - we have no properties
  }

  @Override
  public TaskDao createTaskDao() {
    return dao;
  }
}
