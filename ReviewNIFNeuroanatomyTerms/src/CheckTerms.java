import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class CheckTerms {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// Check terms in data file to see if they are in NIFSTD by
		// using them against the OQ concept web service
		readFile();
	}

	/**
	 * Read in file of terms to review
	 * @throws IOException 
	 */
	private static void readFile() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader("./data_files/poldrack_NIF-anatomical-terms.txt"));
		String sCurrentLine;
		File file = new File("./data_files/neuroanatomy-term-status.txt");
		// if file doesnt exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		
		while ((sCurrentLine = br.readLine()) != null) {
			System.out.println("Line: "+sCurrentLine);
			
			String[] termLine = sCurrentLine.split("\t");
			String term = null;
			String relation = null;
			String parentTerm = null;
			String synonym = null;
			int i = -1;
			
			for (String s : termLine) {
				i++;
				//System.out.println("i["+i+"]: \""+s+"\"");
				
				if (i == 0 ) { 
					term = termLine[0];
				}
				if (i == 1 ) {
					relation = termLine[1];
				}
				if (i == 2) {
					parentTerm = termLine[2];
				}
				if (i == 3) {
					synonym = termLine[3];
				}
			}
			/*System.out.println("\nTerm: "+term+"\n Rel: "+relation
						+"\n Parent: "+parentTerm+"\n Syn: "+synonym+"\n");
		*/
			// Check for term via OQ web service and write to file
			try {
				checkTerm(term, bw);
			} catch (ParserConfigurationException | SAXException e) {
				e.printStackTrace();
			}
		}
		bw.close();
	}

	/**
	 * Check for term in NIFSTD from Ontoquest web services
	 * @param term
	 * @param bw 
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	private static void checkTerm(String term, BufferedWriter bw) throws ParserConfigurationException, IOException, SAXException {
		String ontoquestConceptURL = "http://nif-services.neuinfo.org/ontoquest/concepts/term/";
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

		// Check term with Web service
		System.out.println("Try web service for: "+ontoquestConceptURL+term);
		String encodedTerm = URLEncoder.encode(term, "UTF-8");
		URL url = new URL(ontoquestConceptURL+encodedTerm);
		URLConnection con = url.openConnection();
		con.setConnectTimeout(150000); // 15 seconds
		Document doc = dBuilder.parse(con.getInputStream());

			// http://stackoverflow.com/questions/10689900/get-xml-only-immediate-children-elements-by-name by BizNuge
			doc.getDocumentElement().normalize();	
			String root = doc.getDocumentElement().getNodeName();

			String successMessage = "success";
			if (successMessage.equals(root)) {
				//System.out.println("Root: "+root);

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
						//String nameValue = ((Node)textOVIDList.item(0)).getNodeValue().trim();
						
						NodeList termLabel = firstSearchElement.getElementsByTagName("label");
						Element termLabelValue = (Element)termLabel.item(0);
						NodeList termLabelNL = termLabelValue.getChildNodes();	
						System.out.println("Term Label: "+((Node)termLabelNL.item(0)).getNodeValue().trim());
					
						bw.write("Term:\t"+term+"\tTerm Name:"+((Node)textOVIDList.item(0)).getNodeValue().trim()
								+"\tTerm Label:"+((Node)termLabelNL.item(0)).getNodeValue().trim());
					}
				}
				bw.write("\n");
			}
			else {
				System.out.println("** Web service call failed for: "+ontoquestConceptURL+term);
				bw.write("** Web service call failed for: "+ontoquestConceptURL+term+"\n");
			}	
		System.out.println();
	}
	
	
}
