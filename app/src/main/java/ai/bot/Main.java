package ai.bot;

import ai.bot.api.WordNode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Main implements EventListener, Runnable {
	private WordNode rootNode;

	public static void main(String[] args) throws InterruptedException, IOException {
		// Debug
		System.out.println("===args===");
		for (String str : args) {
			System.out.println(str);
		}
		System.out.println("==========");

		BufferedReader br;
		File tokenfile = new File("aitoken.txt");
		br = new BufferedReader(new FileReader(tokenfile));

		String token;
		while ((token = br.readLine()) == null);
		br.close();

		// Begin program
		JDABuilder builder = JDABuilder.createDefault(token);

		Main mainBot = new Main();

		builder.disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE);
		builder.setBulkDeleteSplittingEnabled(false);
		builder.setActivity(Activity.watching("sara.help"));
		builder.enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_PRESENCES,
				GatewayIntent.GUILD_MEMBERS);
		builder.addEventListeners(mainBot);

		builder.build();

		Thread mainThread = new Thread(mainBot);
		mainThread.start();
	}

	public Main() {
		rootNode = new WordNode(new File("aidata.txt"));
	}

	@Override
	public void onEvent(GenericEvent event) {
		if (event instanceof ReadyEvent) {
			System.out.println("API is ready!");
			System.out.println("=========");
			System.out.println(rootNode.toReadableString());
			System.out.println("=========");
			System.out.println(rootNode);
			System.out.println("=========\n\n");
		}

		if (event instanceof MessageReceivedEvent) {
			Message message = ((MessageReceivedEvent) event).getMessage();
			String messageStr = message.getContentRaw();
			//String guildChannelId = message.getGuildChannel().getId();

			String[] words = messageStr.split(" ");

			if (messageStr.equals("sara.help")) {
				message.reply("Hello! My only semi-working command is `sara.generate`, so come on! Try me!").submit();
				return;
			}

			if (messageStr.startsWith("sara.g")) {
				if(rootNode.isEmpty()) {
					message.reply("Please send more messages to allow me to generate messages!");
				}
				else {
					String res = "";
					// Pick starting word
					WordNode tempNode = rootNode;
					while(res.equals("")) {
						tempNode = rootNode.pickNextRandom();
						if(tempNode != null)
							res += tempNode.getWord();
					}
					res += " ";
					// Pick next until reaching the end
					while(tempNode != null && !tempNode.getWord().equals("")) {
						tempNode = tempNode.pickNextRandom();
						if(tempNode != null)
							res += tempNode.getWord() + " ";
						if(tempNode != null)
							tempNode = rootNode.getWordNode(tempNode.getWord());
					}
					message.reply(res).submit();
				}
				return;
			}

			/* If all other conditions not met and the message is not from the bot, then it's a normal message to learn from */
			if(!message.getAuthor().getId().equals(event.getJDA().getSelfUser().getId())) {
				for(int i = 0; i < words.length - 1; i++) {
					rootNode.addWordNode(words[i], 1);
					rootNode.getWordNode(words[i]).addWordNode(words[i + 1], 1);
					rootNode.addWordNode(words[i + 1], 1);
				}
				if(words.length == 1) {
					rootNode.addWordNode(words[0], 1);
				}
			}

			System.out.println("=========");
			System.out.println(rootNode.toReadableString());
			System.out.println("=========");
			System.out.println(rootNode);
			System.out.println("=========\n\n");
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				// Save current data to file
				File aiOutputFile = new File("aidata.txt");
				FileOutputStream aiOutputStream = new FileOutputStream(aiOutputFile, false);
				aiOutputStream.write(rootNode.toString().getBytes());
				aiOutputStream.close();
			
				Thread.sleep(10000); // Sleep 10 seconds
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
