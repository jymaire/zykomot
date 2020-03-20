package org.zenika.zykomot;

import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/patient")
@Produces("application/json")
@Consumes("application/json")
public class PatientResource {

    @GET
    public List<Patient> getAll(){
        return Patient.listAll();
    }
    @POST
    @Transactional
    public Response create(Patient patient){
        Patient.persist(patient);
        return Response.status(Response.Status.CREATED).entity(patient).build();
    }
}