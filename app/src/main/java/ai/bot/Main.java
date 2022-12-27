package ai.bot;

import ai.bot.api.WordNode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

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
			System.out.println(rootNode);
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

			if (messageStr.equals("sara.generate")) {
				if(rootNode.isEmpty()) {
					message.reply("Please send more messages to allow me to generate messages!");
				}
				else {
					String res = "";
					WordNode curNode = rootNode;
					while(res.equals("")) {
						curNode = rootNode.pickNextRandom();
						if(curNode != null)
							res += curNode.getWord();
					}
					res += " ";
					while(curNode != null && !curNode.getWord().equals("")) {
						curNode = curNode.pickNextRandom();
						if(curNode != null)
							res += curNode.getWord() + " ";
					}
					message.reply(res).submit();
				}
				return;
			}

			/* If all other conditions not met and the message is not from the bot, then it's a normal message to learn from */
			if(!message.getAuthor().getId().equals(event.getJDA().getSelfUser().getId())) {
				WordNode currentNode = rootNode;
				for(int i = 0; i < words.length; i++) {
					currentNode.addWordNode(words[i], 1);
					currentNode = currentNode.getWordNode(words[i]);
				}
			}

			System.out.println(rootNode);
		}
	}

	@Override
	public void run() {
		while (true) {
			// TODO determine the usefulness of a constantly-running thread for this bot

			try {
				Thread.sleep(750);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
