# auto-failed-job-plugin
bonita engine extension to report failed job and automatically replay them

Periodically log the number of failed jobs every hour
Replay them in the same run

It needs to be configurable:
* the period
* activation

Cluster consideration:
 - only one node would have the jar installed
 - either there is a cluster lock to guarantee one execution
 
