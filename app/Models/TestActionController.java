package Models;

import play.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Davin on 29/05/2016.
 */
public class TestActionController implements IActionController {

    String pA1_Title = "Go about your business";
    String pA1_Description = "(Rouses little suspicion, provides witnesses)";
    String pA1_Action_id = "do_nothing";

    String pA2_Title = "Ask someone for a favour";
    String pA2_Description = "(Increases goodwill, you must return the favour)";
    String pA2_Action_id = "ask_favour";

    String pA3_Title = "Visit someone";
    String pA3_Description = "(See if the villager is where they are supposed to be)";
    String pA3_Action_id = "visit_player";

    String pA4_Title = "Meet with the Witchfinder";
    String pA4_Description = "(See the Witchfinder and accuse another player of witchcraft)";
    String pA4_Action_id = "meet_witchfinder";

    String pA5_Title = "Plant evidence of witchcraft";
    String pA5_Description = "(Sneak into another player's house and plant evidence of witchcraft)";
    String pA5_Action_id = "plant_evidence";

    String pA6_Title = "Stay at Home";
    String pA6_Description = "(Stay at home to guard against being framed)";
    String pA6_Action_id = "stay_home";

    String pA7_Title = "Carry out a Favour";
    String pA7_Description = "(Repay a favour you owe to another villager)";
    String pA7_Action_id = "repay_favour";

    String endT_Title = "End of turn";
    String endT_Description = "You have chosen to ";
    String endT_Action_id = "end_of_turn_1";

    private String waiting_Title = "Waiting";
    private String waiting_Description = "Waiting for other players to finish";
    private String waiting_Action_id = "waiting_for_players";

    String test_Title = "Test title";
    String test_Description = "Test description";
    String test_Action_id = "test_action_id";

    private PlayerAction pA1 = new PlayerAction(pA1_Title, pA1_Description, pA1_Action_id);
    private PlayerAction pA2 = new PlayerAction(pA2_Title, pA2_Description, pA2_Action_id);
    private PlayerAction pA3 = new PlayerAction(pA3_Title, pA3_Description, pA3_Action_id);
    private PlayerAction pA4 = new PlayerAction(pA4_Title, pA4_Description, pA4_Action_id);
    private PlayerAction pA5 = new PlayerAction(pA5_Title, pA5_Description, pA5_Action_id);
    private PlayerAction pA6 = new PlayerAction(pA6_Title, pA6_Description, pA6_Action_id);
    private PlayerAction pA7 = new PlayerAction(pA7_Title, pA7_Description, pA7_Action_id);

    private PlayerAction endT1 = new PlayerAction(endT_Title, endT_Description, endT_Action_id);
    private PlayerAction waitingAction = new PlayerAction(waiting_Title, waiting_Description, waiting_Action_id);
    private PlayerAction testAction = new PlayerAction(test_Title, test_Description, test_Action_id);

    private Random rand;
    private List<PlayerAction> testActionList;

    public TestActionController() {
        this.rand = new Random();
        this.testActionList = new ArrayList<>();
        // Populate list
        testActionList.add(0, pA1);
        testActionList.add(1, pA2);
        testActionList.add(2, pA3);
        testActionList.add(3, pA4);
        testActionList.add(4, pA5);
        testActionList.add(5, pA6);
        testActionList.add(6, pA7);
        testActionList.add(7, endT1);
        testActionList.add(8, waitingAction);
    }

    public PlayerAction randomPlayerAction(int roundNo) {
        // return random action for AI depending on round
        List<PlayerAction> actionList = actionListByRound(roundNo);
        int  n = rand.nextInt(actionList.size());
        return actionList.get(n);
    }

    public int randomPlayerID(){
        return rand.nextInt(4);
    }

    public PlayerAction waitingAction(){
        return waitingAction;
    }

    public List<PlayerAction> actionListByRound(int roundNo){
        Logger.debug("actionListByRound in actionController. roundNo: " + roundNo);
        List<PlayerAction> actionList = new ArrayList<>();
        switch (roundNo){
            case 0:
                Logger.debug("Case 0");
                actionList.add(pA1);
                actionList.add(pA2);
                actionList.add(pA3);
                break;
            case 1:
                actionList.add(pA1);
                actionList.add(pA2);
                actionList.add(pA3);
                actionList.add(pA4);
                break;
            case 2:
                actionList.add(pA6);
                actionList.add(pA7);
                actionList.add(pA5);
                break;
            case 3:
                actionList.add(testAction);
                break;
            case 4:
                actionList.add(testAction);
                break;
            default:
                actionList.add(testAction);
                break;
        }
        return actionList;

    }

}
