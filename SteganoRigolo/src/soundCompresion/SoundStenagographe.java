package soundCompresion;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

public class SoundStenagographe implements ISoundSteganographe {



	public  int bitwise = 1;
	public int msglength =0;


	public  SoundStenagographe(int bitwise) {
		this.bitwise = bitwise;

	}

	@Override
	public File encode(String masquePath, String targetPath,String resultPath) throws IllegalArgumentException, UnsupportedAudioFileException,Exception  {


		System.out.println("Stegano Pour Son");

		File target = new File(targetPath);

		if(!target.exists())
		{
			throw new FileNotFoundException(targetPath);	
		}

		AudioInputStream masque = AudioIO.getAudioData(masquePath);

		AudioFormat convertFormat = new AudioFormat(44100, 16, 1, true, false);

		byte[] masqueBytes = AudioIO.getAudioDataBytes(AudioIO.getBytes(masque),convertFormat); //AudioIO.getBytes(masque);

		byte[] message = Files.readAllBytes(target.toPath());
		byte[] messageLength = new byte[4];


		int messageLengthInt = message.length;
		 messageLength = ByteBuffer.allocate(4).putInt(messageLengthInt).array();
		 for (byte b : messageLength) {
			   System.out.format("0x%x ", b);
			}
	        System.err.println("Taille message :"+messageLengthInt);


		byte[] musicToWork = increaseMusicLenght(message.length ,masqueBytes);
		msglength = message.length;

		this.LSBencode(messageLength, musicToWork, 0);
		this.LSBencode(message.clone(), musicToWork, 32);


		//AudioIO.saveFile(masqueBytes, resultPath,AudioFileFormat.Type.WAVE , masque.getFormat());
		AudioIO.saveFile(musicToWork, resultPath,AudioFileFormat.Type.WAVE ,convertFormat);


	System.out.println(Arrays.equals((musicToWork), (AudioIO.getBytes(AudioIO.getAudioData(resultPath)))));


		masque.close();

		return new File(resultPath);
	}



	@Override
	public File decode(String masquePath,String resultPath) throws IOException {

		System.out.println("DeStegano Pour Son");

		
		AudioInputStream masque = AudioIO.getAudioData(masquePath);
		byte bytesMasque[] = AudioIO.getBytes(masque);

		byte[] data =  LSBdecode(bytesMasque);  
		
		

		Path path = Paths.get(resultPath);
		Files.write(path, data);

		
		masque.close();
		return new File(resultPath);
	}
	public byte[] LSBdecode( byte[] audioData)
	{
		        int messageLength = 0;
       

        byte[] load = new byte[4];
        for(int i  = 3; i >=0 ;i-- )
        {
        	load[i] += audioData[i*8] & 1;
        	for(int j = 7 ; j >=0; j-- )
        	{
        		  load[i] <<= 1;
        		  load[i] += audioData[i*8+j] & 1;
        	}
        }
        
        messageLength= (load[0]<<24)&0xff000000|
        	       (load[1]<<16)&0x00ff0000|
        	       (load[2]<< 8)&0x0000ff00|
        	       (load[3]<< 0)&0x000000ff;
        
        
         
        System.err.println("Taille message calcul:"+messageLength);
        byte[] data = new byte[messageLength];
        int dataIndex = 0;
        for (int audioIndex = 32 ;dataIndex < messageLength /*audioIndex < messageLength + 32*/; audioIndex+=8, dataIndex++)
        {
                data[dataIndex] += audioData[audioIndex+7] & 1;
                for (int i = 6; i >= 0; i--)
                {
                        data[dataIndex] <<= 1;
                        data[dataIndex] += audioData[audioIndex + i] & 1;
                }
               
                //System.err.println("Byte " + dataIndex + " : " + data[dataIndex]);
        }
        System.out.println("indexMsg :"+dataIndex );

        return data;
	}

	public void LSBencode(byte[] message, byte[] audioData, int startIndex)
	{
		int messageIndex = 0;
        for (int audioDataIndex = startIndex; messageIndex < message.length; audioDataIndex+=8, messageIndex++)
            for (int j = 0; j < 8; j++)
            {
                  
                    audioData[audioDataIndex+j] = (byte) ( (audioData[audioDataIndex+j] & ~1) ^ (message[messageIndex] & 1) );
                    message[messageIndex] >>= 1;
            }

        System.out.println("indexMsg :"+messageIndex );
	}



	
	private byte[] increaseMusicLenght(int tailleMessage,byte[] musiqueold)
	{
		if(tailleMessage*8+32 <= musiqueold.length)
		{
			return musiqueold;
		}
		else
		{
			int rapport = ((tailleMessage*8+32)/musiqueold.length) +1 ;
			byte[] res = new byte[rapport*musiqueold.length];

			for(int i = 0 ; i < rapport  ; i++  )
			{
				for(int j = 0 ; j < musiqueold.length ; j++)
				{
					res[i*j + j] = musiqueold[j]; 
				}
			}

				return res;
			}


		}


	public  int getBitwise() {
		return bitwise;
	}





	public  void setBitwise(int bitwise) {
		this.bitwise = bitwise;
	}
	
	
	public void comparaison ( byte[] a,byte[] b )
	{
		for(int i = 0 ; i < a.length ; i++)
		{
			if(a[i] != b[i])
			{

				System.err.println("erreur [" +i+"]"+a[i]+" "+b[i]  );
			}
		}
		
	}

}
