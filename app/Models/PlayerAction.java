package Models;

/**
 * Created by Davin on 04/05/2016.
 */
public class PlayerAction {
    private String title;
    private String description;
    private String action_id;
    private int visitedPlayer;

    public PlayerAction(String title, String description, String action_id){
        this.title = title;
        this.description = description;
        this.action_id = action_id;
    }

    public PlayerAction(){}

    public String getTitle() {
        return title;
    }
    public void setTitle(String title){
        this.title = title;
    }

    public String getDescription(){
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getAction_id(){
        return action_id;
    }
    public void setAction_id(String action_id){
        this.action_id = action_id;
    }

    public void recordVisit(int playerVisited){
        this.visitedPlayer = playerVisited;
    }
}
