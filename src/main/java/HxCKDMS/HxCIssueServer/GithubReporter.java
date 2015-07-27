package HxCKDMS.HxCIssueServer;

import org.kohsuke.github.*;

import java.io.IOException;
import java.util.List;

public class GithubReporter extends Thread {
    private String crash;
    private String mod;
    private String title;
    String pasteeeLink;

    public GithubReporter(String crash, String mod, String title) {
        this.crash = crash;
        this.mod = mod;
        this.title = title;
    }

    @Override
    public void run() {
        try {
            pasteeeLink = PasteeePoster.sendCrash(crash, mod, title);
            if(pasteeeLink != null) github();
            else System.out.println("Pasteee link was null!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void github() throws IOException{
        GitHub github = GitHub.connectUsingOAuth(HxCIssueServer.githubAuthenticationKey);
        GHRepository repository = github.getOrganization("HxCKDMS").getRepository("AutomaticCrashReports");

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
}
