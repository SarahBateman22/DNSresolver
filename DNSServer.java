package DNSHandling;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DNSServer {
    private static final int SERVER_PORT = 8053;
    static DNSCache cache;
    static ArrayList<DNSRecord> answers;

    public void startServer() throws IOException {
        System.out.println("Listening on port: " + SERVER_PORT);

        cache = new DNSCache();
        answers = new ArrayList<>();

        DatagramSocket socket = new DatagramSocket(SERVER_PORT);

        //listen for incoming requests forever
        while (true) {
            //make a byte array to hold the incoming message data
            byte[] buffer = new byte[1024];

            //create a datagram packet
            DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length);

            //get the request packet through the socket
            socket.receive(requestPacket);

            //decode the message
            DNSMessage requestMessage = DNSMessage.decodeMessage(requestPacket.getData());

            //create a message type to respond with
            DNSMessage responseMessage = new DNSMessage();

            //parse each question from the request message
            ArrayList<DNSQuestion> questions = requestMessage.getQuestions();
            for (DNSQuestion q : questions) {
                //pull the record from the cache
                DNSRecord cRecord = DNSCache.get(q);

                //check that the record is not null and not expired
                if (cRecord != null && !cRecord.isExpired()) {
                    //add cached record to response
                    responseMessage = DNSMessage.buildResponse(requestMessage,  new ArrayList<DNSRecord>(List.of(cRecord)));

                } else {
                    byte[] requestBytes = requestMessage.toBytes();
                    responseMessage = sendRequestToGoogle(socket, requestBytes, q);
                }
            }
            //send response back to client
            //convert the message to bytes
            byte[] responseData = responseMessage.toBytes();
            //make a packet with the data
            DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length,
                    requestPacket.getAddress(), requestPacket.getPort());
            //send it!
            socket.send(responsePacket);
        }
    }

    static DNSMessage sendRequestToGoogle(DatagramSocket socket, byte[] messageBytes, DNSQuestion question) throws IOException {
        //get the address of Google
        InetAddress googleDNS = InetAddress.getByName("8.8.8.8");

        //create the request packet
        DatagramPacket requestPacket = new DatagramPacket(messageBytes, messageBytes.length, googleDNS, 53);

        //send it to Google
        socket.send(requestPacket);
        System.out.println("Sent request to Google");

        //create a byte array to receive the response in
        byte[] buffer = new byte[1024];

        //create a datagram packet to receive it by passing in the buffer
        DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
        socket.receive(responsePacket);
        System.out.println("Received: " + responsePacket);

        //decode the message to cache answers
        DNSMessage response = DNSMessage.decodeMessage(responsePacket.getData());
        for (DNSRecord answer : response.getAnswers()) {
            DNSCache.put(question, answer);
        }

        return response;
    }

}