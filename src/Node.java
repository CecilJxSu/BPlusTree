public class Node<K extends Comparable<K>, D> {

    public int keyLength;

    public K[] keys;

    public boolean isLeaf;

    public int childLength;

    public Node<K, D>[] children;

    public D[] dataList;

    public Node<K, D> nextNode;

    public Node<K, D> parent;
}
