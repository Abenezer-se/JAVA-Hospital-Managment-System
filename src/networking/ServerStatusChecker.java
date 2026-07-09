package networking;

import java.net.Socket;

public class ServerStatusChecker {
    public static boolean isServerOnline() {
        try (Socket s = new Socket("localhost", 5000)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getStatus() {
        return isServerOnline() ? "ONLINE" : "OFFLINE";
    }
}