# Detaillierter System-Entwurf

- Detail-Entwurf der Komponenten in UML-Diagrammen
    - Mindestens ein Klassendiagramm pro Komponente (mit Schwerpunkt auf den wichtigen Klassen wir persistenten Datenstrukturen und Services)
    - Für komplexere Abläufe bzw. komplexeres Verhalten jeweils ein Aktivitäts oder Zustandsdiagramm
- Entwurfsmuster
- Abhängigkeiten unter den Komponenten und zu externen Komponenten
- Datenmodell
- Verantwortlichkeiten der Entwickler

## Datengenerator
Der Datengenerator besteht aus den fünf Klassen CSVRowReader, RowRepeater, RowDeleter, Batcher und CSVReadIn. <br>
- allgemein Beschreiben wie der Datengenerator funktioniert
- für einzelen KOmponenten evtl. noch grapisch aufzeichnen und bisschen beschreiben was allgemein passiert

**CSVRowReader:**
- nimmt eine CSV Datei und speichert sie in "reader" ab
- ließt erste Zeile / Attribut-Zeile von "reader" ein und gibt sie mit Index "$" (zum wiedererkennen für Algo, das es sich hierbei um Attribut-Namen handelt) zurück
- ließt jede Zeile vom "reader" einzeln ein und gibt sie  zusammen mit einem Index zurück
- wenn es keien neue Zeile mehr gibt wird "None" zurückgegeben
- setzt "reader" und Index zurück
**RowRepeater:**
- ist eine Inner-Klasse vom CSVRowReader
- bekommt eine maximale Anzahl Reihen (= max_row) übergeben
- um Attribut-Zeile zu bekommen, wird die attribute Methode des CSVRowReader aufgerufen
- die nextRow Methode vom CSVRowReader wird solange aufgerufen, wie die anzahl verliebener Reihen (= remaining_rows) (zu anfang gleich max_row) undgleich 0 ist
- wenn die nextRow Methode vom CSVRowReader ein "None" zurück gibt (aber die anzahl der verbliebenen Reihen noch ungleich 0 ist) wird die reset Methode vom CSVRowReader aufgerufen und die nextRow Methode vom CSVRowReader wird erneut aufgerufen
- der Index jeder Zeile wird auf (max_row - remaining_rows - 1) gesetzt
- zurückseten der Klasse, remaining_rows = max_rows und aufrufen der reset Methode vom CSVRowReader
**RowDeleter:**
- bekommt einen row_generator und eine delete_chance übergeben
- max_index wird auf 0 gesetzt
- ruft die attributes Methode vom row_generator auf
- wenn eine Zufällige Zahl zwischen 0 und 1 kleiner ist als die delete_chance wird eine zufällige Reihe ersetzt durch Reihe mit gleichem Index und so vielen leeren Strings wie es Attribute gibt
- wenn die Zufällige Zahl größer ist, wird die nextRow Methode vom row_generator aufgerufen und die Reihe zurück zurückgegeben
- zurücksetzten der Klasse mit aufrufen der reset Methode vom row_generator
**Batcher:**
- bekoomt einen row_generator und eine maximale Batch Größe (=max_batch_size) übergeben
- zunächst ist der Batch ein leerer String
- in attributes werden die Attribut-Namen des row_generator gespeichert in dem die attributes Methode des row_generator aufgerufen wird
- die Attribut-Namen werden in attr_string durch ein Komme getrennt gespeichert (d.h. Attribut1,Attribut2,Attribut3 usw)
- solange die Länge vom Batch kleiner als die maximale Batch Größe ist, wird in row die nextRow Methode vom row_generator gespeichert
- solange row nicht "None" ist wird row_string die Werte mit Komme getrennt gespeichert (d.h. Wert1,Wert2,Wert3 usw)
- in batch wird der aktuelle Wert von batch +  Zeilenumbruch + row_dtring gespeichert
- wenn der Batch leer bleibt das wird "None" zurück gegeben
- am Ende attr_string + batch zurück gegeben
**CSVReadIn:**
- dem Skript wird mit sys.argv die Kommandozeilen-Eingabe übergeben
- 

Verteiltes System (Felix)
