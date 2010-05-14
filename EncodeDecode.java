import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class EncodeDecode {
		
		// Others
	
	public static String IntToReadableString( long IntToCode ){
		String s = "";
		int len = 0;
		while(IntToCode != 0){
			if(len == 3){
				len = 0;
				s = "," + s;
			}			s = IntToCode%10 + s;
			IntToCode /= 10;
			len++;
		}
		return s;
	}
	
	
	
	
		// File Functions
	
	public static int[] FileToIntArray( String FullFileName ) throws Exception {
		File file = new File(FullFileName);
		int length = (int)file.length();
		int[] IntArray = new int[length];	
		
		FileInputStream FileStream = new FileInputStream(FullFileName);
		BufferedInputStream FileBuffered = new BufferedInputStream(FileStream, 1);
		byte data[] = new byte[length];

		FileBuffered.read(data, 0, length);
		
		for( int i=0; i<data.length; i++)
			IntArray[i] = ByteToInt(data[i]);
			
		/*
		for( int i=0 ; i < length ; i++ ){
			FileBuffered.read(data, 0, 1);
			int test = ByteToInt(data[i]);
			System.out.println("[" + i + "] " + DecToHex(test));
		}
		*/
		
		FileBuffered.close();
		return IntArray;
	}
	
	public static void IntArrayToFile( String OutputFileName, int[] IntArray) throws Exception {
		FileOutputStream FileStream = new FileOutputStream( OutputFileName );
		BufferedOutputStream Buffer = new BufferedOutputStream( FileStream, 1 );
		
		byte data[] = new byte[IntArray.length];
		for( int i=0 ; i<IntArray.length ; i++ )
			data[i] = IntToByte( IntArray[i] );
		
		Buffer.write(data);
		Buffer.close();	
	}
	
	
	
	
	
		// Encoding / Decoding
	
	public static int ByteToInt(byte ByteToCode){
		int IntShot = (int) ByteToCode;
		if( IntShot >= 0 )
			return IntShot;
		else
			return 256+IntShot;
	}
	
	public static byte IntToByte(int IntToCode){
		return (byte)IntToCode;
	}
		
	public static String DecToHex(int DecToCode){
		return Integer.toHexString(DecToCode);
	}
	
	public static int HexToDec(String HexToCode){
		return Integer.valueOf(HexToCode, 16);
	}
	
	
}
