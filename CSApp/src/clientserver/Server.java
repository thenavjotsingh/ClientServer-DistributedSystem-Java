
package clientserver;

import java.io.*;
import java.net.*;
import java.util.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.CompletableFuture;
import java.util.Arrays;
import java.util.List;
import java.nio.file.*;
import org.apache.commons.io.*;
import java.util.concurrent.*;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;


class Server implements Hello, java.io.Serializable {

    public Server() throws RemoteException {}

	String currentDir = System.getProperty("user.dir");

	public void upload() throws IOException {
        String source = currentDir + "/ClientFolder";
        File srcDir = new File(source);
        String destination = currentDir + "/DropBox";
        File destDir = new File(destination);
        FileUtils.cleanDirectory(destDir);
        FileUtils.copyDirectory(srcDir, destDir,null,false, REPLACE_EXISTING );
    }

	@Override
    public void uploadAuto() throws IOException {
        System.out.println("auto upload");
        Path serverFile = Paths.get(currentDir + "/DropBox");
        Path clientFile = Paths.get(currentDir + "/SampleFiles");
        long serverTime = Files.getLastModifiedTime(serverFile).to(TimeUnit.SECONDS);
        long clientTime = Files.getLastModifiedTime(clientFile).to(TimeUnit.SECONDS);
        System.out.println("serverTime "+serverTime);
        System.out.println("clientTime "+clientTime);
        if (serverTime < clientTime) {
            upload();
        }
    }

	
    public int add(int a, int b) {
		System.out.println("performing sum operation");
        return a+b;
    }

	public int[] sort(int[] arr) {
		System.out.println("performing sort operation");
		Arrays.sort(arr);
		return arr;
	}

	public CompletableFuture<Integer> asyncAdd(int num1, int num2) {
		return CompletableFuture.supplyAsync(() -> {
            try {
				System.out.println("ACK Signal : Request has been received by server.");
				Thread.sleep(5000);
            } catch (InterruptedException e ) {
                Thread.currentThread().interrupt();
            }
            return num1+num2;
        });
    }

	public CompletableFuture<List<Integer>> asyncSort(int[] arr) {
		return CompletableFuture.supplyAsync(() -> {
            try {
				System.out.println("ACK Signal : Request has been received by server.");
				System.out.println("performing sort operation");
				Arrays.sort(arr);
				Thread.sleep(5000);
		} catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
			List<Integer> res = new ArrayList<>();
			for(int i : arr){
				res.add(i);
			}
            return res;
        });
	}



	public static void main(String[] args)
	{
        try {
            Server obj = new Server();
            Hello stub = (Hello) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("Hello", stub);

            System.err.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            // e.printStackTrace();
        }

		ServerSocket server = null;
        int ClientCounter = 1;

		try {
			server = new ServerSocket(1234);
			server.setReuseAddress(true);
			while (true) {
				Socket client = server.accept();
				System.out.println("client " + ClientCounter++ + " connected " + client.getInetAddress().getHostAddress());
				ClientHandler clientSock = new ClientHandler(client);
				new Thread(clientSock).start();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (server != null) {
				try {
					server.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// ClientHandler class
	private static class ClientHandler implements Runnable {
		private final Socket connectionSocket;
		private String serverDir = "/Users/maitripatel/Downloads/distributed_assignment_1/ClientServerDistributedApplication/ServerFileStrcture/";
		private String clientDir = "/Users/maitripatel/Downloads/distributed_assignment_1/ClientServerDistributedApplication/ClientFileStrcture/";

		// Constructor
		public ClientHandler(Socket socket)
		{
			this.connectionSocket = socket;
		}

		public void run()
		{

			PrintWriter out = null;
			BufferedReader in = null;
			try {
				out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(connectionSocket.getOutputStream())), true);
				in = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

				String line;
				while ((line = in.readLine()) != null) {
					String[] splited = line.split(" ");
					String operation = splited[0];
					
					try {
						if (operation.equalsIgnoreCase("download")) {
							String filename = null;

							if(splited.length > 1) {
								filename = splited[1];
							}
							File filepathToRead = new File(serverDir + filename);
							if (!filepathToRead.exists()) {
								System.out.println("File does not exist or please enter a valid Command");
								out.println("File does not exist or please enter a valid Command");
							} else {
								File filepathToWrite = new File(clientDir + filename);
								FileOperations.performFileTransfer(filepathToRead, filepathToWrite);
								System.out.println(operation + " operation has been performed successfully on " + filename);
								out.println(operation + " operation has been performed successfully on " + filename);
							}
						}

						if (operation.equalsIgnoreCase("upload")) {
							String filename = null;
							if(splited.length > 1) {
								filename = splited[1];
							}
							File filepathToRead = new File(clientDir + filename);
							if (!filepathToRead.exists()) {
								System.out.println("File does not exist or please enter a valid Command");
								out.println("File does not exist or please enter a valid Command");	
							} else {
								File filepathToWrite = new File(serverDir + filename);
								FileOperations.performFileTransfer(filepathToRead, filepathToWrite);
								System.out.println(operation + " operation has been performed successfully on " + filename);
								out.println(operation + " operation has been performed successfully on " + filename);
							}
						}
						if (operation.equalsIgnoreCase("delete")) {
							String filename = null;
							if(splited.length > 1) {
								filename = splited[1];
							}
							File filepathToRead = new File(serverDir + filename);
							if (filepathToRead.exists()) {
								filepathToRead.delete();
								System.out.println(operation + " operation has been performed successfully on " + filename);
								out.println(operation + " operation has been performed successfully on " + filename);
							} else {
								System.out.println("File does not exist or please enter a valid Command");
								out.println("File does not exist or please enter a valid Command");

							}
						}
						if (operation.equalsIgnoreCase("rename")) {
							String filename = null;
							String newFileName = null;
							if(splited.length > 1) {
								filename = splited[1];
								newFileName = splited[2];
							}
							File filepathToRead = new File(serverDir + filename);

							File newFilePath = new File(serverDir + newFileName);
							if (filepathToRead.exists()) {
								filepathToRead.renameTo(newFilePath);
								System.out.println(operation + " operation has been performed successfully on " + filename);
								out.println(operation + " operation has been performed successfully on " + filename);
							} else {
								System.out.println("File does not exist or please enter a valid Command");
								out.println("File does not exist or please enter a valid Command");

							}
						}

					} catch (FileNotFoundException e) {
						System.out.println("inside while exception ");
						e.printStackTrace();
					}			
				}

					// writing the received message from client
                    System.out.println("---------------------------------");
					System.out.printf(" Sent from the client: %s\n",line);
					out.println("Not a right operation");

			}
			catch (IOException e) {
				System.out.println("parent exceptinpol");
				e.printStackTrace();
			} 
			finally {
				try {
					if (out != null) {
						out.close();
					}
					if (in != null) {
						in.close();
						connectionSocket.close();
					}
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
