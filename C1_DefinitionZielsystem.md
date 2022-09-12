# Definition des Zielsystems

## Inclusion Dependencies

Inclusion Dependencies beschreiben, ob alle Werte die ein Attribut $X$ annehmen kann auch von Attribut $Y$ angenommen werden können. $X$ und $Y$ können aus Instanzen des gleichen Schemas (= der gleichen Tabelle) stammen, oder auch aus Instanzen zwei verschiedenen Schematas (= verschiedener Tabellen).
Falls das der Fall ist, ist $X$ abhängig von $Y$ und man schreibt $X ⊆ Y$.

Formal bedeutet das: $∀ t_i[X] ∈ r_i, ∃ t_j[Y] ∈ r_j$ mit $t_i[X] = t_j[Y]$ wobei $t_i, t_j$ Schema-Instanzen sind und $X, Y$ Attribute der Schemata. 

Allgemein werden $X$ und $Y$ als Listen von Attributen gesehen, wobei stehts gelten muss $|X| = |Y|$. <br>
Es wird von *unary* Inclusion Dependencies gesprochen wenn gilt $X ⊆ Y$ mit $|X| = |Y| = 1$. Falls $|X| = |Y| = n$ gilt, handelt es sich um eine *n-ary* Inclusion Dependency.

Für Inclusion Dependencies gelten immer folgende Eigenschaften:

* *Reflexiv:* Es gilt immer $X ⊆ X$
* *Transitiv:* Es gilt $X ⊆ Y \wedge Y ⊆ Z \implies X ⊆ Z$
* *Permutationen:* Es gilt $(X_1, ... , X_n) ⊆ (Y_1, ... , Y_n)$, dann gilt auch $(X_1, ... , X_n) ⊆ (Y_1, ... , Y_n)$ für alle Permutationen σ1, ... , σn 

##### Beispiel für unary Inclusion Dependencies {-}

<p align="center">
  <img src="imgs/unary_IND_Example.jpg" width="500">
  <p align="center">
  TODO quelle! <br>
  X := Attribut "Name" aus Tabelle "Lending"<br>
  Y := Attribut "Titel" aus Tabelle "Book"<br>
  </p>
</p>


Es ist leicht zu sehen, dass alle Werte die "Name" annehmen kann auch in Attribut "Titel" vertreten sind, daher folgt $X ⊆ Y$. <br>
Es ist auch leicht zu sehen, dass $Y ⊆ X$ nicht gilt, da $Y$ den Wert "3D Computer Graphics" annehmen kann, dieser jedoch nicht in $X$ auftaucht.

##### Beispiel für n-ary Inclusion Dependencies {-}

<p align="center">
  <img src="imgs/n-ary_IND_Example.jpg" width="500">
  <p align="center">
  TODO quelle! <br>
  X := Attribute "Student" und "Course" aus Tabelle "Lending"<br>
  Y := Attribute "Name" und "Lecture" aus Tabelle "Student"<br>
  </p>
</p>

Bei n-ary Inclusion Dependencies ist es nicht nur wichtig das alle Werte der einzelnen Attribute aus $X$ in $Y$ auftauchen, sondern das sie vor allem in der Kombination in Y auftauchen, in der sie auch in X auftauchen.<br>
Auch hier ist wieder einfach zu sehen, dass $X ⊆ Y$ gilt, denn die drei unterschiedlichen Kombinationen aus "Student" und "Course" die in $X$ auftauchen sind auch alle in $Y$ vertreten. Das bedeutet also das hier ebenfalls $Y ⊆ X$ gelten würde.

## Datenformat

Ein Datenset besteht aus mehreren __Tabellen__, die unterschiedliche Schematas haben können. Diese Tabellen werden als Stream eingelesen und einzelne Einträge eines Streams (= Zeilen einer Tabelle) können ältere Einträge überschreiben.

<!--
Eine Anforderung an den Algorithmus ist, dass er _Tabellen aus ihren Batches rekonstruiert und updatet_. Dazu muss er eine Repräsentation der Tabellen über die gesamte Ausführung hinweg zwischenspeichern. 
-->

Wir konzipieren unseren Algorithmus so, dass er __Batches aus Änderungen__ einliest. Ein Batch wird immer aus genau einem Input-Stream entnommen und hat das gleiche Schema wie seine Ursprungstabelle, bis auf eine `$` Spalte am Anfang welche die _Position eines Eintrages_ beschreibt.

Änderungen lassen sich in drei Arten unterteilen:

1. Eine __Hinzufügung__ ist ein Änderung, deren Position das Erste mal im Stream auftaucht und bei der _alle Felder_ einen Wert haben.
2. Eine __Modifikation__ ist ein Änderung, deren Position bereits im Stream auftauchte und bei der _alle Felder_ einen Wert haben. Die Position muss dem Eintrag entsprechen, der überschrieben werden soll.
2. Eine __Löschung__ ist ein Änderung, deren Position bereits im Stream auftauchte und bei der _kein Feld_ einen Wert hat. Die Position muss dem Eintrag entsprechen, der gelöscht werden soll.

|$|A|B|C|
|-|--|--|--|
|200|horse|lion|flamingo|
|200|horse|lion|**parrot**|
|200||||

: Tabelle: Beispiel für eine Hinzufügung, Modifikation und Löschung eines Eintrags.

Wir definieren den leeren Zellenwert `NULL` als einen besonderen Marker, der die Abwesenheit eines Wertes beschreiben soll. Der `NULL` Marker muss immer von der Berechnung von Inclusion Dependencies ausgeschlossen werden - also ob ein Attribut fehlen kann oder nicht soll keine Auswirkung auf die gefundenen Inclusion Dependencies haben.

<!-- 
Während der Ausführung soll das System periodisch alle gefundenen Inclusion Dependencies ausgeben. Sollten nach dem Abarbeiten von Änderungen diese Inclusion Dependencies nicht mehr gelten, so soll das auch ausgegeben werden.
-->

## Datenfluss für inkrementelle Updates

Wir legen einen wiederholt-ausführbaren Datenfluss für inkrementelle Updates fest. Wir möchten pro eingelesenes Batch möglichst wenig Operationen durchführen. Die wohl teuerste Operation ist der _Subset-Check_ für Validieren eines IND-Kandidaten. Hierbei werden alle Werte zweier Attribute abgefragt und verglichen. Unser Ziel ist es also, möglichst wenige Subset-Checks durchzuführen.

![Datenfluss für inkrementelle Updates und dazugehörige Speicher](imgs/system-basic3.drawio.svg)

##### _Read Input_ {-}

Es wird ein Batches von einer Quelle eingelesen. Das Format von Batches ist in der Sektion [Datenformat](#datenformat) beschrieben.

##### _Write Array-wise_ {-}

Ein Batch wird nach seinen Attributen aufgespalten und für jedes Attribut werden die Werte in ein eigenes _Column-Array_ geschrieben. Ein Column-Array ist ein Array welches alle Werte eines Attributes an ihrer jeweiligen Positionen beinhaltet.

Anschließend werden die _Delta-Counts_ berechnet. Diese beschreiben, wie häufig ein Wert eines Attributes hinzugefügt oder entfernt wurde. Sollten alle Delta-Counts 0 sein, so haben die Änderungen des Batches definitiv keinen Einfluss auf Inclusion Dependencies und der Datenfluss kann vorzeitig enden.

##### _Write Set-wise_ {-}

Die Delta-Counts eines Attributs werden in das dem Attribut zugehörigen _Column-Set_ geschrieben.

##### _Update Metadata_ {-}

##### _Generate Candidates_ {-}

##### _Purge Candidates_ {-}

##### _Validate Candidates_ {-}

