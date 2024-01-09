/**
 *  This main class creates and initialises all the others: it creates all
 *  rooms, creates the parser and starts the game.  It also evaluates and
 *  executes the commands that the parser returns.
 * 
 * @author Lívia Mendes e Paulo Moura
 * @version 2024.01.08
 */
import java.util.*;
public class Game 
{
    private Parser parser;
    private Room startingRoom;
    private Player player;
    private Mime mime;
    private Random probability = new Random();
    private boolean wantToQuit;
    private ArrayList<Room> rooms = new ArrayList<>();
    private ArrayList<Ending> endings = new ArrayList<>();
    private int endingIndex;
    private String gameEnding;
        
    /**
     * Create the game and initialise its internal map.
     */
    public Game() 
    {
        createRooms();
        createEndings();
        parser = new Parser();
        endingIndex = probability.nextInt(0, endings.size()); //not working, needs to be fixed
        gameEnding = endings.get(endingIndex).getEnding(endingIndex);
    }

    /*
     * Create game endings
     */
    private void createEndings() {
        Ending end1, end2, end3;

        end1 = new Ending("you wake up in your bed, it was all a dream");
        end2 = new Ending(
                "you wake up in your bed, it was all a nightmare \n until you look into the mirror and you don't see yourself \n you see the mime");
        end3 = new Ending(
                "you see the mime sitting in a cute chair while drinking a little tea \n and it kindly offers you some");

        endings.add(end1);
        endings.add(end2);
        endings.add(end3);
    }

    /**
     * Create all the rooms, link their exits together, create and add its items and/or characters.
     */
    private void createRooms()
    {
        Room room1, room2, room3, room4, endRoom;
      
        // create the rooms
        room1 = new Room("at the lounge", true, false, false);
        room2 = new Room("at the living room", false, false,false);
        room3 = new Room("at the kitchen", false, false, false);
        room4 = new Room("at the bedroom", false, false, false);
        endRoom = new Room(gameEnding, false, true, true);

        rooms.add(room1);
        rooms.add(room2);
        rooms.add(room3);
        rooms.add(room4);
        rooms.add(endRoom);

        // initialise room exits
        room1.setExit("south", endRoom);

        room1.setExit("north", room2);
        room2.setExit("south", room1);
        room2.setExit("east", room3);
        room3.setExit("west", room2);
        room3.setExit("south", room4);
        room4.setExit("north", room3);
        room4.setExit("south", room1);



        //add items
        room1.addItem("shotgun", "a lightweight short-range shotgun", 0.632, 6, 100.0, 1);
        room1.addItem("grenade", "a grenade", 0.210, 1, 100000, 3);
        room1.addItem("knife", "a tactical knife", 0.04, 1000, 25, 1);
        room1.addItem("ammunition", "a six bullet ammunition clip, use wisely", 0.5, 6, 0, 2);
        room1.addItem("mirror", "a oval shaped mirror, you can use it to confuse the mime", 0.1, 3, 500, 4);


        //add mime
        mime = new Mime(room4); // creates the mime and adds it to initial room

        startingRoom = room1;  // start game outside
    }

    /**
     *  Main play routine.  Loops until end of play.
     */
    public void play() 
    {
        player = new Player(startingRoom);
        printWelcome();

        // Enter the main command loop.  Here we repeatedly read commands and
        // execute them until the game is over.
                
        boolean finished = false;
        while (! finished) {
            Command command = parser.getCommand();
            finished = processCommand(command);
        }
        System.out.println("Thank you for playing.  Good bye.");
    }

    /**
     * Print out player's current room information(description and/or items, exits and characters).
     */

    private void printLocationInfo() { //tarefa 2
        if (player.getCurrentRoom().isFinal()) {
            System.out.println(player.getCurrentRoom().getDescription());
        } else {
            System.out.println(player.getCurrentRoom().getLongDescription());
            System.out.println();
        }
    }

    /**
     * Print out the opening message for the player.
     */
    private void printWelcome()
    {
        System.out.println();
        System.out.println("Welcome to The Mime!");
        System.out.println(
                "You are a SCP agent and it's your duty to investigate this house due to reports of abnormal activity");
        printHelp();
        System.out.print("Type 'help' if you need to see which commands you have again.");
        printLocationInfo();//tarefa 2
    }

    /**
     * Given a command, process (that is: execute) the command.
     * @param command The command to be processed.
     * @return true If the command ends the game, false otherwise.
     */
    private boolean processCommand(Command command) 
    {
        wantToQuit = false;

        if(command.isUnknown()) {
            System.out.println("I don't know what you mean...");
            return false;
        }

        String commandWord = command.getCommandWord();
        if (commandWord.equals("help")) {
            printHelp();
        }
        else if (commandWord.equals("go")) {
            goRoom(command);
        }
        else if (commandWord.equals("quit")) {
            wantToQuit = quit(command);
        }
        else if(commandWord.equals("look")){
            look(command);//tarefa 8
        }
        else if (commandWord.equals("inventory")) {
            inventory(command);
        }
        else if (commandWord.equals("take")) {
            take(command);
        }
        else if (commandWord.equals("drop")) {
            drop(command);
        }
        else if (commandWord.equals("wield")) {
            wieldItem(command);
        }
        else if (commandWord.equals("attack")) {
            if (mime.getCurrentRoom().equals(player.getCurrentRoom())) {
                attack(command);
            } else {
                System.out.println("there's nothing to attack here");
            }
        }else if(commandWord.equals("use")){
            use(command);
        }
        return wantToQuit;
    }

    // implementations of user commands:

    /**
     * Print out some help information.
     * Here we print some stupid, cryptic message and a list of the 
     * command words.
     */
    private void printHelp() 
    {
        System.out.println("Your command words are:");
        parser.showCommands();
    }
    /** 
     * Try to go in one direction. If there is an exit, enter
     * the new room, otherwise print an error message.
     */
    private void goRoom(Command command) 
    {
        if (!command.hasSecondWord()) {
            // if there is no second word, we don't know where to go...
            System.out.println("Go where?");
            return;
        }
        String direction = command.getSecondWord();
        // Try to leave current room.
        Room nextRoom = null;
        if (direction.equals("back")) { //tarefa 13
            if (player.getCurrentRoom().isInitial()) {
                System.out.println("You can't go back from here");
            } else {
                player.goBackRoom();
            }
        } else {
            nextRoom = player.getCurrentRoom().getExit(direction);//tarefa 3
            if (nextRoom == null) {
                System.out.println("There is no door!");
            } else {
                if(mime.getCurrentRoom().equals(player.getCurrentRoom())){
                    mime.metPlayer();
                }
                player.move(nextRoom);
                if (mime.getHealth() > 0) {
                    mime.move();
                }
                if (player.getCurrentRoom().isInitial()) {
                    System.out.println("You hear the door lock itself behind you. You can't go back from here");
                }
                if (player.getCurrentRoom().isFinal()) {
                    wantToQuit = true;
                }
            }
        }
        printLocationInfo();
    }
    
    /*
     * Type attack to attack the Mime
     * by default, uses the player's currently wielded item to do so but it has to be either a firearm or a knife,
     * both of which behave the same way (with a few exceptions)
     */
    private void attack(Command command) {
        if (command.hasSecondWord()) {
            System.out.println("i know it's to attack the mime, you don't need to tell me");
        }
        if (player.getWieldingItem() != null) { //checks whether the player is wielding an item
            if (player.getWieldingItem().getItemCode() != 1) {
                System.out.println("you need a firearm to attack");
            } else {
                getBehavior(1);
                double prob = Math.abs(probability.nextGaussian()) / 2;
                if (prob > mime.getDefense()) {
                    if (player.getAttackStreak() > 2 && player.getAttackStreak() <= 3) {
                        mime.takeDamage(player.getWieldingItem().getDamage() * player.getAttackStreak());
                    } else {
                        mime.takeDamage(player.getWieldingItem().getDamage());
                    }
                    player.addAttackStreak();// adds 1 to the attackStreak every time the player successfully attacks the mime
                    System.out.println("You attacked the mime.");
                } else {
                    System.out.println("You missed.");
                    player.resetAttackStreak();
                }
                try {
                    Thread.sleep(500); //tempo que o mime "espera" antes de atacar o player
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (mime.getHealth() > 0) {
                    prob = Math.abs(probability.nextGaussian());
                    if (prob > player.getDefense()) {
                        mime.addAttackStreak(); //adds 1 to the mime's attack streak every time it successfully attacks the player
                        System.out.println("The mime attacked you.");
                        player.takeDamage(mime.getAttack());
                    } else {
                        System.out.println("the mime tried to attack you, but missed");
                        mime.resetAttackStreak();
                    }
                }
                System.out.println(allHealthStatus());
                if (mime.getHealth() == 0) {
                    playerWon();
                }
                if (player.getHealth() == 0) {
                    playerDied();
                }
            }
        } else {
            System.out.println("You can't attack without wielding anything.");
        }
    }

    /*
     * command for all non-attacking item
     * such as ammunition clip, mirror, food, etc
     * player doesn't have to necessarily wield the item use unless defined in the item's behavior
     */
    private void use(Command command){
        if (!command.hasSecondWord()) {
            System.out.println("use what?");
        } else {
            String itemName = command.getSecondWord();
            if (player.ownsItem(itemName)) {
                if (player.getItem(itemName).getItemCode() != 1) {
                    getBehavior(player.getItem(itemName).getItemCode());
                } else {
                    System.out.println("type attack to use a firearm or a knife, but you need to wield it first");  
                }
                
            }
        }
    }

    /*
     * Command to wield an item from the player's inventory
     */
    private void wieldItem(Command command) {
        if (!command.hasSecondWord()) {
            System.out.println("wield what?");
        } else {
            String itemName = command.getSecondWord();
            player.setWieldingItem(itemName);
            if (player.getWieldingItem().getItemName().equals(itemName)) {
                System.out.println("you are currently wielding "+itemName);
            }
        }
    }

    /** 
     * "Quit" was entered. Check the rest of the command to see
     * whether we really quit the game.
     * @return true, if this command quits the game, false otherwise.
     */
    
    private boolean quit(Command command) 
    {
        if (command.hasSecondWord()) {
            System.out.println("Quit what?");
            return false;
        } else {
            return true; // signal that we want to quit
        }
    }
    
    /*
     * repeats the room description
     */
    private void look(Command command) {
        if (command.hasSecondWord()) {
            System.out.println("look what?");
        } else {
            System.out.println(player.getCurrentRoom().getLongDescription());
        }
    }

    /*
     * prints inventory list
     */
    private void inventory(Command command) {
        if (command.hasSecondWord()) {
            System.out.println("inventory what?");
        } else {
            player.showInventory();
        }
    }

    /*
     * adds item to the player's inventory
     * if the item specified in the argument exists in the player's current room
     * removing it from the room once its taken
     */
    private void take(Command command) {
        if (!command.hasSecondWord()) {
            System.out.println("take what?");
        } else {
            String itemName = command.getSecondWord();
            if (player.getCurrentRoom().itemExists(itemName)) {
                if (!player.ownsItem(itemName)) {
                    player.addToInventory(itemName, player.getCurrentRoom().getItem(itemName));
                    player.setWieldingItem(itemName);
                    player.getCurrentRoom().removeItem(itemName);
                    System.out.println(itemName + " has been added to your inventory");
                }
            } else {
                System.out.println("this item is not in this room");
            }
        }
    }

    /*
     * adds item to the room
     * if the item specified in the argument exists in the player's inventory
     * removing it from the inventory once its dropped
     */
    private void drop(Command command) {
        if (!command.hasSecondWord()) {
            System.out.println("drop what?");
        } else {
            String itemName = command.getSecondWord();
            if (player.ownsItem(itemName)) {
                Item item = player.getItem(itemName);
                player.removeFromInventory(itemName);
                player.getCurrentRoom().addItem(itemName, item);
                System.out.println(itemName + " has been removed to your inventory");
            } else {
                System.out.println("this item is no longer in your inventory");
            }
        }
    }

    /*
     * @returns the character/player's healthbar in a graphic form
     */
    private String healthBar(double health) {
        String healthBar = "[";
        for (int i = 0; i <= 1000; i += 100) {
            if (health > i) {
                healthBar += /*"#"*/ '\u25A0';//troquei o # por um ■ pra ficar mais bonitinho - livia
            } else {
                healthBar += " ";
            }
        }
        healthBar += "]";
        return healthBar;
    }

    /*
     * takes the graphic from healthBar() and returns it to be printed
     * during combat
     */
    private String allHealthStatus() {
        return "Your health:   " + healthBar(player.getHealth()) + "\n" + "Mime's health: " + healthBar(mime.getHealth()) + "\n";
    }
    
    /*
     * message deployed if the player dies during combat
     * quits the game
     */
    private void playerDied() {
        System.out.println("you died");
        wantToQuit = true;
    }
    
    /*
     * if the player defeats the Mime, it drops a key to the locked door for the player
     * which results in one of the endings created in createEndings()
     */
    private void playerWon() {
        System.out.println("you've sucessfully defeated the mime and it may have dropped something for you, take a look");
        Item key = new Item("key", "a golden key appeared on the floor, you can use it to unlock a certain door", 0.05, 1, 0, 5);
        player.getCurrentRoom().addItem("key", key);
    }

    /*
     * where each item has its behavior defined and assigned by its code
     */
    public void getBehavior(int itemCode){
        switch(itemCode){
            case 1: //firearms and knives
                player.getWieldingItem().changeLifespan(-1);
                break;
            case 2: //ammunition clip
                if (player.getWieldingItem().getItemCode() == 1) {
                    if (player.getWieldingItem().getItemLifespan() > 0) {
                        System.out.println("you don't need to reload now, save your ammunition for later");
                    } else {
                        player.getWieldingItem().changeLifespan(6);
                        player.getItem("ammunition").changeLifespan(-6);
                        player.removeFromInventory("ammunition");
                        System.out.println("your "+player.getWieldingItem().getItemName()+" has been reloaded");
                    }
                }
                break;
            case 3: //grenade
                if (player.getWieldingItem().getItemCode() == itemCode) {
                    System.out.println("you're a dummy and you exploded yourself. congratulations!");
                    player.removeFromInventory(player.getWieldingItem().getItemName());
                    playerDied();
                } else {
                    System.out.println("you need to wield this item to use it");
                }
                break;
            case 4: //flashlight and mirror
                if (player.getWieldingItem().getItemCode() == itemCode) {
                    if (player.getCurrentRoom().equals(mime.getCurrentRoom())) {
                        double x = player.getWieldingItem().getDamage() / 1000;
                        player.getWieldingItem().changeLifespan(-1);
                        mime.setDefense(-x);
                        System.out.println("it looks like the mime got dizzy, attacking it might be easier now");
                        if (player.getWieldingItem().getItemLifespan() == 0) {
                            player.removeFromInventory(player.getWieldingItem().getItemName());
                            System.out.println(player.getWieldingItem().getItemName() + " broke");
                            player.setWieldingItem(null);
                        }
                    } else {
                        System.out.println("chill out, the mime isn't here right now");
                    }
                } else {
                    System.out.println("you need to wield this item to use it");
                }
                break;
            case 5: //key
                if (player.getWieldingItem().getItemCode() == itemCode) {
                    for (Room room : rooms) {
                        if (room.isLocked()) {
                            room.unlockRoom();
                        }
                    }
                    player.removeFromInventory(player.getWieldingItem().getItemName());
                } else {
                    System.out.println("you need to wield this item to use it");
                }
                break;
            default:
                attack(null);
        }
    }

}
