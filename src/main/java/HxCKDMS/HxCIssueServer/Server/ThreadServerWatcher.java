package HxCKDMS.HxCIssueServer.Server;

import java.io.IOException;

public class ThreadServerWatcher extends Thread {
    private long counter = 0;

    public ThreadServerWatcher(String name) {
        setName(name);
    }

    @Override
    public void run() {
        while(HxCIssueServer.running) {
            if(HxCIssueServer.isReceiving) ++counter;
            else if(!HxCIssueServer.isReceiving) counter = 0;

            if(counter == 30e9) {
                try {
                    HxCIssueServer.closeConnections();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
