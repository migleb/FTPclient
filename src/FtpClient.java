import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;


public class FtpClient {
	String ServerHostName = "185.27.134.11";
	int port = 21;
	BufferedReader input;
	BufferedWriter output;
	Scanner scan = new Scanner(System.in);
	Socket socket = null;
	Socket datasocket = null;
	String line = null;
    String responseCode = null;
    boolean loggedIn = false;
	
	/*
	 * Constructor
	 */
	public FtpClient () {
		try {
		socket = connect(ServerHostName, port);
		getGreeting();
		while (!loggedIn) {
			loggedIn = login();
		}
		getAction();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * Connecting
	 */
	public Socket connect(String ServerHostName, int port) throws UnknownHostException, IOException {
		Socket newsocket = new Socket(ServerHostName,port);
		return newsocket;
	}
	
	
	
	/*
	 * Getting greeting from server
	 */
	public void getGreeting() throws IOException{
		input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        do {
            line=input.readLine();
            if(responseCode == null)
                responseCode=line.substring(0,3);
            System.out.println(line);
        } while( !(line.startsWith(responseCode) && line.charAt(3) == ' '));
	}
	
	/*
	 * Function to login
	 */
	public boolean login() throws IOException {
		String username = "b15_16171748";
		String password = "migleb";
//		String username;
		String username_cmd;
		String password_cmd;
		int command;
		
		input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
//		System.out.print("Username: ");
//        username = scan.nextLine();
        username_cmd = "USER " + username + "\n";
        /*
         * Protecting password from echoing
         */
//        Console console = System.console();
//        String fmt = "";
//        char[] password = console.readPassword("Password: ");
//        System.out.println("Password is: " + String.valueOf(password));
        password_cmd = "PASS " + String.valueOf(password) + "\n";
        command = sendCommand(username_cmd);
        if (command >= 300 && command < 400){
        	command = sendCommand(password_cmd);
        	if (command < 200 || command >= 300){
        		System.out.println("Username and/or password is invalid.");
        		return false;
        	} else {
        		System.out.println("You are now connected!");
        		return true;
        	}
        } else {
        	System.out.println("Username is invalid.");
        	return false;
        }
	}
	
	/*
	 * Sending command to server and getting response
	 */
	public int sendCommand (String command) throws IOException, NullPointerException {
		int code;
		
		output.write(command);
		output.flush();
		
		String line = null;
	    String responseCode = null;
		

        do {
            line=input.readLine();
            if(responseCode == null)
                responseCode=line.substring(0,3);
            System.out.println(line);
        } while( !(line.startsWith(responseCode) && line.charAt(3) == ' '));
        
        code = parseRespondCode(responseCode);
        
        if (code == 421) {
        	socket = connect(ServerHostName, port);
			System.out.println("Your server has reconnected");
			getGreeting();
			login();
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        }
        
        return code;
	}

	/*
	 * Transform code to integer
	 */
	public int parseRespondCode(String responseCode){
		int code = Integer.valueOf(responseCode);
		return code;
	}
	
	public void changeDirectory() throws IOException {
		String dirname_cmd;
		String dirname;
		int temp;
		
		System.out.print("Go to: ");
		dirname = scan.nextLine();
		dirname_cmd = "CWD " + dirname + "\n";
		temp = sendCommand(dirname_cmd);
		if (temp != 250) {
			System.out.println("Error occurred accessing directory " + dirname);
		} else {
			System.out.println("Current working directory is " + dirname);
		}
	}
	
	public int getList() throws IOException{
		String command;
		int code;
		String line = null;
	    String responseCode = null;
		Socket data = new Socket();
		
		
		data = enterPassiveMode(data);
		
		BufferedReader input2 = new BufferedReader(new InputStreamReader(data.getInputStream()));
		BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		
		command = "LIST\n";
		
		output.write(command);
		output.flush();

        do {
            line=input.readLine();
            if(responseCode == null)
                responseCode=line.substring(0,3);
            System.out.println(line);
        } while( !(line.startsWith(responseCode) && line.charAt(3) == ' '));
        code = Integer.valueOf(responseCode);
        
		if (code < 100 || code >= 200) {
			System.out.println("List transfer have failed");
			return -1;
		}
		
		
		int got = 1;
		while (got > 0) {
			String temp = "";
			do {
	            line=input2.readLine();
	            if (line != null){
	            	if(responseCode == null)
		                responseCode=line.substring(0,3);
		            System.out.println(line);
		            temp = line;
	            } else break;
	        } while( !(line.startsWith(responseCode) && line.charAt(3) == ' '));
			got = temp.length();
		}

		line = null;
	    responseCode = null;
	    
        do {
            line=input.readLine();
            if(responseCode == null)
                responseCode=line.substring(0,3);
            System.out.println(line);
        } while( !(line.startsWith(responseCode) && line.charAt(3) == ' '));
	        
	    code = Integer.valueOf(responseCode);
	    if (code < 200 || code >= 300) {
	    	System.out.println("List transfer have failed");
			return -1;
	    }
	    
		return 1;
	}
	
	public Socket enterPassiveMode(Socket data) throws IOException {
		String temp = "";
		String command = "PASV\n";
		int code;
	
		int[] ip = new int[4];
		int[] port = new int[2];
		
		output.write(command);
		output.flush();
		
		String line = null;
	    String responseCode = null;
		

        do {
            line=input.readLine();
            //if(responseCode == null)
                responseCode=line.substring(0,3);
            System.out.println(line);
            temp = temp + line;
        } while( !(line.startsWith(responseCode) && line.charAt(3) == ' '));
        
        temp = temp.replaceAll("[^0-9]+", " ");
        
        Scanner parseScanner = new Scanner(temp);
        
        code = parseScanner.nextInt();
        ip[0] = parseScanner.nextInt();
        ip[1] = parseScanner.nextInt();
        ip[2] = parseScanner.nextInt();
        ip[3] = parseScanner.nextInt();
        port[0] = parseScanner.nextInt();
        port[1] = parseScanner.nextInt();
        
        String fullIP = ip[0] + "." + ip[1] + "." + ip[2] + "." + ip[3];
        int fullPort = port[0] * 256 + port[1];
        
        
        if (code < 200 || code >= 300) {
        	return null;
        }
        
        data = connect(fullIP,fullPort);
        
        return data;
        //System.out.println(code + " Entering Passive Mode (" + ip[0] + "," + ip[1] + "," + ip[2] + "," + ip[3] + "," + port[0] + "," + port[1] + ")");
		
	}
	
	/*
	 * Function to print menu
	 */
	public void showMenu() {
		System.out.println("\t---MENU---");
		System.out.println("\t1 : Download file\n\t2 : Change working directory\n\t3 : Show list of files and folders\n\t0 : Quit (Logout)");
	}
	
	public int getAction() throws IOException, NullPointerException {
		int action;
		while (true) {
			showMenu();
			System.out.print("Command: ");
			try {
				action = Integer.valueOf(scan.nextLine());
				
				switch (action) {
				
					case 1: {
						break;
					}
					
					case 2: {
						changeDirectory();
						break;
					}
					
					case 3: {
						getList();
						break;
					}
					
					case 0: {
						String line = null;
					    String responseCode = null;
						output.write("QUIT\n");
						output.flush();
						
						do {
				            line=input.readLine();
				            if(responseCode == null)
				                responseCode=line.substring(0,3);
				            System.out.println(line);
				        } while( !(line.startsWith(responseCode) && line.charAt(3) == ' '));
						
						return 1;
					}
				
				}

			} catch (NumberFormatException e) {
				System.out.println("Invalid input");
			}
			
			
		}
	}
	
	/*
	 * Main function
	 */
	public static void main (String[] args) {
		FtpClient client = new FtpClient();
	}
}
