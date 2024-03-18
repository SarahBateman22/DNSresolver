package DNSHandling;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

/*
+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
|                                               |
|                      QNAME                    |
+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
|                      QTYPE                    |
+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
|                      QCLASS                   |
 +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 */

public class DNSQuestion {
    private String[] qName;
    private int qType;
    private int qClass;
    DNSMessage message;

    //empty constructor
    private DNSQuestion() {}

    public static DNSQuestion decodeQuestion(InputStream inputStream, DNSMessage dnsMessage) throws IOException {
        //create an object to return
        DNSQuestion dnsQuestion = new DNSQuestion();
        //get the name by calling the readDomainName method on the message
        dnsQuestion.qName = DNSMessage.readDomainName(inputStream);
        //read the next two bytes to get the type
        dnsQuestion.qType = readShort(inputStream);
        //and the next two to get the class
        dnsQuestion.qClass = readShort(inputStream);
        return dnsQuestion;
    }
    public void writeBytes(ByteArrayOutputStream outputStream, HashMap<String,Integer> domainNameLocations) throws IOException {
        //write out the name
        DNSMessage.writeDomainName(outputStream, domainNameLocations, qName);
        //write out the short
        writeShort(outputStream, qType);
        //write out the class
        writeShort(outputStream, qClass);
    }

    //helper function for reading shorts
    private static int readShort(InputStream inputStream) throws IOException {
        return (inputStream.read() << 8) | inputStream.read();
    }

    //helper function for writing shorts
    private static void writeShort(ByteArrayOutputStream outputStream, int value) {
        outputStream.write((value >> 8) & 0xFF);
        outputStream.write(value & 0xFF);
    }

    //created by IDE
    @Override
    public String toString() {
        return "DNSQuestion{" +
                "qName='" + qName + '\'' +
                ", qType=" + qType +
                ", qClass=" + qClass +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DNSQuestion question)) return false;
        return qType == question.qType && qClass == question.qClass && Arrays.equals(qName, question.qName) && Objects.equals(message, question.message);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(qType, qClass, message);
        result = 31 * result + Arrays.hashCode(qName);
        return result;
    }

}

