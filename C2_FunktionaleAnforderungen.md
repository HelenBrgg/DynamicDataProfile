# Funktionale Anforderungen

Ragna

## Profiler

### Inkrementelle Entdeckung von Inclusion-Dependencies
* Unary und N-Nary
* Behandeln von Inserts und Deletes
* Behandeln von Updates (Insert + Delete)
* Zwischenspeichern von dynamischen Datensätzen

### Korrektheit

* Nachvollziehbare Beziehungen zwischen Inputs und Outputs (Batch-ID und Timestamps)

## Datengenerator

### Generierung von unendlichen Datensätzen aus einem natürlichen Datenkorpus
* Unendlich viele Zeilen durch Cycling
* Spaltenanzahl ist konfigurierbar
* Anzahl an Tabellen ist konfiguerierbar
* Korpus: CSV Dateien werden aus einem Ordner geladen
* Simulation von Löschung mit unterschiedlichen Häufigkeiten

### Batchgenerierung
* Batches werden generiert und per HTTP an den Profiler geschickt?


