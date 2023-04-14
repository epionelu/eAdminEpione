package lu.esante.agence.epione.util;

import lu.esante.agence.epione.exception.ForbiddenException;
import lu.esante.agence.epione.service.IIdentityService;

public class SecurityUtils {

    private SecurityUtils() {
        throw new IllegalStateException();
    }

    public static void eHealthIdCheck(IIdentityService identity, String eHealthId) {
        if (identity.getEHealthId() == null || !identity.getEHealthId().equals(eHealthId)) {
            throw new ForbiddenException("Invalid eHealthId.");
        }
    }

    public static void ssnCheck(IIdentityService identity, String ssn) {
        if (identity.getName() == null || !identity.getName().equals(ssn)) {
            throw new ForbiddenException("You cannot access this document as it's not yours.");
        }
    }
}
