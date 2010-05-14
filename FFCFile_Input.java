import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

public class FFCFile_Input extends EncodeDecode {

	public String FileLocation;
	public String Format;
	public int NumberOfChannels;
	public int SampleRate;
	public int BytePerSample;
	public int NumberOfSamples;  				// Here, represent the Number of Samples (length of RealData[x][] array)
	public int NumberOfDataBits;
	public int ConversionType;
	public int PredictorType;
	public int EncodingType;
	public int[] EncryptionParameters;
	
	private int[] HeaderArray;
	public int[] DataArray;
	
	public boolean isDataDecompressed;
	public int[][] RealData;
	
	public FFCFile_Input ( String FileLocation ) throws Exception {
		
		isDataDecompressed = false;
		this.FileLocation = FileLocation;

		
		File file = new File(FileLocation);
		int FileSize = (int)file.length();
			
		FileInputStream FileStream = new FileInputStream(FileLocation);
		BufferedInputStream FileBuffered = new BufferedInputStream(FileStream, 1);
		byte ByteData[] = new byte[FileSize];

		FileBuffered.read(ByteData, 0, FileSize);
		
		FileBuffered.close();
		
		int data[] = new int[FileSize];
		for(int i=0 ; i<ByteData.length ; i++)
			data[i] = ByteToInt( ByteData[i] );
		

			// Header reading :
			
			// 4 bytes => "FFC_"
		
		Format = "";
		for( int i=0 ; i<4 ; i++ )
			Format += (char) data[i];

			// 1 byte => Number of channels
		
		NumberOfChannels = data[4];
		
			// 3 bytes => Sample rate
		
		SampleRate = data[5] + data[6]*256 + data[7]*256*256;
		
			// 1 byte => Byte per sample

		BytePerSample = data[8];
		
			// 4 bytes => Number of samples.
		
		NumberOfSamples = data[9] + data[10]*256 + data[11]*256*256 + data[12]*256*256*256;
		
			// 4 bytes => Number of data bits
		
		NumberOfDataBits = data[13] + data[14]*256 + data[15]*256*256 + data[16]*256*256*256;
		
			// 3 byte => The 3 encryption types
		
		ConversionType = data[17];
		PredictorType = data[18];
		EncodingType = data[19];
		
			// Following -> The encryption options
		
		List<Integer> temp_EncryptionParameters = new ArrayList<Integer>();
		int i = 20; 	// i <=> PositionInData
		
		while( (char)data[i] != 'd' || (char)data[i+1] != 'a' || (char)data[i+2] != 't' || (char)data[i+3] != 'a' ){
			temp_EncryptionParameters.add( data[i] );
			i++ ;
		}
		
		EncryptionParameters = new int[ temp_EncryptionParameters.size() ];
		
		int PosInParameters = 0;
		for(int L : temp_EncryptionParameters){
			EncryptionParameters[ PosInParameters ] = L;
			PosInParameters++ ;
		}

			// End of the header.
			
			// Now we just copy the header in the header array, as for the data.
		
		int HeaderLength = i + 4;							// data[i+3] = the last 'a' of 'data', so the total length is i+3.
		HeaderArray = new int[HeaderLength - 4];			// We dont keep the 'data' word.
		DataArray = new int[FileSize - HeaderLength];
				
		for( int j=0; j < HeaderArray.length; j++)
			HeaderArray[j] = data[j];			
		
		for( int j=0; j<DataArray.length ; j++)
			DataArray[j] = data[j + HeaderLength];	
	}

	public void Info(){
		System.out.println("\nInformations on the file " + FileLocation + " :");
		System.out.println("Format -> " + Format);
		System.out.println("Number of channels -> " + NumberOfChannels);
		System.out.println("Sample rate -> " + IntToReadableString(SampleRate) + " hertz");
		System.out.println("Bits per sample -> " + 8*BytePerSample + " bits");
		System.out.println("Number of samples -> " + IntToReadableString(NumberOfSamples));
		System.out.println("Number of data bits -> " + IntToReadableString(NumberOfDataBits));
		System.out.println("Conversion type -> " + ConversionType);
		System.out.println("Predictor type -> " + PredictorType);
		System.out.println("Encoding type -> " + EncodingType);
		for(int i=0 ; i<EncryptionParameters.length ; i++)
			System.out.println("Encryption parameter n°" + (i+1) + " -> " + EncryptionParameters[i]);
	}
	
	public void MakeDataDecompression(){
		if(isDataDecompressed)
			return;

		isDataDecompressed = true;

		BinaryIO_In BinI = new BinaryIO_In(DataArray, NumberOfDataBits);
		
		List<Double> DoubleList = new ArrayList<Double>();
		if( PredictorType == 2 ){
			for(int i=0 ; i < (EncryptionParameters[2]+1) * NumberOfChannels * (int)(1 + NumberOfSamples / EncryptionParameters[1]) ; i++)
				DoubleList.add( BinI.ReadDouble() );
		}
		
		
				// 1)° Binary decoding

		int[][] ErrorOfPredictor = null;
		
		if( EncodingType == 1 )							// Rice coding using fixed k
			ErrorOfPredictor = Decoding_RiceCoding_FixedK( BinI, EncryptionParameters[0] );
		else if( EncodingType == 2 )							
			ErrorOfPredictor = Decoding_RiceCoding_AutoAdaptiveK( BinI );
		else if( EncodingType == 3 )							
			ErrorOfPredictor = Decoding_RiceCoding_AutoAdaptiveKByStep( BinI, EncryptionParameters[0] );
		
		else{
			System.out.println("! Error in decoding - Encoding type " + EncodingType + " does not exist.");
			return;
		}
		
				// 2°) Predictor unapplication
		
		int[][] ConvertedData = null;
		
		if( PredictorType == 1 )
			ConvertedData = Unpredictor_FirstTest( ErrorOfPredictor );
		else if( PredictorType == 2 )
			ConvertedData = Unpredictor_LeastSquaresFitting( ErrorOfPredictor, DoubleList, EncryptionParameters[1], EncryptionParameters[2] );
		
		else{
			System.out.println("! Error in un-predictor - Predictor type " + PredictorType + " does not exist.");
			return;
		}


				// 3°) Channel decompression
		
		if( ConversionType == 1 )
			 Deconversion_LeftSide( ConvertedData );
		
		else{
			System.out.println("! Error in deconversion - Conversion type " + ConversionType + " does not exist.");
			return;
		}
		

		System.out.println("FFC file " + FileLocation + " successfully uncompressed.");
	}
	
	public void Deconversion_LeftSide( int[][] ConvertedData ) {

		RealData = new int[NumberOfChannels][NumberOfSamples];
		for(int i=0 ; i<NumberOfSamples ; i++){
			RealData[0][i] = ConvertedData[0][i];
			RealData[1][i] = ConvertedData[0][i] + ConvertedData[1][i];
		} 
		
	}
	
	
	
	public int[][] Unpredictor_FirstTest( int[][] ErrorOfPredictor ){
		
		int[][] ConvertedData = new int[2][NumberOfSamples];
		
		for(int i=0 ; i<3 ; i++)
			for(int j=0 ; j<2 ; j++)
				ConvertedData[j][i] = ErrorOfPredictor[j][i];
		
		for(int i=3 ; i<NumberOfSamples ; i++)
			for(int j=0 ; j<2 ; j++)
				ConvertedData[j][i] = ErrorOfPredictor[j][i] + 3*ConvertedData[j][i-1] - 3*ConvertedData[j][i-2] + ConvertedData[j][i-3];
		
		return ConvertedData;		
	}
	
	public int[][] Unpredictor_LeastSquaresFitting( int[][] ErrorOfPredictor, List<Double> PolyList, int StepLength, int PolynomialOrder ){
		
		int[][] ConvertedData = new int[2][NumberOfSamples];
		
		double[][] PolyArray = new double[1+PolyList.size()/(PolynomialOrder+1)][PolynomialOrder+1];		//'1+' because we'll read the value one more time than necessary
		int iPoly = 0;
		int iCoef = 0;
		for(double d:PolyList){
			PolyArray[iPoly][iCoef] = d;
			iCoef++;
			if( iCoef == PolynomialOrder + 1 ){
				iCoef = 0;
				iPoly++;
			}
		}
		
		int n = 0;			// Number of datas stocked in y_array
		int iCurrentPolynomial = 0;
		double[] CurrentPolynomial = PolyArray[iCurrentPolynomial];
		double ValueToAdd;
		
		for(int iChannel=0 ; iChannel<NumberOfChannels ; iChannel++){
			for(int iSample=0 ; iSample<NumberOfSamples ; iSample++){
				
				ValueToAdd = getValueUsingPolynomial(n+1, CurrentPolynomial);
				
				ConvertedData[iChannel][iSample] = (int) (ValueToAdd + Math.signum(ValueToAdd)*0.5)
				   									    - ErrorOfPredictor[iChannel][iSample];
				n++;
				if(n == StepLength || iSample == NumberOfSamples-1 ){		// The step is ended, its time to calculate the polynomial.
					iCurrentPolynomial++;
					CurrentPolynomial = PolyArray[iCurrentPolynomial];
					n = 0;
				}
			}			
		}
		
		return ConvertedData;
	}
	
	public static double getValueUsingPolynomial(double x, double[] Polynomial){
		double y = 0;
		for(int i=Polynomial.length-1 ; i>=0 ; i--)
			y = x*y + Polynomial[i];
		
		return y;			               
	}
	
	
	
	
	public int[][] Decoding_RiceCoding_FixedK( BinaryIO_In BinI, int k ){

		int[][] ErrorOfPredictor = new int[2][NumberOfSamples];
		
		int M = (int) Math.pow(2, k);
		
		int iRealData = 0;
		int iChannel=0;
		
		while( BinI.isBitToRead() ){
			int sig = 2*BinI.ReadBit()-1;		// 1 gives +1, 0 gives -1
			int numberOf0 = 0;
			while(BinI.ReadBit() == 0)
				numberOf0++;
			int DecompressedData = numberOf0 * M;
			DecompressedData += BinI.ReadInt(k);
			DecompressedData *= sig;
			
			ErrorOfPredictor[iChannel][iRealData] = DecompressedData;
			if(iChannel != NumberOfChannels - 1)
				iChannel = iChannel + 1;
			else{
				iChannel = 0;
				iRealData++;
			}
		}
		
		return ErrorOfPredictor;
	}
	
	public int[][] Decoding_RiceCoding_AutoAdaptiveK( BinaryIO_In BinI ){

		int[][] ErrorOfPredictor = new int[2][NumberOfSamples];
		
		int k = 16;
		int M = (int) Math.pow(2, k);
		double Average = 0;					// Represents the average of the numbers we saw.
	    double N = 0;							// Represents the number of int we saw.
	    double ln2 = Math.log(2);			// Avoid us to calculate log(2) everytime.
	    
		
		int iRealData = 0;
		int iChannel=0;
		
		while( BinI.isBitToRead() ){
			int sig = 2*BinI.ReadBit()-1;		// 1 gives +1, 0 gives -1
			int numberOf0 = 0;
			while(BinI.ReadBit() == 0)
				numberOf0++;
			int DecompressedData = numberOf0 * M;
			DecompressedData += BinI.ReadInt(k);
			
			ErrorOfPredictor[iChannel][iRealData] = DecompressedData * sig;
			if(iChannel != NumberOfChannels - 1)
				iChannel = iChannel + 1;
			else{
				iChannel = 0;
				iRealData++;
			}
			
				// And now we calculate the new k
			N++;
			Average = Average*(N-1)/N + DecompressedData/N;			// The sig of Decom...a is never apply to it.
			k = (int) (Math.log( Average ) / ln2 );
			if( k < 1 ) k = 1;
			M = (int) Math.pow(2, k);
		}
		
		return ErrorOfPredictor;
	}
	
	public int[][] Decoding_RiceCoding_AutoAdaptiveKByStep( BinaryIO_In BinI, int StepLength ){

		int[][] ErrorOfPredictor = new int[2][NumberOfSamples];
		
		int k = BinI.ReadInt(6);
		int M = (int) Math.pow(2, k);
	    int N = 0;							// Represents the number of int we saw.
		
		int iRealData = 0;
		int iChannel=0;
		
		while( BinI.isBitToRead() ){
			if(k!=0){			// In case k=0, every Sample in this step are 0.
				int sig = 2*BinI.ReadBit()-1;		// 1 gives +1, 0 gives -1
				int numberOf0 = 0;
				while(BinI.ReadBit() == 0)
					numberOf0++;
				int DecompressedData = numberOf0 * M;
				DecompressedData += BinI.ReadInt(k);
				
				ErrorOfPredictor[iChannel][iRealData] = DecompressedData * sig;
			}
			else ErrorOfPredictor[iChannel][iRealData] = 0;  
			
			if(iChannel != NumberOfChannels - 1)
				iChannel = iChannel + 1;
			else{
				iChannel = 0;
				iRealData++;
			}
			
				// And now we calculate the new k
			N++;
			if( N == StepLength ){
				N = 0;
				int IsSameK = BinI.ReadBit();		// Decoding of -> if previous_k == k, then we'll just put a '1' bit instead of k. If not, we'll put a '0' bit & the 6 bits of k.
				if(IsSameK == 0)
					k = BinI.ReadInt(6);
				M = (int) Math.pow(2, k);
			}
		}
		
		return ErrorOfPredictor;
	}
	
	
	
	
	public WavFile_Output ConvertToWav() throws Exception {
		if(!isDataDecompressed)
			MakeDataDecompression();
		
		int[] data = new int[NumberOfSamples * BytePerSample * NumberOfChannels];
		
		int iData = 0;
		
		for(int iRealData=0 ; iRealData<NumberOfSamples ; iRealData++)
			for(int iChannel=0 ; iChannel<2 ; iChannel++){

				int ConcatenateData = RealData[iChannel][iRealData];

				for(int i=0 ; i<BytePerSample ; i++){
					data[iData] = ConcatenateData % 256; 
					ConcatenateData /= 256;
					iData++;
				}
			}
				
		WavFile_Output WavFile = new WavFile_Output(data);
		WavFile.MakeWavHeader(NumberOfChannels, SampleRate, BytePerSample*8);
		
		return WavFile;
	}
	
}

