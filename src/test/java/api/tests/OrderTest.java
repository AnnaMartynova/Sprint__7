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

        Response response = orderApiClient.createOrder(order);

        int track = response.then()
                .statusCode(SC_CREATED)
                .body("track", notNullValue())
                .body("track", greaterThan(0))
                .extract()
                .path("track");

        // Сохраняем трек-номер для очистки в @After
        saveCreatedOrderTrack(track);
    }

    @Test
    @DisplayName("Создание заказа с корректными данными")
    public void createOrderWithValidDataTest() {
        Order order = createTestOrder();
        order.setColor(Arrays.asList("BLACK"));

        Response response = orderApiClient.createOrder(order);

        int track = response.then()
                .statusCode(SC_CREATED)
                .body("track", notNullValue())
                .body("track", greaterThan(0))
                .extract()
                .path("track");

        // Сохраняем трек-номер для очистки в @After
        saveCreatedOrderTrack(track);
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
}