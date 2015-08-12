package HxCKDMS.HxCIssueServer.Server;

import HxCKDMS.HxCIssueServer.GitHub.GitHubReporter;
import HxCKDMS.HxCIssueServer.Logging.Logger;
import HxCKDMS.HxCIssueServer.Streams.CustomSysErrStream;
import HxCKDMS.HxCIssueServer.Streams.CustomSysOutStream;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class HxCIssueServer {
    public static Logger logger = new Logger("HxCIssueServer Logger", "HxCIssueBot_log");

    static volatile boolean running = true;
    private static ServerSocket serverSocket;
    private static Socket connection;
    private static BufferedReader reader;
    private static Gson gson = new Gson();

    static volatile boolean isReceiving = false;

    public static String githubAuthenticationKey;

    public static void main(String[] args) throws IOException {
        System.setOut(new CustomSysOutStream(System.out));
        System.setErr(new CustomSysErrStream(System.err));

        if(args.length != 2){
            logger.severe("Program doesn't accept less and more than 2 arguments [port] [github key]");
            System.exit(-1);
        }
        int port = Integer.parseInt(args[0]);
        githubAuthenticationKey = args[1];

        try{
            serverSocket = new ServerSocket(port, 100);
        } catch (IOException e) {
            logger.severe("Failed to bind to port: " + port + ".", e);
            running = false;
            System.exit(1);
        }

        new ThreadServerWatcher(ThreadServerWatcher.class.getSimpleName()).start();

        while(running) {
            try {
                isReceiving = false;
                waitForConnection();
                isReceiving = true;
                setupInputStream();
                whileReceivingCrash();
                closeConnections();
            } catch (IOException e) {
                logger.warning("connection lost.", e);
            }
        }
    }

    private static void waitForConnection() throws IOException{
        logger.info("waiting...");
        connection = serverSocket.accept();
        logger.info("Connected to: " + connection.getInetAddress().getHostName());
    }

    private static void setupInputStream() throws IOException{
        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        logger.info("setting up streams done");
    }

    private static void whileReceivingCrash() throws IOException{
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line).append("\n");

        try {
            crashSendTemplate receivedFile = gson.fromJson(sb.toString(), crashSendTemplate.class);
            new GitHubReporter(receivedFile.crash, GitHubReporter.class.getSimpleName()).start();
        } catch (Exception e) {
            if(!e.getMessage().contains("BEGIN_ARRAY")) e.printStackTrace();
            else logger.warning("Old reporter!");
        }
    }

    static void closeConnections() throws IOException{
        reader.close();
        connection.close();
        logger.info("Closed all incoming connections.");
    }

    class crashSendTemplate {
        ArrayList<String> crash;

        public crashSendTemplate(ArrayList<String> crash) {
            this.crash = crash;
        }
    }
}
