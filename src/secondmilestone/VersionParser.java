package secondmilestone;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import com.opencsv.CSVReader;

public class VersionParser {
	private String dateFormat = "yyyy-MM-dd'T'HH:mm";
	
	/*
	 * Prende la data di un commit ed un progetto, e ritorna a quale versione di quel progetto appartiene la commit
	 * 
	 */
	public String getVersionName(Date commitDate, String projName) throws IOException, ParseException {
		CSVReader csvReader = null;
		String  result =null;
		try {
			Reader reader = Files.newBufferedReader(Paths.get(projName + "VersionInfo.csv"));
			csvReader = new CSVReader(reader,';',
		    		',', '\'',1);
		
		    List<String[]>  records = csvReader.readAll();
		    for (int i = 0; i < records.size()-1; i++) {
		        Date dateOfVersion = new SimpleDateFormat(this.dateFormat).parse(records.get(i)[3]);
                Date nextDateOfVersion = new SimpleDateFormat(this.dateFormat).parse(records.get(i+1)[3]);
                if (commitDate.compareTo(dateOfVersion) == 0) {
	        			result = records.get(i)[2];
	        	}
	        	if (commitDate.compareTo(dateOfVersion) > 0 && commitDate.compareTo(nextDateOfVersion) <= 0) {
	        			result = records.get(i+1)[2];
	        	}          
		    }       
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}finally {
			csvReader.close();
		}
		
		return result;
	}
	public static void main(String[] args) throws ParseException, IOException {
		VersionParser vp = new VersionParser();
		Date date = new SimpleDateFormat(vp.dateFormat).parse("2010-02-27T00:00");
		System.out.println(vp.getVersionName(date, "OPENJPA"));
	}

}
