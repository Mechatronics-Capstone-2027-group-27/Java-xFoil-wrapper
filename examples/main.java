import matz.xfoil.Xfoil;
import matz.xfoil.XfoilInput;
import matz.xfoil.XfoilOut;
import java.io.IOException;

public class main {
    public static void main(String[] args) {
        try (Xfoil p = new Xfoil("ssh", "-tt", "this", "\"xfoil.exe\"");) {
            XfoilInput input = new XfoilInput();
            XfoilOut ar1 = p.calc(input);
            areoPrint(ar1);
            input.Re = 20000;
            XfoilOut ar2 = p.calc(input);
            areoPrint(ar2);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("error");
        }
    }
    private static void areoPrint(XfoilOut r) {
        System.out.printf(
            "alpha=%.3f, CL=%.4f, Cm=%.4f, CD=%.5f, CDf=%.5f, CDp=%.5f%n",
            r.alpha(), r.cl(), r.cm(), r.cd(), r.cdf(), r.cdp()
        );
    }
}
