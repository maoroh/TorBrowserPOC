# TorBrowserPOC
Tor Browser POC Implementation in Java
<br />This Project developed in Computer Security Course , Open University.


## Short Description:

![alt text](tor.png)

For building POC of TOR Browser , I implement the Directory server that responsible for loading the nodes as well
<br/> on different ports.
<br/> In a real life usage , each node listening for requests on a different host and port.

## Preview Client

![alt text](preview.png)

## Execute Directory Server

The server must listening before the client is up.
<br />Use gradle for build executable jar:
```gradle
gradle serverJar
```

Run:
```java
java -jar TorBrowser-Server.jar
```


## Execute Client

Use gradle for build executable jar:
```gradle
gradle clientJar
```
Run:
```java
java -jar TorBrowser-Client.jar
```


## License
[MIT](https://choosealicense.com/licenses/mit/)
