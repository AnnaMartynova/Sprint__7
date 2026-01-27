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
        // Создаем объект для авторизации
        CourierCredentials credentials = new CourierCredentials(testLogin, testPassword);

        loginCourierStep(credentials)
                .then()
                .statusCode(SC_OK)
                .body("id", notNullValue())
                .log().all();
    }

    @Test
    @DisplayName("Авторизация с неправильным паролем возвращает ошибку")
    public void loginWithWrongPasswordShouldFailTest() {
        // Создаем объект с неправильным паролем
        CourierCredentials credentials = new CourierCredentials(testLogin, "wrong_password");

        loginCourierStep(credentials)
                .then()
                .statusCode(SC_NOT_FOUND)
                .body("message", equalTo("Учетная запись не найдена"))
                .log().all();
    }

    @Test
    @DisplayName("Авторизация с неправильным логином возвращает ошибку")
    public void loginWithWrongLoginShouldFailTest() {
        // Создаем объект с неправильным логином
        CourierCredentials credentials = new CourierCredentials("wrong_login", testPassword);

        loginCourierStep(credentials)
                .then()
                .statusCode(SC_NOT_FOUND)
                .body("message", equalTo("Учетная запись не найдена"))
                .log().all();
    }

    @Test
    @DisplayName("Авторизация без логина возвращает ошибку")
    public void loginWithoutLoginShouldFailTest() {
        // Создаем объект без логина
        CourierCredentials credentials = new CourierCredentials();
        credentials.setPassword(testPassword);

        loginCourierStep(credentials)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .body("message", equalTo("Недостаточно данных для входа"))
                .log().all();
    }

    @Test
    @DisplayName("Авторизация без пароля возвращает ошибку")
    public void loginWithoutPasswordShouldFailTest() {
        // Создаем объект без пароля
        CourierCredentials credentials = new CourierCredentials();
        credentials.setLogin(testLogin);

        loginCourierStep(credentials)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .body("message", equalTo("Недостаточно данных для входа"))
                .log().all();
    }

    @Test
    @DisplayName("Авторизация несуществующего пользователя возвращает ошибку")
    public void loginNonExistentUserShouldFailTest() {
        // Создаем объект для несуществующего пользователя
        CourierCredentials credentials = new CourierCredentials("non_existent_user", "any_password");

        loginCourierStep(credentials)
                .then()
                .statusCode(SC_NOT_FOUND)
                .body("message", equalTo("Учетная запись не найдена"))
                .log().all();
    }

    @Step("Отправка запроса на авторизацию курьера")
    private Response loginCourierStep(CourierCredentials credentials) {
        return given()
                .header("Content-type", "application/json")
                .body(credentials) // Сериализуем объект в JSON
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