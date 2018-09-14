import java.lang.reflect.Array;

public class BPlusTree<K extends Comparable<K>, D> {

    private int degree;

    public int height;

    private Node<K, D> root;

    private Class<K> kClass;

    private Class<D> dClass;

    public BPlusTree(int degree, Class<K> kClass, Class<D> dClass) {
        if (degree < 3) {
            throw new BPlusException("degree must greater than 2");
        }

        this.kClass = kClass;
        this.dClass = dClass;
        this.degree = degree;

        this.root = initLeaf(degree);

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
            leafNode = root.children[pos];
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
        // get pos
        int pos = getLocation(leaf, key);
        // create child
        Node<K, D> child = initLeaf(degree);
        child.nextNode = leaf.nextNode;
        leaf.nextNode = child;
        // insert into first leaf node
        if (pos < leaf.keyLength) {
            int i;
            for (i = leaf.keyLength - 1; i >= pos; i--) {
                child.keys[i - pos] =  leaf.keys[i];
                child.dataList[i - pos] = leaf.dataList[i];
                child.keyLength++;
            }
            leaf.keys[pos] = key;
            leaf.dataList[pos] = data;
            leaf.keyLength = pos + 1;
        }
        // insert into next leaf node
        else if (pos == leaf.keyLength) {
            child.keys[0] = key;
            child.dataList[0] = data;
            child.keyLength = 1;

            key = leaf.keys[leaf.keyLength - 1];
        }

        // split
        splitNode(leaf, child, key);
    }

    private Node<K, D> insertRootNode(K key, Node<K, D> lChild, Node<K, D> rChild) {
        Node<K, D> root = initNonLeaf(degree);
        root.keys[0] = key;
        root.keyLength = 1;
        root.children[0] = lChild;
        root.children[1] = rChild;
        root.childLength = 2;
        height++;
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
    }

    private void insertFullNode(Node<K, D> node, K key, Node<K, D> child) {
        // get pos
        int pos = getLocation(node, key);
        // create brand new child
        Node<K, D> newChild = initNonLeaf(degree);
        newChild.nextNode = node.nextNode;
        node.nextNode = newChild;

        // insert into first node node
        if (pos < node.keyLength) {
            int i;
            for (i = node.keyLength - 1; i >= pos; i--) {
                newChild.keys[i - pos] =  node.keys[i];
                newChild.children[i - pos + 1] = node.children[i + 1];
            }
            node.keys[i] = key;
            node.keyLength = pos + 1;
            node.children[i + 1] = child;
            node.childLength = pos + 2;
            newChild.keyLength = i - pos + 1;
            newChild.childLength = i - pos + 2;
        }
        // insert into next node node
        else if (pos == node.keyLength) {
            newChild.keys[0] = key;
            newChild.keyLength = 1;

            newChild.children[0] = node.children[node.childLength - 1];
            newChild.children[1] = child;
            newChild.childLength = 2;

            key = node.keys[node.keyLength - 1];
            node.keyLength -= 1;
            node.childLength -= 1;
        }

        // split
        splitNode(node, newChild, key);
    }

    private void splitNode(Node<K, D> node, Node<K, D> child, K key) {
        // split
        if (node.parent == null) {
            root = insertRootNode(key, node, child);
            node.parent = root;
        } else if (node.parent.keyLength < degree - 1) {
            insertNotFullNode(node.parent, key, child);
        } else {
            insertFullNode(node.parent, key, child);
        }

        child.parent = node.parent;
    }

    private int getLocation(Node<K, D> node, K key) {
        int pos = 0;
        while (pos < node.keyLength && key.compareTo(node.keys[pos]) > 0) {
            pos++;
        }
        return pos;
    }

    private Node<K, D> initLeaf(int degree) {
        Node<K, D> leaf = new Node<K, D>();
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

    public Node<K, D> initNonLeaf(int degree) {
        Node<K, D> nonLeaf = new Node<K, D>();
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
