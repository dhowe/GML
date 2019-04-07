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



import static processing.core.PApplet.*;


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
		 * @return string [] of filtered directory
		 * @param
		 * @author antiplastik
		 */

		//todo: functionality for userdefinable grammarfile path
		java.io.File folder = new java.io.File("/data");

		// list the files in the data folder passing the filter as parameter
		return folder.list(movFilter);
	}

	// returns a filter (which returns true if file's extension is .json )

	java.io.FilenameFilter movFilter = (dir, name) -> {

		//return ((name.toLowerCase().endsWith(".json")) || (name.toLowerCase().startsWith("Dance")));
		return (name.toLowerCase().endsWith(".json"));
	};


	private void loadWordLists() {
		String[] wordListFilenames;
		wordListFilenames = wordListFolder.list((dir, name) -> name.toLowerCase().endsWith(".txt"));

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

		latestTimeStamp = timeStampWithDate();
		return lines;
	}

	/**
	 * Uses Processing's time formatting to construct day/month/year@hour:minute
	 * @return String formatted hour and minute
	 *
	 *
	 */
	public static String timeStampWithDate() {

		String dateStamp;
		String timeStamp = PApplet.hour() +":"+ PApplet.minute();


			//dateStamp = date.toString();
			// Java date version...needs more work to get right
			// using Processing date methods instead
			dateStamp = PApplet.day() +"/"+ PApplet.month() +"/"+ PApplet.year();

		return (dateStamp+"@"+timeStamp);
	}


	/**
	 * Uses Processing's time formatting to construct @hour:minute
	 * @return String formatted hour and minute
	 *
	 */
	public String timeStamp() {


		String timeStamp = PApplet.hour() +":"+ PApplet.minute();
		println(timeStamp);
		return ("@"+timeStamp);
	}

	public String generateTitleFromFirstLineOfText(String lineOfText){

        return lineOfText.substring( 0,lineOfText.indexOf(' ' ,16));
	}

	// --------------------------- callbacks from grammars --------------------------------

	/**
	 * Method called from Grammar using backtick syntax
	 * Example:  `capitalise(<timingAdverb>);`
	 * @param word Word to be Capitalised
	 * @return String with capital letter
	 *
	 *
	 */
	String capitalise(String word) {

		return RiTa.upperCaseFirst(word);
	}

	/**
	 * Method called from Grammar using backtick syntax
	 * Conjugates the correct verb infection from infinitive
	 * Adds " by" if conjugator deems the result to be Passive
	 * Example with random verb from existing wordlist:
	 * 	`conjugate(singular, 3rd, s, <transferenceOfWeight>);`
	 * Example inline:
	 * 	`conjugate(singular, 3rd, ing, "run");`
	 * @param number singluar or plural
	 * @param person 1st 2nd or 3rd
	 * @param tense ing = present progressive / s = present simple
	 * @param verb infinitive to be conjugated
	 * @return conjugated verb String
	 */

	String conjugate(String number, String person, String tense, String verb) {

		if (tense.equals("ing")) {
			tense = "present";
			conjugator.setProgressive(true);
		}
		else if (tense.equals("s")) {
			tense = "present"; // "present simple";
		}
		String conjugatedVerb = conjugator.conjugate(number, person, tense, verb);
		if (conjugator.isPassive()) conjugatedVerb += " by";
		return conjugatedVerb;
	}

	/**
	 * Method called from Grammar using backtick syntax
	 *
	 * @param floor lowest limit of range
	 * @param ceiling highest limit of range
	 * @return string representation of random integer
	 */
	String randomNumber(int  floor, int ceiling) {

		return str(ceil(pApplet.random(floor, ceiling)));
	}

	/**
	 * Method called from Grammar using backtick syntax
	 * tries to return a unique pairing from a particular rule
	 * ! Needs three or more tokens in a rule or could crash
	 * ! Note the rule name should NOT be enclosed in < >
	 * Example: `uniquePair( bodyPartsPlural, and)`
	 * @param ruleName rule name from which to make a unique pair
	 * @param prep combining preposition
	 * @return string formatted like this example 'leg and elbow'
	 */
	String uniquePair(String ruleName, String prep) {
		int i=0;
		String rule = '<' + ruleName + '>';
		String a = grammar.expandFrom(rule);
		String b = a;

		while (a.equals(b)) {
			b = grammar.expandFrom(rule); i++;
			println(String.format("Trying unique pair: %d times", i));
			if (i>20) { break; }
		}
		return a + ' ' + prep + ' ' + b;
	}

}
