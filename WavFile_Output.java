import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

public class WavFile_Output extends EncodeDecode {

	private boolean haveAnHeader;
	private int[] DataArray;
	private int[] HeaderArray;
	
	public WavFile_Output ( int[] Data ) {
		haveAnHeader = false;
		
		DataArray = new int[Data.length];
		for(int i=0 ; i<Data.length ; i++)
			DataArray[i] = Data[i];
		
		HeaderArray = null;
	}

		//Harmonic creation
	public WavFile_Output (int SoundRate, double Duration, int NumberOfChannels, int BitsPerSample, int SampleRate) throws Exception{
		DataArray = new int[(int)((Duration*SampleRate*BitsPerSample*NumberOfChannels)/8)+50];
		
		boolean flag = true;
		int flagCounter = 0;
		int flagLimit = (SampleRate * NumberOfChannels * BitsPerSample)/8 / SoundRate /2 ;
		
		for(int i=0 ; i<DataArray.length ; i++){
			if(flag)
				DataArray[i] = 230;
			else
				DataArray[i] = 10;
			
			flagCounter++;
			if(flagCounter == flagLimit){
				flagCounter = 0;
				flag = !flag;
			}
		}
		
		MakeWavHeader(NumberOfChannels, SampleRate, BitsPerSample);
	}
	
	public void Export( String OutputLocation ) throws Exception {
		
		if( !haveAnHeader ){
			System.out.println("¤¤¤ Impossible to export the file " + OutputLocation + " without a valid header ¤¤¤");
			return;
		}
		
		FileOutputStream FileStream = new FileOutputStream( OutputLocation );
		BufferedOutputStream Buffer = new BufferedOutputStream( FileStream, 1 );
		
		byte data[] = new byte[HeaderArray.length + DataArray.length];
		for( int i=0 ; i<HeaderArray.length ; i++ )
			data[i] = IntToByte( HeaderArray[i] );
		for( int i=0 ; i<DataArray.length ; i++ )
			data[i+HeaderArray.length] = IntToByte( DataArray[i] );
		
		Buffer.write(data);
		Buffer.close();	
		
	}
	
	public void MakeWavHeader( int NumberOfChannels, int SampleRate, int BitsPerSample) throws Exception {
		haveAnHeader = true;
		
		HeaderArray = new int[44];
		
		String HeaderFile = "C:\\WAVHeader.wav";
		int[] FirstHeaderArray = FileToIntArray(HeaderFile);
		for(int i=0 ; i<44 ; i++)
			HeaderArray[i] = FirstHeaderArray[i];
	
		long FileSize = DataArray.length + HeaderArray.length;
		int BytePerBlock = (NumberOfChannels * BitsPerSample) / 8;
		long ByteRate = SampleRate * BytePerBlock;
		long DataSize = DataArray.length;
		
		for(int i=0 ; i<4 ; i++){
			HeaderArray[4+i] = (int)(FileSize%256);
			FileSize /= 256;
		}

		for(int i=0 ; i<2 ; i++){
			HeaderArray[22+i] = (int)(NumberOfChannels%256);
			NumberOfChannels /= 256;
		}		
		
		for(int i=0 ; i<4 ; i++){
			HeaderArray[24+i] = (int)(SampleRate%256);
			SampleRate /= 256;
		}
		
		for(int i=0 ; i<2 ; i++){
			HeaderArray[34+i] = (int)(BitsPerSample%256);
			BitsPerSample /= 256;
		}				
		
		for(int i=0 ; i<2 ; i++){
			HeaderArray[32+i] = (int)(BytePerBlock%256);
			BytePerBlock /= 256;
		}	
		
		for(int i=0 ; i<4 ; i++){
			HeaderArray[28+i] = (int)(ByteRate%256);
			ByteRate /= 256;
		}	

		for(int i=0 ; i<4 ; i++){
			HeaderArray[40+i] = (int)(DataSize%256);
			DataSize /= 256;
		}
	}

	public boolean HaveAnHeader(){
		return this.haveAnHeader;
	}

	public WavFile_Input toAudioFile(String FuturFileLocation, boolean willExport) throws Exception {
		if( !haveAnHeader ){
			System.out.println("¤¤¤ Impossible to convert the OutputAudioFile to an AudioFile without a valid header ¤¤¤");
			return null;
		}
		
		if(willExport) Export( FuturFileLocation );
		
		return new WavFile_Input(HeaderArray, DataArray, FuturFileLocation);		
	}
	
}
