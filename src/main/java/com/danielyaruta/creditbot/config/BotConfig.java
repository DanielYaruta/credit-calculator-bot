package com.danielyaruta.creditbot.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Загружает конфигурацию бота (токен, учётные данные менеджеров)
 * без хранения секретов в коде или в репозитории.
 * <p>
 * Порядок поиска токена:
 * 1. Переменная окружения {@code BOT_TOKEN} (приоритет — стандартный
 *    способ для серверного/контейнерного запуска, рекомендуется).
 * 2. Локальный файл {@code config.properties} в корне проекта
 *    (для локальной разработки; этот файл должен быть в .gitignore
 *    и никогда не попадать в репозиторий).
 * <p>
 * Если токен не найден ни одним из способов, выбрасывается понятная
 * ошибка вместо запуска бота с null-токеном.
 */
public class BotConfig {

    private static final String ENV_TOKEN_KEY = "BOT_TOKEN";
    private static final String ENV_MANAGER_LOGIN_KEY = "MANAGER_LOGIN";
    private static final String ENV_MANAGER_PASSWORD_KEY = "MANAGER_PASSWORD";
    private static final String CONFIG_FILE_NAME = "config.properties";

    private final String botToken;
    private final String managerLogin;
    private final String managerPassword;

    private BotConfig(String botToken, String managerLogin, String managerPassword) {
        this.botToken = botToken;
        this.managerLogin = managerLogin;
        this.managerPassword = managerPassword;
    }

    public static BotConfig load() {
        Properties fileProperties = loadPropertiesFileIfExists();

        String token = firstNonBlank(
                System.getenv(ENV_TOKEN_KEY),
                fileProperties.getProperty("bot.token")
        );
        String login = firstNonBlank(
                System.getenv(ENV_MANAGER_LOGIN_KEY),
                fileProperties.getProperty("manager.login")
        );
        String password = firstNonBlank(
                System.getenv(ENV_MANAGER_PASSWORD_KEY),
                fileProperties.getProperty("manager.password")
        );

        if (token == null || token.isBlank()) {
            throw new IllegalStateException(
                    "Токен бота не найден. Укажите переменную окружения BOT_TOKEN " +
                            "или создайте файл config.properties с bot.token=<ваш_токен> " +
                            "(см. config.properties.example)."
            );
        }
        if (login == null || password == null) {
            throw new IllegalStateException(
                    "Учётные данные менеджера не найдены. Укажите переменные окружения " +
                            "MANAGER_LOGIN и MANAGER_PASSWORD или заполните config.properties."
            );
        }

        return new BotConfig(token, login, password);
    }

    private static Properties loadPropertiesFileIfExists() {
        Properties properties = new Properties();
        Path configPath = Path.of(CONFIG_FILE_NAME);

        if (Files.exists(configPath)) {
            try (InputStream input = Files.newInputStream(configPath)) {
                properties.load(input);
            } catch (IOException e) {
                throw new IllegalStateException("Не удалось прочитать " + CONFIG_FILE_NAME, e);
            }
        }
        return properties;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    public String botToken() {
        return botToken;
    }

    public String managerLogin() {
        return managerLogin;
    }

    public String managerPassword() {
        return managerPassword;
    }
}
