import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Main {

    public static void main(String[] args) {
        BPlusTree<Integer, Element> bTree = new BPlusTree<>(4, Integer.class, Element.class);

        int scale = 1000000;
        Set<Integer> dataSet = getDataSet(scale, scale * 100);
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
            if (e == null || !e.getValue().equals(i + "")) {
                notFound.add(i);
            }
        });

        System.out.println("Searching not found number: " + notFound.size());

        List<Integer> searchingAfterDeleteNotFound = new ArrayList<>();
        // delete
        Object[] dataSetArr = dataSet.toArray();
        for (int i = 0; i < scale / 2; i++) {
            try {
                bTree.delete((Integer) dataSetArr[i]);
                Element e = bTree.search((Integer) dataSetArr[i]);
                if (e == null) {
                    searchingAfterDeleteNotFound.add((Integer) dataSetArr[i]);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println(i);
                System.exit(1);
            }
        }

        List<Integer> searchingAfterDeleteFound = new ArrayList<>();
        for (int i = scale / 2; i < scale; i++) {
            try {
                Element e = bTree.search((Integer) dataSetArr[i]);
                if (e != null) {
                    searchingAfterDeleteFound.add((Integer) dataSetArr[i]);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println(i);
                System.exit(1);
            }
        }
        System.out.println("Searching after delete not found number: " + searchingAfterDeleteNotFound.size());
        System.out.println("Searching after delete found number: " + searchingAfterDeleteFound.size());
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
