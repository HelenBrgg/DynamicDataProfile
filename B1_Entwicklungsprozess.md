# Entwicklungsprozess

Felix über verteiltes system


Ragna über datengen

## Datengenerator:

> TODO (Felix)

1. Spezifikation des Output-Formats
2. Notwendige Funktionalitäten
3. Implementation (= kombinierbare Generatoren, die einzelne Funktionalitäten bieten)
4. Zuerst HTTP-client, dann in als Prozess in Java eingebunden (erlaubt Pull-Architektur)
5. alles gemacht in Pair Programming

Zu Beginn haben wir uns zunächst Gedanken darüber gemacht was der Datengenerator alles können muss, um einerseits der Aufgabenstellung gerecht zu werden und andererseits geeignete Datensätze für unser System zu liefern. 
Es war also wichtig das der Generator einen beliebig langen Datenfluss generieren und zwischendurch einzelne Zeilen löschen kann.

Vor der Implementierung des Generator haben wir uns die einzelnen Klassen überlegt und definiert was diese jeweils können müssen und was sie dafür brauchen. B
eim Implementieren selbst haben wir auf Pair-Programming gesetzt. Die Planung und das Programmieren des Datengenerators fand zu großen Teilen in unserer ersten gemeinsamen Blockwoche statt und wurde stetig verbessert und schlussendlich finalisiert.

Programmiert wurde der Generator in Python.


