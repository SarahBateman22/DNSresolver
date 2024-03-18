package DNSHandling;
import java.io.*;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

/*    0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                                               |
    /                                               /
    /                      NAME                     /
    |                                               |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                      TYPE                     |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                     CLASS                     |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                      TTL                      |
    |                                               |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                   RDLENGTH                    |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--|
    /                     RDATA                     /
    /                                               /
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
*/
public class DNSRecord {

    private String[] name;
    private int type;
    private int rClass;
    private int rLength;
    private byte[] rData;
    private Date creationDate;
    private long timeToLive; //in seconds

    private DNSRecord() {}

    DNSRecord(String[] name, int type, int rClass, int ttl, int rdLength, byte[] rdata, Date date) {
        this.name = name;
        this.type = type;
        this.rClass = rClass;
        timeToLive = ttl;
        rLength = rdLength;
        rData = rdata;
        creationDate = date;
    }

     static DNSRecord decodeRecord(InputStream inputStream, DNSMessage dnsMessage) throws IOException {
        DNSRecord record = new DNSRecord();
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        //String[] name;

        //mark the current position in the input stream
        dataInputStream.mark(2);

         //read the next two bytes to get the id
         short firstTwoBytes = dataInputStream.readShort();

         //if it's been seen before...
         if ((firstTwoBytes & 0xC000) == 0xC000) {
             //handle compressed domain name
             int offset = firstTwoBytes & 0x3FFF;
             record.name = dnsMessage.readDomainName(offset);
             //System.out.println("Record name" + Arrays.toString(record.name));

         } else {
             //reset the input stream to where we bookmarked to read the full domain name from the first two bytes
             dataInputStream.reset();
             record.name = DNSMessage.readDomainName(dataInputStream);
         }

         //read sets of next two to get the type and class
         record.type = dataInputStream.readShort();
         record.rClass = dataInputStream.readShort();

         //4 bytes for ttl
         record.timeToLive = dataInputStream.readInt();

         //and last two for length
         record.rLength = dataInputStream.readShort();

         //read the data from the byte array
         record.rData = new byte[record.rLength];

         //decode it with the data input stream
         dataInputStream.readFully(record.rData);

         //initialize a new date object
         record.creationDate = new Date();

         return record;
     }

     public void writeBytes(ByteArrayOutputStream byteArrayOutputStream, HashMap<String, Integer> domainNameLocations) throws IOException {
         DNSMessage.writeDomainName(byteArrayOutputStream, domainNameLocations, name);
         DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

         dataOutputStream.writeShort(type);
         dataOutputStream.writeShort(rClass);
         dataOutputStream.writeLong(timeToLive);
         dataOutputStream.writeShort(rData.length);
         dataOutputStream.write(rData);
     }

    //created by IDE
    public String toString() {
        return "DNSRecord{" +
                "rName='" + name + '\'' +
                ", rType=" + type +
                ", rClass=" + rClass +
                ", rdLength=" + rLength +
                ", rdata=" + Arrays.toString(rData) +
                ", creationDate=" + creationDate +
                ", timeToLive=" + timeToLive +
                '}';
    }

    public boolean isExpired() {
        creationDate = new Date();
        long currentTime = System.currentTimeMillis();
        //converting the time to live time into milliseconds so that it matches with current time
        long expirationTime = creationDate.getTime() + (timeToLive * 1000L);
        return currentTime > expirationTime;
    }
}
