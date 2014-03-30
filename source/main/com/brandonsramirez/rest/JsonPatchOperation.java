package com.brandonsramirez.rest;

import java.io.Serializable;

public class JsonPatchOperation implements Serializable {
  private String op;
  private String value;
  private String path;
  private String from;

  public String getOp() {
    return this.op;
  }

  public void setOp(String op) {
    this.op = op;
  }

  public String getValue() {
    return this.value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getPath() {
    return this.path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getFrom() {
    return this.from;
  }

  public void setFrom(String from) {
    this.from = from;
  }
}
