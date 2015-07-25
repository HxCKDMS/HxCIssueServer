package HxCKDMS.HxCIssueServer;

import org.kohsuke.github.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

public class GithubReporter extends Thread {
    private String crash;
    private String mod;
    private String title;
    private String version;
    String pasteeeLink;

    public GithubReporter(String crash, String mod, String title, String version) {
        this.crash = crash;
        this.mod = mod;
        this.title = title;
        this.version = version;
    }

    @Override
    public void run() {
        try {
            if(shouldUpload()) {
                pasteeeLink = PasteeePoster.sendCrash(crash, mod, title);
                if(pasteeeLink != null) github();
            } else {
                System.out.println("File didn't have to be uploaded!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void github() throws IOException{
        GitHub github = GitHub.connectUsingOAuth(HxCIssueServer.githubAuthenticationKey);
        GHRepository repository = github.getOrganization("HxCKDMS").getRepository("Version-and-Crashes");

        List<GHIssue> openedIssues = repository.getIssues(GHIssueState.OPEN);

        for(GHIssue issue : openedIssues){
            if(issue.getTitle().equals(title)) {
                issue.comment(pasteeeLink);
                return;
            }
        }

        GHIssueBuilder issueBuilder = repository.createIssue(title);

        issueBuilder.label("bot");
        issueBuilder.body(pasteeeLink);
        issueBuilder.create();
    }

    private boolean shouldUpload() throws IOException {
        if(mod == null) return false;
        URL url = new URL("https://raw.githubusercontent.com/HxCKDMS/Version-and-Crashes/master/Versions");
        InputStream inputStream = url.openStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        boolean answer = false;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith(mod)) {
                answer = Integer.parseInt(version.replace(".", "")) >= Integer.parseInt(line.replace(mod + ":", "").replace(".", ""));
                break;
            }
        }
        return answer;
    }
}
