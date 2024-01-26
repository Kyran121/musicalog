# Musicalog

## Tech Stack

- Java 17
- Spring Boot 3
- MongoDB
- Mongo Express
- LocalStack (S3) - cover image files wll be stored here, in a bucket called `image-bucket`

## Prerequisites:

- Docker - this project requires running a compose file to start up

## Architecture

You can view my planning for this microservice in this [LucidCharts doc](https://lucid.app/lucidchart/500963f7-93fb-4da4-b8d1-ffa05fc87b88/edit?viewport_loc=808%2C1055%2C2681%2C2757%2C0_0&invitationId=inv_170a0e28-c9af-45b7-be99-8d5ffb73ed72)

Document includes:
- Data Schemas
- Data Transfer Objects
- Sequence Diagrams

## To start application

To start S3 and MongoDB:

```
docker-compose up -d
```

To start musicalog:

```
./gradlew bootRun --args='--spring.profiles.active=local'
```
Or running through IntelliJ in the `local` profile.

## To access Swagger

Once the application has started swagger can be accessed via:
http://localhost:8080/swagger-ui.html

## To stop application

First stop the application, and then run the following to stop S3 and MongoDB:

```
docker-compose down
```

## To access S3 

First you need to access the localstack container by running:

```
docker exec -it localstack-main /bin/bash
```

Then to list cover images in the image-bucket, you can run:

```
awslocal s3api list-objects --bucket image-bucket
```


## To access MongoDB

MongoDB can be accessed through Mongo Express at http://localhost:8081

```
user: admin
pass: pass
```