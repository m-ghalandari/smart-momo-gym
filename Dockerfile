# Offizielles WildFly Image nutzen
FROM quay.io/wildfly/wildfly:latest

# Kopiere dein .war File und nenne es ROOT.war (macht es zur Hauptseite!)
COPY target/smart-momo-gym.war /opt/jboss/wildfly/standalone/deployments/ROOT.war

# WildFly starten und für alle Netzwerke öffnen (0.0.0.0)
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0"]