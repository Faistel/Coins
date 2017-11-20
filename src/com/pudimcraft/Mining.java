package com.pudimcraft;
import com.pudimcraft.Wallet;

import com.pudimcraft.Main;
//import com.vk2gpz.tokenenchant.TokenEnchant;

import java.security.SecureRandom;
import java.sql.ResultSet;
import java.util.Date;
import java.util.UUID;

import org.bukkit.entity.Player;


public class Mining {

	public Main plugin;
	public Mining(Main main) {
		this.plugin = main;
	}
	
	public void NewBlock(Player player, String coin) {
				String Coin_Block = null;
				String Coin_name = null;
				String Coin_COIN=null;
				int Coin_height = 0;
				String Coin_coin = null;
				double Coin_time_blocks = 0;
				double Coin_quantidade = 0;
				double Coin_ming = 0;
				double Coin_limit = 0;
				double Coin_halving = 0;
				double Coin_halving_n = 0;
				String Coin_UUIDCRI = null;
				double Coin_TIME_AVG=0;
				long Coin_last_TIME=0;
				int Coin_Size=0;
				try {
					ResultSet rs = plugin
							.query("SELECT * FROM `coins_coins` WHERE `coin` = '" + coin + "' LIMIT 1;");
					if (rs.next()) {
						Coin_Block = rs.getString("block");
						Coin_name = rs.getString("name");
						Coin_COIN = rs.getString("coin");
						Coin_height= rs.getInt("height");
						Coin_coin = rs.getString("coin");
						Coin_time_blocks = rs.getDouble("time_blocks");
						Coin_TIME_AVG = rs.getDouble("avg_time_blocks");
						Coin_last_TIME = rs.getInt("last_time_blocks");
						Coin_quantidade = rs.getDouble("quantity");
						Coin_ming = rs.getDouble("mining");
						Coin_limit = rs.getDouble("limit");
						Coin_UUIDCRI = rs.getString("uuid");
						Coin_halving = rs.getDouble("halving");
						Coin_halving_n = rs.getDouble("halving_n");
						Coin_Size = rs.getInt("size");
					}
				} catch (Exception ex) {
				}

				if(!plugin.SzNum(Coin_quantidade)){
					Coin_quantidade=0;
				}
				
				if ((Coin_ming + Coin_quantidade) > Coin_limit) {
					Coin_quantidade = Coin_limit - Coin_ming; // limite dos
																// blocos
				}
		
				double depositar=0;
				depositar=trans(coin,player,Coin_Size)+Coin_quantidade;
				
				
							long secs = (new Date().getTime())/1000;
							
							if(Coin_last_TIME==0){
								Coin_last_TIME=secs;
							}
							
							long delta_time=(secs-Coin_last_TIME);
							
							Double AVG_TIME=(Coin_TIME_AVG+delta_time)/2;
							
							double dif_factor=1;
							if(delta_time>Coin_time_blocks){
								dif_factor=0.9;
							}else{
								dif_factor=1.1;
							}
							//double H_FT = halving_calculo(Coin_halving, Coin_height, Coin_halving_n, Coin_quantidade);
							
							double H_FT=1;
							double H_FT2=0;
							if(Coin_height/(Coin_halving_n+1)>=Coin_halving){
								H_FT=2;
								H_FT2=1;
							}
							
							
							if(plugin.updateSQL("UPDATE `coins_coins` SET `mining` = mining + " + Coin_quantidade
									+ ",avg_time_blocks="+AVG_TIME+",last_time_blocks="+secs+",difficulty=difficulty*"+dif_factor+", capitalization=mining*last_price,`quantity` = quantity / " + H_FT + ",`halving_n` = halving_n + " + H_FT2 + " WHERE `coin` = '" + Coin_coin + "';"))
							{
								plugin.WT.Wallet(Coin_coin, depositar, 0, 0, player.getUniqueId(), "MINING",0,true);
								plugin.c_block.put(Coin_COIN, Coin_Block);
								
								double dificuldade=plugin.c_difficulty.get(Coin_COIN)*dif_factor;
								plugin.c_difficulty.put(Coin_COIN, dificuldade);	
								plugin.c_height.put(Coin_COIN, Coin_height);
							}

			}
		

	

	public static int generateRandomInteger(int min, int max) {
		SecureRandom rand = new SecureRandom();
		rand.setSeed(new Date().getTime());
		int randomNum = rand.nextInt((max - min) + 1) + min;
		return randomNum;
	}
	
	/*

	public double halving_calculo(double X, double Y, double H, double B_) {
		double B, K = 0, HC = 0;
		B = B_;
		if (Y != 0) {
			double VER = (X * H) / (Y + B);
			if (VER <= 1) {
				B = B + Y;
				Y = 0;
			} else {
				B = B_;
			}
		}
		while (B > 0) {
			double X_ = H * X;
			if (Y + B <= X_) {
				K = B;
			} else {
				K = X_ - Y;
			}
			B = B - K;
			double H_ = (X_) / (Y + K);
			if (H_ == 1) {
				HC = HC + 2;
				H = H + 1;
				Y = Y + K;
			}
		}
		if (HC == 0) {
			HC = 1;
		}
		return HC;
	}
	*/
	
	public double trans(String coin, Player player, int size){
		String TRASF_C_UUID = "";
		String TRASF_R_UUID = "";
		String TRASF_O_UUID = "";
		String TRASF_COIN = "";
		double TRASF_FEE = 0;
		double TRASF_AMOUNT = 0;
		double qtd = 0;
		double A_RECEBER=0;
		try {
			ResultSet rs = plugin.query("SELECT * FROM `coins_transactions` WHERE `coin` = '"
					+ coin + "' LIMIT "+size+";");
			while (rs.next()) {
				TRASF_O_UUID = rs.getString("O_uuid");
				TRASF_C_UUID = rs.getString("C_uuid");
				TRASF_R_UUID = rs.getString("R_uuid");
				TRASF_COIN = rs.getString("coin");
				TRASF_FEE = rs.getDouble("fee");
				TRASF_AMOUNT = rs.getDouble("amount");
				
				if (plugin.updateSQL("UPDATE `coins_transactions` SET `C_uuid` = '" + player.getUniqueId()
				+ "' WHERE `coin` = '" + coin + "' AND `C_uuid` ='' LIMIT 1;") == true) {
					//qtd = plugin.formatDouble((TRASF_FEE / 100) * TRASF_AMOUNT);
					qtd = TRASF_FEE;
					A_RECEBER=A_RECEBER+qtd;

					//Wallet(TRASF_COIN, qtd, 0, 0, UUID.fromString(TRASF_C_UUID), TRASF_O_UUID); // quemconfirmou
					plugin.WT.Wallet(TRASF_COIN, TRASF_AMOUNT, 0, 0, UUID.fromString(TRASF_R_UUID),TRASF_O_UUID,0,true); //jogador//que//recebe//a,trasasao
				}	
			}
		} catch (Exception ex) {
		}
		if(A_RECEBER!=0){
		//plugin.Wallet(TRASF_COIN, A_RECEBER, 0, 0, player.getUniqueId(), TRASF_O_UUID); // a receber
		}
		plugin.update("DELETE FROM `coins_transactions` WHERE `coin` = '"+coin+"' AND `C_uuid` <>'';");
		return A_RECEBER;
	}
	

}
