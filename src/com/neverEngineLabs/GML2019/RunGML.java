/*
Generative Movement Language is a context-free grammar text generator.


 press space to generate
 press 's' to save a .txt and a .png
 press 'r' to see a breakdown of non-repeating/POS tagged/Open Class tokens derived from the generated result


 (c) cristian vogel 2010-2019

 RiTa natural language library by Daniel C. Howe
 http://www.rednoise.org/rita/

 */
package com.neverEngineLabs.GML2019;

import processing.core.PApplet;
import processing.data.StringList;
import rita.RiTa;

import java.util.Arrays;


public class RunGML extends PApplet {
	//constructor with field assignments
	private GrammarGML grammar = new GrammarGML(this);
	private String[] lines = { "Press spacebar to Generate...\nPress 's' to save...\nPress 'i' for info...\nPress 'r' to reduce..." };
	private String[] linesAlt ;
	private String currentGrammarFile = "grammarFiles/FlowerSpiral.json";
	private String latestTitle = "Welcome to GML";
	private String latestTimeStamp = "Generative Movement Language";
	private Boolean savedFlag = false;
	private int generationCounter = 0;

	// todo: OSC setup
   // public OscP5 oscP5 = new OscP5(this, 8000); // listener

	//Some defs
	//font sizes
    private final int  H1=25, P=20, TINY=12;
	private boolean displayingInfo = false;
	private boolean displayingReduced = false;


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


	private void displayText(String title, String[] body, int lineHeight) {

		drawDecorativeBackground( 15, body.length + generationCounter);
		textSize(H1);
		text(title, width/2, lineHeight);

		textSize(TINY);
		text((savedFlag ? ("saved " + latestTimeStamp) : latestTimeStamp), width / 2, lineHeight*2);

		textSize(P);
		for (int j = 0; j < body.length; j++) {
			text(body[j], width/2, (height/5) + j * lineHeight);
		}


	}


	private void drawDecorativeBackground(int backgroundGrey, int numberOfLines) {

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


    private void expandGrammar() {
	lines = grammar.generateTextAndSplitAtLineBreak();
	savedFlag = false;
	generationCounter++;

	if (lines.length > 0) {
		/* lines = grammar.shuffle(lines);
		//todo: allow shuffle from a grammar callback */
		latestTitle = grammar.generateRhymingTitleFromLinesOfText(lines);//todo: more random title gen
		setTitleBar(latestTitle);
		latestTimeStamp = grammar.latestTimeStamp;
	} else {
		setTitleBar("There was a problem generating the text...");
	}


	displayText(latestTitle, lines, 28);
}


	// was too easy to loose the text by clicking out of app and back in, so removed for now
	public void mouseClicked() {
	//expandGrammar();
	//println((Object) getGeneratedTextAsLines());
	}

	/**
	 * space bar expands grammar
	 * S saves png and txt of result
	 * I gets info about grammar
	 * R gets closed class words
	 */
	public void keyPressed() {
		if (key == ' ' ) {
			expandGrammar();
		}
		if ( key == 's' || key == 'S') {

			//try to save to disk, post status in window title
			if (saveGeneratedTextAndScreenshot(grammar.currentExpansion) ) {

				setTitleBar("Saved successfully "+grammar.timeStampWithDate());
				savedFlag = true;
				displayText(latestTitle, lines, 28);

			}
			else { setTitleBar("ERROR: NOT SAVED"); savedFlag = false;  }
		};

		if ( key == 'i' || key == 'I') {

			displayingInfo=!displayingInfo;
			String info = grammar.displayInfo();


			if (info!="") {linesAlt = split(info, grammar.lineBreaker); println(linesAlt);}

			if (!displayingInfo) {

				displayText(latestTitle, lines, 28);
			}
			else {

				displayText("Grammar File Info", linesAlt, 22);
			}
		}

		if (key=='r' || key == 'R') {
			displayingReduced = !displayingReduced;

			if (displayingReduced) {
				displayText(
						latestTitle + " (Reduced)",
						grammar.arrangeTokensIntoLines(grammar.currentExpansionReduced, 6),
						24
				);
			} else displayText(latestTitle, lines, 28);
		}
	}

    /** save output to disk as txt and png
     *
     * @param outputStrings
     * @return true if successful
     */
	private Boolean saveGeneratedTextAndScreenshot(String[] outputStrings) {


		StringList sList = new StringList ( outputStrings );
		String title = latestTitle;

		String savePath = calcSketchPath()+"/data/Saved/"+RiTa.chomp(title)+"/";
		createPath(savePath);
		String fn = savePath + RiTa.chomp(title);
		String fnReduced = fn+"_reduced";
		//String fn = (System.getProperty("user.home"))+"/documents/"+title+".txt";

		//header
		sList.insert(0, title+"\n\n");

		//footer
		sList.append("\n\n"); //make some space
		sList.append("Generated:" + grammar.timeStampWithDate());
		println(fn);
		try {
			saveStrings(fn+".txt",sList.array());
			saveFrame(fn + ".png"); // save screen shot
			saveStrings(fnReduced+".txt", grammar.arrangeTokensIntoLines(grammar.currentExpansionReduced, 4));

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