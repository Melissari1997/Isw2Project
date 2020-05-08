package secondmilestone;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.opencsv.CSVReader;

public class VersionParser {
	private String dateFormat = "yyyy-MM-dd'T'HH:mm";
	private Logger logger = Logger.getLogger(GetVersionInfo.class.getName());
	
	public List<String> getVersionList(String projName) throws IOException{
		CSVReader csvReader = null;
		List<String>  result =  new ArrayList<>();
		try {
			Reader reader = Files.newBufferedReader(Paths.get(projName + "VersionInfo.csv"));
			csvReader = new CSVReader(reader,';',
		    		',', '\'',1);
		
		    List<String[]>  records = csvReader.readAll();
		    for (int i = 0; i < records.size()-1; i++) {
		    	result.add(records.get(i)[2]);         
		    }       
		} catch (FileNotFoundException e) {
			this.logger.log(Level.INFO, "context", e);
		}finally {
			if(csvReader != null) {
				csvReader.close();
			}
		}
		
		return result;
	}
	
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
			this.logger.log(Level.INFO, "context", e);
		}finally {
			if(csvReader != null) {
				csvReader.close();
			}
		}
		
		return result;
	}
}
