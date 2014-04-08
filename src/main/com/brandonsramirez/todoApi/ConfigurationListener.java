package com.brandonsramirez.todoApi;

import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.brandonsramirez.todoApi.dal.InMemoryTaskDaoFactory;
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
    Properties p = loadPropertiesFile(ctx, "dao.properties");
    if (p != null) {
      ServiceLocator.getTaskManagementService(ctx).setDaoFactory(new MongoFactory(p));
      ctx.log("Initialized TaskDao from " + ctx.getClassLoader().getResource("dao.properties"));
    }
    else {
      ServiceLocator.getTaskManagementService(ctx).setDaoFactory(new InMemoryTaskDaoFactory());
    }
  }

  private static void configureSearchProvider(ServletContext ctx) {
    Properties p = loadPropertiesFile(ctx, "search.properties");
    if (p != null) {
      ServiceLocator.getTaskManagementService(ctx).setSearchProvider(new SearchlySearchProvider(p.getProperty("url")));
      ctx.log("Initialized SearchProvider from " + ctx.getClassLoader().getResource("search.properties"));
    }
    else {
      ServiceLocator.getTaskManagementService(ctx).setSearchProvider(new StubSearchProvider());
      ctx.log("Unable to find search.properties on the class path - search will not function!");
    }
  }

  private static void configureSmsNotifier(ServletContext ctx) {
    Properties p = loadPropertiesFile(ctx, "twilio.properties");
    if (p != null) {
      ServiceLocator.getTaskManagementService(ctx).setSmsNotifier(new TwilioSmsNotifier(p.getProperty("accountSid"), p.getProperty("authToken"), p.getProperty("twilioNumber"), p.getProperty("mobileNumber")));
      ctx.log("Initialized SmsNotifier from " + ctx.getClassLoader().getResource("twilio.properties"));
    }
    else {
      ServiceLocator.getTaskManagementService(ctx).setSmsNotifier(new StubSmsNotifier());
    }
  }

  private static Properties loadPropertiesFile(ServletContext ctx, String fileName) {
    ClassLoader cl = ctx.getClassLoader();

    InputStream is = null;
    try {
      is = cl.getResourceAsStream(fileName);
      if (is != null) {
        Properties p = new Properties();
        p.load(is);
        return p;
      }
    }
    catch (IOException e) {
      ctx.log("Failed reading " + fileName);
    }
    finally {
      if (is != null) {
        try {
          is.close();
        }
        catch (IOException e) { /* ignore */ }
      }
    }

    return null;
  }
}
