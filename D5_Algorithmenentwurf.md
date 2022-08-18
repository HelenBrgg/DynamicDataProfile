# Algorithmenentwurf

![](imgs/Algorithmenentwurf.png)

Die Inserts, Updates und Deletes werden zunächst gespeichert.

## Pruning Pipeline

In der Pruningphase sollen durch Vorarbeit viele mögliche Permutationen für Inclusion Dependencies ausgeschlossen werden. Anstatt also, dass auf der gesamten Datenmenge nach Inclusion Dependencies gesucht wird, wird nur in den Attributen gesucht, in denen eine Abhängigkeit überhaupt in Frage kommt.

(Im Status Quo suchen wir lediglich nach unären Inclusion Dependencies. Als Fortführung könnte man nach n-ären Inclusion Dependencies suchen.)

In einer Pipeline werden nacheinander durch verschiedene Prüfungen Permutationen ausgeschlossen.

### Pruning durch logische Implikation

Durch logische Implikationen können Permutationen ausgeschlossen werden. Dafür werden zum Teil in vorherigen Iterationen Metadaten zu Permutationen gespeichert.
Die Logischen Implikationen sind zum Beispiel:

### Pruning durch Metadata

Aus den Metadaten der Attribute kann man auch Permutationen ausschließen. Durch eine Single Column Analysis erhalten wir verschieden Metadaten.

<table>
<tr><th>Tabelle 1  </th><th>Tabelle 2 </th></tr>
<tr><td>

|   A | B       | C            |
| --: | :------ | :----------- |
|   1 | Mars    | Luxemburg    |
|   2 | Jupiter | Singapur     |
|   3 | Jupiter | Lichtenstein |
|   4 | Luna    | Singapur     |

</td><td>

|   X | Y    | Z      |
| --: | :--- | :----- |
|  10 | Mars | Berlin |
|  20 | Mars | Berlin |
|  30 | Luna | Berlin |

</td></tr> </table>

|              Metadata | Charakterisierung                                                        | Beispiel                                                 |
| --------------------: | :----------------------------------------------------------------------- | :------------------------------------------------------- |
|            `num_rows` | Anzahl der Zeilen in einer Spalte                                        | num_rows(A) = 4<br>num_rows(X) = 3                       |
| `num_distinct_values` | Anzahl aller unterschiedlichen Werte, die in einer Spalte vorkommen      | num_distinct_values(B) = 3<br>num_distinct_values(Y) = 2 |
|          `uniqueness` | Anzahl der Zeilen dividiert durch die Anzahl der unterschiedlichen Werte | uniqueness(B) = 0.75<br>uniqueness(Y) = 0.6666           |

Innerhalb einer Tabelle ist num_rows für jede Spalte gleich. Über Tabellen hinweg darf es verschieden sein.

Eine `uniqueness` von `1.0` bedeutet, alle Werte einer Spalte sind unterschiedlich.

Eine `uniqueness` von `0.0` bedeutet, alle Werte einer Spalte sind gleich.

Data Patterns

|                                     Metadata | Charakterisierung                                                                                       | Beispiel                                                                                                         |
| -------------------------------------------: | :------------------------------------------------------------------------------------------------------ | :--------------------------------------------------------------------------------------------------------------- |
|                                   `datatype` | Gemeinsamer Datentyp für alle Werte einer Spalte                                                        | datatype(A) = UnsignedInteger <br>datatype(B) = String                                                           |
|         `highest_number` <br>`lowest_number` | Höchster Zahlenwert einer Spalte <br> Niedrigster Zahlenwert einer Spalte                               | highest_number(X) = 30 <br> lowest_number(X) = 10                                                                |
| `max_string_length` <br> `min_string_length` | Maximale Länge eines Werts betrachtet als String <br> Minimale Länge eines Werts betrachtet als String. | max_string_length(A) = 1 <br> min_string_length(A) = 1 <br>max_string_length(B) = 7<br> min_string_length(B) = 4 |

Mögliche Datentypen:

- `UnsignedInteger`: 1, 2, 42, 35666

- `Integer`: -10, 0, 10, 20000

- `Real`: 1, 2.0, -1.0e-7

- `Timestamp`: z.B. 2012-12-01 10:00:30

- `String`: the above and anything else, including this sentence

Datentypen können andere Datentypen enthalten:

`UnsignedInteger ⊂ Integer ⊂ Real ⊂ String`
`Timestamp ⊂ String`
(diese Datentypen sind nur ein Vorschlag - cool wäre es, wenn wir eine Auswahl hätten, die wir auch anhand von Papers/Statistiken begründen können! Wir könnten auch Charakterklassen betrachten, z.B. Numeric, Alphabetic, ASCII, Unicode…)

#### Kardinalitäten

|     A     |   B   |
| :-------: | :---: |
| chihuahua |  dog  |
| chihuahua |  dog  |
| dropbear  | horse |
| elephant  |  cat  |
|  dugong   |  cat  |

`num_distinct_values`(A)=5
`num_distinct_values`(B)=3
=> A ⊄ B

Wenn A mehr einzigartige Werte als B hat, dann kann A nicht in B enthalten sein. Somit muss eine Inclusion Dependency von A in B nur überprüft werden, wenn `num_distinct_values`(A)<`num_distinct_values`(B) oder `num_distinct_values`(A)=`num_distinct_values`(B). Nicht aber wenn `num_distinct_values`(A)>`num_distinct_values`(B).
Wenn A keine Inclusion Dependency von B: A erhält ein neues Input und B bleibt gleich, dann kann A immer noch kein Inclusion Dependency sein.

#### Min-/Max-Werte

Für die Extremwerte in einem Attribut kann man überprüfen ob eine Inclusion Dependency besteht.

Wenn der Maxwert von A größer ist als der Maxwert von B, so enthält A Werte die es nicht in B gibt, also kann A nicht in B enthalten sein, B aber in A.

Wenn der Minwert in A kleiner ist als in B, kann A nicht in B enthalten sein, B aber in A.

Somit können bei allen Kombinationen von Inclusion Dependencies die Min- und Maxwerte überprüft werden.

#### Datentyp

### Pruning durch Sketches

#### Bloom Filter

Ein weiterer Ausschluss findet durch Nutzung von Bloom Filtern statt. Genutzt wird ein Counting-Bloomfilter mit einer Größe von 128 und zwei Hash-Funktionen.

Bloomfilter sind eine probabilistische Datenstruktur, die Daten repräsentieren. Ein Bloom Filter ist ein Array aus `m` Bits, die ein Set aus `n` Elementen repräsentiert. ZU Beginn sind alle Bits auf `0`. Für jedes Element im Set werden nun `k` Hashfunktionen ausgeführt, die ein Element auf eine Nummer zwischen `1` bis `m` mappen. Jede dieser Positionen im Array werden dann auf `1` gesetzt. Will man nun prüfen ob ein Element in einer Datenmenge enthalten ist, kann man die Werte berechnen und prüfen ob die Positionen auf `1` sind. Wegen Kollisionen kann das Verfahren zu False Positives führen, allerdings nicht zu False Negatives. Wenn ein Element im Array `0` ist, so wurde der Wert definitiv noch nicht gesehen.

Counting Bloomfilter ergänzen Bloomfilter dahingehend, dass nun mitgezählt wie oft ein Bit im Array auf `1` gesetzt wird. Das ermöglicht auch Elemente zu löschen.

https://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.457.4228&rep=rep1&type=pdf

### Kandidaten üperprüfen

Nachdem wir in der Vorarbeit die Anzahl an Attributen, die wir auf Inclusion Dependencies überprüfen so weit wie möglich reduziert haben, werden nun die übrig gebliebenen Permutationen überprüft. Erst jetzt werden die gespeicherten Tabellen abgerufen um die relevanten Spalten miteinander zu vergleichen.

Hierbei betreiben wir ebenfalls eine Optimierung. Wenn eine gewisse Anzahl an Werten in beiden Attributen untersucht wurde, und die Anzahl verbliebener Werte nicht mehr ausreicht um noch eine Inclusion Dependency zu ergebn, brechen wir ab.

Beispiel:

> A hat 100 einzigarte Werte, B hat 80 einzigartige Werte: Wenn in den ersten 21 Werten von A kein einziger Wert von B auftaucht, so kann B nicht mehr vollständig in A enthalten sein. Hier kann bereits abgebrochen werden.
