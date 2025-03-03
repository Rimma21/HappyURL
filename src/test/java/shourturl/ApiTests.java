package shourturl;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(properties = {
        "custom.domain=localhost",
        "server.port=8080"
})

public class ApiTests extends AbstractTestNGSpringContextTests {
    // These values must match the ones in TestPropertySource/application.properties
    private static final String DOMAIN = "localhost";
    private static final int PORT = 8080;
    private static final String BASE_SHORT_URL_PREFIX = "http://" + DOMAIN + ":" + PORT + "/my/";

    @BeforeClass
    public void setUp() {
        RestAssured.baseURI = "http://" + DOMAIN;
        RestAssured.port = PORT;
    }
    @Test
    public void testInvalidUrl() {
        String longUrl = "https://invalidurl.io";

        Response response =  given()
                 .queryParam("url", longUrl)
                 .when()
                 .get("/shorten")
                 .then()
                 .statusCode(400)
                 .extract().response();

        String errorMessage = response.jsonPath().getString("message");
        assertEquals(errorMessage, "Invalid URL format", "Error message invalid URL");
    }
    @Test
    public void testEmptyUrl() {
        // Create a shortened URL
        String longUrl = " ";

        Response response = given()
                .queryParam("url", longUrl)
                .when()
                .get("/shorten")
                .then()
                .statusCode(400)
                .extract().response();

        String errorMessage = response.jsonPath().getString("message");
        assertEquals(errorMessage, "Url can not be empty", "Error message empty URL");
    }
    //Test POST /shorten with invalid JSON payload
    @Test
    public void testShorten_invalidUrl() {
        String longUrl = "https://unvalid.com";
        Response response = given()
                 .contentType(ContentType.JSON)
                 .body("{\"url\":\"" + longUrl + "\"}")
                 .when()
                 .post("/shorten")
                 .then()
                 .statusCode(400)
                 .contentType(ContentType.JSON)
                 .extract().response();

        String returnedUrl = response.jsonPath().getString("url");
        String fullShortUrl = response.jsonPath().getString("shortUrl");
        assertEquals(returnedUrl, longUrl, "Returned URL should match the input");
        String errorMessage = response.jsonPath().getString("message");
        assertEquals(errorMessage, "Invalid URL format", "Error message should indicate invalid URL format");
    }
    @Test
    public void testShorten_invalidJson() {
    String invalidJson = "{\"url\": \"invalidUrl\"";
        Response response =
                given()
                .contentType(ContentType.JSON)
                .body(invalidJson)
                .when()
                .post("/shorten")
                .then()
                .statusCode(400)
                .extract().response();

        String errorMessage = response.jsonPath().getString("message");
        assertEquals(errorMessage, "Invalid JSON format", "Error message  invalid JSON format");
    }
    @Test
    public void testShorten_emptyJson() {
    String emptyJson = " ";
        Response response =
                given()
                .contentType(ContentType.JSON)
                .body(emptyJson)
                .when()
                .post("/shorten")
                .then()
                .statusCode(400)
                .extract().response();

        String errorMessage = response.jsonPath().getString("message");
        assertEquals(errorMessage, "Empty JSON", "Error message empty JSON");
    }
    @Test
    public void testInvalidId() {
    String invalidId = "invalid-id-format";
        Response response =
                given()
                .redirects().follow(false)
                .when()
                .get("/my/" + invalidId)
                .then()
                .statusCode(400)
                .extract().response();

        String errorMessage = response.jsonPath().getString("message");
        assertEquals(errorMessage, "Invalid URL ID format", "Error message for invalid ID format");
    }
    @Test
    public void testNonExistentId() {
        Long nonExistentId = 999999L;
        Response response =
                given()
                .redirects().follow(false)
                .when()
                .get("/my/" + nonExistentId)
                .then()
                .statusCode(404)
                .extract().response();
        String errorMessage = response.jsonPath().getString("message");
        assertEquals(errorMessage, "URL not found", "Error message for non-existent ID");
    }
    @Test
    public void testEmptyId() {
        String emptyId = " ";
        Response response =
                given()
                .redirects().follow(false)
                .when()
                .get("/my/" + emptyId)
                .then()
                .statusCode(404)
                .extract().response();

        String errorMessage = response.jsonPath().getString("message");
        assertEquals(errorMessage, "URL not found", "Error message for empty ID");
    }
    @Test
    public void testAlreadyShortenedUrl() {
        String longUrl = "https://already-shortened.com";
        String firstShortUrl =
                given()
                .queryParam("url", longUrl)
                .when()
                .get("/shorten_simple")
                .then()
                .statusCode(200)
                .extract().jsonPath().getString("shortUrl");
        Response response =
                given()
                .queryParam("url", longUrl)
                .when()
                .get("/shorten_simple")
                .then()
                .statusCode(200)
                .extract().response();

        String secondShortUrl = response.jsonPath().getString("shortUrl");
        assertEquals(firstShortUrl, secondShortUrl, "URL has been previously shortened");
    }
    @Test
    public void testDoubleShortening() {
        String longUrl = "https://already-shortened.com";
        String firstShortUrl =
                given()
                .queryParam("url", longUrl)
                .when()
                .get("/shorten_simple")
                .then()
                .statusCode(200)
                 .extract().jsonPath().getString("shortUrl");
        Response response =
                given()
                .queryParam("url", firstShortUrl)
                .when()
                .get("/shorten_simple")
                .then()
                .statusCode(200)
                .extract().response();

        String secondShortUrl = response.jsonPath().getString("shortUrl");
        assertNotEquals(firstShortUrl, secondShortUrl, "The second shortened URL should be different from the first one");}

    private Response shortenUrl(String url) {
        return  given()
                .contentType(ContentType.JSON)
                .body("{\"url\":\"" + url + "\"}")
                .when()
                .post("/shorten")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response();
    }
    @Test
    public void testDoubleShortenSecondWay() {
        String longUrl = "https://openai.com";

        Response response = shortenUrl(longUrl);
        String fullShortUrl1 = response.jsonPath().getString("shortUrl");

        response = shortenUrl(fullShortUrl1);
        String fullShortUrl2 = response.jsonPath().getString("shortUrl");
        assertEquals(fullShortUrl2, fullShortUrl1, "The second shortened URL should be different from the first one");
    }
}


