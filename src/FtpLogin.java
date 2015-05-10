import java.io.*;
import java.net.Socket;
import java.util.*;


public class FtpLogin {
	BufferedReader input;
	BufferedWriter output;
	PrintWriter pw;
	String username = "b15_16171748";
	String password = "migleb";
	String username_cmd;
	String password_cmd;
	int temp;
	
	public FtpLogin(Socket socket) throws IOException {
		input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
//		System.out.print("Username: ");
//		Scanner scan = new Scanner(System.in);
//        username = scan.nextLine();
        username_cmd = "USER " + username + "\n";
//        Console console = System.console();
//        String fmt = "";
//        char[] password = console.readPassword("Password: ");
//        System.out.println("Password is: " + String.valueOf(password));
        password_cmd = "PASS " + String.valueOf(password) + "\n";
        temp = sendCommand(username_cmd);
        if (temp >= 300 && temp < 400){
        	temp = sendCommand(password_cmd);
        	if (temp < 200 && temp >= 300){
        		System.out.println("Username and/or password is invalid.");
        	} else System.out.println("You are now connected!");
        } else System.out.println("Username is invalid.");
	}
	
	public int sendCommand (String command) throws IOException {
		output.write(command);
		output.flush();
		
		String line = null;
        String responseCode = null;

        do {
            line=input.readLine();
            if(responseCode == null)
                responseCode=line.substring(0,3);
            //System.out.println(line);
        } while( !(line.startsWith(responseCode) && line.charAt(3) == ' '));
        int temp = parseRespondCode(responseCode);
        return temp;
	}
	
	public int parseRespondCode(String responseCode){
		int code = Integer.valueOf(responseCode);
		return code;
	}

}
