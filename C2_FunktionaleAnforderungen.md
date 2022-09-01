# Funktionale Anforderungen

Ragna

- Überblick, z.B. mittels Use Case Diagramm
- Dokumentation aller funktionaler Anforderungen gemäß des gewählten Prozesses (Use Cases, User Stories, etc.)
- Prioritäten (zumindest Unterteilung in „notwendig“ / „optional“)

## Profiler

### Inkrementelle Entdeckung von Inclusion-Dependencies
* ~~Unary~~ und N-Nary
* ~~Behandeln von Inserts und Deletes / Behandeln von Updates (Insert + Delete)~~
* ~~Zwischenspeichern von dynamischen Datensätzen~~

### Korrektheit

* Nachvollziehbare Beziehungen zwischen Inputs und Outputs (Batch-ID und Timestamps)

## Datengenerator

### Generierung von unendlichen Datensätzen aus einem natürlichen Datenkorpus
* ~~Unendlich viele Zeilen durch Cycling~~
* Spaltenanzahl ist konfigurierbar
* Anzahl an Tabellen ist konfiguerierbar
* Korpus: CSV Dateien werden aus einem Ordner geladen
* ~~Simulation von Löschung mit unterschiedlichen Häufigkeiten~~

### Batchgenerierung
* Batches werden generiert und per HTTP an den Profiler geschickt?



<hr>
Der Datengenerator soll einen beliebig großen Batch einer beliebigen CSV Dateien genereiren. In diesem Batch sollen anschließend mittels des Dynamischen Algorithmuses Inclusion Dependencys gefunden und ausgegeben werden.

## Funktionale Anforderungen an den Datengenerator
Der Datengenerator soll...
- eine beliebige CSV-Datei einlesen können
- unendlich viele Zeilen durch Cycling generieren können
- eine bestimte Anzahl Zeilen generieren können
- bei der Generierung der Zeilen, wieder von vorne beginnen, sollte das Ende der CSV-Datei erreicht sein aber noch nicht die gewünschte Anzahl Zeilen
- jede Spalte mit einem eindeutigen Index versehen können
- eine Zeile mit Wahrscheinlichkeit x löschen können
- eine generierte Datei wieder als CSV-Datei ausgeben können


## Funktionale Anforderungen an das Verteilte System / den Dynamischer Algorithmus
Der Dynamische Algorithmus soll...
- Unary Inclusion Dependencies erkennen und ausgeben können
- Einfügungen und Löschungen sollen erkannt und entsprechend behandelt werden können
- dynamischen Datensätzen zwischenspeichern können
- auch mit großen dynamischen Datensätzen zurecht kommen können 
