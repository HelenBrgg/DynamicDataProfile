Potenzial für zukünftige Arbeiten liegt in der Optimierung des Algorithmus und in der Erweiterung der Aufgabenstellung. 

## Optimierungen
Eine der wichtigsten Eigenschaften des Algorithmus sollte sein, dass er sehr schnell ist. Dies ist sehr wichtig um mit den dynamisch wachsenden Daten mitzukommen. So könnte man noch weitere Möglichkeiten suchen um den Algorithmus zu verschnellern und mehr Kandidaten auszuschließen.

### Logische Implikationen
Anhand von weiteren logischen Implikationen könnte man weitere Kandidaten ausschließen. Es wäre beispielsweise möglich das eigentliche Candidate Checking in mehrere Schritten auszuführen und jeweils nur einen Teil der Kandidaten zu überprüfen. Aus den Zwischenergebnissen können dann für die weiteren Kandidaten wieder vorher einige ausgeschlossen werden. Durch die logischen Implikationen:

Wenn A ⊂ B und B ⊂ C, => A ⊂ C.

So müssten noch weniger von dem teursten Kandidaten-Checking durchgeführt werden. 

### Parallelisierung
Um das Pruning zu verschnellern könnte man einige der Pruning Aufgaben auch bereits Parallelisieren, da sie nicht unbedingt aufeinander aufbauen. Hierbei ist wichtig, beim Speichern der zu prüfenden Kandidaten sorgfältig zu sein. Allerdings muss man prüfen ob es tatsächlich dadurch schneller wird. Vielleicht ist auch das Pipelineprinzip am schnellsten, weil alle Kandidaten, die in einem Schritt ausgeschlossen werden nicht mehr im nächsten geprüft werden müssen.

## Erweiterung der Aufgabenstellung
Zurzeit werden nur Unäre IND's geprüft. Man könnte den Algorithmus dahingehend erweitern, dass man auch N-Äre IND's findet. Also wenn mehrere Spalten und Zeilen in mehreren anderen Spalten und Zeilen enthalten sind. 





