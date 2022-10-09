


## Weiterentwicklung

Potenzial für zukünftige Arbeiten liegt in der Optimierung des Algorithmus und in der Erweiterung der Aufgabenstellung. 

Eine der wichtigsten Eigenschaften des Algorithmus sollte sein, dass er sehr schnell ist. Dies ist sehr wichtig um mit den dynamisch wachsenden Daten mitzukommen. So könnte man noch weitere Möglichkeiten suchen um den Algorithmus zu verschnellern und mehr Kandidaten auszuschließen.


### Logische Implikationen

Anhand von weiteren logischen Implikationen könnte man weitere Kandidaten ausschließen. Es wäre beispielsweise möglich das eigentliche Candidate Checking in mehrere Schritten auszuführen und jeweils nur einen Teil der Kandidaten zu überprüfen. Aus den Zwischenergebnissen können dann für die weiteren Kandidaten wieder vorher einige ausgeschlossen werden. Durch die logischen Implikationen:

Wenn $A ⊂ B$ und $B ⊂ C$, dann $A ⊂ C$.

Wenn $A ⊂ B$ und $B ⊄ C$, dann $A ⊄ C$.


So müssten noch weniger von dem teursten Kandidaten-Checking durchgeführt werden. 


### Parallelisierung der Dateneinlese und -verteilung

In unserer derzeitigen [Akka Architektur](#single-host-akka-system) existiert nur eine einzige `InputWorker`-Instanz, der Daten einliest. Das bedeutet, es kann nur ein Host Daten einlesen. Es liessen sich zwar leicht weitere `InputWorker` hinzufügen, aber das macht keinen Sinn, solange man nur eine einzige `DataDistributor`-Instanz hat.

Der `DataDistributor` verteilt Input-Batches auf mehrere `DataWorker`. Alle Daten, die derzeit eingelesen werden, müssen an den `DataDistributor` gesendet werden. Das macht den `DataDistributor` zum Haupt-Bottleneck unseres Systems.

Das Erstellen mehrerer `DataDistributor`-Instanzen über mehrere Hosts hinweg wäre komplex. Einseits müssten sich die `DataDistributor`-Instanzen darüber abstimmen, wie die Daten auf mehrere `DataWorker` partitioniert werden sollen. Weiterhin müsst Inkonsistenzen verhindert werden, die durch die parallelen Abläufe entstehen können (siehe [Single-Host Akka System](#single-host-akka-system) und [Multi-Host Akka System](#multi-host-akka-system)):

- Out-of-order `NewBatchMessage`s und `MergeRequest`s können dazu führen, dass einzelne `DataWorker` den Merge durchführen, bevor sie die neuen Daten erhalten haben.
- Out-of-order `MergeRequest`s und `SubsetCheck`s können dazu führen, dass Subset-Checks auf einem teilweise veralteten Zustand durchgeführt werden.
- Out-of-order `MergeRequests`s und `SetQueryRequests`s können dazu führen, dass sich während subsequenten Queries sich die unterliegenden Daten ändern.

Derzeit sind diese Abläufe konsistent, eben weil nur eine einzige `DataDistributor`-Instanz eine zeitlich-geordnete Reihenfolge der Nachrichten erzwingt.

### Partitionierung anhand des initialen Batches

Das erste eingelesene Input-Batch kann auf Ähnlichkeiten der Spalten geprüft werden. Diese Information könnte dazu genutzt werden, die Attribute effizienter auf `DataWorker` zu partitionieren.

Attribute die sich sehr ähnlich sind müssen vermutlich später mit teuren Subset-Checks überprüft werden. Diese Subset-Checks sind bereits teuer, wenn ein `DataWorker` die Werte beider Attribute hat; aber noch umso teurer, wenn diese beiden Attribute unterschiedlichen `DataWorker`-Instanzen zugewiesen sind.

### Horizontale Partitionierung

Derzeit wird nur vertikale Partitionierung (spaltenweise Aufteilung) durchgeführt. Es könnte aber effizienter sein, horizontale Partitionierung (zeilenweise Aufteilung) durchzuführen. Man könnte auch beide Partitionierungen gleichzeit durchführen.

Wir haben zu Beginn versucht, gleichzeitig vertikal und horizontal zu partitionieren. Es hat den Code aber signifikant komplexer gemacht, weswegen wir am Ende einzig vertikale Partitionierung umgesetzt haben.

Die Wiedereinführung von horizontale Partitionierung liesse sich mit moderatem Aufwand im `DataDistributor` und `DataWorker` bewerkstelligen. Möchte man allerdings mehrere `DataDistributor` Instanzen laufen lassen (siehe [Parallelisierung der Dateneinlese und -verteilung](#parallelisierung-der-dateneinlese-und--verteilung)), könnte es großen Aufwand erfordern.

### Parallelisierung des Pruning

Um das Pruning zu verschnellern könnte man einige der Pruning Aufgaben auch bereits Parallelisieren, da sie nicht unbedingt aufeinander aufbauen. Hierbei ist wichtig, beim Speichern der zu prüfenden Kandidaten sorgfältig zu sein. Allerdings muss man prüfen ob es tatsächlich dadurch schneller wird. Vielleicht ist auch das Pipelineprinzip am schnellsten, weil alle Kandidaten, die in einem Schritt ausgeschlossen werden nicht mehr im Nächsten geprüft werden müssen.

## Erweiterung der Aufgabenstellung

Zurzeit werden nur unary INDs geprüft. Man könnte den Algorithmus dahingehend erweitern, dass man auch nach n-ary INDs sucht (siehe [Inclusion Dependencies](#inclusion-dependencies)). 





