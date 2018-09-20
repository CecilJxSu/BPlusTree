import java.lang.reflect.Array;

public class BPlusTree<K extends Comparable<K>, D> {

    private int degree;

    public int height;

    private Node<K, D> root;

    private Class<K> kClass;

    private Class<D> dClass;

    public BPlusTree(int degree, Class<K> kClass, Class<D> dClass) {
        if (degree < 4) {
            throw new BPlusException("degree must greater or equals to 4");
        }

        this.kClass = kClass;
        this.dClass = dClass;
        this.degree = degree;

        this.root = initLeaf();

        height = 1;
    }

    public void prettyPrint() {
        prettyPrint(root);
    }

    private void prettyPrint(Node<K, D> node) {
        for (int i = 0; i < node.keyLength; i++) {
            if (i == node.keyLength - 1) {
                System.out.print(node.keys[i]);
            } else {
                System.out.print(node.keys[i] + " ");
            }
        }
        Node<K, D> nextNode = node.nextNode;
        while (nextNode != null) {
            System.out.print(" |");
            for (int i = 0; i < nextNode.keyLength; i++) {
                System.out.print(" " + nextNode.keys[i]);
            }
            nextNode = nextNode.nextNode;
        }

        System.out.println();
        if (node.children != null && node.childLength > 0) {
           prettyPrint(node.children[0]);
        }
    }

    public D search(K key) {
        return search(root, key);
    }

    private D search(Node<K, D> node, K key) {
        if (node == null) {
            return null;
        }

        // search in leaf node
        if (node.isLeaf) {
            for (int pos = 0; pos < node.keyLength; pos++) {
                if (key.compareTo(node.keys[pos]) == 0) {
                    return node.dataList[pos];
                }
            }
            // not found
            return null;
        }

        // search in non-leaf node
        int pos = getLocation(node, key);
        return search(node.children[pos], key);
    }

    public void insert(K key, D data) {
        // non-leaf node
        Node<K, D> leafNode = root;
        int pos;
        // 查找适合插入的leafNode
        int h = height;
        while (h-- > 0 && !leafNode.isLeaf) {
            pos = getLocation(leafNode, key);
            leafNode = leafNode.children[pos];
        }

        // insert into leaf node
        if (leafNode.keyLength < degree - 1) {
            insertNotFullLeaf(leafNode, key, data);
        } else {
            insertFullLeaf(leafNode, key, data);
        }
    }

    private void insertNotFullLeaf(Node<K, D> leaf, K key, D data) {
        // get pos
        int pos = getLocation(leaf, key);

        // move back
        int i;
        for (i = leaf.keyLength; i > pos; i--) {
            leaf.keys[i] =  leaf.keys[i - 1];
            leaf.dataList[i] = leaf.dataList[i - 1];
        }
        // insert
        leaf.keys[i] = key;
        leaf.dataList[i] = data;
        leaf.keyLength += 1;
    }

    private void insertFullLeaf(Node<K, D> leaf, K key, D data) {
        // split node
        Node<K, D> splitNode = initLeaf();
        splitNode.nextNode = leaf.nextNode;
        leaf.nextNode = splitNode;

        // split half
        System.arraycopy(leaf.keys, (leaf.keyLength + 1) / 2, splitNode.keys, 0, leaf.keyLength / 2);
        System.arraycopy(leaf.dataList, (leaf.keyLength + 1) / 2, splitNode.dataList, 0, leaf.keyLength / 2);

        splitNode.keyLength = leaf.keyLength / 2;
        leaf.keyLength = (leaf.keyLength + 1) / 2;

        if (key.compareTo(splitNode.keys[0]) < 0) {
            insertNotFullLeaf(leaf, key, data);
        } else {
            insertNotFullLeaf(splitNode, key, data);
        }

        addToParent(leaf, splitNode, leaf.keys[leaf.keyLength - 1]);
    }

    private Node<K, D> insertRootNode(K key, Node<K, D> lChild, Node<K, D> rChild) {
        Node<K, D> root = initNonLeaf();
        root.keys[0] = key;
        root.keyLength = 1;
        root.children[0] = lChild;
        root.children[1] = rChild;
        root.childLength = 2;
        height++;

        lChild.parent = root;
        rChild.parent = root;
        return root;
    }

    private void insertNotFullNode(Node<K, D> node, K key, Node<K, D> child) {
        int pos = getLocation(node, key);
        int i;
        for (i = node.keyLength; i > pos; i--) {
            node.keys[i] =  node.keys[i - 1];
            node.children[i + 1] = node.children[i];
        }
        node.keys[i] = key;
        node.keyLength += 1;
        node.children[i + 1] = child;
        node.childLength += 1;

        child.parent = node;
    }

    private void insertFullNode(Node<K, D> node, K key, Node<K, D> child) {
        // split node
        Node<K, D> splitNode = initNonLeaf();

        //------B* Tree-----//
        splitNode.nextNode = node.nextNode;
        node.nextNode = splitNode;
        //------B* Tree-----//

        // split half
        System.arraycopy(node.keys, (node.keyLength + 1) / 2, splitNode.keys, 0, node.keyLength / 2);
        System.arraycopy(node.children, (node.keyLength + 1) / 2, splitNode.children, 0, node.keyLength / 2 + 1);

        splitNode.keyLength = node.keyLength / 2;
        splitNode.childLength = node.keyLength / 2 + 1;

        node.childLength = (node.keyLength + 1) / 2;
        node.keyLength = (node.keyLength + 1) / 2;

        K extraKey = node.keys[node.keyLength - 1];
        node.keyLength--;

        // reset child's parent
        for (int i = 0 ; i < splitNode.childLength; i++) {
            splitNode.children[i].parent = splitNode;
        }

        if (key.compareTo(extraKey) < 0) {
            insertNotFullNode(node, key, child);
            child.parent = node;
        } else {
            insertNotFullNode(splitNode, key, child);
            child.parent = splitNode;
        }

        addToParent(node, splitNode, extraKey);
    }

    private void addToParent(Node<K, D> node, Node<K, D> child, K key) {
        // split
        if (node.parent == null) {
            root = insertRootNode(key, node, child);
        } else if (node.parent.keyLength < degree - 1) {
            insertNotFullNode(node.parent, key, child);
        } else {
            insertFullNode(node.parent, key, child);
        }
    }

    private int getLocation(Node<K, D> node, K key) {
        int pos = 0;
        while (pos < node.keyLength && key.compareTo(node.keys[pos]) > 0) {
            pos++;
        }
        return pos;
    }

    private Node<K, D> initLeaf() {
        Node<K, D> leaf = new Node<>();
        leaf.keyLength = 0;
        leaf.keys = (K[]) Array.newInstance(kClass, degree - 1);
        leaf.childLength = 0;
        leaf.children = null;
        leaf.parent = null;
        leaf.nextNode = null;
        leaf.dataList = (D[]) Array.newInstance(dClass, degree - 1);
        leaf.isLeaf = true;
        return leaf;
    }

    public Node<K, D> initNonLeaf() {
        Node<K, D> nonLeaf = new Node<>();
        nonLeaf.keyLength = 0;
        nonLeaf.keys = (K[]) Array.newInstance(kClass, degree - 1);
        nonLeaf.childLength = 0;
        nonLeaf.children = new Node[degree];
        nonLeaf.parent = null;
        nonLeaf.nextNode = null;
        nonLeaf.dataList = null;
        nonLeaf.isLeaf = false;
        return nonLeaf;
    }
}
