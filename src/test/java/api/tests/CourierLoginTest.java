package api.tests;

import api.models.Courier;
import api.steps.TestBase;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.*;

@DisplayName("Тесты API авторизации курьера")
public class CourierLoginTest extends TestBase {

    private String testLogin;
    private String testPassword;

    @Before
    @Step("Подготовка тестовых данных - создание курьера")
    public void setUpTestCourier() {
        // Создаем курьера перед тестами авторизации
        String timestamp = String.valueOf(System.currentTimeMillis());
        testLogin = "login_test_" + timestamp;
        testPassword = "auth_password_" + timestamp;
        String firstName = "Авторизация_" + timestamp;

        Courier testCourier = new Courier(testLogin, testPassword, firstName);

        // Регистрируем курьера
        courierApiClient.createCourier(testCourier)
                .then()
                .statusCode(SC_CREATED);
    }

    @Test
    @DisplayName("Курьер может успешно авторизоваться")
    public void courierCanLoginSuccessfullyTest() {
        // Создаем объект для авторизации
        CourierCredentials credentials = new CourierCredentials(testLogin, testPassword);

        performLoginAndAssertSuccess(credentials);
    }

    @Test
    @DisplayName("Авторизация с неправильным паролем возвращает ошибку")
    public void loginWithWrongPasswordShouldFailTest() {
        // Создаем объект с неправильным паролем
        CourierCredentials credentials = new CourierCredentials(testLogin, "wrong_password");

        performLoginAndAssertNotFound(credentials, "Учетная запись не найдена");
    }

    @Test
    @DisplayName("Авторизация с неправильным логином возвращает ошибку")
    public void loginWithWrongLoginShouldFailTest() {
        // Создаем объект с неправильным логином
        CourierCredentials credentials = new CourierCredentials("wrong_login", testPassword);

        performLoginAndAssertNotFound(credentials, "Учетная запись не найдена");
    }

    @Test
    @DisplayName("Авторизация без логина возвращает ошибку")
    public void loginWithoutLoginShouldFailTest() {
        // Создаем объект без логина
        CourierCredentials credentials = new CourierCredentials();
        credentials.setPassword(testPassword);

        performLoginAndAssertBadRequest(credentials, "Недостаточно данных для входа");
    }

    @Test
    @DisplayName("Авторизация без пароля возвращает ошибку")
    public void loginWithoutPasswordShouldFailTest() {
        // Создаем объект без пароля
        CourierCredentials credentials = new CourierCredentials();
        credentials.setLogin(testLogin);

        performLoginAndAssertBadRequest(credentials, "Недостаточно данных для входа");
    }

    @Test
    @DisplayName("Авторизация несуществующего пользователя возвращает ошибку")
    public void loginNonExistentUserShouldFailTest() {
        // Создаем объект для несуществующего пользователя
        CourierCredentials credentials = new CourierCredentials("non_existent_user", "any_password");

        performLoginAndAssertNotFound(credentials, "Учетная запись не найдена");
    }

    @Step("Выполнение авторизации и проверка успешного ответа")
    private void performLoginAndAssertSuccess(CourierCredentials credentials) {
        courierApiClient.loginCourier(credentials)
                .then()
                .statusCode(SC_OK)
                .body("id", notNullValue());
    }

    @Step("Выполнение авторизации и проверка ошибки 'Не найдено'")
    private void performLoginAndAssertNotFound(CourierCredentials credentials, String expectedMessage) {
        courierApiClient.loginCourier(credentials)
                .then()
                .statusCode(SC_NOT_FOUND)
                .body("message", equalTo(expectedMessage));
    }

    @Step("Выполнение авторизации и проверка ошибки 'Некорректный запрос'")
    private void performLoginAndAssertBadRequest(CourierCredentials credentials, String expectedMessage) {
        courierApiClient.loginCourier(credentials)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .body("message", equalTo(expectedMessage));
    }

    @After
    @Step("Очистка тестовых данных")
    public void tearDown() {
        // Удаляем тестового курьера
        if (testLogin != null && testPassword != null) {
            deleteTestCourier(testLogin, testPassword);
        }
    }
}