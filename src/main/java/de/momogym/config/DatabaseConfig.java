/**
 * KONFIGURATION FÜR DIE DATENBANKVERBINDUNG (NEON)
 * * Verwendung:
 * Diese Klasse definiert die Jakarta EE DataSource für die gesamte Anwendung.
 * Über den JNDI-Namen "java:app/jdbc/NeonDS" kann die Anwendung (z.B. in der persistence.xml)
 * auf die Neon-Datenbank zugreifen.
 * * SICHERHEITSHINWEIS:
 * Diese Datei enthält sensible Zugangsdaten im Klartext und wurde daher in die .gitignore
 * aufgenommen, damit sie niemals in das Git-Repository gepusht wird.
 * * TODO:
 * Eine sicherere Lösung implementieren. Aktuell werden Umgebungsvariablen vom
 * Wildfly-Maven-Plugin in dieser lokalen Umgebung nicht korrekt aufgelöst.
 * Ziel: Umstellung auf Umgebungsvariablen (${env.VAR}) oder externe
 * Konfigurationsdateien, um Hardcoding komplett zu vermeiden.
 */

package de.momogym.config;

import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.ejb.Singleton;

@Singleton
@DataSourceDefinition(
	name = "java:app/jdbc/NeonDS",
	className = "org.postgresql.ds.PGSimpleDataSource",
	url = "${env.DB_URL}",
	user = "${env.DB_USER}",
	password = "${env.DB_PASS}"
)
public class DatabaseConfig { }