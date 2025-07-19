# K3s CLI Tool

A Python CLI tool for managing VMs and clusters through the K3s KVM Master API.

## Installation

1. Navigate to the CLI directory:
   ```bash
   cd cli
   ```

2. Install dependencies using PDM:
   ```bash
   pdm install
   ```

3. Install the CLI tool:
   ```bash
   pdm install -e .
   ```

## Usage

### Modern Commands

The CLI provides modern, descriptive commands:

#### VM Management

```bash
# Create a VM
k3s-cli vm create \
  --name "k3s-worker-4" \
  --ip-address "192.168.50.89" \
  --gateway "192.168.50.1" \
  --system-user "jayden" \
  --ssh-key "/home/jayden/.ssh/id_rsa.pub" \
  --iso-path "/home/jayden/cloud-init/k3s-worker-4-cloud-init.iso"

# Delete a VM
k3s-cli vm delete k3s-worker-4

# List all VMs
k3s-cli vm list
```

#### Cluster Management

```bash
# Get cluster join token
k3s-cli cluster join-token

# Join a node to the cluster
k3s-cli cluster join --node-ip "192.168.50.89"

# Delete a pod
k3s-cli cluster delete-pod --pod-name "k3s-worker-4"

# Update a node
k3s-cli cluster update-node \
  --name "k3s-worker-4" \
  --ip-address "192.168.50.89" \
  --gateway "192.168.50.1" \
  --system-user "jayden" \
  --ssh-key "/home/jayden/.ssh/id_rsa.pub" \
  --iso-path "/home/jayden/cloud-init/k3s-worker-4-cloud-init.iso"
```

### Legacy Commands

For backward compatibility with the original bash script, the CLI also supports numeric commands:

```bash
# 1 = create VM (with default values)
k3s-cli 1

# 2 = delete VM (k3s-worker-4)
k3s-cli 2

# 3 = list VMs
k3s-cli 3

# 4 = get join token
k3s-cli 4

# 5 = join cluster (with default node IP)
k3s-cli 5

# 6 = delete pod (k3s-worker-4)
k3s-cli 6

# 7 = update node (with default values)
k3s-cli 7
```

### Configuration

You can specify a custom API base URL:

```bash
k3s-cli --base-url "http://localhost:9000" vm list
```

## Development

### Running in Development Mode

```bash
# From the cli directory
pdm run python -m cli

# Or with specific command
pdm run python -m cli vm list
```

### Type Checking

The project uses strict type checking with Pyright:

```bash
pdm run pyright
```

### Code Formatting

```bash
pdm run isort cli/
```

## API Endpoints

The CLI interacts with the following API endpoints:

- `POST /api/v1/vms` - Create VM
- `DELETE /api/v1/vms/{name}` - Delete VM
- `GET /api/v1/vms` - List VMs
- `GET /api/v1/clusters/join-token` - Get join token
- `GET /api/v1/clusters/join-cluster` - Join node to cluster
- `DELETE /api/v1/clusters/pod` - Delete pod
- `PUT /api/v1/clusters/node` - Update node

## Error Handling

The CLI provides clear error messages and proper exit codes:

- ✅ Success messages for completed operations
- ❌ Error messages for failed operations
- Proper exception handling with user-friendly messages 