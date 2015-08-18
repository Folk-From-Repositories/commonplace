#!/bin/sh
server_api_key='___SERVER_KEY___'
token='___TARGET_DEVICE_ID___'

curl --header "Authorization: key=$server_api_key" \
--header Content-Type:"application/json" \
https://gcm-http.googleapis.com/gcm/send \
-d "{\"data\":{\"title\":\"GCM demo\",\"message\":\"Google Cloud Messaging 테스트\"},\"to\":\"$token\"}"
