package HxCKDMS.HxCIssueServer;

import org.kohsuke.github.*;

import java.io.IOException;
import java.util.List;

public class GithubReporter extends Thread {
    private String crash;
    private String mod;
    private String title;

    public GithubReporter(String crash, String mod, String title) {
        this.crash = crash;
        this.mod = mod;
        this.title = title;
    }

    @Override
    public void run() {
        try {
            github();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void github() throws IOException{
        GitHub github = GitHub.connectUsingOAuth(HxCIssueServer.githubAuthenticationKey);
        String gitLink = sendCrash(github, crash);

        GHRepository repository = github.getOrganization("HxCKDMS").getRepository("AutomaticCrashReports");
        List<GHIssue> openedIssues = repository.getIssues(GHIssueState.OPEN);

        for(GHIssue issue : openedIssues){
            if(issue.getTitle().equals("[" + mod + "] " + title)) {
                issue.comment(gitLink);
                return;
            }
        }

        GHIssueBuilder issueBuilder = repository.createIssue("[" + mod + "] " + title);

        issueBuilder.label("bot");
        issueBuilder.body(gitLink);
        issueBuilder.create();
    }

    public String sendCrash(GitHub github, String crash) throws IOException {
        GHGistBuilder gistBuilder = github.createGist();
        gistBuilder.description(crash);
        GHGist gist = gistBuilder.create();
        return gist.getUrl().toString();
    }
}
