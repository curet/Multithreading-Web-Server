// José C.Sánchez Curet
// CCOM 4205 - Computer Networking
// Professor: Idealides J. Vergara-Laurens

// Multithreading Web Server

import java.net.*;
import java.io.*;
import java.lang.Thread;
import java.util.StringTokenizer;

/*
 **** class Client implements Runnable ****
 *   class Client will be in charge of the other endpoint of the connection to open the files based on the
 *               file path or return and display 404 NOT FOUND in case there is not a file that match or if isn't found
 *   note: In Java the Runnable keyword is require for the Client/Server connection to work
 */
class Client implements Runnable{
    
    int bytes, BUFFER_SIZE;
    static String SUCCESS = "HTTP/1.0 200 OK\n";
    static String FAILURE = "HTTP/1.1 404\n";
    Socket socket;
    boolean catchedFile;
    String status, contentType, message, inputFile, getline;
    
    // Data, output, file input streams to work with data transfer
    // buffered and tokenizer to read the text and break string into tokens, respectively
    private DataOutputStream   socketOutputStream;
    private InputStreamReader  socketInputReader;
    private BufferedReader     socketBufferedReader;
    private StringTokenizer    socketTokenizer;
    private FileInputStream    socketInputStream;
    
    
    public Client (Socket socket){

        this.socket = socket;
        this.bytes = 0;
        this.BUFFER_SIZE = 1024;
        this.inputFile = "";
        this.catchedFile = true;
        this.getline = "";
        this.status = "";
        this.contentType = "";
        this.message = "";

    }
    
    /*
     processFile() -> to process the file and socketOutputStream, socketInputReader, socketBufferedReader
                      and getline get their first initialization after this the information is send to
                      processBytes() to complete the processing
     */
    private void processFile(){
        
        try{
            socketOutputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            socketInputReader = new InputStreamReader(socket.getInputStream());
            socketBufferedReader = new BufferedReader(socketInputReader);
            getline = socketBufferedReader.readLine();
            
            if (getline.length()!= 0) {
                System.out.println("Persistent: " + getline);
                socketTokenizer = new StringTokenizer(getline);
                socketTokenizer.nextToken();                       // move to next token to access the file path
                this.inputFile = "." + socketTokenizer.nextToken();
                System.out.println("PATH --> " + this.inputFile);
                
                
                try{socketInputStream = new FileInputStream (this.inputFile);}
                catch(FileNotFoundException e){this.catchedFile = false; } // throws exception and avoid adding a wrongful filename
            }
            
            processBytes(socketOutputStream);
            socketBufferedReader.close();
            socket.close();
            
            if(this.catchedFile){System.out.println("FILE FOUND");}
            else{System.out.println("404 -- FILE NOT FOUND!");}
            
        }
        catch(Exception e) { System.out.println(e);}

}
    
    /*
     processBytes()
        @param: DataOutputStream that is the necessary information to process the file
     */
    
    void processBytes(DataOutputStream outputStream){
        
        try{
            if(this.catchedFile){fileCatch(true, this.inputFile);}
            else{fileCatch(false, "");}

            outputStream.writeBytes(this.status);
            outputStream.writeBytes(this.contentType + "\n");
            
            if(this.catchedFile){messageToBytes();}
            else{outputStream.writeBytes(this.message);}

            socketOutputStream.close();
        }
        catch(Exception e) { System.out.println(e);}
    }
    
    /*
     fileCatch()
        @param `catched` verify if the file was received, `message` will pass the file extension or the 404 accordingly
                updating the following variables: status, contenType, and message
     */
    void fileCatch(boolean catched, String message){
        
        if(catched){
            this.status = SUCCESS;
            this.contentType = "Content-Type: " + fileExtensionMatch(message) + "\n";
        }else{
            this.status = FAILURE;
            this.contentType = "Content-Type: text/html\n";
            this.message = "<h1>404\nFILE NOT FOUND!\n:/</h1>";
        }
    }
    
    /*
     messageToBytes()
      @return: the buffered message if there is one
    
     */
    void messageToBytes(){
        try{
            byte[] buffer = new byte[this.BUFFER_SIZE];

            while ((this.bytes = socketInputStream.read(buffer)) != -1) {
                socketOutputStream.write(buffer, 0, this.bytes);                                // send name
                socketOutputStream.flush();
            }
            socketInputStream.close();
        }
        catch(Exception e) { System.out.println(e);}
    }
    
    /*
     fileExtensionMatch()
        @Param receivedFile : takes the file as input
        @return: if receivedFile extension match with the accepted by the system will return the given extension
        
     */
    private static String fileExtensionMatch(String receivedFile){
        String[] acceptedFiletype = {".html", ".jpg", ".png", ".gif",".mp4", ".xml"};          // filetype extensions accepted by this system
        
        for(int i = 0; i < acceptedFiletype.length; i++){                                      // compare received file with the list of accepted filetype
                                                                                               //  to be able to display it in the browser
            String filenameExtension = receivedFile.substring(receivedFile.lastIndexOf("."));
            if(acceptedFiletype[i].equals(filenameExtension)){
                return filenameExtension;                                                      // return file extension
            }
        }return "";}
    
    /*
        run() --> Necessary to run the Client
     */
    public void run() {
        try{processFile();}
        catch(Exception e) { System.out.println(e);}
    }
}


/*
 **** public class Server ****
 *   class Server class will instantiate the Client class and make the requests for a multithreading process
 *
 */

public class MultithreadingWebServer {

    
    public static int clientCount = 0;                            // have tracks numbers of clients
    
    /*
     endPointConnection() -> This function initialize and specify the client and server endpoints and the port where they are going to be established
        @Param endpoint : to indicate the server/client endpoint communication
     
     */
    private static void endPointConnection(ServerSocket endpoint){
        String clientCountMessage = "Connection Esblished for client ";
        
        try{
            Socket receiveEndpoint = endpoint.accept();          // instantiate endpoint
            Client clientEndpoint = new Client(receiveEndpoint); // creating the a client endpoint connection
            Thread thread = new Thread(clientEndpoint);          // create thread per endpoint connection
            System.out.println("Inet Address: " + receiveEndpoint.getInetAddress());
            System.out.println("PORT: " + receiveEndpoint.getLocalPort());
            thread.start();                                      // start tread
            
            clientCount++;
            System.out.println(clientCountMessage + clientCount);
        }
        catch(Exception e){ System.out.println(e);}

    }
    
    public static void main(String[] args){
        MultithreadingWebServer server = new MultithreadingWebServer();
        server.start();
    }
    
    public static void start(){
        try{
            int PORT = 8883;                                     // establishing default port definition [8889]
            ServerSocket SS = new ServerSocket (PORT);           // create socket for the server/client endpoint communication
            System.out.println("Connected with port " + PORT + "!");
            while (true){
                endPointConnection(SS);                          // establish endpoint communication
            }
        }
        catch(Exception e){ System.out.println(e);}
    }
}


