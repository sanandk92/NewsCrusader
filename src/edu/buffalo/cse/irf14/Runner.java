/**
 * 
 */
package edu.buffalo.cse.irf14;


import java.io.File;

import edu.buffalo.cse.irf14.document.Document;
import edu.buffalo.cse.irf14.document.Parser;
import edu.buffalo.cse.irf14.document.ParserException;
import edu.buffalo.cse.irf14.index.IndexReader;
import edu.buffalo.cse.irf14.index.IndexType;
import edu.buffalo.cse.irf14.index.IndexWriter;
import edu.buffalo.cse.irf14.index.IndexerException;

/**
 * @author nikhillo
 *
 */
public class Runner {

	/**
	 * 
	 */
	public Runner() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String ipDir = args[0];
		String indexDir = args[1];
		//more? idk!
		
		File ipDirectory = new File(ipDir);
		final String[] catDirectories = ipDirectory.list();
		
		String[] files;
		File dir;
		
		Document d = null;
		IndexWriter writer = new IndexWriter(indexDir);
	
	//	long startTime=System.currentTimeMillis();
		try {
			for (String cat : catDirectories) {
				
				dir = new File(ipDir+ File.separator+ cat);
				files = dir.list();
				if (files == null)
					continue;
				
				for (String f : files) { // NOPMD by SAnanda on 28/9/14 2:41 AM
					try {
						d = Parser.parse(dir.getAbsolutePath() + File.separator +f);

						writer.addDocument(d);
						
					} catch (ParserException e) {
						// TODO Auto-generated catch block
						System.out.println("ParserException: "+e.getMessage());
					} 
					
				}
				
			}
			
			writer.close();
			
		//	System.out.println("\nTime Taken="+(System.currentTimeMillis()-startTime)+"\n"+"\nParser Skipped File:"+Parser.skippedCount);
		} catch (IndexerException e) {
			// TODO Auto-generated catch block
			System.out.println("IndexerException: "+e.getMessage());
		}
	}

}
