import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import jade.util.leap.ArrayList;
import jade.util.leap.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;


public class PlayBehaviour extends AchieveREInitiator {
    private final ResponderBehaviour parent;
    private static final int FIELDS = 5;
    private final AID arbitrator;

    public PlayBehaviour(Agent a, ACLMessage msg, ResponderBehaviour parent, AID arbitrator) {
        super(a, msg);
        this.parent = parent;
        this.arbitrator = arbitrator;
    }

    @Override
    protected Vector prepareRequests(ACLMessage request) {
        // Split and set units
        Vector<ACLMessage> result = new Vector<ACLMessage>();
        ACLMessage resMsg = (ACLMessage)request.clone();

        DataStore store = getDataStore();
        BlottoAgent agent = (BlottoAgent)myAgent;

        ACLMessage accept = (ACLMessage)store.get(parent.ACCEPT_PROPOSAL_KEY);
        int othersUnits = agent.extractCommittedUnits(accept).getValue();
        GetBlottoResult requestAction = new GetBlottoResult(allocateUnits(othersUnits + parent.givenUnits));

        try {
            myAgent.getContentManager().fillContent(resMsg, new Action(arbitrator, requestAction));
        } catch (Codec.CodecException ex) {
            Logger.getLogger(PlayBehaviour.class.getName()).log(Level.SEVERE, null, ex);
        } catch (OntologyException ex) {
            Logger.getLogger(PlayBehaviour.class.getName()).log(Level.SEVERE, null, ex);
        }

        result.add(resMsg);
        return result;
    }



//    @Override
//    public void action() {
//        DataStore store = getDataStore();
//        BlottoAgent agent = (BlottoAgent)myAgent;
//
//        ACLMessage accept = (ACLMessage)store.get(parent.ACCEPT_PROPOSAL_KEY);
//        int othersUnits = agent.extractCommittedUnits(accept).getValue();
//
//        ACLMessage msg = accept.createReply();
//        boolean failure = false;
//
//        System.out.println("AcceptedProposal");
//        try {
//
//
//            // Split and set units
//            GetBlottoResult request = new GetBlottoResult(allocateUnits(othersUnits + parent.givenUnits));
//
//            try {
//                myAgent.getContentManager().fillContent(playMsg, new Action(arbitrator, request));
//                myAgent.send(playMsg);
//            } catch (Codec.CodecException ex) {
//                Logger.getLogger(PlayBehaviour.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (OntologyException ex) {
//                Logger.getLogger(PlayBehaviour.class.getName()).log(Level.SEVERE, null, ex);
//            }
//
//            // Get the response:
//            ACLMessage response = myAgent.blockingReceive();
//
//            System.out.println("Arbitrator responded: " + response);
//            // Need to properly react to this...
//
//            msg.setPerformative(ACLMessage.INFORM);
//            // TODO return the result.
//        } catch (FIPAException ex) {
//            Logger.getLogger(PlayBehaviour.class.getName()).log(Level.SEVERE, "Couldn't find the arbitrator,", ex);
//            failure = true;
//        }
//
//        if (failure) {
//            msg.setPerformative(ACLMessage.FAILURE);
//            // FIXME add reason here.
//            msg.setContent(accept.getContent());
//        }
//
//
//        store.put(parent.REPLY_KEY, msg);
//        finished = true;
//    }

    private Allocation allocateUnits(int units) {
        List resultList = new ArrayList();
        for (int i = 0; i < FIELDS - 1; ++i) {
            resultList.add(units / FIELDS);
        }

        resultList.add(units / FIELDS + (units % FIELDS));
        return new Allocation(resultList);
    }
}
