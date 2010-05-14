import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import Jama.Matrix;

public class WavFile_Input extends EncodeDecode {

	public String FileLocation;
	public String Format;
	public long FileSize;
	public int NumberOfChannels;
	public int SampleRate;
	public int BitsPerSample;
	private long BlockSize;
	private long ByteRate;
	public int BytePerBlock;
	public int DataSize;
	private int[] HeaderArray;
	public int[] DataArray;
	public int Duration;
	public boolean isDataDecompressed;
	public int[][] RealData;
	private int NumberOfSamples;
	
	public WavFile_Input ( String FileLocation ) throws Exception {
		this.FileLocation = FileLocation;
		File file = new File(FileLocation);
		int length = (int)file.length();
		HeaderArray = new int[44];
		DataArray = new int[length-44];	
		
		FileInputStream FileStream = new FileInputStream(FileLocation);
		BufferedInputStream FileBuffered = new BufferedInputStream(FileStream, 1);
		byte data[] = new byte[length];

		FileBuffered.read(data, 0, length);
		
		FileBuffered.close();

		int dataMistake = 0;
		for( int i=0; i<44; i++){
			if(i==36 && (char)ByteToInt(data[i])!='d')
				dataMistake = 2;
			HeaderArray[i] = ByteToInt(data[i+dataMistake]);			
		}
		
		for( int i=44; i<length-dataMistake; i++)
			DataArray[i-44] = ByteToInt(data[i+dataMistake]);	
		
		
		FileSize = HeaderArray[4] + HeaderArray[5]*256 + HeaderArray[6]*256*256 + HeaderArray[7]*256*256*256;
		Format = ""; for( int i=8 ; i<12 ; i++ ) Format += (char)HeaderArray[i];
		BlockSize = HeaderArray[16] + HeaderArray[17]*256 + HeaderArray[18]*256*256 + HeaderArray[19]*256*256*256;
		NumberOfChannels = HeaderArray[22]+HeaderArray[23]*256;
		SampleRate = HeaderArray[24] + HeaderArray[25]*256 + HeaderArray[26]*256*256 + HeaderArray[27]*256*256*256;
		ByteRate = HeaderArray[28] + HeaderArray[29]*256 + HeaderArray[30]*256*256 + HeaderArray[31]*256*256*256;
		BytePerBlock = HeaderArray[32] + HeaderArray[33]*256;
		BitsPerSample = HeaderArray[34] + HeaderArray[35]*256;
		DataSize = HeaderArray[40] + HeaderArray[41]*256 + HeaderArray[42]*256*256 + HeaderArray[43]*256*256*256;
		Duration = (int)(DataSize/SampleRate/BytePerBlock);
		NumberOfSamples = DataSize/BytePerBlock;

		isDataDecompressed = false;
	}
	
		// Another build type, to convert from WavFile_Output.
	public WavFile_Input( int[] HeaderArray, int[] DataArray, String FuturFileLocation ) throws Exception {
		this.FileLocation = FuturFileLocation;
		this.HeaderArray = HeaderArray.clone();
		this.DataArray = DataArray.clone();
		
		FileSize = HeaderArray[4] + HeaderArray[5]*256 + HeaderArray[6]*256*256 + HeaderArray[7]*256*256*256;
		Format = ""; for( int i=8 ; i<12 ; i++ ) Format += (char)HeaderArray[i];
		BlockSize = HeaderArray[16] + HeaderArray[17]*256 + HeaderArray[18]*256*256 + HeaderArray[19]*256*256*256;
		NumberOfChannels = HeaderArray[22]+HeaderArray[23]*256;
		SampleRate = HeaderArray[24] + HeaderArray[25]*256 + HeaderArray[26]*256*256 + HeaderArray[27]*256*256*256;
		ByteRate = HeaderArray[28] + HeaderArray[29]*256 + HeaderArray[30]*256*256 + HeaderArray[31]*256*256*256;
		BytePerBlock = HeaderArray[32] + HeaderArray[33]*256;
		BitsPerSample = HeaderArray[34] + HeaderArray[35]*256;
		DataSize = HeaderArray[40] + HeaderArray[41]*256 + HeaderArray[42]*256*256 + HeaderArray[43]*256*256*256;
		Duration = (int)(DataSize/SampleRate/BytePerBlock);

		isDataDecompressed = false;
	}

	public void Info(){
		System.out.println("\nInformations on the file " + FileLocation + " :");
		System.out.println("Format -> " + Format);
		System.out.println("File size -> " + IntToReadableString(FileSize) + " bytes");
		System.out.println("Number of channels -> " + NumberOfChannels);
		System.out.println("Sample rate -> " + IntToReadableString(SampleRate) + " hertz");
		System.out.println("Bits per sample -> " + BitsPerSample + " bits");
		System.out.println("Duration -> " + Duration + " seconds.");
	}
	
	public void HeaderReading(){
		System.out.println("\nHeader of the file " + FileLocation + " :");
		
		System.out.print("RIFF word -> ");
		for( int i=0 ; i<4 ; i++ ) System.out.print((char)HeaderArray[i]);
		
		System.out.println("\nFile size -> " + IntToReadableString(HeaderArray[4] + HeaderArray[5]*256 + HeaderArray[6]*256*256 + HeaderArray[7]*256*256*256) + " bytes");
		
		System.out.print("WAVE word -> ");
		for( int i=8 ; i<12 ; i++ ) System.out.print((char)HeaderArray[i]);

		System.out.print("\nFMT word -> ");
		for( int i=12 ; i<16 ; i++ ) System.out.print((char)HeaderArray[i]);

		System.out.println("\nBlock size -> " + (HeaderArray[16] + HeaderArray[17]*256 + HeaderArray[18]*256*256 + HeaderArray[19]*256*256*256) + " bytes.");
		System.out.println("Audio format -> " + (HeaderArray[20]+HeaderArray[21]*256) + " (1 = PCM)" );
		System.out.println("Number of channels -> " + (HeaderArray[22]+HeaderArray[23]*256));
		System.out.println("Sample rate -> " + IntToReadableString(HeaderArray[24] + HeaderArray[25]*256 + HeaderArray[26]*256*256 + HeaderArray[27]*256*256*256) + " hertz");
		System.out.println("Byte rate -> " + IntToReadableString(HeaderArray[28] + HeaderArray[29]*256 + HeaderArray[30]*256*256 + HeaderArray[31]*256*256*256) + " bytes/s");
		System.out.println("Bytes per block -> " + (HeaderArray[32] + HeaderArray[33]*256) + " bytes");
		System.out.println("Bits per sample -> " + (HeaderArray[34] + HeaderArray[35]*256) + " bits");
		
		System.out.print("DATA word -> ");
		for( int i=36 ; i<40; i++ ) System.out.print((char)HeaderArray[i]);
		
		System.out.println("\nDate size -> " + IntToReadableString(HeaderArray[40] + HeaderArray[41]*256 + HeaderArray[42]*256*256 + HeaderArray[43]*256*256*256) + " bytes");
	}

	public void MakeDataDecompression(){
		
		if(isDataDecompressed)
			return;
		
		RealData = new int[NumberOfChannels][DataSize/BytePerBlock];
		int BytePerSample = BitsPerSample / 8;
		
		int iRealData = 0;
		int iData = 0;
		int iChannel=0;
		
		while( iData < DataSize){
			int DecompressedData = 0;
			int Power = 1;
			for(int j=0 ; j<BytePerSample ; j++){
				DecompressedData += Power * DataArray[iData];
				Power *= 256;
				iData++;
			}

			RealData[iChannel][iRealData] = DecompressedData;
			if(iChannel != NumberOfChannels - 1)
				iChannel = iChannel + 1;
			else{
				iChannel = 0;
				iRealData++;
			}
		}
		
		isDataDecompressed = true;
		
		System.out.println("Data array of " + FileLocation + " successfully compressed.");
	}
	
	public void FFCCompression( String FFCFuturFileLocation, int ConversionType, int PredictorType, int EncodingType, int[] EncryptionParameters ) throws Exception {
			// Works only for standart wav file with 2 channels.
		
		MakeDataDecompression();
		BinaryIO_Out BinO = new BinaryIO_Out();
		
					// 1°) Channels conversion
		
		int[][] ConvertedData = null;
		if( ConversionType == 1 )
			 ConvertedData = Conversion_LeftSide();
		
		else{
			System.out.println("! Error in compression - Conversion type " + ConversionType + " does not exist.");
			return;
		}

					// 2°) Predictor application
		
		int[][] ErrorOfPredictor = null;
		if( PredictorType == 1 )
			ErrorOfPredictor = Predictor_FirstTest( ConvertedData );
		else if( PredictorType == 2 )
			ErrorOfPredictor = Predictor_LeastSquaresFitting(ConvertedData, BinO, EncryptionParameters[1], EncryptionParameters[2]);
		
		else{
			System.out.println("! Error in predictor - Predictor type " + PredictorType + " does not exist.");
			return;
		}
		
					// 3)° Binary coding
		
		if( EncodingType == 1 )
			Encoding_RiceCoding_FixedK( BinO, ErrorOfPredictor, EncryptionParameters[0] );
		else if( EncodingType == 2 )
			Encoding_RiceCoding_AutoAdaptiveK( BinO, ErrorOfPredictor );
		else if( EncodingType == 3 )
			Encoding_RiceCoding_AutoAdaptiveKByStep( BinO, ErrorOfPredictor, EncryptionParameters[0] );
		
		else{
			System.out.println("! Error in encoding - Encoding type " + EncodingType + " does not exist.");
			return;
		}
		
		BinO.MakeFFCHeader(NumberOfChannels, SampleRate, BitsPerSample, DataSize/BytePerBlock, ConversionType, PredictorType, EncodingType, EncryptionParameters);
		BinO.Export(FFCFuturFileLocation);
	}
	
	
	
	
	
	private int[][] Conversion_LeftSide(){
			// Conversion - From Left / Right channels to Left / Side (=Right-Left)
		
		int[][] LeftSideData = new int[2][NumberOfSamples];
		
		for(int i=0 ; i<NumberOfSamples ; i++){
			LeftSideData[0][i] = RealData[0][i];
			LeftSideData[1][i] = RealData[1][i] - RealData[0][i];
		}
		
		return LeftSideData;
	}
		
	
	
	
	private int[][] Predictor_FirstTest( int[][] ConvertedData ){
			// Predictor - Lets use for both 'channels' the predictor : U(n) = 3*U(n-1) - 3*U(n-2) + U(n-3) 
		
		int[][] ErrorOfPredictor = new int[2][NumberOfSamples];
		
		for(int i=0 ; i<3 ; i++)
			for(int j=0 ; j<2 ; j++)
				ErrorOfPredictor[j][i] = ConvertedData[j][i];
		
		for(int i=3 ; i<NumberOfSamples ; i++)
			for(int j=0 ; j<2 ; j++)
				ErrorOfPredictor[j][i] = ConvertedData[j][i]
									  - (3*ConvertedData[j][i-1] - 3*ConvertedData[j][i-2] + ConvertedData[j][i-3]);
		
		return ErrorOfPredictor;
	}
	
	private int[][] Predictor_LeastSquaresFitting( int[][] ConvertedData, BinaryIO_Out BinO, int StepLength, int PolynomialOrder ){
		int[][] ErrorOfPredictor = new int[NumberOfChannels][NumberOfSamples];
		
		int n = 0;			// Number of datas stocked in y_array
		int k = PolynomialOrder;
		int iSampleToAdd;
		double[] CurrentPolynomial;
		double ValueToAdd;
		List<Double> PolyList = new ArrayList<Double>();
		double[] y_array = new double[StepLength]; 
		
		for(int iChannel=0 ; iChannel<NumberOfChannels ; iChannel++){
			for(int iSample=0 ; iSample<NumberOfSamples ; iSample++){
				y_array[n] = ConvertedData[iChannel][iSample];
				n++;
				if(n == StepLength || iSample == NumberOfSamples-1 ){		// The step is ended, its time to calculate the polynomial.
					CurrentPolynomial = CalculatePolynomial(y_array, n, k);
											// Then we add the predicted values to the array.
					for(int i=0 ; i<n ; i++){
						iSampleToAdd = iSample-StepLength+1+i;
						ValueToAdd = getValueUsingPolynomial(i+1, CurrentPolynomial);
						ErrorOfPredictor[iChannel][iSampleToAdd] = (int) (ValueToAdd + Math.signum(ValueToAdd)*0.5)
																  - ConvertedData[iChannel][iSampleToAdd];
					}
					
											// And we finally add the poly to the list, to be added later on the beginning of the header.
					for(int i=0 ; i<CurrentPolynomial.length ; i++)
						PolyList.add(CurrentPolynomial[i]);
					
					n = 0;
					y_array = new double[StepLength];
				}
			}			
		}
		
		for(double i:PolyList)
			BinO.addDouble(i);
		
		System.out.println("Size taken by polynamial -> " + IntToReadableString(PolyList.size() * 8) + " bytes.");
		
		return ErrorOfPredictor;
	}
	
	
	private double[] CalculatePolynomial( double[] y_array, int n, int k){
			// n is the number of values in y_array to pick, k the final order of the polynomial
		
		double[][] yT = new double[n][1];
		for(int i=0 ; i<n ; i++)
			yT[i][0] = y_array[i];				// Create an array to be the futur y-vector form
		
		Matrix y = new Matrix(yT);
		
		double[][] XT = new double[n][k+1];
		
		for(int iN = 1 ; iN <= n ; iN++)
			for(int iK = 0 ; iK <= k ; iK++)
				XT[iN-1][iK] = Math.pow(iN, iK);
		
		Matrix X = new Matrix(XT);
		
		Matrix a = X.solve(y);

		return a.transpose().getArray()[0];		// 1st, we transpose, so that the matrix can be horizontal.
												// Then we put it in array form, and we just
													// pick up the first (and only) line.
	}
	
	public static double getValueUsingPolynomial(double x, double[] Polynomial){
		double y = 0;
		for(int i=Polynomial.length-1 ; i>=0 ; i--)
			y = x*y + Polynomial[i];
		
		return y;			               
	}
	
	
	
	public void Encoding_RiceCoding_FixedK( BinaryIO_Out BinO, int[][] ErrorOfPredictor, int k ) throws Exception {	

			// Encoding - Rice Coding, using parameter k
		
		int M = (int) Math.pow(2, k);
		for(int i=0 ; i<NumberOfSamples ; i++)
			for(int j=0 ; j<2 ; j++){
				int data = ErrorOfPredictor[j][i];
				BinO.addBit(data >= 0 ? 1 : 0);	// 1) sign (1 for positive, 0 for negative)
				data = Math.abs(data);
				
				int quo = data / M;
				int rem = data % M;
				
				for(int n=0 ; n<quo ; n++)
					BinO.addBit(0);				// 2) n / (2^k) 0's
				BinO.addBit(1);					// 3) terminating 1
	
				int[] remInBit = new int[k];
				int pos = 0;
				while( rem != 0 ){
					remInBit[pos] = rem % 2;
					pos++;
					rem /= 2;						// 4) k least significant bits of n
				}			
				for(pos = k-1 ; pos >= 0 ; pos--)
					BinO.addBit(remInBit[pos]);
			}
	}
	
	public void Encoding_RiceCoding_AutoAdaptiveK( BinaryIO_Out BinO, int[][] ErrorOfPredictor ) throws Exception {	

			// Encoding - Rice Coding, using no parameter.
			// k is adapted when we advance on the function.
		
		int k = 16; 
		int M = (int) Math.pow(2, k);
		double Average = 0;					// Represents the average of the numbers we saw.
	    double N = 0;						// Represents the number of int we saw.
	    double ln2 = Math.log(2);			// Avoid us to calculate log(2) everytime.
	    
		for(int i=0 ; i<NumberOfSamples ; i++)
			for(int j=0 ; j<2 ; j++){
				int data = ErrorOfPredictor[j][i];
				BinO.addBit(data >= 0 ? 1 : 0);	// 1) sign (1 for positive, 0 for negative)
				data = Math.abs(data);
				
				int quo = data / M;
				int rem = data % M;
				
				for(int n=0 ; n<quo ; n++)
					BinO.addBit(0);				// 2) n / (2^k) 0's
				BinO.addBit(1);					// 3) terminating 1
	
				int[] remInBit = new int[k];
				int pos = 0;
				while( rem != 0 ){
					remInBit[pos] = rem % 2;
					pos++;
					rem /= 2;						// 4) k least significant bits of n
				}			
				for(pos = k-1 ; pos >= 0 ; pos--)
					BinO.addBit(remInBit[pos]);
				
					// And now we calculate the new k
				N++;
				Average = Average*(N-1)/N + data/N;			// That avoids us to have to keep the sum of every numbers.
				k = (int) (Math.log( Average ) / ln2 );
				if(k < 1) k = 1;
				M = (int) Math.pow(2, k);
			}
	}

	public void Encoding_RiceCoding_AutoAdaptiveKByStep( BinaryIO_Out BinO, int[][] ErrorOfPredictor, int StepLength ) throws Exception {	
	
			// Encoding - Rice Coding, using no parameter.
			// k is adapted when we advance on the function.
			// The difference is that k is adapted step by step.
			// After *StepLength* sample for each channels (Sample of channel 1 & Sample of channel 2 = 2 samples),
			//		a new k is build, and saved in the new file.
		
			// First, we create an array containing every value of k that will be used.
		int[] VariantK = new int[NumberOfChannels*NumberOfSamples/StepLength +2];		// +1 in case we get an exact division, we'll try to pick the last number later on a function. In case we dont get an exact division we have to add the +1 in ever case, because the (int) bounding will trunc our number, so we'll be of 1 short.
		int k;
		int iK = 0;							// Position in the VariantK array.
		double Average = 0;					// Represents the average of the numbers we saw.
	    double N = 0;							// Represents the number of int we saw.
	    double ln2 = Math.log(2);			// Avoid us to calculate log(2) everytime.
	    
		for(int i=0 ; i<NumberOfSamples ; i++)
			for(int j=0 ; j<2 ; j++){
				N++;
				Average = Average*(N-1)/N + Math.abs(ErrorOfPredictor[j][i]/N);
				if( N == StepLength ){
					N = 0;
					k = (int) (Math.log( Average ) / ln2 - 0.53);
					if(k < 1) k = 1;
					if(Average == 0) k = 0;				// In case k=0, every Sample in this step are 0.
					VariantK[iK] = k;
					Average = 0;
					iK++;
				}
			}
		
		iK = 0;
		N = 0;
		k = VariantK[iK];
		int previous_k = -1;						// if previous_k == k, then we'll just put a '1' bit instead of k. If not, we'll put a '0' bit & the 6 bits of k.
		
		int temp_k = k;								//
		int[] KInBit = new int[6];					//
		int posK = 0;								//
		while( temp_k != 0 ){						//
			KInBit[posK] = temp_k % 2;				//
			posK++;									//  To add K in the bit file.
			temp_k /= 2;							//
		}											//
		for(posK = 5 ; posK >= 0 ; posK--)			//
			BinO.addBit(KInBit[posK]);				//
		
		int M = (int) Math.pow(2, k);

		for(int i=0 ; i<NumberOfSamples ; i++)
			for(int j=0 ; j<2 ; j++){
				if(k!=0){							// In case k=0, every Sample in this step are 0.
					int data = ErrorOfPredictor[j][i];
					BinO.addBit(data >= 0 ? 1 : 0);	// 1) sign (1 for positive, 0 for negative)
					data = Math.abs(data);
					
					int quo = data / M;
					int rem = data % M;
					
					for(int n=0 ; n<quo ; n++)
						BinO.addBit(0);				// 2) n / (2^k) 0's
					BinO.addBit(1);					// 3) terminating 1
		
					int[] remInBit = new int[k];
					int pos = 0;
					while( rem != 0 ){
						remInBit[pos] = rem % 2;
						pos++;
						rem /= 2;						// 4) k least significant bits of n
					}			
					for(pos = k-1 ; pos >= 0 ; pos--)
						BinO.addBit(remInBit[pos]);
				}
					// And now we pick the new k if we have to
				N++;
				if( N == StepLength ){
					N = 0;
					iK++;
					previous_k = k;
					k = VariantK[iK];
					M = (int) Math.pow(2, k);
					
					if(previous_k == k)			// if previous_k == k, then we'll just put a '1' bit instead of k. If not, we'll put a '0' bit & the 6 bits of k.
						BinO.addBit(1);
					else{
						BinO.addBit(0);
						temp_k = k;									//
						KInBit = new int[6];						//
						posK = 0;									//
						while( temp_k != 0 ){						//
							KInBit[posK] = temp_k % 2;				//
							posK++;									//  To add K in the bit file.
							temp_k /= 2;							//
						}											//
						for(posK = 5 ; posK >= 0 ; posK--)			//
							BinO.addBit(KInBit[posK]);				//
					}
				}

			}
	}
}

