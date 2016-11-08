import java.io.File;

import processing.core.PApplet;
import rita.*;
import rita.support.Conjugator;

public class GrammarGML {
	
	protected PApplet pApplet;
	protected RiGrammar grammar;
	protected Conjugator conjugator;
	private String lineBreaker;

	public GrammarGML(PApplet p) {
		this(p, "/");
	}
		
	public GrammarGML(PApplet p, String lineBreakChar) {

		this.pApplet = p;
		this.lineBreaker = lineBreakChar;
		this.conjugator = new Conjugator();
	}
	
	public void loadFrom(String grammarFile) {
		this.grammar = createGrammar(grammarFile);
	}
	
	public String[] expand() {
		
		String[] lines = grammar.expand(this).split(lineBreaker);
		for (int i = 0; i < lines.length; i++) {
			lines[i] =  lines[i].trim();
		}
		return lines;
	}

	public RiGrammar createGrammar(String grammarFile) {
		
		RiGrammar rg = new RiGrammar();
		rg.loadFrom(grammarFile, this.pApplet);
		
		String pathToWordLists = pApplet.dataPath("");
		File file = new java.io.File(pathToWordLists);
		String[] wordListFilenames = file.list(movFilter);

		for (int i = 0; i < wordListFilenames.length; i++) {
			
			String[] wordList = pApplet.loadStrings(pathToWordLists + "/" + wordListFilenames[i]);
			String ruleName = "<" + wordListFilenames[i].replaceAll(".txt", "") + ">"; 
			
			String terminals = "";
			for (int j = 0; j < wordList.length; j++) {
				terminals += wordList[j] + " | ";
			}
			rg.addRule(ruleName, terminals);
		}
		
		return rg;
	}

	java.io.FilenameFilter movFilter = new java.io.FilenameFilter() {
		public boolean accept(File dir, String name) {
			return name.toLowerCase().endsWith(".txt");
		}
	};
	
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
