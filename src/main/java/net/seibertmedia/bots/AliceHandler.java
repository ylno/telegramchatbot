package net.seibertmedia.bots;

import org.alicebot.ab.Bot;
import org.alicebot.ab.Chat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AliceHandler extends TelegramLongPollingBot {

  private static final Logger logger = LoggerFactory.getLogger(AliceHandler.class);

  public static final int SECONDS_INACTIVITY = 600;

  public static final int CHECK_SESSIONS = 60;

  private Map<Integer, Chat> chats = new HashMap();

  private org.alicebot.ab.Bot bot;

  private String botToken;

  final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool( 1 );

  public AliceHandler(final String botname, final String botPath, String botToken) {

    this.botToken = botToken;
    bot = new Bot(botname, botPath, "chat");

    scheduler.scheduleAtFixedRate(
        new Runnable() {
          @Override public void run() {
            logger.debug("Check active chats {}" , chats.size());
            for (Integer aLong : chats.keySet()) {
              Chat chat = chats.get(aLong);
              try {

                if(chat.inactiveForSeconds(SECONDS_INACTIVITY)) {
                  logger.debug("remove chat");
                  chats.remove(aLong);
                }
              } catch (Exception e) {
                logger.debug("exception {}", e);
              }
            }

          }
        },
        10 /* Startverzögerung */, CHECK_SESSIONS /* Dauer */,
        TimeUnit.SECONDS );


  }

  public void onUpdateReceived(final Update update) {
    logger.debug("update from {}", update);

    if (update.hasMessage()) {

      Message message = update.getMessage();
      if (message.hasText()) {
        handleIncomingMessage(message);
      }
    }

  }

  private void handleIncomingMessage(final Message message) {

    Integer userId = message.getFrom().getId();
    Chat chat;
    if (chats.containsKey(userId)) {
      logger.debug("existing chat {}", userId);
      chat = chats.get(userId);
    } else {
      logger.debug("new chat {}", userId);
      chat = new Chat(bot, false, userId.toString());
      chats.put(userId, chat);
    }

    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(String.valueOf(message.getChatId()));
    // sendMessage.setReplayToMessageId(message.getMessageId());

    logger.info("request from {}: {}", message.getChatId(), message.getText());

    String answer = chat.multisentenceRespond(message.getText());
    logger.info("answer to {}: {}", message.getChatId(), answer);
    sendMessage.setText(answer);
    try {
      sendMessage(sendMessage);
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }

  public String getBotUsername() {
    return "alicebot";
  }

  @Override
  public String getBotToken() {

    return botToken;
  }



}
