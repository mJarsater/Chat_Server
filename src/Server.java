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
            System.out.println("Server hostadress: "+host+ " Port:"+port);
            System.out.println("Number of clients: "+clients.size());
        } catch (IOException ioe){
            System.out.println("Could not initialize server.");
        }
    }

    public void getClient(){
        int counter = 1;
        while(alive){
            try{
                socket = serverSocket.accept();
                Client newClient = new Client(socket, this, counter);
                clients.add(newClient);
                newClient.start();
                counter ++;
                System.out.println("Number of clients: "+clients.size());
            } catch (IOException ioe){
                System.out.println("Error on accept.");
            }
        }
    }

    public synchronized void broadcastExcludeClient(String msg, Client exludeClient){
        for(Client client: clients){
            if(client != exludeClient) {
                client.sendMsg(msg);
            }
        }
    }

    public synchronized void broadcastToAll(String msg){
        for(Client client: clients){
            client.sendMsg(msg);
        }
    }

    public void kill(){
        alive = false;
        System.exit(1);
    }


    public synchronized void removeClient(Client client, int id){
        clients.remove(client);
        System.out.println("Client no"+id+ " disconnected.");
        broadcastToAll("Client no"+id+ " disconnected.");
        System.out.println("Number of clients: "+clients.size());

    }



    public Server(int port){
        initServer(port);
        System.out.println("Waiting for clients...");
        getClient();
    }

 // ------------- MAIN ----------------------- //
    public static void main(String[] args) {
        if(args.length == 1){
            Server newServer = new Server(Integer.parseInt(args[0]));
        } else {
            Server newServer = new Server(2000);
        }
    }
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
        while (alive) {
            try {
                InputStream inputStream = socket.getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                OutputStream outputStream = socket.getOutputStream();
                printWriter = new PrintWriter(outputStream, true);

                String serverMsg = "Client no" + id + " connected.";
                System.out.println(serverMsg);
                server.broadcastExcludeClient(serverMsg, this);


                String clientMsg = "";

                while (!clientMsg.equalsIgnoreCase("exit")) {
                    clientMsg = bufferedReader.readLine();
                    serverMsg = "Client no" + id + ": " + clientMsg;
                    server.broadcastExcludeClient(serverMsg, this);
                }
                printWriter.close();
                bufferedReader.close();
                server.removeClient(this, id);
                socket.close();
                kill();

            } catch (IOException ioe) {
                System.out.println("Error: Client thread");
                printWriter.close();
                server.kill();
                kill();
            }

        }
    }

    public void kill(){
        alive = false;
        System.exit(1);
    }

    public void sendMsg(String msg){
        printWriter.println(msg);
    }



}