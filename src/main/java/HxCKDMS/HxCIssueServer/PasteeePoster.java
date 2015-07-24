package HxCKDMS.HxCIssueServer;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class PasteeePoster {
    private static Gson gson = new Gson();

    public static String sendCrash(String crash, String mod, String name) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL("http://paste.ee/api").openConnection();
        connection.setDoOutput(true); connection.setDoInput(true);

        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
        writer.write("key=" + HxCIssueServer.pasteeeAuthenticationKey + "&description=[" + mod + "]: " + name + "&language=java&paste=" + crash);
        writer.flush();
        writer.close();
        connection.disconnect();


        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) stringBuilder.append(line).append("\n");

        String status = gson.fromJson(stringBuilder.toString(), pasteeeTemplate.class).status;

        if(!status.equals("success")) return null;
        return (String)gson.fromJson(stringBuilder.toString(), pasteeeTemplate2.class).paste.get("link");
    }

    class pasteeeTemplate {
        String status;
    }

    class pasteeeTemplate2 {
        HashMap paste;
    }
}
