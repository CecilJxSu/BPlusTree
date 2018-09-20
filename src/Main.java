import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class Main {

    public static void main(String[] args) {
        BPlusTree<Integer, Element> bTree = new BPlusTree<>(4, Integer.class, Element.class);

        Set<Integer> dataSet = getDataSet(1000000, 10000000);
        dataSet.forEach(e -> {
            Element elem = new Element(e, e + "");
            bTree.insert(elem.getKey(), elem);
        });

        System.out.println("Tree's height: " + bTree.height);

        // too large output
        // bTree.prettyPrint();

        List<Integer> notFound = new ArrayList<>();
        dataSet.forEach(i -> {
            Element e = bTree.search(i);
            if (e == null) {
                notFound.add(i);
            }
        });

        System.out.println("Not found number: " + notFound.size());
    }

    static Set<Integer> getDataSet(int num, int maxElement) {
        Set<Integer> dataSet = new HashSet<>();
        int e;
        for (int i = 0; i < num; i++) {
            do {
                e = ThreadLocalRandom.current().nextInt(0, maxElement);
            } while (!dataSet.add(e));
        }
        return dataSet;
    }
}
