package no.nav.foreldrepenger.los.migrering.gcp;

import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.GeneratorCreationContext;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import java.lang.reflect.Member;
import java.util.Properties;


public class SequenceOrAssignedGenerator extends SequenceStyleGenerator {

    private final String sequenceName;

    public SequenceOrAssignedGenerator(
        SequenceOrAssigned config,
        Member annotatedMember,
        GeneratorCreationContext creationContext) {

        this.sequenceName = config.sequence();
    }

    @Override
    public void configure(GeneratorCreationContext creationContext, Properties parameters) throws MappingException {
        parameters.setProperty(SEQUENCE_PARAM, sequenceName);
        super.configure(creationContext, parameters);
    }

    @Override
    public Object generate(SharedSessionContractImplementor session, Object entity) {
        if (entity instanceof SequenceOrAssignedMarker<?> assignedIdEntity && assignedIdEntity.getId() != null) {
            // vi bruker Id fra FSS
            return assignedIdEntity.getId();
        }
        return super.generate(session, entity);
    }

    @Override
    public boolean allowAssignedIdentifiers() {
        return true;
    }
}
