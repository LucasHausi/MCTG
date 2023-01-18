package org.mtcg.HTTP;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mtcg.cards.Package;
import org.mtcg.cards.SimpleCard;
import org.mtcg.cards.SimpleCardMapper;
import org.mtcg.game.Requirement;
import org.mtcg.game.TradingDeal;
import org.mtcg.service.CardService;
import org.mtcg.service.UserService;
import org.mtcg.user.User;
import org.mtcg.util.Pair;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final BlockingQueue<User> bQPlayers;
    private final BlockingQueue<Pair<User,User>> bqGameResults;
    private final UserService userService;
    private final CardService cardService;

    public ClientHandler(Socket clientSocket, BlockingQueue bQPlayers,BlockingQueue<Pair<User,User>> bqGameResults,
                         UserService userService, CardService cardService) {
        this.clientSocket = clientSocket;
        this.bQPlayers = bQPlayers;
        this.bqGameResults = bqGameResults;
        this.userService = userService;
        this.cardService = cardService;
    }

    @Override
    public void run() {
        BufferedReader bufferedReader = null;
        final RequestContext requestContext;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            requestContext = parseInput(bufferedReader);
            //do something with the request context e.g. login, start game usw
            processRequest(requestContext);
            clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendResponse(Response response) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            bw.write("HTTP/1.1 ");
            bw.write(response.getHttpStatus().getStatusCode() + " ");
            bw.write(response.getHttpStatus().getStatusMessage());
            bw.newLine();
            // write headers
            bw.newLine();
            bw.write(response.getBody());
            bw.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        Response response = new Response();
        switch (path) {
            case ("/users"):
                u1 = new ObjectMapper().readValue(rc.getBody(), User.class);
                sendResponse(userService.addUser(u1));
                break;
            case ("/sessions"):
                u1 = new ObjectMapper().readValue(rc.getBody(), User.class);
                userService.loginUser(u1);
                break;
            case ("/packages"):
                //create Packages (done by an admin)
                // check if the Auth Token is valid
                if ((sentToken = rc.getAuthToken()) != null) {
                    if (userService.checkAdminToken(sentToken)) {
                        ObjectMapper mapper = new ObjectMapper();
                        SimpleCard[] simpleCards = mapper.readValue(rc.getBody(), SimpleCard[].class);
                        SimpleCardMapper scm = new SimpleCardMapper();
                        Package p = scm.mapSimpleCardsToCards(simpleCards);
                        cardService.addPackage(p);
                    } else {
                        System.out.println("The admin token is wrong!");
                    }
                } else {
                    System.out.println("No authentication token");
                }
                break;
            case ("/transactions/packages"):
                //acquire Packages
                //check if the Auth Token is valid
                u1 = userService.authenticateUser(rc.getAuthToken());
                if (u1 != null) {
                    cardService.sellPackage(u1);
                }
                break;
            case ("/cards"):
                //show all cards in the users stack
                //check if the Auth Token is valid
                u1 = userService.authenticateUser(rc.getAuthToken());
                if (u1 != null) {
                    u1.printStack();
                }
                break;
            case ("/deck"):
                //show all cards in the users stack
                //check if the Auth Token is valid
                u1 = userService.authenticateUser(rc.getAuthToken());
                if (u1 != null) {
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
                }
                break;
            case ("/deck?format=plain"):
                //show all cards in the users stack
                //check if the Auth Token is valid
                u1 = userService.authenticateUser(rc.getAuthToken());
                if (u1 != null) {
                    u1.printDeckPlain();
                }
                break;
            case ("/battles"):
                    u1 = userService.authenticateUser(rc.getAuthToken());
                    if (u1 != null) {
                        if (u1.deckEmpty()) {
                            System.out.println(u1.getUsername() + "'s deck is not configured");
                        } else {
                            bQPlayers.add(u1);
                        }
                        try {
                            User winner = bqGameResults.take().right();
                            if(winner != null){
                                System.out.println(winner.getUsername()+" "+ Thread.currentThread().getName()+ " "+u1.getUsername());
                            }
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                break;
            case ("/tradings"):
                u1 = userService.authenticateUser(rc.getAuthToken());
                if (u1 != null) {
                    switch (rc.getHttpVerb()) {
                        case ("GET"):
                            cardService.checkTradingDeals();
                            break;
                        case ("POST"):
                            try {
                                JSONObject jsonObject = new JSONObject(rc.getBody());
                                TradingDeal td = new TradingDeal(   UUID.fromString(jsonObject.getString("Id")),
                                                                    jsonObject.getString("CardToTrade"),
                                                                    u1,
                                                                    new Requirement(jsonObject.getString("Type"),
                                                                            (float) jsonObject.getDouble("MinimumDamage"))
                                );
                                cardService.addTradingDeal(td);
                            } catch (JSONException e) {
                                System.err.println(e);
                            }
                            break;
                    }
                }
                break;
            case ("/stats"):
                u1 = userService.authenticateUser(rc.getAuthToken());
                if (u1 != null) {
                    u1.printStats();
                }
                break;
            case ("/score"):
                userService.scoreBoard();
                break;
            default:
                if (path.contains("/users/")) {
                    String username = path.replace("/users/", "");
                    u1 = userService.getUserByUsername(username);
                    if (u1 != null) {
                        u2 = userService.authenticateUser(rc.getAuthToken());
                        if (u2 != null) {
                            if (u1 != u2) {
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
                                        u1.setUserData(jsonObject.getString("Name"), jsonObject.getString("Bio"), jsonObject.getString("Image"));
                                    } catch (JSONException e) {
                                        System.err.println(e);
                                    }
                                    break;
                            }
                        }
                    } else {
                        System.out.println("The user does not exist!");
                    }
                //path to trade cards
                } else if (path.contains("/tradings/")) {
                    String strTradeID = path.replace("/tradings/", "");
                    u1 = userService.authenticateUser(rc.getAuthToken());
                    if (u1 != null) {
                        switch (rc.getHttpVerb()) {
                            case ("DELETE"):
                                cardService.deleteTradingDeal(UUID.fromString(strTradeID));
                                break;
                            case ("POST"):
                                String offeredCard = rc.getBody();
                                offeredCard = offeredCard.replaceAll("\"", "");
                                cardService.tryToTradeCard(UUID.fromString(strTradeID), offeredCard, u1);
                                break;
                        }
                    }
                } else {
                    System.out.println("No route found!" + rc.getPath());
                }
                //set to null for garbage collector
                u1 = null;
                u2 = null;
        }
    }
}
