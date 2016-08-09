package Client;

import Function.*;
import java.net.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.nio.*;
import java.util.*;
import java.io.*;

/*
 * Developer : Youn Hee Seung
 * Date : 2016 - 07 - 29
 * 
 * Name : FileShare
 * Description : It can share file
 * 
 * */

public class Client_FileShare_Send implements PacketRule
{
	// Instance
	private String _OTP = null;
	
	// About Files
	private FileChannel _fileChannel = null;
	private RandomAccessFile _raf = null;
	
	private String[] _fileNameArray = null;
	private int[] _fileNameSize = null;
	
	private String[] _filePathArray = null;
	
	private File _tempFile = null;
	private long[] _fileSizeArray = null;
	
	// Another Class Instance
	private Client_Server_Connector _csc = null;
	private Client_FileSelector _cfs = null;
	
	// Constructors
	public Client_FileShare_Send()
	{
		ByteChannel b = _fileChannel;
		
		try 
		{
			_csc = Client_Server_Connector.getInstance(4444);
			_cfs = new Client_FileSelector();
		}
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}
	
	// Methods
	public void start()	// when you click the button which turn on 1:1 file share system.
	{
		_cfs.fileFinderON();
		while(!_cfs.getSelectionFinish())
		{
			try 
			{
				Thread.sleep(5);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
		
		_fileNameArray = _cfs.getFileNames();
		_fileNameSize = new int[_fileNameArray.length];
		for(int i = 0; i < _fileNameSize.length; i++)
		{
			_fileNameSize[i] = _fileNameArray[i].length();
		}
		
		_filePathArray = _cfs.getFilePaths();
		
		_fileSizeArray = new long[_filePathArray.length];
		for(int i = 0; i < _filePathArray.length; i++)
		{
			_tempFile = new File(_filePathArray[i]);
			_fileSizeArray[i] = _tempFile.length();
		}
		
		sendFile();	// Temporary
	}
	
	public void sendFile()	// when you click send button
	{
		byte[] garbage = new byte[1024];

		try 
		{
			_csc.configurePacket("FILE_SHARE_SEND");
			byte[] OTP_Packet = new byte[100];
			OTP_Packet[0] = MAKE_OTP;
			_csc.setPacket("FILE_SHARE_SEND", OTP_Packet);
			_csc.setPacket("FILE_SHARE_SEND", garbage);
			_csc.sendAllNotRemove("FILE_SHARE_SEND");
			
			byte[] OTP_Byte = new byte[1024];
			OTP_Byte = _csc.receiveByteArray();
			_OTP = new String(OTP_Byte).trim();
		}
		catch (IOException e1) 
		{
			e1.printStackTrace();
		}
		changeFilesName();
		
		
		for(int i = 0; i < _fileNameArray.length; i++)
		{
			try 
			{
				_csc.configurePacket("FILE_SHARE_SEND");
				byte[] packet = new byte[100];
				packet[0] = FILE_SHARE_RECEIVE;
				packet[1] = (byte)_fileNameArray.length;
				packet[2] = (byte)String.valueOf(_fileSizeArray[i]).getBytes().length;
				packet[3] = (byte)_fileNameArray[i].getBytes().length;
				Function.frontInsertByte(4, String.valueOf(_fileSizeArray[i]).getBytes(), packet);
				Function.frontInsertByte(4 + String.valueOf(_fileSizeArray[i]).getBytes().length, _fileNameArray[i].getBytes(), packet);
				_csc.setPacket("FILE_SHARE_SEND", packet);	// 1
				
				_raf = new RandomAccessFile(_filePathArray[i], "rw");
				_fileChannel = _raf.getChannel();
				
				ByteBuffer buffer;
				while(_fileSizeArray[i] > 0)
				{
					buffer = ByteBuffer.allocateDirect(FILE_BUFFER_SIZE);
					buffer.clear();
					_fileSizeArray[i] -= 1024;
					_fileChannel.read(buffer);
					buffer.flip();
					_csc.setPacket("FILE_SHARE_SEND", buffer);
				}
				System.out.println(_fileNameArray[i] + " 파일이 전송이 완료되었습니다.");
				
				_csc.send("FILE_SHARE_SEND");
				_fileChannel.close();
			} 
			catch (FileNotFoundException e) 
			{
				e.printStackTrace();
			} 
			catch (IOException e) 
			{
				System.exit(1);
				e.printStackTrace();
			}
		}
	}
	
	private void changeFilesName()
	{
		String temp = "";
		for(int i = 0; i < _fileNameArray.length; i++)
		{
			temp = _fileNameArray[i];
			_fileNameArray[i] = _OTP + "★" + temp;
		}
	}
}
