# Roll Your Own Managed Kubernetes (ryomk)

A comprehensive solution for managing K3s clusters with KVM virtualization, featuring a Spring Boot backend API and a Python CLI tool.

## 🏗️ Architecture

This project consists of two main components:

- **Spring Boot Backend API** - RESTful API for VM and cluster management
- **Python CLI Tool** - Command-line interface for easy interaction with the API

## 🚀 Quick Start

### Prerequisites

- Docker and Docker Compose
- Java 17+ (for local development)
- Python 3.12+ (for CLI development)
- PDM (Python dependency manager)
- Maven (for local backend development)

### Using Docker (Recommended)

1. **Clone and start the services:**
   ```bash
   git clone <repository-url>
   cd ryomk
   make build up
   ```

2. **Verify the API is running:**
   ```bash
   curl http://localhost:8080/actuator/health
   ```

3. **Install and use the CLI:**
   ```bash
   cd cli
   pdm install
   pdm run ryomk vm list
   ```

## 📋 Project Structure

```
k3s-kvm-master-api/
├── src/                    # Spring Boot application source
│   ├── main/java/
│   │   └── com/k3skvmmaster/
│   │       ├── controller/ # REST API controllers
│   │       ├── service/    # Business logic services
│   │       ├── model/      # Data models and DTOs
│   │       ├── config/     # Configuration classes
│   │       └── util/       # Utility classes
│   └── resources/          # Configuration files
├── cli/                    # Python CLI application
│   ├── src/cli/           # CLI source code
│   ├── pyproject.toml     # Python dependencies
│   └── README.md          # CLI-specific documentation
├── docker-compose.yaml    # Docker services configuration
├── Dockerfile            # Backend container definition
├── Makefile              # Build and deployment commands
└── pom.xml               # Maven dependencies
```

## 🔧 Backend API (Spring Boot)

### Features

- **VM Management**: Create, delete, start, stop, and list virtual machines
- **Cluster Management**: Join nodes to K3s cluster, manage join tokens
- **Kubernetes Integration**: Direct interaction with K3s cluster
- **Libvirt Integration**: KVM virtualization management
- **SSH Operations**: Remote system management
- **Cloud-Init Support**: Automated VM provisioning

### API Endpoints

View all API endpoint documentation at: `http://localhost:8080/swagger-ui.html`

### Configuration

The backend uses environment variables for configuration:

```bash
# Libvirt Configuration
LIBVIRT_URI=qemu+ssh://user@host/system
VM_BASE_IMAGE_PATH=/path/to/base.qcow2
VM_IMAGES_DIRECTORY=/var/lib/libvirt/images

# K3s Configuration
K3S_MASTER_IP=192.168.50.34
K3S_MASTER_VERSION=v1.30.5+k3s1
K3S_MASTER_NETWORK=kube-net
K3S_MASTER_BRIDGE=br0
```

### Local Development

```bash
# Install dependencies
mvn clean install

# Run the application
./mvnw spring-boot:run

# Run tests
./mvnw test
```

## 🐍 Python CLI Tool

### Features

- **Modern CLI Interface**: Intuitive command structure
- **Type Safety**: Full type checking with Pyright
- **Error Handling**: Clear error messages and proper exit codes
- **Configuration**: Customizable API endpoints
- **Legacy Support**: Backward compatibility with numeric commands

### Installation

```bash
cd cli
pdm install
```

### Usage Examples

View usage examples at `/examples/example.sh`

### Development

```bash
# Run in development mode
pdm run python -m ryomk
```

## 🐳 Docker Deployment

### Using Makefile Commands

```bash
make # show all available commands
```

## 🆘 Support

For issues and questions:
- Check the API documentation at `/swagger-ui.html`
- Review the CLI help with `pdm run ryomk --help`
- Check the logs with `make logs`