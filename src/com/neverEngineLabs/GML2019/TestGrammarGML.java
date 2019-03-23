package com.neverEngineLabs.GML2019;/*
Generative Movement Language is a context-free grammar generator.
 
 It uses a lexicon of movement language, based loosely on 
 Ann Hutchinson Guest's descriptions in the book 'Labanotation'.
 
 The program loads in  wordlists and builds the definitions of its  grammar  
 dynamically, importing all .txt files it finds in the /data directory 
 
 It can interchange grammar files ( in /data/grammarFiles) randomly 
 and repopulate them with definitions before expanding
 
 press 's' to save a .txt and a .png
 press 'r' to see a breakdown of non-repeating/POS tagged/Open Class tokens derived from the generated result
 press 'p' to send sequence to Kyma
 
 (c) cristian vogel 2010
 
 RiTa natural language library by Daniel C. Howe
 http://www.rednoise.org/rita/
 
 */

import processing.core.PApplet;

public class TestGrammarGML extends PApplet {
	
	GrammarGML grammar;
	String[] lines = { "click to generate" };
	
	public void settings() {

		size(1000, 600);
	}

	public void setup() {
		grammar = new GrammarGML(this);
		grammar.loadFrom("grammarFiles/CristianImprov1.json");
		
		textSize(20);
		textAlign(CENTER, CENTER);
		surface.setTitle("GenMov2019");
	}
	
	public void mouseClicked() {
		lines = grammar.expand();
	}
	
	public void draw() {
		
		background(0);
		for (int j = 0; j < lines.length; j++) {
			text(lines[j], width/2, height/3 + j * 28); // leading=28 
		}
	}

	public static void main(String[] args) {

		System.out.println("Running " + TestGrammarGML.class.getName());
		String[] options = {  TestGrammarGML.class.getName() };
		PApplet.main(options);
	}

}