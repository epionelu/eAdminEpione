package lu.esante.agence.epione.controller.v1;

import io.swagger.v3.oas.annotations.Parameter;
import lu.esante.agence.epione.controller.V1AbstractController;
import lu.esante.agence.epione.exception.*;
import lu.esante.agence.epione.model.Document;
import lu.esante.agence.epione.model.DocumentStatus;
import lu.esante.agence.epione.model.DocumentType;
import lu.esante.agence.epione.service.IDocumentService;
import lu.esante.agence.epione.service.IIdentityService;
import lu.esante.agence.epione.util.DocumentUtils;
import lu.esante.agence.epione.util.Utils;
import org.openapitools.api.DocumentApi;
import org.openapitools.model.DocumentDto;
import org.openapitools.model.DocumentDto.DocTypeEnum;
import org.openapitools.model.DocumentDto.StatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
public class DocumentController extends V1AbstractController implements DocumentApi {

    private IDocumentService service;
    private IIdentityService identity;

    @Autowired
    public DocumentController(IDocumentService service, IIdentityService identity) {
        this.service = service;
        this.identity = identity;
    }

    @Override
    @PreAuthorize("hasAnyAuthority('PRACTITIONER')")
    public ResponseEntity<Void> _deleteDocumentFromPerson(
            @Pattern(regexp = "[0-9]{10}") @Size(min = 10, max = 10) @Parameter(name = "eHealthId", description = "practitioner identifier", required = true) @PathVariable("eHealthId") String eHealthId,
            @Pattern(regexp = "[0-9]{13}") @Size(min = 13, max = 13) @Parameter(name = "ssn", description = "patient identifier", required = true) @PathVariable("ssn") String ssn,
            @Parameter(name = "idDocument", description = "Document identifier", required = true) @PathVariable("idDocument") String idDocument,
            @Parameter(name = "body", description = "New document replacing the old one.") @Valid @RequestBody(required = false) org.springframework.core.io.Resource body) {

        if (identity.getEHealthId() == null || !identity.getEHealthId().equals(eHealthId)) {
            throw new ForbiddenException("Invalid eHealthId");
        }

        Optional<Document> oldDoc = service.getByFileId(UUID.fromString(idDocument));
        if (oldDoc.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Optional<Document> newDoc = Optional.empty();
        if (body != null) {
            try {
                byte[] file = DocumentUtils.streamToByteArray(body.getInputStream());
                newDoc = DocumentUtils.parseZipFile(file, identity.getEHealthId(), identity.getName());

                if (newDoc.isEmpty()) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
                if (!newDoc.get().getSsn().equals(ssn)) {
                    throw new ForbiddenException("Document contains a different SSN than request");
                }
                if (!service.getByFileId(newDoc.get().getFileId()).isEmpty()) {
                    return new ResponseEntity<>(HttpStatus.CONFLICT);
                }
            } catch (IOException e) {
                throw new DocumentFormatException("Unable to read document: " + e.getMessage());
            } catch (JAXBException e) {
                throw new DocumentFormatException(
                        "Document has incorrect format : " + e.getLinkedException().getMessage());
            } catch (SAXException e) {
                throw new DocumentFormatException("Document has incorrect format: " + e.getMessage());
            }

        }

        service.cancelDocument(oldDoc.get(), newDoc);
        return new ResponseEntity<>(HttpStatus.OK);

    }

    @Override
    @PreAuthorize("hasAnyAuthority('PRACTITIONER')")
    public ResponseEntity<DocumentDto> _addDocument2Person(
            @Pattern(regexp = "[0-9]{10}") @Size(min = 10, max = 10) @PathVariable("eHealthId") String eHealthId,
            @Pattern(regexp = "[0-9]{13}") @Size(min = 13, max = 13) @PathVariable("ssn") String ssn,
            @Valid @RequestBody(required = false) org.springframework.core.io.Resource body) {

        if (body == null) {
            throw new BadRequestException("No body provided");
        }

        if (identity.getEHealthId() == null || !identity.getEHealthId().equals(eHealthId)) {
            throw new ForbiddenException("Invalid eHealthId");
        }
        try {
            byte[] file = DocumentUtils.streamToByteArray(body.getInputStream());
            Optional<Document> doc = null;
            doc = DocumentUtils.parseZipFile(file, identity.getEHealthId(), identity.getName());

            if (doc.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            if (!doc.get().getSsn().equals(ssn)) {
                throw new ForbiddenException("Document contains a different SSN than request");
            }
            if (!service.getByFileId(doc.get().getFileId()).isEmpty()) {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }

            Document savedDoc = service.save(doc.get());
            return new ResponseEntity<DocumentDto>(businessToDto(savedDoc), HttpStatus.OK);
        } catch (IOException e) {
            throw new DocumentFormatException("Unable to read document: " + e.getMessage());
        } catch (JAXBException e) {
            throw new DocumentFormatException("Document has incorrect format : " + e.getLinkedException().getMessage());
        } catch (SAXException e) {
            throw new DocumentFormatException("Document has incorrect format: " + e.getMessage());
        }

    }

    @Override
    @PreAuthorize("hasAnyAuthority('PATIENT')")
    public ResponseEntity<org.springframework.core.io.Resource> _getDocumentAsPdf(
            @Parameter(name = "ssn", description = "", required = true) @PathVariable("ssn") String ssn,
            @Parameter(name = "idDocument", description = "", required = true) @PathVariable("idDocument") String idDocument) {
        if (!identity.getName().equals(ssn)) {
            throw new ForbiddenException("You cannot access this document");
        }

        Optional<Byte[]> doc = service.getPdfById(UUID.fromString(idDocument));
        if (doc.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Resource resource = new ByteArrayResource(Utils.getBytes(doc.get()));
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('PATIENT')")
    public ResponseEntity<DocumentDto> _getDocument(@Pattern(regexp = "[0-9]{13}") @Size(min = 13, max = 13) String ssn,
            String idDocument) {

        if (!identity.getName().equals(ssn)) {
            throw new ForbiddenException("You cannot access this document");
        }

        Optional<Document> doc = service.getByFileId(UUID.fromString(idDocument));
        if (doc.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (!doc.get().getSsn().equals(identity.getName())) {
            throw new ForbiddenException("This document is not yours");
        }

        return new ResponseEntity<>(businessToDto(doc.get()), HttpStatus.OK);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('CNS')")
    public ResponseEntity<DocumentDto> _getDocumentAsCns(String idDocument, @Valid Object body) {
        Optional<Document> doc = service.getByFileId(UUID.fromString(idDocument));
        if (doc.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(businessToDto(doc.get()), HttpStatus.OK);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('PRACTITIONER')")
    public ResponseEntity<DocumentDto> _getDocumentFromPerson(
            @Pattern(regexp = "[0-9]{10}") @Size(min = 10, max = 10) String eHealthId,
            @Pattern(regexp = "[0-9]{13}") @Size(min = 13, max = 13) String ssn, String idDocument) {
        if (identity.getEHealthId() == null || !identity.getEHealthId().equals(eHealthId)) {
            throw new ForbiddenException("Invalid eHealthId");
        }
        Optional<Document> doc = service.getByFileId(UUID.fromString(idDocument));
        if (doc.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (!doc.get().getSsn().equals(ssn) || !doc.get().getEHealthId().equals(eHealthId)) {
            throw new ForbiddenException("This document is not yours");
        }

        return new ResponseEntity<>(businessToDto(doc.get()), HttpStatus.OK);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('PATIENT')")
    public ResponseEntity<List<DocumentDto>> _getDocumentsFromPerson(
            @Pattern(regexp = "[0-9]{13}") @Size(min = 13, max = 13) String ssn, @Valid Optional<Integer> page,
            @Valid Optional<Integer> size) {
        if (!identity.getName().equals(ssn)) {
            throw new ForbiddenException("You cannot access this document");
        }
        List<Document> docs = service.getAvailableDocuments(ssn);
        List<DocumentDto> res = docs.stream().map(this::businesstoDtoWithoutFile).collect(Collectors.toList());
        return new ResponseEntity<>(res, HttpStatus.OK);

    }

    @Override
    @PreAuthorize("hasAnyAuthority('PATIENT')")
    public ResponseEntity<Void> _sendDocument(@Pattern(regexp = "[0-9]{13}") @Size(min = 13, max = 13) String ssn,
            String idDocument) {

        if (!identity.getName().equals(ssn)) {
            throw new ForbiddenException("You cannot access this document");
        }
        Optional<Document> doc = service.getByFileId(UUID.fromString(idDocument));
        if (doc.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (doc.get().getDocumentStatus() != DocumentStatus.RECEIVED || !doc.get().getSsn().equals(ssn)) {
            throw new ForbiddenException("You cannot update this document");
        }
        if (!doc.get().isPaid()) {
            throw new ForbiddenException("You cannot update this document. MH is not paid and cannot be sent.");
        }
        doc.get().setDocumentStatus(DocumentStatus.TO_SEND);
        service.save(doc.get());
        return new ResponseEntity<>(HttpStatus.OK);

    }

    private DocumentDto businesstoDtoWithoutFile(Document doc) {
        DocumentDto dto = businessToDto(doc);
        dto.setFile(null);
        return dto;
    }

    private DocumentDto businessToDto(Document doc) {
        DocumentDto dto = new DocumentDto();
        dto.setAuthor(doc.getAuthor());
        dto.setCreateAt(doc.getCreatedAt());
        dto.setDocType(map(doc.getDocumentType()));
        dto.setFile(new String(doc.getFile(), StandardCharsets.UTF_8));
        dto.setSsn(doc.getSsn());
        dto.setStatus(map(doc.getDocumentStatus()));
        dto.setUuid(doc.getFileId().toString());
        dto.setPractitionerFirstname(doc.getPractitionerFirstname());
        dto.setPractitionerLastName(doc.getPractitionerLastname());
        dto.setPrice(new BigDecimal(String.format(Locale.ROOT, "%.02f", doc.getPrice())));
        dto.setIsPaid(doc.isPaid());
        dto.setMemoireDate(doc.getMemoireDate());
        return dto;
    }

    private Document dtoToBusiness(DocumentDto dto) {
        Document doc = new Document();
        doc.setAuthor(dto.getAuthor());
        doc.setCreatedAt(dto.getCreateAt());
        doc.setDocumentType(map(dto.getDocType()));
        doc.setFile(dto.getFile().getBytes(StandardCharsets.UTF_8));
        doc.setSsn(dto.getSsn());
        doc.setDocumentStatus(map(dto.getStatus()));
        doc.setFileId(UUID.fromString(dto.getUuid()));
        doc.setPractitionerFirstname(dto.getPractitionerFirstname());
        doc.setPractitionerLastname(dto.getPractitionerLastName());
        doc.setPrice(dto.getPrice().floatValue());
        doc.setPaid(dto.getIsPaid());
        doc.setMemoireDate(dto.getMemoireDate());
        return doc;
    }

    private DocumentDto.DocTypeEnum map(DocumentType type) {
        switch (type) {
            case MH:
                return DocTypeEnum.MH;
            default:
                throw new InvalidDocumentTypeException("Invalid document type");
        }
    }

    private DocumentType map(DocumentDto.DocTypeEnum type) {
        switch (type) {
            case MH:
                return DocumentType.MH;
            default:
                throw new InvalidDocumentTypeException("Invalid document type");
        }
    }

    private DocumentDto.StatusEnum map(DocumentStatus status) {
        switch (status) {
            case CANCEL:
                return StatusEnum.CANCEL;
            case CANCELED:
                return StatusEnum.CANCELED;
            case CANCEL_REPLACE:
                return StatusEnum.CANCEL_REPLACE;
            case RECEIVED:
                return StatusEnum.RECEIVED;
            case SENT:
                return StatusEnum.SENT;
            case TO_SEND:
                return StatusEnum.TO_SEND;
            default:
                throw new InvalidDocumentStatusException("Invalid status type");
        }
    }

    private DocumentStatus map(DocumentDto.StatusEnum status) {
        switch (status) {
            case CANCEL:
                return DocumentStatus.CANCEL;
            case CANCELED:
                return DocumentStatus.CANCELED;
            case CANCEL_REPLACE:
                return DocumentStatus.CANCEL_REPLACE;
            case RECEIVED:
                return DocumentStatus.RECEIVED;
            case SENT:
                return DocumentStatus.SENT;
            case TO_SEND:
                return DocumentStatus.TO_SEND;
            default:
                throw new InvalidDocumentStatusException("Invalid status type");
        }
    }

}
