import jade.content.ContentElementList;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;


public class ResponderBehaviour extends ContractNetResponder {

    public ResponderBehaviour(Agent a, MessageTemplate mt) {
        super(a, mt);
    }

    @Override
    protected ACLMessage handleCfp(ACLMessage cfp)
    {
        // Prepare the propose message to be sent in response
        int minUnits = ((BlottoAgent)myAgent).extractCommittedUnits(cfp).getValue();
        ACLMessage response = cfp.createReply();

        if (minUnits < ((BlottoAgent)myAgent).units) {
            // We can, so let's propose.
            response.setPerformative(ACLMessage.PROPOSE);

            // Try to give all units.
            ContentElementList cel = new ContentElementList();
            cel.add(new CommittedUnits(((BlottoAgent)myAgent).units));
             try
            {
                myAgent.getContentManager().fillContent(response, cel);
            }
            catch (Codec.CodecException ex)
            {
                // ignore
            }
            catch (OntologyException ex)
            {
                // ignore
            }
        } else {
            // Not enough units.
            response.setPerformative(ACLMessage.REFUSE);
        }

        return response;
    }

    // TODO add the actual behaviours for:
    // Accept-proposal -> should initiate the playbehaviour after it's fixed
    // CFP
}
