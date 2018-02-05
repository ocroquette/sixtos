# Sixtos, a simple HTTP file server

Sixtos is a very simple HTTP file server to upload, host and download any kind of files. It supports:

* uploading files with HTTP put requests
* downloading uploaded files with HTTP get requests
* user management using a local credential file

It was initially created as the most simple software artifact server possible, therefore it doesn't allow to overwrite files.

## Building

Since no binary distribution is available currently, you need to build Sixtos from source. You just need a JDK and to run the following command:

```
gradlew shadowJar
```

The resulting JAR file with all dependencies will be created in build/libs.

## Configuration

### Main configuration file

Sixtos is based on [DropWizard](http://www.dropwizard.io/), and supports therefore its numerous configuration options. Here is a sample configuration file:

```
logging:
  level: INFO
server:
  applicationConnectors:
    - type: http
      port: 8090
      bindHost: 127.0.0.1
  adminConnectors:
    - type: http
      port: 8091
      bindHost: 127.0.0.1
storageRoot: storageRoot/
credentialsFile: config/credentials
```

The following options are specific to Sixtos:

* ```storageRoot```: path of the root directory where to store the uploaded files
* ```credentialsFile```: path of the file containing the user credentials (see below)

### Credentials file

All HTTP requests require valid credentials provided through the HTTP basic authentication. You should therefore use HTTPS for production.

To create the credentials file, use the following command line:

```
java -classpath sixtos-<version>-all.jar com.github.ocroquette.sixtos.CredentialsFile credentials <username>
```

It will ask for the password on the console, and create a new file ```credentials```:

```
username:cf02c881bb3d45f59d71b233739c9bbb8c68ac0705ff5d7c6dce697109f68b02a9030caf:
```

You can grant the user some rights by adding one or more roles separated by commas at the end of the line:

```
username:cf02c881bb3d45f59d71b233739c9bbb8c68ac0705ff5d7c6dce697109f68b02a9030caf:PUT,GET
```


* ```PUT```: to upload files
* ```GET```: to download files

## Starting the server

```
java -jar ./build/libs/sixtos-1.0-SNAPSHOT-all.jar server config/config.yml
```

## Testing with curl

For experimenting downloads and uploads, you can use curl on the command line:

```
# Uploading:
curl -u user:password "http://localhost:8090/somefile" --upload-file somefile

# Downloading:
curl -u user:password http://localhost:8090/somefile
```

You can get the list of available files as text by GETting the root directory:

```
# Get List
curl -u user:password http://localhost:8090/
```


