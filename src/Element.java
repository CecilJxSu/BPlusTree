public class Element {

    private int key;

    private String value;

    public Element() {
    }

    public Element(int key, String value) {
        this.key = key;
        this.value = value;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Element{" +
                "key=" + key +
                ", value='" + value + '\'' +
                '}';
    }
}
