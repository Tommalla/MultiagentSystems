import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import jade.util.leap.ArrayList;
import jade.util.leap.List;
import java.util.Vector;
import java.util.logging.Logger;


public class PlayBehaviour extends AchieveREInitiator {
    private static final Logger logger = Logger.getLogger(PlayBehaviour.class.getName());
    private static final int FIELDS = 5;

    private final ResponderBehaviour parentBehaviour;
    private final ACLMessage messageTemplate;
    private final AID arbitrator;


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

        agent.fillMessage(resMsg, new Action(arbitrator, requestAction));

        result.add(resMsg);
        return result;
    }

    @Override
    protected void handleAllResponses(Vector responses) {
        for (Object o : responses)
        {
            ACLMessage response = (ACLMessage) o;
            if (response.getPerformative() != ACLMessage.AGREE) {
                throw new InvalidContentException("An error while communicating with arbitor (expected AGREE): " + response.getContent());
            }
        }
    }

    @Override
    protected void handleAllResultNotifications(Vector resultNotifications) {
        ACLMessage result = (ACLMessage) resultNotifications.get(0);

        DataStore store = getDataStore();
        ACLMessage acceptProposal = ((ACLMessage)store.get(parentBehaviour.ACCEPT_PROPOSAL_KEY));
        ACLMessage reply = acceptProposal.createReply();
        reply.setPerformative(result.getPerformative());
        reply.setContent(result.getContent());
        getDataStore().put(parentBehaviour.REPLY_KEY, reply);

        if (result.getPerformative() == ACLMessage.INFORM) {
            BlottoAgent agent = (BlottoAgent)myAgent;
            agent.finishTransaction();
            agent.addResult(acceptProposal.getSender().getLocalName(),
                agent.extractBlottoResult(result).getResult());
        } else {
            // Failure, reset.
            parentBehaviour.reclaimUnits();
        }
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
