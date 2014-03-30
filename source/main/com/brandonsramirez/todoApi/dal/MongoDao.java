package com.brandonsramirez.todoApi.dal;

import java.util.ArrayList;

import com.brandonsramirez.todoApi.DuplicateTaskException;
import com.brandonsramirez.todoApi.PaginatedSearchResults;
import com.brandonsramirez.todoApi.Task;
import com.brandonsramirez.todoApi.TaskDao;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;

import org.bson.types.ObjectId;

public class MongoDao implements TaskDao {
  private static final String COLLECTION_NAME = "tasks";

  private MongoClient client;
  private DB db;
  private DBCollection tasks;

  MongoDao(MongoClient client, String dbName) {
    this.client = client;
    this.db = client.getDB(dbName);
    this.tasks = db.getCollection(COLLECTION_NAME);
  }

  @Override
  public PaginatedSearchResults<Task> listTasks(int offset, int max) {
    DBCursor cursor = tasks.find().sort(new BasicDBObject("title", 1)).skip(offset).limit(max);
    ArrayList<Task> tasks = new ArrayList<Task>(max);
    while (cursor.hasNext()) {
      tasks.add(new DbObjectAdapter(cursor.next()).toTask());
    }
    tasks.trimToSize();
    return new PaginatedSearchResults<Task>(cursor.count(), tasks);
  }

  @Override
  public Task getTask(String taskId) {
    try {
      DBObject o = tasks.findOne(new BasicDBObject("_id", new ObjectId(taskId)));
      return new DbObjectAdapter(o).toTask();
    }
    catch (IllegalArgumentException e) {
      // happens if the object id is malformed.  if it is, then it clearly does not map to a Task, thus return null.
      return null;
    }
  }

  @Override
  public String createTask(Task task) throws DuplicateTaskException {
    DbObjectAdapter o = new DbObjectAdapter(task);
    try {
      tasks.insert(WriteConcern.JOURNALED, o);
      task.setTaskId(o.getObjectId("_id").toString());
      return task.getTaskId();
    }
    catch (MongoException.DuplicateKey e) {
      throw new DuplicateTaskException(e);
    }
  }

  @Override
  public void updateTask(Task task) {
    tasks.update(new BasicDBObject("_id", new ObjectId(task.getTaskId())), new DbObjectAdapter(task).removeId());
  }

  @Override
  public void deleteTask(String taskId) {
    tasks.remove(new BasicDBObject("_id", new ObjectId(taskId)));
  }
}
