import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;


public class WaitBehaviour extends WakerBehaviour {
    public WaitBehaviour(Agent a, long timeout) {
        super(a, timeout);
    }

    @Override
    protected void onWake() {
        ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
        cfp.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        cfp.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
        cfp.setOntology(BlottoOntology.ONTOLOGY_NAME);
        myAgent.addBehaviour(new InitiatorBehaviour(myAgent, cfp));
        myAgent.removeBehaviour(this);
    }
}
