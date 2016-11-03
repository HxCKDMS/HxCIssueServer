package hxckdms.hxcissueserver.server;

import hxckdms.hxccore.crash.CrashSerializer;
import hxckdms.hxcissueserver.github.GitHubReporter;
import hxckdms.hxcissueserver.logging.Logger;
import hxckdms.hxcissueserver.streams.CustomSysErrStream;
import hxckdms.hxcissueserver.streams.CustomSysOutStream;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class HxCIssueServer {
    public static Logger logger = new Logger("HxCIssueServer Logger", "HxCIssueBot_log");

    static volatile boolean running = true;
    private static ServerSocket serverSocket;
    private static Socket connection;
    private static ObjectInputStream input;

    static volatile boolean isReceiving = false;

    public static String githubAuthenticationKey;

    public static void main(String[] args) throws IOException, ClassNotFoundException {
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



        while(running) {
            try {
                isReceiving = false;
                waitForConnection();
                isReceiving = true;
                setupInputStream();
                whileReceivingCrash();
                closeConnections();
            } catch (IOException e) {
                e.printStackTrace();
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
        input = new ObjectInputStream(connection.getInputStream());
        logger.info("setting up streams done");
    }

    private static void whileReceivingCrash() throws IOException, ClassNotFoundException {
        CrashSerializer crash = (CrashSerializer) input.readObject();

        new GitHubReporter(new ArrayList<>(Arrays.asList(crash.crashString.split("\n"))), GitHubReporter.class.getSimpleName()).start();
    }

    private static void closeConnections() throws IOException{
        input.close();
        connection.close();
        logger.info("Closed all incoming connections.");
    }
}
