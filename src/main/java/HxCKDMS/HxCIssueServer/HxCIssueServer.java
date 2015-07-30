package HxCKDMS.HxCIssueServer;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class HxCIssueServer {
    private static boolean running = true;
    private static ServerSocket serverSocket;
    private static Socket connection;
    private static BufferedReader reader;
    private static Gson gson = new Gson();

    public static String githubAuthenticationKey;

    public static void main(String[] args) {
        if(args.length != 3){
            System.err.println("Program doesn't accept less and more than 3 arguments [port] [github key]");
            System.exit(-1);
        }
        int port = Integer.parseInt(args[0]);
        githubAuthenticationKey = args[1];

        try{
            serverSocket = new ServerSocket(port, 100);
        } catch (IOException e) {
            System.err.println("Failed to bind to port: " + port + ".");
            running = false;
            System.exit(1);
        }

        while(running) {
            try {
                waitForConnection();
                setupInputStream();
                whileReceivingCrash();
                closeConnections();
            } catch (IOException e) {
                System.err.print("connection lost.");
            }
        }
    }

    private static void waitForConnection() throws IOException{
        System.out.println("waiting...");
        connection = serverSocket.accept();
        System.out.println("Connected to: " + connection.getInetAddress().getHostName());
    }

    private static void setupInputStream() throws IOException{
        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        System.out.println("setting up streams done");
    }

    private static void whileReceivingCrash() throws IOException{
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line).append("\n");

        crashSendTemplate receivedFile = gson.fromJson(sb.toString(), crashSendTemplate.class);
        new GithubReporter(receivedFile.crash, receivedFile.mod, receivedFile.title).run();
    }

    private static void closeConnections() throws IOException{
        reader.close();
        connection.close();
    }

    class crashSendTemplate {
        String crash;
        String mod;
        String title;

        public crashSendTemplate(String crash, String mod, String title) {
            this.crash = crash;
            this.mod = mod;
            this.title = title;
        }
    }
}
