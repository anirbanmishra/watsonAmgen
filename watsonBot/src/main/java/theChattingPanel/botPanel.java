package theChattingPanel;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.SQLException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

import theDialogMgmt.dialogMgmt;
import watsonBot.watsonBot;

public class botPanel extends JFrame implements KeyListener{
	String quote;
	watsonBot bot;
	dialogMgmt dia;
	JPanel p=new JPanel(null);
	JTextArea dialog=new JTextArea(20,48);
	JTextArea input=new JTextArea(2,48);
	JScrollPane scroll=new JScrollPane(
			dialog,
			JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
		);
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, ClassNotFoundException, SQLException{
		new botPanel();
	}
	
	public botPanel() throws IOException, ClassNotFoundException, SQLException{
		super("AmBot");
		setSize(550,600);
		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		DefaultCaret caret = (DefaultCaret)dialog.getCaret();
	    caret.setUpdatePolicy(DefaultCaret.OUT_BOTTOM);		
		dialog.setEditable(false);
		input.addKeyListener(this);
		BufferedImage img = ImageIO.read(botPanel.class.getClassLoader().getResourceAsStream("images.png"));
        JLabel label = new JLabel(new ImageIcon(img));
		label.setBounds(0,400, 200, 200);
		scroll.setBounds(530,0,16,400);
		dialog.setBounds(0, 0,530, 397 );
		input.setBounds(200,400,340,600);
		p.add(scroll);
		p.add(dialog);
		p.add(input);
		p.setBackground(new Color(100,80,10));
		p.add(label);
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
			addText("-->You:\t"+quote+"\n");
			String reply="";
			try {
				reply = bot.passSentence(quote);
			} catch (ClientProtocolException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			reply=reply.substring(1, reply.length()-1);
			addText("-->Bot\t"+reply+"\n");
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