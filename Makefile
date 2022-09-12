full.pdf: *.md
	pandoc *.md  -t html --pdf-engine-opt=--enable-local-file-access -o full.pdf --css pandoc.css --number-sections --toc

clean:
	rm full.pdf