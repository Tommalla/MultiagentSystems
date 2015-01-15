import jade.content.ContentElementList;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;


public class InitiatorBehaviour extends ContractNetInitiator {

    private int givenUnits = 0;
    private static final long REPLY_TIMEOUT = 70000L;

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

            // Try to give all units.
            ContentElementList cel = new ContentElementList();
            cel.add(new CommittedUnits(1));
            try
            {
                myAgent.getContentManager().fillContent(newMsg, cel);
            }
            catch (Codec.CodecException ex)
            {
                // ignore
            }
            catch (OntologyException ex)
            {
                // ignore
            }

            msgs.add(newMsg);
        }
        System.out.println(getDataStore());
        return msgs;
    }

    @Override
    protected void handlePropose(ACLMessage propose, Vector acceptances) {
        // Accept/refuse
        BlottoAgent agent = (BlottoAgent)myAgent;
        ACLMessage msg = propose.createReply();

        System.out.println("handlePropose with " + agent.units);

        if (agent.units > 0) {
            ContentElementList cel = new ContentElementList();
            cel.add(new CommittedUnits(agent.units));
            givenUnits = agent.units;
            agent.units = 0;

            try
            {
                myAgent.getContentManager().fillContent(msg, cel);
            }
            catch (Codec.CodecException ex)
            {
                // ignore
            }
            catch (OntologyException ex)
            {
                // ignore
            }
            msg.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
        } else {
            msg.setPerformative(ACLMessage.REJECT_PROPOSAL);
        }
        
        acceptances.add(msg);
    }

    @Override
    protected void handleFailure(ACLMessage failure) {
        int unitsReturned = ((BlottoAgent)myAgent).extractCommittedUnits(failure).getValue();
        ((BlottoAgent)myAgent).units += unitsReturned;
        // FIXME handle sanity checks?
    }


}
