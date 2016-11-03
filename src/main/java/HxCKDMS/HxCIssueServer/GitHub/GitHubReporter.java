package hxckdms.hxcissueserver.github;

import hxckdms.hxcissueserver.server.HxCIssueServer;
import org.kohsuke.github.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
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
        String prevLine = "";

        for(String line : crashFile) {
            try {
                if(line.toLowerCase().contains("duplicate enchantment id!")) {
                    HxCIssueServer.logger.warning("This is a duplicate enchantment id, this will not be reported!");
                    return;
                }
                if(line.toLowerCase().contains("maximum id range exceeded.")) {
                    HxCIssueServer.logger.warning("Id range exceeded not reporting!");
                    return;
                }
                if(line.toLowerCase().contains("java version: 1.7") || line.toLowerCase().contains("java version: 1.6")) {
                    HxCIssueServer.logger.warning("Unsupported Java Version!");
                    return;
                }
                if((mod == null || mod.equalsIgnoreCase("hxccore")) && line.toLowerCase().contains("at hxckdms")) {
                    String[] words;
                    if((words = line.split("\\.")).length >= 2) mod = words[1];
                }

                if(mod != null && line.contains(mod) && !line.contains("asm")) {
                    char[] chars = line.toCharArray();
                    System.out.println(Arrays.toString(chars));
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
                if(!prevLine.startsWith("Description:") && !prevLine.startsWith("Time:") && !prevLine.equalsIgnoreCase("---- Minecraft Crash Report ----") && (title == null || title.equals("")) && line.contains("at"))
                    title = prevLine;
                prevLine = line;
            } catch (Exception e) {
                HxCIssueServer.logger.warning("an error happened whilst parsing the crash: ", e);
            }
        }

        stacktrace = "";
        crash = crashBuilder.toString();

        if(checkMCVersion()) github();
        else HxCIssueServer.logger.warning("Mod version is outdated. version: " + version + ", mod: " + mod + ", minecraft version: " + mc_ver);
    }

    private void github() throws IOException{
        HxCIssueServer.logger.info("Reporting issue : " + "[" + mc_ver + "][" + mod + "] " + title);
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
        if ("".equalsIgnoreCase(version)) {
            HxCIssueServer.logger.warning("was unable to read the mod version.");
            return false;
        }
        int modVersion = Integer.parseInt(version.replace(".", "").replace("1710-", "").replace("18-", "").replace("1102", ""));

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
        return false;
    }
}
