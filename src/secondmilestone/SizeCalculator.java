package secondmilestone;

import java.util.Base64;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
/*
 * 
 * LOC
 * 
 */
public class SizeCalculator {
	public static void main(String[] args) throws Exception {
		
		getSize("BOOKKEEPER","fa713c8018c82cbb37ee3459f5c7de451ec6d560");
	}
	public static int getSize(String projName, String fileSha) throws IOException, JSONException {
		String stringUrl= "https://api.github.com/repos/apache/"+projName+ "/git/blobs/"+fileSha;
		JSONObject json = GithubConnector.readJsonFromUrl(stringUrl);
		String fileInfo = json.getString("content");
		String decodedString=new String(Base64.getMimeDecoder().decode(fileInfo));
		int count = 0;
    	for(int i = 0; i < decodedString.length(); i++) {    
            if(decodedString.charAt(i) == '\n')    
                count++;    
        } 
		return count;
	}
}