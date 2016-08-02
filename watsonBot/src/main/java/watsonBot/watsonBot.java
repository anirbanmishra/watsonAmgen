package watsonBot;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.apache.http.client.ClientProtocolException;

import com.ibm.watson.developer_cloud.natural_language_classifier.v1.NaturalLanguageClassifier;
import com.ibm.watson.developer_cloud.natural_language_classifier.v1.model.Classification;
import com.ibm.watson.developer_cloud.natural_language_classifier.v1.model.Classifier;
import com.ibm.watson.developer_cloud.natural_language_classifier.v1.model.Classifiers;

import theDialogMgmt.dialogMgmt;

public class watsonBot {
	dialogMgmt dia;
	NaturalLanguageClassifier service;
	String NLPUsername;
	String NLPPassword;
	String host;
	String port;
	String classifier_name;
	String training_path;
    String classifier_id;
    String sentence;
    
    //constructor
    
	public watsonBot() throws IOException, ClassNotFoundException, SQLException{
		Properties systemProps = System.getProperties();
		readPasswordPath("C:/Users/amishr02/workspace/watsonBot/src/main/java/watsonBot/password&paths.txt");
		systemProps.put("http.proxyHost", host);
        systemProps.put("http.proxyPort", port);
        systemProps.put("https.proxyHost", host);
        systemProps.put("https.proxyPort", host);
        System.setProperties(systemProps);
        service = new NaturalLanguageClassifier();
        dia=new dialogMgmt();
        dia.getDialogList();
		setClassifierID();
		}
	
	//Read password and paths file to set variables
	
	public void readPasswordPath(String filename) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(filename));		 
		String line = null;
		while ((line = br.readLine()) != null) {
			if(line.contains("NLPUsername"))
				NLPUsername=line.split("=")[1];
			else if(line.contains("NLPPassword"))
				NLPPassword=line.split("=")[1];
			else if(line.contains("host"))
				host=line.split("=")[1];
			else if(line.contains("port"))
				port=line.split("=")[1];
			else if(line.contains("classifier_name"))
				classifier_name=line.split("=")[1];
			else if(line.contains("training_path"))
				training_path=line.split("=")[1];
		}	 
		br.close();
	}
	
	//Set username and password for NLP classifier
	
	public void setNLPUsernamePass(){
		service.setUsernameAndPassword(NLPUsername,NLPPassword );
	}
	
	//Train the classifier with your own data...returns Classifier
	
	public Classifier trainClassifier(String classifier_name, String classifier_training_path){
		setNLPUsernamePass();
		Classifier classifier = service.createClassifier(classifier_name, "en", new File(classifier_training_path));
		return classifier;
	}
	
	//To get all the trained classifier available for your account...returns Classifiers
	
	public Classifiers getClassifierList(){
		setNLPUsernamePass();
		return service.getClassifiers();
	}
	
	//To get classifier details on the basis of a classifier ID...return type is Classifier
	
	public Classifier getClassifierDetails(String classifier_id){
		setNLPUsernamePass();
		Classifier classifier = service.getClassifier(classifier_id);
		return classifier;
	}
	
	//Status of a classifier based on a classifier ID
	
	public String getClassifierStatus(String classifier_id){
		return getClassifierDetails(classifier_id).getStatus().toString();
	}	
	
	//To delete an already trained classifier based on a classifier ID
	
	public void deleteClassifier(String classifier_id){
		setNLPUsernamePass();
		try{
		if(getClassifierDetails(classifier_id)!=null)
			service.deleteClassifier(classifier_id);
		else
			System.out.println("No such classifiers to delete");
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
	
	//To set the classifier ID based on the classifier name provided in the password&paths.txt file
	
	public void setClassifierID(){
		setNLPUsernamePass();
		List<Classifier> classifiers = getClassifierList().getClassifiers();
		for (Classifier classifier : classifiers){
			if(classifier.getName().equals(classifier_name)){
				classifier_id=classifier.getId();
			}
		}
	}
	
	//To classify a particular statement using the classifier ID provided as parameter
	
	public Classification classify(String classifier_id, String statement){
		setNLPUsernamePass();
		Classification classification=null;
		if(getClassifierStatus(classifier_id).equals("AVAILABLE") && statement.length()>0 ){
		classification = service.classify(classifier_id, statement);
		}
		return classification;
	}
	
	public String passSentence(String sent) throws ClientProtocolException, IOException{
		sentence=sent;
		setClassifierID();
		Classification top_class=classify(classifier_id,sentence);
		String reply=dia.getReply(sent,top_class.getTopClass());
		System.out.println(reply);
		return reply;
	}
}
