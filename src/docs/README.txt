SQS Tester
=======================
This simple example utilizes Amazon Web Services Simple Queue Service to demonstrate
  injecting 1 or more messages into a transient queue and reading them after a delay.

To run, execute the bin/sqsdemo.sh script.  The  following env vars may be used to 
  specify configuration. 

  AWS_ACCESS_KEY_ID: The Amazon Web Services access key to use in connecting to AWS
                        If not set, the default public key will be used

  AWS_SECRET_KEY: The Amazon Web Services secret key to use in connecting to AWS
                   If not set, the default public key will be used

The app will create an SQS queu, inject 5 messages into it, pause, read the messages
   from the SQS Queue and then cleanup by deleting the Queu instance.


You may see a sample output of the program by viewing log/sample-output.log
