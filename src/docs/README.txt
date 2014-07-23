QueuePOC
=======================
This simple example utilizes Amazon Web Services Simple Queue Service and Simple
Notification Service to demonstrate injecting messages into a pub/sub topic (SNS)
and having them delivered to multiple message queues for subsequent processing.

To run, execute the bin/queuepoc.sh script.  The following env vars may be used to 
  specify configuration. 

  AWS_ACCESS_KEY_ID: The Amazon Web Services access key to use in connecting to AWS
                        If not set, the default public key will be used

  AWS_SECRET_KEY: The Amazon Web Services secret key to use in connecting to AWS
                   If not set, the default public key will be used

The app will generate messages in one thread and publish them to an SNS topic.  Two
other threads will be started and watch the SQS Queues and process messages on them
as they become available.

You may see a sample output of the program by viewing log/sample-queuepoc-output.log
