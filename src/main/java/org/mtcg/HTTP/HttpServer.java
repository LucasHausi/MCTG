package org.mtcg.HTTP;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mtcg.Cards.Package;
import org.mtcg.Cards.SimpleCard;
import org.mtcg.Cards.SimpleCardMapper;
import org.mtcg.User.User;
import org.mtcg.controller.StoreController;
import org.mtcg.controller.TokenController;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


public class HttpServer {
    //replace later with DB connection to User-Database
    HashMap<User, String> users = new HashMap<>();
    StoreController storeController = new StoreController();

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(10001)) {
            while (true) {
                try (final Socket socket = serverSocket.accept()) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    final RequestContext requestContext = parseInput(bufferedReader);
                    try {
                        //do something with the request context e.g. login, start game usw
                        processRequest(requestContext);
                    } catch (JsonProcessingException ex2) {
                        System.out.println("Error when converting the body");
                        System.err.println(ex2);
                    }


                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    sendResponse(bw, HttpStatus.OK);
                }
            }
        } catch (IOException ex1) {
            System.err.println(ex1);
        }
    }

    public void sendResponse(BufferedWriter bw, HttpStatus status) throws IOException {
        switch (status) {
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

    public void processRequest(RequestContext rc) throws JsonProcessingException {
        User u1;
        User u2;
        String sentToken;
        String path = rc.getPath();
        switch (path) {
            case ("/users"):
                u1 = new ObjectMapper().readValue(rc.getBody(), User.class);
                u2 = getUserByUsername(u1.getUsername());
                if (u2 == null) {
                    users.put(u1, null);
                    System.out.println(u1.getUsername() + " was created");

                } else {
                    System.out.println("This user does already exist!");
                }
                break;
            case ("/sessions"):
                //create dummy user
                this.users.put(new User("kienboec", "daniel"), null);

                u1 = new ObjectMapper().readValue(rc.getBody(), User.class);
                u2 = getUserByUsername(u1.getUsername());
                if (u2 == null) {
                    System.out.println("This user does not exist");
                } else {
                    if (u2.getPassword().equals(u1.getPassword())) {
                        System.out.println("Login was successful");
                        this.users.put(u2, TokenController.generateNewAuthToken());
                    } else {
                        System.out.println("Wrong password");
                    }
                }
                this.users.entrySet().forEach(entry -> {
                    System.out.println(entry.getKey().getUsername() + " " + entry.getValue());
                });
                break;
            case ("/packages"):
                //create Packages (done by an admin)
                //check if the Auth Token is valid
                ObjectMapper mapper = new ObjectMapper();
                SimpleCard[] simpleCards = mapper.readValue(rc.getBody(), SimpleCard[].class);
                SimpleCardMapper scm = new SimpleCardMapper();
                Package p = scm.mapSimpleCardsToCards(simpleCards);
                //p.print();
                storeController.store.addPackage(p);
                break;
            case ("/transactions/packages"):
                //acquire Packages
                //check if the Auth Token is valid
                if ((sentToken = rc.getAuthToken()) != null) {
                    if (checkAuthToken(sentToken)) {
                        u1 = getUserByAuthToken(sentToken);
                        storeController.sellPackage(u1);
                    } else {
                        System.out.println("Wrong authentication token");
                    }
                } else {
                    System.out.println("No authentication token");
                }
                break;
            case ("/cards"):
                //show all cards in the users stack
                //check if the Auth Token is valid
                if ((sentToken = rc.getAuthToken()) != null) {
                    if (checkAuthToken(sentToken)) {
                        u1 = getUserByAuthToken(sentToken);
                        u1.printStack();
                    } else {
                        System.out.println("Wrong authentication Token");
                    }
                } else {
                    System.out.println("No authentication token");
                }
                break;
            case ("/deck"):
                //show all cards in the users stack
                //check if the Auth Token is valid
                if ((sentToken = rc.getAuthToken()) != null) {
                    if (checkAuthToken(sentToken)) {
                        u1 = getUserByAuthToken(sentToken);
                        switch (rc.getHttpVerb()) {
                            case ("GET"):
                                //print the unconfigured deck
                                u1.printDeck();
                                break;
                            case ("PUT"):
                                storeController.sellPackage(u1);
                                storeController.sellPackage(u1);
                                storeController.sellPackage(u1);
                                //configureDeck
                                //List with Card UUIDs
                                List<UUID> cardIDs = new ArrayList<>();
                                try {
                                    JSONArray jsonArr = new JSONArray(rc.getBody());
                                    for (int i = 0; i < jsonArr.length(); i++) {
                                        cardIDs.add(UUID.fromString(jsonArr.get(i).toString()));
                                    }
                                } catch (JSONException e) {
                                    System.err.println(e);
                                }
                                if (cardIDs.size() == 4) {
                                    u1.setDeck(cardIDs);
                                    u1.printDeck();
                                } else {
                                    System.out.println("You need to provide 4 Cards to set your Deck - You have chosen: " + cardIDs.size());
                                }
                                break;
                        }
                    } else {
                        System.out.println("Wrong authentication Token");
                    }
                } else {
                    System.out.println("No authentication token");
                }
                break;
            case ("/deck?format=plain"):
                //show all cards in the users stack
                //check if the Auth Token is valid
                if ((sentToken = rc.getAuthToken()) != null) {
                    if (checkAuthToken(sentToken)) {
                        u1 = getUserByAuthToken(sentToken);
                        u1.printDeckPlain();
                    } else {
                        System.out.println("Wrong authentication Token");
                    }
                } else {
                    System.out.println("No authentication token");
                }
                break;
            default:
                if (path.contains("/users/")) {
                    String username = path.replace("/users/", "");
                    u1 = getUserByUsername(username);
                    if (u1 != null) {
                        if ((sentToken = rc.getAuthToken()) != null) {
                            if (checkAuthToken(sentToken)) {
                                u2 = getUserByAuthToken(sentToken);
                                if(u1!=u2){
                                    System.out.println("The username and the auth. token do not match!");
                                    break;
                                }
                                switch (rc.getHttpVerb()) {
                                    case ("GET"):
                                        u1.printUserData();
                                        break;
                                    case ("PUT"):
                                        try {
                                            JSONObject jsonObject = new JSONObject(rc.getBody());
                                            u1.setUserData(jsonObject.getString("Name"),jsonObject.getString("Bio"),jsonObject.getString("Image"));
                                        } catch (JSONException e) {
                                            System.err.println(e);
                                        }
                                        break;
                                }
                            } else {
                                System.out.println("Wrong authentication Token");
                            }
                        } else {
                            System.out.println("No authentication token");
                        }
                    } else {
                        System.out.println("The user does not exist!");
                    }

                } else {
                    System.out.println("No route found!" + rc.getPath());
                }
        }
        //set to null for garbage collector
        u1 = null;
        u2 = null;
    }

    //DEV Function
    public User getUserByUsername(String username) {
        return users.entrySet().stream()
                .filter(tempUser -> username.equals(tempUser.getKey().getUsername()))
                .map(Map.Entry::getKey).findFirst()
                .orElse(null);
    }

    User getUserByAuthToken(String sentToken) {
        return this.users.entrySet().stream()
                .filter(tempUser -> sentToken.equals(tempUser.getValue()))
                .map(Map.Entry::getKey).findFirst()
                .orElse(null);
    }

    boolean checkAuthToken(String sentToken) {
        return this.users.containsValue(sentToken);
    }
}
