package DNSHandling;

import java.io.*;

/*
                                   1  1  1  1  1  1
     0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                      ID                       |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |QR|   Opcode  |AA|TC|RD|RA|   Z    |   RCODE   |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                    QDCOUNT                    |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                    ANCOUNT                    |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                    NSCOUNT                    |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                    ARCOUNT                    |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 */

public class DNSHeader {

    private int id; //16-bit ID field
    private int flags; //holds all the flags
    private int questionCount; //number of entries in the question section
    private int answerCount; //number of entries in the answer section
    private int authorityCount; //number of entries in the authority section
    private int additionalCount; //number of entries in the additional sectiom

    DNSHeader() {}


    public static DNSHeader decodeHeader(InputStream inputStream) throws IOException {
        //making an empty header
        DNSHeader header = new DNSHeader();
        //first two bytes are the id
        header.id = readTwoBytes(inputStream);
        //next two bytes are all the flags (combined)
        header.flags = readTwoBytes(inputStream);
        //continue reading two bytes to get the QDcount, ANcount, NScount, and ARcount
        header.questionCount = readTwoBytes(inputStream);
        header.answerCount = readTwoBytes(inputStream);
        header.authorityCount = readTwoBytes(inputStream);
        header.additionalCount = readTwoBytes(inputStream);

        //return parsed header
        return header;
    }

    public static DNSHeader buildHeaderForResponse(DNSMessage request){
        //create a new header to return with the request header
        DNSHeader header = request.getHeader();
        //call the getHeader() method from DNSMessage class which will return a DNSHeader to call getID on
        //this might not be necessary because the ID should be the same
        header.id = request.getHeader().getId();
        //set the QR bit to 1 to show that it is a response message
        header.flags = (1 << 15);
        //set answer count to 1 to show we replied
        header.answerCount = 1;

        return header;
    }

    public void writeBytes(OutputStream outputStream) throws IOException {
        //write out the 12 bytes in order
        writeTwoBytes(outputStream, id);
        writeTwoBytes(outputStream, flags);
        writeTwoBytes(outputStream, questionCount);
        writeTwoBytes(outputStream, answerCount);
        writeTwoBytes(outputStream, authorityCount);
        writeTwoBytes(outputStream, additionalCount);

    }

    public String toString(){
        return "ID: " + id +
                ", Flags: " + flags +
                ", QDCount: " + questionCount +
                ", ANCount: " + answerCount +
                ", NSCount: " + authorityCount +
                ", ARCount: " + additionalCount;
    }

    //helper methods to read and write two bytes at a time
    private static int readTwoBytes(InputStream inputStream) throws IOException {
        //.read method reads one byte then moves the pointer forward
        int byte1 = inputStream.read();
        int byte2 = inputStream.read();
        //putting the two bytes back together
        return (byte1 << 8) | byte2;
    }

    private static void writeTwoBytes(OutputStream outputStream, int value) throws IOException {
        //writing the two bytes out individually
        outputStream.write((value >> 8) & 0xFF);
        outputStream.write(value & 0xFF);
    }

    //getters
    public int getId() {
        return id;
    }

    public int getQuestionCount() {
        return questionCount;
    }

    public int getAnswerCount() {
        return answerCount;
    }

    public int getAuthorityCount() {
        return authorityCount;
    }

    public int getAdditionalCount() {
        return additionalCount;
    }


}
