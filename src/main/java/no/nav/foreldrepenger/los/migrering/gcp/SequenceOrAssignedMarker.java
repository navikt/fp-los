package no.nav.foreldrepenger.los.migrering.gcp;

// bruker som markør på aktuelle entiteter for bruk i SequenceOrAssignedGenerator
public interface SequenceOrAssignedMarker<T> {
    T getId();
    void setId(T id);
}
