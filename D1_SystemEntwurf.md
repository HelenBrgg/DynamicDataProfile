# System-Entwurf

## Akka System (Datenfluss)

Bevor wir das System mit Akka Aktoren beschreiben, definieren wir den grundlegenden Datenfluss den wir umsetzen möchten.
Dieser Datenfluss muss wiederholt-ausführbar sein und mit inkrementellen Updates (Batches) arbeiten.

Wir möchten pro eingelesenes Batch möglichst wenig Operationen durchführen. Die wohl teuerste Operation ist der _Subset-Check_ für Validieren eines IND-Kandidaten. Hierbei werden alle Werte zweier Attribute abgefragt und verglichen.
 
Unser Ziel ist es also einen Datenfluss zu definieren, der es uns erlaubt möglichst wenige Subset-Checks (oder andere teure Operationen) durchzuführen.

![Datenfluss für inkrementelle Updates und dazugehörige Speicher](imgs/system-flow.drawio.svg)

\ 

##### _1. Read Input_ {-}

Es wird ein Batches von einer Quelle eingelesen. Das Format von Batches ist in der Sektion [Datenformat](#datenformat) beschrieben.

##### _2. Write Array-wise_ {-}

Ein Batch wird nach seinen Attributen aufgespalten und für jedes Attribut werden die Werte in ein eigenes _Column-Array_ geschrieben. Ein Column-Array ist ein Array welches alle Werte eines Attributes an ihrer jeweiligen Positionen beinhaltet.

Anschließend werden die _Delta-Counts_ berechnet. Diese beschreiben, wie häufig ein Wert eines Attributes hinzugefügt oder entfernt wurde.

Sollten alle Delta-Counts $0$ sein, so haben die Änderungen des Batches definitiv keinen Einfluss auf Inclusion Dependencies und der Datenfluss kann vorzeitig enden.

##### _3. Write Set-wise_ {-}

Die Delta-Counts eines Attributs werden in das dem Attribut zugehörigen _Column-Set_ geschrieben. Ein Column-Set ist ein zählendes Set, welches mitzählt wie häufig eine Ausprägung eines Wertes in einem Column-Array auftaucht.

Beim Schreiben der Delta-Counts wird ein _Set-Diff_ erstellt. Dieses beschreibt, ähnlich dem Diff-Format des populären `diff` UNIX Tools, welche neuen Ausprägungen hinzugefügt oder entfernt wurden. 

Fällt der Zähler von $>0$ auf $0$, so können wir feststellen, dass eine Ausprägung nicht mehr vorkommt (_entfernt wurde_). Gab es vorher keinen Zähler oder steigt der Zähler von $0$ auf $>0$, so so können wir feststellen, dass eine neue Ausprägung hinzugefügt wurde.

Sollten alle Set-Diffs leer sein - also keine Ausprägungen hinzugefügt oder verändert worden sein - so haben die Änderungen keinen Einfluss auf die INDs und der Datenfluss kann vorzeitig enden.

##### _4. Update Metadata_ {-}

Die Set-Diffs werden benutzt, um _Metadata_ der dazugehörigen Attribute zu erstellen und zu aktualisieren.

<!--
Pro Attribut gibt es verschiedene Arten von Metadata:

* Die _Cardinality_ beschreibt die Anzahl der Ausprägungen (also die Größe des Column-Sets).
* Die _Extrema_ beschrieben die Minimum- und Maximum-Werte eines Attributes, nach lexikographischer Ordnung.
* Der _Bloomfilter_ ein probabilistischer Sketch der Ausprägungen.
-->

Mehr zu den verschiedenen Arten von Metadata im Kapitel (TODO verlinkte).

##### _5. Generate Candidates_ {-}

Die Set-Diffs werden benutzt, um die _Candidate-Data_ aller involvierten Attribute zu erstellen und zu aktualisieren.

Für alle neuen Attribute, die bisher nicht vorkamen, werden alle möglichen neuen (unären) Kandidaten generiert.  Bereits-existierende Inclusion Dependencies, die sich geändert haben könnten, werden zurückgesetzt und neue Kandidaten generiert. 

##### _6. Purge Candidates_ {-}

Die generierten Kandidaten werden anhand von _Subset-Prechecks_ gefiltert. Ein Kandidat $A ⊂ B$ wird nur weiter verwendet, wenn die Metadata von A und B diese Subset-Relation erlaubt. 

Mehr zu den verschiedenen Arten von Metadata im Kapitel (TODO verlinkte).

##### _7. Validate Candidates_ {-}

Die verbliebenen Kandidaten werden anhand von _Subset-Checks_ validiert. Dabei müssen die Werte aus mehreren Column-Sets verglichen werden.

Die Ergebnisse werden anschließend in der Candidate-Data gespeichert und für subsequente Candidate-Generation benutzt.

## Akka System (1-Host)

![Kommunikationsdiagramm für die versimpelte 1-Host Architektur](imgs/system-simple.svg)


TODO akka hierarchie beschreiben

## Akka System (n-Hosts)

![Kommunikationsdiagramm für die verteilte n-Hosts Architektur](imgs/system-complex.svg)

TODO akka hierarchie beschreiben

<!--

## Value Representation

### Hashing Long Values

Für lange Values kann stattdessen nur ein Hash gespeichert werden. Dadurch wird Speicher und Netzwerklast eingespart.

```
"foo" => "foo"
"bar" => "bar"
"Lorem ipsum {...}" => $124$cb24d439cebabab24
```

Indem wir mit dem Hash die Quell-Länge speichern (`${LEN}${HASH}`), erhöhen wir die Kollisionsresistenz noch ein wenig. Weiter könnte die Länge noch für die Single-Column-Analysis hilfreich sein. 

### Faster Hash Algorithm

Java's Builtin Hashing (4 byte) ist ob der hohen Kollisionsgefahr ungeeignet für Datenmengen unserer Größe.

Neben Algorithmen der SHA-Familie könnten wir auch [xxHash](https://github.com/Cyan4973/xxHash) oder [MurmurHash](https://en.wikipedia.org/wiki/MurmurHash) verwenden.

### Byte Array Values

Statt Java's Builtin `String` Klasse, die mit ihren eigenen Problemen kommt (potentiell UTF-16 sowie Klassenoverhead), können wir Values im UTF-8 Format als `byte[]` behandeln.

## Smart Candidate Generation

### Elimination-by-Implication

Wenn bereits Kandidaten geprüft wurden, können die Ergebnisse genutzt werden, andere Kandidaten direkt auszuschließen.

```
A c B  /\    B c D  ->   A c D
A c B  /\  !(A c D) -> !(B c D)
```

### Candidate Picking

Statt dass sofort alle Kandidaten generiert und geprüft werden, wird nur eine bestimmte Anzahl von Candidaten generiert, um von den Prüfungs-Ergebnissen nutzen zu machen.

Die gewählten Kandidaten können zufällig sein oder bewusst gewählt, um die potentielle Nützlichkeit der Ergebnisse zu erhöhen. 

Im Idealfall könnten z.B. drei Candidate-Checks zwischen vier Attributen dazu führen, dass man drei andere Candidate-Checks eliminieren kann. 

![](imgs/ideal-implication.drawio.svg)

### Candidate Flagging

Nicht immer, wenn sich ein Column-Set verändert hat, müssen alle assoziierte Candidate-Checks neu ausgeführt werden. 

* Counterexamples

## Single-Column-Analysis Prechecking

Wenn wir bestimmte Eingenschaften einer Column kennen, können wir für einen Candidate-Check vorzeitig ein True-Negative zurückliefern.

* Distinct Value Count
* Datatype (Data Domain)
* Bloomfilter
* Minima/Maxima
* Column-Bytesum

Fraglich ist, wo dieser Filter angebracht werden sollte - vor oder nach der Candidate-Generation. Davor: Candidaten können früher eliminiert werden. Danach: Möglicherweise kostenspielig bei sehr vielen Attributen.

## Optimierte Subset-Checks

### Dirty-Ranges

Beim verändern von Werten eines Sets können dynamische Dirty-Ranges eingesetzt werden. 

... (ähnlich wie Dirty-Flag, aber für eine Range)

### Early-Return

Basierend auf den Distinct Value Counts kann die Iteration eines Subset-Check frühzeitig abgebrochen werden.

### Bidirectional Check

Wenn `A c B` geprüft wird, können wir bei bedarf auch direkt `B c A` in einer Iteration prüfen.

-->
