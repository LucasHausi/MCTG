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
import org.mtcg.game.BattleLog;
import org.mtcg.game.Requirement;
import org.mtcg.game.TradingDeal;
import org.mtcg.service.CardService;
import org.mtcg.service.UserService;
import org.mtcg.user.Deck;
import org.mtcg.user.Stack;
import org.mtcg.user.User;
import org.mtcg.util.Pair;
import org.mtcg.util.Tripple;

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
    private final BlockingQueue<Tripple<User,User, BattleLog>> bqGameResults;
    private final UserService userService;
    private final CardService cardService;

    public ClientHandler(Socket clientSocket, BlockingQueue bQPlayers,BlockingQueue<Tripple<User,User, BattleLog>> bqGameResults,
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
    Response convertDeckToJSON(Deck deck){
        Response response = new Response();
        if(deck.getDeckSize()>0){
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(deck.getCards());
                response.setHttpStatus(HttpStatus.OK);
                response.setBody(json);
            } catch(Exception e) {
                System.err.println("Error when converting Stack to JSON string");
            }
        }else{
            response.setHttpStatus(HttpStatus.NO_CONTENT);
            response.setBody("The request was fine, but the deck doesn't have any cards");
        }
        return response;
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
                sendResponse(userService.loginUser(u1));
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
                        sendResponse(cardService.addPackage(p));
                    } else {
                        System.out.println("The admin token is wrong!");
                        response.setHttpStatus(HttpStatus.FORBIDDEN);
                        response.setBody("Provided user is not \"admin\"");
                        break;
                    }
                } else {
                    System.out.println("No authentication token");
                    response.setHttpStatus(HttpStatus.UNAUTHORIZED);
                    response.setBody("Access token is missing or invalid");
                    break;
                }
                break;
            case ("/transactions/packages"):
                //acquire Packages
                //check if the Auth Token is valid
                u1 = userService.authenticateUser(rc.getAuthToken());
                if (u1 != null) {
                    sendResponse(cardService.sellPackage(u1));
                }
                else{
                    response.setHttpStatus(HttpStatus.UNAUTHORIZED);
                    response.setBody("Access token is missing or invalid");
                    sendResponse(response);
                }
                break;
            case ("/cards"):
                //show all cards in the users stack
                //check if the Auth Token is valid
                u1 = userService.authenticateUser(rc.getAuthToken());
                if (u1 != null) {
                    Stack stack = u1.getCardStack();
                    if(stack.getStackSize()>0){
                        ObjectMapper objectMapper = new ObjectMapper();
                        try {
                            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(stack.getCards());
                            response.setHttpStatus(HttpStatus.OK);
                            response.setBody(json);
                            sendResponse(response);
                        } catch(Exception e) {
                            System.err.println("Error when converting Stack to JSON string");
                        }
                    }else{
                        response.setHttpStatus(HttpStatus.NO_CONTENT);
                        response.setBody("The request was fine, but the user doesn't have any cards");
                        sendResponse(response);
                    }
                }else{
                    response.setHttpStatus(HttpStatus.UNAUTHORIZED);
                    response.setBody("Access token is missing or invalid");
                    sendResponse(response);
                }
                break;
            case ("/deck"):
                //show all cards in the users stack
                //check if the Auth Token is valid
                u1 = userService.authenticateUser(rc.getAuthToken());
                if (u1 != null) {
                    switch (rc.getHttpVerb()) {
                        case ("GET"):
                            Deck deck = u1.getDeck();
                            //print the unconfigured deck
                            u1.printDeck();
                            sendResponse(convertDeckToJSON(deck));
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
                                if(u1.setDeck(cardIDs)){
                                    response.setHttpStatus(HttpStatus.OK);
                                    response.setBody("The deck has been successfully configured");
                                    sendResponse(response);
                                }else{
                                    response.setHttpStatus(HttpStatus.FORBIDDEN);
                                    response.setBody("At least one of the provided cards does not belong to the user or is not available");
                                    sendResponse(response);
                                }
                                u1.printDeck();
                            } else {
                                System.out.println("You need to provide 4 Cards to set your Deck - You have chosen: " + cardIDs.size());
                                response.setHttpStatus(HttpStatus.BAD_REQUEST);
                                response.setBody("The provided deck did not include the required amount of cards");
                                sendResponse(response);
                            }
                            break;
                    }
                }else{
                    response.setHttpStatus(HttpStatus.UNAUTHORIZED);
                    response.setBody("Access token is missing or invalid");
                    sendResponse(response);
                }
                break;
            case ("/deck?format=plain"):
                //show all cards in the users stack
                //check if the Auth Token is valid
                u1 = userService.authenticateUser(rc.getAuthToken());
                if (u1 != null) {
                    Deck deck = u1.getDeck();
                    u1.printDeckPlain();
                    response.setHttpStatus(HttpStatus.OK);
                    response.setBody(deck.getDeckinPlain());
                    sendResponse(response);
                }else{
                    response.setHttpStatus(HttpStatus.UNAUTHORIZED);
                    response.setBody("Access token is missing or invalid");
                    sendResponse(response);
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
                            Tripple<User,User,BattleLog> result = bqGameResults.take();
                            User winner = result.middle();
                            BattleLog battleLog = result.right();
                            response.setHttpStatus(HttpStatus.OK);
                            if(winner != null){
                                response.setBody("The winner is "+ winner.getUsername()+"\n");
                                //persist elo
                                userService.persistElo(u1);
                                //persist Battle
                                if(winner.getUsername() == u1.getUsername()){
                                    userService.persistBattle(battleLog,"Win",u1);
                                }else{
                                    userService.persistBattle(battleLog,"Loose",u1);
                                }

                            }else{
                                response.setBody("It was a draw"+"\n");
                                userService.persistBattle(battleLog,"Draw", u1);
                            }
                            sendResponse(response);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }else{
                        response.setHttpStatus(HttpStatus.UNAUTHORIZED);
                        response.setBody("Access token is missing or invalid");
                        sendResponse(response);
                    }
                break;
            case ("/tradings"):
                u1 = userService.authenticateUser(rc.getAuthToken());
                if (u1 != null) {
                    switch (rc.getHttpVerb()) {
                        case ("GET"):
                            sendResponse(cardService.checkTradingDeals());
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
                                sendResponse(cardService.addTradingDeal(td));
                            } catch (JSONException e) {
                                System.err.println(e);
                            }
                            break;
                    }
                }else{
                    response.setHttpStatus(HttpStatus.UNAUTHORIZED);
                    response.setBody("Access token is missing or invalid");
                    sendResponse(response);
                }
                break;
            case ("/stats"):
                u1 = userService.authenticateUser(rc.getAuthToken());
                if (u1 != null) {
                    JSONObject stats = new JSONObject();
                    try {
                        stats.put("Name",u1.getNickname());
                        stats.put("Elo",u1.getElo());
                    } catch (JSONException e) {
                        System.err.println("Error when converting stats to JSON");
                        response.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
                        response.setBody("Error when converting stats to JSON");
                    }
                    response.setHttpStatus(HttpStatus.OK);
                    response.setBody(stats.toString());
                }else{
                    response.setHttpStatus(HttpStatus.UNAUTHORIZED);
                    response.setBody("Access token is missing or invalid");
                }
                sendResponse(response);
                break;
            case ("/score"):
                sendResponse(userService.scoreBoard());
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
                                response.setHttpStatus(HttpStatus.UNAUTHORIZED);
                                response.setBody("The username and the auth. token do not match!");
                                sendResponse(response);
                                break;
                            }
                            switch (rc.getHttpVerb()) {
                                case ("GET"):
                                    u1.printUserData();
                                    JSONObject userInfo = new JSONObject();
                                    try {
                                        userInfo.put("Name",u1.getNickname());
                                        userInfo.put("Bio",u1.getBio());
                                        userInfo.put("Image",u1.getImage());
                                    } catch (JSONException e) {
                                        System.err.println("Error when converting stats to JSON");
                                        response.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
                                        response.setBody("Error when converting info to JSON");
                                    }
                                    response.setHttpStatus(HttpStatus.OK);
                                    response.setBody(userInfo.toString());
                                    sendResponse(response);
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
                        }else{
                            response.setHttpStatus(HttpStatus.UNAUTHORIZED);
                            response.setBody("Access token is missing or invalid");
                            sendResponse(response);
                            break;
                        }
                    } else {
                        System.out.println("The user does not exist!");
                        response.setHttpStatus(HttpStatus.NOT_FOUND);
                        response.setBody("User not found");
                        sendResponse(response);
                        break;
                    }
                //path to trade cards
                } else if (path.contains("/tradings/")) {
                    String strTradeID = path.replace("/tradings/", "");
                    u1 = userService.authenticateUser(rc.getAuthToken());
                    if (u1 != null) {
                        switch (rc.getHttpVerb()) {
                            case ("DELETE"):
                                sendResponse(cardService.deleteTradingDeal(UUID.fromString(strTradeID)));
                                break;
                            case ("POST"):
                                String offeredCard = rc.getBody();
                                offeredCard = offeredCard.replaceAll("\"", "");
                                sendResponse(cardService.tryToTradeCard(UUID.fromString(strTradeID), offeredCard, u1));
                                break;
                        }
                    }else{
                        response.setHttpStatus(HttpStatus.UNAUTHORIZED);
                        response.setBody("Access token is missing or invalid");
                        sendResponse(response);
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
