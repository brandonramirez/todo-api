package com.brandonsramirez.todoApi;

import com.google.gson.JsonObject;

import io.searchbox.core.*;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;

import org.elasticsearch.index.query.QueryBuilders;

public class SearchlySearchProvider implements SearchProvider {
  private JestClient client;

  SearchlySearchProvider(String endpointUrl) {
    // Construct a new Jest client according to configuration via factory
    JestClientFactory factory = new JestClientFactory();
    factory.setHttpClientConfig(new HttpClientConfig
                           .Builder(endpointUrl)
                           .multiThreaded(true)
                           .build());
    this.client = factory.getObject();
  }

  public void addToIndex(Task task) {
    try {
      client.execute(new Index.Builder(task).index("tasks").type("task").id(task.getTaskId()).build());
    }
    catch (Exception e) {
      // It's pretty bad that JEST generically declares "Exception".  What can I do with that?
      throw new RuntimeException(e);
    }
  }

  public void updateInIndex(Task task) {
    addToIndex(task);
  }

  public void removeFromIndex(String taskId) {
    try {
      client.execute(new Delete.Builder(taskId).index("tasks").type("task").build());
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public PaginatedSearchResults<Task> search(String query, int offset, int max) {
    // Neither the JEST client nor the native Elastic search client "just work" here, so this is slightly convuluted.
    // We use QueryBuilders from the ES client to generate a multi-match query which then has to be wrapepd in a JSON "query" object
    // manually and then piped into the JEST Search.Builder.

    // The following documentation helped:
    // http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-request-body.html
    // http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-request-from-size.html
    // http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-multi-match-query.html
    // http://www.elasticsearch.org/guide/en/elasticsearch/client/java-api/current/query-dsl-queries.html
    // http://rajish.github.io/api/elasticsearch/0.20.0.Beta1-SNAPSHOT/org/elasticsearch/index/query/MultiMatchQueryBuilder.html

    String searchString = "{ \"from\": " + offset + ", \"size\": " + max + ", \"query\": " + QueryBuilders.multiMatchQuery(query, "title^5", "body").toString() + " }";
    Search search = new Search.Builder(searchString).addIndex("tasks").build();

    try {
      // The response object from JEST does not give us the total hit count, so we have
      // to manually extract it from the JSON response.
      JestResult response = client.execute(search);
      JsonObject json = response.getJsonObject();
      return new PaginatedSearchResults<Task>(json.getAsJsonObject("hits").get("total").getAsInt(),
                                              response.getSourceAsObjectList(Task.class));
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}