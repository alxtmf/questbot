/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.p03.questbot;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

/**
 *
 * @author timofeevan
 */
public class Bot extends TelegramLongPollingBot {

    private static final String TOKEN = "430612129:AAHP8Fb0rSsa4WhxW9mxmY-1WSoFZqQ3F24";
    private static final String USERNAME = "annaquestbot";

    public Bot() {
    }

    public Bot(DefaultBotOptions options) {
        super(options);
    }

    @Override
    public String getBotToken() {
        return TOKEN;
    }

    @Override
    public String getBotUsername() {
        return USERNAME;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            processCommand(update);
        } else if (update.hasCallbackQuery()) {
            processCallbackQuery(update);
        } else if (update.hasInlineQuery()) {
        }
    }

    private void processCallbackQuery(Update update) {
        List<SendMessage> answerMessage = null;
        String data = update.getCallbackQuery().getData();
        if (data == null) {
            return;
        }

        answerMessage = AppEnv.getContext().getMenuManager().processCallbackQuery(update);

        if (answerMessage != null && answerMessage.isEmpty()) {
            answerMessage.clear();
        }
    }

    private void processCommand(Update update) {
        SendMessage answerMessage = null;
        try {
            answerMessage = AppEnv.getContext().getMenuManager().processCommand(update);
            if (answerMessage != null) {
                execute(answerMessage);
            }

        } catch (TelegramApiException ex) {
            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
