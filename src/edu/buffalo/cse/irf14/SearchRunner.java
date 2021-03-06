package edu.buffalo.cse.irf14;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.buffalo.cse.irf14.analysis.Analyzer;
import edu.buffalo.cse.irf14.analysis.AnalyzerFactory;
import edu.buffalo.cse.irf14.analysis.TokenStream;
import edu.buffalo.cse.irf14.analysis.Tokenizer;
import edu.buffalo.cse.irf14.analysis.TokenizerException;
import edu.buffalo.cse.irf14.document.FieldNames;
import edu.buffalo.cse.irf14.index.FileUtilities;
import edu.buffalo.cse.irf14.index.IndexReader;
import edu.buffalo.cse.irf14.index.IndexType;
import edu.buffalo.cse.irf14.index.IndexWriter;
import edu.buffalo.cse.irf14.query.Query;
import edu.buffalo.cse.irf14.query.QueryParser;

/**
 * Main class to run the searcher.
 * As before implement all TODO methods unless marked for bonus
 * @author nikhillo
 *
 */
public class SearchRunner {
	public enum ScoringModel {TFIDF, OKAPI};
	
	
	TreeMap <Integer, Double> final_result;
	final double k1=1.2,k3=2,b=0.75;
	PrintStream o_stream;
	char mode;
	private String corpusDir;
	String indexDir;
	Query q;
	/**
	 * Default (and only public) constuctor
	 * @param indexDir : The directory where the index resides
	 * @param corpusDir : Directory where the (flattened) corpus resides
	 * @param mode : Mode, one of Q or E
	 * @param stream: Stream to write output to
	 */
	public SearchRunner(String indexDir, String corpusDir, 
			char mode, PrintStream stream) {
		//TODO: IMPLEMENT THIS METHOD
		
		this.indexDir = indexDir;
		this.corpusDir= corpusDir;
		this.mode = mode;
		FileUtilities.setOutputDir(indexDir);
		readIndex();
		o_stream=stream;
		 final_result=new TreeMap<Integer, Double>();
		 finalp= new ArrayList<Map.Entry<Integer, Double>>();
	}
	
	HashMap<String, Double> qterms=new HashMap<String, Double>();
	//HashMap<Integer, Double> docterms=new HashMap<Integer, Double>();
	String ori_term="";
	Tokenizer t=new Tokenizer();
	Analyzer analyzer;
	List<Map.Entry<Integer, Double>> finalp;
	final AnalyzerFactory fact = AnalyzerFactory.getInstance();
	int flag=0;
	
	public void readIndex() {
		FileUtilities.readDocDic();
		
			IndexWriter.termIndexAC = (TreeMap<String, HashMap<Integer,Double>>) FileUtilities.readIndexFile(FileUtilities.indexAC);
			IndexWriter.termIndexDG = (TreeMap<String, HashMap<Integer,Double>>) FileUtilities.readIndexFile(FileUtilities.indexDG);
			IndexWriter.termIndexHK = (TreeMap<String, HashMap<Integer,Double>>) FileUtilities.readIndexFile(FileUtilities.indexHK);
			IndexWriter.termIndexLP = (TreeMap<String, HashMap<Integer,Double>>) FileUtilities.readIndexFile(FileUtilities.indexLP);
			IndexWriter.termIndexQS = (TreeMap<String, HashMap<Integer,Double>>) FileUtilities.readIndexFile(FileUtilities.indexQS);
			IndexWriter.termIndexTZ = (TreeMap<String, HashMap<Integer,Double>>) FileUtilities.readIndexFile(FileUtilities.indexTZ);
			IndexWriter.termIndexMisc = (TreeMap<String, HashMap<Integer,Double>>) FileUtilities.readIndexFile(FileUtilities.indexMisc);
		
			IndexWriter.AuthorIndex=(TreeMap<String, ArrayList<Integer>>) FileUtilities.readIndexFile(FileUtilities.indexAuth);
		
			IndexWriter.CatIndex=(TreeMap<String, ArrayList<Integer>>) FileUtilities.readIndexFile(FileUtilities.indexCat);
		
			IndexWriter.PlaceIndex=(TreeMap<String, ArrayList<Integer>>) FileUtilities.readIndexFile(FileUtilities.indexPlace);
			
	}
	
	public TreeMap<Integer, Double> getPostings(String term, IndexType type, String op) 
	{
		String term_f;
		
		TreeMap<Integer, Double> postingsMap_f=new TreeMap<Integer, Double>();
		try
		{
		TokenStream t_stream=t.consume(term);
		analyzer = fact.getAnalyzerForField(FieldNames.CONTENT, t_stream);
		
		while (analyzer.increment()) {
			
		}
		if(t_stream!=null && t_stream.hasNext())
		{
			term_f=t_stream.next().toString();
		
			if(flag==0 && !term_f.equals(term)){
				ori_term=term;
				flag=1;
				postingsMap_f=getPostings(term_f, type, "OR");
			}
		}
			if(ori_term.equals(""))
				ori_term=term;
		}
		catch(TokenizerException e)
		{
			
		}
		
		TreeMap<Integer, Double> postingsMap=new TreeMap<Integer, Double>();
		HashMap<Integer, Double> postingList;
		ArrayList<Integer> postingArray;
		double idf=0,tf=0,num;
		Double qw=0.0,Ld=0.0,Lave=0.0,den;
		Double w=0.0;
		
		switch(type){
			case TERM:
				if(null!=term && !term.isEmpty()){
					term=term.toLowerCase();
				
					switch(term.charAt(0)){
					case 'a': case 'b': case 'c':
						postingList=IndexWriter.termIndexAC.get(term);
						
						break;
					case 'd': case 'e': case 'f': case 'g':
						postingList=IndexWriter.termIndexDG.get(term);
						
						break;
					case 'h': case 'i': case 'j': case 'k':
						postingList=IndexWriter.termIndexHK.get(term);
						
						break;
					case 'l': case 'm': case 'n': case 'o': case 'p':
						postingList=IndexWriter.termIndexLP.get(term);
							
						break;
					case 'q': case 'r': case 's':
						postingList=IndexWriter.termIndexQS.get(term);
						
						break;
					case 't': case 'u': case 'v': case 'w': case 'x': case 'y': case 'z':
						postingList=IndexWriter.termIndexTZ.get(term);
						
						break;
					default :
						postingList=IndexWriter.termIndexMisc.get(term);
						
					}
					if(postingList!=null)
					{
						idf=postingList.get(-1);
						for(Integer docId: postingList.keySet()){
							tf=postingList.get(docId);
							
							w=postingsMap.get(docId);
							qw=qterms.get(ori_term);
							
							if(qw==null)
								qw=0.0;
							if(w==null)
								w=0.0;
							
							if(currentmodel==ScoringModel.TFIDF)
							{
								num=(1+Math.log(tf))*idf;
								w+=qw*idf*num; // or use log(1+tf).idf
							//	den=docterms.get(docId);
							//	if(den==null)
							//		den=0.0;
							//	docterms.put(docId, den + (num * num));
							}
							else
							{
								try
								{
								Ld=Double.parseDouble(IndexWriter.docCatList.get(docId)[1]);
								}
								catch(Exception e)
								{
								Ld=0.0;
								}
								try
								{
								Lave=Double.parseDouble(IndexWriter.docCatList.get(-1)[1]);
								}
								catch(Exception e)
								{
								Lave=0.0;
								}
								num=idf*(k1+1)*tf*(k3+1)*qw;
								den=k1 * ((1-b) + (b * (Ld/Lave)));
								w+=num/((k3+qw)*(den+tf));
								
							}
			
							postingsMap.put(docId, w);
						//	qterms.put(term, qw);
						}
					}
					
				}
				break;
			case AUTHOR:
				postingArray=IndexWriter.AuthorIndex.get(term);
				if(postingArray!=null){
					
					for(Integer docId: postingArray){
						w=postingsMap.get(docId);
						if(w==null)
							w=0.0;
						w+=1;
						postingsMap.put(docId, w);
					}
				}
				break;
			case CATEGORY:
				postingArray=IndexWriter.CatIndex.get(term);
				if(postingArray!=null){
					
					for(Integer docId: postingArray){
						w=postingsMap.get(docId);
						if(w==null)
							w=0.0;
						w+=1;
						postingsMap.put(docId, w);
					}
				}
				break;
			case PLACE:
				postingArray=IndexWriter.PlaceIndex.get(term);
				if(postingArray!=null){
				
					for(Integer docId: postingArray){
						w=postingsMap.get(docId);
						if(w==null)
							w=0.0;
						w+=1;
						postingsMap.put(docId, w);
					}
				}
				break;
		}
	
		if(postingsMap_f.size()>0)
			postingsMap=mergePostings(postingsMap, postingsMap_f, "OR");
		return postingsMap;
	}
	int iter=0;
	String defOp="AND";
	String op=defOp;
	public TreeMap<Integer, Double> processblock(TreeMap<Integer, Double> postings1,String val, String[] k) 
	{
		TreeMap<Integer, Double> postings2;
		int i=iter;
		IndexType itype=IndexType.TERM;
		if(val.equals("AND") || val.equals("OR"))
			op=val;
		else
		{
			if(val.contains("<")) // Handle NOT
			{
				op="NOT";
				val=val.replace("<", "");
				val=val.replace(">", "");
			}
			if(val.contains(":"))
			{
			String c_split[]=val.split(":");
			
			if(c_split[0].equals("Term"))
				itype=IndexType.TERM;
			else if(c_split[0].equals("Author"))
				itype=IndexType.AUTHOR;
			else if(c_split[0].equals("Category"))
				itype=IndexType.CATEGORY;
			else if(c_split[0].equals("Place"))
				itype=IndexType.PLACE;
			else if(c_split[0].equals("Term"))
				itype=IndexType.TERM;
			
			val=c_split[1];
			}
			if(val.contains("\""))
			{
				if(!val.endsWith("\""))
				{
					while(!k[++i].contains("\""))
					{
						val+=" "+k[i];	
					}
					val+=" "+k[i];
				}
				val=val.replace("\"", "");
			}
			Double temp=qterms.get(val);
			if(temp==null)
				temp=0.0;
			if(op!="NOT")
				qterms.put(val, temp + 1.0);
			else
				qterms.put(val, 0.0);
			flag=0;
			postings2=getPostings(val,itype,op);
			if(postings1.size()>0)
			postings1=mergePostings(postings1,postings2,op);
			else
				postings1=postings2;
		}
		return postings1;
	}
	
	public TreeMap<Integer,Double> mergePostings(TreeMap<Integer,Double> A, TreeMap<Integer,Double> B,String operator)
	{
		Double d;
		Integer[] akeys;
		if(operator.equals("AND"))
		{
			akeys=A.keySet().toArray(new Integer[A.size()]);
			for(Integer a:akeys)
			{
				d=B.get(a);
				if(d!=null)
					A.put(a, A.get(a)+d);
				else
					A.remove(a);
			}

		}
		else if(operator.equals("OR"))
		{
			for(Integer b:B.keySet())
			{
				d=A.get(b);
				if(d!=null)
					A.put(b, B.get(b)+d);
				else
					A.put(b, B.get(b));
			}
		}
		else
		{
			for(Integer b:B.keySet())
				A.remove(b);
		}
		return A;
	}
	int recur_flag=0;
	ScoringModel currentmodel;
	int res_count=1,res_total=10;
	long startTime;
	/**
	 * Method to execute given query in the Q mode
	 * @param userQuery : Query to be parsed and executed
	 * @param model : Scoring Model to use for ranking results
	 */
	TreeMap<Integer, Double> postings=null;
	
	public void query(String userQuery, ScoringModel model) {
		//TODO: IMPLEMENT THIS METHOD
		 Double max=0.0;
		currentmodel=model;
		
		queryProcessor(userQuery);

		Entry<Integer, Double> doc;
		if(recur_flag==0 && finalp.size()<10)
		{
			recur_flag=1;
			defOp="OR";
			op="OR";
			qterms.clear();
			query(userQuery,model);
			recur_flag=0;
			op="AND";
			defOp="AND";
		}
		else
		{
		double sc=0.0;
		if(postings.size()<1)
		{
			o_stream.println("No Results Found!");
			return;
		}
		max=Collections.max(postings.values());
	
		long time=System.currentTimeMillis()-startTime;
		o_stream.println("Query: "+userQuery);
		o_stream.println("Query time: "+time + " ms");
		o_stream.println("\n");
		String[] snippet_title;
		for(int i=0;res_count<=res_total && i<finalp.size();i++)
		{
			
			doc = finalp.get(i);
			snippet_title=getSnippet_title(IndexWriter.docCatList.get(doc.getKey())[0]);
			sc=(doc.getValue()-0)/(max-0);
			if(sc==0 || doc.getKey()==0)
				i++;
			else
			{
			o_stream.println("Result Rank: "+res_count++);
			o_stream.println("Result title: "+snippet_title[0]);
            o_stream.println("Result snippet: "+snippet_title[1]);
			o_stream.println("Result relevance: "+String.format("%.5f",sc));
			
			o_stream.println("\n");
			}
		}
		res_count=1;
		}

	}
	
	
	/**
	 * Method to execute queries in E mode
	 * @param queryFile : The file from which queries are to be read and executed
	 */
	public void query(File queryFile) {
		//TODO: IMPLEMENT THIS METHOD
//		numQueries=3
//				Q_1A63C:{hello world}
//				Q_6V87S:{Category:oil AND place:Dubai AND ( price OR cost )}
//				Q_4K66L:{long query with several words}
		
		FileReader f_in = null;
		BufferedReader br= null;
		 List<Map.Entry<Integer, Double>> finalPostings;
	        ArrayList<String> outputLines= new ArrayList<String>(); 
	        String outputLine;
		try {
			f_in = new FileReader(queryFile);
			br= new BufferedReader(f_in);
			String line;
			int queryNum=0;
			String queryId;
			String userQuery;
			
			if((line=br.readLine())!=null){
				if(line.trim().startsWith("numQueries=")){
					queryNum=Integer.parseInt(line.substring(line.indexOf("numQueries=")+11));
				}else{
					System.out.println("Corrupted Query File.Invalid numQueries statement");
				}
			}
			int i=0;
			while(i++<queryNum){
				if((line=br.readLine())!=null){
					if(line.contains(":")){
						queryId=line.substring(0,line.indexOf(":"));
						userQuery=line.substring(line.indexOf(":")+2,line.length()-1);
						postings=null;
						finalp.clear();
						final_result.clear();
						currentmodel=ScoringModel.OKAPI;
						 finalPostings= queryProcessor(userQuery);
					
							if(recur_flag==0 && finalPostings.size()<10)
							{
								recur_flag=1;
								defOp="OR";
								op="OR";
								qterms.clear();
								finalPostings=queryProcessor(userQuery);
								recur_flag=0;
								op="AND";
								defOp="AND";
							}
							
	                        if(finalPostings!=null && !finalPostings.isEmpty()){
	                        	double max=Collections.max(postings.values()),sc=0;
	                            outputLine=queryId+":{";
	                            res_count=1;
	                            for(Map.Entry<Integer,Double> posting: finalPostings){
	                            	if(res_count++>res_total)
	                            		break;
	                            	sc=(posting.getValue()-0)/(max-0);
	                                outputLine+=IndexWriter.docCatList.get(posting.getKey())[0]+"#"+String.format("%.5f",sc)+", ";
	                            }
	                            res_count=1;
	                            outputLine=outputLine.substring(0, outputLine.length()-2)+"}";
	                            outputLines.add(outputLine);
	                        
					}else{
						System.out.println("Error at line "+i+" in QueryString");
						break;
					}
				}else{
					System.out.println("Query "+i+" missing in file");
					break;
				}
			}
		}
			if(outputLines!=null && !outputLines.isEmpty() ){
                o_stream.println("numResults="+outputLines.size());
                for(String temp: outputLines){
                    o_stream.println(temp);
                }
            }else{
                
                o_stream.println("numResults=0");
            }
	
        }catch(IOException ioe){
            
        }
 
    }
 
    
    public List<Map.Entry<Integer, Double>> queryProcessor(String userQuery){
    	ScoringModel model=currentmodel;
    	if(recur_flag==0)
			startTime=System.currentTimeMillis();
    	q=QueryParser.parse(userQuery, defOp);
    	if(userQuery.contains("*") || userQuery.contains("?"))
    		processWCQueryTerms();
		String fq=q.toString();
		String[] k=fq.split(" ");
		if(postings==null)
		postings=new TreeMap<Integer,Double>();
		String tempop=defOp;
		String val;
		for(iter=0;iter<k.length;iter++)
		{
			val=k[iter];
			if(val.contains("*") || val.contains("?"))
			{
				String oldval=val;
				String[] vsp=val.split(":");
				StringBuilder sb=new StringBuilder();
				List<String> lst= null;
				if(wcQueryTerms!=null)
					lst = wcQueryTerms.get(val);
				System.out.println(wcQueryTerms+","+val);
				if(lst!=null)
				{
					
				for(int i=0;i<lst.size();i++)
				{
					val=lst.get(i);
					if(!val.contains(" "))
					{
						sb.append(vsp[0]);
						sb.append(":");
					}
					sb.append(val);
					if(i==0)
						val=sb.toString();
					if(i<lst.size()-1)
						sb.append(" OR ");
				}
					fq=fq.replace(oldval, sb.toString());
					fq=fq.replace("{ ", "");
					fq=fq.replace(" }", "");
					//System.out.println("ov:"+oldval+" sb:"+sb.toString()+" Mfq:"+fq);
					q=QueryParser.parse(fq, "OR");
					fq=q.toString();
					k=fq.split(" ");
					//System.out.println("finq:"+fq);
				}
					
			}
			if(val.equals("["))
			{
				TreeMap<Integer, Double> temp=new TreeMap<Integer,Double>();
				val=k[++iter];
				tempop=op;
				while(!val.equals("]"))
				{
					temp=processblock(temp, val,  k);
					val=k[++iter];
				}
				postings=mergePostings(postings,temp,tempop);
			}
			else if(!val.equals("{") && !val.equals("}"))
			{
				postings=processblock(postings,val,k);
			}
			else if(val.equals("AND") || val.equals("OR"))
				tempop=val;
		}
		 postings.remove(-1);
		double doc_len=0,score=0;
	
	
		if(model==ScoringModel.TFIDF)
		{
			double length=0;
			for(double d:qterms.values())
				length+=d*d;
			length=Math.sqrt(length);
			for(Integer docID:postings.keySet())
			{
				try
				{
				doc_len=Double.parseDouble(IndexWriter.docCatList.get(docID)[1]);
				}
				catch(Exception e)
				{
					doc_len=0.0;
				}
			
				score=postings.get(docID)/(length * doc_len);
				postings.put(docID, score);
			}	
		}
		else
		{
			for(Integer docID:postings.keySet())
			{
				score=postings.get(docID);
				postings.put(docID, score);
			}
		}
		   Double min=0.0;
		    if(postings.size()>0)
		    {
		    	
		    for(Entry<Integer, Double> ent : postings.entrySet())
		    {
		    	min=final_result.get(ent.getKey());
		    	if(min==null)
		    		min=0.0;
		    	postings.put(ent.getKey(), Math.max(ent.getValue(),min));
		    }
		    
		    final_result.putAll(postings);
		    finalp=IndexReader.entriesComparator(final_result);
		    }
	
            return finalp;
    }
    
    
    public String[] getSnippet_title(String docId ){
        String[] snippet_title= new String[2];
        String snippet="";
        String docContent="";
        ArrayList<String> sentenceList=new ArrayList<String>();
        HashMap<String,Integer> sentenceWeight=new HashMap<String,Integer>();
        File doc=new File(corpusDir+File.separator+docId);
        try{
            FileReader fr= new FileReader(doc);
            BufferedReader br= new BufferedReader(fr);
            String line;
            String title="NO TITLE FOUND";
            boolean titleFlag=true;
            while((line=br.readLine())!=null){
                if (!line.trim().isEmpty() && titleFlag) {
                    title= line;
                    titleFlag = false;
                }
                docContent+=line+" ";
            }
            Pattern sentencePattern = Pattern.compile("[^.!?\\s][^.!?]*(?:[.!?](?!['\"]?\\s|$)[^.!?]*)*[.!?]?['\"]?(?=\\s|$)", Pattern.MULTILINE | Pattern.COMMENTS);
            Matcher sentenceMatcher = sentencePattern.matcher(docContent);
            while (sentenceMatcher.find()) {
                sentenceList.add(sentenceMatcher.group());
            }
            int weight;
            Pattern termPattern;
            Matcher termMatcher;
            boolean uniqueFlag;
            int uniqueCount=0;
            String matchedString;
            for(String sentence: sentenceList){
                weight=0;
                uniqueCount=0;
                for(String term: qterms.keySet()){
                    uniqueFlag=true;
                    if(qterms.get(term)!=0){
                    termPattern=Pattern.compile("(?i)"+term);
                    termMatcher= termPattern.matcher(sentence);
                        while (termMatcher.find()) {
                            matchedString= termMatcher.group();
                            sentence=sentence.replaceFirst(matchedString, "<b>"+matchedString+"</b>");
                            if(uniqueFlag){
                                uniqueCount++;
                                uniqueFlag=false;
                            }
                            weight++;
                        }
                    }
                }
                sentenceWeight.put(sentence, weight*uniqueCount);
            }
           List<Entry<String,Integer>> sortedSentenceWeight= IndexReader.entriesComparator(sentenceWeight);
           
           for(int i=0,len= sortedSentenceWeight.size();i<len && i<3;i++ ){
               snippet+=sortedSentenceWeight.get(i).getKey()+"...";
           }
           
           snippet_title[0]=title;
           snippet_title[1]=snippet;
		}catch(IOException ioe){
			
		}
        return snippet_title;
	}
	
	/**
	 * General cleanup method
	 */
	public void close() {
		//TODO : IMPLEMENT THIS METHOD
		o_stream.close();
	}
	
	/**
	 * Method to indicate if wildcard queries are supported
	 * @return true if supported, false otherwise
	 */
	public static boolean wildcardSupported() {
		//TODO: CHANGE THIS TO TRUE ONLY IF WILDCARD BONUS ATTEMPTED
		return true;
	}
	
	/**
	 * Method to get substituted query terms for a given term with wildcards
	 * @return A Map containing the original query term as key and list of
	 * possible expansions as values if exist, null otherwise
	 */
	public Map<String, List<String>> getQueryTerms() {
		//TODO:IMPLEMENT THIS METHOD IFF WILDCARD BONUS ATTEMPTED
		return wcQueryTerms;
	}
	
	HashMap<String, List<String>> wcQueryTerms;
	public void processWCQueryTerms() {
		//TODO:IMPLEMENT THIS METHOD IFF WILDCARD BONUS ATTEMPTED
		//wcIndexes
		wcQueryTerms=new HashMap<String, List<String>>();
		List<String> wcTermList;
		HashMap<Integer,Double> postingList;
		ArrayList<Integer> postingArray;
		String queryString=q.toString();
		String[] wildCardList,wcInfo;
		String wildCardQuery,wildCardIndexType;
		queryString=queryString.replaceAll("\\{|\\}|\\<|\\>|\\[|\\]","");
		if(queryString.contains("*") || queryString.contains("?")){
			wildCardList=queryString.split("AND|OR");
			for(String wcTerm: wildCardList){
				wcTermList= new ArrayList<String>();
				wcInfo=wcTerm.split(":");
				wildCardIndexType=wcInfo[0].trim();
				wildCardQuery= wcInfo[1].trim();
				if(wildCardQuery.contains("*")){
					if(wildCardQuery.startsWith("*")){
						String searchText=wildCardQuery.substring(1);
						if(wildCardIndexType.equalsIgnoreCase("term")){
						List<TreeMap<String, HashMap<Integer, Double>>> IndexList= new LinkedList<TreeMap<String,HashMap<Integer,Double>>>();
						IndexList.add(IndexWriter.termIndexAC);IndexList.add(IndexWriter.termIndexDG);IndexList.add(IndexWriter.termIndexHK);
						IndexList.add(IndexWriter.termIndexLP);IndexList.add(IndexWriter.termIndexQS);IndexList.add(IndexWriter.termIndexTZ);
						IndexList.add(IndexWriter.termIndexMisc);
						for(TreeMap<String, HashMap<Integer, Double>> index: IndexList){
							if(index!=null)
							for(String term: index.keySet()){
								if(term.endsWith(searchText)){
									wcTermList.add(term);
								}
							}
						}
						}else if(wildCardIndexType.equalsIgnoreCase("author")){
							for(String term: IndexWriter.AuthorIndex.keySet()){
								if(term.endsWith(searchText)){
									wcTermList.add(term);
								}
							}
							
						}else if(wildCardIndexType.equalsIgnoreCase("category")){
							for(String term: IndexWriter.CatIndex.keySet()){
								if(term.endsWith(searchText)){
									wcTermList.add(term);
								}
							}
						}else if(wildCardIndexType.equalsIgnoreCase("place")){
							for(String term: IndexWriter.PlaceIndex.keySet()){
								if(term.endsWith(searchText)){
									wcTermList.add(term);
								}
							}
						}
						
					}else if(wildCardQuery.endsWith("*")){
						String searchText=wildCardQuery.substring(0,wildCardQuery.length()-1);
						if(wildCardIndexType.equalsIgnoreCase("term")){
							searchText=searchText.toLowerCase();
							TreeMap<String, HashMap<Integer, Double>> tempIndex;
							switch(searchText.charAt(0)){
								case 'a': case 'b': case 'c':
									tempIndex=IndexWriter.termIndexAC;
									break;
								case 'd': case 'e': case 'f': case 'g':
									tempIndex=IndexWriter.termIndexDG;
									break;
								case 'h': case 'i': case 'j': case 'k':
									tempIndex=IndexWriter.termIndexHK;
									break;
								case 'l': case 'm': case 'n': case 'o': case 'p':
									tempIndex=IndexWriter.termIndexLP;
									break;
								case 'q': case 'r': case 's':
									tempIndex=IndexWriter.termIndexQS;
									break;
								case 't': case 'u': case 'v': case 'w': case 'x': case 'y': case 'z':
									tempIndex=IndexWriter.termIndexTZ;
									break;
								default :
									tempIndex=IndexWriter.termIndexMisc;
								}
							if(tempIndex!=null){
								for(String term:tempIndex.keySet()){
									if(term.startsWith(searchText)){
										wcTermList.add(term);
									}	
								}
							}
						}else if(wildCardIndexType.equalsIgnoreCase("author")){
							for(String term: IndexWriter.AuthorIndex.keySet()){
								if(term.startsWith(searchText)){
									wcTermList.add(term);
								}
							}
							
						}else if(wildCardIndexType.equalsIgnoreCase("category")){
							for(String term: IndexWriter.CatIndex.keySet()){
								if(term.startsWith(searchText)){
									wcTermList.add(term);
								}
							}
						}else if(wildCardIndexType.equalsIgnoreCase("place")){
							for(String term: IndexWriter.PlaceIndex.keySet()){
								if(term.startsWith(searchText)){
									wcTermList.add(term);
								}
							}
						}
					}else{
						String[] searcTextStrings= wildCardQuery.split("\\*");
						String searchText1=searcTextStrings[0];
						String searchText2=searcTextStrings[1];
						if(wildCardIndexType.equalsIgnoreCase("term")){
							searchText1=searchText1.toLowerCase();
							searchText2=searchText2.toLowerCase();
							TreeMap<String, HashMap<Integer, Double>> tempIndex;
							switch(searchText1.charAt(0)){
								case 'a': case 'b': case 'c':
									tempIndex=IndexWriter.termIndexAC;
									break;
								case 'd': case 'e': case 'f': case 'g':
									tempIndex=IndexWriter.termIndexDG;
									break;
								case 'h': case 'i': case 'j': case 'k':
									tempIndex=IndexWriter.termIndexHK;
									break;
								case 'l': case 'm': case 'n': case 'o': case 'p':
									tempIndex=IndexWriter.termIndexLP;
									break;
								case 'q': case 'r': case 's':
									tempIndex=IndexWriter.termIndexQS;
									break;
								case 't': case 'u': case 'v': case 'w': case 'x': case 'y': case 'z':
									tempIndex=IndexWriter.termIndexTZ;
									break;
								default :
									tempIndex=IndexWriter.termIndexMisc;
								}
							if(tempIndex!=null){
								for(String term:tempIndex.keySet()){
									if(term.startsWith(searchText1) && term.endsWith(searchText2)){
										wcTermList.add(term);
									}	
								}
							}
						}else if(wildCardIndexType.equalsIgnoreCase("author")){
							for(String term: IndexWriter.AuthorIndex.keySet()){
								if(term.startsWith(searchText1) && term.endsWith(searchText2)){
									wcTermList.add(term);
								}
							}
							
						}else if(wildCardIndexType.equalsIgnoreCase("category")){
							for(String term: IndexWriter.CatIndex.keySet()){
								if(term.startsWith(searchText1) && term.endsWith(searchText2)){
									wcTermList.add(term);
								}
							}
						}else if(wildCardIndexType.equalsIgnoreCase("place")){
							for(String term: IndexWriter.PlaceIndex.keySet()){
								if(term.startsWith(searchText1) && term.endsWith(searchText2)){
									wcTermList.add(term);
								}
							}
						}
					}
					
				}else if(wildCardQuery.contains("?")){
					int len= wildCardQuery.length();
					if(wildCardQuery.startsWith("?")){
						String searchText=wildCardQuery.substring(1);
						if(wildCardIndexType.equalsIgnoreCase("term")){
						List<TreeMap<String, HashMap<Integer, Double>>> IndexList= new LinkedList<TreeMap<String,HashMap<Integer,Double>>>();
						IndexList.add(IndexWriter.termIndexAC);IndexList.add(IndexWriter.termIndexDG);IndexList.add(IndexWriter.termIndexHK);
						IndexList.add(IndexWriter.termIndexLP);IndexList.add(IndexWriter.termIndexQS);IndexList.add(IndexWriter.termIndexTZ);
						IndexList.add(IndexWriter.termIndexMisc);
						for(TreeMap<String, HashMap<Integer, Double>> index: IndexList){
							if(index!=null)
							for(String term: index.keySet()){
								if(term.endsWith(searchText) && term.length()==len){
									wcTermList.add(term);
								}
							}
						}
						}else if(wildCardIndexType.equalsIgnoreCase("author")){
							for(String term: IndexWriter.AuthorIndex.keySet()){
								if(term.endsWith(searchText) && term.length()==len){
									wcTermList.add(term);
								}
							}
							
						}else if(wildCardIndexType.equalsIgnoreCase("category")){
							for(String term: IndexWriter.CatIndex.keySet()){
								if(term.endsWith(searchText) && term.length()==len){
									wcTermList.add(term);
								}
							}
						}else if(wildCardIndexType.equalsIgnoreCase("place")){
							for(String term: IndexWriter.PlaceIndex.keySet()){
								if(term.endsWith(searchText) && term.length()==len){
									wcTermList.add(term);
								}
							}
						}
						
					}else if(wildCardQuery.endsWith("?")){
						String searchText=wildCardQuery.substring(0,wildCardQuery.length()-1);
						if(wildCardIndexType.equalsIgnoreCase("term")){
							searchText=searchText.toLowerCase();
							TreeMap<String, HashMap<Integer, Double>> tempIndex;
							switch(searchText.charAt(0)){
								case 'a': case 'b': case 'c':
									tempIndex=IndexWriter.termIndexAC;
									break;
								case 'd': case 'e': case 'f': case 'g':
									tempIndex=IndexWriter.termIndexDG;
									break;
								case 'h': case 'i': case 'j': case 'k':
									tempIndex=IndexWriter.termIndexHK;
									break;
								case 'l': case 'm': case 'n': case 'o': case 'p':
									tempIndex=IndexWriter.termIndexLP;
									break;
								case 'q': case 'r': case 's':
									tempIndex=IndexWriter.termIndexQS;
									break;
								case 't': case 'u': case 'v': case 'w': case 'x': case 'y': case 'z':
									tempIndex=IndexWriter.termIndexTZ;
									break;
								default :
									tempIndex=IndexWriter.termIndexMisc;
								}
							if(tempIndex!=null){
								for(String term:tempIndex.keySet()){
									if(term.startsWith(searchText) && term.length()==len){
										if(term.contains("wo")){
											System.out.println(term);
										}
										wcTermList.add(term);
									}	
								}
							}
						}else if(wildCardIndexType.equalsIgnoreCase("author")){
							for(String term: IndexWriter.AuthorIndex.keySet()){
								if(term.startsWith(searchText) && term.length()==len){
									wcTermList.add(term);
								}
							}
							
						}else if(wildCardIndexType.equalsIgnoreCase("category")){
							for(String term: IndexWriter.CatIndex.keySet()){
								if(term.startsWith(searchText) && term.length()==len){
									wcTermList.add(term);
								}
							}
						}else if(wildCardIndexType.equalsIgnoreCase("place")){
							for(String term: IndexWriter.PlaceIndex.keySet()){
								if(term.startsWith(searchText) && term.length()==len){
									wcTermList.add(term);
								}
							}
						}
					}else{
						String[] searcTextStrings= wildCardQuery.split("\\?");
						String searchText1=searcTextStrings[0];
						String searchText2=searcTextStrings[1];
						if(wildCardIndexType.equalsIgnoreCase("term")){
							searchText1=searchText1.toLowerCase();
							searchText2=searchText2.toLowerCase();
							TreeMap<String, HashMap<Integer, Double>> tempIndex;
							switch(searchText1.charAt(0)){
								case 'a': case 'b': case 'c':
									tempIndex=IndexWriter.termIndexAC;
									break;
								case 'd': case 'e': case 'f': case 'g':
									tempIndex=IndexWriter.termIndexDG;
									break;
								case 'h': case 'i': case 'j': case 'k':
									tempIndex=IndexWriter.termIndexHK;
									break;
								case 'l': case 'm': case 'n': case 'o': case 'p':
									tempIndex=IndexWriter.termIndexLP;
									break;
								case 'q': case 'r': case 's':
									tempIndex=IndexWriter.termIndexQS;
									break;
								case 't': case 'u': case 'v': case 'w': case 'x': case 'y': case 'z':
									tempIndex=IndexWriter.termIndexTZ;
									break;
								default :
									tempIndex=IndexWriter.termIndexMisc;
								}
							if(tempIndex!=null){
								for(String term:tempIndex.keySet()){
									if(term.startsWith(searchText1) && term.endsWith(searchText2) && term.length()==len){
										wcTermList.add(term);
									}	
								}
							}
						}else if(wildCardIndexType.equalsIgnoreCase("author")){
							for(String term: IndexWriter.AuthorIndex.keySet()){
								if(term.startsWith(searchText1) && term.endsWith(searchText2) && term.length()==len){
									wcTermList.add(term);
								}
							}
							
						}else if(wildCardIndexType.equalsIgnoreCase("category")){
							for(String term: IndexWriter.CatIndex.keySet()){
								if(term.startsWith(searchText1) && term.endsWith(searchText2) && term.length()==len){
									wcTermList.add(term);
								}
							}
						}else if(wildCardIndexType.equalsIgnoreCase("place")){
							for(String term: IndexWriter.PlaceIndex.keySet()){
								if(term.startsWith(searchText1) && term.endsWith(searchText2) && term.length()==len){
									wcTermList.add(term);
								}
							}
						}
					}
				}
				wcTerm=wcTerm.trim();
				if(!wcTermList.isEmpty()){
					wcQueryTerms.put(wcTerm, wcTermList);
				}else{
					wcQueryTerms.put(wcTerm, null);
				}
			}
		}
//		return wcQueryTerms;
	}

	
	/**
	 * Method to indicate if speel correct queries are supported
	 * @return true if supported, false otherwise
	 */
	public static boolean spellCorrectSupported() {
		//TODO: CHANGE THIS TO TRUE ONLY IF SPELLCHECK BONUS ATTEMPTED
		return false;
	}
	
	/**
	 * Method to get ordered "full query" substitutions for a given misspelt query
	 * @return : Ordered list of full corrections (null if none present) for the given query
	 */
	public List<String> getCorrections() {
		//TODO: IMPLEMENT THIS METHOD IFF SPELLCHECK EXECUTED
		return null;
	}
}
