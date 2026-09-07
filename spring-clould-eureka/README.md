# Eureka Server Sample

## Build & Run
### Run
`./gradlew bootRun`


## Server Resource
### Port
default port is 8761

### URL Parameter
| Path             | Description  |
|------------------|--------------|
| /                | Home page (HTML UI) listing service registrations    |
| /eureka/apps     | Raw registration metadata |

## Docker Container

There is a Maven goal (using a [plugin](https://github.com/spring-cloud-samples/eureka/blob/feature/docker/pom.xml#L48)) to 
generate the Docker container. The container is published in dockerhub at [springcloud/eureka](https://hub.docker.com/r/springcloud/eureka).
