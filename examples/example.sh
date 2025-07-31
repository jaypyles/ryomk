#!/bin/bash

# Change to the CLI directory
cd "../cli" || exit 1

if [ "$1" = "1" ]; then
  echo "Creating VM..."
  pdm run ryomk vm create \
    --name "k3s-worker-4" \
    --ip-address "192.168.50.89" \
    --gateway "192.168.50.1" \
    --system-user "jayden" \
    --iso-path "/home/%s/cloud-init/%s-cloud-init.iso"

elif [ "$1" = "2" ]; then
  echo "Deleting VM..."
  pdm run ryomk vm delete k3s-worker-4

elif [ "$1" = "3" ]; then
  echo "Listing VMs..."
  pdm run ryomk vm list

elif [ "$1" = "4" ]; then
  echo "Getting join token..."
  pdm run ryomk cluster join-token

elif [ "$1" = "5" ]; then
  echo "Joining node to cluster..."
  pdm run ryomk cluster join --node-ip "192.168.50.89"

elif [ "$1" = "6" ]; then
  echo "Creating node..."
  pdm run ryomk cluster create \
    --name "k3s-worker-ooooo" \
    --ip-address "192.168.50.91" \
    --gateway "192.168.50.1" \
    --system-user "jayden" \
    --vcpu 2 \
    --memory 4096 \
    --iso-path "/home/%s/cloud-init/%s-cloud-init.iso"

elif [ "$1" = "7" ]; then
  echo "Deleting node..."
  pdm run ryomk cluster delete k3s-worker-ooooo

elif [ "$1" = "8" ]; then
  echo "Creating node..."
  pdm run ryomk --base-url=http://192.168.50.34:30001 cluster create \
    --name "k3s-worker-4" \
    --ip-address "192.168.50.89" \
    --gateway "192.168.50.1" \
    --system-user "jayden" \
    --vcpu 2 \
    --memory 4096 \
    --iso-path "/home/%s/cloud-init/%s-cloud-init.iso"

else
  echo "Usage: $0 [1|2|3|4|5|6|7]"
  echo "  1 = create VM"
  echo "  2 = delete VM"
  echo "  3 = list VMs"
  echo "  4 = get join token"
  echo "  5 = join node to cluster"
  echo "  6 = create node"
  echo "  7 = delete node"
fi

