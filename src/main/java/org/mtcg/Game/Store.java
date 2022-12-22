package org.mtcg.Game;
import org.mtcg.Cards.Package;
import java.util.ArrayList;
import java.util.List;

public class Store {
    List<Package> packages;

    public Store() {
        this.packages = new ArrayList<>();
    }

    public void addPackage(Package p){
        this.packages.add(p);
    }
}
