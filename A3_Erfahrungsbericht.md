# Erfahrungsbericht

Wir mussten feststellen, dass es besser ist mit existierenden Algorithmen zu arbeiten bevor man einen eigenen Algorithmus versucht zu implementieren. Außerdem wäre Spark ein deutlich besseres Framework für unsere Problemstellung gewesen wäre (wie es auch für die meisten OLAP Anwendungen ist).

##### Eigener Algorithmus vs Existierender Algorithmus {-}

Da wir sofort einen eigenen Algorithmus implementiert haben, hat uns für die Entwicklung eine Referenz-Implementierung gefehlt. Dadurch konnten wir nicht feststellen, ob unser System besser oder schlechter als existierende Lösungen performt. 

Insgesamt hat uns auch sehr viel mehr Aufwand beschert, da wir häufig unseren Ansatz anpassen mussten, was jedesmal große und radikale Änderungen in der Codebase bedeutete.  Hätten wir einen existierenden Algorithmus gewählt und diesen versucht weiter zu verbessern, hätten wir schnell bei jeder Iteration feststellen können, ob wir den Algorithmus verbessert oder verschlechter haben.

##### Akka vs Spark {-}

Wir haben Akka gewählt, da wir uns größere Kontrolle über Task-Abarbeitung und Datenverteilung gewünscht haben. In Anbetracht darauf, dass wir einen eigenen Algorithmus implementiert haben, hätten wir in Spark allerdings deutlich schneller arbeiten können.

In Akka überlegt man sich einen abstrakten Datenfluss (oder Algorithmus) und versucht diesen dann mit einem Aktoren-Protokoll zu implementieren. Das bedeutet auch, dass bei jeder Anpassung des Algorithmus auch das Aktoren-Protokoll angepasst werden muss. Wenn man also seinen Algorithmus falsch konzipiert, muss man möglicherweise große Teile seiner Arbeit verwerfen und neu implementieren.

In Spark hingegen kann man einen Algorithmus sehr natürlich in wenigen Zeilen implementieren. Schwerer wird es dann nur, die Implementierung weiter zu optimieren (wie z.B. die Partitionierung anzupassen). Spark ist damit allerdings deutlich besser geeignet für das Prototyping neuer Algorithmen.


