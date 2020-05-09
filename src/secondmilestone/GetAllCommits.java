package secondmilestone;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.*;


import firstmilestone.RetrieveTicketsID;

public class GetAllCommits {
	
	private JSONArray resultJson = new JSONArray();
	private Date startDate = null;
	private Date endDate = null;
	
	public GetAllCommits(String startDate, String endDate) throws ParseException {
		this.startDate = new SimpleDateFormat("yyyy-MM-dd").parse(startDate);
		this.endDate = new SimpleDateFormat("yyyy-MM-dd").parse(endDate);
	}
	
	public void compareDate(String dateToCompare, JSONObject commit) throws ParseException {
		Date formattedDateToCompare = new SimpleDateFormat("yyyy-MM-dd").parse(dateToCompare);
		if (this.startDate.compareTo(formattedDateToCompare) <= 0 && this.endDate.compareTo(formattedDateToCompare) >= 0) {
			this.resultJson.put(commit);
		}
	}

	public JSONArray getExtendedCommits(String projName, String fileName) throws IOException, JSONException, ParseException {
		JSONArray result = new JSONArray();
        String token = new String(Files.readAllBytes(Paths.get(fileName)));
        JSONArray object = new JSONArray(token);
        VersionParser vp = new VersionParser();
        List<String> ticketsID = RetrieveTicketsID.getTicketID(projName);
		int i;
		int total = object.length();
		System.out.println(total);
        for (i = 0; i < total; i++) {
        	
        	JSONObject jsonCommit = GithubConnector.readJsonFromUrl(object.getJSONObject(i).getString("url"));
        	Date commitDate = new SimpleDateFormat("yyyy-MM-dd").parse(jsonCommit.getJSONObject("commit").getJSONObject("committer").getString("date"));
        	jsonCommit.put("Version", vp.getVersionName(commitDate, projName));
        	
        	String commitMessage = jsonCommit.getJSONObject("commit").get("message").toString(); 
            if(commitMessage.length() < projName.length()+ 7) {
   				 break;
   			}
   			  
		    String ticketMessage = "";
   			int[] resultArray = RetrieveTicketsID.findStartEnd(projName, commitMessage);
   			int start = resultArray[0];
   			int end = resultArray[1];
   			if (start<end) { 
  			  ticketMessage = commitMessage.substring(start,end); // prendo tutto finchè non trovo ] o :
            }    	
   		    if (commitMessage.contains(ticketMessage) && ticketsID.contains(ticketMessage)) {
         	  jsonCommit.put("FixCommit", ticketMessage);
         	}
         	else {
         	  jsonCommit.put("FixCommit", "");
         	}  
        	result.put(jsonCommit);
        }
        return result;
		
	}
	public  JSONArray getAllCommits (String projName , String organization) throws JSONException, IOException, ParseException {
		Integer i = 0;
		
		Integer page =1;
		Integer perPage = 100;
		Logger.getLogger(GetAllCommits.class.getName());
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
    	 
    	  String endDate = "2014-02-02";  
    	  String startDate = "2011-12-07";
    	  String projName = "BOOKKEEPER";
    	  String organization = "apache";
    	  FileWriter file = null;
    	  Logger logger = Logger.getLogger(GetAllCommits.class.getName());
    	  GetAllCommits getCommits = new GetAllCommits(startDate,endDate);
    	  File tmpDir = new File(projName +"_Commits_Sha.JSON");
    	  if ( !tmpDir.exists()) {
        	  JSONArray commitsJsonArray = getCommits.getAllCommits(projName, organization);
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
    	  JSONArray extendedCommitsJsonArray = getCommits.getExtendedCommits(projName, projName +"_Commits_Sha.JSON");
    	  file = new FileWriter(projName +"_Extended_Commits_Sha.JSON");
	      
	      try {
	    		  file.write(extendedCommitsJsonArray.toString());
	              file.flush();
	      }catch(Exception e){
	    	  logger.log(Level.INFO, "context", e);
	      }finally{
	    	     file.close();
	      }
    	  
       }
}



    
