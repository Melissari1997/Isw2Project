package firstMilestone;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TreeMap;
import java.net.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import com.opencsv.CSVWriter;

public class RetrieveTicketsID {


	private static TreeMap<String,Integer> mapDate = new TreeMap<String,Integer>();

    private static String readAll(Reader rd) throws IOException {
		      StringBuilder sb = new StringBuilder();
		      int cp;
		      while ((cp = rd.read()) != -1) {
		         sb.append((char) cp);
		      }
		      return sb.toString();
		   }

	   public static JSONArray readJsonArrayFromUrl(String url) throws IOException, JSONException {
	      InputStream is = new URL(url).openStream();
	      try {
	         BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
	         String jsonText = readAll(rd);
	         JSONArray json = new JSONArray(jsonText);
	         return json;
	       } finally {
	         is.close();
	       }
	   }

   public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
      InputStream is = new URL(url).openStream();
      try {
         BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
         String jsonText = readAll(rd);
         JSONObject json = new JSONObject(jsonText);
         return json;
       } finally {
         is.close();
       }
   }
   
   
   public static ArrayList<String> GetTicketID(String projName) throws IOException, JSONException{
	 
	   Integer j = 0, i = 0, total = 1;
	   ArrayList<String> ticketList =  new ArrayList<String>();
      //Get JSON API for closed bugs w/ AV in the project
      do {
         //Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
         j = i + 1000;
         String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
                + projName + "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"
                + "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,resolutiondate,versions,created&startAt="
                + i.toString() + "&maxResults=" + j.toString();
         JSONObject json = readJsonFromUrl(url);
         JSONArray issues = json.getJSONArray("issues");
         total = json.getInt("total");
         for (; i < total && i < j; i++) {
            //Iterate through each bug
            String key = issues.getJSONObject(i%1000).get("key").toString();
            ticketList.add(key);
         }  
      } while (i < total);
      return ticketList;
   }
   

  	   public static void main(String[] args) throws Exception {
		   
	   String projName ="Mahout";
	   String organization = "apache";
	   Integer i = 0;
	   Integer page = 1;
	   Integer per_page = 100;
	   int threshold = 7;   
	   CSVWriter csvWriter = new CSVWriter(new FileWriter(projName + "BugChart.csv"),';',
               CSVWriter.NO_QUOTE_CHARACTER,
               CSVWriter.DEFAULT_ESCAPE_CHARACTER,
               CSVWriter.DEFAULT_LINE_END);

	   ArrayList<String> ticketsID = GetTicketID(projName);

      do {

    	  System.out.println("Page: " + page.toString());
    	  String res = readJsonArrayFromUrl("https://api.github.com/repos/" +organization + "/"+ projName +"/commits?page="+ page.toString()+"&per_page=" + per_page.toString()).toString();
    	
    	  JSONArray jsonArray = new JSONArray(res);
   	      int total = jsonArray.length();

   	      if (total == 0 ) {
   	    	  break;
   	      }
   	      
   	      
    	  for (i = 0;  i<total; i++) {
    		  JSONObject key = jsonArray.getJSONObject(i%1000);
			  String commitMessage = key.getJSONObject("commit").get("message").toString();
			  if(commitMessage.length() < projName.length()+ threshold) {
				 break;
			  }
			  String ticketMessage = null;
			  int start= commitMessage.substring(0, projName.length()+threshold).toLowerCase().indexOf((projName + "-").toLowerCase());
			  int end = -1;
			  String date = key.getJSONObject("commit").getJSONObject("committer").getString("date");
              if (commitMessage.toLowerCase().contains("[" + projName.toLowerCase()+ "-")) {
        		  end = commitMessage.indexOf("]"); // trova l'occorrenza del carattere ], ovvero della fine della dichiarazione del ticket
              }
              else {
            	  if(commitMessage.toLowerCase().contains(projName.toLowerCase()+ "-") && commitMessage.contains(":")) {
            		  end = commitMessage.indexOf(":");
            	  }
              }
              if (start != -1 && end != -1 && start<end) {
            	 
    			  ticketMessage = commitMessage.substring(start,end); // prendo tutto finchè non trovo ] o :
    			    System.out.println(ticketMessage);
					System.out.println(date);
					System.out.println("----------------");
    			  if(ticketsID.contains(ticketMessage)) {
    				  	Date data = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(date); 
			            Calendar cal = Calendar.getInstance();
			            cal.setTime(data);
			            Integer month= cal.get(Calendar.MONTH) +1;
			            Integer year = cal.get(Calendar.YEAR);
						date = String.valueOf(year) + "/" + String.format("%02d",month);
			            System.out.println(ticketMessage);
			            System.out.println(date);
						System.out.println("----------------");
						if(mapDate.containsKey(date)) {		            	
			            	mapDate.put(date, mapDate.get(date)+1);
			            }
			            else {
			            	mapDate.put(date, 1);
			            }	  
    			  }
    		  }  
           } 
    	  page++;
      } while (true);
      mapDate.entrySet().forEach(entry->{
    	    System.out.println("Data: " + entry.getKey() + " ---> Numero: " + entry.getValue());  
    	    csvWriter.writeNext(new String[]{entry.getKey() , String.valueOf(entry.getValue())});
      });
      csvWriter.close();
      //TODO aggiungere commenti 
   }
}