package theDialogMgmt;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.http.client.ClientProtocolException;

import com.ibm.watson.developer_cloud.dialog.v1.DialogService;
import com.ibm.watson.developer_cloud.dialog.v1.model.Conversation;
import com.ibm.watson.developer_cloud.dialog.v1.model.Dialog;

import databaseConnectivity.database;

public class dialogMgmt {
	DialogService service;
	String DialogUsername;
	String DialogPassword;
	String host;
	String port;
	String dialog_name;
	String dialog_path;
	String dialog_id;
	static String sentence;
	static int client_id=0;
	static int conversation_id=0;
	static int count=0;
	database db;

	//constructor

	public dialogMgmt() throws IOException, ClassNotFoundException, SQLException{
		Properties systemProps = System.getProperties();
		readPasswordPath("C:/Users/amishr02/workspace/watsonBot/src/main/java/watsonBot/password&paths.txt");
		systemProps.put("http.proxyHost", host);
		systemProps.put("http.proxyPort", port);
		systemProps.put("https.proxyHost", host);
		systemProps.put("https.proxyPort", host);
		System.setProperties(systemProps);
		service = new DialogService();
		db=new database();
	}

	//Sets up environment variables and paths for Dialog Service.

	public void readPasswordPath(String filename) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(filename));		 
		String line = null;
		while ((line = br.readLine()) != null) {
			if(line.contains("DialogUsername"))
				DialogUsername=line.split("=")[1];
			else if(line.contains("DialogPassword"))
				DialogPassword=line.split("=")[1];
			else if(line.contains("host"))
				host=line.split("=")[1];
			else if(line.contains("port"))
				port=line.split("=")[1];
			else if(line.contains("dialog_path"))
				dialog_path=line.split("=")[1];
			else if(line.contains("dialog_name"))
				dialog_name=line.split("=")[1];
		}	 
		br.close();
	}

	//Set username and password for Dialog Trainer

	public void setDialogUsernamePass(){
		service.setUsernameAndPassword(DialogUsername,DialogPassword );
	}

	//Gets list of dialogs available for your account...return type is List	

	public void getDialogList(){
		setDialogUsernamePass();
		List<Dialog> dialogs = service.getDialogs();
		for (Dialog dialog : dialogs){
			if(dialog.getName().equals(dialog_name)){
				dialog_id=dialog.getId();
			}
		}
	}

	//Create Dialog flow...input is xml file.

	public void createDialog(){
		setDialogUsernamePass();
		Dialog dialog = service.createDialog(dialog_name, new File(dialog_path));
		//System.out.println(dialog);			
	}

	//Delete a particular dialog flow.

	public void deleteDialog(){
		setDialogUsernamePass();
		service.deleteDialog(dialog_id);
	}

	//Update a dialog flow

	public void updateDialog(){
		setDialogUsernamePass();
		Dialog dialog = service.updateDialog(dialog_id, new File(dialog_path));
		//System.out.println(dialog);
	}

	//Converse using a particular Dialog flow.

	public Conversation dialogConverse(String sentence){
		setDialogUsernamePass();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(DialogService.DIALOG_ID,dialog_id);
		if (client_id!=0)
			params.put(DialogService.CLIENT_ID,client_id);
		params.put(DialogService.INPUT, sentence);
		if(conversation_id!=0)
			params.put(DialogService.CONVERSATION_ID,conversation_id);
		Conversation conversation = service.converse(params);
		return conversation;
	}

	//Get reply to users comment.

	public String getReply(String sentence, String top_class) throws ClientProtocolException, IOException{
		Conversation reply;
		if(client_id==0){
			reply=dialogConverse(sentence);
			count++;
		}
		else if(count==1){
			updateUsername(sentence);
			//powershellScripts.scripts();
			reply=dialogConverse(sentence);
			count++;
		}
		else{
			updateProfile(top_class);
			if (sentence.toLowerCase().contains("available categories"))
				updateCategory();
			else if(sentence.toLowerCase().contains("software")||sentence.toLowerCase().contains("hardware"))
				updateSubCategory(sentence.toLowerCase());
			reply=dialogConverse(sentence);
		}
		client_id=reply.getClientId();
		conversation_id=reply.getId();
		sentence=reply.getResponse().toString();
		
		return sentence;
	}

	//update profile variables.

	public void updateProfile(String top_class) throws ClientProtocolException, IOException{
		setDialogUsernamePass();
		Map<String, String> profile = new HashMap<String,String>();
		profile.put("Intent", top_class);
		service.updateProfile(dialog_id, client_id, profile);
	}
	
	//Update Username
	
	public void updateUsername(String sent){
		setDialogUsernamePass();
		//updateDialog();
		Map<String, String> profile = new HashMap<String,String>();
		profile.put("Username", sent);
		service.updateProfile(dialog_id, client_id, profile);
	}
	
	//Update categories
	
	public void updateCategory(){
		setDialogUsernamePass();
		//updateDialog();
		StringBuilder sb=new StringBuilder();
		Map<String, String> profile = new HashMap<String,String>();
		ArrayList<String> temp=db.getCategory();
		for(int i=1; i<=temp.size();i++){
			sb.append(i +"--->" +temp.get(i-1)+"\n\t\t");
		}
		profile.put("Category", sb.toString());
		service.updateProfile(dialog_id, client_id, profile);
	}

	//Update sub category profile variable
	
	public void updateSubCategory(String sentence){
		setDialogUsernamePass();
		//updateDialog();
		String subCategory="";
		if(sentence.contains("hardware"))
			subCategory="hardware";
		else
			subCategory="software";		
		StringBuilder sb=new StringBuilder();
		Map<String, String> profile = new HashMap<String,String>();
		ArrayList<String> temp=db.getSubCategory(subCategory);
		for(int i=1; i<=temp.size();i++){
			sb.append(i +"--->" +temp.get(i-1)+"\n\t\t");
		}
		profile.put("Sub_category", sb.toString());
		service.updateProfile(dialog_id, client_id, profile);
	}
	
	//get profile variables

	public void getProfile(){
		//System.out.println(service.getProfile(dialog_id,client_id));
	}


	/*public static void main(String args[]) throws IOException, ClassNotFoundException, SQLException{
			dialogMgmt dia=new dialogMgmt();
			dia.getDialogList();
			dia.updateDialog();}*/

		
}
