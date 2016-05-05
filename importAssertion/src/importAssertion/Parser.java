package importAssertion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Parser {

	public Parser(String file) {
		File data = checkFile(file);
		List<String> rawData = null;
		try {
			rawData = getRowData(data);
		} catch (IOException e) {
			System.out.println("Fehler beim einlesen. Programm Ende");
			System.exit(-1);
		}
		List<String> assertions = parseAssertions(rawData);
	}

	private File checkFile(String file) {
		if (!file.endsWith(".asn")) {
			System.out.println("Datei sollte mit \".asn\" enden\nProgramm Ende");
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

	private List<String> parseAssertions(List<String> rawData) {
		List<String> data = new ArrayList<>();
		int countBrakets = 0;
		boolean inQuotes = false;
		String assertion = "";
		for (String s : rawData) {
			char[] letters = s.toCharArray();
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
		System.out.println("In "+ rawData.size()+" relevanten Zeilen " + data.size() + " Assertions gefunden");
		return data;
	}
}
