package com.neverEngineLabs.GML2019;

import processing.core.PApplet;
import java.io.File;


public class FileIOHelpers extends PApplet
{

    //import interface to handle PApplet file selector callback
    FileSelectionHandler handler;

    public File selectedFolder;
    public boolean waiting;
    public File selectedFile;


    public void checkFolderExistsOrGetUserLocation(File path, FileSelectionHandler f) {

        // Store the handler to use later (the lambda we supply as the 2nd argument)
        handler = f;

        selectedFolder = null;

        if (!path.exists()) {
            waiting = true;
            println("searching for folder...");
            //gets local path maybe doesn't work once built?
            path = new File(PApplet.calcSketchPath());

            //for some reason, does not show Prompt in MacOS Mojave native...
            selectFolder("Looking for "+(path.toString()), "folderSelected", path, this, null, null);

        } else {
            // Path does exist, so just hand it back straight away
            f.onFileSelected(path);
        }
    }

    public void checkFileExistsOrGetUserLocation(File fileToLookFor, FileSelectionHandler f) {

        // Store the handler to use later (the lambda we supply as the 2nd argument)
        handler = f;

        selectedFolder = null;

        if (!fileToLookFor.exists()) {
            waiting = true;
            println("searching for file....");
            //gets local path maybe doesn't work once built?
            File path = new File(PApplet.calcSketchPath());

            //for some reason, does not show Prompt in MacOS Mojave native...
            selectInput("Locate a valid grammar file...", "fileSelected", path, this, null, null);

        } else {
            // Path does exist, so just hand it back straight away
            f.onFileSelected(fileToLookFor);
        }
    }

    // apparently folderSelected needs to be public for Processing to be able to call it back.
    public void folderSelected(File selection) {
        if (selection == null) {
            PApplet.println("Window was closed or the user hit cancel.");
        } else {
            PApplet.println("User selected " + selection.getAbsolutePath());
        }
        // Pass the selection folder back to our code
        handler.onFileSelected(selection);
        // NOTE : No return value.
        waiting = false;
        selectedFolder = selection;
    }


    public void fileSelected(File selection) {
        if (selection == null) {
            PApplet.println("Window was closed or the user hit cancel.");
        } else {
            PApplet.println("User selected file " + selection.getAbsolutePath());
        }
        // Pass the selection folder back to our code
        handler.onFileSelected(selection);
        // NOTE : No return value.
        waiting = false;
        selectedFile = selection;
    }


    public File getPathToDesktopFolder() {

        return new File(System.getProperty("user.home") + "/Desktop");
    }


}

