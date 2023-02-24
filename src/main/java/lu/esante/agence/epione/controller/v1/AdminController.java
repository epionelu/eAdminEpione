package lu.esante.agence.epione.controller.v1;

import org.openapitools.api.AdminApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import lu.esante.agence.epione.controller.V1AbstractController;
import lu.esante.agence.epione.service.impl.admin.AdminServiceImpl;

@RestController
public class AdminController extends V1AbstractController implements AdminApi {

    @Autowired
    private AdminServiceImpl adminService;

    @Override
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<Void> _getData() throws Exception {
        return null;
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<Void> _getStatistics() throws Exception {
        adminService.exportCnsStat();
        return null;
    }
}
