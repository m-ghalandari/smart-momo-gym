FROM quay.io/wildfly/wildfly:latest

COPY target/smart-momo-gym.war /opt/jboss/wildfly/standalone/deployments/ROOT.war

CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-Djboss.annotation.property.replacement=true"]