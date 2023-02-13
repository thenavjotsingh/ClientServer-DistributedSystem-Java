package clientserver;

import java.io.*;
import java.net.*;
import java.util.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.CompletableFuture;
import java.util.Arrays;
import java.util.List;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;


// Client class
class Client {

		private static String serverDir = "/Users/maitripatel/Downloads/distributed_assignment_1/ClientServerDistributedApplication/ServerFileStrcture/";
		private static String clientDir = "/Users/maitripatel/Downloads/distributed_assignment_1/ClientServerDistributedApplication/ClientFileStrcture/";


		// public void syncFiles(clientDir, serverDir) {

		// 	for (File filepath : clientDir.listFiles()) {
        //     // checks if give path is a file or a directory
        //     if (filepath.isFile())
        //         // adds the byte size of a file
        //         System.out.println(filepath.lastModified());
        //     else
        //         // makes a recursive call to calculate the byte size of the file
           
		// }
	
	// driver code
	public static void main(String[] args)
	{
		// establish a connection by providing host and port number
		try (Socket socket = new Socket("localhost", 1234)) {
			Registry registry = LocateRegistry.getRegistry("localhost");
					Hello stubForAutoUpload = (Hello) registry.lookup("Hello");

			{
                Timer timer = new Timer();
                timer.schedule( new TimerTask()
                {
                    public void run()  {
                        try {
                            stubForAutoUpload.uploadAuto();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, 0, 60*(1000*1)); //after every 60 seconds
            }

			PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			Scanner scan = new Scanner(System.in);
			String operation = "";
			String delimiter = " ";

			while (!"exit".equalsIgnoreCase(operation)) {
				System.out.println("What operation would you like to perform? & please provide the file name(inputs are separated by space)");
				operation = scan.nextLine().trim();
				while(operation.isEmpty()){
					System.out.println("What operation would you like to perform? & please provide the file name(inputs are separated by space)");
					operation = scan.nextLine().trim();
					System.out.println("New operation is: " + operation + ";");
				}				
				String actualOperation = operation.split(delimiter)[0];
				if(actualOperation.equalsIgnoreCase("syncRPC")) {				
					//Registry registry = LocateRegistry.getRegistry("localhost");
					Hello stub = (Hello) registry.lookup("Hello");

					System.out.println("Do you want perform sum or sort?");
					String arithmaticOperation = scan.nextLine();
					if (arithmaticOperation.equalsIgnoreCase("sum")) {
						System.out.println("Enter two number on which you would like to perform summmation (separated by space)");
						int num1 = scan.nextInt();
						int num2 = scan.nextInt();
						int response = stub.add(num1, num2);
						System.out.println(operation + "operation has been performed successfully, syncRPC response: " + response);
						// out.println(actualOperation + response);
					} 
					else if (arithmaticOperation.equalsIgnoreCase("sort")) {
						int num;
						System.out.println("Enter the elements on which you would like to perform sorting (separated by space)");
						String input = scan.nextLine();
						String[] splited = input.split(delimiter);
						int[] numbers = new int[splited.length];
						for(int i=0;i<splited.length;i++) {
							numbers[i] = Integer.parseInt(splited[i]);
						}				
						int[] response = stub.sort(numbers);
						System.out.println(operation + " operation has been performed successfully, syncRPC response: " + Arrays.toString(response));
						out.println("test messsage pushed after syncRPC sorting");
						// out.flush();

					}
	
				}
				else if(actualOperation.equalsIgnoreCase("asyncRPC")) {
					// Registry registry = LocateRegistry.getRegistry("localhost");
					// Hello stub = (Hello) registry.lookup("Hello");

					// Server server = new Server();
					Hello hello = new Server();
					// CompletableFuture<Integer> future = server.asyncAdd(num1, num2);
					// int response = future.join();
					// System.out.println(operation + " operation has been performed successfully, asyncRPC response: " + response);

					System.out.println("Do you want perform sum or sort?");
					String arithmaticOperation = scan.nextLine();
					if (arithmaticOperation.equalsIgnoreCase("sum")) {
						System.out.println("Enter two number on which you would like to perform summmation (separated by space)");
						int num1 = scan.nextInt();
						int num2 = scan.nextInt();
						CompletableFuture<Integer> future = hello.asyncAdd(num1, num2);
						future.thenApply(res -> {
							System.out.println("Async operation has been performed successfully, asyncRPC response: " + res);
							return res;
						});
					} 
					else if (arithmaticOperation.equalsIgnoreCase("sort")) {
						int num;
						System.out.println("Enter the elements on which you would like to perform sorting (separated by space)");
						String input = scan.nextLine();
						String[] splited = input.split(delimiter);
						int[] numbers = new int[splited.length];
						for(int i=0;i<splited.length;i++) {
							numbers[i] = Integer.parseInt(splited[i]);
						}
						CompletableFuture<List<Integer>> future = hello.asyncSort(numbers);
						future.thenApply(res -> {
							System.out.println("Async operation has been performed successfully, asyncRPC response: " + res);
							return res;
						});
					}
				}
				else if (actualOperation.equalsIgnoreCase("rename")) {
					System.out.println("please provide the new file name (inputs are separated by space)");
					String newFileName = scan.nextLine();
					out.println(operation + delimiter + newFileName);
					out.flush();
					System.out.println("Server replied : " + in.readLine());
				}
				else if (actualOperation.equalsIgnoreCase("startSync")){
					Thread t1 = new Thread(new Runnable() {
						@Override
						public void run() {
							syncFiles(clientDir, serverDir);
						}
					});  
					t1.start();
				}
				 else {
					out.println(operation);
					out.flush();
					System.out.println("Server replied : " + in.readLine());
				}
			}

			socket.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		} 	
	}
	public static void syncFiles(String clientDirstr, String serverDirstr){
		long lastTimestamp = 0;
		while(true){
			int syncafterevery5secs = 5000;
			long currentTimestamp = System.currentTimeMillis();
			File clientDir = new File(clientDirstr);
			int counter = 0;
			try{
				for (File filepath : clientDir.listFiles()) {
					if(filepath.lastModified() > lastTimestamp) {
						System.out.println(filepath.getName() + " " + lastTimestamp + " : " + filepath.lastModified());
						String filename = filepath.getName();
						File serverDir = new File(serverDirstr + filename);	
						FileOperations.performFileTransfer(filepath, serverDir);
						counter++;
					}
				}
				lastTimestamp = currentTimestamp;
				System.out.println("Sync done. File transfered: " + counter);
				Thread.sleep(10000);
			}catch(Exception e){
				System.out.println("Error while Sync");
				break;
			}
		}
	}
}
