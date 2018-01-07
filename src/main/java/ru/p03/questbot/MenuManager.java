/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.p03.questbot;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.methods.send.SendSticker;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import ru.p03.questbot.bot.state.StateHolder;
import ru.p03.questbot.bot.document.spi.DocumentMarshalerAggregator;
import ru.p03.questbot.bot.schema.Action;
import ru.p03.questbot.bot.state.QuestEnumeration;
import ru.p03.questbot.bot.state.QuestStateHolder;
import ru.p03.questbot.model.ClsAnswer;
import ru.p03.questbot.model.ClsQuest;
import ru.p03.questbot.model.ClsQuestPhoto;
import ru.p03.questbot.model.repository.ClassifierRepository;
import ru.p03.questbot.model.repository.ClassifierRepositoryImpl;
import ru.p03.questbot.util.ActionBuilder;
import ru.p03.questbot.util.InlineKeyboardButtonBuilder;
import ru.p03.questbot.util.UpdateUtil;

/**
 *
 * @author timofeevan
 */
public class MenuManager {

    public static final String OPEN_MAIN = "OM";
    public static final String GET_ANSWER = "GA";

    public static final String smiling_face_with_heart_eyes = new String(Character.toChars(0x1F60D));
    public static final String winking_face_with_tongue = new String(Character.toChars(0x1F61C));
    public static final String winking_face = new String(Character.toChars(0x1F609));
    public static final String bouquet = new String(Character.toChars(0x1F490));
    public static final String party_popper = new String(Character.toChars(0x1F389));

    private final DocumentMarshalerAggregator marshalFactory;
    private final QuestStateHolder questStateHolder;
    private final ClassifierRepositoryImpl classifierRepository;

    private Bot bot;

    public MenuManager(ClassifierRepository classifierRepository,
            DocumentMarshalerAggregator marshalFactory, StateHolder stateHolder,
            QuestStateHolder questStateHolder) {
        this.marshalFactory = marshalFactory;
        //this.stateHolder = stateHolder;
        this.classifierRepository = (ClassifierRepositoryImpl) classifierRepository;
        this.questStateHolder = questStateHolder;
    }

    public void setBot(Bot bot) {
        this.bot = bot;
    }

    private void initQuests(Update update) {
        List<ClsQuest> q = classifierRepository.find(ClsQuest.class, false);
        Collections.shuffle(q);
        questStateHolder.put(update, new QuestEnumeration(q));
    }

    public SendMessage processCommand(Update update) {
        SendMessage answerMessage = null;
        String text = update.getMessage().getText();
        if ("/start".equalsIgnoreCase(text)) {
            answerMessage = new SendMessage();
            answerMessage.setText("<b>Привет, красотка!" + smiling_face_with_heart_eyes + 
                    "\nВо-первых с днем рождения!" + bouquet + bouquet + bouquet + party_popper
                    + " А во-вторых, ты готова поиграть в увлекательную викторину?</b>"
//                    + " Будет легко и смешно: 26 шуточных вопросов с веселыми или красивыми фоточками, имеющими отношение ко всем тем, кто знает об этой викторине."
//                    + " Если захочешь начать сначала, введи /start."
//                    + " Если после нажатия на кнопочку несколько секунд ничего не происходит, не волнуйся и жди - это грузятся фоточки"
//                    + "\nПобедителю викторины - приз!" + winking_face_with_tongue
            );
            answerMessage.setParseMode("HTML");
            answerMessage.setChatId(update.getMessage().getChatId());
            InlineKeyboardMarkup markup = keyboard(update);

            answerMessage.setReplyMarkup(markup);
        }
        return answerMessage;
    }

    private void sendQuest(Update update) throws TelegramApiException {
        QuestEnumeration qe = questStateHolder.get(update);
        Long chatId = UpdateUtil.getChatFromUpdate(update).getId();
        ClsQuest nextQuest = qe.nextElement();
        if (nextQuest != null) {
            SendMessage quest = new SendMessage();
            quest.setParseMode("HTML");
            quest.setText("<b>Вопрос " + qe.getCurrentQuest() + ":</b>  "
                    + nextQuest.getQuestText());
            quest.setChatId(chatId);
            bot.execute(quest);
            for (ClsQuestPhoto clsQuestPhoto : nextQuest.getClsQuestPhotoCollection()) {
                SendPhoto sendPhoto = new SendPhoto();
                sendPhoto.setChatId(chatId);
                sendPhoto.setNewPhoto(new File(AppEnv.getContext().getRootPath()
                        + "\\photo" + clsQuestPhoto.getRelFilePath()));
                bot.sendPhoto(sendPhoto);
            }

            SendMessage answers = new SendMessage();
            answers.setParseMode("HTML");
            answers.setText("<b>Варианты ответа:</b>");
            answers.setChatId(chatId);
            answers.setReplyMarkup(keyboardAnswer(update, nextQuest));
            bot.execute(answers);
        }else{
            SendMessage answers = new SendMessage();
            answers.setParseMode("HTML");
            answers.setText("<b>Ну вот и все! Подробности на процедуре награждения</b> \n "
                    + "Если хочешь начать заново нажми кнопку 'Начать' или введи /start");
            answers.setChatId(chatId);
            InlineKeyboardMarkup markup = keyboard(update);
            answers.setReplyMarkup(markup);
            bot.execute(answers);
            
            SendSticker sticker = new SendSticker();
            sticker.setChatId(chatId);
            
            File stikerFile = new File(AppEnv.getContext().getRootPath() + "\\photo\\stiker.png");
            if (stikerFile.exists()){
                sticker.setNewSticker(stikerFile);
                bot.sendSticker(sticker);
            }
            
            
        }
    }
    
    public List<SendMessage> processCallbackQuery(Update update) {
        List<SendMessage> answerMessages = new ArrayList<>();
        try {
            Action action = new ActionBuilder(marshalFactory).buld(update);
            String data = update.getCallbackQuery().getData();
            Long chatId = UpdateUtil.getChatFromUpdate(update).getId();//update.getCallbackQuery().getMessage().getChatId();
            if (data == null || action == null) {
                return null;
            }

            if (MenuManager.OPEN_MAIN.equals(action.getName())) {
                initQuests(update);

                sendQuest(update);
            }

            if (MenuManager.GET_ANSWER.equals(action.getName())) {
                Long answId = Long.parseLong(action.getValue());
                ClsAnswer answ = classifierRepository.find(ClsAnswer.class, answId);
                SendMessage comment = new SendMessage();
                comment.setParseMode("HTML");
                comment.setText("<b>Твой ответ:</b> "
                        + answ.getAnswerText()
                        + "\n<b>Комментарий к ответу:</b> "
                        + answ.getAnswerComment()
                        + "\n");
                comment.setChatId(chatId);
                bot.execute(comment);
                
                sendQuest(update);
            }

        } catch (Exception ex) {
            Logger.getLogger(MenuManager.class.getName()).log(Level.SEVERE, null, ex);
            answerMessages.add(errorMessage());
        }
        return answerMessages;
    }

    public SendMessage errorMessage() {
        SendMessage answerMessage = new SendMessage();
        answerMessage.setText("Ой, что-то пошло не так, попробуйте еще раз или перейдите в главное меню");
        InlineKeyboardMarkup keyboardMain = keyboard(null);
        answerMessage.setReplyMarkup(keyboardMain);
        return answerMessage;
    }

    public InlineKeyboardMarkup keyboard(Update update) {
        final InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(Arrays.asList(buttonMain()));
        markup.setKeyboard(keyboard);
        return markup;
    }

    public InlineKeyboardButton buttonMain() {
        InlineKeyboardButton button = new InlineKeyboardButtonBuilder()
                .setText("Начать!" + winking_face)
                .setCallbackData(new ActionBuilder(marshalFactory)
                        .setName(OPEN_MAIN)
                        .asString())
                .build();
        return button;
    }

    public InlineKeyboardMarkup keyboardAnswer(Update update, ClsQuest quest) {
        final InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        for (ClsAnswer clsAnswer : quest.getClsAnswerCollection()) {
            keyboard.add(Arrays.asList(buttonAnswer(clsAnswer)));
        }
        markup.setKeyboard(keyboard);
        return markup;
    }

    public InlineKeyboardButton buttonAnswer(ClsAnswer clsAnswer) {
        InlineKeyboardButton button = new InlineKeyboardButtonBuilder()
                .setText(clsAnswer.getAnswerText())
                .setCallbackData(new ActionBuilder(marshalFactory)
                        .setName(GET_ANSWER)
                        .setValue(clsAnswer.getId().toString())
                        .asString())
                .build();
        return button;
    }
}
