# Qpid cpp broker
FROM centos:centos7

# Add qpidd group / user
RUN groupadd -r qpidd && useradd -r -d /var/lib/qpidd -m -g qpidd qpidd

# Install all dependencies
RUN yum -y install epel-release \
        && yum -y install qpid-cpp-server qpid-tools

RUN chown -R qpidd:qpidd /var/lib/qpidd
VOLUME /var/lib/qpidd

# Add entrypoint
ADD entrypoint.sh /
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]

USER qpidd:qpidd
