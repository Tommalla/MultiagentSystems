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

    private static final long REPLY_TIMEOUT = 70000L;

    public InitiatorBehaviour(Agent a, ACLMessage cfp) {
        super(a, cfp);
    }

    @Override
    protected Vector prepareCfps(ACLMessage cfp) {
        Vector<ACLMessage> msgs = new Vector<ACLMessage>();
        List<AID> agentsList = ((BlottoAgent)myAgent).getAgentsFromDF();
        for (AID agentAID : agentsList) {
            if (agentAID != myAgent.getAID()) {
                ACLMessage newMsg = (ACLMessage)cfp.clone();
                newMsg.addReceiver(agentAID);
                newMsg.addReplyTo(myAgent.getAID());
                newMsg.setReplyByDate(new Date(
                        Calendar.getInstance().getTime().getTime() + REPLY_TIMEOUT));

                // Try to give all units.
                ContentElementList cel = new ContentElementList();
                cel.add(new CommittedUnits());
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
        }
        System.out.println(getDataStore());
        return msgs;
    }

    @Override
    protected void handlePropose(ACLMessage propose, Vector acceptances) {
        // Accept/refuse
        int unitsNeeded = ((BlottoAgent)myAgent).extractCommittedUnits(propose).getValue();
        ACLMessage msg = propose.createReply();
        // We repeat the content of the proposal.
        msg.setContent(propose.getContent());

        if (((BlottoAgent)myAgent).units >= unitsNeeded) {
            ((BlottoAgent)myAgent).units -= unitsNeeded;
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
