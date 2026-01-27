package api.steps;

import api.models.Courier;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.Data;

import org.junit.Before;

import static io.restassured.RestAssured.given;

public class TestBase {

    @Before
    public void setUp() {
        RestAssured.baseURI = "http://qa-scooter.praktikum-services.ru";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Step("Создание тестового курьера")
    protected Courier createTestCourier() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        return new Courier(
                "courier_" + timestamp,
                "password_" + timestamp,
                "TestCourier_" + timestamp
        );
    }

    @Step("Удаление тестового курьера")
    protected void deleteTestCourier(String login, String password) {
        try {
            CourierCredentials credentials = new CourierCredentials(login, password);
            // Сначала получаем ID курьера для удаления
            Response loginResponse = given()
                    .header("Content-type", "application/json")
                    .body(credentials)
                    .when()
                    .post("/api/v1/courier/login");

            if (loginResponse.statusCode() == 200) {
                String courierId = loginResponse.jsonPath().getString("id");

                // Удаляем курьера
                given()
                        .when()
                        .delete("/api/v1/courier/" + courierId)
                        .then()
                        .statusCode(200);
            }
        } catch (Exception e) {
            // Игнорируем ошибки при удалении
        }
    }

    @Step("Авторизация курьера")
    protected String loginCourier(String login, String password) {
        CourierCredentials credentials = new CourierCredentials(login, password);

        Response response = given()
                .header("Content-type", "application/json")
                .body(credentials)
                .when()
                .post("/api/v1/courier/login");

        return response.jsonPath().getString("id");
    }

    // Внутренний класс для авторизации
    @Data
    public static class CourierCredentials {
        private String login;
        private String password;

        public CourierCredentials(){}

        public CourierCredentials(String login, String password) {
            this.login = login;
            this.password = password;
        }

        public String getLogin() {
            return login;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public void setLogin(String login) {
            this.login = login;
        }
    }
}