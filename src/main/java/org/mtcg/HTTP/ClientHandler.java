package org.mtcg.HTTP;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mtcg.cards.Package;
import org.mtcg.cards.SimpleCard;
import org.mtcg.cards.SimpleCardMapper;
import org.mtcg.controller.StoreController;
import org.mtcg.game.Game;
import org.mtcg.repository.InMemoryUserRepository;
import org.mtcg.repository.PostgresCardRepository;
import org.mtcg.repository.PostgresUserRepository;
import org.mtcg.user.User;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

public class ClientHandler implements Runnable{
    private final Socket clientSocket;
    private final InMemoryUserRepository userRepository;
    private final StoreController storeController;
    private final PostgresUserRepository postgresUserRepository;
    private final PostgresCardRepository postgresCardRepository;
    private final BlockingQueue blockingQueue;

    public ClientHandler(Socket clientSocket, InMemoryUserRepository userRepository,
                         PostgresUserRepository postgresUserRepository, PostgresCardRepository postgresCardRepository,
                         StoreController storeController, BlockingQueue blockingQueue) {
        this.clientSocket = clientSocket;
        this.storeController = storeController;
        this.userRepository = userRepository;
        this.postgresCardRepository = postgresCardRepository;
        this.blockingQueue = blockingQueue;
        this.postgresUserRepository = postgresUserRepository;
    }

    @Override
    public void run() {
        BufferedReader bufferedReader = null;
        final RequestContext requestContext;
        BufferedWriter bufferedWriter = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            requestContext = parseInput(bufferedReader);
            //do something with the request context e.g. login, start game usw
            processRequest(requestContext);

            bufferedWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            sendResponse(bufferedWriter, HttpStatus.OK);
            clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
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
                u2 = userRepository.getUserByUsername(u1.getUsername());
                if (u2 == null) {
                    userRepository.addUser(u1);
                    postgresUserRepository.addUser(u1);
                    System.out.println("The user "+
                                       u1.getUsername()+" was created");
                } else {
                    System.out.println("This user does already exist!");
                }
                break;
            case ("/sessions"):
                u1 = new ObjectMapper().readValue(rc.getBody(), User.class);
                u2 = userRepository.getUserByUsername(u1.getUsername());
                if (u2 == null) {
                    System.out.println("This user does not exist");
                } else {
                    if (u2.getPassword().equals(u1.getPassword())) {
                        System.out.println("Login was successful");
                        userRepository.addAuthToken(u2);
                    } else {
                        System.out.println("Wrong password");
                        System.out.println(u1.getPassword());
                        System.out.println(u2.getPassword());
                    }
                }
                userRepository.printIMUserRepository();
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
                PostgresCardRepository.addPackage(p);

                break;
            case ("/transactions/packages"):
                //acquire Packages
                //check if the Auth Token is valid
                if ((sentToken = rc.getAuthToken()) != null) {
                    if (userRepository.checkAuthToken(sentToken)) {
                        u1 = userRepository.getUserByAuthToken(sentToken);
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
                    if (userRepository.checkAuthToken(sentToken)) {
                        u1 = userRepository.getUserByAuthToken(sentToken);
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
                    if (userRepository.checkAuthToken(sentToken)) {
                        u1 = userRepository.getUserByAuthToken(sentToken);
                        switch (rc.getHttpVerb()) {
                            case ("GET"):
                                //print the unconfigured deck
                                u1.printDeck();
                                break;
                            case ("PUT"):
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
                    if (userRepository.checkAuthToken(sentToken)) {
                        u1 = userRepository.getUserByAuthToken(sentToken);
                        u1.printDeckPlain();
                    } else {
                        System.out.println("Wrong authentication Token");
                    }
                } else {
                    System.out.println("No authentication token");
                }
                break;
            case("/battles"):
                try {
                    if ((sentToken = rc.getAuthToken()) != null) {
                        if (userRepository.checkAuthToken(sentToken)) {
                            u1 = userRepository.getUserByAuthToken(sentToken);
                            blockingQueue.add(u1);
                        } else {
                            System.out.println("Wrong authentication Token");
                        }
                    } else {
                        System.out.println("No authentication token");
                    }
                    if(blockingQueue.stream().count()>=2){
                        User tempUser1 = (User) blockingQueue.take();
                        User tempUser2 = (User) blockingQueue.take();
                        Game round = new Game();
                        round.startGame(tempUser1, tempUser2);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                break;
            default:
                if (path.contains("/users/")) {
                    String username = path.replace("/users/", "");
                    u1 = userRepository.getUserByUsername(username);
                    if (u1 != null) {
                        if ((sentToken = rc.getAuthToken()) != null) {
                            if (userRepository.checkAuthToken(sentToken)) {
                                u2 = userRepository.getUserByAuthToken(sentToken);
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


}
