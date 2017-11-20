package com.pudimcraft;
import com.pudimcraft.Wallet;
import com.pudimcraft.Coins;

import com.pudimcraft.Main;
//import com.vk2gpz.tokenenchant.TokenEnchant;

import java.security.SecureRandom;
import java.sql.ResultSet;
import java.util.Date;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;


public class Trade {

	public Main plugin;
	public Trade(Main main) {
		this.plugin = main;
	}
	
	public void ordersEx(int ORDER_ID) {
		
		if(!plugin.bloquear.get("trade")){
			plugin.bloquear.put("trade", true);
		}
		
		double QTD = 0;
		String COIN = null;
		String ORDER = null;
		String UUID_ = null;
		String UUID_CRIADOR =null;
		double PRICE = 0;
		double FEE = 0;

		try {
			ResultSet rs_or = plugin.query("SELECT * FROM `coins_orders` WHERE `id` = " + ORDER_ID + "");
			if (rs_or.next()) {
				QTD = rs_or.getDouble("amount");
				COIN = rs_or.getString("coin");
				PRICE = rs_or.getDouble("price");
				ORDER = rs_or.getString("type");
				UUID_ = rs_or.getString("uuid");

			}

			try {
				ResultSet rs_coin = plugin.query("SELECT * FROM `coins_coins` WHERE `coin` = '" + COIN + "'");
				if (rs_coin.next()) {
					FEE = rs_coin.getDouble("fee");
					UUID_CRIADOR= rs_coin.getString("uuid");
				}
			} catch (Exception ex) {
			}

			ResultSet rs = null;
			if (ORDER.equalsIgnoreCase("BUY")) {
				rs = plugin.query("SELECT * FROM `coins_orders` WHERE `price` <= " + PRICE
						+ " AND `type` = 'SELL' AND `coin` = '" + COIN + "' ORDER BY `price` ASC");
			} else {
				rs = plugin.query("SELECT * FROM `coins_orders` WHERE `price` >= " + PRICE
						+ " AND `type` = 'BUY' AND `coin` = '" + COIN + "' ORDER BY `price` DESC");
			}
			while (rs.next()) {
				
				double QTD_VENDEDOR = 0;
				double PRECO_VENDEDOR = 0;

				double QTD_DISPO = 0;
				double QTD_COMPRADOR = 0;
				double MONEY_DO_COMPRADOR = 0;
				double QTD_MOEDA_VENDEDOR = 0;
				double PRECO_COMPRADOR = 0;
				int ID_COMPRADOR = 0;
				int ID_VENDEDOR = 0;
				int ID_ORDEM_COMPRADOR = 0;
				int ID_ORDEM_VENDEDOR = 0;
				String UUID_COMPRADOR = null;
				String UUID_VENDEDOR = null;

				if (ORDER.equalsIgnoreCase("BUY")) {
					ID_ORDEM_VENDEDOR = rs.getInt("id");
					UUID_VENDEDOR = rs.getString("uuid");
					QTD_VENDEDOR = rs.getDouble("amount");
					PRECO_VENDEDOR = rs.getDouble("price");

					ResultSet rs_0 = plugin.query("SELECT * FROM `coins_orders` WHERE `id` = " + ORDER_ID + " AND `coin` = '" + COIN + "'"); // atualizar
																											// dados
					if (rs_0.next()) {
						QTD = rs_0.getDouble("amount");
						ID_ORDEM_COMPRADOR = rs_0.getInt("id");
						PRECO_COMPRADOR = rs_0.getDouble("price");
					}

					ResultSet rs_1 = plugin.query("SELECT * FROM `coins_players` WHERE `uuid`='" + UUID_ + "' LIMIT 1;");
					if (rs_1.next()) {
						MONEY_DO_COMPRADOR = rs_1.getDouble("money");
						UUID_COMPRADOR = rs_1.getString("uuid");
					}

					if (QTD_VENDEDOR >= QTD) {
						QTD_DISPO = QTD;
					} else {
						QTD_DISPO = QTD_VENDEDOR;
					}

				} else {
					UUID_COMPRADOR = rs.getString("uuid");
					ID_ORDEM_COMPRADOR = rs.getInt("id");
					QTD_COMPRADOR = rs.getDouble("amount");
					PRECO_COMPRADOR = rs.getDouble("price");
					ResultSet rs_3 = plugin.query(
							"SELECT * FROM `coins_players` WHERE `uuid`= '" + UUID_COMPRADOR + "' LIMIT 1");
					if (rs_3.next()) {
						MONEY_DO_COMPRADOR = rs_3.getDouble("money");
					}

					ResultSet rs_2 = plugin.query("SELECT * FROM `coins_orders` WHERE `id` = " + ORDER_ID + " AND `coin` = '" + COIN + "'"); // atualizardados
					if (rs_2.next()) {
						QTD = rs_2.getDouble("amount");
						PRECO_VENDEDOR = rs_2.getDouble("price");
						UUID_VENDEDOR = rs_2.getString("uuid");
						ID_ORDEM_VENDEDOR = rs_2.getInt("id");
					}

					if (QTD_COMPRADOR >= QTD) {
						QTD_DISPO = QTD;
					} else {
						QTD_DISPO = QTD_COMPRADOR;
					}

				}

				Player p_vendedor = Bukkit.getPlayer(UUID.fromString(UUID_VENDEDOR));
				int MSG_VENDEDOR = 1;
				if (p_vendedor == null) {
					MSG_VENDEDOR = 0;
				}

				Player p_comprador = Bukkit.getPlayer(UUID.fromString(UUID_COMPRADOR));
				int MSG_COMPRADOR = 1;
				if (p_comprador == null) {
					MSG_COMPRADOR = 0;
				}

				double PRECO = 0;
				if (ORDER.equalsIgnoreCase("BUY")) {
					if (PRECO_VENDEDOR <= PRECO_COMPRADOR) {
						PRECO = PRECO_VENDEDOR;
					} else {
						PRECO = PRECO_COMPRADOR;
					}
				} else {
					if (PRECO_COMPRADOR >= PRECO_VENDEDOR) {
						PRECO = PRECO_COMPRADOR;
					} else {
						PRECO = PRECO_VENDEDOR;
					}
				}

				ResultSet rs_4 = plugin.query("SELECT * FROM `coins_wallet` WHERE `coin` = '" + COIN + "' AND `uuid` = '"
						+ UUID_VENDEDOR + "'");
				if (rs_4.next()) {
					QTD_MOEDA_VENDEDOR = rs_4.getDouble("amount");
				}
				if (QTD_MOEDA_VENDEDOR < (QTD_DISPO * (1 + (FEE / 100)))) {
					QTD_DISPO = plugin.formatDouble(QTD_MOEDA_VENDEDOR / (1 + (FEE / 100)));
				}

				if (QTD_MOEDA_VENDEDOR != 0) {
					if (MONEY_DO_COMPRADOR < (QTD_DISPO * PRECO)) {
						QTD_DISPO = plugin.formatDouble(MONEY_DO_COMPRADOR / PRECO);
					}
					
					if (QTD_DISPO > 0) {
						if (plugin.PI.PlayerInfo(-QTD_DISPO * PRECO,-QTD_DISPO * PRECO_COMPRADOR, 0, null, UUID.fromString(UUID_COMPRADOR),false)) {// comprador
																											// #X2
							if (plugin.WT.Wallet(COIN, -QTD_DISPO, 1, 0, UUID.fromString(UUID_VENDEDOR), UUID_COMPRADOR,-QTD_DISPO,false)) { // comprador																										// #X3
								
								if (plugin.updateSQL("UPDATE `coins_orders` SET amount=amount-" + QTD_DISPO + " WHERE `id`="
										+ ID_ORDEM_COMPRADOR + " AND amount>=" + QTD_DISPO + "")) { // #X9
									
									if (plugin.updateSQL("UPDATE `coins_orders` SET amount=amount-" + QTD_DISPO
											+ " WHERE `id`=" + ID_ORDEM_VENDEDOR + " AND amount>=" + QTD_DISPO + "")) {
										
										plugin.PI.PlayerInfo(QTD_DISPO * PRECO,0, 0, null, UUID.fromString(UUID_VENDEDOR),false); // vendedor
										plugin.WT.Wallet(COIN, QTD_DISPO, 1, 0, UUID.fromString(UUID_COMPRADOR),UUID_VENDEDOR,0,false);
										
										if (MSG_VENDEDOR == 1) {
											p_vendedor.sendMessage("" + plugin.getConfig().getString("prefix")
													+ "§7Ordem §5#" + ID_ORDEM_VENDEDOR + "§f->§5#" + ID_ORDEM_COMPRADOR
													+ " §7executou §a" + QTD_DISPO + " §6" + COIN + " §7a §a$" + PRECO*QTD_DISPO
													+ "");
										}
										if (MSG_COMPRADOR == 1) {
											p_comprador.sendMessage("" + plugin.getConfig().getString("prefix")
													+ "§7Ordem §5#" + ID_ORDEM_COMPRADOR + "§f->§5#" + ID_ORDEM_VENDEDOR
													+ " §7executou §a" + QTD_DISPO + " §6" + COIN + " §7a §c$" + PRECO*QTD_DISPO
													+ "");
										}

										plugin.updateSQL(
												"UPDATE `coins_coins` SET vol_c=0,vol_m=0,trades_24hr=0,last_price_h=0, last_price_l=0, last_price_a=last_price, date='"
														+ plugin.Data() + "' WHERE `date`<>'" + plugin.Data() + "' AND `coin`='"
														+ COIN + "'");
																	
										plugin.updateSQL("UPDATE `coins_coins` SET trades=trades +" + 1 + ",trades_24hr=trades_24hr +" + 1 + ",vol_c=vol_c +" + QTD_DISPO + ",vol_m=vol_m +" + (QTD_DISPO * PRECO) + ",last_price=" + PRECO + ",capitalization=mining * " + PRECO + " WHERE `coin`='" + COIN + "'");

										
										plugin.updateSQL("UPDATE `coins_coins` SET last_price_h=" + PRECO
												+ " WHERE last_price_h <" + PRECO + " AND `coin`='" + COIN + "'");
										
										plugin.updateSQL("UPDATE `coins_coins` SET last_price_l=" + PRECO
												+ " WHERE (last_price_l >" + PRECO + " OR last_price_l=0) AND `coin`='"
												+ COIN + "'");
										

									} else {
										plugin.updateSQL(
												"UPDATE `coins_orders` SET amount=amount+" + QTD_DISPO + " WHERE `id`="
														+ ID_ORDEM_COMPRADOR + " AND amount>=" + QTD_DISPO + ""); // DEVOLVER
																													// #X9
									}
								} else {
									//QTD_DISPO+((FEE/100)+1)*QTD_DISPO
									double QTD_DISPO_DEVOLVER=plugin.formatDouble(((FEE/100)+1)*QTD_DISPO);
									plugin.WT.Wallet(COIN, QTD_DISPO_DEVOLVER, 0, 0, UUID.fromString(UUID_VENDEDOR), "ORDER",-QTD_DISPO_DEVOLVER,false); // devolve// #X3
									plugin.WT.Wallet(COIN, -(QTD_DISPO_DEVOLVER-QTD_DISPO), 0, 0, UUID.fromString(UUID_CRIADOR), "ORDER",0,false); // verificar melhor
									
									plugin.PI.PlayerInfo(0,0, 0,null, UUID.fromString(UUID_CRIADOR),false);
										
									
								}

							} else {
								plugin.PI.PlayerInfo(QTD_DISPO * PRECO,QTD_DISPO * PRECO_COMPRADOR, 0, null, UUID.fromString(UUID_COMPRADOR),false); // devolver																			// #X2
							}
						}

					} else {
						plugin.update("DELETE FROM `coins_orders` WHERE `id`=" + ID_ORDEM_COMPRADOR + "");
						if (MSG_COMPRADOR == 1) {
							p_comprador.sendMessage("" + plugin.getConfig().getString("prefix") + "§cOrdem §5#"
									+ ID_ORDEM_COMPRADOR + " §cfoi cancelada");
						}
					}
				} else {
					plugin.update("DELETE FROM `coins_orders` WHERE `id`=" + ID_ORDEM_VENDEDOR + "");
					if (MSG_VENDEDOR == 1) {
						p_vendedor.sendMessage("" + plugin.getConfig().getString("prefix") + "§cOrdem §5#"
								+ ID_ORDEM_VENDEDOR + " §cfoi cancelada");
					}
				}
				if (QTD == 0 || QTD - QTD_DISPO == 0) {
					break;
				}
			}
			
			String UUID_ORDEM="";
			ResultSet rs_trades = plugin.query("SELECT * FROM `coins_orders` WHERE `amount` = 0");
			while (rs_trades.next()) {
				UUID_ORDEM = rs_trades.getString("uuid");
				//Coins.p_trades.put(UUID.fromString(UUID_ORDEM), Coins.p_trades.get(UUID.fromString(UUID_ORDEM))-1 );
				RemTrade(UUID.fromString(UUID_ORDEM));
			}
			
			plugin.update("DELETE FROM `coins_orders` WHERE amount=0");

		} catch (Exception ex) {
		}
		
		plugin.bloquear.put("trade", false);
		
	}

	public int TotalTrade(Player player){
		int N=0;
		try {
			ResultSet rs_soma = plugin.query("SELECT COUNT(id) FROM `coins_orders` WHERE `uuid`='" + player.getUniqueId() + "'");
			if (rs_soma.next()) {
				N = rs_soma.getInt(1);
			}
		} catch (Exception ex) {
		}
		return N;
	}
	
	public boolean AddTrade(UUID uuid){
		Bukkit.getServer().broadcastMessage(uuid.toString());
		Bukkit.getServer().broadcastMessage(String.valueOf(plugin.p_trades.get(uuid)));
		if(plugin.p_trades.get(uuid)<10){
			plugin.p_trades.put(uuid, plugin.p_trades.get(uuid)+1 );
			return true;
		}else{
			plugin.PM.SendMessage(null, uuid, true, "Voce chegou ao maximo de ordens.",null);
			return false;
		}
	}
	
	public boolean RemTrade(UUID uuid){
		if(plugin.p_trades.get(uuid)>0){
			plugin.p_trades.put(uuid, plugin.p_trades.get(uuid)-1 );
			return true;
		}else{
			return false;
		}
		}
	
	
}
