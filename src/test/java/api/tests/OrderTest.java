package api.tests;

import api.models.Order;
import api.steps.TestBase;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.hamcrest.Matchers.*;

@RunWith(Parameterized.class)
@DisplayName("Тесты API создания заказа")
public class OrderTest extends TestBase {

    private final List<String> colors;

    public OrderTest(List<String> colors) {
        this.colors = colors;
    }

    @Parameterized.Parameters(name = "Цвета: {0}")
    public static Object[][] getColors() {
        return new Object[][] {
                {Arrays.asList("BLACK")},           // Только BLACK
                {Arrays.asList("GREY")},            // Только GREY
                {Arrays.asList("BLACK", "GREY")},   // Оба цвета
                {null},                             // Без указания цвета
                {Arrays.asList()}                   // Пустой список
        };
    }

    @Test
    @DisplayName("Создание заказа с различными параметрами цвета")
    public void createOrderWithDifferentColorOptionsTest() {
        Order order = createTestOrder();
        order.setColor(colors);

        createOrderStep(order)
                .then()
                .statusCode(SC_CREATED)
                .body("track", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа с корректными данными")
    public void createOrderWithValidDataTest() {
        Order order = createTestOrder();
        order.setColor(Arrays.asList("BLACK"));

        createOrderStep(order)
                .then()
                .statusCode(SC_CREATED)
                .body("track", notNullValue())
                .body("track", greaterThan(0));
    }

    @Step("Создание тестового заказа")
    private Order createTestOrder() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        return new Order(
                "Иван_" + timestamp,
                "Петров",
                "ул. Тестовая, д. " + timestamp,
                "Сокольники",
                "+7999123" + timestamp.substring(timestamp.length() - 4),
                3,
                "2024-12-31",
                "Тестовый заказ " + timestamp,
                null
        );
    }

    @Step("Отправка запроса на создание заказа")
    private Response createOrderStep(Order order) {
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
}