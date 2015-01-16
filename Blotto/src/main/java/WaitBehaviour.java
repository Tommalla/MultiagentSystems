import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;


public class WaitBehaviour extends WakerBehaviour {
    public WaitBehaviour(Agent a, long timeout) {
        super(a, timeout);
    }

    
    @Override
    protected void onWake() {
        myAgent.addBehaviour(((BlottoAgent)myAgent).getNewInitiatorBehaviour());
        myAgent.removeBehaviour(this);
    }
}
