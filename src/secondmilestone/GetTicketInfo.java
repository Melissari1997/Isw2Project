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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
  
public class GetTicketInfo {
	   private static double p= 0;
	   private static double totalP =0;
	   private static int count = 1;
	   private static String formatDate = "yyyy-MM-dd";
	   
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
      public static String proportion(String fixedVersion, String openingVersion) throws ParseException, IOException {
    	  /*
    	  Date fixedDate = new SimpleDateFormat(formatDate).parse(resolutionDate);
    	  Date openingDate = new SimpleDateFormat(formatDate).parse(createdDate);
    	  String fixedVersion = vp.getVersionName(fixedDate, projName);
    	  String openingVersion = vp.getVersionName(openingDate, projName);
    	  */
  	      int injectedVersion =  (int) (Double.valueOf(fixedVersion) -((Double.valueOf(fixedVersion)-Double.valueOf(openingVersion))*p));
  	      
  	      if (injectedVersion <= 0)
  	    	  injectedVersion = 1;
  	      return String.valueOf(injectedVersion);
      }
      public static void computeP(int fixedVersion, int openingVersion, int injectedVersion) {
    	  Integer numerator = Integer.valueOf(fixedVersion)-Integer.valueOf(injectedVersion);
    	  Integer denominator = Integer.valueOf(fixedVersion)-Integer.valueOf(openingVersion);
    	  double newP = 0.0;
    	  if(denominator != 0) {
    		  newP = numerator/denominator;
    	  }
       	  totalP += newP;
       	  count++;
       	  p = totalP/count;

      }
      
      public static void addAffectedVersions(String injectedVersion, String fixedVersion, JSONArray affectedVersion) throws JSONException {
    	  for(int i = Integer.valueOf(injectedVersion); i < Integer.valueOf(fixedVersion); i++) {
    		  JSONObject affectedV = new JSONObject();
    		  affectedV.put("name", String.valueOf(i));
    		  affectedVersion.put(affectedV);
    	  }
      }
      
      public static boolean checkIv(String fixedVersion, String openingVersion, String injectedVersion) throws ParseException, IOException {
    	  //System.out.println("FixVersion: "+ fixedVersion + ";OpeningVersion: "+ openingVersion+"; Injected: " + injectedVersion);
    	  if(fixedVersion != null && openingVersion!= null && injectedVersion != null) {
    		 if(Integer.valueOf(fixedVersion)-Integer.valueOf(openingVersion) >= 1 && Integer.valueOf(openingVersion)-Integer.valueOf(injectedVersion) >= 0){	 
	           	  return true;
    		 }
    		  
    	  }
    	  return false;
      }
      public static boolean computeInjectedVersion(JSONObject key, String projName) throws ParseException, JSONException, IOException {
    	   JSONArray affectedVersion = key.getJSONObject("fields").getJSONArray("versions");
    	   Date iv = null;
		   List<Date> affectedVersionList =  new ArrayList<>(); //lista delle AV trovate quando ho risolto un bug
		   String injectedVersion = null;
		   VersionParser vp = new VersionParser();
	  	   vp.setProgress(1.0);
	  	   Date fixedDate = null;
	       String fixedVersion = null;
	       
	       if(key.getJSONObject("fields").getJSONArray("fixVersions").getJSONObject(0).has("releaseDate")) {
	  	     fixedDate = new SimpleDateFormat(formatDate).parse(key.getJSONObject("fields").getJSONArray("fixVersions").getJSONObject(0).getString("releaseDate"));
	  	     fixedVersion = vp.getVersionName(fixedDate, projName);
	       }else {
	      		fixedVersion = (key.getJSONObject("fields").getJSONArray("fixVersions").getJSONObject(0).getString("name"));
	      	}
	        
	  	   Date openingDate = new SimpleDateFormat(formatDate).parse(key.getJSONObject("fields").getString("created"));
	  	   String openingVersion = vp.getVersionName(openingDate, projName);
		   
      	   for(int index = 0; index< affectedVersion.length();index++) {
      		if(affectedVersion.getJSONObject(index).has("releaseDate"))
      			affectedVersionList.add(new SimpleDateFormat("yyyy-MM-dd").parse(affectedVersion.getJSONObject(index).getString("releaseDate")));  
      	   }     	
      	   if(affectedVersionList.size() != 0) {
      		iv = Collections.min(affectedVersionList, (o1,o2) ->  o1.compareTo(o2));	
      		injectedVersion = vp.getVersionName(iv, projName);
      		if(iv.compareTo(new SimpleDateFormat(formatDate).parse(key.getJSONObject("fields").getString("created"))) > 0 || openingVersion == null){
      			return false;
      	    }
      		computeP(Integer.valueOf(fixedVersion), Integer.valueOf(openingVersion), Integer.valueOf(injectedVersion));
      		key.getJSONObject("fields").put("injectedversion", injectedVersion);
      		return true;
      	   }
  	   // injectedVersion = proportion(key.getJSONObject("fields").getString("resolutiondate"),key.getJSONObject("fields").getString("created"));
	  	    injectedVersion = proportion(fixedVersion,openingVersion);
	  	    boolean checkIv = checkIv(fixedVersion, openingVersion,injectedVersion);
	  	    if(checkIv == false) {
	  	    	return false;
	  	    }
	      	if(affectedVersionList.size() != 0) {
	      		computeP(Integer.valueOf(fixedVersion), Integer.valueOf(openingVersion), Integer.valueOf(injectedVersion));
	      	}
	      	key.getJSONObject("fields").put("injectedversion", injectedVersion); 
	      	
	      	addAffectedVersions(injectedVersion,fixedVersion, affectedVersion);
	        return true;
      }
      
      public static void takeOnlyLastFix(JSONArray fixVersions) throws ParseException, JSONException {
    	  List<Date> fixVersionsList = new ArrayList<>();
    	  for(int i = 0; i< fixVersions.length(); i++) {
    		  fixVersionsList.add(new SimpleDateFormat(formatDate).parse(fixVersions.getJSONObject(i).getString("releaseDate"))); 
    	  }
    	  Date fixVersion = Collections.max(fixVersionsList, (o1,o2) ->  o1.compareTo(o2));
    	  for(int i = 0; i< fixVersions.length(); i++) {
    		  if(fixVersion.compareTo(new SimpleDateFormat(formatDate).parse(fixVersions.getJSONObject(i).getString("releaseDate"))) != 0){
    			  fixVersions.remove(i);
    			  i--;
    		  }
    	  }
      }
      
     public static void deleteNonReleasedVersions(JSONArray versions) throws JSONException, ParseException, IOException {
    	 if(versions.length() >0) {
    		 for(int i = 0; i< versions.length();i++) {
    			 if(versions.getJSONObject(i).has("released") && versions.getJSONObject(i).getBoolean("released") == false) {
        			 //System.out.println("Removed"+ versions.getJSONObject(i));
        			 versions.remove(i);
        			 i--;
    			 }
        	 }
    	 }
     }
      
	public static JSONArray getTicketID(String projName) throws IOException, JSONException, ParseException{
		 
		  Integer j = 0;
		  Integer i = 0; 
		  Integer total = 1;
		  VersionParser vp = new VersionParser();
		  //vp.setProgress(1.0);
		  JSONArray ticketList = new JSONArray();
	     do {
	         j = i + 1000;
	         String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project%20%3D%20"+projName+"%20AND%20issuetype%20%3D%20Bug%20AND%20status%20in%20(Resolved,%20Closed)%20AND%20resolution%20=%20Fixed%20&&fields=resolutiondate,fixVersions,versions,created&startAt="+ 
		         		i.toString() + "&maxResults=" + j.toString();
	         JSONObject json = readJsonFromUrl(url);
	         JSONArray issues = json.getJSONArray("issues");
	         total = json.getInt("total");
	         
	         for (; i < total && i < j; i++) {
	            //Iterate through each bug
	        	 
	            JSONObject key = issues.getJSONObject(i%1000);
	           // System.out.println(key);
	            deleteNonReleasedVersions(key.getJSONObject("fields").getJSONArray("fixVersions"));
	            deleteNonReleasedVersions(key.getJSONObject("fields").getJSONArray("versions"));
	            
            	
	            if(key.getJSONObject("fields").getJSONArray("fixVersions").length() == 0) {
	            	JSONObject jsonresolutionDate = new JSONObject();
	            	Date resolutionDate = new SimpleDateFormat("yyyy-MM-dd").parse(key.getJSONObject("fields").getString("resolutiondate")); 
	            	String versionName = vp.getVersionName(resolutionDate, projName);
	            	
	            	if(versionName == null) {
	            		continue; // non considero date di ticket creati nel periodo di una versione recente non ancora rilasciata
	            	}
	            	jsonresolutionDate.put("name", versionName);
	            		            	//System.out.println(jsonresolutionDate);
	            	key.getJSONObject("fields").getJSONArray("fixVersions").put(jsonresolutionDate);
	            } 
	            
	            if(key.getJSONObject("fields").getJSONArray("fixVersions").length()>1) {
	            	takeOnlyLastFix(key.getJSONObject("fields").getJSONArray("fixVersions"));
	            }
	            
	            
            	if(computeInjectedVersion(key, projName) == false) {
            		continue;
            	}
            	//JSONArray affectedVersion = key.getJSONObject("fields").getJSONArray("versions");
            	
            	
            	//key.getJSONObject("fields").put("injectedversion", IV);  
	            ticketList.put(key);
	         } 
	         }while(i<total);
	     
	      return ticketList;
	   }
	public static void main(String[] args) throws IOException, JSONException, ParseException {
		String projName = "OPENJPA";
		FileWriter file = new FileWriter(projName +"_TicketInfo.JSON");
	    JSONArray resultJSONArray = getTicketID(projName);
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
