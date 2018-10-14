import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BPlusTree<K extends Comparable<K>, D> {

    public int height;

    public Node<K, D> leaf;

    private int degree;

    private int maxKeyLength;

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
        this.leaf = this.root;

        height = 1;
        maxKeyLength = degree - 1;
    }

    public void prettyPrint() {
        List<Node<K, D>> nodes = Collections.singletonList(root);
        while (!nodes.isEmpty()) {
            List<Node<K, D>> children = new ArrayList<>();
            for (int nodePos = 0; nodePos < nodes.size(); nodePos++) {
                Node<K, D> node = nodes.get(nodePos);
                for (int i = 0; i < node.keyLength; i++) {
                    if (i == 0 && nodePos != 0) {
                        if (node.parent == nodes.get(nodePos - 1).parent) {
                            System.out.print("  ");
                        } else {
                            System.out.print("  |  ");
                        }
                    }
                    System.out.print(node.keys[i]);
                    if (i != node.keyLength - 1) {
                        System.out.print(",");
                    }
                }

                for (int i = 0; i < node.childLength; i++) {
                    children.add(node.children[i]);
                }
            }

            System.out.println();
            nodes = children;
        }
    }

    public D search(K key) {
        // leaf node
        Node<K, D> leafNode = searchToLeaf(key);

        // search in leaf node
        int pos = getLocation(leafNode, key);
        if (pos < leafNode.keyLength && key.compareTo(leafNode.keys[pos]) == 0) {
            return leafNode.dataList[pos];
        }
        // not found
        return null;
    }

    public Node<K, D> searchToLeaf(K key) {
        // leaf node
        Node<K, D> leafNode = root;
        int pos;
        // 查找适合插入的leafNode
        int h = height;
        while (h-- > 0 && !leafNode.isLeaf) {
            pos = getLocation(leafNode, key);
            leafNode = leafNode.children[pos];
        }
        return leafNode;
    }

    public void insert(K key, D data) {
        // leaf node
        Node<K, D> leafNode = searchToLeaf(key);

        // duplicate key, update data
        int pos = getLocation(leafNode, key);
        if (pos < leafNode.keyLength && key.compareTo(leafNode.keys[pos]) == 0) {
            leafNode.dataList[pos] = data;
            return;
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

        // ------B* Tree-----
        // splitNode.nextNode = node.nextNode;
        // node.nextNode = splitNode;
        // ------B* Tree-----

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

    public D delete(K key) {
        Node<K, D> leafNode = searchToLeaf(key);

        final int pos = getLocation(leafNode, key);
        if (pos < leafNode.keyLength && key.compareTo(leafNode.keys[pos]) == 0) {
            D value = leafNode.dataList[pos];
            // delete key
            for (int i = pos; i < leafNode.keyLength - 1; i++) {
                leafNode.keys[i] = leafNode.keys[i + 1];
                leafNode.dataList[i] = leafNode.dataList[i + 1];
            }
            leafNode.keyLength--;

            // keyLength greater than half of maxLength or leafNode is root node when leafNode's parent is null;
            if (leafNode.keyLength >= maxKeyLength / 2 || leafNode.parent == null) {
                return value;
            }

            int parentPos = getLocation(leafNode.parent, key);

            Node<K, D> lNode;
            Node<K, D> rNode;
            Node<K, D> siblingNode;

            if (parentPos >= leafNode.parent.keyLength) {
                parentPos--;
                siblingNode = leafNode.parent.children[parentPos];
            } else {
                siblingNode = leafNode.parent.children[parentPos + 1];
            }
            lNode = leafNode.parent.children[parentPos];
            rNode = leafNode.parent.children[parentPos + 1];

            if (siblingNode.keyLength <= maxKeyLength / 2 + 1) {
                leafNodeFusing(lNode, rNode, parentPos);
            } else {
                leafNodeSharing(lNode, rNode, parentPos);
            }

            return value;
        }

        return null;
    }

    private void deleteParent(Node<K, D> node, final int pos) {
        K key = node.keys[pos];
        for (int i = pos; i < node.keyLength - 1; i++) {
            node.keys[i] = node.keys[i + 1];
            node.children[i + 1] = node.children[i + 2];
        }
        node.keyLength--;
        node.childLength--;

        if (node.keyLength >= maxKeyLength / 2) {
            return;
        }

        if (node.parent == null) {
            if (node.keyLength == 0) {
                node.children[0].parent = null;
                root = node.children[0];
                deleteNode(node);
            }
            return;
        }

        int parentPos = getLocation(node.parent, key);

        Node<K, D> lNode;
        Node<K, D> rNode;
        Node<K, D> siblingNode;
        if (parentPos >= node.parent.keyLength) {
            parentPos--;
            siblingNode = node.parent.children[parentPos];
        } else {
            siblingNode = node.parent.children[parentPos + 1];
        }
        lNode = node.parent.children[parentPos];
        rNode = node.parent.children[parentPos + 1];

        if (siblingNode.keyLength <= maxKeyLength / 2 + 1) {
            nonLeafNodeFusing(lNode, rNode, parentPos);
        } else {
            nonLeafNodeSharing(lNode, rNode, parentPos);
        }
    }

    private void leafNodeFusing(Node<K, D> lNode, Node<K, D> rNode, int pos) {
        // all of rNode's keys and data transfer to lNode.
        for (int i = 0; i < rNode.keyLength; i++) {
            lNode.keys[lNode.keyLength + i] = rNode.keys[i];
            lNode.dataList[lNode.keyLength + i] = rNode.dataList[i];
        }
        lNode.keyLength += rNode.keyLength;

        lNode.nextNode = rNode.nextNode;

        deleteParent(lNode.parent, pos);

        deleteNode(rNode);
    }

    private void nonLeafNodeFusing(Node<K, D> lNode, Node<K, D> rNode, int pos) {
        lNode.keys[lNode.keyLength] = lNode.parent.keys[pos];
        lNode.keyLength++;
        // all of rNode's keys and data transfer to lNode.
        for (int i = 0; i < rNode.keyLength; i++) {
            lNode.keys[lNode.keyLength + i] = rNode.keys[i];
        }
        lNode.keyLength += rNode.keyLength;

        for (int i = 0; i < rNode.childLength; i++) {
            lNode.children[lNode.childLength + i] = rNode.children[i];
            lNode.children[lNode.childLength + i].parent = lNode;
        }
        lNode.childLength += rNode.childLength;

        // ------B* Tree-----
        // lNode.nextNode = rNode.nextNode;
        // ------B* Tree-----

        deleteParent(lNode.parent, pos);

        deleteNode(rNode);
    }

    private void leafNodeSharing(Node<K, D> lNode, Node<K, D> rNode, int pos) {
        if (lNode.keyLength > rNode.keyLength) {
            // left sharing to right
            int i;
            for (i = rNode.keyLength; i > 0; i--) {
                rNode.keys[i] = rNode.keys[i - 1];
                rNode.dataList[i] = rNode.dataList[i - 1];
            }
            rNode.keys[i] = lNode.keys[lNode.keyLength - 1];
            rNode.dataList[i] = lNode.dataList[lNode.keyLength - 1];
            rNode.keyLength++;
            lNode.keyLength--;
        } else {
            // right sharing to left
            lNode.keys[lNode.keyLength] = rNode.keys[0];
            lNode.dataList[lNode.keyLength] = rNode.dataList[0];
            for (int i = 0; i < rNode.keyLength - 1; i++) {
                rNode.keys[i] = rNode.keys[i + 1];
                rNode.dataList[i] = rNode.dataList[i + 1];

            }
            rNode.keyLength--;
            lNode.keyLength++;
        }

        // update parent's key.
        lNode.parent.keys[pos] = lNode.keys[lNode.keyLength - 1];
    }

    private void nonLeafNodeSharing(Node<K, D> lNode, Node<K, D> rNode, int pos) {
        if (lNode.keyLength > rNode.keyLength) {
            // left sharing to right
            for (int i = rNode.keyLength - 1; i >= 0; i--) {
                rNode.keys[i + 2] = rNode.keys[i];
            }
            for (int i = rNode.childLength - 1; i >= 0; i--) {
                rNode.children[i + 2] = rNode.children[i];
            }
            rNode.keys[1] = lNode.parent.keys[pos];
            rNode.keys[0] = lNode.keys[lNode.keyLength - 1];
            lNode.parent.keys[pos] = lNode.keys[lNode.keyLength - 2];
            rNode.keyLength += 2;
            lNode.keyLength -= 2;

            rNode.children[1] = lNode.children[lNode.childLength - 1];
            rNode.children[0] = lNode.children[lNode.childLength - 2];
            rNode.childLength += 2;
            lNode.childLength -= 2;
            // update parent.
            rNode.children[1].parent = rNode;
            rNode.children[0].parent = rNode;
        } else {
            // right sharing to left
            lNode.keys[lNode.keyLength] = lNode.parent.keys[pos];
            lNode.keys[lNode.keyLength + 1] = rNode.keys[0];
            lNode.parent.keys[pos] = rNode.keys[1];
            lNode.keyLength += 2;

            for (int i = 2; i < rNode.keyLength; i++) {
                rNode.keys[i - 2] = rNode.keys[i];
            }
            rNode.keyLength -= 2;

            lNode.children[lNode.childLength] = rNode.children[0];
            lNode.children[lNode.childLength + 1] = rNode.children[1];
            lNode.children[lNode.childLength].parent = lNode;
            lNode.children[lNode.childLength + 1].parent = lNode;
            lNode.childLength += 2;
            for (int i = 2; i < rNode.childLength; i++) {
                rNode.children[i - 2] = rNode.children[i];
            }
            rNode.childLength -= 2;
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

    private Node<K, D> initNonLeaf() {
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

    private void deleteNode(Node<K, D> node) {
        node.keys = null;
        node.children = null;
        node.dataList = null;
        node.parent = null;
        node.nextNode = null;
    }

    public void printAllKeys() {
        Node<K, D> leaf = this.leaf;
        while (leaf != null) {
            for (int i = 0; i < leaf.keyLength; i++) {
                System.out.print(leaf.keys[i]);
                if (i != leaf.keyLength - 1) {
                    System.out.print(",");
                }
            }
            leaf = leaf.nextNode;
            if (leaf != null) {
                System.out.print(",");
            }
        }
        System.out.println();
    }
}
