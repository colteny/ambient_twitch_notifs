package ambient_twitch_notifs;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.json.JSONException;
import org.json.JSONObject;



public class StreamerOnlineCheck {
	
	public static void main(String[] argv) throws IOException, JSONException {

    System.out.println(checkIfOnline("Jankos"));
    System.out.println(checkIfOnline("C9Sneaky"));

}

public static boolean checkIfOnline(String channel) throws IOException, JSONException {
    String channerUrl = "https://api.twitch.tv/kraken/streams/" + channel;

    String jsonText = readFromUrl(channerUrl);// reads text from URL
    JSONObject json = new JSONObject(jsonText);

    return !json.isNull("stream");
}

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
