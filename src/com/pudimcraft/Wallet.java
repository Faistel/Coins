package com.pudimcraft;

import com.pudimcraft.Main;
//import com.vk2gpz.tokenenchant.TokenEnchant;

import java.security.SecureRandom;
import java.sql.ResultSet;
import java.util.Date;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;


public class Wallet {

	public Main plugin;
	public Wallet(Main main) {
		this.plugin = main;
	}
	
	public boolean Wallet(String Coin_COIN, double Coin_AMOUNT, int fee_, double fee_2, UUID uuid_, String O_UUID,double bloquear, boolean chat) {
		// int fee_=1 combra as taxas e envia para uuid; se for 2 cobra a taxa
		// mas nao envia
		Bukkit.getServer().broadcastMessage(String.valueOf(Coin_AMOUNT));
		Coin_AMOUNT = plugin.formatDouble(Coin_AMOUNT);
		fee_2 = plugin.formatDouble(fee_2);

		Player p = Bukkit.getPlayer(uuid_);
		Coin_COIN = Coin_COIN.toUpperCase();

		String uuid;
		int EnvMsg = 1;
		if (p != null) {
			uuid = p.getUniqueId().toString();
		} else {
			uuid = uuid_.toString();
			EnvMsg = 0;
			// Bukkit.getServer().broadcastMessage(""+this.getConfig().getString("prefix")+"UUID
			// OFFLINE: "+uuid+"");
		}
		if(chat==false){
			EnvMsg = 0;
		}

		double Coin_FEE_COINS = 0;
		double Coin_FEE = 0;
		String Coin_UUID_CRIADOR = "";
		try {
			ResultSet rs = plugin.query("SELECT * FROM `coins_coins` WHERE `coin` = '" + Coin_COIN + "' LIMIT 1;");
			if (rs.next()) {
				Coin_FEE = rs.getDouble("fee");
				Coin_UUID_CRIADOR = rs.getString("uuid");
			}
		} catch (Exception ex) {
		}

		if (fee_ == 1 || fee_ == 2 || fee_2 > 0) { // calcula as taxas
			//double Coin_FEE_ = Coin_FEE + fee_2;

			Coin_AMOUNT=Coin_AMOUNT+FeeTotal(Coin_AMOUNT,Coin_FEE+fee_2);
			bloquear=bloquear+FeeTotal(bloquear,Coin_FEE+fee_2);
	
			
			//double Coin_FEE_ = 0;
			//Coin_FEE = plugin.formatDouble(1 - (Coin_FEE_ / 100));
			//Coin_FEE_COINS = plugin.formatDouble(Math.abs(Coin_AMOUNT) - (Coin_FEE * Math.abs(Coin_AMOUNT)));
			//Coin_AMOUNT = plugin.formatDouble(Coin_AMOUNT - Coin_FEE_COINS- fee_2);
			//bloquear= plugin.formatDouble(bloquear * ((Coin_FEE_/100)+1));
		}

		int wallet_CoinID = 0;
		double wallet_amount = 0;
		double wallet_amount_total = 0;
		double wallet_amount_ = 0;
		double wallet_amount_blocked=0;
		String wallet_uuid = "";
		try {
			ResultSet rs = plugin.query("SELECT * FROM `coins_wallet` WHERE `coin` = '" + Coin_COIN + "' AND `uuid` = '" + uuid
					+ "' LIMIT 1;");
			if (rs.next()) {
				wallet_CoinID = rs.getInt("id");
				wallet_amount_total = rs.getDouble("amount");
				wallet_amount_blocked = rs.getDouble("amount_blocked");
				
				wallet_amount=plugin.formatDouble(wallet_amount_total-wallet_amount_blocked);
				if(bloquear<0){
					wallet_amount=wallet_amount-bloquear;
				}
			}
		} catch (Exception ex) {
		}
		
		
		if(wallet_amount<bloquear){
			if (p != null) {
				p.sendMessage("" + plugin.getConfig().getString("prefix") + "Voce nao possui toda essa quantidade.");
			}
			return false;
		}
		

		if (wallet_amount + Coin_AMOUNT >= 0) {
			//wallet_amount_ = wallet_amount + Coin_AMOUNT;
			double valor_bloquear=0;
			double saldo_disponivel=0;
			saldo_disponivel=wallet_amount;
						

			valor_bloquear= plugin.formatDouble(wallet_amount_blocked+bloquear);
			wallet_amount_ =  plugin.formatDouble(wallet_amount_total + Coin_AMOUNT);
			
			if (wallet_amount_ > 0 || valor_bloquear>0) {
				plugin.update("REPLACE INTO coins_wallet SET `id` = " + wallet_CoinID + ", `coin` = '" + Coin_COIN
						+ "', `amount` =" + wallet_amount_ + ", `amount_blocked`="+valor_bloquear+",`uuid`='" + uuid + "';");
			} else {
				plugin.update("DELETE FROM `coins_wallet` WHERE `id` = " + wallet_CoinID + "");
			}

			if (Coin_AMOUNT > 0) {
				plugin.update("INSERT INTO `coins_history` (`id`, `coin`, `amount`, `total`, `O_uuid`, `R_uuid`) VALUES (NULL, '"
						+ Coin_COIN + "', '" + Math.abs(Coin_AMOUNT) + "', '" + wallet_amount_ + "', '" + O_UUID
						+ "', '" + uuid + "');");
			}
			if (Coin_AMOUNT < 0) {
				plugin.update("INSERT INTO `coins_history` (`id`, `coin`, `amount`, `total`, `O_uuid`, `R_uuid`) VALUES (NULL, '"
						+ Coin_COIN + "', '" + Math.abs(Coin_AMOUNT) + "', '" + wallet_amount_ + "', '" + uuid + "', '"
						+ O_UUID + "');");
			}
			
			if(Coin_AMOUNT!=0){
				if (wallet_amount_ > wallet_amount) {
					if (EnvMsg == 1) {
						p.sendMessage("" + plugin.getConfig().getString("prefix") + "Foi depositado §a"
								+ Math.abs(Coin_AMOUNT) + " §6" + Coin_COIN + " §7em sua carteira");
					}
				} else {
					if (EnvMsg == 1) {
						p.sendMessage("" + plugin.getConfig().getString("prefix") + "Foi debitado §c" + Math.abs(Coin_AMOUNT)
								+ " §6" + Coin_COIN + " §7de sua carteira");
					}
				}
			}
			
			if (Coin_FEE_COINS != 0) {
				if (EnvMsg == 1) {
					p.sendMessage("" + plugin.getConfig().getString("prefix") + "Taxas: §f"
							+ plugin.formatDouble(((1 - Coin_FEE)) * 100) + "% §7= §c" + Coin_FEE_COINS + " §6" + Coin_COIN
							+ "");
				}
			}
			
			if (Coin_FEE_COINS != 0 && fee_ == 1) {
				boolean chat_fee = true;
				if(Coin_UUID_CRIADOR.equalsIgnoreCase(uuid)){
					if(EnvMsg==0){
						chat_fee=false;
					}
				}
				Wallet(Coin_COIN, Coin_FEE_COINS, 0, 0, UUID.fromString(Coin_UUID_CRIADOR), uuid,0,chat_fee);
			}
			return true;
		} else {
			if (EnvMsg == 1) {
				p.sendMessage("" + plugin.getConfig().getString("prefix") + "§cVoce nao possui toda essa quantidade de §6"
						+ Coin_COIN + "");
			}
			return false;
		}
	}
	
	public double FeeTotal(double Coin_AMOUNT,double Taxas){
		double TotalEnviado=Math.ceil(Coin_AMOUNT);
		double TotalDeTaxas=TotalEnviado*Taxas; // total de texas
		return plugin.formatDouble(TotalDeTaxas);
	}

}
