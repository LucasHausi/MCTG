package org.mtcg.HTTP;

public class Header {
    private String name;
    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    //DEV functions
    @Override
    public String toString() {
        return "Name: " + name + ", Value: " + value;
    }
}
