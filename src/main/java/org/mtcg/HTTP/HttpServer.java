package org.mtcg.HTTP;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mtcg.Cards.Package;
import org.mtcg.Cards.SimpleCard;
import org.mtcg.Cards.SimpleCardMapper;
import org.mtcg.Game.Store;
import org.mtcg.User.User;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class HttpServer {
    //replace later with DB conection to Userdatabase
    Set<User> users = new HashSet<>();
    Store store = new Store();
    public void start(){
        try(ServerSocket serverSocket = new ServerSocket(10001)){
            while(true){
                try(final Socket socket = serverSocket.accept()){
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    final RequestContext requestContext = parseInput(bufferedReader);

                    //requestContext.print();
                    try {
                        //do something with the request context e.g. login, start game usw
                        processRequest(requestContext);
                    }catch (JsonProcessingException ex2){
                        System.out.println("Error when converting the body");
                        System.err.println(ex2);
                    }


                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    sendResponse(bw, HttpStatus.OK);
                }
            }
        }catch (IOException ex1){
            System.err.println(ex1);
        }
    }

    public void sendResponse(BufferedWriter bw, HttpStatus status) throws IOException {
        switch (status){
            case OK -> bw.write("HTTP/1.1 200 OK");
            case CREATED -> bw.write("HTTP/1.1 201 Created");
            case NO_CONTENT -> bw.write("HTTP/1.1 204 No Content");
            case BAD_REQUEST -> bw.write("HTTP/1.1 400 Bad Request");
            case UNAUTHORIZED -> bw.write("HTTP/1.1 401 Unauthorized");
            case FORBIDDEN -> bw.write("HTTP/1.1 403 Forbidden");
            case INTERNAL_SERVER_ERROR -> bw.write("HTTP/1.1 500 Internal Server Error");
        }
        bw.newLine();
        bw.flush();
    }
    public RequestContext parseInput(BufferedReader br) throws IOException {

        RequestContext requestContext = new RequestContext();
        String versionString = br.readLine();

        final String[] splitVersionString = versionString.split(" ");
        requestContext.setHttpVerb(splitVersionString[0]);
        requestContext.setPath(splitVersionString[1]);

        List<Header> headerList = new ArrayList<>();
        HeaderParser headerParser = new HeaderParser();
        String input;
        do {
            input = br.readLine();
            if (input.equals("")) {
                break;
            }
            headerList.add(headerParser.parseHeader(input));
        } while (true);
        requestContext.setHeaders(headerList);

        int contentLength = requestContext.getContentLength();
        char[] buffer = new char[contentLength];
        br.read(buffer, 0, contentLength);
        requestContext.setBody(new String(buffer));
        return requestContext;
    }

    User getUserByUsername(String username){
        return users.stream()
                .filter(tempUser -> username.equals(tempUser.getUsername()))
                .findFirst()
                .orElse(null);
    }

    public void processRequest(RequestContext rc) throws JsonProcessingException {
        User u1;
        User u2;
        switch (rc.getPath()){
            case("/users"):
                u1 = new ObjectMapper().readValue(rc.getBody(), User.class);
                u2 = getUserByUsername(u1.getUsername());
                if(u2 == null){
                    users.add(u1);
                    System.out.println(u1.getUsername()+" was created");
                }else {
                    System.out.println("This user does already exist!");
                }
                break;
            case("/sessions"):
                //create dummy to login
                //users.add(new User("kienboec","daniel"));

                u1 = new ObjectMapper().readValue(rc.getBody(), User.class);
                u2 = getUserByUsername(u1.getUsername());
                if(u2==null){
                    System.out.println("This user does not exist");
                }else{
                    if(u2.getPassword().equals(u2.getPassword())){
                        System.out.println("Login was successful");
                    }
                }
                break;
            case("/packages"):
                //create Packages (done by an admin)
                //check if the Auth Token is valid
                ObjectMapper mapper = new ObjectMapper();
                SimpleCard[] simpleCards = mapper.readValue(rc.getBody(), SimpleCard[].class);
                SimpleCardMapper scm = new SimpleCardMapper();
                Package p = scm.mapSimpleCardsToCards(simpleCards);
                //p.print();
                store.addPackage(p);
                break;
            case("/transactions/packages"):
                //acquire Packages
                //check if the Auth Token is valid

        }
        //set to null for garbage collector
        u1=null;
        u2=null;
    }
}
