#!/bin/bash

if [ "$1" = "1" ]; then
  curl -X POST http://127.0.0.1:8080/api/v1/vms \
    -H "Content-Type: application/json" \
    -d '{
      "name": "k3s-worker-4",
      "ipAddress": "192.168.50.89",
      "gateway": "192.168.50.1",
      "systemUser": "jayden",
      "sshKey": "/home/jayden/.ssh/id_rsa.pub",
      "isoPath": "/home/jayden/cloud-init/k3s-worker-4-cloud-init.iso"
    }'

elif [ "$1" = "2" ]; then
  curl -X DELETE http://127.0.0.1:8080/api/v1/vms/k3s-worker-4 \
    -H "Content-Type: application/json"

elif [ "$1" = "3" ]; then
  curl -X GET http://127.0.0.1:8080/api/v1/vms \
    -H "Content-Type: application/json | jq ."

elif [ "$1" = "4" ]; then
  curl -X GET http://127.0.0.1:8080/api/v1/clusters/join-token \
    -H "Content-Type: application/json"

elif [ "$1" = "5" ]; then
  curl -X GET http://127.0.0.1:8080/api/v1/clusters/join-cluster?nodeIp=192.168.50.89 \
    -H "Content-Type: application/json"

else
  echo "Usage: $0 [1|2]"
  echo " 1 = create VM"
  echo " 2 = delete VM"
fi

