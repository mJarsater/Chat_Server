import java.net.*;
import java.io.*;
import java.util.HashSet;
import java.util.Set;


class Server{
    private Socket socket = null;
    private ServerSocket serverSocket = null;
    private boolean alive = true;
    private Set<Client> clients = new HashSet<>();


    /*
    Metod som initierar server med
    en serversocket och port som har skickats
    med som parameter.

    Sätter strängen host till serverns lokala
    IP-adress.

     */
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


    /*
    Metod som väntar på att klienter ska ansluta.
    Skapar en klient när någon försöker ansluta
    och skickar med socket, server(this) och
    counter (för id).
    Lägger till klienten i hashsetet "clients".
     */

    public void getClient(){
        int counter = 1;
        while(alive){
            try{
                socket = serverSocket.accept();
                Client newClient = new Client(socket, this, counter);
                clients.add(newClient);
                counter ++;
                System.out.println("Number of clients: "+clients.size());
            } catch (IOException ioe){
                System.out.println("Error on accept.");
            }
        }
    }

    /*
    Skickar ut meddelande till alla klienter,
    förutom klienten som skickas med som parameter.
    */
    public synchronized void broadcastExcludeClient(String msg, Client exludeClient){
        for(Client client: clients){
            if(client != exludeClient) {
                client.sendMsg(msg);
            }
        }
    }


    /*
     Skickar ut meddelande till alla klienter.
     */
    public synchronized void broadcastToAll(String msg){
        for(Client client: clients){
            client.sendMsg(msg);
        }
    }


    /*
    Tar bort den klient som skickats med som parameter,
    samt skickar meddelande till alla klienter.
    */
    public synchronized void removeClient(Client client, int id){
        clients.remove(client);
        System.out.println("Client no"+id+ " disconnected.");
        broadcastToAll("Client no"+id+ " disconnected.");
        System.out.println("Number of clients: "+clients.size());

    }


    //Konstruktor för klassen Server
    public Server(int port){
        initServer(port);
        System.out.println("Waiting for clients...");
        getClient();
    }


    /*------------- MAIN -----------------------*/
    public static void main(String[] args) {
        if(args.length == 1){
            Server newServer = new Server(Integer.parseInt(args[0]));
        } else {
            Server newServer = new Server(2000);
        }
    }
    /*------------- MAIN END-------------------- */
}


class Client extends Thread{
    private int id;
    private boolean alive = true;
    private Socket socket;
    private Server server;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;



    // Konstruktor för klassen Klient
    public Client(Socket socket, Server server, int id){
        this.socket = socket;
        this.server = server;
        this.id = id;

        /*
        "Startar" klienten genom att kalla på start-metoden
        */
        start();
    }


    /*
    Start-metoden som körs så länga alive är sant.
    */
    public void run() {
        while (alive) {
            try {
                //Hämtar inputstream från servens socket.
                InputStream inputStream = socket.getInputStream();

                // Skapar en bufferedreader med inputstreamen.
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));


                //Hämtar outputstream från servens socket.
                OutputStream outputStream = socket.getOutputStream();

                // Skapar en printwriter med outpurstreamen.
                printWriter = new PrintWriter(outputStream, true);


                String serverMsg = "Client no" + id + " connected.";
                System.out.println(serverMsg);
                server.broadcastExcludeClient(serverMsg, this);


                String clientMsg = "";

                // Körs sålänge input != "exit"
                while (!clientMsg.equalsIgnoreCase("exit")) {
                    clientMsg = bufferedReader.readLine();
                    /*
                    Skapar ett meddelande med information om klienten
                    samt meddelandet som klienten skickat.
                    */
                    serverMsg = "Client no" + id + ": " + clientMsg;
                    /*
                    Skickar meddelandet till alla förutom klienten(this).
                    */
                    server.broadcastExcludeClient(serverMsg, this);
                }
                //Stänger printwriter
                printWriter.close();
                //Stänger bufferedreader
                bufferedReader.close();
                //Tar bort klienten
                server.removeClient(this, id);
                //Stänger socketanslutningen
                socket.close();
                //Dödar tråden
                kill();

            } catch (IOException ioe) {
                System.out.println("Client disconnected:");
                printWriter.close();
                server.removeClient(this, id);
                kill();
            }

        }
    }

    /*
    Sätter alive till false vilket
    dödar tråden.
    */
    public void kill(){
        alive = false;

    }
    /*
    Skriver ut meddelanden från server
    till klienten.
    */
    public void sendMsg(String msg){
        printWriter.println(msg);
    }



}