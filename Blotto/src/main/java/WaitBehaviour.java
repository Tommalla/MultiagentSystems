import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import java.util.Date;


public class WaitBehaviour extends WakerBehaviour {
    private DataStore store;

    public WaitBehaviour(Agent a, Date wakeupDate, DataStore store) {
        super(a, wakeupDate);
        this.store = store;
    }

    @Override
    protected void onWake() {
        ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
        cfp.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        myAgent.addBehaviour(new InitiatorBehaviour(myAgent, cfp, store));
        myAgent.removeBehaviour(this);
    }
}
