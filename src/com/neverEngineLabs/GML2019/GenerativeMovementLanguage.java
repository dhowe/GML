package com.neverEngineLabs.GML2019;

/*
this program was originally a Processing Sketch designed to be
sending OSC signals to a Symbolic Sound Kyma system, where
its preset list would try to map to recognised words
in the generated text - it is included in the repository for future reference

 */


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
 
 */




import java.io.File;
import java.util.*;

import javax.swing.JFileChooser;

import processing.core.*;
import oscP5.*;
import netP5.*;
import rita.*;
import rita.support.Conjugator;




public class GenerativeMovementLanguage extends PApplet {
	
	RiText[] rts;
	RiGrammar rg;
	Conjugator rc;
	RiLexicon lexicon;
	
	OscP5 oscP5;
	JFileChooser chooser;
	NetAddress slimeLocation;

	// the total duration of matched presets sequence in seconds
	float playDuration = (60 * 3);

	// choose to have unique tokens or allow repeated tokens in playing sequence
	boolean uniqueTokensFlag = false;

	Date date = new Date();

	PFont font;
	int ypos = 5;
	String pathToWordLists;
	String pathToGrammarFiles;
	String[] wordListFilenames;
	String[][] wordLists; // an array of string arrays
	String[] grammarFiles; // number of grammar files
	String[] finalProcessedTokens;
	ArrayList uniqueOpenClassWords;
	String[] allOpenClassWords;
	String[] seperateTokens;
	int uniqueListSize;

	String result;
	int resultCounter = 0;
	String generativeTitle;
	String rule, def;
	String ritaError = "";

	boolean pageFlip = false;
	int savedFileIndex;
	String saveState = "'s' to save";

	ArrayList presetNames;
	int presetCount;
	int presetPlayIndex = 0;
	int presetNumber;
	String presetName = "";
	ArrayList presetPlaySequence;
	Boolean finishedPlaying = true;
	float beatClockDuration;
	int defaultPresetNumber;
	String currentGrammar;

	public void settings() {

		size(1000, 600);
	}
	
	public void setup() {
		rc = new Conjugator();
		RiGrammar rg = new RiGrammar();
		rg.loadFrom("grammarFiles/CristianImprov1.json");
		System.out.println(rg.expand(this));
		frameRate(5);
		smooth();
		rectMode(CORNERS);
		textAlign(LEFT);

		slimeLocation = new NetAddress("169.254.67.151", 8000);
		lexicon = new RiLexicon(this);
		chooser = new JFileChooser();
		rc = new Conjugator();
		//
		// edit this with the directory which contains the wordlists referenced by
		// the Grammar
		//
		// pathToWordLists =
		// "/Applications/JanusNode of CIE Gilles Jobin/JanusNode Resources/BrainFood";
		pathToWordLists = sketchPath("src") + "/data"; // this version looks inside
																								// sketch data folder

		println("Checking " + pathToWordLists+" ...");
		wordListFilenames = filesInSameDirectory(pathToWordLists);
		println("Found " + wordListFilenames.length + " word lists:");

		// the location of the context free grammar files,
		// for now must be in the same folder as wordlists in a subfolder called
		// /grammarFiles
		pathToGrammarFiles = pathToWordLists + "/grammarFiles/"; // inside
																															// (wordlistsDir)/grammarFiles

		grammarFiles = filesInSameDirectory(pathToGrammarFiles);
		println("Found "+grammarFiles.length+" grammar files");
		if (grammarFiles.length == 0) {
			fill(0);
			text("Grammar files missing!", width * 0.5f, height * 0.5f);
			exit();
		}

		rg = new RiGrammar();
		currentGrammar = pathToGrammarFiles + grammarFiles[0];
		rg.loadFrom(currentGrammar);

		// process our wordlists to the correct format for Grammar files and build
		// the grammar on the fly
		// sweet.

		wordLists = new String[wordListFilenames.length][100]; // declare big enough
																													// 2D array?
		for (int i = 0; i < wordListFilenames.length; i++) {
			String path = (pathToWordLists + "/" + wordListFilenames[i]);
			wordLists[i] = loadStrings(path);
			// print("["+wordListFilenames[i]+"]");
			println("<" + wordListFilenames[i] + ">");

			// println(" ... "+wordLists[i].length+" tokens.");
		}

		for (int i = 0; i < wordListFilenames.length; i++) {

			// remove the "Dance_" from the front and the ".txt". from the end
			wordListFilenames[i] = wordListFilenames[i].replaceAll("Dance_", "");
			wordListFilenames[i] = wordListFilenames[i].replaceAll(".txt", "");
		}

		setDefinitionsFromFiles(wordListFilenames);

		rg.print();

		// end of string processing

		// position tagger select maximum entropy tagger
		// TODO: RiPosTagger.setDefaultTagger(RiPosTagger.MAXENT_POS_TAGGER);

		// attributes of save dialog
		chooser.setFileHidingEnabled(true);

		// OSC setup

		oscP5 = new OscP5(this, 8000); // listener

		/*
		 * osc plug service osc messages with a specific address pattern can be
		 * automatically forwarded to a specific method of an object. in this
		 * example a message with address pattern /test will be forwarded to a
		 * method test(). below the method test takes 2 arguments - 2 ints.
		 * therefore each message with address pattern /test and typetag ii will be
		 * forwarded to the method test(int theA, int theB)
		 */
		oscP5.plug(this, "response", "/osc/response_from"); // bi-directional setup
																												// message
		oscP5.plug(this, "presetTotal", "/osc/notify/presets/ironman"); // hopefully
																																		// received
																																		// number
																																		// presets
		initialisePacarana();

		saveState = "";
		ypos = (int) (height * 0.3);
		display("GenMov", "a language based score generator \nevents for sound, bodies and imagination.\n \n \n \nWhen ready , click to generate...\n \n \n \n \n controls: \n 's' to save \n 'r' to view reduced text \n 'p' to send sequence to kyma \n 'w' to close", ypos, 40); // method...
																																																																																																																																								// display(<title>
																																																																																																																																								// ,
																																																																																																																																								// <body>
																																																																																																																																								// ,
																																																																																																																																								// yposition,
																																																																																																																																								// number
																																																																																																																																								// of
																																																																																																																																								// generative
																																																																																																																																								// graphics);
	}

	public void draw() {

		// do nothing
	}

	public void mouseClicked() {

		background(127);
		saveState = "type 's' to save";
		ritaError = "";
		for (int i = 0; i < 1; i++) {
			sendOscMessage("/osc/notify/presets/ironman", (int) 1);
			sendOscMessage("/osc/notify/vcs/ironman", (int) 0);
		}
		delay(500);

		finishedPlaying = true;
		// TODO: RiTa.stopAllTimers
		sendOscMessage("/preset", defaultPresetNumber);

		currentGrammar = pathToGrammarFiles + grammarFiles[round(random(grammarFiles.length - 1))];
		rg.loadFrom(currentGrammar);
		// an example of loading in new grammar files at random

		setDefinitionsFromFiles(wordListFilenames); // must rebuild all the rules
																								// again for new grammer file

		ypos = 80;

		try {
			result = rg.expand(this); // build string from grammar file, the moment of
														// generation
			date = new Date();
		}
		catch (NullPointerException e) {
			e.printStackTrace();
			result = "error";
		}
		catch (IllegalStateException e) {
			ritaError = RiTa.stackToString(e);
			int snip = ritaError.indexOf(">") + 2;
			println(ritaError);
			result = "Error in grammar file!\n\n" + ritaError.substring(0, snip) + " or missing block symbol { } ...";
			generativeTitle = "";
		}

		if (ritaError.equals("")) {

			// this part of the program runs certain manipulations on the text

			seperateTokens = RiTa.tokenize(result); // split back into seperate words

			// text without so called closed-class words ( the , at , a
			allOpenClassWords = stripClosedClassWords(seperateTokens);

			uniqueOpenClassWords = stripRepeats(allOpenClassWords); // an ArrayList

			uniqueListSize = uniqueOpenClassWords.size();

			seperateTokens = (String[]) uniqueOpenClassWords.toArray(seperateTokens); // strip
																																								// all
																																					// etc
			println("Unique tokens list:" + uniqueListSize + " All tokens:" + allOpenClassWords.length);

			// title ideas, generated before tagging the open class words
			int randomTokenPick = (int) random(allOpenClassWords.length);

			try {
				generativeTitle = capitalise(lexicon.randomWord("in")) + " " + capitalise(allOpenClassWords[randomTokenPick]);
			}
			catch (NullPointerException e) {
				e.printStackTrace();
				generativeTitle = "Exception";
			}

			finalProcessedTokens = new String[uniqueListSize];
			for (int i = 0; i < uniqueListSize; i++) {

				// optionally run pos tagger
				// there's 38 tokens in the Penn tagger see guide at
				// http://www.rednoise.org/rita/documentation/ripostagger_class_ripostagger.htm
				// or 4 for WordNet

				// finalProcessedTokens[i] = tagger.tagInline( trim(seperateTokens[i]));
				finalProcessedTokens[i] = trim(seperateTokens[i]);
			}

			// the array finalProcessedTokens, contains a list of the words from the
			// generated text, reduced to
			// open class words, the essence of the text,
			//
		}
		resultCounter++;

		generativeTitle = "Gen #" + resultCounter + " at " + date.getHours() + ":" + date.getMinutes();
		display(generativeTitle, result, ypos, 127);

		// matchWithPresets(finalProcessedTokens);
	}

	private static List closedClassWords = null;

	public static String[] stripClosedClassWords(String[] words) {

		if (closedClassWords == null) closedClassWords = Arrays.asList(RiTa.CLOSED_CLASS_WORDS);
		ArrayList<String> stripped = new ArrayList<String>();
		for (int i = 0; i < words.length; i++) {
			if (!closedClassWords.contains(words[i])) stripped.add(words[i]);
		}
		return stripped.toArray(new String[0]);
	}

	void display(String title, String body, int y, int l) {

		ypos = y;
		background(random(l));
		// draw decorations
		fill(250);
		noiseDetail(8, 0.8f);
		noiseSeed((long) random(200));
		int numberOfLines = l;
		for (int i = 1; i < numberOfLines; i++) {
			stroke(i, 80);
			strokeWeight(random(i));
			line(-50, (noise(i * 0.5f) * (height * (map(i, 1, numberOfLines, 0.2f, 1)))) - 200, width + 50, (noise(i * 0.501f) * (height * (map(i, 1, numberOfLines, 0.2f, 1)))) - 200);
		}

		textSize(28);

		text(title, 15, ypos - 15);

		textSize(14);
		text(body, 20, ypos + 15, width * 0.95f, height);

		fill(200);
		text(saveState, width * 0.8f, height - 15);
		if (!saveState.equals("")) {
			text("click to rebuild", 5, height - 15);
			textSize(10);
			String[] grammarName = split(currentGrammar, "/");
			text("generated from " + grammarName[grammarName.length - 1], width * 0.28f, height - 30);
			text("    on " + date.toString(), width * 0.28f, height - 15);
		}
	}

	void flipPage() {

		if (!pageFlip) {
			String s = "";
			for (int i = 0; i < finalProcessedTokens.length; i++) {
				s += finalProcessedTokens[i] + "     ";
			}
			display(uniqueListSize + " Open Class Tokens", s + "\n \n unique = " + uniqueTokensFlag + " (press 'u' to switch)", ypos, finalProcessedTokens.length);
			pageFlip = true;
		}
		else {
			display(generativeTitle, result, ypos, 127);
			pageFlip = false;
		}
	}

	void setDefinitionsFromFiles(String[] files) {

		for (int i = 0; i < files.length; i++) {
			String ruleName = files[i]; // lovely cleaned ruleName string
			String terminals = "";
			for (int j = 0; j < wordLists[i].length; j++) {
				terminals += wordLists[i][j] + " | ";
			}
			rg.addRule(ruleName, terminals);
		}
		// TODO: rg.setLineBreakCharacter("/");
	}

	// strip repeated tokens method
	ArrayList stripRepeats(String[] textArray) {

		ArrayList unique = new ArrayList();

		for (int i = 0; i < textArray.length; i++) {

			if (uniqueTokensFlag) {
				if (!(unique.contains(textArray[i]))) {
					unique.add(textArray[i]);
				}
			}
			else {
				unique.add(textArray[i]);
			}
		}
		return unique;
	}

	public void keyPressed() {

		if (key == CODED) {
			if ((keyCode == RIGHT) && (!finishedPlaying)) {
				// if end of sequence, return Kyma to a default

				if (presetPlayIndex > presetPlaySequence.size() - 1) {
					//TODO: RiTa.stopCallbackTimers();
					println("timers stopped and recalling Default!");
					finishedPlaying = true;

					sendOscMessage("/preset", defaultPresetNumber);

				}
				else {

					// next preset name get and send

					// sendOscMessage("/vcs/beatClockDuration/1", beatClockDuration);
					// sendOscButton("/vcs/eventChange/1");
					Integer p;

					p = (Integer) presetPlaySequence.get(presetPlayIndex);
					sendOscMessage("/preset", p + 1);
					presetPlayIndex++;
					// RiTa.resetCallbackTimer(this, "beatClock", 0, beatClockDuration,
					// false);

					println("Playing " + presetNames.get(p));
				}
			}
		}

		if (key == 'p' || key == 'P') {

			matchWithPresets(finalProcessedTokens);
			println("Sent to Kyma!");
		}

		if (key == 's' || key == 'S') {

			// save pop up using JFileChooser

			int returnVal = chooser.showSaveDialog(this.frame);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				String saveFilename = chooser.getSelectedFile().toString();
				println(saveFilename);
				if (saveFilename != "") {
					saveFilename = saveFilename + ".txt";
				}
				else {
					saveFilename = generativeTitle + ".txt";
				}

				String[] grammarName = split(currentGrammar, "/");
				result = generativeTitle + "\n\n" + result + "\n\n" + "generated on " + date.toString() + "\n" + "by " + grammarName[grammarName.length - 1];

				saveStrings(saveFilename, result.split("\\n")); // save simple text file
				fill(127);
				rect(0, height - 25, width, height);

				textSize(10);
				fill(60);
				text("generated on " + date.toString(), width * 0.28f, height - 15);
				saveFrame(saveFilename + ".png"); // save screen shot

				saveState = generativeTitle + " saved!";
				textSize(13);
				fill(0, 180, 5);
				text(saveState, width * 0.725f, height - 25, width + 15, height);
				fill(80);
				text("click to rebuild", 5, height - 15);
			}
		}

		if (key == 'r') { // show reduced/processed result
			flipPage();
		}

		if (key == 'w') { // quit
			oscP5.stop();
			exit();
		}

		if (key == 'u') { // flag unique token list generation
			uniqueTokensFlag = !uniqueTokensFlag;
			if (uniqueTokensFlag) {
				uniqueOpenClassWords = stripRepeats(allOpenClassWords);
				finalProcessedTokens = new String[uniqueOpenClassWords.size()];
				finalProcessedTokens = (String[]) uniqueOpenClassWords.toArray(finalProcessedTokens);
				uniqueListSize = uniqueOpenClassWords.size();
			}
			else {
				finalProcessedTokens = allOpenClassWords;
				uniqueListSize = finalProcessedTokens.length;
			}
			pageFlip = false;
			background(0);
			flipPage();
		}
	}

	void matchWithPresets(String[] tokens) {

		// method to match preset names and build preset sequence from tokens

		// improve? match with contains() to catch stems?

		presetNames.clear();
		presetPlaySequence.clear();

		for (int i = 0; i < (presetCount); i++)

		// their is a lag between the request and the receipt of the OSC message for
		// the preset names from kyma
		// so a small wait loop...

		{
			sendOscMessage("/osc/preset", i);
			while (presetName.equals("")) {
				// delay(5);
				print(".");
			}

			println("\n preset number: " + presetNumber);
			println(" preset name: " + presetName);
			presetNames.add(presetName);
			presetName = "";
		}

		println(presetNames); // this should be the filled ArrayList
		defaultPresetNumber = 1 + ((Integer) presetNames.indexOf("Default"));

		for (int i = 0; i < tokens.length; i++) {
			String s = RiTa.stem(tokens[i]);
			if (presetNames.contains(tokens[i])) {
				println("got one! matchin the preset " + tokens[i]);
				int theTokensMatchedIndex = presetNames.indexOf(tokens[i]);
				println(theTokensMatchedIndex);
				presetPlaySequence.add(theTokensMatchedIndex);

			}

			else if (presetNames.contains(s)) {

				println("got one as stem! matchin the preset " + tokens[i] + " from stem " + s);
				int theTokensMatchedIndex = presetNames.indexOf(s);
				println(theTokensMatchedIndex);
				presetPlaySequence.add(theTokensMatchedIndex);
			}

		}
		println(presetPlaySequence);

		// now OSC panic!

		if ((presetPlaySequence.size() > 0) && finishedPlaying) {
			presetPlayIndex = 0; // reset to start

			beatClockDuration = (playDuration / presetPlaySequence.size());
			println("ready sequence , calculated duration" + beatClockDuration);
			// sendOscMessage("/vcs/beatClockDuration/1", beatClockDuration);

			// RiTa.setCallbackTimer(this, "beatClock", 0, 1, false); // start
			// sequence player, first clock , a pause
			finishedPlaying = false;

		}

	}

	// RiTa event handler

	void onRiTaEvent(RiTaEvent re) {

		// text input
		if (re.type() == RiTa.TEXT_ENTERED) {

			/*
			 * TODO: if (re.name().equals("totalDuration")) { String input =
			 * (String)re.getData(); playDuration = float( input) ; println
			 * (playDuration); }
			 */
		}
	}

	// OSC incoming methods

	/* incoming osc message are forwarded to the oscEvent method. */
	void oscEvent(OscMessage theOscMessage) {

		print("### received an osc message.");
		print(" addrpattern: " + theOscMessage.addrPattern());
		println(" typetag: " + theOscMessage.typetag());

		// received a presetname
		if (theOscMessage.addrPattern().equals("/osc/preset")) {
			presetNumber = theOscMessage.get(0).intValue();
			presetName = theOscMessage.get(1).stringValue();
		}
	}

	void response(int p) {

		println("Beslime sending on UDP:" + p);
	}

	void presetTotal(int p) {

		presetCount = p;
		println("preset count:" + presetCount);
	}

	// OSC outgoing methods

	void sendOscButton(String message) { // button on and off with 100 ms lag

		OscMessage buttonOn = new OscMessage(message);

		OscMessage buttonOff = new OscMessage(message);

		// button press
		buttonOn.add(1.0);
		oscP5.send(buttonOn, slimeLocation);
		buttonOn.clear();

		delay(5);
		// button release
		buttonOff.add(0.0);
		oscP5.send(buttonOff, slimeLocation);
		buttonOff.clear();

	}

	void sendOscMessage(String message, float value) {

		OscMessage slimeMessage = new OscMessage(message);
		slimeMessage.add((float) value);
		oscP5.send(slimeMessage, slimeLocation);
		slimeMessage.clear();

	}

	void sendOscMessage(String message, int value) {

		OscMessage slimeMessageInt = new OscMessage(message);
		slimeMessageInt.add((int) value);
		oscP5.send(slimeMessageInt, slimeLocation);
		slimeMessageInt.clear();

	}

	void initialisePacarana() {

		OscBundle slimeSetup = new OscBundle();

		sendOscMessage("/osc/respond_to", (int) 8000); // initialize communication
																										// with Kyma
		delay(500);
		sendOscMessage("/vcs/beatClockDuration/1", beatClockDuration);
		sendOscMessage("/vcs/eventChange/1", 0);
		presetNames = new ArrayList(); // to hold the names
		presetPlaySequence = new ArrayList(); // to hold the numbers
	}

	String conjugate(String number, String person, String tense, String verb) {
		
	  if (tense.equals("ing")) {
	    tense="present";
	    rc.setProgressive(true);
	  }
	  else if (tense.equals("s")) {
	    tense="present";    // "present simple";
	  }

	  String s = rc.conjugate(number, person, tense, verb); 
	  if (rc.isPassive()) s += " by";
	  
	  return s;
	} 


	/*String conjugate(String number, String person, String tense, String verb) {
				
	  if (tense.equals("ing")) {
	    tense = "present"; 
	    //TODO: rc.setProgressive(true);
	  }
	  else if (tense.equals("s")) {
	    tense = "present";   
	  }
	  
	  Map options = createVerbMap(number, person, tense);

	  String s = RiTa.conjugate(verb, options); 
	  //TODO: if (rc.isPassive()) s += " by";
	  return s;
	}

	private Map createVerbMap(String number, String person, String tense) {
		 Map<String, Integer> args = new HashMap<String, Integer>();
		 args.put("tense", RiTa.PRESENT_TENSE);
//		 args.put("person", person);
//		 args.put("number", number);
		return args;
	}*/

	// capitalise method called from Grammar file!
	String capitalise(String word) {

		return RiTa.upperCaseFirst(word);
	}

	// random number method , called from Grammar file
	String rand(float r) {

		int rr = ceil(random(1, r));
		String randomInt = str(rr);
		return randomInt;
	}

	// look for unique word, first find which rule the word belongs to then
	// keep picking from it until the word does not repeat itself
	String unique(String word) {

		// find which rule a word belongs to, code by Daniel Howe
		String tempBuffer = "";
		String target = word;
		String parentRule = "";
		int targetIndex = -1;
		String[] terminals = new String[200];
		Map defs = rg._rules;
		for (Iterator it = defs.keySet().iterator(); it.hasNext();) {
			String rule = (String) it.next();
			System.out.println(rule+": "+defs.get(rule));

			String def = (String) defs.get(rule); // converts all the terminals of the
																						// rule into a string

			// following command is neat text processing,
			// splits string at |, trims any whitespace
			// off end and start and builds string array.
			// Now all terminals are seperated. Useful?
			terminals = trim(split(def, "|"));

			for (int c = 0; c < terminals.length; c++) {
				if (terminals[c].equals(target)) {
					println("found " + target + " in rule: " + rule + " at index:" + c);
					parentRule = rule;
					targetIndex = c;
					break;
				}
			}

			if (targetIndex != -1) {
				break;
			}
		}

		String soFar = null;
		// TODO: soFar = rg.getBuffer(); // grab generated buffer of words so far
		println("target: " + target);
		tempBuffer = target;
		
		if (soFar.contains(target)) {

			println("target repeats");

			int q = 0;
			while (soFar.contains(tempBuffer)) {
				q++;
				tempBuffer = rg.expandFrom(parentRule, true);
				if (q > 1500) {
					println("breakOut!");
					break;
				} // try not to crash if the unique search gets stuck in loop
			}
		}
		
		return tempBuffer;
	}

	// return present participle
	String pp(String v) {

		String result = RiTa.getPresentParticiple(v);
		return result;
	}

	// pluralise

	String plural(String n) {

		/*
		 * RiPluralizer rp = new RiPluralizer(); RiStemmer rs = new RiStemmer();
		 * String stem = rs.stem(n); String result = rp.pluralize(n);
		 */
		return n;
	}
	
	String[] filesInSameDirectory(String path) {

		/**
		 * List Text Files modification of listing-files taken from
		 * http://wiki.processing.org/index.php?title=Listing_files
		 * 
		 * @author antiplastik
		 */

		// we'll have a look in the data folder
		java.io.File folder = new java.io.File(dataPath(path));

		// list the files in the data folder passing the filter as parameter
		return folder.list(movFilter);
	}

	// let's set a filter (which returns true if file's extension is .txt)

	java.io.FilenameFilter movFilter = new java.io.FilenameFilter() {

		public boolean accept(File dir, String name) {

			return ((name.toLowerCase().endsWith(".json")) || (name.toLowerCase().startsWith("Dance")));
		}
	};
	
	public static void main(String[] args) {

		System.out.println("Running " + GenerativeMovementLanguage.class.getName());
		// String[] options = { "--present", "--hide-stop","--bgcolor=#000000",
		String[] options = { "--hide-stop", "--bgcolor=#000000", GenerativeMovementLanguage.class.getName() };
		PApplet.main(options);
	}

}