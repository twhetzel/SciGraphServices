import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchDataFederation {

	public static void runFederationSearch(ArrayList<String> iriFragments) throws IOException {
		// Iterate through ArrayList of IRI Fragments
		for (String termId : iriFragments) {
			// Web service Documentation: http://neuinfo.org/servicesv1/resource_FederationService.html#path__federation_search.html
			String prodUri = "http://nif-services.neuinfo.org/servicesv1/v1/federation/search?q="+termId;
			//System.out.println("Query String: "+prodUri);
			File file = new File("/Users/whetzel/Documents/workspace/DeprecatedConceptswithData/retired_terms_data_check.txt"); 

			// if file doesn't exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getName(),true);
			BufferedWriter bw = new BufferedWriter(fw);

			int prodTotalResults = queryProd(prodUri);
			System.out.println("Total Search results for termId: "+termId+" "+prodTotalResults+" "+prodUri);
			bw.write("Total Search results for termId: "+termId+"\t"+prodTotalResults+"\n");
			bw.close();	
		}
	}


	private static int queryProd(String prodUri) {
		String total;
		int prodTotalResults = 0;
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

			URL url = new URL(prodUri);
			// Allow for slow web service responses by setting connection timeout
			URLConnection con = url.openConnection();
			con.setConnectTimeout(150000); // 15 seconds
			Document doc = dBuilder.parse(con.getInputStream());

			// http://stackoverflow.com/questions/10689900/get-xml-only-immediate-children-elements-by-name by BizNuge
			doc.getDocumentElement().normalize();	
			//String root = doc.getDocumentElement().getNodeName();

			Element docEl = doc.getDocumentElement(); 
			Node childNode = docEl.getFirstChild();     
			while( childNode.getNextSibling()!=null ){          
				childNode = childNode.getNextSibling();         
				if (childNode.getNodeType() == Node.ELEMENT_NODE) {         
					Element childElement = (Element) childNode;    
					total = childElement.getAttribute("total");
					prodTotalResults = Integer.parseInt(total);
					System.out.println("Results Total: " + childElement.getAttribute("total"));          
				}       
				else {
					System.out.println("Result Attribute not Found");
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error with Federation web service for call: "+prodUri);
		}
		return prodTotalResults;
	}
}


