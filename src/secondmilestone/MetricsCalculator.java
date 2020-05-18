package secondmilestone;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MetricsCalculator {
	//private static List<String[]> fileRecords;
	public static void updateChurn(JSONObject commit, List<String[]> fileRecords) throws JSONException {
		HashMap<String,Integer> hashChurn = new HashMap<>(); 	
    	JSONArray fileListJson = commit.getJSONArray("files");
		for(int j = 0; j<fileListJson.length(); j ++) {
			int additions = fileListJson.getJSONObject(j).getInt("additions");
			int deletions = fileListJson.getJSONObject(j).getInt("deletions");
			int churn = additions-deletions;
			hashChurn.put(fileListJson.getJSONObject(j).getString("filename"), churn);
		}		
        for (int i = 0; i < fileRecords.size(); i++) {
	    	if(fileRecords.get(i).length >2) {
		    	if (hashChurn.containsKey(fileRecords.get(i)[0]) && commit.getString("Version").equals(fileRecords.get(i)[1])) {
		    		fileRecords.get(i)[5]=  String.valueOf((Integer.valueOf(fileRecords.get(i)[5])+hashChurn.get(fileRecords.get(i)[0])));
		    		if(Integer.valueOf(fileRecords.get(i)[6])<hashChurn.get(fileRecords.get(i)[0])) {
		    			fileRecords.get(i)[6] = String.valueOf(hashChurn.get(fileRecords.get(i)[0]));
		    		}
		    	}
	    	}
	    } 
        
	}
	public static void updateChgSetSize(JSONObject commit, List<String[]> fileRecords) throws JSONException {
		List<String> fileList = new ArrayList<>();
		JSONArray fileListJson = commit.getJSONArray("files");
    	for(int j = 0; j<fileListJson.length(); j ++) {
    		fileList.add(fileListJson.getJSONObject(j).getString("filename"));	
    	}
    	for (int i = 0; i < fileRecords.size(); i++) {
	    	if(fileRecords.get(i).length >2) {
		    	if (fileList.contains(fileRecords.get(i)[0]) && commit.getString("Version").equals(fileRecords.get(i)[1])) {
		    		fileRecords.get(i)[8]=  String.valueOf((Integer.valueOf(fileRecords.get(i)[8])+fileListJson.length()));	
		    		if(Integer.valueOf(fileRecords.get(i)[9])<fileListJson.length()) {
		    			fileRecords.get(i)[9] = String.valueOf(fileListJson.length());
		    		}
		    	}
	    	}
	    }  
        		
	}
	
	public static List<String>  getBuggyFiles(String projName, String commitMessage) throws IOException, JSONException {
		List<String> fileList = new ArrayList<>();
		String token = new String(Files.readAllBytes(Paths.get(projName + "_Extended_Commits_Sha.JSON")));
        JSONArray object = new JSONArray(token);
        for(int i = 0; i< object.length(); i++) {
        	if(object.getJSONObject(i).getString("FixCommit").equalsIgnoreCase(commitMessage) ) {
        		JSONArray fileListJson = object.getJSONObject(i).getJSONArray("files");
        		for(int j = 0; j<fileListJson.length(); j ++) {
        			fileList.add(fileListJson.getJSONObject(j).getString("filename"));	
        		}
        	}	
        }
        return fileList;
		
	}
	public static void updateNumberOfRevision(JSONObject commit, List<String[]> fileRecords) throws IOException, JSONException {
		List<String> fileList = new ArrayList<>();
    	JSONArray fileListJson = commit.getJSONArray("files");
		for(int j = 0; j<fileListJson.length(); j ++) {
			fileList.add(fileListJson.getJSONObject(j).getString("filename"));	
		}
        for (int i = 0; i < fileRecords.size(); i++) {
	    	if(fileRecords.get(i).length >2) {
		    	if (fileList.contains(fileRecords.get(i)[0]) && commit.getString("Version").equals(fileRecords.get(i)[1])) {
		    		fileRecords.get(i)[2]=  String.valueOf((Integer.valueOf(fileRecords.get(i)[2])+1));	
		    	}
	    	}
	    }    
	}
	
	public static void updateNumberFix(JSONObject commit, List<String[]> fileRecords) throws JSONException {
		List<String> fileList = new ArrayList<>();  	
    	JSONArray fileListJson = commit.getJSONArray("files");
		for(int j = 0; j<fileListJson.length(); j ++) {
			fileList.add(fileListJson.getJSONObject(j).getString("filename"));	
		}
        for (int i = 0; i < fileRecords.size(); i++) {
	    	if(fileRecords.get(i).length >2) {
		    	if (fileList.contains(fileRecords.get(i)[0]) && !commit.getString("FixCommit").isEmpty() && commit.getString("Version").equals(fileRecords.get(i)[1])) {
		    		fileRecords.get(i)[3]=  String.valueOf((Integer.valueOf(fileRecords.get(i)[3])+1));	
		    	}
	    	}
	    }  
	}
	
	public static void setBuggy(String projName,JSONArray affectedVersions, String commitMessage, List<String[]> fileRecords) throws Exception {
		/*
		 * Con un altro metodo, costruisco la lista di file che sono presenti in Fix commit
		 * nel periodo AV = [IV, FV)
		 * 
		 */
		VersionParser vp = new VersionParser();
		List<String> versionsList = new ArrayList<>();
		String versionID = null;
		for(int i = 0; i< affectedVersions.length(); i++) {
			if(affectedVersions.getJSONObject(i).has("name")) {
				versionID = affectedVersions.getJSONObject(i).getString("name");
			}else {
			 Date versionDate = new SimpleDateFormat("yyyy-MM-dd").parse(affectedVersions.getJSONObject(i).getString("releaseDate"));
			 versionID = vp.getVersionName(versionDate, projName);
			}
			if(versionID != null)
				versionsList.add(versionID);	
		}
		System.out.println("Affected versions "+ versionsList);
		List<String> fileList = getBuggyFiles(projName, commitMessage) ;
		    for (int i = 0; i < fileRecords.size(); i++) {
		    	if(fileRecords.get(i).length >2) {
			    	if (fileList.contains(fileRecords.get(i)[0])&& versionsList.contains((fileRecords.get(i)[1]))) {
			    			fileRecords.get(i)[11]=  "yes";	
			    	}
		    	}
		    } 
	}
	
	/*
	 * trovo le AV
	 */
	public static void searchInTicketList(String projName, String commitMessage, List<String[]> fileRecords) throws Exception {
		JSONArray ticketsID = GetTicketInfo.getTicketID(projName);
		for(int i = 0; i< ticketsID.length(); i++) {
			String ticketID = ticketsID.getJSONObject(i).getString("key");
			if (ticketID.equalsIgnoreCase(commitMessage)) {
				JSONArray affectedVersions = ticketsID.getJSONObject(i).getJSONObject("fields").getJSONArray("versions");
				setBuggy(projName, affectedVersions, commitMessage, fileRecords);
				
			}
		}
	}
	public static void computeLoc(String projName, String version, String commitSha, String fileName, List<String[]> fileRecords) throws Exception {
		
		for (int i = 0; i < fileRecords.size(); i++) {
	    	if(fileRecords.get(i).length >2) {
		    	if (fileRecords.get(i)[4].equals("0") && fileName.equals((fileRecords.get(i)[0]))&& version.equals(((fileRecords.get(i)[1])))) {
		    		int loc = SizeCalculator.getSize(projName, commitSha);	
		    		fileRecords.get(i)[4]=  String.valueOf(loc);
		    			//System.out.println("LOC for: " + fileRecords.get(i)[0] + " =" + loc );
		    			
		    	}
	    	}
	    } 
	}

	public static List<String[]> findBuggyness(String projName, List<String[]> fileRecords) throws Exception {
		String token = new String(Files.readAllBytes(Paths.get(projName + "_Extended_Commits_Sha.JSON")));
        JSONArray object = new JSONArray(token);
        int totalRequests = 0;
        int total = object.length();
        System.out.println("Iterazioni per le metriche: " + total);
        /*
        for(int i = 0; i< total ; i++) {
        	System.out.println("Iteration: " + i);
        	String fixCommit =object.getJSONObject(i).getString("FixCommit");
        	updateNumberOfRevision(object.getJSONObject(i),fileRecords);
        	updateNumberFix(object.getJSONObject(i),fileRecords);
        	updateChgSetSize(object.getJSONObject(i),fileRecords);
        	updateChurn(object.getJSONObject(i),fileRecords);
        	if(!fixCommit.isEmpty()) {
        		searchInTicketList(projName, fixCommit, fileRecords);
        	}
        }
        
        for(int i = 0; i < fileRecords.size(); i++) {
        	if(Integer.valueOf(fileRecords.get(i)[2])>0) {
        		fileRecords.get(i)[7] = String.valueOf(Double.valueOf(fileRecords.get(i)[5])/Double.valueOf(fileRecords.get(i)[2]));
        		fileRecords.get(i)[10] = String.valueOf(Double.valueOf(fileRecords.get(i)[8])/Double.valueOf(fileRecords.get(i)[2]));
        	}
        }
        */
        VersionParser vp = new VersionParser();
    	List<String> versionsList = vp.getVersionList(projName);
    	versionsList.remove(versionsList.size()-1);
    	HashMap<String,JSONArray> listTreeSha = new HashMap<>();
    	for(int k = 0; k < versionsList.size(); k ++) {
    		CreateDataset.getTreeSha(projName, projName + "VersionInfo.csv", versionsList.get(k));
    		listTreeSha.put(versionsList.get(k),CreateDataset.getTreeSha(projName, projName + "VersionInfo.csv", versionsList.get(k)));
    	}
       // System.out.println("Total iteration for LOC: " + fileRecords.size());
		for(String version: versionsList) {
        		
        		JSONArray treeSha =listTreeSha.get(version);
        		//System.out.println(treeSha);
        		try {
        			System.out.println("Numero di iterazioni per " + version + " : " + treeSha.length());
                    for ( int i = 0; i < treeSha.length(); i++) {
                   
                    	if(totalRequests >=27750) {
                    		return fileRecords;
                    		//System.out.println("Sleep");
                    		//Thread.sleep(3600000);
                    		
                    	}
                    	System.out.println("Number: " + i);
                        String type = treeSha.getJSONObject(i).getString("type");
                        if(type.equals("blob") && treeSha.getJSONObject(i).getString("path").contains(".java")) {
                        	//JSONObject infoJson = GithubConnector.readJsonFromUrl(treeSha.getJSONObject(i).getString("url"));
                        	computeLoc(projName,version, treeSha.getJSONObject(i).getString("sha"),treeSha.getJSONObject(i).getString("path"), fileRecords);
                        	totalRequests++;
                        } 
                     }  
          	      }catch(Exception e) {
          		  e.printStackTrace();
          	      }
        		System.out.println("-----------------------");
        	}
		
		
        return fileRecords;
	}

}
