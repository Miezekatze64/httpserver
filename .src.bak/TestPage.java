import com.mieze.html.HTMLBuilder;
import com.mieze.html.HTMLBuilder.*;

public class TestPage {
    public static void main(String[] args) {
        echo(new HTMLBuilder().toString());
    }

    private static void echo(String s) {
        System.out.print(s);
    }
}
