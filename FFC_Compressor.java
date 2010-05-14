import java.io.*;
import java.lang.*;

public class FFC_Compressor extends EncodeDecode { // Free flawless compression
	
	public static void main(String[] args) throws Exception {	long time = System.currentTimeMillis();
		String[] Files = new String[] {"ACDLL", "PetitPapaNoel", "test_1", "rammstein"};
		String FileToOpen = "C:\\" + Files[2] + ".wav";
	
		String OutputFile = FileToOpen + "_FFC_Compressed.ffc";
		
		WavFile_Input WavTest = new WavFile_Input(FileToOpen);
		WavTest.Info();
		
		int ConversionType = 1;
		int PredictorType = 1;
		int EncodingType = 3;
		
		int[] Parameters = new int[]
		             {20};
		
		WavTest.FFCCompression( OutputFile, ConversionType, PredictorType, EncodingType, Parameters );
		
		FFCFile_Input FFCTest = new FFCFile_Input( OutputFile );
		FFCTest.Info();
		
		WavFile_Output DecompWav = FFCTest.ConvertToWav();
		DecompWav.Export(FileToOpen + "_WAV_Recompressed.wav");
		
		//TotalCDCompression();

		long TotalTime = System.currentTimeMillis() - time;
		System.out.println("\nEnded in " + TotalTime + " ms.");
		System.out.println("Inital size : " + IntToReadableString(new File(FileToOpen).length()));
		System.out.println("Final size :  " + IntToReadableString(new File(OutputFile).length()));
		System.out.println("\nCOMPRESSION RATIO :\n       " +
					(100*new File(OutputFile).length()/new File(FileToOpen).length()) + "%");
	}


	public static void TotalCDCompression() throws Exception {
		int ConversionType = 1;
		int PredictorType = 1;
		int EncodingType = 3;
		
		int[] Parameters = new int[] {12};
		
		for(int i=1 ; i<=17 ; i++){
			WavFile_Input TrackFile = new WavFile_Input("H:\\Compression Tests\\" + i + ".wav");
			TrackFile.FFCCompression( "H:\\Compression Tests\\" + i + ".ffc", ConversionType, PredictorType, EncodingType, Parameters );			
		}
	}
}
