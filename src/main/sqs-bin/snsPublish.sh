#!/bin/sh

#######################################################################
#
# Publish a message using an HTTP POST into the topic used by queuepoc
# For Reference, see AWS SNS API Documentation at
#     http://docs.aws.amazon.com/sns/latest/APIReference/API_Publish.html
#
#######################################################################
function rawurlencode() {
  local string="${1}"
  local strlen=${#string}
  local encoded=""

  for (( pos=0 ; pos<strlen ; pos++ )); do
     c=${string:$pos:1}
     case "$c" in
        [-_.~a-zA-Z0-9] ) o="${c}" ;;
        * )               printf -v o '%%%02x' "'$c"
     esac
     encoded+="${o}"
  done
  ENCODED_STR=${encoded}
}

TOPIC_ARN=arn:aws:sns:us-east-1:524950595403:QueuePOC-Demo-Topic
AWS_HOST=sns.us-east-1.amazonaws.com
AWS_ACCESS_KEY=AKIAICIJW4N4A5MZUHVA
AWS_SECRET_KEY=xHPmlSlKolPM2n3QrglzezbU/lpgyTMv2pjObZ+k

#####################################
# The example message to send
#####################################
epochTS=$(($(date +'%s * 1000 + %-N / 1000000')))
msg="{"
msg+="  \"MessageId\": \"1234-5678-9012-3456\","
msg+="  \"MessageName\": \"VideoEntitlements\","
msg+="  \"AccountNumber\":  \"80092320357266\","
msg+="  \"LastModified\": $epochTS "
msg+="}"

#### Build the HTTP data that envelopes the message

pubParams="AWSAccessKeyId="
rawurlencode $AWS_ACCESS_KEY
pubParams+=$ENCODED_STR

pubParams+="&Action=Publish"

reqDate=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
pubParams+="&Timestamp="
rawurlencode $reqDate
pubParams+=$ENCODED_STR

pubParams+="&SignatureVersion=2"
pubParams+="&SignatureMethod=HmacSHA256"
pubParams+="&Version=2010-03-31"

pubParams+="&TopicArn="
rawurlencode $TOPIC_ARN
pubParams+=$ENCODED_STR

pubParams+="&Message="
rawurlencode "$msg"
pubParams+=$ENCODED_STR

read -r -d '' REQUEST_DATA <<EOF
GET
$AWS_HOST
/
$pubParams
EOF

REQ_SIG=$(/bin/echo -n "$REQUEST_DATA" | openssl dgst -sha256 -hmac $AWS_SECRET_KEY -binary | openssl enc -base64 | sed 's/+/%2B/g;s/=/%3D/g;') 
pubParams+="&Signature=$REQ_SIG"

echo "Sending HTTP GET to $AWS_HOST with Query Params :: $pubParams"
curl -v "http://$AWS_HOST/?$pubParams"
