# Offizielles WildFly Image nutzen
FROM quay.io/wildfly/wildfly:latest

# Dein kompiliertes .war File in den Deployment-Ordner des Servers kopieren
COPY target/smart-momo-gym.war /opt/jboss/wildfly/standalone/deployments/

# WildFly starten und für alle Netzwerke öffnen (0.0.0.0)
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0"]