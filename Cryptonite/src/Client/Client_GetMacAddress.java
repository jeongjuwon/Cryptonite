package Client;

import java.net.*;

/*
 * Developer : Youn Hee Seung
 * Date : 2016 - 07 - 22
 * 
 * Name : Get Mac-Address
 * Description : if you know InetAddress, then you can find your device address.
 * 
 * */

public class Client_GetMacAddress 
{
	// MacAddress Instance
	private NetworkInterface _ni = null;
	private byte[] _mac = null;
	private String _macAddress = "";
	
	// Methods
	public Client_GetMacAddress(InetAddress localIP)
	{
		try 
		{
			_ni = NetworkInterface.getByInetAddress(localIP);
			_mac = _ni.getHardwareAddress();
			for(int i  = 0; i < _mac.length; i++) 
			{
				_macAddress += String.format("%02X%s", _mac[i], (i < _mac.length - 1) ? "-" : "");
			}
		} 
		catch (SocketException e) 
		{
			e.printStackTrace();
		}
	}
	
	public String getMacAddress()
	{
		return this._macAddress;
	}
}
