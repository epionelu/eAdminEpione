package lu.esante.agence.epione.structure;

public class Mapper extends AbstractMapper<String, String> {

    @Override
    public String entityToBusiness(String b) {
        return b;
    }

    @Override
    public String businessToEntity(String a) {
        return a;
    }

}
