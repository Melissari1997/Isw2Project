package firstmilestone;
import java.io.*;
import java.net.*;

public class githubConnector {

	 public  String getHTML(String urlToRead) throws IOException {
	      StringBuilder result = new StringBuilder();
	      URL url = new URL(urlToRead);
	      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	      conn.setRequestMethod("GET");
	      BufferedReader rd = null;
		try {
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      String line;
	      while ((line = rd.readLine()) != null) {
	         result.append(line);
	      }
	      rd.close();
	      return result.toString();
	   }

	   public static void main(String[] args) throws Exception
	   {
		 githubConnector test = new githubConnector();
	     System.out.println(test.getHTML(args[0]));
	   }
}


