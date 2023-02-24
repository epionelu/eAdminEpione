package lu.esante.agence.epione.service;

public interface ITeamVerifier {
    boolean isTeamMember(String identifier, String eHealthId);
}
