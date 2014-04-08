package com.brandonsramirez.todoApi;

import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.brandonsramirez.todoApi.dal.MongoFactory;

@WebListener
public class ConfigurationListener implements ServletContextListener {
  @Override
  public void contextInitialized(ServletContextEvent event) {
    ServletContext ctx = event.getServletContext();
    configureDao(ctx);
    configureSearchProvider(ctx);
    configureSmsNotifier(ctx);
  }

  @Override
  public void contextDestroyed(ServletContextEvent e) {
    // nothing to do
  }

  private static void configureDao(ServletContext ctx) {
    ClassLoader cl = ctx.getClassLoader();

    InputStream is = null;
    try {
      is = cl.getResourceAsStream("dao.properties");
      if (is != null) {
        ctx.log("Initializing TaskDao from " + cl.getResource("dao.properties"));
        Properties p = new Properties();
        p.load(is);
        ServiceLocator.getTaskManagementService(ctx).setDaoFactory(new MongoFactory(p));
        ctx.log("Succesfully initialized TaskDao from " + cl.getResource("dao.properties") + " with DAO class " + p.getProperty("className"));
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

  private static void configureSearchProvider(ServletContext ctx) {
    ClassLoader cl = ctx.getClassLoader();

    InputStream is = null;
    try {
      is = cl.getResourceAsStream("search.properties");
      if (is != null) {
        ctx.log("Initializing SearchProvider from " + cl.getResource("search.properties"));
        Properties p = new Properties();
        p.load(is);
        ServiceLocator.getTaskManagementService(ctx).setSearchProvider(new SearchlySearchProvider(p.getProperty("url")));
        ctx.log("Succesfully initialized SearchProvider from " + cl.getResource("search.properties"));
      }
      else {
        ServiceLocator.getTaskManagementService(ctx).setSearchProvider(new StubSearchProvider());
        ctx.log("Unable to find search.properties on the class path - search will not function!");
      }
    }
    catch (IOException e) {
      ctx.log("Failed reading search.properties.", e);
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

  private static void configureSmsNotifier(ServletContext ctx) {
    ClassLoader cl = ctx.getClassLoader();

    InputStream is = null;
    try {
      is = cl.getResourceAsStream("twilio.properties");
      if (is != null) {
        ctx.log("Initializing SmsNotifier from " + cl.getResource("twilio.properties"));
        Properties p = new Properties();
        p.load(is);
        ServiceLocator.getTaskManagementService(ctx).setSmsNotifier(new TwilioSmsNotifier(p.getProperty("accountSid"), p.getProperty("authToken"), p.getProperty("twilioNumber"), p.getProperty("mobileNumber")));
        ctx.log("Succesfully initialized SmsNotifier from " + cl.getResource("twilio.properties"));
      }
      else {
        ServiceLocator.getTaskManagementService(ctx).setSmsNotifier(new StubSmsNotifier());
      }
    }
    catch (IOException e) {
      ctx.log("Failed reading twilio.properties");
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
}
