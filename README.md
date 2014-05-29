# AWS-Lib

Utility library for accessing and performing common tasks against the AWS API


## Install

```
mvn clean install
```

## Deploy


Merge stable code into master, and run the jenkins build:
[http://jenkins.dev-charter.net/job/aws-lib/](http://jenkins.dev-charter.net/job/aws-lib/)
<br />
And tag master with the release version.

## EipAssociationModule Usage

Bind the following module to the Bootstrap class
<pre>
binder.install(new EipAssociationModule());
</pre>

Allocate XX amount of Elastic IP addresses for use with the target application.
<br />

Number of EIP's should be at least 2 times the amount of actively running EC2 instances to compensate deployment.
<br /><br />

Create a DNS TXT record containing using the following format:
```
txt.${archaius.deployment.applicationId}.${archaius.deployment.region}.${aesd.archaius.deployment.domain}
```
<br />
Containing space delimited EIP addresses wrapped in quotes
<br />

Example
```
txt.helloworld.us-west-2.dev-charter.net. TXT "11.22.33.44" "55.66.77.88"
```