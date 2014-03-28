package com.brandonsramirez.todoApi;

import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class ConfigurationListener implements ServletContextListener {
  @Override
  public void contextInitialized(ServletContextEvent event) {
    ServletContext ctx = event.getServletContext();
    ClassLoader cl = ctx.getClassLoader();

    InputStream is = null;
    try {
      is = cl.getResourceAsStream("dao.properties");
      if (is != null) {
        ctx.log("Initializing TaskManagementService from " + cl.getResource("dao.properties"));
        Properties p = new Properties();
        p.load(is);
        TaskManagementService.setDaoFactory(buildDao(p, ctx));
        ctx.log("Succesfully initialized TaskManagementService from " + cl.getResource("dao.properties") + " with DAO class " + p.getProperty("className"));
      }
      else {
        ctx.log("Unable to find dao.properties on the class path - todo api will not function!");
      }
    }
    catch (IOException e) {
      ctx.log("Failed reading dao.properties.", e);
    }
    finally {
      if (is != null) {
        try {
          is.close();
        }
        catch (IOException e) { /* ignore */ }
      }
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent e) {
    // nothing to do
  }

  @SuppressWarnings("unchecked")  // due to use of reflections
  private DaoFactory buildDao(Properties p, ServletContext ctx) {
    String className = p.getProperty("className", "com.brandonsramirez.todoApi.dal.InMemoryTaskDaoFactory");

    try {
      Class<DaoFactory> clazz = (Class<DaoFactory>) Class.forName(className);
      DaoFactory factory = clazz.newInstance();
      factory.initialize(p);
      return factory;
    }
    catch (ClassNotFoundException e) {
      ctx.log("Unable to find DAO implementation class " + className, e);
    }
    catch (IllegalAccessException e) {
      ctx.log("Scoping problem with DAO implementation class " + className, e);
    }
    catch (InstantiationException e) {
      ctx.log("Unable to create instance of class " + className, e);
    }

    return null;
  }
}
