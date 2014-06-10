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

### Configuring EIP Pool

Allocate XX amount of Elastic IP addresses for use with the target application. 
Number of EIP's should be at least 2 times the amount of actively running EC2 instances to compensate deployment.
<br /><br />

Create a DNS TXT record using the following format:
<br />

```
txt.${archaius.deployment.applicationId}.${archaius.deployment.region}.${aesd.archaius.deployment.domain}
```

Containing space delimited EIP addresses wrapped in quotes
<br />

### Example:

```
txt.helloworld.us-west-2.dev-charter.net. TXT "11.22.33.44" "55.66.77.88"
```

### IAM Policy
The EC2 instance using this module will require ec2:AssociateAddress and ec2:DescribeAddresses permissions in IAM

## S3Client Usage

### S3AuthType.INSTANCE_PROFILE
Uses an instance profile IAM to authenticate and access an S3 bucket
<br />

Usage:
```
S3Client client = new S3Client.Builder(S3AuthType.INSTANCE_ROLE).build();
```

### S3AuthType.PROFILE
Uses a profile config file to load AWS credentials for accessing an S3 bucket
<br />

Usage:
```
S3Client client = new S3Client.Builder(S3AuthType.PROFILE).build();
```

Usage with a profile name:
```
S3Client client = new S3Client.Builder(S3AuthType.PROFILE).setProfileName("my-profile").build();
```

Usage with a profile name, and config path:
```
S3Client client = new S3Client.Builder(S3AuthType.INSTANCE_ROLE).setProfileName("my-profile").setProfileConfigFilePath("/etc/.aws-credentials").build();
```

### S3 Profile Config File
The profile config can be used to specify one or mor set of AWS credentials distinguished by a profile name
<br />
The default location for a profile config file is in the home directory
```
~/.aws/credentials
```

Config file format:
[default]
aws_access_key_id=testAccessKey
aws_secret_access_key=testSecretKey

[my-profile]
aws_access_key_id=testAccessKey
aws_secret_access_key=testSecretKey