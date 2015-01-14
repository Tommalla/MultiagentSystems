import jade.content.ContentElementList;
import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;


public class ResponderBehaviour extends ContractNetResponder {

    public ResponderBehaviour(Agent a, MessageTemplate mt, DataStore store) {
        super(a, mt, store);
    }

    @Override
    protected ACLMessage handleCfp(ACLMessage cfp)
    {
        // Prepare the propose message to be sent in response
        int minUnits = extractCommittedUnits(cfp).getValue();
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

    private CommittedUnits extractCommittedUnits(ACLMessage request) {
        // Extracting committed units.

        if (!FIPANames.ContentLanguage.FIPA_SL.equals(request.getLanguage()))
        {
            throw new IllegalArgumentException(
                    "Unrecognized content language: '" + request.getLanguage() +
                            "'I recognize fipa-sl content only.");
        }

        if (!BlottoOntology.ONTOLOGY_NAME.equals(request.getOntology()))
        {
            throw new IllegalArgumentException("Unrecognized ontology: I recognize blotto-ontology only.");
        }

        ContentManager cm = myAgent.getContentManager();
        try
        {
            // FIXME!!!
            return (CommittedUnits) cm.extractContent(request);
        }
        catch (Codec.CodecException ex)
        {
            throw new IllegalArgumentException("Content is invalid: " + ex.getMessage());
        }
        catch (OntologyException ex)
        {
            throw new IllegalArgumentException("Content is invalid: " + ex.getMessage());
        }
        catch (NullPointerException ex)
        {
            throw new IllegalArgumentException("Content is invalid: " + ex.getMessage());
        }
    }
    // TODO add the actual behaviours for:
    // Proposal
    // Accept-proposal -> should initiate the playbehaviour after it's fixed
    // CFP
}
