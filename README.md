# Smart Momo Gym - Projekt Dokumentation

Dies ist eine Jakarta EE Trainings-Anwendung ("smart-momo-gym"), basierend auf WildFly, Jakarta EE 10 und Oracle DB.

## 📋 Überblick & Tech-Stack

* **Server:** WildFly (via Maven Plugin, keine lokale Installation nötig)
* **Datenbank:** Oracle 21c Express Edition (via Docker)
* **Java:** JDK 21
* **Build Tool:** Maven

---

## 🚀 Schnellstart (Runbook)

Wenn die Infrastruktur (Docker) bereits läuft, nutze diese Befehle zum Starten:

### 1. Java-Version setzen (PowerShell)
Da auf dem Arbeitsrechner oft Java 8 Standard ist, muss für dieses Projekt Java 21 erzwungen werden:
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
# Check:
& "$env:JAVA_HOME\bin\java.exe" -version
```

### 2. Server starten (mit Private Settings) um Konflikte mit Firmen-Repositories (Nexus/Artifactory) zu vermeiden, nutzen wir eine eigene Settings-Datei:

```powershell
mvn clean wildfly:run -s private-settings.xml
```

* Die Anwendung ist danach erreichbar unter: [http://localhost:8080/smart-momo-gym](http://localhost:8080/smart-momo-gym)

---

## 🛠 Infrastruktur Einrichtung (Einmalig) falls der Docker-Container gelöscht wurde oder das Projekt auf einem neuen Rechner eingerichtet wird.

### 1. Datenbank Container starten
Wir mappen den lokalen Port **1522** auf den Container-Port 1521, um Konflikte mit lokalen Oracle-Installationen zu vermeiden.

```powershell
docker run -d --name oracle-db -p 1522:1521 -e ORACLE_PASSWORD=oracle gvenzl/oracle-xe
```

*Warten, bis `docker logs oracle-db` meldet: "DATABASE IS READY TO USE!"*

### 2. Datenbank-User erstellen (SQL Setup)
Da wir eine Pluggable Database (PDB) nutzen, ist der Ablauf strikt einzuhalten.

1. **Einloggen als System:**
```powershell
docker exec -it oracle-db sqlplus system/oracle
```


2. **SQL-Befehle ausführen:**
```sql
-- WICHTIG: In die PDB wechseln! Sonst landet der User im falschen Container.
ALTER SESSION SET CONTAINER = XEPDB1;
-- Antwort muss sein: "Session altered."

-- User erstellen
CREATE USER momogym_user IDENTIFIED BY dbpass;

-- Rechte geben (löst ORA-01045)
GRANT CONNECT, RESOURCE TO momogym_user;

-- Speicherplatz zuweisen
ALTER USER momogym_user QUOTA UNLIMITED ON USERS;

-- Beenden
EXIT;
```



---

## 💻 Entwicklungsumgebung (IDE)
### IntelliJ Datenbank-Verbindung
Damit IntelliJ (DataGrip) auf die Datenbank zugreifen kann:

| Einstellung | Wert | Hinweis |
| --- | --- | --- |
| **Connection Type** | Service Name | **Nicht** SID wählen! |
| **Host** | localhost | (oder 127.0.0.1 bei Problemen) |
| **Port** | **1522** | Wir nutzen den weitergeleiteten Port! |
| **Service Name** | `XEPDB1` | Das ist der Name der PDB |
| **Authentication** | User & Password | Nicht Kerberos |
| **User** | `momogym_user` |  |
| **Password** | `dbpass` |  |
| **Driver** | Oracle (Thin) |  |

### Maven Konfiguration (Isolation)
Das Projekt nutzt eine `private-settings.xml` im Root-Ordner.

* **Zweck:** Umgeht die `settings.xml` der Firma.
* **Funktion:** Leitet alle Anfragen direkt an Maven Central (`repo.maven.apache.org`) und speichert Abhängigkeiten in einem isolierten Ordner (`.m2/repository-private`).

---

## ⚙️ WildFly Konfiguration (Infrastructure as Code)
Der Server wird **automatisch** beim Start durch die `pom.xml` konfiguriert.
Es ist keine manuelle Bearbeitung der `standalone.xml` nötig.

**Was passiert automatisch?**

1. Der Oracle JDBC Treiber wird als Modul installiert.
2. Die Datasource `java:/jdbc/OracleDS` wird registriert.
3. Verbindung zur DB unter `localhost:1522/XEPDB1` wird hergestellt.

---

## 📝 Wichtige Befehle
**Deployment manuell anstoßen:**

```powershell
mvn clean package wildfly:deploy -s private-settings.xml
```

*Terminal 2 (Zum Aktualisieren):*
Wenn du Code geändert hast, öffne ein zweites Terminal (oder Tab in IntelliJ) und führe aus:
```powershell
mvn package wildfly:deploy -s private-settings.xml
```
Was passiert da? Der Server in Terminal 1 bleibt an. Der Befehl in Terminal 2 baut nur deine .war Datei neu und schiebt sie in den laufenden Server. Das dauert meist nur wenige Sekunden statt Minuten.

**Debugging:**
```powershell
mvn clean wildfly:run "-Dwildfly.javaOpts=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8787" -s private-settings.xml

```

**Container Status prüfen:**

```powershell
docker ps -a
docker logs oracle-db
```

**Container Neustart (falls Connection refused):**

```powershell
docker restart oracle-db
```

## 🌍 Mobile Deployment (Via Ngrok)

Um die Anwendung auf dem Smartphone (z.B. im Fitnessstudio) zu nutzen, ohne einen Server zu mieten, "tunneln" wir den lokalen Port ins Internet.

**Voraussetzung:**
* Der Server muss lokal laufen (`mvn clean wildfly:run ...`).
* Ngrok muss installiert sein.

**1. Tunnel starten:**
Öffne ein **neues** Terminal-Fenster und führe aus:

```powershell
ngrok http 8080
```

**2. URL ablesen:**
Du siehst im Terminal eine Ausgabe wie:
`Forwarding https://abcd-1234-5678.ngrok-free.app -> http://localhost:8080`

**3. Auf dem Handy öffnen:**
Kopiere die URL und hänge **zwingend** den Projektnamen an:

`https://[DEINE-NGROK-ID].ngrok-free.app/smart-momo-gym/`

**Wichtig:**

* Das Terminal mit Ngrok darf **nicht geschlossen** werden, sonst bricht die Verbindung ab.
* Die URL ändert sich bei jedem Neustart von Ngrok (in der Free-Version).
* Beim ersten Aufruf auf dem Handy zeigt Ngrok eine Warnseite ("You are visiting..."). Klicke dort auf "Visit Site".
