import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.schema.ConceptSchema;
import jade.content.schema.PredicateSchema;


public class BlottoOntology extends Ontology {

    public static final String ONTOLOGY_NAME = "blotto-ontology";
    public static final String PLAY_BLOTTO = "play-blotto";
    public static final String GET_BLOTTO_RESULT = "get-blotto-result";
    public static final String COMMITTED_UNITS = "committed-units";
    public static final String ALLOCATION = "allocation";
    public static final String ASSIGNMENT = "assignment";
    public static final String BLOTTO_RESULT = "blotto-result";
    public static final String RESULT = "result";

    // Handy.
    public static final String VALUE = "value";
    public static final String TEXT = "text";

    // Singleton
    private static final Ontology instance = new BlottoOntology();
    public static Ontology getInstance() {
        return instance;
    }

    private BlottoOntology() {
        super(ONTOLOGY_NAME, BasicOntology.getInstance());

        try
        {
            // CommitedUnits predicate
            PredicateSchema ps = new PredicateSchema(COMMITTED_UNITS);
            add(ps, CommittedUnits.class);
            ps.add(VALUE, getSchema(BasicOntology.INTEGER), ConceptSchema.MANDATORY);
        }
        catch (OntologyException ex)
        {
            ex.printStackTrace();
        }
    }
}