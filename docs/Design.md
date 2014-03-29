Design artifacts
----------------

Java package: com.brandonsramirez.todoApi

TaskResource:

  - JAX-RS annotated class providing REST API
  - should basically be a proxy to TaskManagementService

Task:

  - Central entity.  Contains only state and (possibly) validation
  - Properties:
    - taskId: Persistent primary key
    - title: User-supplied textual identifier of the task (does this need to be unique?)
    - body: Freeform text describing the task that needs to be done
    - done: Boolean, true if the task has been complete, otherwise false

DbObjectAdapter:

  - implements the MongoDB client's DbObject interface
  - has a `toTask()` method which returns a Task object
  - constructor takes a Task object and uses that as the source for DbObject's `put` and `get` methods.

TaskManagementService:

  - defines static methods for listing tasks, searching for tasks, CRUD

TaskDao interface:

  - Defines a way to read and write todo items from storage
  - InMemoryTaskDao - HashMap-based implementation
  - MongoDbTaskDao - MongoDB-based implementation

DaoFactory interface:

  - Defines an abstract method for creating an instance of TaskDao
  - abstract factory pattern
  - used for initializing the app in proper production mode or unit test mode
  - dependency injection

ConfigurationListner:

  - ServletContextListener which reads in /dao.properties and /search.properties from class path
  - dao.properties will drive which DAO impl to create and its initialization
  - search.properties defines API details for searbox.io
  - call TaskManagementService.setDao with a reference to the new `DaoFactory` object.
  - call TaskManagementService.setSearchProvider with a reference to the `SearchProvider` object.
