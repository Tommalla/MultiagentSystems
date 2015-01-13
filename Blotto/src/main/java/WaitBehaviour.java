import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import java.util.Date;


public class WaitBehaviour extends WakerBehaviour {

    public WaitBehaviour(Agent a, Date wakeupDate) {
        super(a, wakeupDate);
    }

    @Override
    protected void onWake() {
        this.myAgent.addBehaviour(new PlayBehaviour());
    }
}
