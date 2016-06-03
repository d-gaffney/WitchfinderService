package Models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Davin on 11/05/2016.
 */
public class GameList {

    private Map gameInstanceList;
    private int lastGameID;
    private int playerClientCount; // temp field to return unique playerClientID


    public GameList(){
        gameInstanceList = new HashMap();
        lastGameID = 0;
        playerClientCount = 1; // should retrieve from database for unique clientIDs
    }

    public boolean isEmpty(){
        return gameInstanceList.isEmpty();
    }

    public boolean lastGameIsWaiting(){

        GameInstance lastGame = (GameInstance)gameInstanceList.get(lastGameID);
        return !lastGame.isRunning();
    }

    public void tidyList(){
        // iterate through list and remove any finished games
    }



    public void addGameInstance(){
        lastGameID++;
        IActionController actionController = new TestActionController();
        gameInstanceList.put(lastGameID, new GameInstance(lastGameID, actionController));

    }

    public GameInstance findGameByID(int gameID){

        return (GameInstance) gameInstanceList.get(gameID);
    }


    public Player addNewPlayer(int clientID){
        // check lastGame is not full / started
        if (clientID == 0){
            clientID = playerClientCount;
            playerClientCount++;
            // should trigger adding setPlayer to database
        }
        Player setPlayer;
        if (lastGameID == 0 || !lastGameIsWaiting()){
            addGameInstance();
        }
        setPlayer = findGameByID(lastGameID).addPlayer(clientID);
        setPlayer.setGameID(lastGameID);
        // if game not waiting create new game, add player to it, set lastGameID, player.gameID.playerID and return
        return setPlayer;
    }

    public void addAction(int gameID, int playerID, PlayerAction playerResponse){

        findGameByID(gameID).addAction(playerID, playerResponse);
    }

    /* temp test method
    public Player newPlayer(int clientID){

        GameInstance gameInstance = new GameInstance(lastGameID);
        Player setPlayer = gameInstance.addPlayer(clientID);
        addGameInstance();
        setPlayer.setGameID(lastGameID);
        return setPlayer;
    }*/
}
