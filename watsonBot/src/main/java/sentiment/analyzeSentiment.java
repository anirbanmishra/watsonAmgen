package sentiment;

import java.io.IOException;
import java.util.Properties;

import com.ibm.watson.developer_cloud.tone_analyzer.v1.ToneAnalyzer;

public class analyzeSentiment {
    public static void main(String args[]) throws IOException {
    	String host="webproxy.amgen.com";
    	String port="8080";
    	String date="VERSION_DATE_2016_05_19";
    	Properties systemProps = System.getProperties();
		systemProps.put("http.proxyHost", host);
		systemProps.put("http.proxyPort", port);
		systemProps.put("https.proxyHost", host);
		systemProps.put("https.proxyPort", host);
		System.setProperties(systemProps);
    	ToneAnalyzer service = new ToneAnalyzer();
    	service.setUsernameAndPassword("aa4635c5-3581-47f0-bf0b-bb9b3d3deb45", "HnnNThV0tfmQ");
    	System.out.println(service.getTone("hello"));
        
    }
}