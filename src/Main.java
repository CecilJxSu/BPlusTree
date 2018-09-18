public class Main {

    public static void main(String[] args) {
        BPlusTree<Integer, Element> bTree = new BPlusTree<Integer, Element>(3, Integer.class, Element.class);

        Element e1 = new Element(10, "10");
        Element e2 = new Element(20, "20");
        Element e3 = new Element(30, "30");
        Element e4 = new Element(15, "15");

        Element e5 = new Element(25, "25");
        Element e6 = new Element(28, "28");

        Element e7 = new Element(11, "11");
        Element e8 = new Element(13, "13");


        bTree.insert(e1.getKey(), e1);
        bTree.insert(e2.getKey(), e2);
        bTree.insert(e3.getKey(), e3);
        bTree.insert(e4.getKey(), e4);
        bTree.insert(e5.getKey(), e5);
        bTree.insert(e6.getKey(), e6);
        bTree.insert(e7.getKey(), e7);
        bTree.insert(e8.getKey(), e8);

        System.out.println("height: " + bTree.height);
        bTree.prettyPrint();

        System.out.println(bTree.search(30));
    }
}
