package secondmilestone;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import secondmilestone.VersionParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

public class CreateDataset {
	
	public static JSONObject getCommitFromVersionName(String fileName, String Version) throws IOException, JSONException {
		String token = new String(Files.readAllBytes(Paths.get(fileName)));
        JSONArray object = new JSONArray(token);
        int total = object.length();
        JSONArray commitsOfVersion = new JSONArray();     
        int i;
        for (i=0; i < total; i++) {
        	if( object.getJSONObject(i).getString("Version").equals(Version)) {
        		
        		commitsOfVersion.put(object.getJSONObject(i));
        		return commitsOfVersion.getJSONObject(0);
        	};
        }
        if(commitsOfVersion.length() == 0) {
        	System.out.println(Version);
        }       
		return null;
	}
	public static JSONObject getVersionCommit(String projName, String Version) throws IOException, JSONException {
		
		CSVReader csvReader = null;
		JSONObject result = new JSONObject();
		Logger logger = Logger.getLogger(GetVersionInfo.class.getName());
		try {
			Reader reader = Files.newBufferedReader(Paths.get(projName + "VersionInfo.csv"));
			csvReader = new CSVReader(reader,';',
		    		',', '\'',1);
		
		    List<String[]>  records = csvReader.readAll();
		    for (int i = 0; i < records.size(); i++) {
		    	System.out.println(records.get(i)[4]);
		    	if(records.get(i)[4].isEmpty()) {
		    		return getCommitFromVersionName(projName + "_Extended_Commits_Sha.JSON", Version);
		    	}
		    	else{
		    		if(records.get(i)[0].equals(Version)) {
			    		 result = GithubConnector.readJsonFromUrl("https://api.github.com/repos/apache/"+ projName+"/commits/" + records.get(i)[4]);
			    		 System.out.println(result);
			    		 return result;
			    	}
		    	}   
		    }       
		} catch (FileNotFoundException e) {
			logger.log(Level.INFO, "context", e);
		}finally {
			if(csvReader != null) {
				csvReader.close();
			}
		}
		return null;		
	}
	public static JSONArray getTreeSha(String projName, String fileName, String Version) throws IOException, JSONException {
		
		JSONObject versionCommit = getVersionCommit(projName,Version);
		JSONObject treeSha = GithubConnector.readJsonFromUrl(versionCommit.getJSONObject("commit").getJSONObject("tree").getString("url")+"?recursive=1");
		System.out.println(treeSha.getJSONArray("tree"));
		return treeSha.getJSONArray("tree");
	}
 
	public static void main(String[] args) throws ParseException, JSONException, IOException {
  	  String projName = "OPENJPA";
  	  String fileName = projName + "VersionInfo.csv";
  	  VersionParser vp = new VersionParser();
  	  List<String> versionsList = vp.getVersionList(projName);
  	  CSVWriter csvWriter =  new CSVWriter(new FileWriter(projName + "Dataset.csv"),';',
            CSVWriter.NO_QUOTE_CHARACTER,
            CSVWriter.DEFAULT_ESCAPE_CHARACTER,
            CSVWriter.DEFAULT_LINE_END);

     csvWriter.writeNext(new String[] {"FileName","Version Name","Buggy"});
  	  for( String version :versionsList) {
  	  	  JSONArray treeSha = getTreeSha(projName, fileName, version);
  	  	  try {
            for ( int i = 0; i < treeSha.length(); i++) {
                String type = treeSha.getJSONObject(i).getString("type");
                if(type.equals("blob")) {
                	csvWriter.writeNext(new String[] {treeSha.getJSONObject(i).getString("path"),version, "No"});
                }     
             }  
  	      }catch(Exception e) {
  		  e.printStackTrace();
  	      }
	 }
  	 if (csvWriter != null) {
		 csvWriter.close();
	 }

   }
	
}
