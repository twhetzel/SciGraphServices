import java.util.ArrayList;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class SciGraphParser {

	public static String parseJSON(String output) throws ParseException {
		boolean categoryStatus = true; 
		String categoryValue = null;
		//http://examples.javacodegeeks.com/core-java/json/java-json-parser-example/
		JSONParser parser = new JSONParser();
		JSONObject obj = (JSONObject)parser.parse(output);
		System.out.println("Obj:"+obj);

		JSONArray concepts = (JSONArray) obj.get("concepts");
		// take the elements of the json array
		/*for(int i=0; i<concepts.size(); i++){
			System.out.println("The " + i + " element of the array: "+concepts.get(i));
		}*/
		Iterator i = concepts.iterator();
		// take each value from the json array separately
		while (i.hasNext()) {
			JSONObject innerObj = (JSONObject) i.next();
			
			// Get deprecated status
			boolean deprecatedStatus = (boolean) innerObj.get("deprecated");
			System.out.println("DepStatus: "+deprecatedStatus+"\n");
						
			// Get category values 
			//System.out.println("categories "+ innerObj.get("categories"));			
			JSONArray categories = (JSONArray) innerObj.get("categories");
			if (categories.size() == 0) {
				//System.out.println("** No category values");
				categoryStatus = false;  //not currently used
				//If deprecated Status is true, then category is null and should be returned as deprecated
				categoryValue = "none-"+deprecatedStatus; //After a trial run, if all ok then swap "none" for deprecatedStatus
			}
			else {
				int index = 0;
				categoryValue = categories.get(index).toString(); 
			}
		}
		return categoryValue;
	}
}
