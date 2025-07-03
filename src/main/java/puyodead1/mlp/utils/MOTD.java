package puyodead1.mlp.utils;


public enum MOTD {
    DEFAULT("default", null),
    COMMUNITY("community", null),
    CREATIVE("creative", null),
    BIGOTRY("bigotry", null),
    FURRY("furry", null),
    LGBT("lgbt", null);

    String name;
    Boolean search;
    MOTD(String name, Boolean search){
        this.name = name;
        this.search = search;
    }

    public void setSearch(Boolean search) {
        this.search = search;
    }

    public Boolean shouldSearch(){
        return this.search;
    }

    public String getName() {
        return name;
    }
}
