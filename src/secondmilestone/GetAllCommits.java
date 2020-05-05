package secondmilestone;

import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.*;

import firstmilestone.RetrieveTicketsID;

public class GetAllCommits {
	
	private JSONArray resultJson = new JSONArray();
	private Date date = null;
	
	public GetAllCommits(String date) throws ParseException {
		this.date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").parse(date);
	}
	
	public void compareDate(String dateToCompare, JSONObject commit) throws ParseException {
		Date formattedDateToCompare = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").parse(dateToCompare);
		if (this.date.compareTo(formattedDateToCompare) >= 0) {
			this.resultJson.put(commit);
		}
		

	}
	public  JSONArray getAllCommits (String projName , String organization) throws JSONException, IOException, ParseException {
		Integer i = 0;
		
		Integer page =1;
		Integer perPage = 100;
		Logger.getLogger(GetAllCommits.class.getName());
        // prendo tutti i commits
        int total = 0;
        do {
        	
        	String res = RetrieveTicketsID.readJsonArrayFromUrl("https://api.github.com/repos/" + organization + "/"+ projName +"/commits?page="+ page.toString()+"&per_page=" + perPage.toString()).toString();
        	JSONArray jsonArray = new JSONArray(res);
        	
        	
        	total = jsonArray .length();
        
        	for (i = 0; i< total; i++) {
        		JSONObject commit = jsonArray.getJSONObject(i%1000);
        		this.compareDate(commit.getJSONObject("commit").getJSONObject("committer").getString("date"), commit);
        	}
            page++;
            
       
        }while(total>0);
        return resultJson;
	}
             
      public static void main(String[] args) throws JSONException, IOException, ParseException {
    	 
    	  String strDate = "2014-02-02T00:00";  
    	  String projName = "BOOKKEEPER";
    	  String organization = "apache";
    	  Logger logger = Logger.getLogger(GetAllCommits.class.getName());
    	  
    	  GetAllCommits getCommits = new GetAllCommits(strDate);
    	  JSONArray commitsJsonArray = getCommits.getAllCommits(projName, organization);
    	  FileWriter file = null;
          
	      file = new FileWriter(projName +"_Commits_Sha.JSON");
	      
	      try {
	    		  file.write(commitsJsonArray.toString());
	              file.flush();
	      }catch(Exception e){
	    	  logger.log(Level.INFO, "context", e);
	      }finally{
	    	     file.close();
	      }
	    		  
      	
          }
      }



    
