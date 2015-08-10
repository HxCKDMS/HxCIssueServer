package HxCKDMS.HxCIssueServer.Server;

import java.io.IOException;

public class ThreadServerWatcher extends Thread {
    private int counter = 0;

    public ThreadServerWatcher(String name) {
        setName(name);
    }

    @Override
    public void run() {
        while(HxCIssueServer.running) {
            if(HxCIssueServer.isReceiving) counter++;
            else if(counter != 0 && !HxCIssueServer.isReceiving) counter = 0;
            try {
                sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(counter == 30) {
                counter = 0;
                try {
                    HxCIssueServer.closeConnections();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
