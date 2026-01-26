package api;

import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("Тесты API создания курьера")
public class CourierTest extends TestBase {

    private Courier testCourier;
    private String testLogin;
    private String testPassword;

    @Test
    @DisplayName("Курьера можно создать с корректными данными")
    public void createCourierWithValidDataSuccess() {
        // Создаем уникальные данные для теста
        String timestamp = String.valueOf(System.currentTimeMillis());
        testLogin = "test_courier_" + timestamp;
        testPassword = "password123";
        String firstName = "Иван_" + timestamp;

        testCourier = new Courier(testLogin, testPassword, firstName);

        createCourierStep(testCourier)
                .then()
                .statusCode(201)
                .body("ok", equalTo(true));
    }

    @Test
    @DisplayName("Нельзя создать двух одинаковых курьеров")
    public void createDuplicateCourierShouldFail() {
        // Создаем уникального курьера
        String timestamp = String.valueOf(System.currentTimeMillis());
        testLogin = "duplicate_test_" + timestamp;
        testPassword = "password456";
        String firstName = "Дубликат_" + timestamp;

        testCourier = new Courier(testLogin, testPassword, firstName);

        // Первое создание - успешно
        createCourierStep(testCourier)
                .then()
                .statusCode(201)
                .log().all();

        // Второе создание - должно вернуть ошибку
        createCourierStep(testCourier)
                .then()
                .statusCode(409)
                .body("message", equalTo("Этот логин уже используется. Попробуйте другой."));
    }

    @Test
    @DisplayName("Создание курьера без логина возвращает ошибку")
    public void createCourierWithoutLoginShouldFail() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        testPassword = "password789";
        String firstName = "БезЛогина_" + timestamp;

        // Создаем курьера без логина
        Courier courierWithoutLogin = new Courier();
        courierWithoutLogin.setPassword(testPassword);
        courierWithoutLogin.setFirstName(firstName);

        createCourierStep(courierWithoutLogin)
                .then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @DisplayName("Создание курьера без пароля возвращает ошибку")
    public void createCourierWithoutPasswordShouldFail() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        testLogin = "no_password_" + timestamp;
        String firstName = "БезПароля_" + timestamp;

        // Создаем курьера без пароля
        Courier courierWithoutPassword = new Courier();
        courierWithoutPassword.setLogin(testLogin);
        courierWithoutPassword.setFirstName(firstName);

        createCourierStep(courierWithoutPassword)
                .then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @DisplayName("Успешное создание возвращает ok: true")
    public void successfulCreationReturnsOkTrue() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        testLogin = "success_test_" + timestamp;
        testPassword = "password000";
        String firstName = "Успешный_" + timestamp;

        testCourier = new Courier(testLogin, testPassword, firstName);

        createCourierStep(testCourier)
                .then()
                .assertThat()
                .body("ok", equalTo(true));
    }

    @Step("Отправка запроса на создание курьера")
    private Response createCourierStep(Courier courier) {
        return given()
                .header("Content-type", "application/json")
                .body(courier)
                .when()
                .post("/api/v1/courier")
                .then()
                .log().ifError()
                .extract()
                .response();
    }

    @After
    public void tearDown() {
        // Удаляем созданного курьера после каждого теста.
        if (testLogin != null && testPassword != null) {
            deleteTestCourier(testLogin, testPassword);
        }
    }
}