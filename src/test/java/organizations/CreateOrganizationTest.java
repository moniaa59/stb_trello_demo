package organizations;

import base.BaseTest;
import com.github.javafaker.Faker;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.*;

public class CreateOrganizationTest extends BaseTest {

    private static String organizationId;

    @Test
    public void createNewOrganization() {

        Response response = given()
                .spec(reqSpec)
                .queryParam("displayName", "New organization")
                .queryParam("name", "abc")
                .when()
                .post(BASE_URL + ORGANIZATIONS)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();

        JsonPath json = response.jsonPath();

        assertThat(json.getString("displayName")).isEqualTo("New organization");

//        assertThat(json.getString("name")).isEqualTo("abc");
//        assertions isEqualto for name will not work - name is generated with other signs - no information in docs (https://developer.atlassian.com/)

        assertThat(json.getString("name")).contains("abc");


        organizationId = json.getString("id");

        given()
                .spec(reqSpec)
                .when()
                .delete(BASE_URL + ORGANIZATIONS + organizationId)
                .then()
                .statusCode(HttpStatus.SC_OK);

    }

    @Test
    public void createNewOrganizationWithoutDisplayName() {
        given()
                .spec(reqSpec)
                .queryParam("displayName", "")
                .when()
                .post(BASE_URL + ORGANIZATIONS)
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void createNewOrganizationWithIncorrectWebsite() {
        Response response = given()
                .spec(reqSpec)
                .queryParam("displayName", "New organization")
                .queryParam("website", "www.abc.pl")
                .when()
                .post(BASE_URL + ORGANIZATIONS)
                .then()
                .statusCode(HttpStatus.SC_OK)
                //it should not be Status code 200 - protocol http or https is required - no information in docs (https://developer.atlassian.com/)
                .extract()
                .response();

        JsonPath json = response.jsonPath();

        assertThat(json.getString("website")).startsWith("http://");

        organizationId = json.getString("id");

        given()
                .spec(reqSpec)
                .when()
                .delete(BASE_URL + ORGANIZATIONS + organizationId)
                .then()
                .statusCode(HttpStatus.SC_OK);

    }

    @Test
    public void createNewOrganizationWithUppercaseLetters() {

        Response response = given()
                .spec(reqSpec)
                .queryParam("displayName", "New organization")
                .queryParam("name", "ABC")
                .when()
                .post(BASE_URL + ORGANIZATIONS)
                .then()
                .statusCode(HttpStatus.SC_OK)
                //it should not be Status code 200 - lowercase letters are required - no information in docs (https://developer.atlassian.com/)
                .extract()
                .response();

        JsonPath json = response.jsonPath();

        assertThat(json.getString("displayName")).isEqualTo("New organization");
        assertThat(json.getString("name")).isLowerCase();

        organizationId = json.getString("id");

        given()
                .spec(reqSpec)
                .when()
                .delete(BASE_URL + ORGANIZATIONS + organizationId)
                .then()
                .statusCode(HttpStatus.SC_OK);

    }

    @Test
    public void createNewOrganizationsWithNotUniqueNames() {

        String fakeOrganizationName = new Faker().company().name();

        Response response = given()
                .spec(reqSpec)
                .queryParam("displayName", "New organization")
                .queryParam("name", fakeOrganizationName)
                .when()
                .post(BASE_URL + ORGANIZATIONS)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();

        JsonPath json = response.jsonPath();
        String organizationName1 = json.getString("name");

        Response response2 = given()
                .spec(reqSpec)
                .queryParam("displayName", "New organization")
                .queryParam("name", fakeOrganizationName)
                .when()
                .post(BASE_URL + ORGANIZATIONS)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();

        JsonPath json2 = response2.jsonPath();
        String organizationName2 = json2.getString("name");

        assertThat(organizationName1).isNotEqualTo(organizationName2);

        organizationId = json.getString("id");

        given()
                .spec(reqSpec)
                .when()
                .delete(BASE_URL + ORGANIZATIONS + organizationId)
                .then()
                .statusCode(HttpStatus.SC_OK);

        organizationId = json2.getString("id");

        given()
                .spec(reqSpec)
                .when()
                .delete(BASE_URL + ORGANIZATIONS + organizationId)
                .then()
                .statusCode(HttpStatus.SC_OK);

    }

}
