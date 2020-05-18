package secondmilestone;

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
	  public static String readAll(Reader rd) throws IOException {
	      StringBuilder sb = new StringBuilder();
	      int cp;
	      while ((cp = rd.read()) != -1) {
	         sb.append((char) cp);
	      }
	      return sb.toString();
	   }
 
    public static JSONObject readJsonFromUrl(String stringUrl) throws IOException, JSONException {
        URL url = new URL(stringUrl);
        HttpsURLConnection uc = null;
        uc = (HttpsURLConnection) url.openConnection();
        uc.setRequestProperty("X-Requested-With", "Curl");
        String username =  "Melissari1997";
        String token =  "";
        String userpass = username + ":" + token;
        byte[] encodedBytes = Base64.getEncoder().encode(userpass.getBytes());
        String basicAuth = "Basic " + new String(encodedBytes);
        uc.setRequestProperty("Authorization", basicAuth);
        InputStreamReader inputStreamReader = new InputStreamReader(uc.getInputStream());
        try 
        (BufferedReader rd = new BufferedReader(inputStreamReader);
        ){
        JSONObject  jsonObject = new JSONObject(readAll(rd));
        return jsonObject;
      }  finally {
    	  inputStreamReader.close();
      }

    }
    public static JSONArray readJsonArrayFromUrl(String stringUrl) throws IOException, JSONException {
    	URL url = new URL(stringUrl);
        HttpsURLConnection uc = null;
      
        uc = (HttpsURLConnection) url.openConnection();
       
        uc.setRequestProperty("X-Requested-With", "Curl");
        String username =  "Melissari1997";
        String token =  "7be774adc1e222c2c6ee22318cb320ca1a232bbc";
        String userpass = username + ":" + token;
        byte[] encodedBytes = Base64.getEncoder().encode(userpass.getBytes());
        String basicAuth = "Basic " + new String(encodedBytes);
        uc.setRequestProperty("Authorization", basicAuth);

        InputStreamReader inputStreamReader = new InputStreamReader(uc.getInputStream());
        try 
        (BufferedReader rd = new BufferedReader(inputStreamReader);
        ){
        JSONArray  jsonArray = new JSONArray(readAll(rd));
        return jsonArray;
      } finally {
    	  inputStreamReader.close();
      }

    }
    
    public static void main(String[] args) throws IOException, JSONException {
    	System.out.println(readJsonFromUrl("https://api.github.com/repos/apache/bookkeeper/commits/8bb0cde556cf1dc07d62cd74ca6739b392242cf0"));
    	
    }
}