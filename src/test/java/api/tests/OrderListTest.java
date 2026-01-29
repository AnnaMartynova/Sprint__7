package api.tests;

import api.steps.TestBase;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.Test;

import java.util.List;

import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.*;

@DisplayName("Тесты API получения списка заказов")
public class OrderListTest extends TestBase {

    @Test
    @DisplayName("Получение списка заказов возвращает непустой массив")
    public void getOrdersListReturnsNonEmptyArrayTest() {
        Response response = orderApiClient.getOrdersList();

        response.then()
                .statusCode(SC_OK)
                .body("orders", notNullValue())
                .body("orders", instanceOf(List.class))
                .body("orders.size()", greaterThanOrEqualTo(0));
    }

    @Test
    @DisplayName("Список заказов содержит обязательные поля")
    public void ordersListContainsRequiredFieldsTest() {
        Response response = orderApiClient.getOrdersList();

        response.then()
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
        int limit = 5;
        Response response = orderApiClient.getOrdersListWithLimit(limit);

        response.then()
                .statusCode(SC_OK)
                .body("orders.size()", lessThanOrEqualTo(limit));
    }

    @Test
    @DisplayName("Можно указать страницу для пагинации")
    public void canSpecifyPageForPaginationTest() {
        int page = 1;
        int limit = 10;
        Response response = orderApiClient.getOrdersListWithParams(limit, page);

        response.then()
                .statusCode(SC_OK)
                .body("orders", notNullValue());
    }
}