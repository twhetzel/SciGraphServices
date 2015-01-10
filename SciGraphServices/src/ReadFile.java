import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ReadFile {
	public static Map<String, ArrayList<String>> readFile(File file) {
		Map<String, ArrayList<String>> mapAll = new HashMap<String, ArrayList<String>>();
		BufferedReader br = null;
		String[] items = null;
		
		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader(file));

			while ((sCurrentLine = br.readLine()) != null) {
				//System.err.println(sCurrentLine);
				// Add lines to mapAll
				items = sCurrentLine.split("\t");
				
				// Build-up values ArrayList
				ArrayList<String> values = new ArrayList<String>();
				values.add(items[2]);
				values.add(items[3]);
				String key = items[0];
				mapAll.put(key, values);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return mapAll;
	}
}
