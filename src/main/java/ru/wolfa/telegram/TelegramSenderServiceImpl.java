package ru.wolfa.telegram;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.wolfa.telegram.dto.PhotoSend;
import static ru.wolfa.cam.signals.receiver.ApplicationConstants.*; 

@Component
public class TelegramSenderServiceImpl {
	private final WebClient client;
	private final String botName;

	public void sendPhoto(int chatId, ByteArrayResource photo, String text) {
		String alertMessage = dateFormat.format(new Date()) + (text == null ? "" : " " + text);
		MultipartBodyBuilder mbb = new MultipartBodyBuilder();
		mbb.part(TELEGRAM_REQUEST_CHAT_ID_PARAM, chatId);
		mbb.part(TELEGRAM_REQUEST_PHOTO_PARAM, new ByteArrayResource(photo.getByteArray(), photo.getDescription()) {
			@Override
			public String getFilename() {
				return "snapshot.jpg";
			}
		});
		mbb.part(TELEGRAM_REQUEST_CAPTION_PARAM, alertMessage);
		log.trace("MultipartBodyBuilder done");

		client.post().uri(b -> b.path("/sendPhoto").build()).contentType(MediaType.MULTIPART_FORM_DATA)
				.body(BodyInserters.fromMultipartData(mbb.build())).retrieve().bodyToMono(ByteArrayResource.class)
				.subscribe(res -> {
					log.trace("Send photo done");
					ObjectMapper om = new ObjectMapper();
					try {
						PhotoSend result = om.readValue(res.getByteArray(), PhotoSend.class);
						if (result.isOk()) {
							log.trace("Sending photo id {}", result.getResult().getMessageId());
						} else {
							log.error("Sending photo failed");
						}
					} catch (IOException e) {
						log.error("IOException", e);
					}
				});
	}

	public void sendMessage(int chatId, String text) {
		MultipartBodyBuilder mbb = new MultipartBodyBuilder();
		mbb.part(TELEGRAM_REQUEST_CHAT_ID_PARAM, chatId);
		mbb.part(TELEGRAM_REQUEST_TEXT_PARAM, text);
		log.trace("MultipartBodyBuilder done");

		client.post().uri(b -> b.path("/sendMessage").build()).contentType(MediaType.MULTIPART_FORM_DATA)
				.body(BodyInserters.fromMultipartData(mbb.build())).retrieve().bodyToMono(ByteArrayResource.class)
				.subscribe(res -> {
					log.trace("Send message done res={}", res);
				});
	}

	public String getBotName() {
		return botName;
	}

	public boolean isAllowedChat(int chatId) {
		boolean result = false;
		for (Integer i : allowedChats) {
			if (i == chatId) {
				result = true;
				break;
			}
		}
		return result;
	}

	public TelegramSenderServiceImpl(Environment env) {
		botName = env.getProperty(CONFIG_TELEGRAM_BOT_NAME);

		String botId = env.getProperty(CONFIG_TELEGRAM_BOT_ID);
		String botToken = env.getProperty(CONFIG_TELEGRAM_BOT_TOKEN);

		client = WebClient.builder()
				.baseUrl(TELEGRAM_BOT_URL + botId + ":" + botToken).build();

		String[] allowedChatsStr = env.getProperty("telegram.allowed_chats").split(",");
		for(String s : allowedChatsStr) {
			allowedChats.add(Integer.parseInt(s));
		}

	}

	private final List<Integer> allowedChats = new ArrayList<>();

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

	private static final Logger log = LoggerFactory.getLogger(TelegramSenderServiceImpl.class);

}
