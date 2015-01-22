import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.parser.ParseException;




public class Main {

	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) {
		Map<String, ArrayList<String>> mapAll = getData();
		System.out.println("** Map Size: "+mapAll.size());
		checkCategoryAssignment(mapAll);		 
		System.out.println("** Done **");
	}

	/**
	 * Check category assignment for all term IRIs in file/mapAll
	 * @param mapAll
	 * @throws InterruptedException 
	 */
	private static void checkCategoryAssignment(Map<String, ArrayList<String>> mapAll) {
		int lineCount = -1; //was 0
		int restartLine = -1; //adjust as needed to re-start script
		for (Entry<String, ArrayList<String>> entry : mapAll.entrySet()) {
			lineCount++;
			String key = entry.getKey();
			ArrayList<String> values = entry.getValue();
			String idFragment = key;
			String termIRI = values.get(0);
			try {
				// Add re-start ability 
				if (lineCount > restartLine) {
					Thread.sleep(20);
					String categoryValue = SciGraphService.findByURI(idFragment);		
					System.out.println("LINE:"+lineCount+" Key: " + entry.getKey() + " Value: "+ entry.getValue()+" CV: "+categoryValue+"\n");				
					//bw.write("Key: "+key+TAB+"Value: "+values+TAB+"CV: "+categoryValue+"\n");
					writeAnalysisFile(lineCount, key, values, categoryValue);
				}
			}
			catch (InterruptedException | ParseException e) {
				e.printStackTrace();
			}	
		}
	} 



	/**
	 * Write results to a file
	 * @param lineCount 
	 * @param key
	 * @param values
	 * @param categoryValue
	 */
	private static void writeAnalysisFile(int lineCount, String key, ArrayList<String> values, String categoryValue) {
		String TAB = "\t";

		try {
			File file = new File("./data_files/category_analysis_jan222014.txt");
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.append("Line:"+lineCount+TAB+"Key: "+key+TAB+"Value: "+values+TAB+"CV: "+categoryValue+"\n");
			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * Read in data file containing all Concept Ids, IRIs, and ontology file IRI
	 * E.g. PR_000006763	[http://purl.obolibrary.org/obo/PR_000006763, http://ontology.neuinfo.org/NIF/BiomaterialEntities/pr.owl]	http://purl.obolibrary.org/obo/PR_000006763	http://ontology.neuinfo.org/NIF/BiomaterialEntities/pr.owl
	 * @return 
	 */
	private static Map<String, ArrayList<String>> getData() {
		File file = new File("./data_files/mapData.txt");
		Map<String,ArrayList<String>> mapAll = ReadFile.readFile(file);
		return mapAll;
	}

}
