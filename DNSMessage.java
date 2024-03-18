package DNSHandling;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/*  +---------------------
    |        Header       |
    +---------------------+
    |       Question      |
    +---------------------+
    |        Answer       |
    +---------------------+
    |      Authority      |
    +---------------------+
    |      Additional     |
    +---------------------+

 */
public class DNSMessage {

    private DNSHeader header;
    private ArrayList<DNSQuestion> questions;
    private ArrayList<DNSRecord> answers;
    private ArrayList<DNSRecord> authorityRecords;
    private ArrayList<DNSRecord> additionalRecords;
    private byte[] completeMessage;
    HashMap<String, Integer> domains;

    //need to initialize with empty arrays to avoid errors
    DNSMessage() {
        questions = new ArrayList<>();
        answers = new ArrayList<>();
        authorityRecords = new ArrayList<>();
        additionalRecords = new ArrayList<>();
    }

    public static DNSMessage decodeMessage(byte[] bytes) throws IOException {
        DNSMessage dnsMessage = new DNSMessage();
        //holding onto the entire message in the completeMessage MV
        dnsMessage.completeMessage = Arrays.copyOf(bytes, bytes.length);

        //put the bytes through a byte input stream
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

        //decode the header with the DNSHeader class method
        dnsMessage.header = DNSHeader.decodeHeader(inputStream);

        //assign the questions MV by creating a new array of DNSQuestion type
        //size is using the getQuestionCount getter method in DNSHeader class
        int qSize = dnsMessage.header.getQuestionCount();
        for (int i = 0; i < qSize; i++) {
            //calling decodeQuestion method in DNSQuestion class
            dnsMessage.questions.add(DNSQuestion.decodeQuestion(inputStream, dnsMessage));
        }

        //next four sections are the same as above for different classes

        int anSize = dnsMessage.header.getAnswerCount();
        for (int i = 0; i < anSize; i++) {
            dnsMessage.answers.add(DNSRecord.decodeRecord(inputStream, dnsMessage));
        }

        int arSize = dnsMessage.header.getAuthorityCount();
        for (int i = 0; i < arSize; i++) {
            dnsMessage.authorityRecords.add(DNSRecord.decodeRecord(inputStream, dnsMessage));
        }

        int adSize = dnsMessage.header.getAdditionalCount();
        for (int i = 0; i < adSize; i++) {
            dnsMessage.additionalRecords.add(DNSRecord.decodeRecord(inputStream, dnsMessage));
        }

        //return the whole message
        return dnsMessage;
    }

    public static String[] readDomainName(InputStream inputStream) throws IOException {
        ArrayList<String> domainName = new ArrayList<>();
        //read in first byte
        int currentByte = inputStream.read();

        if(currentByte == 0) {
            //name is empty
            return new String[0];
        }

        while(currentByte > 0){
            //make a byte array to store the pieces
            byte[] pieces = new byte[currentByte];
            //read the pieces into the byte array
            inputStream.read(pieces, 0, pieces.length);

            //combine the pieces to the domainName
            String allPieces = new String(pieces, StandardCharsets.UTF_8);
            domainName.add(allPieces);
            currentByte = inputStream.read();
        }

        //return the string
        return domainName.toArray(new String[0]);
    }

    String[] readDomainName(int firstByte) throws IOException {
        //create a byte input stream starting at the passed in byte position
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(completeMessage, firstByte,
                completeMessage.length - firstByte);

        //pass the input stream into the previous readDomainName
        return readDomainName(byteArrayInputStream);
    }



    public static DNSMessage buildResponse(DNSMessage request, ArrayList<DNSRecord> answers){
        //create a new message to fill in with a response
        DNSMessage response = new DNSMessage();
        //fill in the header with the method in DNSHeader
        response.header = DNSHeader.buildHeaderForResponse(request);
        //fill in the questions (hasn't changed)
        response.questions = request.questions;
        //fill in the answers with the answers param
        response.answers = answers;
        //fill in the authority records
        response.authorityRecords = request.authorityRecords;
        //fill in the additional records
        response.additionalRecords = request.additionalRecords;;

        //return the response
        return response;
    }

    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        //use the writeBytes function from the DNSHeader class to write out the header
        header.writeBytes(outputStream);

        //initializing my hashmap of domains MV
        domains = new HashMap<>();

        //write out questions, answers, authority records, and additional records
        for (DNSQuestion question : questions) {
            question.writeBytes(outputStream, domains);
        }

        //return one answer
        //answers.get(0).writeBytes(outputStream, domains);
        for (DNSRecord answer : answers) {
            answer.writeBytes(outputStream, domains);
        }

        for (DNSRecord authorityRecord : authorityRecords) {
            authorityRecord.writeBytes(outputStream, domains);
        }

        for (DNSRecord additionalRecord : additionalRecords) {
            additionalRecord.writeBytes(outputStream, domains);
        }

        return outputStream.toByteArray();
    }

    public static void writeDomainName(ByteArrayOutputStream outputStream, HashMap<String,Integer> domainLocations, String[] domainPieces) throws IOException {
        //get the full domain name by joining the pieces
        String domainName = joinDomainName(domainPieces);

        if(domainName.length() == 0){
            outputStream.write(0);
        }

        else{
            //save the current position
            Integer location = outputStream.size();
            //add the name and location to domainLocations hash map
            domainLocations.put(domainName, location);

            //write the pieces to the ByteArrayOutputStream
            for (String s : domainPieces) {
                //write the length
                outputStream.write(s.length());
                //write the bytes
                outputStream.write(s.getBytes());
            }

            //write null byte to terminate
            outputStream.write(0);
        }
    }

    public static String joinDomainName(String[] pieces){
        return String.join(".", pieces);
    }

    public String toString(){

        return "Header: " + header + "\n" +
                "Questions: " + questions + "\n" +
                "Answers: " + answers + "\n" +
                "Authority Records: " + authorityRecords + "\n" +
                "Additional Records: " + additionalRecords + "\n" +
                "Complete Message: " + completeMessage + "\n" +
                "Domain Locations: " + domains + "\n";
    }

    //getter used in DNSHeader buildHeaderForResponse
    public DNSHeader getHeader() {
        return header;
    }

    public ArrayList<DNSQuestion> getQuestions() {
        return questions;
    }

    public ArrayList<DNSRecord> getAnswers() {
        return answers;
    }

    public void setAnswer(DNSRecord answer) {
        this.answers = new ArrayList<>();
        answers.add(answer);
    }
}