package controllers;

import Models.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.api.libs.json.JsPath;
import play.libs.Json;
import play.mvc.*;

//import scala.util.parsing.json.JSONObject;
import views.html.*;
import play.Logger;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.BodyParser;



/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    String pA1_Title = "Go about your business";
    String pA1_Description = "(Rouses little suspicion, provides witnesses)";
    String pA1_Action_id = "do_nothing";

    String pA2_Title = "Ask someone for a favour";
    String pA2_Description = "(Increases goodwill, you must return the favour)";
    String pA2_Action_id = "ask_favour";

    String pA3_Title = "Visit someone";
    String pA3_Description = "(See if the villager is where they are supposed to be)";
    String pA3_Action_id = "visit_player";

    String endT_Title = "End of turn";
    String endT_Description = "You have chosen to ";
    String endT_Action_id = "end_of_turn_1";

    String waiting_Title = "Waiting";
    String waiting_Description = "Waiting for other players to finish";
    String waiting_Action_id = "waiting_for_players";

    public PlayerAction pA1 = new PlayerAction(pA1_Title, pA1_Description, pA1_Action_id);
    public PlayerAction pA2 = new PlayerAction(pA2_Title, pA2_Description, pA2_Action_id);
    public PlayerAction pA3 = new PlayerAction(pA3_Title, pA3_Description, pA3_Action_id);

    public PlayerAction endT1 = new PlayerAction(endT_Title, endT_Description, endT_Action_id);
    public PlayerAction waitingAction = new PlayerAction(waiting_Title, waiting_Description, waiting_Action_id);

    // game list. needs to be injected into controller
    public GameList gameList = new GameList();

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public Result index() {
        return ok(index.render("WitchFinder"));
    }


    public Result actionList(int gameID) {
        //int gameStage = gameList.findGameByID(gameID).getGameStage();
        // return the list of options for each turn
        // have game hold/have access to masterList and let it return array depending on round
        Logger.debug("List request received for game: " + gameID);
        GameInstance gameInstance = gameList.findGameByID(gameID);
        Logger.debug("List request game: " + gameInstance.getGameStage());
        List<PlayerAction> actionArray = gameInstance.getPlayerActions();

        return ok(Json.toJson(actionArray));
    }

    @BodyParser.Of(play.mvc.BodyParser.Json.class)
    public Result actionMessage() {
        Logger.debug("End of turn message received");
        ObjectNode result = Json.newObject();
        JsonNode json = request().body().asJson();
        String description = json.findPath("action_id").textValue();
        if(description == null) {
            return badRequest("Missing parameter [name]");
        } else {
            result.put("description", description);
            result.put("action_id", "message");
            result.put("title", "message");
            return ok(result);
        }

    }

    public Result start() {
        String connected = "Connected to Witchfinder";
        return ok(connected);
    }

    public Result joinGame(int clientID) {
        // Check game at gameInstanceList.getlastGameID()
        IActionController actionController = new TestActionController();
        Player setPlayer = gameList.addNewPlayer(clientID);

        return ok(Json.toJson(setPlayer));
    }


    public Result endTurn(int gameID, int playerID){
        JsonNode selectedAction = request().body().asJson();

        if(selectedAction == null) {
            return badRequest("Expecting Json data");
        } else {
            // add PlayerAction to player's ActionList
            PlayerAction playerResponse = Json.fromJson(selectedAction, PlayerAction.class);
            String title = playerResponse.getTitle();
            if(title == null) {
                return badRequest("Missing parameter [title]");
            } else {
                // addAction needs to return appropriate
                PlayerAction returnAction = gameList.findGameByID(gameID).addAction(playerID, playerResponse);
                return ok(Json.toJson(returnAction));
            }
        }
    }

    public Result startRequest(String first, int gameID){
        Logger.debug("startRequest: first: " + first);
        GameInstance requestedGame = gameList.findGameByID(gameID);
        String startedResponse;
        if (requestedGame.isRunning()){
            startedResponse = "started";
        } else {
            if (first.equals("true")){
                requestedGame.startedPoll();
            }
            startedResponse = "waiting";
        }
        return ok(startedResponse);
    }

    public Result roundStartRequest(int gameID, int playerID){
        Logger.debug("roundRequest: gID: " + gameID + " pID: " + playerID);
        GameInstance requestedGame = gameList.findGameByID(gameID);
        String roundResponse;

        if (playerID == 0){
            requestedGame.waitingPoll();
        }
        Logger.debug(String.format("round: " + requestedGame.getGameStage() + ". response ready: %s", requestedGame.getPlayer(playerID).readyCheck()));
        if (requestedGame.getPlayer(playerID).readyCheck()){
            roundResponse = "ready";
        } else {
            roundResponse = "waiting";
        }

        Logger.debug("response : " + roundResponse);
        return ok(roundResponse);
    }

    public Result testGetJson(){
        Player player = new Player(3,1,2);
        PlayerAction playerAction = pA3;
        return ok(Json.toJson(playerAction));
    }

    public Result testPostJson(){
        JsonNode testJson = request().body().asJson();
        System.out.println(testJson.asText());
        PlayerAction playerResponse = Json.fromJson(testJson, PlayerAction.class);
        PlayerAction playerReply = endT1;
        playerReply.setDescription(playerReply.getDescription() + playerResponse.getTitle());
        //Player player = Json.fromJson(testJson, Player.class);
        return ok(Json.toJson(playerReply));
    }


}
