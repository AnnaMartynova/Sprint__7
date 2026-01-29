package api.client;

import api.models.Order;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class OrderApiClient {

    @Step("Создание заказа")
    public Response createOrder(Order order) {
        return given()
                .header("Content-type", "application/json")
                .body(order)
                .when()
                .post("/api/v1/orders")
                .then()
                .log().ifError()
                .extract()
                .response();
    }

    @Step("Отмена заказа по трек-номеру: {track}")
    public Response cancelOrder(int track) {
        return given()
                .header("Content-type", "application/json")
                .body("{\"track\": " + track + "}")
                .when()
                .put("/api/v1/orders/cancel")
                .then()
                .log().ifError()
                .extract()
                .response();
    }

    @Step("Получение списка заказов")
    public Response getOrdersList() {
        return given()
                .header("Content-type", "application/json")
                .when()
                .get("/api/v1/orders")
                .then()
                .log().ifError()
                .extract()
                .response();
    }

    @Step("Получение списка заказов с параметрами")
    public Response getOrdersListWithParams(Integer limit, Integer page) {
        return given()
                .header("Content-type", "application/json")
                .queryParam("limit", limit)
                .queryParam("page", page)
                .when()
                .get("/api/v1/orders")
                .then()
                .log().ifError()
                .extract()
                .response();
    }

    @Step("Получение списка заказов с лимитом")
    public Response getOrdersListWithLimit(Integer limit) {
        return given()
                .header("Content-type", "application/json")
                .queryParam("limit", limit)
                .when()
                .get("/api/v1/orders")
                .then()
                .log().ifError()
                .extract()
                .response();
    }
}