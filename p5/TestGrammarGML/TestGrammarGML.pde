// A simple test for GML grammars

import rita.*;
import rita.support.*;

GrammarGML grammar;
String[] lines = { "click to generate" };

void setup() {
  size(1000, 600);

  grammar = new GrammarGML(this);
  grammar.loadFrom("grammarFiles/CristianImprov1.json");

  textSize(20);
  textAlign(CENTER, CENTER);
}

void mouseClicked() {
  lines = grammar.expand();
}

void draw() {

  background(0);
  for (int j = 0; j < lines.length; j++) {
    text(lines[j], width/2, height/3 + j * 28); // leading=28
  }
}