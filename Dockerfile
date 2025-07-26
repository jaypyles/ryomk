FROM ubuntu:22.04

# Install Java and libvirt dependencies
RUN apt update -y && apt install -y \
    openjdk-21-jdk \
    libvirt-clients \
    libvirt-daemon-system \
    libvirt-dev \
    openssh-client \
    maven \
    genisoimage \
    libguestfs-tools \
    && rm -rf /var/lib/apt/lists/*



WORKDIR /app

# Copy Maven wrapper and pom files first (dependency info)
COPY pom.xml .

# Now copy the rest of the source code
COPY src src

# Build the package skipping tests
RUN mvn clean package -DskipTests

CMD ["mvn", "spring-boot:run"]
