package org.zenika.zykomot;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.CREATED;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class PatientResourceTest {

    @Test
    public void testGetAllEndpoint() {
        given()
                .when().get("/patient")
                .then()
                .statusCode(200)
                .body(is("[{\"id\":1,\"firstName\":\"Robert\",\"name\":\"Michu\"},{\"id\":2,\"firstName\":\"Jeanne\",\"name\":\"Dupont\"}]"));
    }

    @Test
    void testCreateEndpoint() {
        given()
                .body("{\"firstName\":\"Raoul\",\"name\":\"Ferdinand\"}")
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .header(ACCEPT, APPLICATION_JSON)
                .when()
                .post("/patient")
                .then()
                .statusCode(CREATED.getStatusCode());

        /* It would have been great to use a bean definition, like described right after, but for the moment,
        this is not ready in Quarkus, see issue https://github.com/quarkusio/quarkus-quickstarts/issues/205
        Patient patient = new Patient();
        patient.name = "Ferdinand";
        patient.firstName = "Raoul";
         */
    }

}