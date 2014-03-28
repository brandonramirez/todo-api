package com.brandonsramirez.todoApi.dal;

import java.net.UnknownHostException;
import java.util.Properties;

import com.brandonsramirez.todoApi.DaoFactory;
import com.brandonsramirez.todoApi.TaskDao;

import com.mongodb.MongoClient;

public class MongoFactory implements DaoFactory {
  private TaskDao dao;

  @Override
  public void initialize(Properties p) {
    try {
      MongoClient client = new MongoClient(p.getProperty("host", "localhost"), Integer.parseInt(p.getProperty("port", "27017")));
      this.dao = new MongoDao(client, p.getProperty("database", "todo"));
    }
    catch (UnknownHostException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public TaskDao createTaskDao() {
    return dao;
  }
}
