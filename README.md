# job-health-check-plugin

bonita engine extension to report failed job and automatically replay them

## Goals
Periodically log the number of failed jobs every hour
Replay them in the same run

It needs to be configurable:
* the period
* activation

Cluster consideration:
 - only one node would have the jar installed
 - either there is a cluster lock to guarantee one execution
 
## Generate

    mvn package

## Installation procedure example with Tomcat bundle

* Stop the application server
* Copy the jar file into ./server/webapps/bonita/WEB-INF/lib/
* Start the application server

## Configuration

### Default values
```
org.bonitasoft.support.plugin.job.DELAY_IN_MILLIS=3600000
org.bonitasoft.support.plugin.job.MAX_RESULTS=100
```

### Change from default

There are 2 possibilities

#### 1. Define these environment variables

    export org_bonitasoft_support_plugin_job_DELAY_IN_MILLIS=3600000
    export org_bonitasoft_support_plugin_job.MAX_RESULTS=100

#### 2. Edit the setup/platform_conf/current/tenants/1/tenant_engine/bonita-tenant-sp-custom.properties file
```
cd setup
./setup.sh pull
cat >> platform_conf/current/tenants/1/tenant_engine/bonita-tenant-sp-custom.properties
org.bonitasoft.support.plugin.job.DELAY_IN_MILLIS=86400000
org.bonitasoft.support.plugin.job.MAX_RESULTS=100
./setup.sh push
```

# Report log messages generated
```
2019-02-14 16:03:21.495 +0100 INFO: org.bonitasoft.support.plugin.job.JobHealthCheckService THREAD_ID=28 | HOSTNAME=Tetrapharmakon | TENANT_ID=1 | checking failed jobs.
2019-02-14 16:03:21.497 +0100 INFO: org.bonitasoft.support.plugin.job.JobHealthCheckService THREAD_ID=28 | HOSTNAME=Tetrapharmakon | TENANT_ID=1 | 0 failed jobs found.
2019-02-14 16:03:41.498 +0100 INFO: org.bonitasoft.support.plugin.job.JobHealthCheckService THREAD_ID=28 | HOSTNAME=Tetrapharmakon | TENANT_ID=1 | checking failed jobs.
2019-02-14 16:03:41.501 +0100 INFO: org.bonitasoft.support.plugin.job.JobHealthCheckService THREAD_ID=28 | HOSTNAME=Tetrapharmakon | TENANT_ID=1 | 0 failed jobs found.
```
