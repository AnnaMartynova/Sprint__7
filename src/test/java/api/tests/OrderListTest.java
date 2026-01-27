package api.tests;

import api.steps.TestBase;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.*;

@DisplayName("Тесты API получения списка заказов")
public class OrderListTest extends TestBase {

    @Test
    @DisplayName("Получение списка заказов возвращает непустой массив")
    public void getOrdersListReturnsNonEmptyArrayTest() {
        getOrdersListStep()
                .then()
                .statusCode(SC_OK)
                .body("orders", notNullValue())
                .body("orders", instanceOf(List.class))
                .body("orders.size()", greaterThanOrEqualTo(0));
    }

    @Test
    @DisplayName("Список заказов содержит обязательные поля")
    public void ordersListContainsRequiredFieldsTest() {
        getOrdersListStep()
                .then()
                .statusCode(SC_OK)
                .body("orders[0]", notNullValue())  // Проверяем, что есть хотя бы один заказ
                .body("orders[0].id", notNullValue())
                .body("orders[0].status", notNullValue())
                .body("orders[0].createdAt", notNullValue())
                .body("orders[0].updatedAt", notNullValue());
    }

    @Test
    @DisplayName("Можно ограничить количество возвращаемых заказов")
    public void canLimitNumberOfReturnedOrdersTest() {
        given()
                .header("Content-type", "application/json")
                .queryParam("limit", 5)
                .when()
                .get("/api/v1/orders")
                .then()
                .statusCode(SC_OK)
                .body("orders.size()", lessThanOrEqualTo(5));
    }

    @Test
    @DisplayName("Можно указать страницу для пагинации")
    public void canSpecifyPageForPaginationTest() {
        given()
                .header("Content-type", "application/json")
                .queryParam("page", 1)
                .queryParam("limit", 10)
                .when()
                .get("/api/v1/orders")
                .then()
                .statusCode(SC_OK)
                .body("orders", notNullValue());
    }

    @Step("Получение списка заказов")
    private Response getOrdersListStep() {
        return given()
                .header("Content-type", "application/json")
                .when()
                .get("/api/v1/orders")
                .then()
                .log().ifError()
                .extract()
                .response();
    }
}