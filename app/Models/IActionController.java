package Models;

import java.util.List;

/**
 * Created by Davin on 27/05/2016.
 */
public interface IActionController {
    //
    PlayerAction randomPlayerAction(int roundNo);
    PlayerAction waitingAction();
    List<PlayerAction> actionListByRound(int roundNo);
    int randomPlayerID();
}
