import com.sun.source.tree.BreakTree;

import java.net.*;
import java.io.*;
import java.util.HashSet;
import java.util.Set;


class Server{
    private Socket socket = null;
    private ServerSocket serverSocket = null;
    private boolean alive = true;
    private Set<Client> clients = new HashSet<>();

    public void initServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server initialized.");
            String host = serverSocket.getInetAddress().getLocalHost().getHostName();
            System.out.println("Server hostadress: "+host);
        } catch (IOException ioe){
            System.out.println("Could not initialize server.");
        }
    }

    public void getClient(){
        int counter = 0;
        while(alive){
            try{
                socket = serverSocket.accept();
                Client newClient = new Client(socket, this, counter);
                clients.add(newClient);
                newClient.start();
                counter ++;
            } catch (IOException ioe){
                System.out.println("Error on accept.");
            }
        }
    }

    public synchronized void broadcast(String msg, Client exludeClient){
        for(Client client: clients){
            if(client != exludeClient) {
                client.sendMsg(msg);
            }
        }
    }


    public synchronized void removeClient(Client client, int id){
        clients.remove(client);
        System.out.println("Client no"+id+ " disconnected.");
        broadcast("Client no"+id+ " disconnected.", client);
    }



    public Server(int port){
        initServer(port);
        System.out.println("Waiting for clients...");
        getClient();
    }


    public static void main(String[] args) {
        Server newServer = new Server(2000);
    }
}

class Input{

}

class Output{

}

class Client extends Thread{
    private int id;
    private boolean alive = true;
    private Socket socket;
    private Server server;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;

    public Client(Socket socket, Server server, int id){
        this.socket = socket;
        this.server = server;
        this.id = id;
    }

    public void run() {
            try {
                InputStream inputStream = socket.getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                OutputStream outputStream = socket.getOutputStream();
                printWriter = new PrintWriter(outputStream, true);

                String serverMsg = "Client no" + id + " connected.";
                System.out.println(serverMsg);
                server.broadcast(serverMsg, this);

                String clientMsg = "";

                while (!clientMsg.equalsIgnoreCase("exit")) {
                    clientMsg = bufferedReader.readLine();
                    serverMsg = "Client no" + id + ": " + clientMsg;
                    server.broadcast(serverMsg, this);
                }
                server.removeClient(this, id);
                socket.close();

            } catch (IOException ioe) {
                System.out.println("Error: Client thread");


            }

    }

    public void kill(){
        alive = false;
    }

    public void sendMsg(String msg){
        printWriter.println(msg);
    }



}