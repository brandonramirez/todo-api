package com.brandonsramirez.todoApi;

import java.io.Serializable;

public class Task implements Serializable {
   private static final long serialVersionUID = 1L;

   private String taskId;
   private String title;
   private String body;
   private boolean done;

   public String getTaskId() {
      return this.taskId;
   }

   public void setTaskId(String taskId) {
      this.taskId = taskId;
   }

   public String getTitle() {
      return this.title;
   }

   public void setTitle(String title) {
      this.title = title;
   }

   public String getBody() {
      return this.body;
   }

   public void setBody(String body) {
      this.body = body;
   }

   public boolean isDone() {
      return this.done;
   }

   public void setDone(boolean done) {
      this.done = done;
   }

   public boolean equals(Object o) {
      if (o instanceof Task) {
         Task other = (Task) o;
         return other.taskId != null && other.taskId.equals(this.taskId);
      }
      else {
         return false;
      }
   }

   public int hashCode() {
      return this.taskId.hashCode();
   }
}