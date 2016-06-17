package net.seibertmedia;

import static java.util.Locale.setDefault;

import java.util.Locale;
import java.util.Properties;

import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.TelegramBotsApi;

import net.seibertmedia.bots.AliceHandler;

public class Botrunner {

  public static final String BOT_NAME = "bot-name";

  public static final String BOT_PATH = "bot-path";

  public static final String TELEGRAM_BOT_KEY = "telegram-bot-key";

    public static final String LANGUAGE = "language";

    public static final String COUNTRY = "country";

    public static void main(String[] args) {

    TelegramBotsApi telegramBotsApi = new TelegramBotsApi();

    Properties properties = Botpropperties.readProperties();
    String language = properties.getProperty(LANGUAGE);
    String country = properties.getProperty(COUNTRY);
    Locale l = new Locale(language, country);
    setDefault(l);

    checkProperties(properties);

    try {
      telegramBotsApi.registerBot(
          new AliceHandler(
              properties.getProperty(BOT_NAME),
              properties.getProperty(BOT_PATH),
              properties.getProperty(TELEGRAM_BOT_KEY)));
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }

  private static void checkProperties(final Properties properties) {
    checkProperty(properties, BOT_NAME);
    checkProperty(properties, BOT_PATH);
    checkProperty(properties, TELEGRAM_BOT_KEY);
  }

  private static void checkProperty(final Properties properties, final String parameterName) {
    if (properties.getProperty(parameterName) == null || properties.getProperty(parameterName).isEmpty()) {
      throw new RuntimeException("Parameter missing: " + parameterName + ". Please define it in settings.properties");
    }
  }
}
