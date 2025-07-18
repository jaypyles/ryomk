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
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (cached if pom.xml unchanged)
RUN ./mvnw dependency:go-offline

# Now copy the rest of the source code
COPY src src

# Build the package skipping tests
RUN ./mvnw clean package -DskipTests

CMD ["./mvnw", "spring-boot:run"]
