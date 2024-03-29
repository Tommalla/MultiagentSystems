import jade.content.ContentElementList;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Logger;


public class InitiatorBehaviour extends ContractNetInitiator {
    private static final Logger logger = Logger.getLogger(InitiatorBehaviour.class.getName());
    private static final long REPLY_TIMEOUT = 70000L;
    private final Random rand = new Random();

    private int givenUnits = 0;


    public InitiatorBehaviour(Agent a, ACLMessage cfp) {
        super(a, cfp);
    }


    @Override
    protected Vector prepareCfps(ACLMessage cfp) {
        Vector<ACLMessage> msgs = new Vector<ACLMessage>();
        List<AID> agentsList = ((BlottoAgent)myAgent).getAgentsFromDF();
        for (AID agentAID : agentsList) {
            ACLMessage newMsg = (ACLMessage)cfp.clone();
            newMsg.addReceiver(agentAID);
            newMsg.addReplyTo(myAgent.getAID());
            newMsg.setReplyByDate(new Date(
                    Calendar.getInstance().getTime().getTime() + REPLY_TIMEOUT));

            ContentElementList cel = new ContentElementList();
            cel.add(new Action(agentAID, new PlayBlotto()));
            cel.add(new CommittedUnits(1)); // Anything more than 0;

            ((BlottoAgent)myAgent).fillMessage(newMsg, cel);

            msgs.add(newMsg);
        }

        return msgs;
    }


    @Override
    protected void handlePropose(ACLMessage propose, Vector acceptances) {
        // Accept/refuse
        BlottoAgent agent = (BlottoAgent)myAgent;
        ACLMessage msg = propose.createReply();

        if (agent.getUnits() > 0) {

            ContentElementList cel = new ContentElementList();
            cel.add(agent.extractPlayBlottoAction(propose));
            cel.add(new CommittedUnits(agent.getUnits()));
            cel.add(agent.extractCommittedUnits(propose));
            giveUnits(agent.getUnits());

            agent.fillMessage(msg, cel);

            msg.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
        } else {
            msg.setPerformative(ACLMessage.REJECT_PROPOSAL);
        }

        acceptances.add(msg);
    }


    @Override
    protected void handleFailure(ACLMessage failure) {
        reclaimUnits();
    }


    @Override
    protected void handleInform(ACLMessage inform) {
        BlottoAgent agent = (BlottoAgent)myAgent;
        agent.finishTransaction();
        agent.addResult(inform.getSender().getLocalName(),
                agent.extractBlottoResult(inform).getResult());
    }


    @Override
    public int onEnd() {
        BlottoAgent agent = (BlottoAgent)myAgent;
        if (!agent.isFinished()) {
            // If not finished, readd.
            agent.addBehaviour(agent.getNewWaitBehaviour(rand.nextLong() % 5000L));
        }

        agent.removeBehaviour(this);
        return super.onEnd();
    }


    private void giveUnits(int given) {
        ((BlottoAgent)myAgent).startTransaction(given);
        givenUnits = given;
    }


    private void reclaimUnits() {
        ((BlottoAgent)myAgent).breakTransaction(givenUnits);
        givenUnits = 0;
    }

}
