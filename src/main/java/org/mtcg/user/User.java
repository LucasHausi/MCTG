package org.mtcg.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.mtcg.cards.Card;
import org.mtcg.cards.Package;
import org.mtcg.game.TradingDeal;
import org.mtcg.repository.PostgresUserRepository;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class User {
    private String username;
    private String password;
    private int coins;
    private Deck deck;
    private Stack cardStack;
    private String nickname;
    private String bio;
    private String image;
    private int elo;

    @JsonCreator
    public User(@JsonProperty("Username") String username, @JsonProperty("Password") String password) {
        this.username = username;
        this.password = encryptSHA512(password);
        this.coins = 20;
        this.elo = 100;
        this.cardStack = new Stack();
        this.deck = new Deck();

        //this.chooseDeck();
    }
    public User(String username, String password, int coins, String nickname, String bio, String image, int elo) {
        this.username = username;
        this.password = password;
        this.coins = coins;
        this.elo = elo;
        this.deck = new Deck();
        this.cardStack = new Stack();
        if(nickname==null){
            this.nickname = username;
        }else{
            this.nickname = nickname;
        }
        this.bio = bio;
        this.image = image;
    }
    public String getPassword() {
        return password;
    }
    public String encryptSHA512(String password){
        try {
            // getInstance() method is called with algorithm SHA-512
            MessageDigest md = MessageDigest.getInstance("SHA-512");

            byte[] messageDigest = md.digest(password.getBytes());

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            String hashtext = no.toString(16);

            // Add preceding 0s to make it 32 bit
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }

            return hashtext;
        }

        // error when generating hash
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    void processDeckSelection(String input) {
        String[] splitInput = input.split("\\s");
        int[] cardIndexes = Arrays.stream(splitInput).mapToInt(Integer::parseInt).toArray();
        this.copyCardsToDeck(cardIndexes);
    }
    boolean assertInput(String input) {
        String[] splitInput = input.split("\\s");

        //check for if enough cards are selected
        if (splitInput.length < 4) {
            System.out.println("You did not provide enough Cards try again");
            return false;
        }
        //check if the input is all numbers
        for (String splitElement : splitInput) {
            try {
                Integer.parseInt(splitElement);
            } catch (NumberFormatException ex1) {
                System.out.println(splitElement.concat(" is not a Number"));
                return false;
            }
        }
        //check if the numbers are in the correct selection range
        for (String splitElement : splitInput) {
            int intElement = Integer.parseInt(splitElement);
            if (intElement >= this.cardStack.getStackSize() || intElement < 0) {
                System.out.println(("The provided number ").concat(splitElement).concat(" does not correlate to a card"));
                return false;
            }
        }
        //check if there are duplicates
        //Sets do not allow duplicate entries, so check if adding to one is possible
        Set<String> set = new HashSet<>();
        for (String splitElement : splitInput) {
            if (set.add(splitElement) == false) {
                System.out.println("You can choose a card only one time");
                return false;
            }
        }
        return true;
    }
    public void setCardStack(Stack cardStack) {
        this.cardStack = cardStack;
    }
    public Card getCardToAttack() {
        Random rand = new Random();
        int randIndex = rand.nextInt(0, this.deck.getDeckSize());
        return this.deck.getCard(randIndex);
    }
    public boolean removeCardFromDeck(Card c) {
        return this.deck.removeCard(c);
    }
    public void removeCardFromStack(Card c){
        this.cardStack.removeCard(c);
    }
    public void addCardToStack(Card c){
        this.cardStack.addCard(c);
    }
    public Card getCardFromStack(String strCardID){
        return this.cardStack.getCard(UUID.fromString(strCardID));
    }
    public void addCardToDeck(Card c) {
        this.deck.addCard(c);
    }
    public String getUsername() {
        return username;
    }
    public void printStats(){
        System.out.println("The elo of "+this.nickname+" is: "+this.elo);
    }
    public boolean lockCard(String strCardID){
       return this.cardStack.lockCard(strCardID);
    }
    public int getElo() {
        return elo;
    }
    public void win(){
        this.elo+=3;
    }
    public void loose(){
        this.elo-=5;
    }
    public void setDeck(List<UUID> cardIDs) {
        Deck temp = new Deck();
        boolean error = false;
        for (UUID id : cardIDs) {
            Card c = this.cardStack.getCard(id);
            if (c != null) {
                temp.addCard(c);
                //evtl entfernen weils leichter ist nach einem battle die karten zu tauschen
                //this.cardStack.removeCard(c);
            }else{
                System.out.println("The user does not own the card: " + id.toString());
                return;
            }
        }
        //if no error happened add cards to real deck
        if(!error){
            this.deck = temp;
        }
    }
    public void acquirePackage(Package p) {
        boolean errWhenAddingCards = false;
        //variable to store the amount of successful purchased cards
        int succBought = 0;
        //add all Cards to Stack
        for (Card c : p.getCards()) {
            //check if the card could be added to the stack
            if (this.cardStack.addCard(c)) {
                succBought++;
            } else {
                errWhenAddingCards = true;
            }
        }

        if (errWhenAddingCards) {
            System.out.println(5 - succBought + " cards could not be added because the id was taken already");
        } else {
            System.out.println("The user " + username + " successfully acquired a package");
        }
        this.coins -= succBought;
    }
    public boolean hasEnoughMoney() {
        if (this.coins >= 5) {
            return true;
        } else {
            return false;
        }
    }
    public void printStack() {
        this.cardStack.printStack();
    }
    public void printDeck() {
        System.out.println("This is " + username + "'s deck: ");
        this.deck.printDeck("normal");
    }
    public void printDeckPlain(){
        System.out.println("Plain deck view");
        this.deck.printDeck("plain");
    }
    public void printUserData(){
        System.out.println("Name: "+ this.nickname+"\nBio: "+this.bio+"\nImage: "+this.image);
    }
    public void setUserData(String name, String bio, String image){
        this.nickname=name;
        this.bio=bio;
        this.image=image;
        PostgresUserRepository.updateUserdata(name,bio,image, this.username);
    }
    public boolean deckEmpty(){
        return deck.isEmpty();
    }

    public int getCoins() {
        return coins;
    }

    public String getNickname() {
        return nickname;
    }

    public String getBio() {
        return bio;
    }

    public String getImage() {
        return image;
    }

    // DEV Functions:
    public void printDeckSize() {
        System.out.println(this.username + " Deck Size: " + this.deck.getDeckSize());
    }

    public void copyCardsToDeck(int[] cardIndexes) {
        for (int index : cardIndexes) {
            Card c = this.cardStack.getCard(index);
            this.deck.addCard(c);
        }
        for (int i = 0; i < cardIndexes.length; i++) {
            Card c = this.cardStack.getCard(cardIndexes[i] - i);
            this.cardStack.removeCard(c);
        }
    }

    public void chooseDeck() {
        boolean finished = false;
        System.out.println(this.username.concat(" please choose the cards for your deck: "));
        this.cardStack.printStack();

        while (!finished) {
            Scanner myScan = new Scanner(System.in);
            System.out.print("Your selection: ");
            String input = myScan.nextLine();

            if (this.assertInput(input)) {
                finished = true;
                this.processDeckSelection(input);
            }
        }
    }
}
