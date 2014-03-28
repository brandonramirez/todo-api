package com.brandonsramirez.todoApi.dal;

import com.brandonsramirez.todoApi.Task;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import org.bson.BSONObject;

class DbObjectAdapter extends BasicDBObject {
  DbObjectAdapter(Task task) {
    put("_id", task.getTaskId());
    put("title", task.getTitle());
    put("body", task.getBody());
    put("done", task.isDone());
  }

  DbObjectAdapter(BSONObject o) {
    putAll(o.toMap());
  }

  Task toTask() {
    Task t = new Task();
    t.setTaskId(getObjectId("_id").toString());
    t.setTitle(getString("title"));
    t.setBody(getString("body"));
    t.setDone(getBoolean("done"));
    return t;
  }
}