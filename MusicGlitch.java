
public class MusicGlitch extends FFC_Compressor {
	
	public static void OnlyKeepOneChannel( WavFile_Input WavFile, int ChannelToKeep, String OneChanneledFileLocation ) throws Exception {
		WavFile.MakeDataDecompression();
		int BytesPerSample = WavFile.BitsPerSample / 8;
		int[] FinalData = new int[WavFile.RealData[ChannelToKeep].length * BytesPerSample];

		int iFinalData = 0;
		for(int iData=0 ; iData < WavFile.RealData[ChannelToKeep].length ; iData++){
			int Data = WavFile.RealData[ChannelToKeep][iData];
			for(int i=0 ; i<BytesPerSample ; i++){
				FinalData[iFinalData] = (int)(Data%256);
				Data /= 256;
				iFinalData++;
			}
		}
		
		WavFile_Output FinalFile = new WavFile_Output(FinalData);
		FinalFile.MakeWavHeader(1, WavFile.SampleRate, WavFile.BitsPerSample);
		FinalFile.Export( OneChanneledFileLocation );
		
		System.out.println("Left channel of " + WavFile.FileLocation + " successfully export in " + OneChanneledFileLocation + ".");
	}
	
	
	
	public static void WavCut( WavFile_Input WavFile, double Duration ) throws Exception {
		int BytesToCatch = (int)(Duration * WavFile.SampleRate * WavFile.NumberOfChannels * WavFile.BitsPerSample / 8);
		int NumberOfFile = WavFile.DataArray.length / BytesToCatch;
		
		int start = 0;
		for(int num = 0 ; num < NumberOfFile ; num++){
			int[] OutData = new int[BytesToCatch];
			for(int i=0 ; i<BytesToCatch ; i++)
				OutData[i] = WavFile.DataArray[i+start];
			start += BytesToCatch;
			
			WavFile_Output OutFile = new WavFile_Output(OutData);
			OutFile.MakeWavHeader(WavFile.NumberOfChannels, WavFile.SampleRate, WavFile.BitsPerSample);
			OutFile.Export(WavFile.FileLocation + "_cut_n_" + (num+1) + ".wav");
		}

		int[] OutData = new int[WavFile.DataArray.length - start];
		if(OutData.length != 0){
			for(int i=0 ; i<OutData.length ; i++)
				OutData[i] = WavFile.DataArray[i+start];
			
			WavFile_Output OutFile = new WavFile_Output(OutData);
			OutFile.MakeWavHeader(WavFile.NumberOfChannels, WavFile.SampleRate, WavFile.BitsPerSample);
			OutFile.Export(WavFile.FileLocation + "_cut_n_" + (NumberOfFile+1) + ".wav");
		}
		
		System.out.println("\nFile " + WavFile.FileLocation + " successfully cut.");
	}
	
	public static void WavBound( String Prefix, String Suffix, int EndNum) throws Exception{
		int FinalDataSize = 0;
		
		WavFile_Input[] Files = new WavFile_Input[EndNum];
		for(int i=0 ; i<EndNum ; i++){
			Files[i] = new WavFile_Input(Prefix + (i+1) + Suffix);
			FinalDataSize += Files[i].DataArray.length;
		}

		int[] FinalData = new int[FinalDataSize];
		int start = 0;
		for(int i=0 ; i<EndNum ; i++){
			for(int j=0 ; j<Files[i].DataArray.length ; j++)
				FinalData[j+start] = Files[i].DataArray[j];
			start = start + Files[i].DataArray.length;
		}
		
		WavFile_Output OutFile = new WavFile_Output(FinalData);
		OutFile.MakeWavHeader(Files[0].NumberOfChannels, Files[0].SampleRate, Files[0].BitsPerSample);
		OutFile.Export(Prefix + "_bound.wav");
		
		System.out.println("\nFile " + Prefix + "_bound.wav successfully bounded.");
	}
	
	
	
	public static void Create_PetitPapaNoel() throws Exception{
		String path = "C:\\PetitPapaNoel\\PetitPapaNoel";
		double tempo = 200; 
		int Do = 262, Re = 294, Mi = 330, Fa = 350, Sol = 392, La = 440, Si = 466;
		double C = .5, N = 1, NP = 1.5, B = 2, BP = 3, R = 4;
		
		int[] freq = new int[] {Do,Fa,Fa,Fa,Sol,Fa,Fa,Sol,La,La,La,Si,La,Sol,Fa,Fa,Fa,Fa,Mi,Re,Do,Do,Fa,Fa,Fa,Fa,Mi,Fa,Sol};
		
		double[] time = new double[] {N,N,N,N,N,BP,C,C,N,N,N,N,BP,N,NP,C,C,C,C,C,BP,C,C,B,C,C,C,C,R};

		Make_Music(freq, time, tempo, 0.10, path);
	}
	
	public static void Create_FrereJacques() throws Exception{
		String path = "C:\\FrereJacques\\FrereJacques_";
		double tempo = 500; 
		int Sol = 196, La = 220, Si = 247, Do = 262, Re = 294, Mi = 330, ReM = 147;
		double DC = .25, C = .5, CP = .75, N = 1, B = 2;
		
		int[] freq = new int[] {Sol,La,Si,Sol,        Sol,La,Si,Sol,
								Si,Do,Re,             Si, Do,Re,
								Re,Mi,Re,Do,Si,Sol,	  Re,Mi,Re,Do,Si,Sol,
								Sol,ReM,Sol,          Sol,ReM,Sol};
		
		double[] time = new double[] {N,N,N,N,         N,N,N,N,
									  N,N,B,           N,N,B,
									  CP,DC,C,C,N,N,   CP,DC,C,C,N,N,
									  N,N,B,           N,N,B};

		Make_Music(freq, time, tempo, 0.10, path);
	}
	
	public static void Create_AuClairDeLaLune() throws Exception{
		String path = "C:\\ACDLL\\ACDLL_";
		double tempo = 120;
		int Sol = 196, La = 220, Si = 247, Do = 264, Re = 297, Mi = 330;
		double C = .5, N = 1, B = 2;
		
		int[] freq = new int[] {Do,Do,Do,Re,Mi,Re,Do,Mi,Re,Re,Do,
								Re,Re,Re,Re,Sol,Sol,Re,Do,Si,La,Sol,
								Do,Do,Do,Re,Mi,Re,Do,Mi,Re,Re,Do};
		
		double[] time = new double[] {C,C,C,C,N,N,C,C,C,C,B};

		Make_Music(freq, time, tempo, 0.10, path);
	}
	
	
	public static void Make_Music( int[] freq, double[] time, double tempo, double silence_time, String path) throws Exception{
		
		for(int i=0 ; i<freq.length ; i++){
			(new WavFile_Output(freq[i], time[i%time.length]*60/tempo, 2, 16, 44100)).Export(path + (2*i+1) + ".wav");
			(new WavFile_Output(80000, silence_time*60/tempo, 2, 16, 44100)).Export(path + (2*i+2) + ".wav");
		}
		
		WavBound(path, ".wav", 2*freq.length);		
		
		System.out.println("\nMusic successfully build in " + path + ".");		
	}

}
