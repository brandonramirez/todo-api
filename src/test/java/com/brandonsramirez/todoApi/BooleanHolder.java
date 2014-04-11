package com.brandonsramirez.todoApi;

/**
 * Work-around for inner classes only being able to see final local variables from outer classes.
 */
public class BooleanHolder {
  public boolean val;

  BooleanHolder() {
    this(false);
  }
  BooleanHolder(boolean val) {
    this.val = val;
  }
}