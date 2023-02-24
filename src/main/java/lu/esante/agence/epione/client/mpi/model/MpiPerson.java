package lu.esante.agence.epione.client.mpi.model;

import lombok.Data;

@Data
public class MpiPerson {

  private String status;
  private String reqId;
  private String matricule;
  private String birthtime;
  private String familyname;
  private String firstname;
  private String othername;
  private String sex;
  private String streetAddress;
  private String otherAddress;
  private String city;
  private String postalCode;
  private String country;
}