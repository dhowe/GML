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


import java.awt.*;
import java.io.File;

import com.sun.deploy.panel.ExceptionListDialog;
import processing.core.PApplet;
import rita.*;
import rita.support.Conjugator;

import static processing.core.PApplet.println;


public class GrammarGML {

	protected PApplet pApplet;
	protected RiGrammar grammar;
	protected Conjugator conjugator;
	protected String pathToWordLists;


	private String lineBreaker;
	private FileIOHelpers fileHelper;
	private File wordListFolder;

	public GrammarGML(PApplet p) {
		this(p, "/");
	}

	public GrammarGML(PApplet p, String lineBreakChar) {

		pApplet = p;
		lineBreaker = lineBreakChar;
		conjugator = new Conjugator();
		fileHelper = new FileIOHelpers();

		// default wordLists path todo: make user definable and disk stored preference?
		pathToWordLists = "data/wordLists";
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

		println( "exists?" +  ((File) wordListFolder).exists());


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
		RiGrammar rg = new RiGrammar();
		rg.loadFrom(grammarFile, pApplet);

		// MDK : Also we don't load the words immediately, we'll load them when the folder select has completed
		return rg;
	}

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

	public String[] expand() {

		String[] lines = grammar.expand(this).split(lineBreaker);
		for (int i = 0; i < lines.length; i++) {
			lines[i] =  lines[i].trim();
		}
		return lines;
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
