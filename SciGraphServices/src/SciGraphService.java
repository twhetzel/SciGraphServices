import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.json.simple.parser.ParseException;


public class SciGraphService {

	public static String findByURI(String termIRI) throws ParseException  {
		boolean categoryStatus = true;
		String categoryValue = null;
		try {
			/*@SuppressWarnings("deprecation")
			String urlEncodedTermIRI = URLEncoder.encode(termIRI);
			URL url = new URL("http://matrix.neuinfo.org:9000/scigraph/vocabulary/uri/"+urlEncodedTermIRI);
			*/
			//Create File writer to generate error log file

			File errorLog = new File("./data_files/error_log_jan222014.txt");
			if (!errorLog.exists()) {
				errorLog.createNewFile();
			}
			
			FileWriter fw = new FileWriter(errorLog.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			
			// http://matrix.neuinfo.org:9000/scigraph/docs/#!/vocabulary/findById -> findByURI is down
			URL url = new URL("http://matrix.neuinfo.org:9000/scigraph/vocabulary/id/"+termIRI);
			System.out.println("URL "+url);
			
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");

			if (conn.getResponseCode() != 200) { 
				//throw new RuntimeException("Failed : HTTP error code : "+ conn.getResponseCode()); //Comment out to allow script to continue running
				bw.write("SciGraph call: "+url+"\n");
				bw.write("Failed : HTTP error code : "+ conn.getResponseCode()+"\n");
				System.err.println("Failed : HTTP error code : "+ conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			String output;
			//System.out.println("Output from Server ....");
			while ((output = br.readLine()) != null) {
				//System.out.println(output);
				// Parse JSON response and check if "categories" is empty or null
				categoryValue = SciGraphParser.parseJSON(output);
				//System.out.println("Returned CV:"+categoryValue);
			}
			conn.disconnect();
			bw.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return categoryValue;
	}
}
