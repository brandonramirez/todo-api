todo-api
========

This API provides a todo list.  Each todo item is called a task and has the following attributes:

* (string) taskId
* (string) title
* (string) body
* (boolean) done

Building
--------

To build, clone the repo, cd into the directory and run `ant build test`.

The build process will output a file named `todoApi.war` in the top-level directory.  This is a standard Java EE
web application which can be deployed into any servlet container.

Installing
----------

0. Create a file called `dao.properties` in the servlet container's classpath with the following settings and customize them for your database server.

        host=localhost
        port=27017
        database=todo

0. Create a file called `search.properties` in the servlet container's classpath with the following settings and customize them with your Searchly API key:

        url=http://site:<YOUR_API_KEY>@api.searchbox.io

0. Create a file called `twilio.properties` in the servlet container's classpath with the following settings and customize them with your Twilio API credentials, Twilio phone number and the phone number to which you want SMS alerts sent.

        accountSid=YOUR ACCOUNT SID
        authToken=YOUR TOKEN
        twilioNumber=YOUR TWILIO-PROVIDED PHONE NUMBER
        mobileNumber=PHONE NUMBER WHERE YOU WANT TO RECEIVE ALERTS

0. Copy `todoApi.war` into the servlet container's deployment directory.  For Tomcat, this is `$TOMCAT_HOME/webapps`.
0. Start up the servlet container.

Consuming the API
-----------------

The API expects JSON for input and will return JSON for output.

Base resource endpoint: `/todoApi/rest/todo`

### Listing tasks ###

You can list all tasks alphabetically by title.

Make a GET request to `/todoApi/rest/todo`

Results are paginated.  By default, only the first 10 are shown.  You can control this with the following query string parameters:

* `offset` - How far into the results do we fetch?
* `max` - At most how many results do I want to return?

For example, to see the second set of 20:

    GET /todoApi/rest/todo?offset=20&max=20

The result is a JSON object with 2 attributes:

* `totalCount` - Total number of tasks regardless of how many you requested.  This is helpful for clients who want to paginate.
* `results` - The results for the requested offset/max range.

Sample output:

    {
       "results":[
          {
             "body":"Make sure everything is committed in git",
             "done":false,
             "location":"http://installedlocation/todoApi/rest/todo/533784490cf2c80447314edf",
             "taskId":"533784490cf2c80447314edf",
             "title":"Check in code"
          },
          {
             "body":"There is a whole lot to do here",
             "done":true,
             "location":"http://installedlocation/todoApi/rest/todo/5337841f0cf2c80447314edd",
             "taskId":"5337841f0cf2c80447314edd",
             "title":"First todo"
          }
       ],
       "totalCount":6
    }

### Creating a task ###

Tasks can be created by `POST`ing the following JSON structure to `/todoApi/rest/todo`:

    {
      "title": "Quick summary of task I need to do",
      "body": "Details, details, details",
      "done": false
    }

You may not create multiple tasks with the same title.  If you attempt to create a task with a title of a task that already exists, an HTTP 409 error will be returned.

If the request succeeds, a `Location` header will be returned with the URL of the newly created task.

### Updating a task ###

Existing tasks can be updated using an HTTP `PUT` request to the task-specific URL that is returned when it is created and in the listing.  The same structure can be used as creating a task.

When an update transitions a task from not done to done, an SMS notification will be sent to the configured recipient.

### Deleting a task ###

A task can be deleted by making an HTTP `DELETE` request to the task-specific URL that is specified during creation and listing.

### Searching for tasks ###

A keyword search may be used to find tasks.  Both titles and bodies will be searched and the results will be ranked by relevance.

The endpoint URL is identical to listing tasks but takes a query parameter named `q`.  The same `max` and `offset` parameters may be specified.  Output will be paginated identically to listing.

For example,

    GET /todoApi/rest/todo?q=whole
    
    {
       "results":[
          {
             "body":"There is a whole lot to do here",
             "done":true,
             "location":"http://installedlocation/todoApi/rest/todo/5337841f0cf2c80447314edd",
             "taskId":"5337841f0cf2c80447314edd",
             "title":"First todo"
          }
       ],
       "totalCount":1
    }

### Partial updates ###

There are 2 ways to perform partial updates:

0. Using an HTTP `PATCH` request with a body formatted in the JSON Patch (RFC 6902) format (https://tools.ietf.org/html/rfc6902).
0. Using an HTTP `POST` REQUEST with form-encoded parameters in the body to the URL of the task resource itself.  Supported form parameters:
    * `title`
    * `body`
    * `done`

The PATCH method is preferred mechanism for partial updates in the todo API.  The POST method is supported for clients which don't support PATCH requests.

### How to mark a task complete ###

There are 3 ways to mark a task complete.  The first is the most flexible, but the least scalable because it transfers the entire resource twice.  The latter two are more efficient if you just want to mark the task complete.

0. Issue a GET request, change the `done` property of the object that is returned and then issue a PUT request to the same URL with the updated object.
0. Make the following PATCH request to the task's URL (`/todoApi/rest/todo/<taskId>`):

        [
          { "op": "replace", "path": "/done", "value": true }
        ]

0. Make the following POST request to the task's URL (`/todoApi/rest/todo/<taskId>`):

        done=true

All of these methods can also be used to mark a task incomplete by simply passing `false` instead of `true` for the `done` property.