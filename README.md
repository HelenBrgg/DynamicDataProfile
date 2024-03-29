# Benutzerdokumentation

## Datengenerator

Bevor das Datengenerator-Script ausgeführt werden kann muss der TPC-H Datensatz entpackt und die Abhängigkeiten installiert werden:

```
cd data && unzip TPCH.zip
pip install argparse
```

Das Script kann wie folgt ausgeführt werden:

```bash
scripts/datagenerator.py CSV_FILE [OPTIONS]
```

Wobei `CSV_FILE` der Pfad zu einer CSV-Datei sein muss. `OPTIONS` kann eine Kombination aus verschiedenen Parametern sein:

* (keine Parameter): Liest einen Datensatz einmal aus.
* `--repeat`: Wiederholt einen Datensatz unendlich oft.
* `--repeat --max-output 100`: Wiederholt einen Datensatz, bis 100MB an Daten ausgegeben wurden.
* `--delete 0.1`: Liest einen Datensatz einmal aus mit 10% Wahrscheinlichkeit, dass alte Zeilen gelöscht werden.
* `--repeat --mutate`: Wiederholt einen Datensatz unendlich oft mit Mutierungen nach jeder Wiederholung.
* `--batch-size 1`: Liest einen Datensatz einmal aus mit einer Batch-Größe von maximal 1KB (sonst: 64KB).

Weitere Optionen sind unter `./scripts/datagenerator.py --help` aufgelistet.

## Akka-System

Das Akka-System läuft unter Java 18. Maven wird für die Kompilation benötigt:

```bash
mvn package -f pom.xml
```

Das Akka-System lässt sie wie folgt ausführen:

```
java -Xmx8g -ea -cp target/ddm-akka-1.0.jar de.ddm.Main master [OPTIONS]
```

`OPTIONS` kann eine Kombination aus verschiedenen Parametern sein:

* (keine Parameter): In seiner Default-Konfiguration liest das Akka-System den TPC-H Datensatz einmal aus und berechnet INDs mit 8 lokalen Workern.
* `-ip data/example`: Verwendet den `data/example` Datensatz statt den TPC-H Datensatz.
* `-dg '--repeat --max-output 100'`: Führt das Datengenerator-Skript mit den Parametern `--repeat --max-output 100` aus (wiederholt Datensatz ausgeben bis 100MB an Output erreicht).
* `-w 16`: Berechnet die INDs mit 16 lokalen Workern.

Weitere Optionen sind unter `java -Xmx8g -ea -cp target/ddm-akka-1.0.jar de.ddm.Main --help` aufgelistet.

Eine Ausführung mit dem `data/example` Datensatz, mit 100MB Output pro CSV-Datei und 16 lokalen Workern sähe also so aus:

```
java -Xmx8g -ea -cp target/ddm-akka-1.0.jar de.ddm.Main master -ip data/example -dg '--repeat --max-output 100' -w 16
```

Das Akka-System beendet sich automatisch und gibt seine Ergebnisse in `live-results.csv` und `final-results.txt` aus.

# Entwicklerdokumentation

## Projektstruktur

Die `main` Branch enthält unsere CodeBase.

Die `doku` Branch enthält die schriftliche Ausarbeitung mit generierter PDF Datei.

* `scripts/`: Hier befindet sich derzeit nur das `datagenerator.py` Skript.
* `src/main/java/de/ddm/actors/profiling/`: Hier befinden sich alle unsere Aktoren für unser Aktoren-Protokoll.
* `src/main/java/de/ddm/structures`: Hier befinden sich alle unsere Klassen, die nicht direkt von Akka abhängig sind.
* `src/main/java/de/ddm/configuration`: Hier können die Kommandozeilen-Befehle des Akka-Systems angepasst werden.
* `ddm-akka/`: Hier befindet sich eine alte Version unserer Codebase, die für das Testen verwendet wird.
* `ddm-spark/`: Hier befindet sich eine Implementation des SINDY-Algorithmus in Spark.

## Testen

Wir testen die Ergebnisse mittels dem `run_tests.sh` Skript.

Dabei testen wir unserer neuen Codebase gegen die Ergebnisse einer alte Version unserer Codebase.  Diese alte Version befindet sich in der Unter-Repository `ddm-akka`.  Unsere Tests prüfen derzeit nur, ob alle INDs in einem statischen Datensatz korrekt gefunden wurde. Für dynamische Datensätze haben wir derzeit keine Tests.

Bei unserem Test werden die beiden Output-Dateies `final-results.txt` und `ddm-akka/results.txt` verglichen. Beiden Dateien werden mittels dem UNIX `diff` Tool auf Gleichheit geprüft: `sort ddm-akka/results.txt | diff final-results.csv -`. Bei gescheiterten Tests werden die nicht-gefunden und falsch-gefunden Einträge hervorgehoben.

