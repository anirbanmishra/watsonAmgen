package alchemyAPI;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class keywordExtractor {

    private String _apiKey;
    private String _requestUri = "http://access.alchemyapi.com/calls/";

    private keywordExtractor() {
    	Properties systemProps = System.getProperties();
    	String host="webproxy.amgen.com";
    	String port="8080";
		systemProps.put("http.proxyHost", host);
		systemProps.put("http.proxyPort", port);
		systemProps.put("https.proxyHost", host);
		systemProps.put("https.proxyPort", host);
		System.setProperties(systemProps);
    }


    static public keywordExtractor GetInstanceFromFile(String keyFilename)
        throws FileNotFoundException, IOException
    {
        keywordExtractor api = new keywordExtractor();
        api.LoadAPIKey(keyFilename);

        return api;
    }

    static public keywordExtractor GetInstanceFromString(String apiKey)
    {
        keywordExtractor api = new keywordExtractor();
        api.SetAPIKey(apiKey);

        return api;
    }

    public void LoadAPIKey(String filename) throws IOException, FileNotFoundException
    {
        if (null == filename || 0 == filename.length())
            throw new IllegalArgumentException("Empty API key file specified.");

        File file = new File(filename);
        FileInputStream fis = new FileInputStream(file);

        BufferedReader breader = new BufferedReader(new InputStreamReader(fis));

        _apiKey = breader.readLine().replaceAll("\\n", "").replaceAll("\\r", "");

        fis.close();
        breader.close();

        if (null == _apiKey || _apiKey.length() < 5)
            throw new IllegalArgumentException("Too short API key.");
    }

    public void SetAPIKey(String apiKey) {
        _apiKey = apiKey;

        if (null == _apiKey || _apiKey.length() < 5)
            throw new IllegalArgumentException("Too short API key.");
    }

    public void SetAPIHost(String apiHost) {
        if (null == apiHost || apiHost.length() < 2)
            throw new IllegalArgumentException("Too short API host.");

        _requestUri = "http://" + apiHost;
    }

    public Document TextGetRankedKeywords(String text) throws IOException, SAXException,
            ParserConfigurationException, XPathExpressionException {
        return TextGetRankedKeywords(text, new AlchemyAPI_KeywordParams());
    }
    
    public Document TextGetRankedKeywords(String text, AlchemyAPI_KeywordParams params) throws IOException, SAXException,
    ParserConfigurationException, XPathExpressionException 
    {
		CheckText(text);
		
		params.setText(text);
		return POST("TextGetRankedKeywords", "text", params);
	}

   
    private void CheckText(String text) {
        if (null == text )
            throw new IllegalArgumentException("Enter some text to analyze.");
    }

    
    private Document GET(String callName, String callPrefix, AlchemyAPI_Params params)throws IOException, SAXException,ParserConfigurationException, XPathExpressionException
	{
	    StringBuilder uri = new StringBuilder();
	    uri.append(_requestUri).append(callPrefix).append('/').append(callName)
	       .append('?').append("apikey=").append(this._apiKey);
	    uri.append(params.getParameterString());
	
	    URL url = new URL(uri.toString());
	    HttpURLConnection handle = (HttpURLConnection) url.openConnection();
	    handle.setDoOutput(true);
	
	    return doRequest(handle, params.getOutputMode());
	}

    private Document POST(String callName, String callPrefix, AlchemyAPI_Params params)
        throws IOException, SAXException,
               ParserConfigurationException, XPathExpressionException
    {
        URL url = new URL(_requestUri + callPrefix + "/" + callName);

        HttpURLConnection handle = (HttpURLConnection) url.openConnection();
        handle.setDoOutput(true);

        StringBuilder data = new StringBuilder();

        data.append("apikey=").append(this._apiKey);
        data.append(params.getParameterString());

        handle.addRequestProperty("Content-Length", Integer.toString(data.length()));

        DataOutputStream ostream = new DataOutputStream(handle.getOutputStream());
        ostream.write(data.toString().getBytes());
        ostream.close();

        return doRequest(handle, params.getOutputMode());
    }

    private Document doRequest(HttpURLConnection handle, String outputMode)
        throws IOException, SAXException,
               ParserConfigurationException, XPathExpressionException
    {
        DataInputStream istream = new DataInputStream(handle.getInputStream());
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(istream);

        istream.close();
        handle.disconnect();

        XPathFactory factory = XPathFactory.newInstance();

        if(AlchemyAPI_Params.OUTPUT_XML.equals(outputMode)) {
        	String statusStr = getNodeValue(factory, doc, "/results/status/text()");
        	if (null == statusStr || !statusStr.equals("OK")) {
        		String statusInfoStr = getNodeValue(factory, doc, "/results/statusInfo/text()");
        		if (null != statusInfoStr && statusInfoStr.length() > 0)
        			throw new IOException("Error making API call: " + statusInfoStr + '.');

        		throw new IOException("Error making API call: " + statusStr + '.');
        	}
        }
        else if(AlchemyAPI_Params.OUTPUT_RDF.equals(outputMode)) {
        	String statusStr = getNodeValue(factory, doc, "//RDF/Description/ResultStatus/text()");
        	if (null == statusStr || !statusStr.equals("OK")) {
        		String statusInfoStr = getNodeValue(factory, doc, "//RDF/Description/ResultStatus/text()");
        		if (null != statusInfoStr && statusInfoStr.length() > 0)
        			throw new IOException("Error making API call: " + statusInfoStr + '.');

        		throw new IOException("Error making API call: " + statusStr + '.');
        	}
        }

        return doc;
    }

    private String getNodeValue(XPathFactory factory, Document doc, String xpathStr)
        throws XPathExpressionException
    {
        XPath xpath = factory.newXPath();
        XPathExpression expr = xpath.compile(xpathStr);
        Object result = expr.evaluate(doc, XPathConstants.NODESET);
        NodeList results = (NodeList) result;

        if (results.getLength() > 0 && null != results.item(0))
            return results.item(0).getNodeValue();

        return null;
    }
    
    private static String getStringFromDocument(Document doc) {
        try {
            DOMSource domSource = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);

            return writer.toString();
        } catch (TransformerException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    public static void main(String args[]) throws XPathExpressionException, IOException, SAXException, ParserConfigurationException{
    	keywordExtractor ke=new keywordExtractor();
    	ke.SetAPIKey("8e26528ef4015d53320fc0f8624993cb01c1f305");
    	ke.SetAPIHost("gateway-a.watsonplatform.net/calls/");
    	
    	String keyword=ke.getStringFromDocument(ke.TextGetRankedKeywords("How do I download Acrobat Standard?"));
    	System.out.println(keyword);
    	Pattern pattern= Pattern.compile("<text>.*</text>");
    	Matcher matcher = pattern.matcher(keyword);
    	ArrayList<String> key=new ArrayList<String>();
    	while (matcher.find()) {
    	    key.add(StringUtils.substringBetween(matcher.group(0),"<text>","</text>"));
    	} 
    		System.out.println(key);
    	}
}