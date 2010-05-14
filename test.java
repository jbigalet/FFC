import Jama.Matrix;

public class test {
	public static void main(String[] args) {
		
		int k = 6;
		int n = 20;
		
		double[][] yT = new double[][] {{2},{8},{24},{45},{70},{80},{85},{80},{70},{45},{24},{8},{2},{4},{8},{14},{30},{32},{28},{14}};
		Matrix y = new Matrix(yT);
		
		double[][] XT = new double[n][k+1];
		
		for(int iN = 1 ; iN <= n ; iN++)
			for(int iK = 0 ; iK <= k ; iK++)
				XT[iN-1][iK] = Math.pow(iN, iK);
		
		Matrix X = new Matrix(XT);
		
		Matrix a = X.solve(y);

		a.print(3, 3);
		
		System.out.println("R² = " + R2(a.getArray(), yT));
	}
	
	
	public static double R2(double[][] a, double[][] y){
		double n = y.length;
		double Ay = 0;
		for(int i=0 ; i<n ; i++)
			Ay = Ay + y[i][0];
		Ay = Ay / n;

		double F = 0;
		for(int i=0 ; i<n ; i++)
			F += Math.pow(getSupposedValue((i+1), a) - Ay, 2);
		
		double d = 0;
		for(int i=0 ; i<n ; i++)
			d += Math.pow(y[i][0] - Ay, 2);
		
		return F/d;		
	}
	
	public static double getSupposedValue(double x, double[][] a){
		double y = 0;
		for(int i=0 ; i<a.length ; i++)
			y += a[i][0]*Math.pow(x, i);
		
		return y;
	}
	
	public static double getSupposedValue2(double x, double[][] a){
		double y = 0;
		for(int i=a.length-1 ; i>=0 ; i--)
			y = x*y + a[i][0];
		
		return y;			               
	}
}
