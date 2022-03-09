import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 *
 * @author zz
 * @date 2022/3/7
 */
public class Test {
    public static void main(String[] args) {
        Double aDouble = Double.valueOf("-85.6064453125");
        System.out.println(aDouble);

        ArrayList<Integer> objects = new ArrayList<>();
        for (int i=0;i<5;i++) {
            List <Integer> a =new ArrayList<>();
            a.add(i);
            objects.add(a.get(0));
        }
        System.out.println(objects.toString());
    }

}
