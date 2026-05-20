package api.steps;

import api.client.CourierApiClient;
import api.client.OrderApiClient;
import api.models.Courier;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.Data;
import org.junit.After;
import org.junit.Before;

import static org.apache.http.HttpStatus.SC_OK;

public class TestBase {

    protected CourierApiClient courierApiClient;
    protected OrderApiClient orderApiClient;
    private Integer createdOrderTrack;

    @Before
    @Step("Настройка базовых параметров API")
    public void setUp() {
        RestAssured.baseURI = "http://qa-scooter.praktikum-services.ru";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        courierApiClient = new CourierApiClient();
        orderApiClient = new OrderApiClient();
        createdOrderTrack = null;
    }

    @After
    @Step("Очистка тестовых данных")
    public void tearDown() {
        // Отмена созданного заказа, если он есть
        if (createdOrderTrack != null) {
            cancelTestOrder(createdOrderTrack);
        }
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

    @Step("Удаление тестового курьера: login={login}")
    protected void deleteTestCourier(String login, String password) {
        try {
            CourierCredentials credentials = new CourierCredentials(login, password);
            Response loginResponse = courierApiClient.loginCourier(credentials);

            if (loginResponse.statusCode() == SC_OK) {
                String courierId = loginResponse.jsonPath().getString("id");
                courierApiClient.deleteCourier(courierId)
                        .then()
                        .statusCode(SC_OK);
            }
        } catch (Exception e) {
            // Игнорируем ошибки при удалении
        }
    }

    @Step("Отмена тестового заказа: track={track}")
    protected void cancelTestOrder(Integer track) {
        try {
            if (track != null && track > 0) {
                orderApiClient.cancelOrder(track);
            }
        } catch (Exception e) {
            // Игнорируем ошибки при отмене заказа
        }
    }

    @Step("Сохранение трек-номера созданного заказа: {track}")
    protected void saveCreatedOrderTrack(Integer track) {
        this.createdOrderTrack = track;
    }

    @Step("Авторизация курьера: login={login}")
    protected String loginCourier(String login, String password) {
        CourierCredentials credentials = new CourierCredentials(login, password);
        Response response = courierApiClient.loginCourier(credentials);
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

        public void setPassword(String password) {
            this.password = password;
        }

        public void setLogin(String login) {
            this.login = login;
        }
    }
}