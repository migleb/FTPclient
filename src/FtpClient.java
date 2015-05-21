import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
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
	 * 
	 * Constructor
	 *
	 */
	public FtpClient () {
		try {
		socket = connect(ServerHostName, port);
		getGreeting();
		while (!loggedIn) {
			loggedIn = login();
		}
		sendCommand(socket,"TYPE I\n",true);
		getAction();
		socket.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * 
	 * Connecting
	 *
	 */
	public Socket connect(String ServerHostName, int port) throws UnknownHostException, IOException {
		Socket newsocket = new Socket(ServerHostName,port);
		return newsocket;
	}
	
	/*
	 * 
	 * Getting greeting from server
	 *
	 */
	public void getGreeting() throws IOException{
		getResponse(socket,true);
//		input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//
//        do {
//            line=input.readLine();
//            if(responseCode == null)
//                responseCode=line.substring(0,3);
//            System.out.println(line);
//        } while( !(line.startsWith(responseCode) && line.charAt(3) == ' '));
	}
	
	/*
	 * 
	 * Function to login
	 *
	 */
	public boolean login() throws IOException {
//		String username = "b15_16171748";
//		String password = "migleb";
		String username;
		String username_cmd;
		String password_cmd;
		int command;
		
		
		System.out.print("Username: ");
        username = scan.nextLine();
        username_cmd = "USER " + username + "\n";
        /*
         * Protecting password from echoing
         */
        Console console = System.console();
//        String fmt = "";
//      System.out.println("Password is: " + String.valueOf(password));
        command = sendCommand(socket, username_cmd, true);
        if (command >= 300 && command < 400){
        	char[] password = console.readPassword("Password: ");
        	password_cmd = "PASS " + String.valueOf(password) + "\n";
        	command = sendCommand(socket, password_cmd, true);
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
	 * 
	 * Sending command to server and getting response
	 *
	 */
	public int sendCommand (Socket mySocket, String command, boolean needPrinting) throws IOException, NullPointerException {
		int code;
		output = new BufferedWriter(new OutputStreamWriter(mySocket.getOutputStream()));
		
		output.write(command);
		output.flush();
		
		code = getResponse(mySocket, needPrinting);
        
        if (code == 421) {
        	if (command != "QUIT\n"){
        		reconnectSocket(mySocket);
        	} else System.out.println("Goodbye.\nLogout.");
        }
        
        return code;
	}
	
	/*
	 * 
	 * Reconnect socket
	 * 
	 */
	public void reconnectSocket(Socket mySocket) throws IOException {
		socket = connect(ServerHostName, port);
		System.out.println("Your server has reconnected");
		getGreeting();
		login();
		input = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
		output = new BufferedWriter(new OutputStreamWriter(mySocket.getOutputStream()));
	}
	
	
	/*
	 * 
	 * Get response
	 * 
	 */
	public int getResponse(Socket mySocket, boolean needPrinting) throws IOException{
		String line = null;
	    String responseCode = null;
	    
	    input = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
	    
	    do {
            line=input.readLine();
            if (line != null) {
            	if(responseCode == null)
	                responseCode=line.substring(0,3);
	            if (needPrinting) {
	            	System.out.println(line);
	            }
            } else break;
        } while( !(line.startsWith(responseCode) && line.charAt(3) == ' '));
        
	    return getResponseCode(responseCode);
	}

	/*
	 * 
	 * Transform code to integer
	 *
	 */
	public int getResponseCode(String responseCode){
		int code = Integer.valueOf(responseCode);
		return code;
	}
	
	/*
	 * 
	 * Change directory
	 * 
	 */
	public void changeDirectory() throws IOException {
		String dirnameCmd;
		String dirname;
		int temp;
		
		System.out.print("Go to: ");
		dirname = scan.nextLine();
		dirnameCmd = "CWD " + dirname + "\n";
		temp = sendCommand(socket, dirnameCmd, true);
		if (temp != 250) {
			System.out.println("Error occurred accessing directory /" + dirname);
		} else {
			System.out.println("Current working directory is /" + dirname);
		}
	}
	
	/*
	 * 
	 * Download file
	 * 
	 */
	public int getFile() throws IOException {
		Socket data = null;
		String fileName;
		String command;
		int code;

		if ((data = enterPassiveMode(data)) == null){
			return -1;
		}
		
//		BufferedInputStream input2 = new BufferedInputStream(data.getInputStream());
//		DataInputStream input2 = new DataInputStream(data.getInputStream());
		
		
		System.out.print("Enter file name: ");
		fileName = scan.nextLine();
		
		command = "RETR " + fileName + "\n";
		
		code = sendCommand(socket, command, true);
		
		if (code < 100 || code >= 200) {
			System.out.println("File transfer has failed");
			return -1;
		}
		
		System.out.println("Downloading...");  
		    
		
		byte[] aByte = new byte[10000];
		int bytesRead;
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataInputStream in = new DataInputStream(data.getInputStream());
		
        if (in != null) {
            FileOutputStream fos = null;
            BufferedOutputStream bos = null;
            try {
                fos = new FileOutputStream(fileName);
                bos = new BufferedOutputStream(fos);
                
                do {
                	bytesRead = in.read(aByte);
                    if (bytesRead > 0) {
                		baos.write(aByte, 0, bytesRead);
//                      bos.write(aByte);
                	}   
                } while (bytesRead != -1);

                bos.write(baos.toByteArray());
                baos.close();
                bos.flush();
                bos.close();
                fos.close();
            } catch (IOException ex) {
                
            }
        }
        
		code = getResponse(socket,true);
		
		if (code < 200 || code >= 300) {
			System.out.println("File transfer has failed");
			return -1;
		} else System.out.println("Successfully downloaded");
		
		return 1;
		
	}
	
	
	/*
	 * 
	 * 	Get list
	 * 
	 * 
	 */
	
	public int getList() throws IOException{
		String command;
		int code;
		String line = null;
	    String responseCode = null;
		Socket data = null;
		
		if( (data = enterPassiveMode(data)) == null) {
			return -1;
		}
		
		BufferedReader input2 = new BufferedReader(new InputStreamReader(data.getInputStream()));
		
		command = "LIST\n";
		
		code = sendCommand(socket,command, true);
		
		if (code < 100 || code >= 200) {
			System.out.println("List transfer have failed");
			data.close();
			return -1;
		}
		
		int got = 1;
		while (got > 0) {
			String temp = "";
			line = null;
		    responseCode = null;
			do {
				line = input2.readLine();
				if (line != null) {
					if(responseCode == null)
						responseCode = line.substring(0, 3);
					System.out.println(line);
					temp = line;
				} else break;
			} while ( !(line.startsWith(responseCode) && line.charAt(3) == ' '));
			got = temp.length();
		}
		
		line = null;
	    responseCode = null;
	    
        code = getResponse(socket,true);
        
        data.close();
        
	    if (code < 200 || code >= 300) {
	    	System.out.println("List transfer have failed");
			return -1;
	    }
	    
		return 1;
	}
	
	/*
	 * 
	 * Enter passive mode
	 * 
	 */
	public Socket enterPassiveMode(Socket data) throws IOException {
		String temp = "";
		String command = "PASV\n";
		int code;
		int next;
		
		input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
	
		int[] ip = new int[4];
		int[] port = new int[2];
		
		output.write(command);
		output.flush();
		
		String line = null;
	    String responseCode = null;
		

        do {
        	line=input.readLine();
            if (line != null) {
            	if(responseCode == null)
	                responseCode=line.substring(0,3);
            //System.out.println(line);
            temp = temp + line;
            } else break;
        } while( !(line.startsWith(responseCode) && line.charAt(3) == ' '));
        
        temp = temp.replaceAll("[^0-9]+", " ");
        
        Scanner parseScanner = new Scanner(temp);


        if (!(parseScanner.hasNextInt())) {
        	reconnectSocket(socket);
        	parseScanner.close();
        	return null;
        }
        code = parseScanner.nextInt();
        ip[0] = parseScanner.nextInt();
        ip[1] = parseScanner.nextInt();
        ip[2] = parseScanner.nextInt();
        ip[3] = parseScanner.nextInt();
        port[0] = parseScanner.nextInt();
        port[1] = parseScanner.nextInt();
        
        parseScanner.close();
        
        String fullIP = ip[0] + "." + ip[1] + "." + ip[2] + "." + ip[3];
        int fullPort = port[0] * 256 + port[1];
        
        
        if (code < 200 || code >= 300) {
        	data.close();
        	return null;
        }
        
        data = connect(fullIP,fullPort);
        
        return data;
        //System.out.println(code + " Entering Passive Mode (" + ip[0] + "," + ip[1] + "," + ip[2] + "," + ip[3] + "," + port[0] + "," + port[1] + ")");
		
	}
	
	/*
	 * 
	 * Function to print menu
	 *
	 */
	public void showMenu() {
		System.out.println("\t---MENU---");
		System.out.println("\t1 : Download file\n"
						 + "\t2 : Change working directory\n"
						 + "\t3 : Show list of files and folders\n"
						 + "\t0 : Quit (Logout)");
	}
	
	/*
	 * 
	 * Action chooser
	 * 
	 */
	public int getAction() throws IOException, NullPointerException {
		int action;
		while (true) {
			showMenu();
			System.out.print("Command: ");
			try {
				action = Integer.valueOf(scan.nextLine());
				
				switch (action) {
				
					case 1: {
						getFile();
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
						sendCommand(socket,"QUIT\n",true);				
						return 1;
					}
				
				}

			} catch (NumberFormatException e) {
				System.out.println("Invalid input");
			}
			
			
		}
	}
	
	/*
	 * 
	 * Main function
	 *
	 */
	public static void main (String[] args) {
		FtpClient client = new FtpClient();
	}
}
