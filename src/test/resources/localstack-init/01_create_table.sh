#!/bin/bash
awslocal dynamodb create-table \
  --table-name Order \
  --billing-mode PAY_PER_REQUEST \
  --attribute-definitions \
      AttributeName=orderId,AttributeType=S \
  --key-schema \
      AttributeName=orderId,KeyType=HASH
