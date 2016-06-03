package Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import play.Logger;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Davin on 07/05/2016.
 */
public class Player {

    private int playerID;
    private int gameID;
    private int playerClientId;

    private Map playerActionMap;
    private List<Integer> favorsOwed;
    private PlayerAction roundResponseAction;
    private int owedFavors;
    private int suspicion;
    private int goodWill;

    public Player(int playerID, int gameID, int playerClientId){
        this.playerID = playerID;
        this.gameID = gameID;
        this.playerClientId = playerClientId;
        this.playerActionMap = new HashMap();
        this.favorsOwed = new ArrayList<>();
        this.owedFavors = 0;
        this.suspicion = 0;
        this.goodWill = 0;
    }

    public Player(){

    }

    public int getPlayerID() {
        return playerID;
    }
    public void setPlayerID(int playerID) {
        this.playerID = playerID;
    }
    public int getGameID() {
        return gameID;
    }
    public void setGameID(int gameID) {
        this.gameID = gameID;
    }
    public int getPlayerClientId() {
        return playerClientId;
    }
    public void setPlayerClientId(int playerClientId) {
        this.playerClientId = playerClientId;
    }

    @JsonIgnore
    public boolean isAssigned(){
        return playerClientId != 0;
    }

    public void addAction(int roundNo, PlayerAction playerResponse){
        playerActionMap.put(roundNo, playerResponse);
    }
    public int actionsTakenNo(){
        return playerActionMap.size();
    }

    public PlayerAction findActionByRound (int roundNo){
        return (PlayerAction) playerActionMap.get(roundNo);
    }

    public void incrementGoodWill(){
        goodWill++;
    }

    public void incrementSuspicion(){
        suspicion++;
    }

    @JsonIgnore
    public int getSuspicion(){
        return suspicion;
    }

    @JsonIgnore
    public int getGoodWill(){
        return goodWill;
    }

    public int suspicionLessGoodWill(){
        return suspicion - goodWill;
    }

    public void incrementOwedFavors(){
        owedFavors++;
    }

    public void addFavourOwed(int playerOwed){
        favorsOwed.add(playerOwed);
    }

    public int repayFavour(){
        int playerOwed = -1;
        if (favorsOwed.size() > 0){
            playerOwed  = favorsOwed.get(0);
            favorsOwed.remove(0);
        }
        return playerOwed;
    }

    public boolean readyCheck(){
        return roundResponseAction != null;
    }

    public void putRoundResponse(PlayerAction roundResponse){
        Logger.debug("round response added to player: " + playerID);
        this.roundResponseAction = roundResponse;
    }

    public PlayerAction popRoundResponse(){
        PlayerAction roundResponse = this.roundResponseAction;
        this.roundResponseAction = null;
        return roundResponse;
    }

    public boolean visitMe(int roundNo){
        String actionID = findActionByRound(roundNo).getAction_id();
        switch (actionID){
            case "ask_favour":
                return false;
            case "visit_player":
                return false;
            default:
                return true;
        }
    }

}
