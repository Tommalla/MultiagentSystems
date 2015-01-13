import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;


public class InitiatorBehaviour extends ContractNetInitiator {

    public InitiatorBehaviour(Agent a, ACLMessage cfp, DataStore store) {
        super(a, cfp, store);
    }

    // Move current play behaviour here here.

}
