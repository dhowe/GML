package com.neverEngineLabs.GML2019;
/*
Generative Movement Grammar

A token based score generator which uses context-free grammars
and (optional) word lists to generate scores. By working with tokens
as the mode of generated meaning, a score can be interpreted
by human performers and/or virtual agents during realtime performance

GML system and codebase was conceived and maintained by Cristian Vogel
with essential contributions from Daniel Howe of RiTa lib and MDK of Korisna Media
 */



import java.io.File;
import processing.core.PApplet;
import rita.*;
import rita.support.Conjugator;


import static processing.core.PApplet.println; //need to do static import of Processing methods


public class GrammarGML {


	protected PApplet pApplet;
	protected RiGrammar grammar;
	protected Conjugator conjugator;
	protected String pathToWordLists;

	public String latestTimeStamp;

	private String lineBreaker;
	private FileIOHelpers fileHelper;
	private File wordListFolder;
	private RiGrammar rg;




    public GrammarGML(PApplet p) {
		this(p, "/");
	}

	public GrammarGML(PApplet p, String lineBreakChar) {

		pApplet = p;
		lineBreaker = lineBreakChar;
		conjugator = new Conjugator();
		fileHelper = new FileIOHelpers();
         rg = new RiGrammar();

		// default wordLists path todo: make user definable and disk stored preference?
		pathToWordLists = "data/wordLists";
		latestTimeStamp="";
	}


	public void loadFrom(String grammarFile) {
		println("Load Grammar from " + grammarFile);
		// MDK : removed 'this.' from grammar, its redundant unless you
		// need to resolve name collisions.
		grammar = createGrammar(grammarFile);


		//Now we've created the grammar, handle the word lists location
		//default wordList path should be data/wordLists defined in pathToWorldLists

		wordListFolder = new File(pathToWordLists);

		//File dialog to handle lost default path for wordList data
		//functionality will allow user to select folder location
		//in future version

		println("Word List Folder " + wordListFolder.getAbsolutePath());

		println( "exists? " +  ((File) wordListFolder).exists());


		fileHelper.checkFolderExistsOrGetUserLocation(wordListFolder, (File f) -> {
			println("Folder selected " + f.getAbsolutePath());
			// Here is where you can work with the selected file 'f'
			// store it or whatever.
			wordListFolder = f;
			// Carry on...
			loadWordLists();
		});

	}




	public RiGrammar createGrammar(String grammarFile) {

		// MDK: If you only do this once I would move construction of RiGrammar into the GrammarGML constructor
	//	RiGrammar rg = new RiGrammar();
		rg.loadFrom(grammarFile, pApplet);

		// MDK : Also we don't load the words immediately, we'll load them when the folder select has completed
		return rg;
	}

	public String[] filesInSameDirectory(File asThisfile) {

		/**
		 * List Text Files modification of listing-files taken from
		 * http://wiki.processing.org/index.php?title=Listing_files
		 *
		 * @author antiplastik
		 */

		//todo: functionality for userdefinable grammarfile path
		java.io.File folder = new java.io.File("/data");

		// list the files in the data folder passing the filter as parameter
		return folder.list(movFilter);
	}

	// returns a filter (which returns true if file's extension is .json )

	java.io.FilenameFilter movFilter = new java.io.FilenameFilter() {

		public boolean accept(File dir, String name) {

			//return ((name.toLowerCase().endsWith(".json")) || (name.toLowerCase().startsWith("Dance")));
			return (name.toLowerCase().endsWith(".json"));
		}
	};


	private void loadWordLists() {
		String[] wordListFilenames = wordListFolder.list((dir, name) -> name.toLowerCase().endsWith(".txt"));

		for (int i = 0; i < wordListFilenames.length; i++) {

			String[] wordList = pApplet.loadStrings(wordListFolder.getAbsolutePath() + "/" + wordListFilenames[i]);
			String ruleName = "<" + wordListFilenames[i].replaceAll(".txt", "") + ">";

			String terminals = "";
			for (int j = 0; j < wordList.length; j++) {
				terminals += wordList[j] + " | ";
			}
			grammar.addRule(ruleName, terminals);
		}
	}

	public String toTitleCase( String lineOfText) {

		String[] tokens = lineOfText.split(" ");
		String result = "";

		for (int i=0; i< tokens.length; i++) {
			result = result.concat(capitalise((tokens[i]+" ")));
		}
		return result;
	}

	public String[] generateTextAndSplitAtLineBreak() {

		String[] lines = grammar.expand(this).split(lineBreaker);
		for (int i = 0; i < lines.length; i++) {
			lines[i] =  lines[i].trim();
		}

		latestTimeStamp = timeStamp(true);
		return lines;
	}

	/* method returns a string with a formatted time stamp
	optionally add date
	*/
	public static String timeStamp( boolean addDate) {

		String dateStamp ="";
		String timeStamp = PApplet.hour() +":"+ PApplet.minute();
		if (addDate) {

			//dateStamp = date.toString();
			// Java date version...needs more work to get right
			// using Processing date methods instead
			dateStamp = PApplet.day() +"/"+ PApplet.month() +"/"+ PApplet.year();
		}
		return (dateStamp+"@"+timeStamp);
	}


	public String timeStamp( ) {


		String timeStamp = PApplet.hour() +":"+ PApplet.minute();
		return ("@"+timeStamp);
	}

	public String generateTitleFromLineOfText( String lineOfText){

        return lineOfText.substring( 0,lineOfText.indexOf(' ' ,16));
	}

	// --------------------------- callbacks from grammars --------------------------------

	String capitalise(String word) {

		return RiTa.upperCaseFirst(word);
	}

	String conjugate(String number, String person, String tense, String verb) {

		if (tense.equals("ing")) {
			tense = "present";
			conjugator.setProgressive(true);
		}
		else if (tense.equals("s")) {
			tense = "present"; // "present simple";
		}
		String s = conjugator.conjugate(number, person, tense, verb);
		if (conjugator.isPassive()) s += " by";
		return s;
	}

	String rand(float r) {

		return PApplet.str(PApplet.ceil(pApplet.random(1, r)));
	}

	String uniquePair(String ruleName, String prep) {

		String rule = '<' + ruleName + '>';
		String a = grammar.expandFrom(rule), b = a;
		while (a.equals(b)) b = grammar.expandFrom(rule);
		return a + ' ' + prep + ' ' + b;
	}

}
