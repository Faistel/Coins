package com.pudimcraft.Commands;
import java.sql.ResultSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.pudimcraft.*;


	public class Sell {
		
	
	public Main plugin;

	//PlayerMessage PM = new PlayerMessage(plugin);
	public Sell(Main main) {
		this.plugin = main;
	}
	
	Mining NB_ = new Mining(plugin);
	Wallet WT_ = new Wallet(plugin);
	Trade TR_ = new Trade(plugin);
	PlayerInfo PI_ = new PlayerInfo(plugin);
	PlayerMessage PM_ = new PlayerMessage(plugin);
	Vault VT_ = new Vault(plugin);
	
	public void Sell(Player player, String[] args){
		if (args.length == 4) {
			Bukkit.getServer().broadcastMessage(player.toString());
			//this.PM.SendMessage(null, UUID.fromString("c5257b10-47c6-310b-a982-dd5d5819e3ed"), true,player.toString(),null);
	
			if (args[0].equalsIgnoreCase("sell") && !plugin.bloquear.get("trade") && !plugin.bloquear.get("order")) {
				if (plugin.isNumeric(args[1]) && plugin.isNumeric(args[3])) {
					if (player.hasPermission("coins.sell") || player.hasPermission("coins.player")) {
						if (plugin.verificarCoins(args[2],player) == true) {
	
							if(!plugin.bloquear.get("order")){
								plugin.bloquear.put("order", true);
							}
							
						
							double qtd = Double.parseDouble(args[1]);
							double val = Double.parseDouble(args[3]);
							String coin = args[2].toUpperCase();
							
							if (qtd > 0 && val >= 0 && plugin.SzNum(val) && plugin.SzNum(qtd)) {
								if(TR_.AddTrade(player.getUniqueId())){
									// if para bloquear valor
									if(WT_.Wallet(coin, 0, 2, 0, player.getUniqueId(),player.getUniqueId().toString(),qtd,false)){
									if(PI_.PlayerInfo(0,0, 0,null, player.getUniqueId(),false)){ //incrementar o trade
									if(plugin.updateSQL("UPDATE `coins_orders` SET `amount` = amount + '"+qtd+"' WHERE `uuid` = '" + player.getUniqueId() + "' AND `price` = '"+val+"' AND `type` = 'SELL'")==false){
										
										if(plugin.updateSQL("INSERT INTO `coins_orders` (`id`, `coin`, `price`, `type`, `amount`, `uuid`) VALUES (NULL, '"
											+ coin + "', " + val + ", 'SELL', " + qtd
											+ ", '" + player.getUniqueId() + "')")){
											this.PM_.SendMessage(player, null, true, null ,"Sales_Order");
										}
									}else{
										PM_.SendMessage(player, null, true, null ,"Updated_Sales_Order");
									}
									
									int ID_ORDER = 0;
									try {
										ResultSet rs = plugin.query("SELECT * FROM `coins_orders` WHERE `coin` = '"
												+ coin + "' AND `amount`=" + qtd + " AND `price`=" + val
												+ " AND `uuid`='" + player.getUniqueId() + "'LIMIT 1;");
										if (rs.next()) {
											ID_ORDER = rs.getInt("id");
										}
									} catch (Exception ex) {
									}
									if (ID_ORDER > 0) {
										TR_.ordersEx(ID_ORDER);
									}
									}
								}
							}
							} else {
								PM_.SendMessage(player, null, true, null ,"Syntax_Error");
							}
						}
					} else {
						PM_.SendMessage(player, null, true, null ,"NoPerm");
					}
				} else {
					PM_.SendMessage(player, null, true, null ,"Syntax_Error");
				}
				plugin.bloquear.put("order", false);
			}
		}
	}

}
