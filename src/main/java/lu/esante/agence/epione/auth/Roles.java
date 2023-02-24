package lu.esante.agence.epione.auth;

public class Roles {

    private Roles() {
        throw new IllegalStateException(this.getClass() + " can't be instanciated");
    }

    public static final String PRACTITIONER = "PRACTITIONER";
    public static final String PATIENT = "PATIENT";
    public static final String CNS = "CNS";
    public static final String ADMIN = "ADMIN";
}
