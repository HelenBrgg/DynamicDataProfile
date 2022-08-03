# Beschreibung

## Projekt

NAME DES PROJEKTS: Dynamische Detektion von Inklusionsabhängigkeiten

STARTTERMIN: 24.10.2021

ENDTERMIN:

Projektteilnehmende: Felix Köpge, Ragna Solterbeck, Helen Brüggmann

### Die Ausgangslage

Im Status Quo sind die meisten Dataprofiling Algorithmen statisch. Sie untersuchen eine statische Datenmenge auf Abhängigkeiten, wie Funktionale Abhängigkeiten oder Inklusionsabhängigkeiten. Wenn die Daten sich aber ändern, so muss der Algorithmus auf der gesamten Datenmenge neu ausgeführt werden. Dies ist sehr Zeit- und Datenaufwendig.

### Projektvorhaben

Im Rahmen dieser Arbeit soll ein ein dynamischer Datenprofiling Algorithmus entwickelt werden, der Inklusionsabhängigkeiten auf dynamisch wachsenden Daten entdeckt. Er soll alle Inklusionsabhängigkeiten entdecken und beim Einfügen neuer Daten oder Löschen von Daten berechnen, ob dadurch Inklusionsabhängigkeiten aufgelöst werden oder Neue entstehen.

### Projektidee

Als Ansatz soll ein verteilter Algorithmus entstehen, der alle Änderungen registriert und prüft welche Kandidaten für neue Inklusions Abhängigkeiten entstanden sind, oder welche Inklusionsabhängigkeiten sich aufgelöst haben könnten. Pruningmethoden sollen vermeiden, dass auf der gesamten Datenmenge gesucht wird. Beispielsweise sollen durch Betrachten von Metadaten und durch logische Implikationen bereits viele Datenkombinationen ausgeschlossen werden. Somit soll der dynamische Algorithmus wesentlich schneller ablaufen als ein statischer Algorithmus.

## Auftraggeber

Die Arbeit ist im Rahmen einer Projektarbeit in der Arbeitsgruppe Big Data Analytics am Fachbereich Mathematik und Informatik der Philipss-Universität Marburg entstanden. Sie hat sich über zwei Semester erstreckt. Der Leitende Professor ist Prof. Thorsten Papenbrock.

## Qualitätsanforderungen

Felix

- (siehe DDM folien)
