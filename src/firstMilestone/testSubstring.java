package firstMilestone;

public class testSubstring {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String test = "[MAHOUT-2088] Update Apache parent pom to latest version (23) from 18 (#392)\\n\\n* [MAHOUT-2088] Update Apache parent pom to latest version (23) from 18\\r\\n\\r\\n* [MAHOUT-2088]update Apache Parent pom to latest version (23),prepare for RC5: reset to 14.1-SNAPSHOT\\r\\n\\r\\n* [MAHOUT-2088] update Apache Parent pom to latest version (23), tweaks\"";
		int start = test.toLowerCase().indexOf("mahouT".toLowerCase());
		
		int end = test.indexOf("]");
	
		
		if (start!= -1 && end != -1 && start<end) {
			String substring = test.substring(start,end );
			System.out.println(substring);
		}
		else {
			System.out.println("Not present");
		}
	
	}

}
