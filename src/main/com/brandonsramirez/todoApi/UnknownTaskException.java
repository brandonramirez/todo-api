package com.brandonsramirez.todoApi;

public class UnknownTaskException extends Exception {
  public UnknownTaskException() {
    super();
  }

  public UnknownTaskException(Throwable t) {
    super(t);
  }
}
