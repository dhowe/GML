/*
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

 OSC functionality provided by OscP5 library for Processing

 */
package com.neverEngineLabs.GML2019;

import processing.core.PApplet;
import processing.data.StringList;




public class RunGML extends PApplet {
	//constructor with field assignments
	private GrammarGML grammar = new GrammarGML(this);
	private String[] lines = { "Press space to Generate...\nPress 's' to save..." };
	private String currentGrammarFile = "grammarFiles/FlowerSpiral.json";
	private String latestTitle = "Welcome to GML";
	private String latestTimeStamp = "Generative Movement Language";
	private Boolean savedFlag = false;
	private int generationCounter = 0;

	// todo: OSC setup
   // public OscP5 oscP5 = new OscP5(this, 8000); // listener

	//Some defs
	//font sizes
	public final int  H1=25, P=20, TINY=12;


	////////////////////////





	public void settings() {

		size(1000, 900);
	}

	public void setup() {
        grammar.loadFrom(currentGrammarFile); // todo:  user or random selection of new grammars from disk
        textSize(P);
		textAlign(CENTER, CENTER);
		setTitleBar(latestTitle + grammar.getLatestTimeStamp());
		displayText(latestTitle, lines, 28);
	}

	public void draw() {
		/*
		usually called every frame by Processing
		but we only need to draw the text Score from an interaction
		 so graphics are done inside grammar expansion event driven method
		 like this

		displayText(latestTitle, lines, 28);
		*/

	}


	public void displayText(String title, String[] body, int lineHeight ) {

		drawDecorativeBackground( 15, body.length + generationCounter);
		textSize(H1);
		text(title, width/2, lineHeight);

		textSize(TINY);
		text((savedFlag ? ("saved " + latestTimeStamp) : latestTimeStamp), width / 2, lineHeight*2);

		textSize(P);
		for (int j = 0; j < body.length; j++) {
			text(lines[j], width/2, (height/5) + j * lineHeight);
		}


	}


	public void drawDecorativeBackground(int backgroundGrey, int numberOfLines) {

		background(backgroundGrey);

		//fill(250);
		noiseDetail(8, 0.8f);

		if (numberOfLines > 1) {
			for (int i = 1; i < numberOfLines * 10; i++) {

				stroke(i+30, 80);
				strokeWeight(random(i));
				line(-50, (noise(i * 0.5f) * (height * map(i, 1, numberOfLines, 0.2f, 1))) - 200,
						width + 50, (noise(i * 0.501f) * (height * map(i, 1, numberOfLines, 0.2f, 1))) - 200);
			}
		}
	}


public void expandGrammar() {
	lines = grammar.generateTextAndSplitAtLineBreak();
	savedFlag = false;
	generationCounter++;

	if (lines.length > 0) {
		/* lines = grammar.shuffle(lines);
		//todo: allow shuffle from a grammar callback */
		latestTitle = grammar.generateTitleFromLineOfText(lines[0]);//todo: more random title gen
		setTitleBar(latestTitle);
		latestTimeStamp = grammar.latestTimeStamp;
	} else {
		setTitleBar("There was a problem generating the text...");
	}


	displayText(latestTitle, lines, 28);
}




	public String[] getGeneratedTextAsLines() {
		return lines;
	}

	public void mouseClicked() {
	expandGrammar();
	println((Object) getGeneratedTextAsLines());
	}


	public void keyPressed() {
		if (key == ' ' ) {
			expandGrammar();
		}
		if ( key == 's' || key == 'S') {
			//try to save to disk, post status in window title
			if (saveOutputToDisk(getGeneratedTextAsLines())) {

				setTitleBar("Saved successfully "+grammar.timeStampWithDate());
				savedFlag = true;
				displayText(latestTitle, lines, 28);

			}
			else { setTitleBar("ERROR: NOT SAVED"); savedFlag = false;  }
		};
	}

	Boolean saveOutputToDisk( String [] outputStrings ) {

		//save a generated script, come up with a filename and title
		// made from minimum 16 characters of the first line
		// saved to user/documents
		// return successful save
		StringList sList = new StringList ( outputStrings );
		String firstLine = sList.get(0);
		String title = firstLine.substring( 0,firstLine.indexOf(' ' ,16));
		String fn = (System.getProperty("user.home"))+"/documents/"+title+".txt";
		//header
		sList.insert(0, title+"\n\n");

		//footer
		sList.append("\n\n"); //make some space
		sList.append("Generated:" + grammar.timeStampWithDate());
		println(fn);
		try {
			saveStrings(fn,sList.array());
			saveFrame(fn + ".png"); // save screen shot

		} catch (Exception e) {
			background(255,0,0);
			return false;
		}

		return true;
	}


	public void setTitleBar(String s) {
		surface.setTitle(s);
	}


/// Java main

	public static void main(String[] args) {

		System.out.println("Running " + RunGML.class.getName());
		String[] options = {  RunGML.class.getName() };
		PApplet.main(options);
	}

}