import java.io.*;

public class main {
    public static void main(String[] args) throws IOException {
        double x, y;
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        System.out.print("Введите координату x: ");
        x = Double.parseDouble(in.readLine());
        System.out.print("Введите координату y: ");
        y = Double.parseDouble(in.readLine());

        if ((x - 7) * (x - 7) + (y - 7) * (y - 7) < 12.25) {
            if (y > 0.5 * x + 1) {
                if (y > 2 * x - 2) {
                    System.out.println("Область 2");
                } else {
                    System.out.println("Область 3");
                }
            } else {
                System.out.println("Область 5");
            }
        } else {
            if (y > 0.5 * x + 1) {
                if (y > 2 * x - 2) {
                    System.out.println("Область 1");
                } else {
                    if (x < 7 && y < 7) {
                        System.out.println("Область 7");
                    } else {
                        System.out.println("Область 4");
                    }
                }
            } else {

                if (y > 2 * x - 2) {

                    System.out.println("Область 8");
                } else {
                    System.out.println("Область 6");
                }
            }
        }
    }
}
