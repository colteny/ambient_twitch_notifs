package ambient_twitch_notifs;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

import sun.audio.*;

import javax.swing.*;
import javax.sound.sampled.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;

import javax.swing.text.NumberFormatter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
/*
 * Credit for checkOnline & readFromURL method code: https://stackoverflow.com/questions/21926374/checking-if-a-twitch-tv-stream-is-online-and-receive-viewer-counts-using-pircbot
 */

public class MainInterface {

	private static ArrayList<String> channels = new ArrayList<String>();
	private static ArrayList<JButton> buttons = new ArrayList<JButton>();
	private static ArrayList<Integer> alreadyOnline = new ArrayList<Integer>();
	private static Runtime rt = Runtime.getRuntime();
	private static int updateTime = 10;
	private static String currentDirectory = new File("").getAbsolutePath();



	public static void main(String[] argv) throws IOException, JSONException {

		String  thisLine = null;
		try{
			// initial settings
			//System.out.println(currentDirectory);
			BufferedReader br = new BufferedReader(new FileReader(new File(currentDirectory + "\\src\\ambient_twitch_notifs\\settings.txt")));
			thisLine = br.readLine();
			if(!isInteger(thisLine) || thisLine == null) {
				updateTime = 10;
				//if rest of file is empty
				if((thisLine = br.readLine()) == null) {
					br.close();
					System.out.println("closed");
					return;
				}
			}
			else {
				updateTime = Integer.parseInt(thisLine);
				thisLine = br.readLine();

			}
			System.out.println(updateTime);
			
			while (thisLine != null) {
				channels = getFollows(thisLine);
				System.out.println(thisLine);
				thisLine = br.readLine();
			}

			br.close();
			System.out.println("closed");

		}catch(Exception e){
			e.printStackTrace();
		}

		//Initialize channels
		
		
	  Color startColor = new Color(75,225,225);
	  Color endColor = new Color(225,75,75);

		//create and initialize frames and menus
		//Frame
		JFrame frame = new JFrame("Twitch Ambient Interface");
		frame.setLayout(new GridLayout(0, 3));
		JButton menu = new JButton("Settings");
		menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFrame frame = new JFrame("Settings");
			    JPanel list = new JPanel();

									   list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

									   NumberFormat intFormat = NumberFormat.getIntegerInstance();
									   NumberFormatter numberFormatter = new NumberFormatter(intFormat);
									   numberFormatter.setValueClass(Integer.class); //ensures integer value
									   numberFormatter.setAllowsInvalid(false); //never allows anything else
									   numberFormatter.setMinimum(0); //lowest value
									   numberFormatter.setMaximum(Integer.MAX_VALUE); //max value of an integer, change it if you like

									   JLabel ref = new JLabel("Refresh Time Cycle:  (in seconds)");
									   final JTextField refreshTime = new JFormattedTextField(numberFormatter);
									   JButton changeTime = new JButton("Accept");
									   changeTime.addActionListener(new ActionListener() {
										   public void actionPerformed(ActionEvent e) {
											   updateTime = Integer.parseInt(refreshTime.getText());
											   System.out.println(updateTime);
										   }
									   });
									   list.add(ref);
									   list.add(refreshTime);
									   list.add(changeTime);
									   list.add(Box.createRigidArea(new Dimension(0, 7)));

									   JLabel sn = new JLabel("Username:");
									   final JTextField user = new JTextField(15);
									   JButton addUser = new JButton("Add");
									   addUser.addActionListener(new ActionListener() {
																	   public void actionPerformed(ActionEvent e) {
																		   try {
																			   if (getResponseCode("http://www.twitch.tv/" + user.getText()) != 404) {
																				   channels.set(0, user.getText());
																			   }
																		   }
																		   catch (IOException ioe) {
																			   ioe.printStackTrace();
																		   }

																	   }
																   }
									   );

									   list.add(sn);
									   list.add(user);
									   list.add(addUser);


									   JLabel ms = new JLabel("Messages");
									   //messages if anything goes wrong in adding someone
									   final JTextArea messages = new JTextArea(1, 20);
									   messages.setEditable(false);

									   list.add(ms);
									   list.add(messages);

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
			alreadyOnline.add(0);
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
				//alreadyOnline.add(1);
				b1.setBackground(startColor);
			}
			else {
				//alreadyOnline.add(0);
				b1.setBackground(endColor);
			}
			b1.setPreferredSize(new Dimension(125, 50));
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
							buttons.get(i+1).setBackground(endColor);
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
							buttons.get(i+1).setBackground(startColor);
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

public static ArrayList<String> getFollows(String user) throws IOException, JSONException {
	String userUrl = "https://api.twitch.tv/kraken/users/" + user + "/follows/channels";
	
	ArrayList<String> streamers = new ArrayList<String>();

	//parses JSONArray to get the name of the streamer
  String jsonText = readFromUrl(userUrl);// reads text from URL
  JSONObject json = new JSONObject(jsonText);
  //total number of people following
  int total = (int) json.get("_total");
  //System.out.println(total);
  //System.out.println(json.toString());
  
  if(total > 25) {
  	total = 25;
  }
  
//parses JSONArray to get the name of the streamer then adds them to an array
  for(int i = total-1; i >= 0;i--) {

  	JSONArray arr1 = (JSONArray) json.getJSONArray("follows");
    JSONObject obj1 = (JSONObject) arr1.get(i);
    JSONObject obj2 = (JSONObject) obj1.getJSONObject("channel");
    String obj3 = (String) obj2.get("name");
    

    streamers.add(obj3);
  }
  

  return streamers;
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
		
		File soundFile = new File(currentDirectory + "\\src\\ambient_twitch_notifs\\sound1.wav");
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

	public static int getResponseCode(String urlString) throws MalformedURLException, IOException {
		URL u = new URL(urlString);
		HttpURLConnection huc =  (HttpURLConnection)  u.openConnection();
		huc.setRequestMethod("GET");
		huc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)");
		huc.connect();
		return huc.getResponseCode();
	}

	public static boolean isInteger( String input ) {
		try {
			Integer.parseInt( input );
			return true;
		}
		catch( Exception e ) {
			return false;
		}
	}

}
