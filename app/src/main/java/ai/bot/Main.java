package ai.bot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Main implements EventListener, Runnable {
	public static void main(String[] args) throws InterruptedException, IOException {
		// Debug
		System.out.println("===args===");
		for(String str : args) {
			System.out.println(str);
		}
		System.out.println("==========");
		
		BufferedReader br;
		File tokenfile = new File("aitoken.txt");
		br = new BufferedReader(new FileReader(tokenfile));
		
		String token;
		while((token = br.readLine()) == null);
		
		br.close();
		
		// Begin program
		JDABuilder builder = JDABuilder.createDefault(token);
		
		Main mainBot = new Main();

		builder.disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE);
		builder.setBulkDeleteSplittingEnabled(false);
		builder.setActivity(Activity.watching("sara.help"));
		builder.enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MEMBERS);
		builder.addEventListeners(mainBot);
		
		builder.build();
		
		Thread mainThread = new Thread(mainBot);
		mainThread.start();
	}

	public Main() {
		//messageCounts = new HashMap<>();
		//scheduledMains = new ConcurrentHashMap<>();
	}

	@Override
	public void onEvent(GenericEvent event) {
		if(event instanceof ReadyEvent)
			System.out.println("API is ready!");
		
		if(event instanceof MessageReceivedEvent) {
			Message message = ((MessageReceivedEvent) event).getMessage();
			String messageStr = message.getContentRaw();
			String guildChannelId = message.getGuildChannel().getId();

			String[] words = messageStr.split(" ");

			if(messageStr.equals("sara.help")) {
				message.reply("Hello!").submit();
			}

			

			if(messageStr.equals("sara.generate")) {
				message.reply("This part of the code is under construction.").submit();
			}
		}
	}

	@Override
	public void run() {
		while(true) {
			// TODO determine the usefulness of a constantly-running thread for this bot

			try {
				Thread.sleep(750);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
