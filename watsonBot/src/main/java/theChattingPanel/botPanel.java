package theChattingPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

import sentiment.analyzeSentiment;
import theDialogMgmt.dialogMgmt;
import watsonBot.watsonBot;
public class botPanel extends JFrame implements KeyListener{
	String quote;
	watsonBot bot;
	dialogMgmt dia;
	StringBuilder sb=new StringBuilder();
	JPanel p=new JPanel(new BorderLayout());
	JTextPane dialog=new JTextPane();
	JTextArea input=new JTextArea(3,50);
	JScrollPane scroll=new JScrollPane(
		dialog,
		JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
		JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
	);
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, ClassNotFoundException, SQLException{
		new botPanel();
	}
	
	public botPanel() throws IOException, ClassNotFoundException, SQLException{
		super("AmBot");
		setSize(600,400);
		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		dialog.setEditable(false);
		dialog.setContentType("text/html");
		input.addKeyListener(this);
	
		p.add(scroll,BorderLayout.CENTER);
		p.add(input,BorderLayout.SOUTH);
		p.setBackground(new Color(255,200,0));
		add(p);		
		setVisible(true);
		bot=new watsonBot();
	}
	
	public void keyPressed(KeyEvent e){
		if(e.getKeyCode()==KeyEvent.VK_ENTER){
			//String output_array[]=null;
			input.setEditable(false);
			quote=input.getText();
			input.setText("");
			sb.append("<font size='5'>-->You:&nbsp;&nbsp;"+quote+"</font><br>");
			dialog.setText(sb.toString());
			if(quote.length()==0)
				quote="null";			
			String reply="";
			try {
				reply = bot.passSentence(quote);
			} catch (ClientProtocolException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			reply=reply.substring(1, reply.length()-1);
			if(reply.toLowerCase().equals("solution")){
				System.out.println("Hi");
				sb.append("<font size='5'>-->Bot:&nbsp;&nbsp;&nbsp;"+"<a href=\"C:/Users/amishr02/Desktop/test.hta\">Download Fix</a>"+"</font><br>Download the fix and let me know if the issue is fixed.<br>");
				dialog.setText(sb.toString());
				dialog.addHyperlinkListener(new HyperlinkListener() {
				    public void hyperlinkUpdate(HyperlinkEvent e) {
				        if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				           System.out.println(e.getDescription());
				           File f=new File(e.getDescription());
				           try {
							URL url=new URL(f.toURL().toString());
							sun.net.www.protocol.file.FileURLConnection connection = null;
							try {
								connection = (sun.net.www.protocol.file.FileURLConnection) url.openConnection();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							int filesize = connection.getContentLength(); 
							float totalDataRead=0;
							java.io.BufferedInputStream in = new java.io.BufferedInputStream(connection.getInputStream());
							java.io.FileOutputStream fos = new java.io.FileOutputStream("test.hta");
							java.io.BufferedOutputStream bout = new BufferedOutputStream(fos,1024); 
							byte[] data = new byte[1024]; 
							int i=0; 
							while((i=in.read(data,0,1024))>=0) { 
								totalDataRead=totalDataRead+i; 
								bout.write(data,0,i);  }
							bout.close(); 
							in.close();
							String[] cmd = {"C:\\WINDOWS\\system32\\cmd.exe","/c","start","test.hta"};
							Runtime.getRuntime().exec(cmd);
						} catch (MalformedURLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
				        }
				    }
				});
				}
			else {
				sb.append("<font size='5'>-->Bot:&nbsp;&nbsp;&nbsp;"+reply+"</font><br>");
				dialog.setText(sb.toString());}
		}
	}
	
	public String sentenceReceived(){
		String sent=quote;
		return sent;
	}
	
	public void keyReleased(KeyEvent e){
		if(e.getKeyCode()==KeyEvent.VK_ENTER){
			input.setEditable(true);
		}
	}
	
	public void keyTyped(KeyEvent e){}
	
	public void addText(String str){
		dialog.setText(dialog.getText()+str);
	}
}