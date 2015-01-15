import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetResponder;
import java.util.logging.Level;
import java.util.logging.Logger;


public class PlayBehaviour extends Behaviour {
    private boolean finished = false;
    private ContractNetResponder parent;
    private static final int fields = 5;

    public PlayBehaviour(Agent a, ContractNetResponder parent) {
        super(a);
        this.parent = parent;
    }

    @Override
    public void action() {
        ACLMessage accept = ((ACLMessage)getDataStore().get(parent.ACCEPT_PROPOSAL_KEY));
        int neededUnits = ((BlottoAgent)myAgent).extractCommittedUnits(accept).getValue();
        ACLMessage msg = accept.createReply();
        boolean failure = false;

        if (((BlottoAgent)myAgent).units >= neededUnits) {
            ((BlottoAgent)myAgent).units -= neededUnits;
            System.out.println("AcceptedProposal");
            try {
                AID arbitrator = getArbitrator();
                // TODO initiate play behaviour.

                msg.setPerformative(ACLMessage.INFORM);
                // TODO return the result.
            } catch (FIPAException ex) {
                Logger.getLogger(PlayBehaviour.class.getName()).log(Level.SEVERE, "Couldn't find the arbitrator,", ex);
                failure = true;
            }
        } else {
            failure = true;
        }

        if (failure) {
            msg.setPerformative(ACLMessage.FAILURE);
            // FIXME add reason here.
            msg.setContent(accept.getContent());
        }

        finished = true;
    }

    @Override
    public boolean done() {
        return finished;
    }

    private AID getArbitrator() throws FIPAException {
        DFAgentDescription temp = new DFAgentDescription();
        final ServiceDescription sd = new ServiceDescription();
        sd.setType("Blotto-Play");
        temp.addServices(sd);
        // We return the first one that fits the description.
        return DFService.search(myAgent, temp)[0].getName();
    }
}
