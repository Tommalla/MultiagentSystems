import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;


public class ResponderBehaviour extends ContractNetResponder {

    public ResponderBehaviour(Agent a, MessageTemplate mt, DataStore store) {
        super(a, mt, store);
    }

    // TODO add the actual behaviours for:
    // Proposal
    // Accept-proposal -> should initiate the playbehaviour after it's fixed
    // CFP
}
