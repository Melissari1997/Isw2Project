package secondmilestone;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GetTicketInfo {
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
		      try 
		         (BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
		         ){
		         return new JSONArray(readAll(rd));
		       } finally {
		         is.close();
		       }
		   }

      public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
		   InputStream is = new URL(url).openStream();
		   try 
		      (BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
		      ){
		      return new JSONObject(readAll(rd));
		    } finally {
		      is.close();
		    }
		}
      
      
      public static String getInjectedVersion(JSONObject key) throws ParseException, JSONException {
    	  JSONArray affectedVersion = key.getJSONObject("fields").getJSONArray("versions");
    	   Date IV = null;
		   List<Date> affectedVersionList =  new ArrayList<>(); //lista delle AV trovate quando ho risolto un bug
		   
      	for(int index = 0; index< affectedVersion.length();index++) {
      	    affectedVersionList.add(new SimpleDateFormat("yyyy-MM-dd").parse(affectedVersion.getJSONObject(index).getString("releaseDate")));  
      	}     	
      	if(affectedVersionList.size() != 0) {
      		IV = Collections.min(affectedVersionList, (o1,o2) ->  o1.compareTo(o2));
      		
      	}
      	else {
      		// per ora, invece di proportion, metto IV = OV
      		IV = new SimpleDateFormat("yyyy-MM-dd").parse(key.getJSONObject("fields").getString("created"));
      	}
      	Calendar cal = Calendar.getInstance();
        cal.setTime(IV);
        Integer month= cal.get(Calendar.MONTH) +1;
        Integer year = cal.get(Calendar.YEAR);
        Integer day = cal.get(Calendar.DAY_OF_MONTH);
      	return String.valueOf(year) + "-" + String.format("%02d",month)+ "-" + String.valueOf(day);
      }
      
     public static void deleteFutureVersions(JSONArray fixVersions, String projName) throws JSONException, ParseException, IOException {
    	 VersionParser vp = new VersionParser();
    	 System.out.println(fixVersions.length());
    	 for(int i = 0; i< fixVersions.length();i++) {
    		 Date releaseDate = new SimpleDateFormat("yyyy-MM-dd").parse(fixVersions.getJSONObject(i).getString("releaseDate"));
    		 
    		 System.out.println(fixVersions.getJSONObject(i));
    		 if(vp.getVersionName(releaseDate, projName) == null) {
    			 System.out.println("Removed"+ fixVersions.getJSONObject(i));
    			 fixVersions.remove(i);
    			 i--;
    		 }
    	 }
    	 System.out.println("----------------");
     }
      
	public static JSONArray getTicketID(String projName,String start, String end) throws IOException, JSONException, ParseException{
		 
		   Integer j = 0;
		   Integer i = 0; 
		   Integer total = 1;
		   VersionParser vp = new VersionParser();
		   /*
		   List<String> versionList = vp.getVersionList(projName);
		   
		   String temp =(String) versionList.stream()
		   .map(String::toString) // maps String to a value returned by toString method
		   .collect(Collectors.joining(","));
		   */
		   JSONArray ticketList = new JSONArray();
	      //Get JSON API for closed bugs w/ AV in the project
	      do {
	         //Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
	         j = i + 1000;
	         /*
	         String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project%20%3D%20"+projName+"%20AND%20issuetype%20%3D%20Bug%20AND%20status%20in%20(Resolved,%20Closed)%20AND%20resolution%20=%20Fixed%20and%20(resolutiondate%20<=%20\""+end+"\"%20or%20fixVersion%20in%20("+temp+"))%20and%20"
	         		+ "createdDate%20%20>=%22"+start+"%22%20"
	         		+ "%20and%20createdDate%20%20<=%22"+end+"%22%20"
	         		+ "ORDER%20BY%20resolutiondate%20DESC"
	         		+ "&&fields=resolutiondate,fixVersions,versions,created&startAt="+ 
	         		i.toString() + "&maxResults=" + j.toString();
	         		*/
	         String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project%20%3D%20"+projName+"%20AND%20issuetype%20%3D%20Bug%20AND%20status%20in%20(Resolved,%20Closed)%20AND%20resolution%20=%20Fixed%20&&fields=resolutiondate,fixVersions,versions,created&startAt="+ 
		         		i.toString() + "&maxResults=" + j.toString();
	         JSONObject json = readJsonFromUrl(url);
	         JSONArray issues = json.getJSONArray("issues");
	         total = json.getInt("total");
	         
	         for (; i < total && i < j; i++) {
	            //Iterate through each bug
	            JSONObject key = issues.getJSONObject(i%1000);
	            
	            if(key.getJSONObject("fields").getJSONArray("fixVersions").length() == 0) {
	            	JSONObject jsonresolutionDate = new JSONObject();
	            	Date resolutionDate = new SimpleDateFormat("yyyy-MM-dd").parse(key.getJSONObject("fields").getString("resolutiondate")); 
	            	jsonresolutionDate.put("name", vp.getVersionName(resolutionDate, projName));
	            	key.getJSONObject("fields").getJSONArray("fixVersions").put(jsonresolutionDate);
	            } /*else {
	            	deleteFutureVersions(key.getJSONObject("fields").getJSONArray("fixVersions"), projName);
	            }*/
	            
	            
	            //JSONArray affectedVersion = key.getJSONObject("fields").getJSONArray("versions");
            	String IV = getInjectedVersion(key);
            	key.getJSONObject("fields").put("injectedversion", IV);  
	            ticketList.put(key);
	         }  
	      } while (i < total);
	      return ticketList;
	   }
	public static void main(String[] args) throws IOException, JSONException, ParseException {
		String projName = "BOOKKEEPER";
		String end = "2014-02-02";  
  	    String start = "2011-12-07";
		FileWriter file = new FileWriter(projName +"_TicketInfo.JSON");
	    JSONArray resultJSONArray = getTicketID(projName, start,end);
	      try {
	    		  file.write(resultJSONArray.toString());
	              file.flush();
	      }catch(Exception e){
	    	 e.printStackTrace();
	      }finally{
	    	     file.close();
	      }

	}

}
