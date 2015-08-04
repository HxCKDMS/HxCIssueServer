package HxCKDMS.HxCIssueServer;

import org.kohsuke.github.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GithubReporter extends Thread {
    private ArrayList<String> crash;

    private String mod;
    private String title;
    private String version;
    private String stacktrace;
    private String mc_ver;

    public GithubReporter(ArrayList<String> crash) {
        this.crash = crash;
    }

    @Override
    public void run() {
        try {
            parseCrash(crash);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseCrash(ArrayList<String> crash) throws Exception{
        StringBuilder crashBuilder = new StringBuilder();
        StringBuilder stacktraceBuilder = new StringBuilder();
        int lineNumber = 0;
        boolean hasEncounteredEmptyAfterAt = false;

        for(String line : crash) {
            if(++lineNumber == 7) title = line;
            if((mod == null || mod.equals("HxCCore")) && line.contains("HxCKDMS")) {
                String[] words;
                if((words = line.split("\\.")).length >= 2) mod = words[1];
            }

            if(lineNumber >= 7 && line.contains("at") && !hasEncounteredEmptyAfterAt) {
                if(!line.contains("at")) hasEncounteredEmptyAfterAt = true;
                stacktraceBuilder.append(line).append("\n");
            }

            if(mod != null && line.contains(mod)) {
                char[] chars = line.toCharArray();
                boolean hasEncounteredCurlyBracket = false;
                StringBuilder versionBuilder = new StringBuilder();
                for(Character character : chars) {
                    if(character.equals('{')) hasEncounteredCurlyBracket = true;
                    if(hasEncounteredCurlyBracket && character.equals('}')) return;
                    if(hasEncounteredCurlyBracket) versionBuilder.append(character);
                }
                version = versionBuilder.toString();
            }
            if(line.contains("Minecraft Version: ")) mc_ver = line.trim().replace("Minecraft Server: ", "");
            crashBuilder.append(line).append("\n");
        }

        stacktrace = stacktraceBuilder.toString();
        if(checkMCVersion())github(crashBuilder.toString());
        else System.out.println("Mod version is outdated.");
    }

    private void github(String crash) throws Exception{
        GitHub github = GitHub.connectUsingOAuth(HxCIssueServer.githubAuthenticationKey);
        String gitLink = sendCrash(github, crash);

        GHRepository repository = github.getOrganization("HxCKDMS").getRepository("AutomaticCrashReports");
        List<GHIssue> openedIssues = repository.getIssues(GHIssueState.OPEN);

        for(GHIssue issue : openedIssues){
            if(issue.getTitle().equals("[" + mc_ver + "][" + mod + "] " + title)) {
                issue.comment(gitLink);
                return;
            }
        }

        GHIssueBuilder issueBuilder = repository.createIssue("[" + mc_ver + "][" + mod + "] " + title);

        issueBuilder.label("bot");
        issueBuilder.body(gitLink);
        issueBuilder.create();
    }

    private String sendCrash(GitHub github, String crash) throws Exception {
        GHGistBuilder gistBuilder = github.createGist();
        gistBuilder.file("[" + mc_ver + "][" + mod + "] " + title, crash);

        GHGist gist = gistBuilder.create();
        return gist.getHtmlUrl().toString();
    }

    private boolean checkMCVersion() throws IOException{
        URL url = new URL("https://raw.githubusercontent.com/HxCKDMS/HxCLib/master/HxCLib.txt");
        InputStream inputStream = url.openStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        while((line = reader.readLine()) != null) {
            if(line.contains(mod) && line.contains(mc_ver)) {
                int currentVersion = Integer.parseInt(line.replace(mod, "").replace(mc_ver, "").replace("-", "").replace(":", "").replace(".", ""));
                int modVersion = Integer.parseInt(version.replace(".", ""));
                if(modVersion >= currentVersion) return true;
            }
        }
        return  false;
    }
}
