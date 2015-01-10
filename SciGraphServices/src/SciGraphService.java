import java.io.BufferedReader;
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
			
			// http://matrix.neuinfo.org:9000/scigraph/docs/#!/vocabulary/findById -> findByURI is down
			URL url = new URL("http://matrix.neuinfo.org:9000/scigraph/vocabulary/id/"+termIRI);
			//System.out.println("URL "+url);
			
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");

			if (conn.getResponseCode() != 200) { 
				throw new RuntimeException("Failed : HTTP error code : "
				+ conn.getResponseCode());
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
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return categoryValue;
	}
}
