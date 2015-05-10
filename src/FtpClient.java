import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;


public class FtpClient {
	String ServerHostName = "185.27.134.11";
	int port = 21;
	BufferedReader reader = null;
	Socket socket = null;
	
	
	public FtpClient () {
		try {
		establishClient(ServerHostName, port);
		getGreeting();
		FtpLogin login = new FtpLogin(socket);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main (String[] args) {
		FtpClient client = new FtpClient();
	}
	
	public void establishClient(String ServerHostName, int port) throws UnknownHostException, IOException {
		socket = new Socket(ServerHostName,port);
		System.out.println("Connecting to server...");
	}

	
	public void getGreeting() throws IOException{
		
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String line = null;
        String responseCode = null;

        do {
            line=reader.readLine();
            if(responseCode == null)
                responseCode=line.substring(0,3);
            System.out.println(line);
        } while( !(line.startsWith(responseCode) && line.charAt(3) == ' '));
		
	}
}
