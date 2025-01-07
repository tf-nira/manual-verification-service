# Base image with OpenJDK 11
FROM openjdk:11

# Metadata arguments (passed during build)
ARG SOURCE
ARG COMMIT_HASH
ARG COMMIT_ID
ARG BUILD_TIME

# Application configuration arguments (passed during build)
ARG spring_config_label
ARG active_profile
ARG spring_config_url
ARG is_glowroot
ARG artifactory_url

# Container runtime user configuration
ARG container_user=mosip
ARG container_user_group=mosip
ARG container_user_uid=1001
ARG container_user_gid=1001

# Labels for build metadata
LABEL source=${SOURCE}
LABEL commit_hash=${COMMIT_HASH}
LABEL commit_id=${COMMIT_ID}
LABEL build_time=${BUILD_TIME}

# Set environment variables for runtime
ENV active_profile_env=${active_profile} \
    spring_config_label_env=${spring_config_label} \
    spring_config_url_env=${spring_config_url} \
    is_glowroot_env=${is_glowroot} \
    artifactory_url_env=${artifactory_url} \
    iam_adapter_url_env="" \
    work_dir=/home/${container_user} \
    loader_path=/home/${container_user}/additional_jars

# Install necessary tools, create user and working directories
RUN apt-get update && apt-get install -y --no-install-recommends unzip \
    && groupadd -g ${container_user_gid} ${container_user_group} \
    && useradd -u ${container_user_uid} -g ${container_user_group} -s /bin/sh -m ${container_user} \
    && mkdir -p ${loader_path} \
    && chown -R ${container_user}:${container_user} ${work_dir} \
    && rm -rf /var/lib/apt/lists/*

# Switch to the container user
USER ${container_user_uid}:${container_user_gid}

# Set the working directory
WORKDIR ${work_dir}

# Copy application JAR
COPY ./target/manual-verification-service-*.jar manual-verification-service.jar

# Expose application port
EXPOSE 9002

# Command to start the service
CMD if [ "$is_glowroot_env" = "present" ]; then \
        echo "Enabling Glowroot monitoring..."; \
        wget -q "${artifactory_url_env}/artifactory/libs-release-local/io/mosip/testing/glowroot.zip" -O glowroot.zip && \
        unzip -q glowroot.zip && \
        rm -f glowroot.zip && \
        sed -i 's/manual-verification-service/g' glowroot/glowroot.properties; \
    fi && \
    echo "Downloading IAM Adapter JAR..."; \
    wget -q "${iam_adapter_url_env}" -O "${loader_path}/kernel-auth-adapter.jar" && \
    echo "Starting manual-verification-service..."; \
    java -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:MaxRAMFraction=1 \
        -XX:+HeapDumpOnOutOfMemoryError -XX:+UseG1GC -XX:+UseStringDeduplication \
        -Dloader.path="${loader_path}" \
        ${is_glowroot_env:+-javaagent:glowroot/glowroot.jar} \
        -Dspring.cloud.config.label="${spring_config_label_env}" \
        -Dspring.profiles.active="${active_profile_env}" \
        -Dspring.cloud.config.uri="${spring_config_url_env}" \
        -jar manual-verification-service.jar
