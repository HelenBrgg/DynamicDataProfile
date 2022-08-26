# Projektdokumentation

Generierung des PDF Dokuments:

```sh
pandoc *.md  -t html --pdf-engine-opt=--enable-local-file-access -o full.pdf  -f markdown+implicit_figures --toc
```
