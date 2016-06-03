package Models;

import play.Logger;

import java.security.SecureRandom;
import java.util.*;


/**
 * Created by Davin on 07/05/2016.
 */
public class GameInstance {

    private Map playerList;
    private IActionController actionController;
    private int gameID;
    private int gameStage;
    private int playerCount;
    private int roundActionCount;
    private int waitingPolls;
    private GameStatus gameStatus;
    private boolean started;

    // On instantiate should fill with AI players
    // Adding players should replace AI players (isAssigned == false)
    // After each assign() check if unassigned players left
    // If not, stop waiting and start game.
    public GameInstance(int gameID, IActionController actionController){
        this.playerList = new HashMap(4);
        this.actionController = actionController;
        this.gameID = gameID;
        this.playerCount = 0;
        this.gameStage = 0;
        this.waitingPolls = 0;
        this.roundActionCount = 0;
        this.gameStatus = GameStatus.WAITING;
        this.started = false;
        fillGame();
        // waitForPlayers();
    }

    public void fillGame(){
        // fill game with AI players
        for (int i = 0; i < 5; i++){
            Player player = new Player(i, gameID, 0);
            playerList.put(i, player);
        }
    }

    public void waitForPlayers(){
        // wait 30 seconds then start game
        // using TimerTask or Akka actors?

    }
    // trigger each AI player to select an action based on game stage
    public void aiPlayerActions(){
        for (int i = playerCount; i < 5 ; i++){
            //playerList.get(i).takeAction(gameStage);
        }
    }


    public boolean isRunning(){
        return started;
    }
    public boolean isWaiting() {return roundActionCount < playerCount; }
    public boolean allActionsIn() {
        return playerCount == roundActionCount;
    }
    public boolean isFinished(){
        return gameStage > 4;
    }

    public Player addPlayer(int clientID){

        Player player = new Player(playerCount, gameID, clientID);
        playerList.put(playerCount, player);
        // increment playerCount only if player is assigned client
        if (clientID != 0){
            playerCount++;
        }
        if (playerCount == 4){
            this.started = true;
        }
        return player;
    }

    // adds the given action to the relevant player and adds up the counts
    // when the counts equal the playerCount that means all players have acted and CPU players can act
    public PlayerAction addAction(int playerID, PlayerAction playerResponse){
        // needs to change to return PlayerAction
        // check if response is ready, if so return it
        // if legit action return waiting action

        Player player = getPlayer(playerID);
        PlayerAction actionReply;
        String responseID = playerResponse.getAction_id();
        Logger.debug("addAction triggered by: " + responseID + ". player Count: " + playerCount
                + ". isRunning: " + isRunning());
        // rebuff waiting action unless round response is ready
        if (responseID.equals("waiting_for_players")){
            if (player.readyCheck()){
                Logger.debug("roundResponse popped");
                actionReply = player.popRoundResponse();
            } else {
                actionReply = actionController.waitingAction();
            }
        } else {
            // check that player has not already acted this round
            if (player.actionsTakenNo() <= gameStage){
                player.addAction(gameStage, playerResponse);
                roundActionCount++;
                Logger.debug(" action added. player actions taken: " + player.actionsTakenNo()
                        + " game stage: " + gameStage);
                Logger.debug(" All actions in: " + allActionsIn()
                        + " isRunning: " + isRunning() + ". Round action count: " + roundActionCount);
                // if all players have acted and game has started
                // calculate AI actions and prepare next round
                if (allActionsIn() && isRunning()){
                    calculateRoundEnd();
                    Logger.debug("calculate round end triggered by addAction()");
                }
            }
            actionReply = actionController.waitingAction();
        }
        return actionReply;
    }

    public void calculateRoundEnd(){
        Logger.debug("calculating round end");
        // if gameStage == 4 : calculate witchfinder's actions
        // for each player in playerList if clientID == 0 (CPU):
        // select a random pAction from masterList(where is list?) and add it to each player
        for (Object player: playerList.values()){
            Player pl = (Player) player;
            if (pl.getPlayerClientId() == 0){
                pl.addAction(gameStage, actionController.randomPlayerAction(gameStage));
            }
        }
        // take action for each player

        for (int i = 0; i < 4; i++) {
            takeAction(gameStage, i);
        }
        // a final parsing iteration to add in players that visited

        // for each player where clientID != 0:
        // put playeraction from parseActionReply to roundResponse
        roundActionCount = 0;
        gameStage++;
        // all active players have submitted their actions for the round
        // select actions for each CPU player
        // this method needs to implement each action for the round (increment suspicion, goodwill etc)
        // create responseActions and make them available when clients poll service.

        // possible to inject an ActionController interface into the Player objects and have it carry out actions
    }

    public List<PlayerAction> calculateWitchFinderAction(){
        List<Player> suspicionList = new ArrayList<>();
        int highest = 0;
        // get player(s) with highest suspicion
        for (int i = 0; i < playerList.size(); i++){
            Player suspiciousPlayer = (Player)playerList.get(i);
            int suspicion = suspiciousPlayer.getSuspicion();
            if (suspicion > highest){
                highest = suspicion;
            }
        }
        // if list not empty
        if (highest > 0){
            for (int i = 0; i < playerList.size(); i++){
                Player suspiciousPlayer = (Player)playerList.get(i);
                if (suspiciousPlayer.getSuspicion() >= highest){
                    suspicionList.add(suspiciousPlayer);
                }
            }
        } else {
            // select random because there was no one drawing the WF suspicion (no sus > 0)
            suspicionList.add((Player)playerList.get(actionController.randomPlayerID()));
        }
        // if more than one player on suspicionList
        if (suspicionList.size() > 1){
            // deduct goodWill and check again
            int highestSusp = suspicionList.get(0).suspicionLessGoodWill();
            for (int i = 1; i < suspicionList.size(); i++){
                if (suspicionList.get(i).suspicionLessGoodWill() > highestSusp){
                    highestSusp = suspicionList.get(i).suspicionLessGoodWill();
                }
            }
            // if player has less suspicion after goodwill remove them from list
            for (int i = 1; i < suspicionList.size(); i++){
                if (suspicionList.get(i).suspicionLessGoodWill() < highestSusp){
                    suspicionList.remove(i);
                }
            }
        }

        List<PlayerAction> actionList = new ArrayList<>();
        actionList = parseWitchFinderAction(suspicionList);

        return actionList;
    }

    // method to apply the specific action chosen by a player
    public void takeAction(int roundNo, int playerID){
        Logger.debug("takeAction started");
        //Random random = new Random();
        List<Integer> numbers = new ArrayList<>(Arrays.asList(0,1,2,3));
        numbers.remove(playerID);
        Collections.shuffle(numbers, new SecureRandom());
        int otherPlayerID = numbers.get(0);
        Logger.debug("random number calculated: " + otherPlayerID);
        Player actingPlayer = getPlayer(playerID);
        Logger.debug("actingPlayer: " + actingPlayer.getPlayerID());
        // select random player to ask favor of
        Player otherPlayer = getPlayer(otherPlayerID);
        Logger.debug("otherPlayer: " + otherPlayer.getPlayerID() );
        // check if they are there
        boolean present = otherPlayer.visitMe(roundNo);
        Logger.debug("otherPlayer present: " + present);
        PlayerAction takenAction = actingPlayer.findActionByRound(roundNo);

        String takenActionID = takenAction.getAction_id();
        Logger.debug("actingPlayer's action: " + takenActionID);

        switch (takenActionID){
            case "stay_home":
            case "do_nothing":
                break;
            case "ask_favour":
                if (present){
                    // record that they owe selected player
                    otherPlayer.incrementOwedFavors();
                    actingPlayer.addFavourOwed(otherPlayerID);
                    // increment player goodwill
                    actingPlayer.incrementGoodWill();
                } else { // if not
                    // increment Suspicion of asked Player
                    otherPlayer.incrementSuspicion();
                }
                break;
            case "visit_player":
                if (present){ // if so
                    otherPlayer.incrementSuspicion();
                }
                break;
            case "meet_witchfinder":
                otherPlayer.incrementSuspicion();
                break;
            case "plant_evidence":
                if (present){
                }else {
                    otherPlayer.incrementSuspicion();
                }
                break;
            case "repay_favour":
                // if favour owed repay it and return id of repaid player
                // if no player owed: returns -1
                otherPlayerID = actingPlayer.repayFavour();
                break;
            default:
                break;

        }
        if (actingPlayer.isAssigned()){
            Logger.debug("about to parse player action");
            actingPlayer.putRoundResponse(parseActionReply(otherPlayerID, takenActionID, present));
            Logger.debug("player action parsed");
        }
    }

    public PlayerAction parseActionReply(int otherPlayerID, String actionChoice, boolean present){
        PlayerAction actionReply = new PlayerAction();
        String otherPlayer = playerStringByID(otherPlayerID);
        String description;
        actionReply.setTitle("End of Turn");
        actionReply.setAction_id("end_of_turn");
        switch(actionChoice){
            case "stay_home":
                actionReply.setDescription("You stayed at home and slept through the night");
            case "do_nothing":
                actionReply.setDescription("You stayed at work and received visits from ");
                break;
            case "ask_favour":
                description = "You went to ask a favour of the " + otherPlayer + ". ";
                if (present){
                    description += "You found them at their place of work and they granted you a favour. " +
                            "You increased your good will among the village but you now owe the " + otherPlayer + ". " +
                            "They may call on you to return this favour in a time of need";
                } else {
                    description += "You arrived at their place of work but they were nowhere to be found. " +
                            "When you remarked on this to the other villagers they found it very suspicious. ";
                }
                actionReply.setDescription(description);
                break;
            case "visit_player":
                description = "You went to visit the " + otherPlayer + ". ";
                if (present){
                    description += "You found them there and discussed the arrival of the WitchFinder. ";
                } else {
                    description += "You arrived at their place of work but they were nowhere to be found. " +
                            "When you remarked on this to the other villagers they found it very suspicious. ";
                }
                actionReply.setDescription(description);
                break;
            case "meet_witchfinder":
                description = "You met with the WitchFinder and told him how you had seen the " + otherPlayer
                        + " dancing in the moon's light. " +
                        "They wore not a stitch of clothing and " +
                        "under their arm they held a black goose with whom they seemed to dance. ";
                actionReply.setDescription(description);
                break;
            case "plant_evidence":
                description = "You sneak into the house of the " + otherPlayer + " in the dark of night. " +
                        "Above their hearth you place a small jar filled with crows beaks. ";
                actionReply.setDescription(description);
                break;
            case "error":
                description = "There was an error";
                actionReply.setDescription(description);
                break;
            default:
                description = "There was a default error";
                actionReply.setDescription(description);
                break;

        }
        Logger.debug("Action Parser run: " + actionChoice);
        return actionReply;
    }

    public List<PlayerAction> parseWitchFinderAction(List<Player> suspicionList){
        PlayerAction actionReply = new PlayerAction();
        int accused = suspicionList.size();
        actionReply.setTitle("The WitchFinder's Conclusion");
        actionReply.setAction_id("final_turn");
        String description = "After a comprehensive investigation, interrogation of numerous villagers and long deliberation" +
                "the WitchFinder has uncovered conclusive evidence of WitchCraft in your quiet village. ";
        if (accused > 1){
            String acc = "";
            switch (accused){
                case 2: acc += "Two "; break;
                case 3: acc += "Three "; break;
                case 4: acc += "Four "; break;
            }
            description += acc + "villagers have been accused of Witchcraft, ";

            for (int i = 0; i < accused; i++){
                String villager = playerStringByID(suspicionList.get(i).getPlayerID());
                if (i == accused - 1){
                    description += "and the " + villager + ". ";
                } else {
                    description += "the " + villager + ", ";
                }

            }
        } else {
            String villager = playerStringByID(suspicionList.get(0).getPlayerID());
            description += "One villager has been accused of WitchCraft, the " + villager + ". ";
        }


        description += "A great pyre is constructed in the town centre and the accused are burned upon it. ";

        actionReply.setDescription(description);
        List<PlayerAction> actionList = new ArrayList<>();
        actionList.add(actionReply);
        return actionList;
    }

    public String playerStringByID(int playerID){
        switch (playerID){
            case 0: return "cobbler";
            case 1: return "seamstress";
            case 2: return "baker";
            case 3: return "midwife";
            default: return "(error)";
        }
    }

    // catches polls from
    public void waitingPoll(){
        waitingPolls++;
        Logger.debug("waitingPolls incremented to: " + waitingPolls + " in game: " + gameID);
        if (waitingPolls == 5 && gameStage == 0){
            started = true;
            Logger.debug("started set to true");
            // if all actions are in before game has closed
            // calc round as game closes
            if (allActionsIn()) {
                calculateRoundEnd();
                Logger.debug("calc round end triggered by waitingPoll");
            }


        }
    }

    public void startedPoll(){
        waitingPolls++;
        Logger.debug("startedPolls incremented to: " + waitingPolls + " in game: " + gameID);
        if (waitingPolls > 4){
            gameStage = 1;
            waitingPolls = 0;
            Logger.debug("gameStage incremented");
        }
    }

    public int getGameID(){
        return gameID;
    }
    public void setGameID(int gameID){this.gameID = gameID;}
    public int getPlayerCount() {
        return playerCount;
    }
    public Player getPlayer(int playerNo){
        return (Player) playerList.get(playerNo);
    }
    public int getGameStage(){return this.gameStage;}

    public List<PlayerAction> getPlayerActions(){
        Logger.debug("List request in game: " + gameID + " for round: " + gameStage);
        // called by actionList.controller. based on gameStage returns proper PlayerAction list
        List<PlayerAction> actionList;
        if (gameStage < 3){
            actionList = actionController.actionListByRound(gameStage);
        } else {
            actionList = calculateWitchFinderAction();
        }

        return actionList;
    }



}
