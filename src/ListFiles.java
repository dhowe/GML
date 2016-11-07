import java.io.File;

import processing.core.PApplet;

public class ListFiles {
	
	static String[] filesInSameDirectory(PApplet p, String path) {

		/**
		 * List Text Files modification of listing-files taken from
		 * http://wiki.processing.org/index.php?title=Listing_files
		 * 
		 * @author antiplastik
		 */

		// we'll have a look in the data folder
		java.io.File folder = new java.io.File(p.dataPath(path));

		// list the files in the data folder passing the filter as parameter
		String[] results = folder.list(movFilter);

		return results;

	}

	// let's set a filter (which returns true if file's extension is .txt)

	static java.io.FilenameFilter movFilter = new java.io.FilenameFilter() {

		public boolean accept(File dir, String name) {

			return ((name.toLowerCase().endsWith(".txt")) || (name.toLowerCase().startsWith("Dance")));
		}
	};
}
