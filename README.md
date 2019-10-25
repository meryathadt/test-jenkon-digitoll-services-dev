### Insert default admin user

To create the credential admin:jackp0t

```
db.users.insert({ "_id" : ObjectId("5cbd15759f8e4b0001c59b03"), "username" : "admin", "firstName" : "Admin", "lastName" : "Admin", "password" : "$2a$10$xDdp//76R6uJfgwoqkV7cex.R/tyZ6cdVuzZNXMwZ2P0OruS8p.0u", "roles" : [ DBRef("roles", ObjectId("5cbd154d9f8e4b0001c59b02")) ], "createdAt" : ISODate("2019-04-22T01:14:29.367Z"), "_class" : "com.hyperaspect.auth.model.User" })

db.roles.insert({ "_id" : ObjectId("5cbd15419f8e4b0001c59b01"), "code" : "ROLE_USERS", "name" : "Users", "createdAt" : ISODate("2019-04-22T01:13:36.426Z"), "_class" : "com.hyperaspect.auth.model.Role" })
db.roles.insert({ "_id" : ObjectId("5cbd154d9f8e4b0001c59b02"), "code" : "ROLE_ADMIN", "name" : "Admin", "createdAt" : ISODate("2019-04-22T01:13:49.564Z"), "_class" : "com.hyperaspect.auth.model.Role" })
```

How to start the project on localhost

The server-side part of the project consists of two services:
  1. A backend for the DigoToll Website - Rest (by default runs on port 8080)
  2. A backend for the ERP functionality - Erp (by default runs on port 8081)
  
You can use a local mongo installation or connect to the development instance deployed online.

In order to have the complete functionality on localhost, you need also to start up the frontend. 

https://github.com/HyperAspect-Digitoll/digitoll-client

In ./src/config/index.js 

you will find BACKEND_URL for DEV and ERPDEV that you should change to the corresponding localhost URLs.

Refere to the frontend README on how to start it locally.

IMPORTANT
If you add properties in the application.properties file, copy them both to application.properties.dev and application.properties.prod. 
The application.properties file is used when you run the app locally. The .dev and .prod are used for the dev and prod environmets.

To skip Integration tests locally use 
mvn -pl -integration install 