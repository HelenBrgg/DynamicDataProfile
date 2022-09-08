# Zusammenfassung

Wir haben ein verteiltes System für das Finden von Inclusion Dependencies (INDs) in dynamischen Datensets implementiert.

Unsere Lösung ist insofern verteilt, als dass das Speichern von Tabellenwerten und das Prüfen von IND-Kandidaten auf mehrere Data-Nodes verteilt werden kann. Das Einlesen von Datensets und das Generieren von IND-Kandidaten ist auf den einzelnen Master-Node beschränkt. 

> Ergebnisse und Ausblick
> 
> * Können unary und nary Deps verteilt / multi-process
> * das sind unsere limits unter diesen beschränkungen
> * Entwicklung für Cluster

