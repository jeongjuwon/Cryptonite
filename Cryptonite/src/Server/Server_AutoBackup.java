package Server;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import Function.*;

/*
 * Developer : Youn Hee Seung
 * Date : 2016 - 07 - 29
 * 
 * Name : AutoBackup System
 * Description : It can backup encrypted files
 * 
 * */

public class Server_AutoBackup extends Server_Funtion implements PacketRule
{
	// protectedFolder
	private String _address = "Server_Folder\\";
	private File _protectedFolder;
	
	// Instance
	private int _count = 1;
	
	// About File
	private String _checkProperty = null;
	private String _fileName = null;
	private long _fileSize = 0;
	
	private RandomAccessFile _raf = null;
	private FileChannel _fileChannel = null;
	private PacketProcessor p = null;
	
	// Constructors
	public Server_AutoBackup() { }
	
	private void setFolder()
	{
		_address = _address + _activity.getClientCode();
		_protectedFolder = new File(_address);
		if(!_protectedFolder.exists())
		{
			_protectedFolder.mkdirs();
		}
	}
	
	// Methods
	private int sendPacketSize(long fileSize)
	{
		int remainder = (int)(fileSize / FILE_BUFFER_SIZE);
		if((fileSize % FILE_BUFFER_SIZE) > 0)
		{
			remainder++;
		}
		
		return remainder;
	}
	
	private void setFileInformation(byte[] packet)
	{
		_checkProperty = "FILE";
		
		int end = 0;
		
		byte[] sizeTemp = new byte[packet[2]];
		for(int i = 0; i < sizeTemp.length; i++)
		{
			sizeTemp[i] = packet[i + 4];
			end = i+4;
		}
		_fileSize = Long.parseLong(new String(sizeTemp).trim());
		
		int max = end;
		while(packet[max] != 0)
		{
			max++;
		}
		
		byte[] nameTemp = new byte[packet[3]];
		for(int i = 0; i < nameTemp.length; i++)
		{
			nameTemp[i] = packet[i + end + 1];
		}
		_fileName = new String(nameTemp).trim();
		System.out.println("파일 이름 : " + _fileName);
		System.out.println("파일 용량 : " + _fileSize + " (Byte)");
	}
	
	@Override
	public void Checker(byte[] packet, Server_Client_Activity activity) 
	{
		_activity = activity;
		//_activity.receive.setAllocate(1024);
		
		setFolder();
		if(packet[1] == DIRECTORY)
		{
			_checkProperty = "DIRECTORY";
			_packetMaxCount = 2;
		}
		else if(packet[1] == FILE)
		{
			setFileInformation(packet);
			_packetMaxCount = 1 + sendPacketSize(_fileSize);
			
			try {
				_raf = new RandomAccessFile(_address + "\\" + _fileName, "rw");
				_fileChannel = _raf.getChannel();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			p = new PacketProcessor(_fileChannel, false);
			_activity.receive.setAllocate(_fileSize);
		}
	}

	@Override
	public void running() throws IOException 
	{
		//System.out.println("NOW AUTOBACKUP RUNNING");
		if(_checkProperty.equals("DIRECTORY"))
		{
			_fileName = new String(_activity.receive.getByte()).trim();
			File newFolder = new File(_address + "\\" + _fileName);
			newFolder.mkdir();
		}
		else if(_checkProperty.equals("FILE"))
		{	
			while(_activity.IsReadable())
			{
				_count++;
				p.setPacket(_activity.receive.getByte()).write();
			}
			
			if(_count == _packetMaxCount)
			{
				System.out.println("AUTOBACKUP COMPLETE !!");
				p.close();
				_count = 1;
			}
			
			/*try 
			{
				ByteBuffer buffer;
				buffer = ByteBuffer.allocateDirect(FILE_BUFFER_SIZE);
				while(_activity.IsReadable())
				{
					_count++;
					buffer.clear();
					buffer.put(_activity.receive.getByte());
					
					_fileSize -= FILE_BUFFER_SIZE;
					buffer.flip();
					while(!_fileChannel.isOpen())
					{
						Thread.sleep(1);
					}
					_fileChannel.write(buffer);
					
					if(_fileSize <= 0)
					{
						break;
					}
				}
				
				if(_count == _packetMaxCount)
				{
					System.out.println("끝");
					_fileChannel.close();
					_count = 1;
				}
			} 
			catch (FileNotFoundException e) 
			{
				e.printStackTrace();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}*/
		}
	}
}