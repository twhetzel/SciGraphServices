import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class AnalyzeTerms {

	/**
	 * @param args
	 * @throws SAXException 
	 * @throws IOException 
	 * @throws ParserConfigurationException 
	 */
	public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
		/*
		 * Read in data file, parse, and check that for any term that has an 
		 * overlapping synonym in a "nif" term it is mapped to an OBO Foundry term
		 */
		HashMap<String, String> synTermMap = readAndParseFile();
		checkIds(synTermMap);
		System.out.println("** Finished **");
	}

	/*
	 * Check that all Ids are mapped via Ontoquest Concept Web service
	 */
	private static void checkIds(HashMap<String, String> synTermMap) throws ParserConfigurationException, IOException, SAXException {
		ArrayList<String> nifTerms = new ArrayList<String>();
		ArrayList<String> nonNifTerms = new ArrayList<String>();
		String eqTermCheck = null;
		
		File outputFile = new File ("./sameSynomymsAnalysis.txt");
		FileWriter fw = new FileWriter(outputFile.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw); 
		
		Iterator it = synTermMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			System.out.println(pairs.getKey() + " = " + pairs.getValue());
			it.remove(); // avoids a ConcurrentModificationException
			String synonymKey = (String) pairs.getKey();
			String termIdValues = (String) pairs.getValue();

			//Format values
			System.out.println("IDs to Format: "+termIdValues);
			String leftFormat = termIdValues.replace("[", "");
			String rightFormat = leftFormat.replace("]", "");
			String[] termsToCheck = rightFormat.split(",");
			
			bw.write(synonymKey+"\t"+termIdValues+"\t");
			
			// Analyze termIds, "nif" terms include "nifext", "birnlex", "nlx_inv", "sao" 
			if (termIdValues.contains("nifext") || termIdValues.contains("birnlex") 
					|| termIdValues.contains("nlx_inv") || termIdValues.contains("sao") || termIdValues.contains("nlx_res") 
					|| termIdValues.contains("nlx_anat") || termIdValues.contains("nlx_dys")) {
				System.out.println("** ID VALUES CONTAINS NIF TERM ID ** ");

				for (int x=0; x<termsToCheck.length; x++) {
					if (termsToCheck[x].contains("nifext") || termsToCheck[x].contains("birnlex") 
							|| termsToCheck[x].contains("nlx_inv") || termsToCheck[x].contains("sao") 
							|| termsToCheck[x].contains("nlx_res") || termsToCheck[x].contains("nlx_anat")
							|| termsToCheck[x].contains("nlx_dys")) {
						nifTerms.add(termsToCheck[x]);
						//System.err.println("NIF Term(s): "+nifTerms);
					}
					else {
						nonNifTerms.add(termsToCheck[x].trim());
					}
				}
				if (nifTerms.size() == 1) {
					System.out.println("Single NIF Term: "+nifTerms);
					String nTerm = null;
					for (String n : nifTerms) {
						nTerm = n.trim();
					}
					// Call Ontoquest Web service with all other IDs and check that it returns the NIF ID
					eqTermCheck = checkForEquivalentTerm(nTerm, nonNifTerms);
					System.out.println(eqTermCheck);
					System.out.println();
					bw.write(eqTermCheck+"\n");
				}
				else {
					System.out.println("More than 1 NIF Term found "+nifTerms);
					eqTermCheck = "** Review Manually **";
					System.out.println(eqTermCheck);
					bw.write("** Review Manually ** \n");
				}		
				nifTerms.clear();
				nonNifTerms.clear();
				System.out.println();
			}
			else {
				System.out.println("No NIF Term ID "+termIdValues+"\n");
				bw.write("No NIF Term ID values in: "+termIdValues+"\n");
			} 
		}
	}	



	/*
	 * Check web service for matching NIF term
	 */
	private static String checkForEquivalentTerm(String nTerm,
			ArrayList<String> nonNifTerms) throws ParserConfigurationException, IOException, SAXException {
		String ontoquestConceptURL = "http://nif-services.neuinfo.org/ontoquest/concepts/";
		String match = nTerm;
		String matchingTermResponse = null;
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

		// Check all nonNIfTerms with Web service
		for (String checkId : nonNifTerms) {
			System.out.println("Try web service for: "+ontoquestConceptURL+checkId);
			URL url = new URL(ontoquestConceptURL+checkId);
			URLConnection con = url.openConnection();
			con.setConnectTimeout(150000); // 15 seconds
			Document doc = dBuilder.parse(con.getInputStream());

			// http://stackoverflow.com/questions/10689900/get-xml-only-immediate-children-elements-by-name by BizNuge
			doc.getDocumentElement().normalize();	
			String root = doc.getDocumentElement().getNodeName();

			String successMessage = "success";
			if (successMessage.equals(root)) {
				System.out.println("Root: "+root);

				Element docEl = doc.getDocumentElement(); 
				Node childNode = docEl.getFirstChild(); 
				//System.out.println("childNode: "+childNode);

				NodeList nodeListLabel = doc.getElementsByTagName("class");
				for(int s=0; s<nodeListLabel.getLength(); s++) {
					Node firstSearchNode = nodeListLabel.item(s);

					if(firstSearchNode.getNodeType() == Node.ELEMENT_NODE){
						Element firstSearchElement = (Element)firstSearchNode;                    

						NodeList termName = firstSearchElement.getElementsByTagName("name");
						Element termNameValue = (Element)termName.item(0);	
						NodeList textOVIDList = termNameValue.getChildNodes();					
						System.out.println("Term Name: "+((Node)textOVIDList.item(0)).getNodeValue().trim());
						String nameValue = ((Node)textOVIDList.item(0)).getNodeValue().trim();
						if (match.equals(nameValue)) {
							System.out.println("Terms Match");
							matchingTermResponse = "Match";
						}
						else {
							System.out.println("Terms Do Not match");
							matchingTermResponse = "Do Not match";
						}
					}
				}
			}
			else {
				System.out.println("** Web service call failed for: "+ontoquestConceptURL+checkId);
			}


		}
		return matchingTermResponse;
	}


	/*
	 * Read in file to analyze
	 */
	private static HashMap<String, String> readAndParseFile() {
		BufferedReader br = null;
		HashMap<String, String> termMap = new HashMap<String, String>();
		String synonymKey = null;
		String termIdValues = null;

		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader("/Users/whetzel/git/OntologyQA/QA-SameLabelsSynonyms/allSameSynonyms_11172014.txt"));

			while ((sCurrentLine = br.readLine()) != null) {
				//System.out.println(sCurrentLine);
				String[] termLine = sCurrentLine.split("\t");
				synonymKey = termLine[0];
				termIdValues = termLine[1];
				termMap.put(synonymKey, termIdValues);	
				//System.out.println("Key: "+synonymKey+"\t"+"Values: "+termIdValues);
			}

			Iterator it = termMap.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pairs = (Map.Entry)it.next();
				System.out.println(pairs.getKey() + " = " + pairs.getValue());
				//it.remove(); // avoids a ConcurrentModificationException
			}	
			System.out.println("Map Size: "+termMap.size());

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}	
		return termMap;
	}

}
