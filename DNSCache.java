package DNSHandling;
import java.util.HashMap;

public class DNSCache {
    private static HashMap<DNSQuestion, DNSRecord> cache;

    public DNSCache() {
        cache = new HashMap<>();
    }

    public static DNSRecord get(DNSQuestion question) {
        System.out.println("Checking the cache...");
        if (cache.containsKey(question)) {
            DNSRecord record = cache.get(question);
            //if the record is expired
            if (record != null && record.isExpired()) {
                //remove it from the cache
                cache.remove(question);
                return null;
            }
            else {
                System.out.println("Cache contains the query!");
                return record;
            }
        }
        //if not found
        System.out.println("Cache does not contain query");
        return null;
    }

    public static void put(DNSQuestion question, DNSRecord record) {
        System.out.println("Putting record in the cache\n");
        cache.put(question, record);
    }
}
