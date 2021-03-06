package edu.buffalo.cse.irf14.analysis;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateFilter  extends TokenFilter {
	// Update on 17th Sep by anand
	
	final Pattern p = Pattern.compile("(\\d{4})(-)(\\d{2}|\\d{4})(.|,|!|\\?|:|;)*");
	Matcher m1=null;
	Matcher int_matcher = null; 
	
	public DateFilter(TokenStream stream) {
		super(stream);
		// TODO Auto-generated constructor stub
		
		monthList.put("january",1);
		monthList.put( "february",2 );
		monthList.put("march", 3);
		monthList.put("april",4);
		monthList.put( "may", 5);
		monthList.put("june",6);
		monthList.put( "july",7);
		monthList.put( "august",8 );
		monthList.put("september",9 );
		monthList.put("october", 10);
		monthList.put("november", 11);
		monthList.put("december",12);
		monthList.put("jan",1);
		monthList.put("feb",2);
		monthList.put("mar",3);
		monthList.put("apr",4);
		monthList.put("jun",6);
		monthList.put("jul",7);
		monthList.put("aug",8);
		monthList.put("sep",9);
		monthList.put("oct",10);
		monthList.put("nov",11);
		monthList.put("dec",12);
		
		t_stream=stream;
		f_type=TokenFilterType.DATE;
	}	
	final Pattern intpattern = Pattern.compile("[012]?[0-9]:[0-5][0-9](:[0-5][0-9])?([a-zA-z.]{3})?");
    
	public static String Number(String string,boolean suffix){
		char ch=' ';
		StringBuilder sb=new StringBuilder();
		int i;
	    try {
	    	if(!Character.isDigit(string.charAt(string.length()-1)))
	    	{
	    		ch=string.charAt(string.length()-1);
	    		string=string.substring(0,string.length()-1);
	    	}
	    	i=Integer.parseInt(string);
	    	
	    	if((suffix) || i>1800 && i<2100)
	    	{		sb.append('~');
	    	sb.append(String.format("%04d", i));

	    	if(ch!=' '){
	    		sb.append('^');
	    		sb.append(ch);
	    	}
	    }
	    		
	    	else if(i<32)
	    	{
	    		sb.append(String.format("%02d", i));	
	    	}
	    		//return String.format("%02d", i);
	    	
	    	
	    	 return sb.toString();
	    } catch (Exception e) {
	       return "";
	        
	    }
	}
	
	public String month(String str)
	{
		char ch=' ';
		if(str!=null && !str.equals(""))	{
			if(!Character.isLetter(str.charAt(str.length()-1)))
	    	{
	    		ch=str.charAt(str.length()-1);
	    		str=str.substring(0,str.length()-1);
	    	}
			Integer i=monthList.get(str.toLowerCase());
			if(i==null)
				return "";
			else
			{
				if(ch==' ')
					return String.format("%02d", i);
				else
					return String.format("%02d", i)+ch;
			}
			
		}
		else
			return "";
	}
	
	public String year_or_date(String str)
	{
		if(str.contains("BC"))
			return "-"+Number(str.replaceAll("BC", ""),true);
		else if(str.contains("AD"))
			return Number(str.replaceAll("AD", ""),true);
		else
			return Number(str,false);
	}
	
	final static HashMap<String,Integer> monthList = new HashMap<String,Integer>();
	
	@Override
	public boolean increment() throws TokenizerException {
		// TODO Auto-generated method stub
		 Token current_token,next;
		 ArrayList<String> time = null;
		 String ch_time = "";
		 int cnt=0;
		 String t1="",t2 = "",t3="";
		  StringBuilder date_builder = new StringBuilder();
			current_token=t_stream.next();
			if(current_token==null)
				return false;
			String str=current_token.getTermText();
			if(int_matcher!=null)
				int_matcher.reset(str);
			else
				int_matcher=intpattern.matcher(str);
		
	        if(t_stream.hasNext())
			{
				String y=t_stream.next().getTermText();
				if(y.contains("BC")){
					str=str+"BC";
					cnt++;
				}
				else if(str.contains("AD")){
					str=str+"BC";
					cnt++;
				}
				t_stream.previous();
			}
			t1=year_or_date(str);
			
			if(!t1.equals("")) // DD MM YY
			{
				if(t_stream.hasNext())
				{
					next=t_stream.next();
					t2=month(next.toString());
					
					if(!t2.equals("") && t_stream.hasNext())
					{
						next=t_stream.next();
						t_stream.previous();
						cnt++;
						t3=year_or_date(next.toString());
					}
					t_stream.previous();
				}
				if(t2.equals("") && t3.equals("") && !t1.contains("~"))
					date_builder.setLength(0);
				else
				{
				if(t2.equals(""))
					t2="01";				
				if(t3.contains("~")) //t3=year
				{
					t1=(t1.equals(""))?"01":t1;
					date_builder.append(t3.replaceAll("~", ""));
					date_builder.append(t2);
					date_builder.append(t1);
					cnt++;
				}
				else if(t1.contains("~")) //t1=year
				{
					t3=(t3.equals(""))?"01":t3;
					if(t1.startsWith("~00"))
						date_builder.setLength(0);
					else{
					
						date_builder.append(t1.replaceAll("~", ""));
						date_builder.append(t2);
						date_builder.append(t3);
					}
				}
				else
				{
					t3=(t3.equals(""))?"1900":t3;	
					date_builder.append(t3);
					date_builder.append(t2);
					date_builder.append(t1);
				}
				}
			}
				
			else if(int_matcher.matches() && str.contains(":") && str.length()>1)
			{
				int ap=-1;
				String l=str.toLowerCase();
				if(l.contains("pm")){
					ap=2;
					str=l.replaceAll("pm", "");
				}
				else if(l.contains("am")){
					ap=1;
					str=l.replaceAll("am", "");
				}
				else
				{
					if(t_stream.hasNext())
					{
						String y=t_stream.next().getTermText().toLowerCase();
						if(y.contains("pm")){
							ap=2;
							cnt++;
						}
						else if(y.contains("am"))
						{
							ap=1;
							cnt++;
						}

						if(y.length()>1 && !Character.isDigit(y.charAt(y.length()-1)))
							ch_time=y.charAt(y.length()-1)+"";
				    	
						t_stream.previous();
					}
				}
				if(!Character.isDigit(str.charAt(str.length()-1)))
				{
					ch_time=str.charAt(str.length()-1)+"";
					str=str.substring(0,str.length()-1);
				}
		    	
				String [] tt=str.split(":");
				time=new ArrayList<String>();
				int i=0;
				for(String s:tt){
					if(i==0 && ap==2)
					{
						ap=Integer.parseInt(s)+12;
						time.add(ap+"");
					}
					else
						time.add(s);
				}
				
			}
			
			else // MM DD YY
			{
				t2=month(str);
				if(!t2.equals(""))
				{
					if(t_stream.hasNext()){
					next=t_stream.next();
					t1=year_or_date(next.toString());
					
					if(!t1.equals("") && t_stream.hasNext())
					{
						next=t_stream.next();
						t3=year_or_date(next.toString());
						t_stream.previous();
						cnt++;
					}
					t_stream.previous();
					
					}					
					if(t3.contains("~"))
					{
						t1=(t1.equals(""))?"01":t1;
						date_builder.append(t3.replaceAll("~", ""));
						date_builder.append(t2);
						date_builder.append(t1);
						cnt++;
					}
					else if(t1.contains("~"))
					{
						if(t3.equals(""))
							t3="01";
						else
							cnt++;
						date_builder.append(t1.replaceAll("~", ""));
						date_builder.append(t2);
						date_builder.append(t3);
					}
					else
					{
						t3=(t3.equals(""))?"1900":t3;	
						t1=(t1.equals(""))?"01":t1;
						date_builder.append(t3);
						date_builder.append(t2);
						date_builder.append(t1);
					}
					
				}
				else
				{
					if(m1==null)
						m1=p.matcher(str);
					else
						m1.reset(str);
					if(m1.matches()) // for 2011-12.
					{
						String[] yr=str.split("-");
						int i=0;
						for(String s:yr)
						{
							try{
								if(i>0 && s.length()<4){
									s=yr[i-1].substring(0,2)+s;
								}
							}catch(Exception e){
//								System.out.println("Datefilter error"+yr[i-1]);
							}
									
								t1=year_or_date(s);
								if(i!=0 && t1!=""){
									date_builder.append("-");
									date_builder.append(t1.replaceAll("~", ""));
									date_builder.append("0101");
									cnt++;
								}
								else if(t1!=""){
									date_builder.append(t1.replaceAll("~", ""));
									date_builder.append("0101");
								}
							i++;
						}
					}
				}
				
			}
			String delim="^";
			int di=date_builder.indexOf(delim);
			String ch="";
			while(di>0)
			{
				ch=date_builder.charAt(di+1)+"";
				date_builder.deleteCharAt(di);
				date_builder.deleteCharAt(di);
				di=date_builder.indexOf(delim);
			}
			date_builder.append(ch);
			int i=0;
			if(time!=null)
			{
				for(String s:time)
				{
					date_builder.append(s);
					date_builder.append(":");
					i++;
				}
				while(i>0 && i<3)
				{
					date_builder.append("00");
				
					if(++i<3)
						date_builder.append(":");
				}
				date_builder.append(ch_time);
			}
			
			if(!date_builder.toString().equals(""))
			{
			current_token.setTermText(date_builder.toString());
			t_stream.replace(current_token);
			}
			while(cnt>0)
			{
				t_stream.next();
				t_stream.remove();
				cnt--;
			}
			if(t_stream.hasNext())
				return true;
			else
				return false;
			
	}

	@Override
	public TokenStream getStream() {
		// TODO Auto-generated method stub
		return t_stream;
	}

}
