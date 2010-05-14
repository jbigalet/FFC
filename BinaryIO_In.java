import java.util.List;

public class BinaryIO_In {
	
	public int[] IntArray;
	public int TotalNumberOfBits;
	private int iCurrentInt;
	private int[] CurrentIntArray;
	private int iNextBitToRead;
	public int NumberOfViewedBits;
	
	public BinaryIO_In( int[] data, int NumberOfBits ){
		TotalNumberOfBits = NumberOfBits;
		IntArray = data.clone();
		iCurrentInt = -1;
		iNextBitToRead = 8;
		CurrentIntArray = new int[8];
		NumberOfViewedBits = 0;
	}
	
	public int ReadBit(){
		if(NumberOfViewedBits >= TotalNumberOfBits){
			System.out.println("No other bit to read.");
			return -1;
		}
		
		NumberOfViewedBits++;
		
		if(iNextBitToRead == 8){	
			CurrentIntArray = new int[8];
			iNextBitToRead = 0;
			iCurrentInt++;
			int Int = IntArray[iCurrentInt];
			int pos = 7;
			while( Int != 0 ){
				CurrentIntArray[pos] = Int % 2;				// Decompressing of the Byte in an array of Bits.
				pos--;
				Int /= 2;
			}
		}
		
		iNextBitToRead++;

		return CurrentIntArray[iNextBitToRead-1];
	}
	
	public int ReadInt(int nBitsToRead){
		
		int FinalInt = 0;
		
		for(int i=0 ; i<nBitsToRead ; i++)
			FinalInt = 2*FinalInt + ReadBit();
		
		return FinalInt;
	}
	
	public long ReadLong(int nBitsToRead){
		
		long FinalLong = 0;
		
		for(int i=0 ; i<nBitsToRead ; i++)
			FinalLong = 2*FinalLong + ReadBit();
		
		return FinalLong;
	}
	
	public double ReadDouble(){
		
			// 1 bit -> The sign
		double sign = 2*ReadBit() - 1;
		
			// 7 bits -> The exponent (1 bit for the sign, and 6 bits for the value)
		double expSign = 2*ReadBit() - 1;
		double exp = expSign * ReadInt( 6 );
		
			// 56 bits -> The value

		double x = ReadLong( 56 );
		
		x *= Math.pow(10, exp - 16);
		
		return x * sign;
	}
	
	public boolean isBitToRead(){
		if(NumberOfViewedBits >= TotalNumberOfBits)
			return false;
		else
			return true;
	}
	
}
