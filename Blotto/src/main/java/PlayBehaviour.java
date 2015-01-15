import jade.content.ContentElementList;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.DataStore;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.util.leap.ArrayList;
import jade.util.leap.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class PlayBehaviour extends Behaviour {
    private boolean finished = false;
    private ResponderBehaviour parent;
    private static final int FIELDS = 5;

    public PlayBehaviour(Agent a, ResponderBehaviour parent) {
        super(a);
        this.parent = parent;
    }

    @Override
    public void action() {
        DataStore store = getDataStore();
        BlottoAgent agent = (BlottoAgent)myAgent;

        ACLMessage accept = (ACLMessage)store.get(parent.ACCEPT_PROPOSAL_KEY);
        int othersUnits = agent.extractCommittedUnits(accept).getValue();

        ACLMessage msg = accept.createReply();
        boolean failure = false;

        System.out.println("AcceptedProposal");
        try {
            AID arbitrator = getArbitrator();
            ACLMessage playMsg = new ACLMessage(ACLMessage.REQUEST);
            playMsg.addReceiver(arbitrator);
            playMsg.addReplyTo(myAgent.getAID());
            playMsg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
            playMsg.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
            playMsg.setOntology(BlottoOntology.ONTOLOGY_NAME);

            // Split and set units
            GetBlottoResult request = new GetBlottoResult(allocateUnits(othersUnits + parent.givenUnits));

            try {
                myAgent.getContentManager().fillContent(playMsg, new Action(arbitrator, request));
                myAgent.send(playMsg);
            } catch (Codec.CodecException ex) {
                Logger.getLogger(PlayBehaviour.class.getName()).log(Level.SEVERE, null, ex);
            } catch (OntologyException ex) {
                Logger.getLogger(PlayBehaviour.class.getName()).log(Level.SEVERE, null, ex);
            }

            // Get the response:
            ACLMessage response = myAgent.blockingReceive();

            System.out.println("Arbitrator responded: " + response);

            msg.setPerformative(ACLMessage.INFORM);
            // TODO return the result.
        } catch (FIPAException ex) {
            Logger.getLogger(PlayBehaviour.class.getName()).log(Level.SEVERE, "Couldn't find the arbitrator,", ex);
            failure = true;
        }

        if (failure) {
            msg.setPerformative(ACLMessage.FAILURE);
            // FIXME add reason here.
            msg.setContent(accept.getContent());
        }


        store.put(parent.REPLY_KEY, msg);
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

    private Allocation allocateUnits(int units) {
        List resultList = new ArrayList();
        for (int i = 0; i < FIELDS - 1; ++i) {
            resultList.add(units / FIELDS);
        }

        resultList.add(units / FIELDS + (units % FIELDS));
        return new Allocation(resultList);
    }
}
