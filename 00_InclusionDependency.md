# Inclusion Dependencies

Die Frage die beim finden von Inclusion Dependencies geklärt werden muss ist, ob alle Werte die ein Attribut X annehmen kann auch von Attribut Y angenommen werden können. Falls das der Fall ist, ist X abhängig von Y und man schreibt X ⊆ Y.
Formal bedeutet das: ∀ t<sub>i</sub>[X] ∈ r<sub>i</sub> ∃ t<sub>j</sub>[Y] ∈ r<sub>j</sub> mit t<sub>i</sub>[X] = t<sub>j</sub>[Y].

Allgemein werden X und Y als Listen von Attributen gesehen, wobei stehts gelten muss |X| = |Y|.

Es wird von *unary* Inclusion Dependencies gesprochen wenn gilt X ⊆ Y mit |X| = |Y| = 1. Falls |X| = |Y| = n gilt, handelt es sich um eine *n-ary* Inclusion Dependency.

Inclusion Dependencies sind immer...

* **Reflexiv:** Es gilt immer X ⊆ X
* **Transitiv:** Es gilt X ⊆ Y \wedge Y ⊆ Z => X ⊆ Z
* **Permutationen:** Es gilt (X<sub>1</sub>, ... X<sub>n</sub>) ⊆ (Y<sub>1</sub>, ..., Y<sub>n</sub>), dann gilt auch (X<sub>σ1</sub>, ..., X<sub>σn</sub>) ⊆ (Y<sub>σ1</sub>, ..., Y<sub>σn</sub>) für alle Permutationen σ1, ..., σn 


## Beispiel:
***unary* Inclusion Dependencies:**
![](\imgs\unary_IND_Example.jpg)
X := Attribut "Name" aus Tabelle "Lending"
Y := Attribut "Titel" aus Tabelle "Book"

Es ist leicht zu sehen, dass alle Werte die "Name" annehmen kann auch in Attribut "Titel" vertreten sind, daher folgt X ⊆ Y.
Es ist auch leicht zu sehen, dass Y ⊆ X nicht gilt, da Y den Wert "3D Computer Graphics" annehmen kann, dieser jedoch nicht in X auftaucht.


***n-ary* Inclusion Dependencies:**
![](\imgs\n-ary_IND_Example.jpg)
X := Attribute "Student" und "Course" aus Tabelle "Lending"
Y := Attribute "Name" und "Lecture" aus Tabelle "Student"

Bei n-ary Inclusion Dependencies ist es nicht nur wichtig das alle Werte der einzelnen Attribute aus X in Y auftauchen, sondern das sie vor allem in der Kombination in Y auftauchen, in der sie auch in X auftauchen.
Auch hier ist wieder einfach zu sehen, dass X ⊆ Y gilt, denn die drei unterschiedlichen Kombinationen aus "Student" und "Course" die in X auftauchen sind auch alle in Y vertreten.
In diesem Fall würde sogar auch Y ⊆ X gelten.
