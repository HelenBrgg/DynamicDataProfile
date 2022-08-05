# Inclusion Dependencies

Die Frage die beim finden von Inclusion Dependencies geklärt werden muss ist, ob alle Werte die ein Attribut X annehmen kann auch von Attribut Y angenommen werden können. Falls das der Fall ist, ist X abhängig von Y und man schreibt X ⊆ Y.
Formal bedeutet das: ∀ t<sub>i</sub>[X] ∈ r<sub>i</sub> ∃ t<sub>j</sub>[Y] ∈ r<sub>j</sub> mit t<sub>i</sub>[X] = t<sub>j</sub>[Y].

Dabei können X und Y aber auch eine Menge von Attributen sein.
Gilt X ⊆ Y mit |X| = |Y| = 1 so handelt es sich um *unary* Inclusion Dependency, gilt hingegen X ⊆ Y mit |X| = |Y| = n so spricht man von einer *n-ary* Inclusion Dependency.

