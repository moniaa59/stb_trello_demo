package board;

import base.BaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class BoardTest extends BaseTest {

    @Test
    public void createNewBoard() {
        Response response = given()
                .spec(reqSpec)
                .queryParam("name", "My first board")
                .when()
                .post(BASE_URL + BOARDS)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();


        JsonPath json = response.jsonPath();

        assertThat(json.getString("name")).isEqualTo("My first board");

        String boardId = json.get("id");

        given()
                .spec(reqSpec)
                .when()
                .delete(BASE_URL + BOARDS + boardId)
                .then()
                .statusCode(HttpStatus.SC_OK);

    }

    @Test
    public void createNewBoardEmptyName() {
        Response response = given()
                .spec(reqSpec)
                .queryParam("name", "")
                .when()
                .post(BASE_URL + BOARDS)
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract()
                .response();
    }

    @Test
    public void createNewBoardWithoutDefaultLists() {
        Response response = given()
                .spec(reqSpec)
                .queryParam("name", "My first board")
                .queryParam("defaultLists", false)
                .when()
                .post(BASE_URL + BOARDS)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();

        JsonPath json = response.jsonPath();

        assertThat(json.getString("name")).isEqualTo("My first board");

        String boardId = json.get("id");

        Response responseGet = given()
                .spec(reqSpec)
                .when()
                .get(BASE_URL + BOARDS + boardId + LISTS)
                .then()
                .statusCode(200)
                .extract()
                .response();

        JsonPath jsonGet = responseGet.jsonPath();
        List<String> idList = jsonGet.getList("id");

        assertThat(idList.size()).isEqualTo(0);

        given()
                .spec(reqSpec)
                .when()
                .delete(BASE_URL + BOARDS + boardId)
                .then()
                .statusCode(HttpStatus.SC_OK);

    }

    @Test
    public void createNewBoardWithDefaultLists() {
        Response response = given()
                .spec(reqSpec)
                .queryParam("name", "My board with default lists")
                .queryParam("defaultLists", true)
                .when()
                .post(BASE_URL + BOARDS)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();

        JsonPath json = response.jsonPath();
        assertThat(json.getString("name")).isEqualTo("My board with default lists");

        String boardId = json.get("id");

        Response responseGet = given()
                .spec(reqSpec)
                .when()
                .get(BASE_URL + BOARDS + boardId + LISTS)
                .then()
                .statusCode(200)
                .extract()
                .response();

        JsonPath jsonGet = responseGet.jsonPath();
        List<String> nameList = jsonGet.getList("name");

        assertThat(nameList).hasSize(3).contains("Do zrobienia", "Zrobione", "W trakcie");

        given()
                .spec(reqSpec)
                .when()
                .delete(BASE_URL + BOARDS + boardId)
                .then()
                .statusCode(HttpStatus.SC_OK);


    }

}
