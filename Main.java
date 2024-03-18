import DNSHandling.DNSServer;

public class Main {
    public static void main(String[] args) {
        try {
            DNSServer dnsServer = new DNSServer();
            dnsServer.startServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}