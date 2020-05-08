package secondmilestone;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class testDate {

	public static void main(String[] args) throws ParseException, IOException {
		// TODO Auto-generated method stub
		VersionParser vp = new VersionParser();
	   List<String> versionList = vp.getVersionList("OPENJPA");
	   String temp = versionList.toString();

	
		
		//System.out.println((temp));
		compareDate("2020-05-07T08:02:45Z");

	}
	public static void compareDate(String dateToCompare) throws ParseException {
		Date formattedDateToCompare = new SimpleDateFormat("yyyy-MM-dd").parse(dateToCompare);
		Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").parse("2010-02-27T00:00");
		if (date.compareTo(formattedDateToCompare) > 0) {
			System.out.println("Successiva");
		}
		if (date.compareTo(formattedDateToCompare) == 0) {
			System.out.println("Uguale");
		}
		if (date.compareTo(formattedDateToCompare) < 0) {
			System.out.println("Precedente");
		}
	}
}
