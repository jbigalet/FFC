import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.*;

public class BinaryIO_Out extends EncodeDecode {
	
	public List<Integer> IntList;
	public int TotalNumberOfBits;
	private int NumberOfBitsInCurrentByte;
	private int CurrentByte;
	private int[] HeaderArray; 
	
	public BinaryIO_Out(){
		IntList = new ArrayList<Integer>();
		NumberOfBitsInCurrentByte = 0;
		CurrentByte = 0;
		TotalNumberOfBits = 0;
		HeaderArray = new int[0];
	}
	
	public void addBit(int bit){
		TotalNumberOfBits++;
		CurrentByte = 2*CurrentByte + bit;
		NumberOfBitsInCurrentByte++;
		if(NumberOfBitsInCurrentByte == 8){
			IntList.add(CurrentByte);
			NumberOfBitsInCurrentByte = 0;
			CurrentByte = 0;
		}
	}

	
	public void addDouble( double x ){
			// 1 bit -> The sign
		addBit( x>=0 ? 1 : 0 );
		
		x = Math.abs(x);
		
			// 7 bits -> The exponent (1 bit for the sign, and 6 bits for the value)
		int exp = 0;
		if( x == 0 )
			addBit( 1 );
		else if( x >= 1 ){			// Positif exponent
			addBit( 1 );
			while( x >= 1 ){
				x /= 10;
				exp++;
			}
		}
		else {
			addBit( 0 );
			while( x < 0.1 ){
				x *= 10;
				exp++;
			}
		}
		
		addInt( exp, 6 );
		
			// 56 bits -> The value

		x = x * Math.pow(10, 16);

		addInt( (long)x , 56 );		
	}
	
	public void addInt( long x, int NumberOfBits ){
	
		int[] BitsToAdd = new int[NumberOfBits];
		int pos = 0;								
		while( x != 0 ){						
			BitsToAdd[pos] = (int)(x % 2);
			pos++;									
			x /= 2;							
		}											
		for(pos = NumberOfBits-1 ; pos >= 0 ; pos--)			
			addBit(BitsToAdd[pos]);
		
	}
	
	
	public void MakeFFCHeader(int NumberOfChannels, int SampleRate, int BitsPerSample, int NumberOfSample,
							  int ConversionType, int PredictorType, int EncodingType, int[] EncryptionParameters
																					) throws Exception {
		
		List<Integer> HeaderList = new ArrayList<Integer>();
		
			//4 bytes => "FFC_"
		
		HeaderList.add( (int) 'F' );
		HeaderList.add( (int) 'F' );
		HeaderList.add( (int) 'C' );
		HeaderList.add( (int) '_' );
		
			// 1 byte => Number of channels
		
		HeaderList.add( NumberOfChannels );
		
			// 3 bytes => Sample rate
		
		for(int i=0 ; i<3 ; i++){
			HeaderList.add( SampleRate % 256 );
			SampleRate /= 256;
		}
		
			// 1 byte => Byte per sample
		
		HeaderList.add( BitsPerSample / 8 );
		
			// 4 bytes => Number of samples.
		
		for(int i=0 ; i<4 ; i++){
			HeaderList.add( NumberOfSample % 256 );
			NumberOfSample /= 256;
		}
		
			// 4 bytes => Number of data bits
		
		int NumberOfData = TotalNumberOfBits;
		for(int i=0 ; i<4 ; i++){
			HeaderList.add( NumberOfData % 256 );
			NumberOfData /= 256;
		}
		
			// 3 byte => The 3 Encryption types
		
		HeaderList.add( ConversionType );
		HeaderList.add( PredictorType );
		HeaderList.add( EncodingType );
		
			// Following -> The encryption options
		
		for(int i=0 ; i<EncryptionParameters.length ; i++)
			HeaderList.add( EncryptionParameters[i] );
		
			// 4 bytes => "data"

		HeaderList.add( (int) 'd' );
		HeaderList.add( (int) 'a' );
		HeaderList.add( (int) 't' );
		HeaderList.add( (int) 'a' );
				
		
			// Create the header array
		HeaderArray = new int[ HeaderList.size() ];
		int positionOnHeaderArray = 0;
		for( int IntToAdd : HeaderList){
			HeaderArray[ positionOnHeaderArray ] = IntToAdd ;
			positionOnHeaderArray ++ ;
		}
	}
	
	public void Export(String OutputLocation) throws Exception {
		IntList.add(CurrentByte*(int)Math.pow(2, 8-NumberOfBitsInCurrentByte));
		
		FileOutputStream FileStream = new FileOutputStream( OutputLocation );
		BufferedOutputStream Buffer = new BufferedOutputStream( FileStream, 1 );
		
		byte data[] = new byte[HeaderArray.length + IntList.size()];
		for( int i=0 ; i<HeaderArray.length ; i++ )
			data[i] = IntToByte( HeaderArray[i] );
		int i=HeaderArray.length;
		for( int L:IntList ){
			data[i] = IntToByte( L );
			i++;			
		}
		
		Buffer.write(data);
		Buffer.close();	
	}
	
}
