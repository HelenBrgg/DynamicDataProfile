# Detaillierter System-Entwurf

- Detail-Entwurf der Komponenten in UML-Diagrammen
    - Mindestens ein Klassendiagramm pro Komponente (mit Schwerpunkt auf den wichtigen Klassen wir persistenten Datenstrukturen und Services)
    - Für komplexere Abläufe bzw. komplexeres Verhalten jeweils ein Aktivitäts oder Zustandsdiagramm
- Entwurfsmuster
- Abhängigkeiten unter den Komponenten und zu externen Komponenten
- Datenmodell
- Verantwortlichkeiten der Entwickler


- allgemein Beschreiben wie der Datengenerator funktioniert
- für einzelen KOmponenten evtl. noch grapisch aufzeichnen und bisschen beschreiben was allgemein passiert

## Datengenerator
Der Datengenerator besteht aus den fünf Klassen CSVRowReader, RowRepeater, RowDeleter, Batcher und CSVReadIn. <br>

<p align="center">
  <img src="/imgs/Komponentenentwurf_Datagen_6.png" width="400">
  <p align="center">
  Beispielhafte Darstellung der einmaligen Ausführung des Datengenerators
  </p>
</p>

Dem Datengenerator wird die Adresse einer CSV-Datei und die Anzahl an Reihen die insgesamt ausgegeben werden sollen, übergeben. Der Generator nimmt diese CSV-Datei und generiert daraus einen Batch *(5)*. <br>
Dafür wird zunächchst die Datei eingelesen, wobei jede Spalte in ein Array das die Einträge der Spalte als String enthält, umgewandelt und Fortlaufend durchnummeriert *(1)*. <br>
Diese Zeilen-Arrays werden jetzt so lange von vorne nach hinten wiederholt bis die übergebene Anzahl an gewünschten Reihen erreicht ist *(2)*. Dabei wird vor hinzufügen jedes neuen Zeilen-Arrays, eine vorherige Zeile mit einer Wahrscheinlichkeit von 10% gelöscht. Dafür wird eine Array mit leerer Liste aber bekanntem Index hinzugefügt *(3)*. <br>
Wenn die Anzahl an gewünschten Reihen erreicht wurde, wird daraus der Batch generiert. Dafür wird jedes Array wieder in eine String umgewandelt und die einzelnen Attribut-Werte durch Kommas getrennt. Es wird also wieder eine CSV-Datei generiert *(4)*.
 

Verteiltes System (Felix)
