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
import java.util.*;

import processing.core.PApplet;
import rita.*;
import rita.support.*;

import static processing.core.PApplet.*;
import static rita.RiTa.randomItem;
import static rita.RiTa.splitSentences;

public class GrammarGML {

	protected PApplet pApplet;
	protected RiGrammar grammar;
	protected Conjugator conjugator;
	protected String pathToWordLists;

	public String latestTimeStamp;

	public String lineBreaker; // set this to "%" as GrammarGML default
	private FileIOHelpers fileHelper;
	private File wordListFolder;
	private String grammarFilename;

	public String [] currentExpansion;
	public String [] closedClassWords = RiTa.CLOSED_CLASS_WORDS ;
	public String[] currentExpansionReduced;

	private HashMap<String, String>  fixedChoices;




	public GrammarGML(PApplet p) {
		this(p, "%");
	}

	public GrammarGML(PApplet p, String lineBreakChar) {

		pApplet = p;
		lineBreaker = lineBreakChar;
		conjugator = new Conjugator();
		fileHelper = new FileIOHelpers();
		grammar = new RiGrammar();

		// default wordLists path todo: make user definable and disk stored preference?
		pathToWordLists = "data/wordLists";
		latestTimeStamp= " started on " + this.timeStampWithDate();
		fixedChoices = new HashMap<>(64);

	}


	public void loadFrom(String fileName) {

		grammarFilename = fileName;
		File grammarFile = new File(grammarFilename);
		println("Grammar file location: " + grammarFile.getAbsolutePath());
		/*
		// not working yet...
		//
		fileHelper.checkFileExistsOrGetUserLocation(grammarFile, (File f) -> {
			println("Grammar selected " + f.getAbsolutePath());
			// Here is where you can work with the selected file 'f'
			// store it or whatever.
			grammarFilename = f.toString();
			// Carry on...

		});
*/

		println("Building Grammar from " + grammarFilename);
		// MDK : removed 'this.' from grammar, its redundant unless you
		// need to resolve name collisions.
		grammar = createGrammar(grammarFilename);


		//Now we've created the grammar, handle the word lists location
		//default wordList path should be data/wordLists defined in pathToWorldLists

		wordListFolder = new File(pathToWordLists);

		//File dialog to handle lost default path for wordList data
		//functionality will allow user to select folder location
		//in future version

		println("Word List Folder " + wordListFolder.getAbsolutePath());

		println( "exists? " +  (wordListFolder.exists()));


		fileHelper.checkFolderExistsOrGetUserLocation(wordListFolder, (File f) -> {
			println("Folder selected " + f.getAbsolutePath());
			// Here is where you can work with the selected file 'f'
			// store it or whatever.
			wordListFolder = f;
			// Carry on...
			loadWordLists();
			println("extra word lists loaded ok");
		});

	}




	public RiGrammar createGrammar(String grammarFile) {

		// MDK: If you only do this once I would move construction of RiGrammar into the GrammarGML constructor
		//	RiGrammar rg = new RiGrammar();

		grammar.loadFrom(grammarFile, pApplet);

		// MDK : Also we don't load the words immediately, we'll load them when the folder select has completed
		return grammar;
	}

	public String [] getGeneratedBufferAsLines() {
		return (splitSentences(grammar.buffer));  // Daniel Howe basic implementation of buffer access , may change
	}


	public String[] filesInSameDirectory(File asThisfile) {

		/**
		 * List Text Files modification of listing-files taken from
		 * http://wiki.processing.org/index.php?title=Listing_files
		 * @return string [] of filtered directory
		 * @param
		 * @author antiplastik
		 */

		//todo: functionality for selectable grammar files UI
		java.io.File folder = new java.io.File("/data");

		// list the files in the data folder passing the filter as parameter
		return folder.list(movFilter);
	}

	// returns a filter (which returns true if file's extension is .json )

	java.io.FilenameFilter movFilter = (dir, name) -> {

		//return ((name.toLowerCase().endsWith(".json")) || (name.toLowerCase().startsWith("Dance")));
		return (name.toLowerCase().endsWith(".json"));
	};

	/**
	 * Integrates new Rules and Tokens from the data/wordLists directory
	 * Rule name will be the filename and tokens will be its contents
	 * Rules are then added to the main grammar and can be referred to in the
	 * grammar files for example
	 * <bodyPartsPlural> will generate from data/wordLists/bodyPartsPlural.txt
	 */

	private void loadWordLists() {
		String[] wordListFilenames;
		wordListFilenames = wordListFolder.list((dir, name) -> name.toLowerCase().endsWith(".txt"));

		for (String wordListFilename : wordListFilenames) {

			String[] wordList = pApplet.loadStrings(wordListFolder.getAbsolutePath() + "/" + wordListFilename);
			String ruleName = "<" + wordListFilename.replaceAll(".txt", "") + ">";

			String terminals = "";
			for (String s : wordList) {
				terminals += s + " | ";
			}
			grammar.addRule(ruleName, terminals);
		}
	}


	/**
	 * Main grammar expander/generator
	 * @return probabilistically expanded lines of text in String array
	 */
	public String[] generateTextAndSplitAtLineBreak() {

		try {
			currentExpansion = grammar.expand(this).split(lineBreaker);

		} catch (IllegalStateException e) {
			String ritaError = RiTa.stackToString(e);
			int snip = ritaError.indexOf(">") + 2;
			println(ritaError);
			currentExpansion[0] = "There is something wrong in the grammar file definitions..\n\n" + ritaError.substring(0, snip) + "Syntax error?";
		}

		for (int i = 0; i < currentExpansion.length; i++) {
			currentExpansion[i] =  currentExpansion[i].trim();
		}

		latestTimeStamp = timeStampWithDate();
		currentExpansionReduced = stripRepeats(stripOpenClassWords(currentExpansion));
		//currentExpansionReduced = stripOpenClassWords(currentExpansion);
		return currentExpansion;
	}

	/**
	 * find which rule a word,  terminal or phrase belongs to, code by Daniel Howe
	 * @param terminal
	 * @return
	 */

	String getParentRuleFromTerminal(String terminal) {


		String target = terminal;
		String parentRule = "";
		int targetIndex = -1;
		String[] terminals;
		Map defs = grammar._rules;
		for (Object o : defs.keySet()) {
			String rule =  o.toString();
			println(rule + ": " + defs.get(rule));

			String def = defs.get(rule).toString();
			// converts all the terminals of the
			// rule into a string

			// following command is neat text processing,
			// splits string at |, trims any whitespace
			// from end and start then builds string array.
			// Now all terminals are separated.
			terminals = trim(split(def, "|"));

			for (int c = 0; c < terminals.length; c++) {
				if (terminals[c].contains(target)) {
					println("found " + target + " in rule: " + rule + " at index:" + c);
					parentRule = rule;
					targetIndex = c;
					break;
				}
			}
			if (targetIndex != -1) break;
		}

		return parentRule;
	}

	public String displayInfo() {

		String info = "";

		if (grammar._rules.containsKey("<info>")) {

			info = grammar.expandFrom("<info>");
		}

		return info;
	}

	public String toTitleCase( String lineOfText) {

		String[] tokens = lineOfText.split(" ");
		String result = "";

		for (int i=0; i< tokens.length; i++) {
			result = result.concat(capitalise((tokens[i]+" ")));
		}
		return result;
	}




    public String getLatestTimeStamp() {
        return latestTimeStamp;
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


	/**
	 * Shortens and title-izes a line of text
	 * @param lineOfText line to be title-ized
	 * @return short capitalised title
	 */

	public String generateTitleFromLineOfText(String lineOfText) {
		int len = 0;
		if (lineOfText.length() >= 16) {
			len = (int) pApplet.random(5, 16);
		} else {
			len = floor(lineOfText.length() / 2);
		}
		String result;
		int firstSpace = lineOfText.indexOf(' ' ,len);
		if (firstSpace == -1) {  firstSpace = lineOfText.indexOf(' ' ,0); }
		result = toTitleCase(lineOfText.substring( 0,firstSpace));
        return result;
	}

	/**
	 * Generates a title from lines of text
	 * @param linesOfText line to be title-ized
	 * @return short capitalised title
	 */

	public String generateRhymingTitleFromLinesOfText(String [] linesOfText) {

		if (linesOfText.length < 2) {
			generateTitleFromLineOfText(linesOfText[0]);
		}

		String line = (String) randomItem(linesOfText);
		String [] token = currentExpansionReduced;
		String w = (String) randomItem(token);
		if (w == null) {
			// no rhyme found
			w = generateTitleFromLineOfText(line);
			 }
		println("Rhyme with: " + w);
		return toTitleCase(randomItem(token)+" "+rhyme(w));
	}

	/**
	 * removes all Open Class Words (the, a, an , is etc)
	 * @param lines String Array of lines
	 * @return
	 */


	public  String [] stripOpenClassWords(String [] lines) {

		ArrayList filtered = new ArrayList<String>();
		String [] tokenizer;
		for (int i=0; i<lines.length; i++) {
			tokenizer = RiTa.tokenize(lines[i]);
			for (int j = 0; j < tokenizer.length; j++) {
				if (!Arrays.toString(closedClassWords).contains(tokenizer[j].toLowerCase())) {
					filtered.add(tokenizer[j]);
				}
			}
		}
		currentExpansionReduced = asStringArray(filtered);
		println ("filtered result:" + Arrays.toString(currentExpansionReduced));
		return currentExpansionReduced;
	}

	/** returns a random rhyming word
	 * @param word word to rhyme
	 */
	public String rhyme(String word) {
		return (String) randomItem(RiTa.similarBySound(word));
	}

	/** combines array of individual tokens into better lines layout
	 * @param numberPerLine number of words to consider as one line
	 * @param tokens array of individual tokens
	 */

	public String [] arrangeTokensIntoLines(String [] tokens, int numberPerLine) {
		ArrayList result = new ArrayList<String>();
		String temp;
		for (int j = 0; j <= (tokens.length-numberPerLine); j += numberPerLine) {
			temp = "";
			for (int i = 0; i < numberPerLine; i++) {
				temp += tokens[i+j] + " ";
			}
			result.add(temp);
		}
		return asStringArray(result);
	}


	/**
	 * Strips repeats and keeps order using Java8 Stream construct which I am getting into now
	 * https://www.javacodeexamples.com/java-string-array-remove-duplicates-example/849
	 */

	public String [] stripRepeats(String[] textArray) {
	return	Arrays.stream(textArray).distinct().toArray(String[]::new);
	}

	/**
	 * @deprecated
	 * Strips repeats from String []
	 * Does not keep order!
	 *
	 * @param textArray String array containing possible repeated tokens
	 * @return
	 */
	public String [] stripRepeatsUnordered(String[] textArray) {
		Set<String> textAsSet = asSet(textArray);
		return asStringArray(textAsSet);
	}

	/**
	 * Shuffles a collection of Strings
	 * @param collection string array to be shuffled
	 * @return shuffled array
	 */
	public String [] shuffle( String [] collection) {
		int [] randomIndex = RiTa.randomOrdering(collection.length);
		String [] result = new String[collection.length];
		for (int i=0; i<collection.length;i++) {
			result[i] = collection[randomIndex[i]];
		}
		return result;
	}

	/**
	 * Shuffles a collection of integers
	 * @param collection
	 * @return
	 */
	public int [] shuffle( int [] collection) {
		int [] randomIndex = RiTa.randomOrdering(collection.length);
		int [] result = new int[collection.length];
		for (int i=0; i<collection.length;i++) {
			result[i] = collection[randomIndex[i]];
		}
		return result;
	}

	/** Convert a List into String Array
	 * https://stackoverflow.com/questions/1018750/how-to-convert-object-array-to-string-array-in-java
	 * @param inputList List to convert (eg ArrayList )
	 **/
	public String [] asStringArray(List inputList) {
		return Arrays.stream(inputList.toArray()).toArray(String[]::new);
	}
	/**
	 * Convert String Array to Set (removes duplicates)
	 * https://stackoverflow.com/questions/11986593/java-how-to-convert-string-to-list-or-set
	 * @param inputArray String array to convert into Set
	 */
	public Set asSet(String [] inputArray) {
		Set<String> resultingSet = new HashSet<>(Arrays.asList(inputArray));
		return resultingSet;
	}
	/** Convert a Set into String Array
	 * https://stackoverflow.com/questions/1018750/how-to-convert-object-array-to-string-array-in-java
	 * @param inputSet Set to convert (eg ArrayList )
	 **/
	public String [] asStringArray(Set inputSet) {
		return Arrays.stream(inputSet.toArray()).toArray(String[]::new);
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
	 * Conjugates the correct verb inflection from an infinitive
	 * Adds " by" if conjugator deems the result to be Passive
	 *
	 * Example with random verb from existing wordlist:
	 * 	`conjugate(singular, 3rd, s, <transferenceOfWeight>);`
	 *
	 * Example inline:
	 * 	`conjugate(singular, 3rd, ing, 'run');`
	 *
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
	 * best to have three or more tokens in a rule
	 * Note the rule name syntax should NOT be enclosed in < >
	 * Example: `uniquePair( bodyPartsPlural, and);`
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
			if (i>20) { b = "the other "+a; }
		}
		return a + ' ' + prep + ' ' + b;
	}

	/**
	 * set a choice to a variable in the grammar
	 * and recall it later in the text when needed
	 * Example:
	 * `set(surface,<hardSurfaces>);'
	 * `get(surface);`
	 * if the key has not been set yet, or is mis-spelled (in the example above 'surface' is the key)
	 * then the key itself will be inserted into the text
	 * @param key a variable name to be referenced in the grammar
	 * @param terminal the generated terminal to be fixed
	 */

	String set(String key, String terminal) {
	fixedChoices.put( key, terminal);
	String fixed = fixedChoices.get(key);
	println(fixedChoices.values());
	return fixed;
	}

	String get(String key) {
		String value;
		if (fixedChoices.containsKey(key)) {
			 value = fixedChoices.get(key);
		} else { value = key;}

		return value;
	}


	/**
	 * repeat the same terminal in the output separated by a comma
	 * @param terminal
	 * @return
	 */

	String twice(String terminal) {

		String newChoice = grammar.expandFrom(getParentRuleFromTerminal(terminal));
		newChoice = newChoice + ", "+grammar.expandFrom(getParentRuleFromTerminal(terminal));
		return newChoice;
	}

	/**
	 * tries to place a unique word that does not appear anywhere else in the generation so far,
	 * first find which rule the word belongs to then
	 * keep picking from it until the word does not repeat itself
	 *
	 * @param terminal
	 */

	String unique(String terminal) {
		String parentRule = getParentRuleFromTerminal(terminal);
		String buffer = grammar.buffer;
		while (buffer.contains(" " + terminal)) {
			terminal = grammar.expandFrom(parentRule);
		}
		return terminal;
	}

}
