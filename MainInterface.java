package ambient_twitch_notifs;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

import sun.audio.*;

import javax.swing.*;
import javax.sound.sampled.*;

import org.json.JSONException;
import org.json.JSONObject;

/*
 * Credit for checkOnline & readFromURL method code: https://stackoverflow.com/questions/21926374/checking-if-a-twitch-tv-stream-is-online-and-receive-viewer-counts-using-pircbot
 */

public class MainInterface {
	
	private static ArrayList<String> channels = new ArrayList<String>();
	private static ArrayList<JButton> buttons = new ArrayList<JButton>();
	private static ArrayList<Integer> alreadyOnline = new ArrayList<Integer>();
	private static Runtime rt = Runtime.getRuntime();
	

	
	public static void main(String[] argv) throws IOException, JSONException {
		
		//Initialize channels
		channels.add("Jankos");
		channels.add("C9Sneaky");
		channels.add("flosd");
		channels.add("Trick2g");
		
	  Color startColor = new Color(75,225,225);
	  Color endColor = new Color(225,75,75);
	  int updateTime = 10;

		
		//create and initialize frames and menus
		//Frame
		JFrame frame = new JFrame("Twitch Ambient Interface");
		frame.setLayout(new GridLayout(0, 3));
		JButton menu = new JButton("Settings");
		menu.addActionListener(new ActionListener()
			{				
				public void actionPerformed(ActionEvent e) {
					JFrame frame = new JFrame("Settings");
					JPanel list = new JPanel();
					
					list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
          
          JLabel ref = new JLabel("Refresh Time Cycle: " + updateTime + " sec");	//updateTime local variable, asking for 'final'
          JTextField refreshTime = new JTextField(2);
          JButton changeTime = new JButton("Accept");
          changeTime.addActionListener(new ActionListener()
          		{
        	  		public void actionPerformed(ActionEvent e) {
        	  			updateTime = Integer.parseInt(refreshTime.getText());	//updateTime local variable, asking for 'final'
        	  		}
        		});
					list.add(ref);
					list.add(refreshTime);
					list.add(changeTime);
					list.add(Box.createRigidArea(new Dimension(0,7)));
					
					JLabel sn = new JLabel("Add new Streamer");
          JTextField streamer = new JTextField(15);
          JButton addStream = new JButton("Add");
          list.add(sn);
					list.add(streamer);
					list.add(addStream);
					
					frame.add(list);
					frame.pack();
					frame.setVisible(true);
				}			
			}
		);
		menu.setPreferredSize(new Dimension(85, 85));
    buttons.add(menu);
		
	  // Add a window listener for close button
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
	  });
		
		//Populate Grid by creating buttons
		for(int i=0; i<channels.size(); i++) {
			final String channelName = channels.get(i);
			JButton b1 = new JButton(channelName);
			//testing to see if color change and sound works.
			//alreadyOnline.add(0);
			b1.addActionListener(new ActionListener()
      {
      	public void actionPerformed(ActionEvent e)
      	{
      		String url = "http://www.twitch.tv/" + channelName;
      		String os = System.getProperty("os.name").toLowerCase();
      		
      		if (os.indexOf( "win" ) >= 0) {
      		  try {
							rt.exec( "rundll32 url.dll,FileProtocolHandler " + url);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
      		}
      		else if(os.indexOf( "mac" ) >= 0) {
      			try {
							rt.exec( "open " + url);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
      		}
      	}
      });
			b1.setToolTipText(channels.get(i));
			if(checkIfOnline(channels.get(i))) {
				alreadyOnline.add(1);
				b1.setBackground(startColor);
			}
			else {
				alreadyOnline.add(0);
				b1.setBackground(endColor);
			}
      b1.setPreferredSize(new Dimension(85, 85));
      buttons.add(b1);
		}
		//add buttons
		for(int i = 0;i < buttons.size();i++) {
			frame.add(buttons.get(i));
		}
		//create frame
		frame.pack();
		frame.setVisible(true);
		
		//update button colors
		while(true) {
			for(int i=0; i<channels.size(); i++) {
				if(checkIfOnline(channels.get(i))) {
					//checks if the person is already set to Online
					if(alreadyOnline.get(i)!=1) {
					//Plays sound when someone comes online
						alreadyOnline.set(i, 1);
						try {
							playAudio();
						} catch (IOException e2) {
							// TODO Auto-generated catch block
							e2.printStackTrace();
						}
						//for loop changes slowly changes the color of button when the streamer becomes online
						for(int q = 0; q < 150; q++) {
							endColor = new Color (endColor.getRed()- 1, endColor.getGreen() +1, endColor.getBlue() +1);
							buttons.get(i).setBackground(endColor);
							try {
								Thread.sleep(7);
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					  startColor = new Color(75,225,225);
					  endColor = new Color(225,75,75);
					}
				}
				else {
					if(alreadyOnline.get(i) != 0) {
						//checks if the person is already set to Offline
						alreadyOnline.set(i, 0);
						//for loop changes slowly changes the color of button when the streamer becomes offline
						for(int q = 0; q < 150; q++) {
							startColor = new Color (startColor.getRed()+ 1, startColor.getGreen() -1, startColor.getBlue() -1);
							buttons.get(i).setBackground(startColor);
							try {
								Thread.sleep(7);
							} 	catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					  startColor = new Color(75,225,225);
					  endColor = new Color(225,75,75);
					}
				}
			}
			//Delay before next refresh
		  try {
		    Thread.sleep(updateTime * 1000);                 //1000 milliseconds is one second.
		  } 
		  catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		  }
		}

}

//Checks if a Stream is online
public static boolean checkIfOnline(String channel) throws IOException, JSONException {
    String channerUrl = "https://api.twitch.tv/kraken/streams/" + channel;

    String jsonText = readFromUrl(channerUrl);// reads text from URL
    JSONObject json = new JSONObject(jsonText);

    return !json.isNull("stream");
}

//method to help read URL into JSON object
private static String readFromUrl(String url) throws IOException {
  URL page = new URL(url);
  StringBuilder sb = new StringBuilder();
  Scanner scanner = null;
  try{
      scanner = new Scanner(page.openStream(), StandardCharsets.UTF_8.name());
      while (scanner.hasNextLine()){
          sb.append(scanner.nextLine());
      }
  }finally{
      if (scanner!=null)
          scanner.close();
  }
  return sb.toString();
}

public static void playAudio() throws IOException {
	URL path = MainInterface.class.getResource("sound1.wav");
	File soundFile = new File(path.getFile());
	try {
		AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
		Clip clip = AudioSystem.getClip();
		
		clip.open(audioIn);
		clip.start();
	} 
	catch (UnsupportedAudioFileException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (LineUnavailableException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}	
}


}
