package UBC.G8.SmartSafe;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class SafeServer {
	
	
	public String appMessage = new String("app");
	public String serverMessage = new String("safe");
	
	//main function just starts up server and runs the server until the program is closed
	public static void main(String[] args) {
		try {
			System.out.println("entering main");
			SafeServer server = new SafeServer(ServerPort);
			server.serve();
		} catch (IOException e) {
			e.getMessage();
			e.printStackTrace();
		}
	}
	
	//Server fields
	public static final int ServerPort = 42060;
	private ServerSocket serverSocket;
	private InetAddress ipAddress;
	private Database database = new Database();

	//Constructor
	public SafeServer(int port) throws IOException {
		ipAddress = InetAddress.getByName("192.168.43.168");
		serverSocket = new ServerSocket(ServerPort, 10, ipAddress);
	}
	
	
	public void serve() throws IOException {
		boolean listening = true;
		
		while(listening){
			System.out.println("Waiting for client");
			new Thread(new ProcessClient(serverSocket.accept())).start();
			System.out.println("Client connected");
		}
	}
	
	class ProcessClient implements Runnable {
		private Socket socket;
		private BufferedReader input;
		private BufferedWriter output;
		
		public ProcessClient(Socket socket){
			this.socket = socket;
		}
		
		@Override
		public void run() {
			try {
				//string to store the client ID in
				String clientType;
				//block until the server receives a connection
				//create a new read buffer to read the client type
				output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				output.flush();
				input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				clientType = input.readLine();
				//System.out.println(clientType);
				//close the BufferReader since we wont use it again this method
				//input.close();
				//figure out what to do based on the response
				if (clientType.equals(appMessage))
				{
					//if its an app, run the app processing operation infinitely
					AppProcessing processing = new AppProcessing(database, this.socket);
					processing.process();
				}
				else if (clientType.equals(serverMessage))
				{
					//if its a safe, runt he safe processing operation infinitely,
					SafeProcessing  ProcessingSafe = new SafeProcessing(database, socket, input, output);
					ProcessingSafe.processSafe();
				}
				//*Note, processSafe and process do not return under normal conditions
				
			} catch (IOException e) {
				return;
			} 
		}
	}
	
}
