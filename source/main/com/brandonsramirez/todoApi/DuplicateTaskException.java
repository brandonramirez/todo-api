package com.brandonsramirez.todoApi;

public class DuplicateTaskException extends Exception {
  public DuplicateTaskException() {
    super();
  }

  public DuplicateTaskException(Throwable t) {
    super(t);
  }
}
