package firstmilestone;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Base64;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GithubConnector {
	private GithubConnector() {
	}
	  public static String readAll(Reader rd) throws IOException {
	      StringBuilder sb = new StringBuilder();
	      int cp;
	      while ((cp = rd.read()) != -1) {
	         sb.append((char) cp);
	      }
	      return sb.toString();
	   }
	public static InputStreamReader setupConnection(String stringUrl) throws IOException {
		URL url = new URL(stringUrl);
        HttpsURLConnection uc = null;
        uc = (HttpsURLConnection) url.openConnection();
        uc.setRequestProperty("X-Requested-With", "Curl");
        String username =  "Melissari1997";
        String token =  GitInfo.getToken(); 
        String userpass = username + ":" + token;
        byte[] encodedBytes = Base64.getEncoder().encode(userpass.getBytes());
        String basicAuth = "Basic " + new String(encodedBytes);
        uc.setRequestProperty("Authorization", basicAuth);
        return new InputStreamReader(uc.getInputStream());
	}
    public static JSONObject readJsonFromUrl(String stringUrl) throws IOException, JSONException {
        
        InputStreamReader inputStreamReader = setupConnection(stringUrl);
        try 
        (BufferedReader rd = new BufferedReader(inputStreamReader);
        ){
        return new JSONObject(readAll(rd));
      }  finally {
    	  inputStreamReader.close();
      }

    }
    public static JSONArray readJsonArrayFromUrl(String stringUrl) throws IOException, JSONException {
    	InputStreamReader inputStreamReader = setupConnection(stringUrl);
        try 
        (BufferedReader rd = new BufferedReader(inputStreamReader);
        ){
        return new JSONArray(readAll(rd));
      } finally {
    	  inputStreamReader.close();
      }

    }
}