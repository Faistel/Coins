package com.pudimcraft;

import com.pudimcraft.Main;
//import com.vk2gpz.tokenenchant.TokenEnchant;

import java.security.SecureRandom;
import java.sql.ResultSet;
import java.util.Date;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerInfo {

	public Main plugin;
	public PlayerInfo(Main main) {
		this.plugin = main;
	}
	
	public boolean PlayerInfo(double Money, double bloquear, double Player_Hash, String Player_Mining, UUID uuid_,
			boolean chat) { /////////// verficiar
		/////////// uuid
		Player p = Bukkit.getPlayer(uuid_);

		String uuid;
		int EnvMsg = 1;
		if (p != null) {
			uuid = p.getUniqueId().toString();
		} else {
			uuid = uuid_.toString();
			EnvMsg = 0;
		}
		if (chat == false) {
			EnvMsg = 0;
		}

		double Upgrade_cost = 0;
		double price = plugin.getConfig().getDouble("Price");
		int totallevel = plugin.getConfig().getInt("MaxLevel");
		double Player_Hash2 = 0;
		int erro = 0;
		double Player_Money = 0;
		double Player_Money_Total=0;
		double Player_money_blocked = 0;
		double Player_Money_ = 0;
		String Player_Mining2 = "";
		try {
			ResultSet rs = plugin.query("SELECT * FROM `coins_players` WHERE `uuid` = '" + uuid + "';");
			if (rs.next()) {
				Player_Hash2 = rs.getDouble("hashpower");
				Player_Money = rs.getDouble("money");
				Player_money_blocked = rs.getDouble("money_blocked");
				Player_Money_Total=Player_Money;
				
				Player_Money=Player_Money-Player_money_blocked;
				if(bloquear<0){
					Player_Money=Player_Money-bloquear; //se bloquear for menor que zero, add no saldo
				}
				
				Player_Mining2 = rs.getString("mining");
			}
		} catch (Exception ex) {
		}
		

		if (Player_Hash > 0) {
			Upgrade_cost = (price * Math.pow(2, Player_Hash2 + Player_Hash));// price

			if (Player_Money >= Upgrade_cost) {
				if (Player_Hash2 + Player_Hash > totallevel) {
					Player_Hash = totallevel;
					erro = 1;
					String totallevel_out = Integer.toString(totallevel);
					if (EnvMsg == 1) {
						p.sendMessage(plugin.getConfig().getString("prefix")
								+ plugin.getConfig().getString("Level_Max").replace("{MAX_LEVEL}", totallevel_out));
					}
				} else {
					Player_Hash = Player_Hash2 + Player_Hash;
					String Player_Hash_out = Double.toString(Player_Hash);
					if (EnvMsg == 1) {
						p.sendMessage(plugin.getConfig().getString("prefix") + plugin.getConfig()
								.getString("UpgradeSuccessfully").replace("{UP_LEVEL}", Player_Hash_out));
						// Coins.p_hashpower.put(p.getUniqueId(),
						// Double.toString(Player_Hash));
					}
					Money = Money - Upgrade_cost;
				}
			} else {
				erro = 1;
				if (EnvMsg == 1) {
					p.sendMessage(
							plugin.getConfig().getString("prefix") + plugin.getConfig().getString("Not_Enought_Money"));
				}
			}
		} else {
			Player_Hash = Player_Hash2;
		}

		if (Money != 0) {
			if (Player_Money + Money >= 0) {
				Player_Money_ = Player_Money + Money;
				if (Player_Money < Player_Money_) {
					if (EnvMsg == 1) {
						p.sendMessage("" + plugin.getConfig().getString("prefix") + "§7Foi depositado §a$"
								+ plugin.formatDouble(Math.abs(Money)) + " §7em sua carteira.");
					}
				} else {
					if (EnvMsg == 1) {
						p.sendMessage("" + plugin.getConfig().getString("prefix") + "§7Foi debitado  §c$"
								+ plugin.formatDouble(Math.abs(Money)) + " §7de sua carteira.");
					}
				}

			} else {
				Player_Money_ = Player_Money;
				erro = 1;
				if (EnvMsg == 1) {
					p.sendMessage("" + plugin.getConfig().getString("prefix") + "Voce nao tem todo esse dinheiro.");
				}
			}
		} else {
			Player_Money_ = Player_Money;
		}

		if (Player_Mining == null) {
			Player_Mining = Player_Mining2;
		} else {
			if (Player_Mining.equalsIgnoreCase(Player_Mining2)) {
				Player_Mining = "";
				if (EnvMsg == 1) {
					p.sendMessage("" + plugin.getConfig().getString("prefix") + "Voce desativou a mineração.");
					plugin.p_mining.put(p.getUniqueId(), null);
				}
			} else {
				Player_Mining = Player_Mining.toUpperCase();
				if (EnvMsg == 1) {
					p.sendMessage("" + plugin.getConfig().getString("prefix") + "Minerando §6"+Player_Mining.toUpperCase()+"§7.");
					plugin.p_mining.put(p.getUniqueId(), Player_Mining);
				}
			}
		}
		
		if(Player_Money_<bloquear){
			if (p != null) {
				p.sendMessage("" + plugin.getConfig().getString("prefix") + "Voce nao possui toda essa quantidade.");
			}
			erro =1;
		}

		if (erro == 0) {
			plugin.p_mining.put(p.getUniqueId(), Player_Mining);
			plugin.p_hashpower.put(p.getUniqueId(), Player_Hash);
			
			
			double valor_bloquear= plugin.formatDouble(Player_money_blocked+bloquear);
			double Player_money_atu=Player_Money_Total+Money;
	
			if(plugin.updateSQL("REPLACE INTO coins_players SET `uuid` = '" + uuid + "', `money` = " + Player_money_atu
					+ ", `money_blocked` = " + valor_bloquear+ ", `hashpower` = " + Player_Hash + ", `trades` =trades , `mining` = '" + Player_Mining + "';")){
				
			}else{
				if (p != null) {
					p.sendMessage("" + plugin.getConfig().getString("prefix") + "Erro ao enviar");
				}
				return false;
			}
			return true;
		} else {
			return false;
		}
	}

}
