# Inclusion Dependencies

Die Frage die beim finden von Inclusion Dependencies geklärt werden muss ist, ob alle Werte die ein Attribut $X$ annehmen kann auch von Attribut $Y$ angenommen werden können. $X$ und $Y$ können aus Instanzen des gleichen Schemas (= der gleichen Tabelle) stammen, oder auch aus Instanzen zwei verschiedenen Schematas.<br>
Falls das der Fall ist, ist $X$ abhängig von $Y$ und man schreibt $X ⊆ Y$.

Formal bedeutet das: $∀ t_i[X] ∈ r_i, ∃ t_j[Y] ∈ r_j$ mit $t_i[X] = t_j[Y]$ wobei $t_i, t_j$ Schema-Instanzen sind und $X, Y$ Attribute der Schemata.

Allgemein werden $X$ und $Y$ als Listen von Attributen gesehen, wobei stehts gelten muss $|X| = |Y|$. <br>
Es wird von *unary* Inclusion Dependencies gesprochen wenn gilt $X ⊆ Y$ mit $|X| = |Y| = 1$. Falls $|X| = |Y| = n$ gilt, handelt es sich um eine *n-ary* Inclusion Dependency.

Für Inclusion Dependencies gelten immer folgende Eigenschaften:
* **Reflexiv:** Es gilt immer $X ⊆ X$
* **Transitiv:** Es gilt $X ⊆ Y /wedge Y ⊆ Z \implies X ⊆ Z$
* **Permutationen:** Es gilt $(X_1, ... , X_n) ⊆ (Y_1, ... , Y_n)$, dann gilt auch $(X_1, ... , X_n) ⊆ (Y_1, ... , Y_n)$ für alle Permutationen σ1, ... , σn 
<br><br>
## Beispiel:
***unary* Inclusion Dependencies:**
<p align="center">
  <img src="/imgs/unary_IND_Example.jpg" width="400">
  <p align="center">
  X := Attribut "Name" aus Tabelle "Lending"<br>
  Y := Attribut "Titel" aus Tabelle "Book"<br>
  </p>
</p>

Es ist leicht zu sehen, dass alle Werte die "Name" annehmen kann auch in Attribut "Titel" vertreten sind, daher folgt $X ⊆ Y$. <br>
Es ist auch leicht zu sehen, dass $Y ⊆ X$ nicht gilt, da $Y$ den Wert "3D Computer Graphics" annehmen kann, dieser jedoch nicht in $X$ auftaucht.

***n-ary* Inclusion Dependencies:**
<p align="center">
  <img src="/imgs/n-ary_IND_Example.jpg" width="400">
  <p align="center">
  X := Attribute "Student" und "Course" aus Tabelle "Lending"<br>
  Y := Attribute "Name" und "Lecture" aus Tabelle "Student"<br>
  </p>
</p>

Bei n-ary Inclusion Dependencies ist es nicht nur wichtig das alle Werte der einzelnen Attribute aus $X$ in $Y$ auftauchen, sondern das sie vor allem in der Kombination in Y auftauchen, in der sie auch in X auftauchen.<br>
Auch hier ist wieder einfach zu sehen, dass $X ⊆ Y$ gilt, denn die drei unterschiedlichen Kombinationen aus "Student" und "Course" die in $X$ auftauchen sind auch alle in $Y$ vertreten. Das bedeutet also das hier ebenfalls $Y ⊆ X$ gelten würde.
