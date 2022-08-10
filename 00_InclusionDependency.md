# Inclusion Dependencies

Inclusion Dependencies beschreben, ob alle Werte die ein Attribut $X$ annehmen kann auch von Attribut $Y$ angenommen werden können. $X$ und $Y$ können aus Instanzen des gleichen Schemas (= in der gleichen Tabelle) stammen, oder auch aus Instanzen zwei verschiedenen Schematas.

Falls das der Fall ist, ist X abhängig von Y und man schreibt $X ⊆ Y$.

Formal sagt man: $\forall t_i[X] ∈ r_i\ \ \exists t_j [Y] ∈ r_j\ \ \text{mit}\ \ t_i[X] = t_j[Y]$, wobei $t_i, t_j$ Schema-Instanzen (Tabellen) sind und $X, Y$ Attribute des Schemas / der Schemata.

Allgemein werden X und Y als Tupel von Attributen gesehen, wobei stets gelten muss $|X| = |Y|$ (d.h. wenn X ein 2-er Tupel ist, muss auch Y ein 2-er Tupel sein damit man sie vergleichen kann).

Es wird von *unary* Inclusion Dependencies gesprochen wenn gilt $X ⊆ Y$ mit $|X| = |Y| = 1$.

Falls $|X| = |Y| = n ≥ 1$ gilt, handelt es sich um eine *n-ary* Inclusion Dependency.

Fúr Inclusion Dependencies gelten immer folgende Eigenschaften:

* **Reflexiv:** Es gilt immer $X ⊆ X$
* **Transitiv:** Es gilt $X ⊆ Y ∧  Y ⊆ Z \implies X ⊆ Z$
* **Permutationen:** Gilt $(X_1, \dots, X_n) ⊆ (Y_1, .\dots, Y_n)$, dann gilt auch $(X_{σ_1}, \dots, X_{σ_n}) ⊆ (Y_{σ_1}, \dots, Y_{σ_n})$ für alle Tupel-Permutationen $σ_1, \dots, σ_n$


## Beispiel:
***unary* Inclusion Dependencies:**
![](/imgs/unary_IND_Example.jpg)
X := Attribut "Name" aus Tabelle "Lending"
Y := Attribut "Titel" aus Tabelle "Book"

Es ist leicht zu sehen, dass alle Werte die Attribut $Name$ annehmen kann auch in Attribut $Titel$ vertreten sind, daher folgt $X ⊆ Y$.
Es ist auch leicht zu sehen, dass $Y ⊆ X$ nicht gilt, da $Y$ den Wert "3D Computer Graphics" annehmen kann, dieser jedoch nicht in $X$ auftaucht.


***n-ary* Inclusion Dependencies:**
![](/imgs/n-ary_IND_Example.jpg)
X := Attribute "Student" und "Course" aus Tabelle "Lending"
Y := Attribute "Name" und "Lecture" aus Tabelle "Student"

Bei n-ary Inclusion Dependencies ist es nicht nur wichtig das alle Werte der einzelnen Attribute aus $X$ in $Y$ auftauchen, sondern das sie vor allem in der Kombination in $Y$ auftauchen, in der sie in $X$ auftauchen.
Auch hier ist wieder einfach zu sehen, dass $X ⊆ Y$ gilt, denn die drei unterschiedlichen Kombinationen aus $Student$ und $Course$ die in $X$ auftauchen sind auch alle in $Y$ vertreten.
In diesem Fall würde sogar $Y ⊆ X$ gelten, d.h. $X = Y$.
