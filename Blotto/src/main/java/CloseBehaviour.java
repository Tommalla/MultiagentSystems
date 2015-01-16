import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;


public class CloseBehaviour extends WakerBehaviour {
    private static final long TIME_DELTA_MS = 2500L;    // 2.5 sec for everything to clean up

    public CloseBehaviour(Agent a) {
        super(a, TIME_DELTA_MS);
    }

    @Override
    protected void onWake() {
        myAgent.doDelete();
    }

}
