package importAssertion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Parser {

	public class Assertion {
		public String name;
		public String condition;
		public String select;
	}

	public List<Assertion> precheckedAssertionsInsert;
	public List<Assertion> precheckedAssertionsCheck;
	public List<Assertion> precheckedAssertionsDrop;

	public Parser(String file) {
		File data = checkFile(file);
		precheckedAssertionsCheck = new ArrayList<>();
		precheckedAssertionsInsert= new ArrayList<>();
		precheckedAssertionsDrop = new ArrayList<>();
		List<String> rawData = null;
		try {
			rawData = getRowData(data);
		} catch (IOException e) {
			System.out.println("Fehler beim einlesen. Programm Ende");
			System.exit(-1);
		}
		Map<String,List<String>> assertions = parseAssertions(rawData);
		runPreCheck(assertions);
		grepSelects();
	}

	private File checkFile(String file) {
		if (!file.endsWith(".asn")) {
			System.out
					.println("Datei sollte mit \".asn\" enden\nProgramm Ende");
			System.exit(-1);

		}
		File data = new File(file);
		if (!data.exists()) {
			System.out.println("Datei existiert nicht");
			System.exit(-1);
		}
		return data;
	}

	private List<String> getRowData(File f) throws IOException {
		List<String> val = new ArrayList<>();
		BufferedReader br = new BufferedReader(new FileReader(f));
		String s;
		do {
			s = br.readLine();
			if (s == null || s.length() == 0 || s.trim().startsWith("#"))
				continue;
			val.add(s.trim());
		} while (s != null);
		br.close();
		return val;
	}

	private Map<String,List<String>> parseAssertions(List<String> rawData) {
		List<String> data = new ArrayList<>();
		int countBrakets = 0;
		boolean inQuotes = false;
		String assertion = "";
		for (String s : rawData) {
			char[] letters = s.toCharArray();
			if (assertion.length() > 0
					&& s.toLowerCase().trim().startsWith("create assertion")) {
				System.out.println("Error:" + assertion);
				System.out
						.println("\tERROR: Assertion skipped. Quotes, brakets or ; is missing. New \"create assertion\" found"
								);

				inQuotes = false;
				assertion = "";
				countBrakets = 0;
			}
			for (int i = 0; i < letters.length; i++) {
				switch (letters[i]) {
				case '"':
					inQuotes = !inQuotes;
					continue;
				case '(':
					if (!inQuotes)
						countBrakets++;
					continue;
				case ')':
					if (!inQuotes)
						countBrakets--;
					continue;
				}
			}
			assertion += " " + s;
			if (!inQuotes && countBrakets == 0 && s.endsWith(";")) {
				data.add(assertion);
				assertion = "";
			}
		}
		System.out.println("In " + rawData.size() + " relevanten Zeilen "
				+ data.size() + " Assertions gefunden");
		return data;
	}

	private void grepSelects() {
		List<Assertion> fails = new ArrayList<>();
		for (Assertion a : precheckedAssertions) {
			a.select = getSelectFromAssertion(a);
			if (a.name == null || a.condition == null || a.select == null) {
				if (a.name == null)
					System.out.println("ERROR: Assertion has no name");
				else if (a.condition == null){
					System.out.println("Error in assertion: " + a.name);
					System.out.println("\t ERROR: Assertion " + a.name
							+ "has not condition");
				}
				else if (a.select == null){
					System.out.println("Error in assertion: " + a.name);
					System.out
							.println("\t ERROR: Could not fetch select from condition.");
				}
				fails.add(a);
			}
		}
		precheckedAssertions.removeAll(fails);
		System.out.println("Precheck done: " + precheckedAssertions.size()
				+ " passed");
		System.out.println("-----------------------------------------------");
	}

	private String getSelectFromAssertion(Assertion a) {
		String[] s = a.condition.split(" ");
		String condition;
		// check for exists or not exists
		if (s[0].equals("not") && s[1].startsWith("exist")
				|| s[0].startsWith("exist")) {
			// remove first and last braked
			condition = a.condition.substring(a.condition.indexOf('(') + 1,
					a.condition.length() - 1).trim();

		} else {
			condition = null;
			System.out.println("Error in assertion: " + a.name);
			System.out.println("\tERROR: The condtion"
					+ " should start with \"exist\" or \"not exist\"");
		}
		return condition;
	}

	private void runPreCheck(Map<String,List<String>> rawData) {
		System.out.println("---------------------------------------");
		System.out.println("Running Precheck...");
		for (String s : rawData) {
			String[] words = s.trim().split(" ");
			// check keywords
			if (words.length < 5)
				continue;// not a valid number of tokens
			if (!words[0].toLowerCase().equals("create")) {
				System.out.println("Error: " + s);
				System.out.println("\tERROR: Missing Keyword create at " + words[0]);
				continue;// first word has to be create, in upper or lower or
							// mixed case
			}
			if (!words[1].toLowerCase().equals("assertion")) {
				System.out.println("Error: " + s);
				System.out.println("\tERROR: Missing Keyword assertion at " + words[1]);
				continue;// see to rows above
			}
			if (!words[3].toLowerCase().startsWith("check")) {
				System.out.println("Error: " + s);
				System.out.println("\tERROR: Missing Keyword check " + words[3]);
				continue;// see four rows above
			}
			if (words[3].length() > 5)
				words[3] = words[3].substring(5);
			else
				words[3] = "";
			// by reaching this line, the assertion may be okay
			Assertion a = new Assertion();
			a.name = words[2];
			a.condition = "";
			precheckedAssertions.add(a);
			for (int i = 3; i < words.length; i++)
				a.condition += " " + words[i];
			a.condition = a.condition.trim();
			if (a.condition.endsWith(";")) {// belongs to the create assertion
				a.condition = a.condition
						.substring(0, a.condition.length() - 1).trim();
			}
			if (a.condition.startsWith("(")) {
				a.condition = a.condition.substring(1);
				if (a.condition.endsWith(")")) // belongs to
					a.condition = a.condition.substring(0,
							a.condition.length() - 1).trim();
				else{
					System.out.println("Warning in assertion: " + a.name);
					System.out.println("\tWARNING: Check Brackets!");
				}		
			}

			// System.out.println(a.condition);
		}
	}
}
