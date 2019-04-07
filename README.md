## Generative Movement Language

### by Cristian Vogel

A token based score generator which uses context-free grammars and (optional) word lists to generate text scores. By working with words (aka Tokens) as symbolic data for input and output, a score can be assigned meaning through interpretation by human performers and/or software agents.

GML system and codebase was conceived and maintained by Cristian Vogel with essential contributions from Daniel Howe of RiTa lib and MDK (aka Normalised)

*Note: RiTa grammars must now be in proper JSON or YAML format*

Project was originally written in Processing
and is now being developed in the IntelliJ Idea IDE
**Current entry point TestGrammerGML.java**

![alt text](https://www.cristianvogel.com/publicimage/generatedRitualsExample.png "Example output")


### Generative Movement Language is a context-free grammar generator.

In addition to Token lists defined in the grammar file, GML uses an additional lexicon of movement language definitions, based loosely on Ann Hutchinson Guest's descriptions from her book 'Labanotation'.

The program loads in wordLists and builds the definitions of its grammar
dynamically, importing all .txt files it finds in the /data/wordLists directory

~~It can interchange JSON grammar files ( in /data/grammarFiles) randomly and repopulate them with definitions before expanding~~

Code maintained by Cristian Vogel 2010 - 2019 with essential contributions from Daniel C.Howe and MDK (aka Normalised)

RiTa natural language library by Daniel C. Howe http://www.rednoise.org/rita/

Processing core http://processing.org/