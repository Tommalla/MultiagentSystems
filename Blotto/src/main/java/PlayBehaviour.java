import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import jade.util.leap.ArrayList;
import jade.util.leap.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;


public class PlayBehaviour extends AchieveREInitiator {
    private final ResponderBehaviour parentBehaviour;
    private final ACLMessage messageTemplate;
    private static final int FIELDS = 5;
    private final AID arbitrator;
    private static final Logger logger = Logger.getLogger(PlayBehaviour.class.getName());

    public PlayBehaviour(Agent a, ACLMessage msg, ResponderBehaviour parentBehaviour, AID arbitrator) {
        super(a, msg);
        this.parentBehaviour = parentBehaviour;
        this.arbitrator = arbitrator;
        this.messageTemplate = msg;
    }

    @Override
    protected Vector prepareRequests(ACLMessage request) {
        // Split and set units
        Vector<ACLMessage> result = new Vector<ACLMessage>();
        ACLMessage resMsg = (ACLMessage)messageTemplate.clone();

        DataStore store = getDataStore();
        BlottoAgent agent = (BlottoAgent)myAgent;

        ACLMessage accept = (ACLMessage)store.get(parentBehaviour.ACCEPT_PROPOSAL_KEY);
        int othersUnits = agent.extractCommittedUnits(accept).getValue();
        GetBlottoResult requestAction = new GetBlottoResult(allocateUnits(othersUnits + parentBehaviour.givenUnits));

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

    @Override
    protected void handleAllResponses(Vector responses) {
        for (Object o : responses)
        {
            ACLMessage response = (ACLMessage) o;
            if (response.getPerformative() != ACLMessage.AGREE) {
                throw new IllegalArgumentException("An error while communicating with arbitor: " + response.getContent());
            }
        }
    }

    @Override
    protected void handleAllResultNotifications(Vector resultNotifications) {
        ACLMessage result = (ACLMessage) resultNotifications.get(0);
        System.out.println(">>>> Got a result notification!!!");

        DataStore store = getDataStore();
        ACLMessage reply = ((ACLMessage)store.get(parentBehaviour.ACCEPT_PROPOSAL_KEY)).createReply();
        reply.setPerformative(result.getPerformative());
        reply.setContent(result.getContent());
        getDataStore().put(parentBehaviour.REPLY_KEY, reply);

        if (result.getPerformative() == ACLMessage.INFORM) {
            // Success, note it (TODO).
        } else {
            // Failure, reset.
            ((BlottoAgent)myAgent).units += parentBehaviour.givenUnits;
            parentBehaviour.givenUnits = 0;
        }
    }

    @Override
    public int onEnd() {
        System.out.println("Ending...");
        return super.onEnd(); //To change body of generated methods, choose Tools | Templates.
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
