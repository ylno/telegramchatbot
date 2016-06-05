package net.seibertmedia.bots;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.alicebot.ab.Bot;
import org.alicebot.ab.Chat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

public class AliceHandler extends TelegramLongPollingBot {

  private static final Logger logger = LoggerFactory.getLogger(AliceHandler.class);

  public static final int SECONDS_INACTIVITY = 600;

  public static final int CHECK_SESSIONS = 60;

  private Map<Long, Chat> chats = new HashMap();

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
            for (Long aLong : chats.keySet()) {
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

    Chat chat;
    if (chats.containsKey(message.getChatId())) {
      logger.debug("existing chat {}", message.getChatId());
      chat = chats.get(message.getChatId());
    } else {
      logger.debug("new chat {}", message.getChatId());
      chat = new Chat(bot, false, message.getChatId().toString());
      chats.put(message.getChatId(), chat);
    }

    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(message.getChatId().toString());
    // sendMessage.setReplayToMessageId(message.getMessageId());
    sendMessage.setText(chat.multisentenceRespond(message.getText()));
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
