package secondmilestone;

import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;
import org.json.*;

import firstmilestone.RetrieveTicketsID;

public class GetAllCommits {
	
	private JSONArray resultJson = new JSONArray();
	private Date date = null;
	
	public GetAllCommits(String date) throws ParseException {
		this.date = new SimpleDateFormat("yyyy-MM-dd").parse(date);
	}
	
	public void compareDate(String dateToCompare, JSONObject commit) throws ParseException {
		Date formattedDateToCompare = new SimpleDateFormat("yyyy-MM-dd").parse(dateToCompare);
		if (this.date.compareTo(formattedDateToCompare) >= 0) {
			this.resultJson.put(commit);
			//System.out.println("Added commit " + commit.toString());
    		//System.out.println("Actual JSON: " + resultJson.toString());
		}
		
		return;
	}
	public  JSONArray getAllCommits (String projName , String organization) throws JSONException, IOException, ParseException {
		Integer i = 0;
		
		Integer page =50;
		Logger.getLogger(GetAllCommits.class.getName());
        // prendo tutti i commits
        int total = 0;
        do {
        	JSONArray jsonArray = RetrieveTicketsID.readJsonArrayFromUrl("https://api.github.com/repos/" +organization + "/"+ projName +"/commits?page="+ page.toString());	       	
        	
        	
        	total = jsonArray .length();
        	//System.out.println(jsonArray);
        	for (i = 0; i< total; i++) {
        		JSONObject commit = jsonArray.getJSONObject(i%1000);
        		
        		this.compareDate(commit.getJSONObject("commit").getJSONObject("committer").getString("date"), commit);
        		
        		//JSONObject res = RetrieveTicketsID.readJsonFromUrl("https://api.github.com/repos/" + organization + "/" + projName + "/commits/" + commitSha);
        	}
        	System.out.println("Page:" + page.toString());
            page++;
       
        }while(total>0);
        return resultJson;
	}
             
      public static void main(String[] args) throws JSONException, IOException, ParseException {
    	 
    	  String strDate = "2014-02-02T00:00";  
    	  String projName = "BOOKKEEPER";
    	  String organization = "apache";
    	  
    	  GetAllCommits getCommits = new GetAllCommits(strDate);
    	  JSONArray commitsJsonArray = getCommits.getAllCommits(projName, organization);
    	  FileWriter file = null;
          try {
          	file = new FileWriter("Commits Sha");
          	file.write(commitsJsonArray.toString());
          }catch(Exception e) {
          	e.printStackTrace();
          }finally {
          	try {
          		file.flush();
          		file.close();
          	}catch(Exception e) {
          		e.printStackTrace();
          	}
          }
      }
}


    
