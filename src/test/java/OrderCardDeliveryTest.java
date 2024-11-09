import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Keys;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderCardDeliveryTest {

    private String generateDate(int days, String pattern) {
        return LocalDate.now().plusDays(days).format(DateTimeFormatter.ofPattern(pattern));
    }

    @BeforeEach
    void setUp() {
        Selenide.open("http://localhost:9999");
    }

    // TASK #1
    // Positive Test
    @Test
    void shouldOrderCardDelivery() {
        String selectDate = generateDate(4, "dd.MM.yyyy");

        $("[data-test-id='city'] .input__control").setValue("Москва");
        $("[data-test-id='date'] .input__control").sendKeys(
                Keys.chord(Keys.SHIFT, Keys.HOME), Keys.BACK_SPACE
        );
        $("[data-test-id='date'] .input__control").setValue(selectDate);
        $("[data-test-id='name'] .input__control").setValue("Иван Салтыков-Щедрин");
        $("[data-test-id='phone'] .input__control").setValue("+71231234567");
        $("[data-test-id='agreement'] span").click();
        $("button .button__text").shouldBe(Condition.text("Забронировать")).click();

        String notificationContent = $("[data-test-id='notification'] .notification__content").shouldBe(Condition.visible, Duration.ofSeconds(15)).text();
        assertEquals("Встреча успешно забронирована на " + selectDate, notificationContent);
    }

    // Negative Test - wrong city
    @Test
    void shouldNotAllowToSelectNotRussianCity() {
        // City is not from Russia
        $("[data-test-id='city'] .input__control").setValue("Минск");
        $("button .button__text").shouldBe(Condition.text("Забронировать")).click();

        String warning = $("[data-test-id='city'] .input__sub").text();
        assertEquals("Доставка в выбранный город недоступна", warning);
    }

    // Negative Test - wrong date
    @Test
    void shouldNotAllowToSelectDateToBeforeLessThen_3_Days() {
        String selectDate = generateDate(1, "dd.MM.yyyy");

        $("[data-test-id='city'] .input__control").setValue("Москва");
        $("[data-test-id='date'] .input__control").sendKeys(
                Keys.chord(Keys.SHIFT, Keys.HOME), Keys.BACK_SPACE
        );
        // Time is more than 3 days before current date
        $("[data-test-id='date'] .input__control").setValue(selectDate);
        $("button .button__text").shouldBe(Condition.text("Забронировать")).click();

        String warning = $("[data-test-id='date'] .input__sub").text();
        assertEquals("Заказ на выбранную дату невозможен", warning);
    }

    // Negative Test - wrong name
    @Test
    void shouldNotAllowLatinName() {
        String selectDate = generateDate(4, "dd.MM.yyyy");

        $("[data-test-id='city'] .input__control").setValue("Москва");
        $("[data-test-id='date'] .input__control").setValue(selectDate);
        // Latin letters on name
        $("[data-test-id='name'] .input__control").setValue("John Travolta");
        $("button .button__text").shouldBe(Condition.text("Забронировать")).click();

        String warning = $("[data-test-id='name'] .input__sub").text();
        assertEquals("Имя и Фамилия указаные неверно. Допустимы только русские буквы, пробелы и дефисы.", warning);
    }

    // Negative Test - wrong phone number
    @Test
    void shouldNotAllowShortNumber() {
        String selectDate = generateDate(4, "dd.MM.yyyy");

        $("[data-test-id='city'] .input__control").setValue("Москва");
        $("[data-test-id='date'] .input__control").setValue(selectDate);
        $("[data-test-id='name'] .input__control").setValue("Иван Салтыков-Щедрин");
        // Short number
        $("[data-test-id='phone'] .input__control").setValue("+7123");
        $("button .button__text").shouldBe(Condition.text("Забронировать")).click();

        String warning = $("[data-test-id='phone'] .input__sub").text();
        assertEquals("Телефон указан неверно. Должно быть 11 цифр, например, +79012345678.", warning);
    }

    // Negative Test - wrong phone number
    @Test
    void shouldNotAllowInvalidNumber() {
        String selectDate = generateDate(4, "dd.MM.yyyy");

        $("[data-test-id='city'] .input__control").setValue("Москва");
        $("[data-test-id='date'] .input__control").setValue(selectDate);
        $("[name='name']").setValue("Иван Салтыков-Щедрин");
        // Plus on end of phone number
        $("[data-test-id='phone'] .input__control").setValue("71231234567+");
        $("button .button__text").shouldBe(Condition.text("Забронировать")).click();

        String warning = $("[data-test-id='phone'] .input__sub").text();
        assertEquals("Телефон указан неверно. Должно быть 11 цифр, например, +79012345678.", warning);
    }

    // Negative Test - without agreement
    @Test
    void shouldNotAllowWithoutAgreement() {
        String selectDate = generateDate(4, "dd.MM.yyyy");

        $("[data-test-id='city'] .input__control").setValue("Москва");
        $("[data-test-id='date'] .input__control").setValue(selectDate);
        $("[name='name']").setValue("Иван Салтыков-Щедрин");
        $("[data-test-id='phone'] .input__control").setValue("+71231234567");
        // Step with agreement was missed
        $("button .button__text").shouldBe(Condition.text("Забронировать")).click();

        String warning = $("[data-test-id='agreement'] [role='presentation']").text();
        assertEquals("Я соглашаюсь с условиями обработки и использования моих персональных данных", warning);
    }

    // TASK #2
    // Positive Test - Select city from list and select next month by click
    @Test
    void shouldOrderCardDelivery_Advanced() {
        String selectDate = generateDate(25, "dd.MM.yyyy");
        String oneDay = generateDate(25, "d");
        String currentMonth = generateDate(0, "MM");
        String selectedMoth = generateDate(25, "MM");

        // Enter two first letters
        $("[data-test-id='city'] .input__control").setValue("Мо");
        $$("span").filter(visible).find(Condition.exactText("Москва")).click();

        // Check will we need to select next month or not
        $("[data-test-id='date'] .input__icon").click();
        if (!currentMonth.equals(selectedMoth)) {
            $("[data-step='1'].calendar__arrow_direction_right").click();
        }
        $$(".popup__container .calendar__day").filter(visible).find(Condition.exactText(oneDay)).click();

        $("[name='name']").setValue("Иван Салтыков-Щедрин");
        $("[data-test-id='phone'] .input__control").setValue("+71231234567");
        $("[data-test-id='agreement'] span").click();
        $("button .button__text").shouldBe(Condition.text("Забронировать")).click();

        String notificationContent = $("[data-test-id='notification'] .notification__content").shouldBe(visible, Duration.ofSeconds(15)).text();
        assertEquals("Встреча успешно забронирована на " + selectDate, notificationContent);
    }
}
