# submission-migrator

The submission migrator tool is used to export submissions from Kinetic Request RE and import submissions to Kinetic Request CE.

## Requirements

* Use gradle to build the project
  * This will build a submission-migration.jar file in the build/libs dir
* Use java to run the tool
  * The tool is run on the command line
* A config.yaml file is required in the same directory as the submission-migration.jar

## Create Yaml

```
Request RE:
  server: www.servername.com
  port: 3000
  username: Demo
  password: 
  query_limit: 1000
  qualification: >
      'fieldId':"value"
Request CE:
  url: https://www.servername.com/kinetic
  space: space-slug
  kapp: kapp-slug
  username: username
  password: password
```

## Export

There are there option to export submissions:
* Extract submissions from ALL catalogs and ALL forms
  * ```java -jar submission-migrator.jar export```
* Extract submissions from ALL forms within the specified catalog
  * ```java -jar submission-migrator.jar export “<catalog name>”```
* Extract submissions from the specifics form within the specified catalog
  * ```java -jar submission-migrator.jar export “<catalog name>” “<form name>”```
  
The commands assume you are in the directory with submission-migrator.jar. The config.yam must also be in the same directory.

## Import
