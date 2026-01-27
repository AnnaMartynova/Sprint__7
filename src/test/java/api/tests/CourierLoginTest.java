package api.tests;

import api.models.Courier;
import api.steps.TestBase;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.*;

@DisplayName("Тесты API авторизации курьера")
public class CourierLoginTest extends TestBase {

    private String testLogin;
    private String testPassword;

    @Before
    public void setUpTestCourier() {
        // Создаем курьера перед тестами авторизации
        String timestamp = String.valueOf(System.currentTimeMillis());
        testLogin = "login_test_" + timestamp;
        testPassword = "auth_password_" + timestamp;
        String firstName = "Авторизация_" + timestamp;

        Courier testCourier = new Courier(testLogin, testPassword, firstName);

        // Регистрируем курьера
        createCourierStep(testCourier)
                .then()
                .statusCode(SC_CREATED);
    }

    @Test
    @DisplayName("Курьер может успешно авторизоваться")
    public void courierCanLoginSuccessfullyTest() {
        loginCourierStep(testLogin, testPassword)
                .then()
                .statusCode(SC_OK)
                .body("id", notNullValue())
                .log().all();
    }

    @Test
    @DisplayName("Авторизация с неправильным паролем возвращает ошибку")
    public void loginWithWrongPasswordShouldFailTest() {
        loginCourierStep(testLogin, "wrong_password")
                .then()
                .statusCode(SC_NOT_FOUND)
                .body("message", equalTo("Учетная запись не найдена"))
                .log().all();
    }

    @Test
    @DisplayName("Авторизация с неправильным логином возвращает ошибку")
    public void loginWithWrongLoginShouldFailTest() {
        loginCourierStep("wrong_login", testPassword)
                .then()
                .statusCode(SC_NOT_FOUND)
                .body("message", equalTo("Учетная запись не найдена"))
                .log().all();
    }

    @Test
    @DisplayName("Авторизация без логина возвращает ошибку")
    public void loginWithoutLoginShouldFailTest() {
        given()
                .header("Content-type", "application/json")
                .body("{\"password\": \"" + testPassword + "\"}")
                .when()
                .post("/api/v1/courier/login")
                .then()
                .statusCode(SC_BAD_REQUEST)
                .body("message", equalTo("Недостаточно данных для входа"))
                .log().all();
    }

    @Test
    @DisplayName("Авторизация без пароля возвращает ошибку")
    public void loginWithoutPasswordShouldFailTest() {
        given()
                .header("Content-type", "application/json")
                .body("{\"login\": \"" + testLogin + "\"}")
                .when()
                .post("/api/v1/courier/login")
                .then()
                .statusCode(SC_BAD_REQUEST)
                .body("message", equalTo("Недостаточно данных для входа"))
                .log().all();
    }

    @Test
    @DisplayName("Авторизация несуществующего пользователя возвращает ошибку")
    public void loginNonExistentUserShouldFailTest() {
        loginCourierStep("non_existent_user", "any_password")
                .then()
                .statusCode(SC_NOT_FOUND)
                .body("message", equalTo("Учетная запись не найдена"))
                .log().all();
    }

    @Step("Отправка запроса на авторизацию курьера")
    private Response loginCourierStep(String login, String password) {
        return given()
                .header("Content-type", "application/json")
                .body("{\"login\": \"" + login + "\", \"password\": \"" + password + "\"}")
                .when()
                .post("/api/v1/courier/login")
                .then()
                .log().ifError()
                .extract()
                .response();
    }

    @Step("Создание курьера")
    private Response createCourierStep(Courier courier) {
        return given()
                .header("Content-type", "application/json")
                .body(courier)
                .when()
                .post("/api/v1/courier")
                .then()
                .extract()
                .response();
    }

    @After
    public void tearDown() {
        // Удаляем тестового курьера
        if (testLogin != null && testPassword != null) {
            deleteTestCourier(testLogin, testPassword);
        }
    }
}