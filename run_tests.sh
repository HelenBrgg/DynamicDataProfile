#!/bin/bash

# fetch ddm-akka
if [[ ! -x ddm-akka/data/ ]]; then
    git submodule init
    git submodule update
fi

# create link for TPCH dataset

echo ""
echo "+++++ Testing Akka-System on example dataset +++++"
echo ""
rm final-results.csv ddm-akka/results.txt 2> /dev/null

rm ddm-akka/data/TPCH 2> /dev/null
$(cd ddm-akka/data/ && ln -s ../../data/example/ ./TPCH)

java -Xmx8g -ea -cp target/ddm-akka-1.0.jar de.ddm.Main master -ip data/example > /dev/null
$(cd ddm-akka && java -Xmx8g -ea -cp target/ddm-akka-1.0.jar de.ddm.Main master > /dev/null)

OUTPUT=$( sort ddm-akka/results.txt 2>&1 | diff final-results.csv - 2>&1)

if [[ ! -z $OUTPUT ]]; then
    echo ""
    echo "+++++ FAILED TEST +++++"
    echo ""
    echo "$OUTPUT"
    exit 1
else
    echo ""
    echo "+++++ SUCCESSFUL TEST +++++"
    echo ""
fi

echo "+++++ Testing Akka-System on TPC-H dataset +++++"

rm final-results.csv ddm-akka/results.txt 2> /dev/null

rm ddm-akka/data/TPCH 2> /dev/null
$(cd ddm-akka/data/ && ln -s ../../data/TPCH/ ./TPCH)

java -Xmx8g -ea -cp target/ddm-akka-1.0.jar de.ddm.Main master -ip data/TPCH > /dev/null
$(cd ddm-akka && java -Xmx8g -ea -cp target/ddm-akka-1.0.jar de.ddm.Main master > /dev/null)

OUTPUT=$( sort final-results.csv 2>&1 | diff final-results.csv - 2>&1)


if [[ ! -z $OUTPUT ]]; then
    echo ""
    echo "+++++ FAILED TEST +++++"
    echo ""
    echo "$OUTPUT"
    exit 1
else
    echo ""
    echo "+++++ SUCCESSFUL TEST +++++"
    echo ""
fi

