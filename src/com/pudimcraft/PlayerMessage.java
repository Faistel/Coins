package com.pudimcraft;

import com.pudimcraft.Main;
//import com.vk2gpz.tokenenchant.TokenEnchant;

import java.security.SecureRandom;
import java.sql.ResultSet;
import java.util.Date;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerMessage {

	public Main plugin;
	
	public PlayerMessage(Main main) {
		this.plugin = main;
	}

	public boolean SendMessage(Player player, UUID uuid, boolean prefix, String Message, String file){
		int EnvMsg = 1;
		
		if(player == null){
		player = Bukkit.getPlayer(uuid);

		if (player == null) {
			EnvMsg = 0;
		}
		}
		
		if(EnvMsg==1){
			if(file!=null){
				Message=plugin.getConfig().getString(file);
			}
			if(prefix){
				Message=plugin.getConfig().getString("prefix")+Message;
			}
			player.sendMessage(Message);
		}
		return true;
	}

}
