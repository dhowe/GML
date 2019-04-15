## Generative Movement Language


A token based score generator which uses context-free grammars and (optional) word lists to generate text scores. By working with text as symbolic data for input and output, a score can be assigned meaning through interpretation by human performers and/or software agents.

GML system and codebase was conceived and maintained by Cristian Vogel with essential contributions from Daniel Howe of RiTa lib and MDK (aka Normalised)

*Note: RiTa grammars must now be in proper JSON or YAML format*

Project was originally written in Processing
and is now being developed in the IntelliJ Idea IDE
**Current entry point RunGML.java**

![alt text](https://www.cristianvogel.com/publicimage/generatedRitualsExample.png "Example output")


### Generative Movement Language is a context-free grammar generator.

In addition to Token lists defined in grammar files, GML uses an additional lexicon of movement language definitions stored in the data/wordLists folder. These definitions were derived from Ann Hutchinson Guest's descriptions from her book 'Labanotation'.

The program import all .txt files it finds in the /data/wordLists directory and adds them in with any definitions found inside the grammar.

~~It can interchange JSON grammar files ( in /data/grammarFiles) randomly and repopulate them with definitions before expanding~~

FlowerSpiral grammar file was derived from a performance ritual created by LOGEN performance group during the MELABCph University of Aalborg / MAKROPOL Singularity workshop, April 10th 2019

FlowerSpiral encoded by Cristian Vogel using GML / Generative Movement Language
https://github.com/cristianvogel/GML/

GML maintained by Cristian Vogel 2010 - 2019 with essential contributions from Daniel C.Howe and MDK (aka Normalised)
RiTa natural language library by Daniel C. Howe http://www.rednoise.org/rita/
Processing core http://processing.org/
