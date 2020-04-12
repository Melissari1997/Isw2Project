package firstMilestone;
import java.io.FileWriter;
import java.io.IOException;

import com.opencsv.CSVWriter;
public class CSV_Test {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		CSVWriter csvWriter = new CSVWriter(new FileWriter("example.csv"), ';');
		String[] entries = new String[] {"first","second","third"};
		csvWriter.writeNext(entries);   
		csvWriter.close();
 
        csvWriter.close();
	}

}
