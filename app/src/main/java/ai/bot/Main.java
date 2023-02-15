package ai.bot;

import ai.bot.api.WordTree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

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
	private HashMap<String, WordTree> guildWords;

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
		guildWords = new HashMap<String, WordTree>();
	}

	public boolean loadAiJSONs()
	{
		File thisDir = new File(".");
		File[] thisDirArray = thisDir.listFiles();

		if (thisDirArray == null)
			return false;

		for (File child : thisDirArray)
		{
			String childName = child.getName();

			int prefixIndex = childName.indexOf("aidata-");
			if (prefixIndex < 0)
				continue;

			String guildId = childName.substring(prefixIndex + "aidata-".length());

			int suffixIndex = guildId.indexOf(".json");
			if (suffixIndex < 0)
				continue;
			
			guildId = guildId.substring(0, suffixIndex);
			for (char c : guildId.toCharArray())
				if (c < '0' || c > '9')
					return false;

			guildWords.put(guildId, new WordTree(child));
		}

		return true;
	}

	@Override
	public void onEvent(GenericEvent event) {
		if (event instanceof ReadyEvent) {
			System.out.println("API is ready!");
			System.out.println("Loading JSONs");
			if (loadAiJSONs())
			{
				System.out.println("Success!");
			}
			else
			{
				System.out.println("Failed to load JSONs.");
			}
		}

		if (event instanceof MessageReceivedEvent) {
			Message message = ((MessageReceivedEvent) event).getMessage();
			String messageStr = message.getContentRaw();
			WordTree messageGuildTree = guildWords.getOrDefault(message.getGuild().getId(), null);
			if (messageGuildTree == null)
			{
				messageGuildTree = new WordTree();
				guildWords.put(message.getGuild().getId(), messageGuildTree);
			}

			if (messageStr.equals("sara.help")) {
				message.reply("Hello! My only semi-working command is `sara.generate`, so come on! Try me!").submit();
				return;
			}

			if (messageStr.startsWith("sara.g") && "sara.generate".startsWith(messageStr)) {
				if(messageGuildTree.isEmpty()) {
					message.reply("Please send more messages to allow me to generate messages!");
				}
				else {
					message.reply(messageGuildTree.generateSentence()).submit();
				}
				return;
			}

			/* If all other conditions not met and the message is not from the bot, then it's a normal message to learn from */
			if(!message.getAuthor().getId().equals(event.getJDA().getSelfUser().getId())) {
				messageGuildTree.addSentence(messageStr);
			}
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				// Save current data to files
				for (Entry<String, WordTree> entry : guildWords.entrySet())
				{
					File aiOutputFile = new File("aidata-" + entry.getKey() + ".json");
					FileOutputStream aiOutputStream = new FileOutputStream(aiOutputFile, false);
					aiOutputStream.write(entry.getValue().toData().getBytes());
					aiOutputStream.close();
					System.out.println("Success saving to " + "aidata-" + entry.getKey() + ".json" + "!");
				}
				System.out.println();
			
				Thread.sleep(30000); // Sleep 30 seconds
			} catch (Exception e) {
				System.err.println("Error saving data...");
				e.printStackTrace();
			}
		}
	}
}
