package ambient_twitch_notifs;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.json.JSONException;
import org.json.JSONObject;

/*
 * Credit for code: https://stackoverflow.com/questions/21926374/checking-if-a-twitch-tv-stream-is-online-and-receive-viewer-counts-using-pircbot
 */

public class MainInterface {
	
	private static ArrayList<String> channels = new ArrayList<String>();
	private static ArrayList<JButton> buttons = new ArrayList<JButton>();
	private static Runtime rt = Runtime.getRuntime();

	
	public static void main(String[] argv) throws IOException, JSONException {
		
		//Initialize channels
		channels.add("Jankos");
		channels.add("C9Sneaky");
		channels.add("flosd");
		channels.add("Trick2g");

		
		//create and initialize frames and menus
		//Frame
		JFrame frame = new JFrame("Twitch Ambient Interface");
		frame.setLayout(new GridLayout(0, 3));
		//Menus
		JMenuBar menubar = new JMenuBar();
		JMenu menu = new JMenu("menubar");
		JMenuItem settings = new JMenuItem("Settings");
		menu.setMnemonic(KeyEvent.VK_A);
		menu.getAccessibleContext().setAccessibleDescription(
		        "test");
		menu.add(settings);
		menubar.add(menu);
		//Adding menu to frame.
		frame.add(menubar,BorderLayout.PAGE_START);
		
	  // Add a window listner for close button
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
	  });
		
		//Populate Grid by creating buttons
		for(int i=0; i<channels.size(); i++) {
			final String channelName = channels.get(i);
			JButton b1 = new JButton();
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
				b1.setBackground(Color.GREEN);
			}
			else {
				b1.setBackground(Color.RED);
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
					buttons.get(i).setBackground(Color.GREEN);
				}
				else {
					buttons.get(i).setBackground(Color.RED);
				}
			}
			//Delay before next refresh
		  try {
		    Thread.sleep(30000);                 //1000 milliseconds is one second.
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

}
