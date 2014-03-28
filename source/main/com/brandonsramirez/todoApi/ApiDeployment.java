package com.brandonsramirez.todoApi;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/rest")
public class ApiDeployment extends Application {
  // nothing to do in here at this time - the presence of this class
  // configures the JAX-RS API through our annotations.
}
