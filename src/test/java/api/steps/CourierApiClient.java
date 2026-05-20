package api.client;

import api.models.Courier;
import api.steps.TestBase.CourierCredentials;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class CourierApiClient {

    @Step("Создание курьера")
    public Response createCourier(Courier courier) {
        return given()
                .header("Content-type", "application/json")
                .body(courier)
                .when()
                .post("/api/v1/courier");
    }

    @Step("Авторизация курьера")
    public Response loginCourier(CourierCredentials credentials) {
        return given()
                .header("Content-type", "application/json")
                .body(credentials)
                .when()
                .post("/api/v1/courier/login");
    }

    @Step("Удаление курьера по ID: {courierId}")
    public Response deleteCourier(String courierId) {
        return given()
                .when()
                .delete("/api/v1/courier/" + courierId);
    }
}