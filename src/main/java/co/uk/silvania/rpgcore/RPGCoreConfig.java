package co.uk.silvania.rpgcore;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class RPGCoreConfig {
	
	public static File rpgCoreConfigFile;
	
	public static String[] factions;
	public static int baseXp;
	public static int skillPointsPerLevel;
	
	public static void init(String configPath) {
		rpgCoreConfigFile = new File(configPath + "RPGCore.cfg");
		
		initConfig(rpgCoreConfigFile);
	}
	
	public static Configuration config;
	
	public static void initConfig(File configFile) {
		config = new Configuration(configFile);
						
		try {
			config.load();
			factions = config.getStringList("Factions", Configuration.CATEGORY_GENERAL, new String[] {"Caelum", "Mortalitas"}, "Add new factions for players to select.");
			baseXp = config.getInt("baseXP", Configuration.CATEGORY_GENERAL, 83, 10, Integer.MAX_VALUE, "Base int used for level up curve multipliers. Higher numbers mean ALL skills take longer to level.");
			skillPointsPerLevel = config.getInt("skillPointsPerLevel", Configuration.CATEGORY_GENERAL, 3, 1, 999, "How many skill points are awarded for each Global Level increase.");			
		} catch (Exception e) {
			System.out.println("### WARNING! RPGCore could not load it's config files! ###");
		} finally {
			if (config.hasChanged()) {
				config.save();
			}
		}
	}
}