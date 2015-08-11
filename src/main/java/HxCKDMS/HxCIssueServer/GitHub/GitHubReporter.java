package HxCKDMS.HxCIssueServer.GitHub;

import HxCKDMS.HxCIssueServer.Server.HxCIssueServer;
import org.kohsuke.github.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GitHubReporter extends Thread {
    private ArrayList<String> crashFile;

    private String crash;
    private String mod;
    private String title;
    private String version;
    private String stacktrace;
    private String mc_ver;

    public GitHubReporter(ArrayList<String> crashFile, String name) {
        this.crashFile = crashFile;
        setName(name);
    }

    @Override
    public void run() {
        try {
            parseCrash();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseCrash() throws IOException{
        StringBuilder crashBuilder = new StringBuilder();
        StringBuilder stacktraceBuilder = new StringBuilder();
        int lineNumber = 0;
        boolean hasEncounteredEmptyAfterAt = false;
        boolean hasTitle = false;
        String prevLine = "";

        for(String line : crashFile) {
            if(!hasTitle) {
                if(line.contains("at")) {
                    title = prevLine;
                    hasTitle = true;
                }
                prevLine = line;
            }

            if((mod == null || mod.equals("HxCCore")) && line.contains("at HxCKDMS")) {
                String[] words;
                if((words = line.split("\\.")).length >= 2) mod = words[1];
            }

            if(lineNumber >= 7 && line.contains("at") && !hasEncounteredEmptyAfterAt) {
                if(!line.contains("at")) hasEncounteredEmptyAfterAt = true;
                stacktraceBuilder.append(line).append("\n");
            }

            if(mod != null && line.contains(mod) && !line.contains("Asm")) {
                char[] chars = line.toCharArray();
                boolean hasEncounteredCurlyBracket = false;
                StringBuilder versionBuilder = new StringBuilder();
                for(Character character : chars) {
                    if(hasEncounteredCurlyBracket && character.equals('}')) break;
                    if(hasEncounteredCurlyBracket) versionBuilder.append(character);
                    if(character.equals('{')) hasEncounteredCurlyBracket = true;
                }
                version = versionBuilder.toString();
            }
            if(line.contains("Minecraft Version: ")) mc_ver = line.trim().replace("Minecraft Version: ", "");
            crashBuilder.append(line).append("\n");
        }

        stacktrace = stacktraceBuilder.toString();
        crash = crashBuilder.toString();

        if(checkMCVersion())github();
        else HxCIssueServer.logger.warning("Mod version is outdated. version: " + version + ", mod: " + mod + ", minecraft version: " + mc_ver);
    }

    private void github() throws IOException{
        GitHub github = GitHub.connectUsingOAuth(HxCIssueServer.githubAuthenticationKey);
        String gitLink = sendCrash(github);

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

    private String sendCrash(GitHub github) throws IOException {
        GHGistBuilder gistBuilder = github.createGist();
        gistBuilder.file("[" + mc_ver + "][" + mod + "] ", crash);

        GHGist gist = gistBuilder.create();
        return gist.getHtmlUrl().toString();
    }

    private boolean checkMCVersion() throws IOException{
        if (version.equals("")) {
            HxCIssueServer.logger.warning("was unable to read the mod version.");
            return false;
        }
        int modVersion = Integer.parseInt(version.replace(".", "").replace("1710-", "").replace("18-", ""));

        URL url = new URL("https://raw.githubusercontent.com/HxCKDMS/HxCLib/master/HxCVersions.txt");
        InputStream inputStream = url.openStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        while((line = reader.readLine()) != null) {
            if(line.split(":")[0].contains(mod) && line.split(":")[1].split("\\-")[0].contains(mc_ver)) {
                int currentVersion = Integer.parseInt(line.split(":")[1].split("\\-")[1].replace(".", ""));
                HxCIssueServer.logger.info("Mod: " + mod + " crash version: " + modVersion + " HxCLib version: " + currentVersion);
                if(modVersion >= currentVersion) return true;
            }
        }
        return  false;
    }
}
