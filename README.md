# Basic Kubernetes Wrapper

Just a test project, learning how Spring Boot works.

## Initialize 

`mvn -N io.takari:maven:wrapper`

`mvn clean install`

`make build up`

Ex:

```sh
curl -X GET 'http://127.0.0.1:8080/pods' | jq .[0]
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100  1533    0  1533    0     0   4094      0 --:--:-- --:--:-- --:--:--  4098
{
  "nodeName": "node-1",
  "creationTime": "2025-03-31T18:24:38Z",
  "isReady": "True",
  "IP": "10.0.0.247",
  "name": "test-pod-1"
}
```